package com.way.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.way.db.ChatProvider;
import com.way.db.ChatProvider.ChatConstants;
import com.way.util.L;
import com.way.util.PreferenceConstants;
import com.way.util.PreferenceUtils;
import com.way.util.TimeUtil;
import com.way.util.XMPPHelper;
import com.way.xx.R;

public class ChatAdapter extends SimpleCursorAdapter {

	private static final int DELAY_NEWMSG = 2000;
	private Context mContext;
	private LayoutInflater mInflater;

	public ChatAdapter(Context context, Cursor cursor, String[] from) {
		// super(context, android.R.layout.simple_list_item_1, cursor, from,
		// to);
		super(context, 0, cursor, from, null);
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = this.getCursor();
		cursor.moveToPosition(position);

		long dateMilliseconds = cursor.getLong(cursor
				.getColumnIndex(ChatProvider.ChatConstants.DATE));

		int _id = cursor.getInt(cursor
				.getColumnIndex(ChatProvider.ChatConstants._ID));
		String date = TimeUtil.getChatTime(dateMilliseconds);
		String message = cursor.getString(cursor
				.getColumnIndex(ChatProvider.ChatConstants.MESSAGE));
		int come = cursor.getInt(cursor
				.getColumnIndex(ChatProvider.ChatConstants.DIRECTION));// 消息来自
		boolean from_me = (come == ChatConstants.OUTGOING);
		String jid = cursor.getString(cursor
				.getColumnIndex(ChatProvider.ChatConstants.JID));
		int delivery_status = cursor.getInt(cursor
				.getColumnIndex(ChatProvider.ChatConstants.DELIVERY_STATUS));
		ViewHolder viewHolder;
		if (convertView == null
				|| convertView.getTag(R.drawable.ic_launcher + come) == null) {
			if (come == ChatConstants.OUTGOING) {
				convertView = mInflater.inflate(R.layout.chat_item_right,
						parent, false);
			} else {
				convertView = mInflater.inflate(R.layout.chat_item_left, null);
			}
			viewHolder = buildHolder(convertView);
			convertView.setTag(R.drawable.ic_launcher + come, viewHolder);
			convertView
					.setTag(R.string.app_name, R.drawable.ic_launcher + come);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
					+ come);
		}

		if (!from_me && delivery_status == ChatConstants.DS_NEW) {
			markAsReadDelayed(_id, DELAY_NEWMSG);
		}

		bindViewData(viewHolder, date, from_me, jid, message, delivery_status);
		return convertView;
	}

	private void markAsReadDelayed(final int id, int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				markAsRead(id);
			}
		}, delay);
	}

	/**
	 * 标记为已读消息
	 * 
	 * @param id
	 */
	private void markAsRead(int id) {
		Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
				+ ChatProvider.TABLE_NAME + "/" + id);
		L.d("markAsRead: " + rowuri);
		ContentValues values = new ContentValues();
		values.put(ChatConstants.DELIVERY_STATUS, ChatConstants.DS_SENT_OR_READ);
		mContext.getContentResolver().update(rowuri, values, null, null);
	}

	private void bindViewData(ViewHolder holder, String date, boolean from_me,
			String from, String message, int delivery_status) {
		holder.avatar.setBackgroundResource(R.drawable.login_default_avatar);
		if (from_me
				&& !PreferenceUtils.getPrefBoolean(mContext,
						PreferenceConstants.SHOW_MY_HEAD, true)) {
			holder.avatar.setVisibility(View.GONE);
		}
		holder.content.setText(message);
		holder.time.setText(date);

	}

	private ViewHolder buildHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.content = (TextView) convertView.findViewById(R.id.textView2);
		holder.time = (TextView) convertView.findViewById(R.id.datetime);
		holder.avatar = (ImageView) convertView.findViewById(R.id.icon);
		return holder;
	}

	private static class ViewHolder {
		TextView content;
		TextView time;
		ImageView avatar;

	}

}
