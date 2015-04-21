package com.way.smack;

import java.util.Collection;
import java.util.Date;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;
import com.way.db.RosterProvider;
import com.way.db.RosterProvider.RosterConstants;
import com.way.exception.XXException;
import com.way.service.XXService;
import com.way.util.L;
import com.way.util.PreferenceConstants;
import com.way.util.PreferenceUtils;
import com.way.util.StatusMode;
import com.way.xx.R;

public class SmackImpl implements Smack {
	// 客户端名称和类型。主要是向服务器登记，有点类似QQ显示iphone或者Android手机在线的功能
	public static final String XMPP_IDENTITY_NAME = "XMPP";// 客户端名称
	public static final String XMPP_IDENTITY_TYPE = "phone";// 客户端类型

	private static final int PACKET_TIMEOUT = 30000;// 超时时间
	// 发送离线消息的字段
	final static private String[] SEND_OFFLINE_PROJECTION = new String[] {
			ChatConstants._ID, ChatConstants.JID, ChatConstants.MESSAGE,
			ChatConstants.DATE, ChatConstants.PACKET_ID };
	// 发送离线消息的搜索数据库条件，自己发出去的OUTGOING，并且状态为DS_NEW
	final static private String SEND_OFFLINE_SELECTION = ChatConstants.DIRECTION
			+ " = "
			+ ChatConstants.OUTGOING
			+ " AND "
			+ ChatConstants.DELIVERY_STATUS + " = " + ChatConstants.DS_NEW;

	static {
		registerSmackProviders();
	}

	// 做一些基本的配置
	static void registerSmackProviders() {
		ProviderManager pm = ProviderManager.getInstance();
		// add IQ handling
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// add delayed delivery notifications
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
		// add carbons and forwarding
		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
				new Forwarded.Provider());
		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
		pm.addExtensionProvider("received", Carbon.NAMESPACE,
				new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());

