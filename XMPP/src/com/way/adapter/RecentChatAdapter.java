package com.way.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;
import com.way.ui.view.CustomDialog;
import com.way.util.TimeUtil;
import com.way.util.XMPPHelper;
import com.way.xx.R;

public class RecentChatAdapter extends SimpleCursorAdapter {
	private static final String SELECT = ChatConstants.DATE
			+ " in (select max(" + ChatConstants.DATE + ") from "
			+ ChatProvider.TABLE_NAME + " group by " + ChatConstants.JID
			+ " having count(*)>0)";// 查询合并重复jid字段的所有聊天对象
	private static final String[] FROM = new String[] {
			ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE,
			ChatProvider.ChatConstants.DIRECTION,
			ChatProvider.ChatConstants.JID, ChatProvider.ChatConstants.MESSAGE,
			ChatProvider.ChatConstants.DELIVERY_STATUS };// 查询字段
	private static final String SORT_ORDER = ChatConstants.DATE + " DESC";
	private ContentResolver mContentResolver;
	private LayoutInflater mLayoutInflater;
	private Activity mContext;

	public RecentChatAdapter(Activity context) {
		super(context, 0, null, FROM, null);
		mContext = context;
		mContentResolver = context.getContentResolver();
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void requery() {
		Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI, FROM,
				SELECT, null, SORT_ORDER);
		Cursor oldCursor = getCursor();
		changeCursor(cursor);
		mContext.stopManagingCursor(oldCursor);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = this.getCursor();
		cursor.moveToPosition(position);
		long dateMilliseconds = cursor.getLong(cursor
				.getColumnIndex(ChatProvider.ChatConstants.DATE));
		String date = TimeUtil.getChatTime(dateMilliseconds);
		String message = cursor.getString(cursor
				.getColumnIndex(ChatProvider.ChatConstants.MESSAGE));
		String jid = cursor.getString(cursor
				.getColumnIndex(ChatProvider.ChatConstants.JID));

		String selection = ChatConstants.JID + " = '" + jid + "' AND "
				+ ChatConstants.DIRECTION + " = " + ChatConstants.INCOMING
				+ " AND " + ChatConstants.DELIVERY_STATUS + " = "
				+ ChatConstants.DS_NEW;// 新消息数量字段
		Cursor msgcursor = mContentResolver.query(ChatProvider.CONTENT_URI,
				new String[] { "count(" + ChatConstants.PACKET_ID + ")",
						ChatConstants.DATE, ChatConstants.MESSAGE }, selection,
				null, SORT_ORDER);
		msgcursor.moveToFirst();
		int count = msgcursor.getInt(0);
		ViewHolder viewHolder;
		if (convertView == null
				|| convertView.getTag(R.drawable.ic_launcher
						+ (int) dateMilliseconds) == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.recent_listview_item, parent, false);
			viewHolder = buildHolder(convertView, jid);
			convertView.setTag(R.drawable.ic_launcher + (int) dateMilliseconds,
					viewHolder);
			convertView.setTag(R.string.app_name, R.drawable.ic_launcher
					+ (int) dateMilliseconds);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
					+ (int) dateMilliseconds);
		}
		viewHolder.jidView.setText(XMPPHelper.splitJidAndServer(jid));
		viewHolder.msgView.setText(XMPPHelper
				.convertNormalStringToSpannableString(mContext, message, true));
		viewHolder.dataView.setText(date);

		if (msgcursor.getInt(0) > 0) {
			viewHolder.msgView.setText(msgcursor.getString(msgcursor
					.getColumnIndex(ChatConstants.MESSAGE)));
			viewHolder.dataView.setText(TimeUtil.getChatTime(msgcursor
					.getLong(msgcursor.getColumnIndex(ChatConstants.DATE))));
			viewHolder.unReadView.setText(msgcursor.getString(0));
		}
		viewHolder.unReadView.setVisibility(count > 0 ? View.VISIBLE
				: View.GONE);
		viewHolder.unReadView.bringToFront();
		msgcursor.close();

		return convertView;
	}

	private ViewHolder buildHolder(View convertView, final String jid) {
		ViewHolder holder = new ViewHolder();
		holder.jidView = (TextView) convertView
				.findViewById(R.id.recent_list_item_name);
		holder.dataView = (TextView) convertView
				.findViewById(R.id.recent_list_item_time);
		holder.msgView = (TextView) convertView
				.findViewById(R.id.recent_list_item_msg);
		holder.unReadView = (TextView) convertView.findViewById(R.id.unreadmsg);
		holder.deleteBtn = (Button) convertView
				.findViewById(R.id.recent_del_btn);
		holder.deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeChatHistoryDialog(jid, XMPPHelper.splitJidAndServer(jid));
			}
		});
		return holder;
	}

	private static class ViewHolder {
		TextView jidView;
		TextView dataView;
		TextView msgView;
		TextView unReadView;
		Button deleteBtn;
	}

	void removeChatHistory(final String JID) {
		mContentResolver.delete(ChatProvider.CONTENT_URI,
				ChatProvider.ChatConstants.JID + " = ?", new String[] { JID });
	}

	void removeChatHistoryDialog(final String JID, final String userName) {
		new CustomDialog.Builder(mContext)
				.setTitle(R.string.deleteChatHistory_title)
				.setMessage(
						mContext.getString(R.string.deleteChatHistory_text,
								userName, JID))
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								removeChatHistory(JID);
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).create().show();
	}
}