		ServiceDiscoveryManager.setIdentityName(XMPP_IDENTITY_NAME);
		ServiceDiscoveryManager.setIdentityType(XMPP_IDENTITY_TYPE);
	}

	private ConnectionConfiguration mXMPPConfig;// 连接配置
	private XMPPConnection mXMPPConnection;// 连接对象
	private XXService mService;// 主服务
	private Roster mRoster;// 联系人对象
	private final ContentResolver mContentResolver;// 数据库操作对象

	private RosterListener mRosterListener;// 联系人动态监听
	private PacketListener mPacketListener;// 消息动态监听
	private PacketListener mSendFailureListener;// 消息发送失败动态监听
	private PacketListener mPongListener;// ping pong服务器动态监听

	// ping-pong服务器
	private String mPingID;// ping服务器的id
	private long mPingTimestamp;// 时间戳
	private PendingIntent mPingAlarmPendIntent;// 是通过闹钟来控制ping服务器的时间间隔
	private PendingIntent mPongTimeoutAlarmPendIntent;// 判断服务器连接超时的闹钟
	private static final String PING_ALARM = "com.way.xx.PING_ALARM";// ping服务器闹钟BroadcastReceiver的Action
	private static final String PONG_TIMEOUT_ALARM = "com.way.xx.PONG_TIMEOUT_ALARM";// 判断连接超时的闹钟BroadcastReceiver的Action
	private Intent mPingAlarmIntent = new Intent(PING_ALARM);
	private Intent mPongTimeoutAlarmIntent = new Intent(PONG_TIMEOUT_ALARM);
	private PongTimeoutAlarmReceiver mPongTimeoutAlarmReceiver = new PongTimeoutAlarmReceiver();
	private BroadcastReceiver mPingAlarmReceiver = new PingAlarmReceiver();

	// ping-pong服务器

	public SmackImpl(XXService service) {
		String customServer = PreferenceUtils.getPrefString(service,
				PreferenceConstants.CUSTOM_SERVER, "");// 用户手动设置的服务器名称，本来打算给用户指定服务器的
		int port = PreferenceUtils.getPrefInt(service,
				PreferenceConstants.PORT, PreferenceConstants.DEFAULT_PORT_INT);// 端口号，也是留给用户手动设置的
		String server = PreferenceUtils.getPrefString(service,
				PreferenceConstants.Server, PreferenceConstants.GMAIL_SERVER);// 默认的服务器，即谷歌服务器
		boolean smackdebug = PreferenceUtils.getPrefBoolean(service,
				PreferenceConstants.SMACKDEBUG, false);// 是否需要smack debug
		boolean requireSsl = PreferenceUtils.getPrefBoolean(service,
				PreferenceConstants.REQUIRE_TLS, false);// 是否需要ssl安全配置
		if (customServer.length() > 0
				|| port != PreferenceConstants.DEFAULT_PORT_INT)
			this.mXMPPConfig = new ConnectionConfiguration(customServer, port,
					server);
		else
			this.mXMPPConfig = new ConnectionConfiguration(server); // use SRV

		this.mXMPPConfig.setReconnectionAllowed(false);
		this.mXMPPConfig.setSendPresence(false);
		this.mXMPPConfig.setCompressionEnabled(false); // disable for now
		this.mXMPPConfig.setDebuggerEnabled(smackdebug);
		if (requireSsl)
			this.mXMPPConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
		this.mService = service;
		mContentResolver = service.getContentResolver();
	}

	@Override
	public boolean login(String account, String password) throws XXException {// 登陆实现
		try {
			if (mXMPPConnection.isConnected()) {// 首先判断是否还连接着服务器，需要先断开
				try {
					mXMPPConnection.disconnect();
				} catch (Exception e) {
					L.d("conn.disconnect() failed: " + e);
				}
			}
			SmackConfiguration.setPacketReplyTimeout(PACKET_TIMEOUT);// 设置超时时间
			SmackConfiguration.setKeepAliveInterval(-1);
			SmackConfiguration.setDefaultPingInterval(0);
			registerRosterListener();// 监听联系人动态变化
			mXMPPConnection.connect();
			if (!mXMPPConnection.isConnected()) {
				throw new XXException("SMACK connect failed without exception!");
			}
			mXMPPConnection.addConnectionListener(new ConnectionListener() {
				public void connectionClosedOnError(Exception e) {
					mService.postConnectionFailed(e.getMessage());// 连接关闭时，动态反馈给服务
				}

				public void connectionClosed() {
				}

				public void reconnectingIn(int seconds) {
				}

				public void reconnectionFailed(Exception e) {
				}

				public void reconnectionSuccessful() {
				}
			});
			initServiceDiscovery();// 与服务器交互消息监听,发送消息需要回执，判断是否发送成功
			// SMACK auto-logins if we were authenticated before
			if (!mXMPPConnection.isAuthenticated()) {
				String ressource = PreferenceUtils.getPrefString(mService,
						PreferenceConstants.RESSOURCE, XMPP_IDENTITY_NAME);
				mXMPPConnection.login(account, password, ressource);
			}
			setStatusFromConfig();// 更新在线状态

		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage(),
					e.getWrappedThrowable());
		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			L.e(SmackImpl.class, "login(): " + Log.getStackTraceString(e));
			throw new XXException(e.getLocalizedMessage(), e.getCause());
		}
		registerAllListener();// 注册监听其他的事件，比如新消息
		return mXMPPConnection.isAuthenticated();
	}

	/**
	 * 注册所有的监听
	 */
	private void registerAllListener() {
		// actually, authenticated must be true now, or an exception must have
		// been thrown.
		if (isAuthenticated()) {
			registerMessageListener();// 注册新消息监听
			registerMessageSendFailureListener();// 注册消息发送失败监听
			registerPongListener();// 注册服务器回应ping消息监听
			sendOfflineMessages();// 发送离线消息
			if (mService == null) {
				mXMPPConnection.disconnect();
				return;
			}
			// we need to "ping" the service to let it know we are actually
			// connected, even when no roster entries will come in
			mService.rosterChanged();
		}
	}

	/************ start 新消息处理 ********************/
	private void registerMessageListener() {
		// do not register multiple packet listeners
		if (mPacketListener != null)
			mXMPPConnection.removePacketListener(mPacketListener);

		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mPacketListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					if (packet instanceof Message) {// 如果是消息类型
						Message msg = (Message) packet;
						String chatMessage = msg.getBody();

						// try to extract a carbon
						Carbon cc = CarbonManager.getCarbon(msg);
						if (cc != null
								&& cc.getDirection() == Carbon.Direction.received) {// 收到的消息
							L.d("carbon: " + cc.toXML());
							msg = (Message) cc.getForwarded()
									.getForwardedPacket();
							chatMessage = msg.getBody();
							// fall through
						} else if (cc != null
								&& cc.getDirection() == Carbon.Direction.sent) {// 如果是自己发送的消息，则添加到数据库后直接返回
							L.d("carbon: " + cc.toXML());
							msg = (Message) cc.getForwarded()
									.getForwardedPacket();
							chatMessage = msg.getBody();
							if (chatMessage == null)
								return;
							String fromJID = getJabberID(msg.getTo());

							addChatMessageToDB(ChatConstants.OUTGOING, fromJID,
									chatMessage, ChatConstants.DS_SENT_OR_READ,
									System.currentTimeMillis(),
									msg.getPacketID());
							// always return after adding
							return;// 记得要返回
						}

						if (chatMessage == null) {
							return;// 如果消息为空，直接返回了
						}

						if (msg.getType() == Message.Type.error) {
							chatMessage = "<Error> " + chatMessage;// 错误的消息类型
						}

						long ts;// 消息时间戳
						DelayInfo timestamp = (DelayInfo) msg.getExtension(
								"delay", "urn:xmpp:delay");
						if (timestamp == null)
							timestamp = (DelayInfo) msg.getExtension("x",
									"jabber:x:delay");
						if (timestamp != null)
							ts = timestamp.getStamp().getTime();
						else
							ts = System.currentTimeMillis();

						String fromJID = getJabberID(msg.getFrom());// 消息来自对象

						addChatMessageToDB(ChatConstants.INCOMING, fromJID,
								chatMessage, ChatConstants.DS_NEW, ts,
								msg.getPacketID());// 存入数据库，并标记为新消息DS_NEW
						mService.newMessage(fromJID, chatMessage);// 通知service，处理是否需要显示通知栏，
					}
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					L.e("failed to process packet:");
					e.printStackTrace();
				}
			}
		};

		mXMPPConnection.addPacketListener(mPacketListener, filter);// 这是最关健的了，少了这句，前面的都是白费功夫
	}

	/**
	 * 将消息添加到数据库
	 * 
	 * @param direction
	 *            是否为收到的消息INCOMING为收到，OUTGOING为自己发出
	 * @param JID
	 *            此消息对应的jid
	 * @param message
	 *            消息内容
	 * @param delivery_status
	 *            消息状态 DS_NEW为新消息，DS_SENT_OR_READ为自己发出或者已读的消息
	 * @param ts
	 *            消息时间戳
	 * @param packetID
	 *            服务器为了区分每一条消息生成的消息包的id
	 */
	private void addChatMessageToDB(int direction, String JID, String message,
			int delivery_status, long ts, String packetID) {
		ContentValues values = new ContentValues();

		values.put(ChatConstants.DIRECTION, direction);
		values.put(ChatConstants.JID, JID);
		values.put(ChatConstants.MESSAGE, message);
		values.put(ChatConstants.DELIVERY_STATUS, delivery_status);
		values.put(ChatConstants.DATE, ts);
		values.put(ChatConstants.PACKET_ID, packetID);

		mContentResolver.insert(ChatProvider.CONTENT_URI, values);
	}

	/************ end 新消息处理 ********************/

	/***************** start 处理消息发送失败状态 ***********************/
	private void registerMessageSendFailureListener() {
		// do not register multiple packet listeners
		if (mSendFailureListener != null)
			mXMPPConnection
					.removePacketSendFailureListener(mSendFailureListener);

		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mSendFailureListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					if (packet instanceof Message) {
						Message msg = (Message) packet;
						String chatMessage = msg.getBody();

						Log.d("SmackableImp",
								"message "
										+ chatMessage
										+ " could not be sent (ID:"
										+ (msg.getPacketID() == null ? "null"
												: msg.getPacketID()) + ")");
						changeMessageDeliveryStatus(msg.getPacketID(),
								ChatConstants.DS_NEW);// 当消息发送失败时，将此消息标记为新消息，下次再发送
					}
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					L.e("failed to process packet:");
					e.printStackTrace();
				}
			}
		};

		mXMPPConnection.addPacketSendFailureListener(mSendFailureListener,
				filter);// 这句也是关键啦！
	}

	/**
	 * 改变消息状态
	 * 
	 * @param packetID
	 *            消息的id
	 * @param new_status
	 *            新状态类型
	 */
	public void changeMessageDeliveryStatus(String packetID, int new_status) {
		ContentValues cv = new ContentValues();
		cv.put(ChatConstants.DELIVERY_STATUS, new_status);
		Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
				+ ChatProvider.TABLE_NAME);
		mContentResolver.update(rowuri, cv, ChatConstants.PACKET_ID
				+ " = ? AND " + ChatConstants.DIRECTION + " = "
				+ ChatConstants.OUTGOING, new String[] { packetID });
	}

	/***************** end 处理消息发送失败状态 ***********************/

	/***************** start 处理ping服务器消息 ***********************/
	private void registerPongListener() {
		// reset ping expectation on new connection
		mPingID = null;// 初始化ping的id

		if (mPongListener != null)
			mXMPPConnection.removePacketListener(mPongListener);// 先移除之前监听对象

		mPongListener = new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				if (packet == null)
					return;

				if (packet.getPacketID().equals(mPingID)) {// 如果服务器返回的消息为ping服务器时的消息，说明没有掉线
					L.i(String.format(
							"Ping: server latency %1.3fs",
							(System.currentTimeMillis() - mPingTimestamp) / 1000.));
					mPingID = null;
					((AlarmManager) mService
							.getSystemService(Context.ALARM_SERVICE))
							.cancel(mPongTimeoutAlarmPendIntent);// 取消超时闹钟
				}
			}

		};

		mXMPPConnection.addPacketListener(mPongListener, new PacketTypeFilter(
				IQ.class));// 正式开始监听
		mPingAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPingAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);// 定时ping服务器，以此来确定是否掉线
		mPongTimeoutAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPongTimeoutAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);// 超时闹钟
		mService.registerReceiver(mPingAlarmReceiver, new IntentFilter(
				PING_ALARM));// 注册定时ping服务器广播接收者
		mService.registerReceiver(mPongTimeoutAlarmReceiver, new IntentFilter(
				PONG_TIMEOUT_ALARM));// 注册连接超时广播接收者
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis()
								+ AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						mPingAlarmPendIntent);// 15分钟ping以此服务器
	}

	/**
	 * BroadcastReceiver to trigger reconnect on pong timeout.
	 */
	private class PongTimeoutAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			L.d("Ping: timeout for " + mPingID);
			mService.postConnectionFailed(XXService.PONG_TIMEOUT);
			logout();// 超时就断开连接
		}
	}

	/**
	 * BroadcastReceiver to trigger sending pings to the server
	 */
	private class PingAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			if (mXMPPConnection.isAuthenticated()) {
				sendServerPing();// 收到ping服务器的闹钟，即ping一下服务器
			} else
				L.d("Ping: alarm received, but not connected to server.");
		}
	}

	/***************** end 处理ping服务器消息 ***********************/

	/***************** start 发送离线消息 ***********************/
	public void sendOfflineMessages() {
		Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI,
				SEND_OFFLINE_PROJECTION, SEND_OFFLINE_SELECTION, null, null);// 查询数据库获取离线消息游标
		final int _ID_COL = cursor.getColumnIndexOrThrow(ChatConstants._ID);
		final int JID_COL = cursor.getColumnIndexOrThrow(ChatConstants.JID);
		final int MSG_COL = cursor.getColumnIndexOrThrow(ChatConstants.MESSAGE);
		final int TS_COL = cursor.getColumnIndexOrThrow(ChatConstants.DATE);
		final int PACKETID_COL = cursor
				.getColumnIndexOrThrow(ChatConstants.PACKET_ID);
		ContentValues mark_sent = new ContentValues();
		mark_sent.put(ChatConstants.DELIVERY_STATUS,
				ChatConstants.DS_SENT_OR_READ);
		while (cursor.moveToNext()) {// 遍历之后将离线消息发出
			int _id = cursor.getInt(_ID_COL);
			String toJID = cursor.getString(JID_COL);
			String message = cursor.getString(MSG_COL);
			String packetID = cursor.getString(PACKETID_COL);
			long ts = cursor.getLong(TS_COL);
			L.d("sendOfflineMessages: " + toJID + " > " + message);
			final Message newMessage = new Message(toJID, Message.Type.chat);
			newMessage.setBody(message);
			DelayInformation delay = new DelayInformation(new Date(ts));
			newMessage.addExtension(delay);
			newMessage.addExtension(new DelayInfo(delay));
			newMessage.addExtension(new DeliveryReceiptRequest());
			if ((packetID != null) && (packetID.length() > 0)) {
				newMessage.setPacketID(packetID);
			} else {
				packetID = newMessage.getPacketID();
				mark_sent.put(ChatConstants.PACKET_ID, packetID);
			}
			Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
					+ ChatProvider.TABLE_NAME + "/" + _id);
			// 将消息标记为已发送再调用发送，因为，假设此消息又未发送成功，有SendFailListener重新标记消息
			mContentResolver.update(rowuri, mark_sent, null, null);
			mXMPPConnection.sendPacket(newMessage); // must be after marking
													// delivered, otherwise it
													// may override the
													// SendFailListener
		}
		cursor.close();
	}

	/**
	 * 作为离线消息存储起来，当自己掉线时调用
	 * 
	 * @param cr
	 * @param toJID
	 * @param message
	 */
	public static void saveAsOfflineMessage(ContentResolver cr, String toJID,
			String message) {
		ContentValues values = new ContentValues();
		values.put(ChatConstants.DIRECTION, ChatConstants.OUTGOING);
		values.put(ChatConstants.JID, toJID);
		values.put(ChatConstants.MESSAGE, message);
		values.put(ChatConstants.DELIVERY_STATUS, ChatConstants.DS_NEW);
		values.put(ChatConstants.DATE, System.currentTimeMillis());

		cr.insert(ChatProvider.CONTENT_URI, values);
	}

	/***************** end 发送离线消息 ***********************/
	/******************************* start 联系人数据库事件处理 **********************************/
	private void registerRosterListener() {
		mRoster = mXMPPConnection.getRoster();
		mRosterListener = new RosterListener() {
			private boolean isFristRoter;

			@Override
			public void presenceChanged(Presence presence) {// 联系人状态改变，比如在线或离开、隐身之类
				L.i("presenceChanged(" + presence.getFrom() + "): " + presence);
				String jabberID = getJabberID(presence.getFrom());
				RosterEntry rosterEntry = mRoster.getEntry(jabberID);
				updateRosterEntryInDB(rosterEntry);// 更新联系人数据库
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesUpdated(Collection<String> entries) {// 更新数据库，第一次登陆
				// TODO Auto-generated method stub
				L.i("entriesUpdated(" + entries + ")");
				for (String entry : entries) {
					RosterEntry rosterEntry = mRoster.getEntry(entry);
					updateRosterEntryInDB(rosterEntry);
				}
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesDeleted(Collection<String> entries) {// 有好友删除时，
				L.i("entriesDeleted(" + entries + ")");
				for (String entry : entries) {
					deleteRosterEntryFromDB(entry);
				}
				mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
			}

			@Override
			public void entriesAdded(Collection<String> entries) {// 有人添加好友时，我这里没有弹出对话框确认，直接添加到数据库
				L.i("entriesAdded(" + entries + ")");
				ContentValues[] cvs = new ContentValues[entries.size()];
				int i = 0;
				for (String entry : entries) {
					RosterEntry rosterEntry = mRoster.getEntry(entry);
					cvs[i++] = getContentValuesForRosterEntry(rosterEntry);
				}
				mContentResolver.bulkInsert(RosterProvider.CONTENT_URI, cvs);
				if (isFristRoter) {
					isFristRoter = false;
					mService.rosterChanged();// 回调通知服务，主要是用来判断一下是否掉线
				}
			}
		};
		mRoster.addRosterListener(mRosterListener);
	}

	private String getJabberID(String from) {
		String[] res = from.split("/");
		return res[0].toLowerCase();
	}

	/**
	 * 更新联系人数据库
	 * 
	 * @param entry
	 *            联系人RosterEntry对象
	 */
	private void updateRosterEntryInDB(final RosterEntry entry) {
		final ContentValues values = getContentValuesForRosterEntry(entry);

		if (mContentResolver.update(RosterProvider.CONTENT_URI, values,
				RosterConstants.JID + " = ?", new String[] { entry.getUser() }) == 0)// 如果数据库无此好友
			addRosterEntryToDB(entry);// 则添加到数据库
	}

	/**
	 * 添加到数据库
	 * 
	 * @param entry
	 *            联系人RosterEntry对象
	 */
	private void addRosterEntryToDB(final RosterEntry entry) {
		ContentValues values = getContentValuesForRosterEntry(entry);
		Uri uri = mContentResolver.insert(RosterProvider.CONTENT_URI, values);
		L.i("addRosterEntryToDB: Inserted " + uri);
	}

	/**
	 * 将联系人从数据库中删除
	 * 
	 * @param jabberID
	 */
	private void deleteRosterEntryFromDB(final String jabberID) {
		int count = mContentResolver.delete(RosterProvider.CONTENT_URI,
				RosterConstants.JID + " = ?", new String[] { jabberID });
		L.i("deleteRosterEntryFromDB: Deleted " + count + " entries");
	}

	/**
	 * 将联系人RosterEntry转化成ContentValues，方便存储数据库
	 * 
	 * @param entry
	 * @return
	 */
	private ContentValues getContentValuesForRosterEntry(final RosterEntry entry) {
		final ContentValues values = new ContentValues();

		values.put(RosterConstants.JID, entry.getUser());
		values.put(RosterConstants.ALIAS, getName(entry));

		Presence presence = mRoster.getPresence(entry.getUser());
		values.put(RosterConstants.STATUS_MODE, getStatusInt(presence));
		values.put(RosterConstants.STATUS_MESSAGE, presence.getStatus());
		values.put(RosterConstants.GROUP, getGroup(entry.getGroups()));

		return values;
	}

	/**
	 * 遍历获取组名
	 * 
	 * @param groups
	 * @return
	 */
	private String getGroup(Collection<RosterGroup> groups) {
		for (RosterGroup group : groups) {
			return group.getName();
		}
		return "";
	}

	/**
	 * 获取联系人名称
	 * 
	 * @param rosterEntry
	 * @return
	 */
	private String getName(RosterEntry rosterEntry) {
		String name = rosterEntry.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		name = StringUtils.parseName(rosterEntry.getUser());
		if (name.length() > 0) {
			return name;
		}
		return rosterEntry.getUser();
	}

	/**
	 * 获取状态
	 * 
	 * @param presence
	 * @return
	 */
	private StatusMode getStatus(Presence presence) {
		if (presence.getType() == Presence.Type.available) {
			if (presence.getMode() != null) {
				return StatusMode.valueOf(presence.getMode().name());
			}
			return StatusMode.available;
		}
		return StatusMode.offline;
	}

	private int getStatusInt(final Presence presence) {
		return getStatus(presence).ordinal();
	}

	/******************************* end 联系人数据库事件处理 **********************************/

	/**
	 * 与服务器交互消息监听,发送消息需要回执，判断对方是否已读此消息
	 */
	private void initServiceDiscovery() {
		// register connection features
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(mXMPPConnection);
		if (sdm == null)
			sdm = new ServiceDiscoveryManager(mXMPPConnection);

		sdm.addFeature("http://jabber.org/protocol/disco#info");

		// reference PingManager, set ping flood protection to 10s
		PingManager.getInstanceFor(mXMPPConnection).setPingMinimumInterval(
				10 * 1000);
		// reference DeliveryReceiptManager, add listener

		DeliveryReceiptManager dm = DeliveryReceiptManager
				.getInstanceFor(mXMPPConnection);
		dm.enableAutoReceipts();
		dm.registerReceiptReceivedListener(new DeliveryReceiptManager.ReceiptReceivedListener() {
			public void onReceiptReceived(String fromJid, String toJid,
					String receiptId) {
				L.d(SmackImpl.class, "got delivery receipt for " + receiptId);
				changeMessageDeliveryStatus(receiptId, ChatConstants.DS_ACKED);// 标记为对方已读，实际上遇到了点问题，所以其实没有用上此状态
			}
		});
	}

	@Override
	public void setStatusFromConfig() {// 设置自己的当前状态，供外部服务调用
		boolean messageCarbons = PreferenceUtils.getPrefBoolean(mService,
				PreferenceConstants.MESSAGE_CARBONS, true);
		String statusMode = PreferenceUtils.getPrefString(mService,
				PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE);
		String statusMessage = PreferenceUtils.getPrefString(mService,
				PreferenceConstants.STATUS_MESSAGE,
				mService.getString(R.string.status_online));
		int priority = PreferenceUtils.getPrefInt(mService,
				PreferenceConstants.PRIORITY, 0);
		if (messageCarbons)
			CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(
					true);

		Presence presence = new Presence(Presence.Type.available);
		Mode mode = Mode.valueOf(statusMode);
		presence.setMode(mode);
		presence.setStatus(statusMessage);
		presence.setPriority(priority);
		mXMPPConnection.sendPacket(presence);
	}

	@Override
	public boolean isAuthenticated() {// 是否与服务器连接上，供本类和外部服务调用
		if (mXMPPConnection != null) {
			return (mXMPPConnection.isConnected() && mXMPPConnection
					.isAuthenticated());
		}
		return false;
	}

	@Override
	public void addRosterItem(String user, String alias, String group)
			throws XXException {// 添加联系人，供外部服务调用
		addRosterEntry(user, alias, group);
	}

	private void addRosterEntry(String user, String alias, String group)
			throws XXException {
		mRoster = mXMPPConnection.getRoster();
		try {
			mRoster.createEntry(user, alias, new String[] { group });
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage());
		}
	}

	@Override
	public void removeRosterItem(String user) throws XXException {// 删除联系人，供外部服务调用
		// TODO Auto-generated method stub
		L.d("removeRosterItem(" + user + ")");

		removeRosterEntry(user);
		mService.rosterChanged();
	}

	private void removeRosterEntry(String user) throws XXException {
		mRoster = mXMPPConnection.getRoster();
		try {
			RosterEntry rosterEntry = mRoster.getEntry(user);

			if (rosterEntry != null) {
				mRoster.removeEntry(rosterEntry);
			}
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage());
		}
	}

	@Override
	public void renameRosterItem(String user, String newName)
			throws XXException {// 重命名联系人，供外部服务调用
		// TODO Auto-generated method stub
		mRoster = mXMPPConnection.getRoster();
		RosterEntry rosterEntry = mRoster.getEntry(user);

		if (!(newName.length() > 0) || (rosterEntry == null)) {
			throw new XXException("JabberID to rename is invalid!");
		}
		rosterEntry.setName(newName);
	}

	@Override
	public void moveRosterItemToGroup(String user, String group)
			throws XXException {// 移动好友到其他分组，供外部服务调用
		// TODO Auto-generated method stub
		tryToMoveRosterEntryToGroup(user, group);
	}

	private void tryToMoveRosterEntryToGroup(String userName, String groupName)
			throws XXException {

		mRoster = mXMPPConnection.getRoster();
		RosterGroup rosterGroup = getRosterGroup(groupName);
		RosterEntry rosterEntry = mRoster.getEntry(userName);

		removeRosterEntryFromGroups(rosterEntry);

		if (groupName.length() == 0)
			return;
		else {
			try {
				rosterGroup.addEntry(rosterEntry);
			} catch (XMPPException e) {
				throw new XXException(e.getLocalizedMessage());
			}
		}
	}

	private void removeRosterEntryFromGroups(RosterEntry rosterEntry)
			throws XXException {// 从对应组中删除联系人，供外部服务调用
		Collection<RosterGroup> oldGroups = rosterEntry.getGroups();

		for (RosterGroup group : oldGroups) {
			tryToRemoveUserFromGroup(group, rosterEntry);
		}
	}

	private void tryToRemoveUserFromGroup(RosterGroup group,
			RosterEntry rosterEntry) throws XXException {
		try {
			group.removeEntry(rosterEntry);
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage());
		}
	}

	private RosterGroup getRosterGroup(String groupName) {// 获取联系人分组
		RosterGroup rosterGroup = mRoster.getGroup(groupName);

		// create group if unknown
		if ((groupName.length() > 0) && rosterGroup == null) {
			rosterGroup = mRoster.createGroup(groupName);
		}
		return rosterGroup;

	}

	@Override
	public void renameRosterGroup(String group, String newGroup) {// 重命名分组
		// TODO Auto-generated method stub
		L.i("oldgroup=" + group + ", newgroup=" + newGroup);
		mRoster = mXMPPConnection.getRoster();
		RosterGroup groupToRename = mRoster.getGroup(group);
		if (groupToRename == null) {
			return;
		}
		groupToRename.setName(newGroup);
	}

	@Override
	public void requestAuthorizationForRosterItem(String user) {// 重新向对方发出添加好友申请
		// TODO Auto-generated method stub
		Presence response = new Presence(Presence.Type.subscribe);
		response.setTo(user);
		mXMPPConnection.sendPacket(response);
	}

	@Override
	public void addRosterGroup(String group) {// 增加联系人组
		// TODO Auto-generated method stub
		mRoster = mXMPPConnection.getRoster();
		mRoster.createGroup(group);
	}

	@Override
	public void sendMessage(String toJID, String message) {// 发送消息
		// TODO Auto-generated method stub
		final Message newMessage = new Message(toJID, Message.Type.chat);
		newMessage.setBody(message);
		newMessage.addExtension(new DeliveryReceiptRequest());
		if (isAuthenticated()) {
			addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
					ChatConstants.DS_SENT_OR_READ, System.currentTimeMillis(),
					newMessage.getPacketID());
			mXMPPConnection.sendPacket(newMessage);
		} else {
			// send offline -> store to DB
			addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
					ChatConstants.DS_NEW, System.currentTimeMillis(),
					newMessage.getPacketID());
		}
	}

	@Override
	public void sendServerPing() {
		if (mPingID != null) {// 此时说明上一次ping服务器还未回应，直接返回，直到连接超时
			L.d("Ping: requested, but still waiting for " + mPingID);
			return; // a ping is still on its way
		}
		Ping ping = new Ping();
		ping.setType(Type.GET);
		ping.setTo(PreferenceUtils.getPrefString(mService,
				PreferenceConstants.Server, PreferenceConstants.GMAIL_SERVER));
		mPingID = ping.getPacketID();// 此id其实是随机生成，但是唯一的
		mPingTimestamp = System.currentTimeMillis();
		L.d("Ping: sending ping " + mPingID);
		mXMPPConnection.sendPacket(ping);// 发送ping消息

		// register ping timeout handler: PACKET_TIMEOUT(30s) + 3s
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ PACKET_TIMEOUT + 3000, mPongTimeoutAlarmPendIntent);// 此时需要启动超时判断的闹钟了，时间间隔为30+3秒
	}

	@Override
	public String getNameForJID(String jid) {
		if (null != this.mRoster.getEntry(jid)
				&& null != this.mRoster.getEntry(jid).getName()
				&& this.mRoster.getEntry(jid).getName().length() > 0) {
			return this.mRoster.getEntry(jid).getName();
		} else {
			return jid;
		}
	}

	@Override
	public boolean logout() {// 注销登录
		L.d("unRegisterCallback()");
		// remove callbacks _before_ tossing old connection
		try {
			mXMPPConnection.getRoster().removeRosterListener(mRosterListener);
			mXMPPConnection.removePacketListener(mPacketListener);
			mXMPPConnection
					.removePacketSendFailureListener(mSendFailureListener);
			mXMPPConnection.removePacketListener(mPongListener);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPingAlarmPendIntent);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPongTimeoutAlarmPendIntent);
			mService.unregisterReceiver(mPingAlarmReceiver);
			mService.unregisterReceiver(mPongTimeoutAlarmReceiver);
		} catch (Exception e) {
			// ignore it!
			return false;
		}
		if (mXMPPConnection.isConnected()) {
			// work around SMACK's #%&%# blocking disconnect()
			new Thread() {
				public void run() {
					L.d("shutDown thread started");
					mXMPPConnection.disconnect();
					L.d("shutDown thread finished");
				}
			}.start();
		}
//		setStatusOffline();
		this.mService = null;
		return true;
	}

	/**
	 * 将所有联系人标记为离线状态
	 */
	public void setStatusOffline() {
		ContentValues values = new ContentValues();
		values.put(RosterConstants.STATUS_MODE, StatusMode.offline.ordinal());
		mContentResolver.update(RosterProvider.CONTENT_URI, values, null, null);
	}
}
