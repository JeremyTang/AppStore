package com.way.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.way.db.RosterProvider;
import com.way.db.RosterProvider.RosterConstants;
import com.way.ui.iphonetreeview.IphoneTreeView;
import com.way.ui.iphonetreeview.IphoneTreeView.IphoneTreeHeaderAdapter;
import com.way.ui.pulltorefresh.PullToRefreshScrollView;
import com.way.ui.pulltorefresh.PullToRefreshBase.Mode;
import com.way.util.L;
import com.way.util.PreferenceConstants;
import com.way.util.PreferenceUtils;
import com.way.util.StatusMode;
import com.way.xx.R;

public class RosterAdapter extends BaseExpandableListAdapter implements
		IphoneTreeHeaderAdapter {
	// 不在线状态
	private static final String OFFLINE_EXCLUSION = RosterConstants.STATUS_MODE
			+ " != " + StatusMode.offline.ordinal();
	// 在线人数
	private static final String COUNT_AVAILABLE_MEMBERS = "SELECT COUNT() FROM "
			+ RosterProvider.TABLE_ROSTER
			+ " inner_query"
			+ " WHERE inner_query."
			+ RosterConstants.GROUP
			+ " = "
			+ RosterProvider.QUERY_ALIAS
			+ "."
			+ RosterConstants.GROUP
			+ " AND inner_query." + OFFLINE_EXCLUSION;
	// 总人数
	private static final String COUNT_MEMBERS = "SELECT COUNT() FROM "
			+ RosterProvider.TABLE_ROSTER + " inner_query"
			+ " WHERE inner_query." + RosterConstants.GROUP + " = "
			+ RosterProvider.QUERY_ALIAS + "." + RosterConstants.GROUP;
	private static final String[] GROUPS_QUERY_COUNTED = new String[] {
			RosterConstants._ID,
			RosterConstants.GROUP,
			"(" + COUNT_AVAILABLE_MEMBERS + ") || '/' || (" + COUNT_MEMBERS
					+ ") AS members" };
	// 联系人查询序列
	private static final String[] ROSTER_QUERY = new String[] {
			RosterConstants._ID, RosterConstants.JID, RosterConstants.ALIAS,
			RosterConstants.STATUS_MODE, RosterConstants.STATUS_MESSAGE, };
	private Context mContext;
	private ContentResolver mContentResolver;
	private List<Group> mGroupList;
	private boolean mIsShowOffline;// 是否显示离线联系人
	private LayoutInflater mInflater;
	private IphoneTreeView mIphoneTreeView;
	private HashMap<Integer, Integer> groupStatusMap;
	private PullToRefreshScrollView mPullToRefreshScrollView;

	public RosterAdapter(Context context, IphoneTreeView iphoneTreeView,
			PullToRefreshScrollView pullToRefreshScrollView) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mIphoneTreeView = iphoneTreeView;
		mPullToRefreshScrollView = pullToRefreshScrollView;
		mInflater = LayoutInflater.from(context);
		mContentResolver = context.getContentResolver();
		mGroupList = new ArrayList<Group>();
		groupStatusMap = new HashMap<Integer, Integer>();
		mIsShowOffline = PreferenceUtils.getPrefBoolean(mContext,
				PreferenceConstants.SHOW_OFFLINE, true);
	}

	public void requery() {
		if (mGroupList != null && mGroupList.size() > 0)
			mGroupList.clear();
		// 是否显示在线人数
		mIsShowOffline = PreferenceUtils.getPrefBoolean(mContext,
				PreferenceConstants.SHOW_OFFLINE, true);
		String selectWhere = null;
		if (!mIsShowOffline)
			selectWhere = OFFLINE_EXCLUSION;
		Cursor groupCursor = mContentResolver.query(RosterProvider.GROUPS_URI,
				GROUPS_QUERY_COUNTED, selectWhere, null, RosterConstants.GROUP);
		groupCursor.moveToFirst();
		while (!groupCursor.isAfterLast()) {
			Group group = new Group();
			group.setGroupName(groupCursor.getString(groupCursor
					.getColumnIndex(RosterConstants.GROUP)));
			group.setMembers(groupCursor.getString(groupCursor
					.getColumnIndex("members")));
			mGroupList.add(group);
			groupCursor.moveToNext();
		}
		groupCursor.close();
		L.i("cursor size = " + mGroupList.size());
		notifyDataSetChanged();
	}

	protected List<Roster> getChildrenRosters(String groupname) {
		// Given the group, we return a cursor for all the children within
		// that group
		List<Roster> childList = new ArrayList<Roster>();

		String selectWhere = RosterConstants.GROUP + " = ?";
		if (!mIsShowOffline)
			selectWhere += " AND " + OFFLINE_EXCLUSION;
		Cursor childCursor = mContentResolver.query(RosterProvider.CONTENT_URI,
				ROSTER_QUERY, selectWhere, new String[] { groupname }, null);
		childCursor.moveToFirst();
		while (!childCursor.isAfterLast()) {
			Roster roster = new Roster();
			roster.setJid(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.JID)));
			roster.setAlias(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.ALIAS)));
			roster.setStatus_message(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.STATUS_MESSAGE)));
			roster.setStatusMode(childCursor.getString(childCursor
					.getColumnIndexOrThrow(RosterConstants.STATUS_MODE)));
			childList.add(roster);
			childCursor.moveToNext();
		}
		childCursor.close();
		return childList;
	}

	@Override
	public int getGroupCount() {
		return mGroupList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (mGroupList.size() <= 0)
			return 0;
		return getChildrenRosters(mGroupList.get(groupPosition).getGroupName())
				.size();
	}

	@Override
	public Group getGroup(int groupPosition) {
		return mGroupList.get(groupPosition);
	}

	@Override
	public Roster getChild(int groupPosition, int childPosition) {
		return getChildrenRosters(mGroupList.get(groupPosition).getGroupName())
				.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contact_buddy_list_group,
					null);
		}
		TextView groupName = (TextView) convertView
				.findViewById(R.id.group_name);
		Group group = getGroup(groupPosition);

		groupName.setText(TextUtils.isEmpty(group.getGroupName()) ? mContext
				.getString(R.string.default_group) : group.getGroupName());
		TextView onlineNum = (TextView) convertView
				.findViewById(R.id.online_count);
		onlineNum.setText(group.getMembers());
		ImageView indicator = (ImageView) convertView
				.findViewById(R.id.group_indicator);
		if (isExpanded)
			indicator.setImageResource(R.drawable.indicator_expanded);
		else
			indicator.setImageResource(R.drawable.indicator_unexpanded);
		// 必须使用资源Id当key（不是资源id会出现运行时异常），android本意应该是想用tag来保存资源id对应组件。
		// 将groupPosition，childPosition通过setTag保存,在onItemLongClick方法中就可以通过view参数直接拿到了
		convertView.setTag(R.id.xxx01, groupPosition);
		convertView.setTag(R.id.xxx02, -1);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Roster roster = getChild(groupPosition, childPosition);
		int presenceMode = Integer.parseInt(roster.getStatusMode());
		ViewHolder holder;
		if (convertView == null
				|| convertView.getTag(R.drawable.ic_launcher + presenceMode) == null) {
			L.i("liweiping", "new  child ");
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.contact_list_item_for_buddy, parent, false);
			holder.headView = (ImageView) convertView.findViewById(R.id.icon);
			holder.statusView = (ImageView) convertView
					.findViewById(R.id.stateicon);
			holder.nickView = (TextView) convertView
					.findViewById(R.id.contact_list_item_name);
			holder.onlineModeView = (ImageView) convertView
					.findViewById(R.id.online_mode);
			holder.statusMsgView = (TextView) convertView
					.findViewById(R.id.contact_list_item_state);
			convertView.setTag(R.drawable.ic_launcher + presenceMode, holder);
			convertView.setTag(R.string.app_name, R.drawable.ic_launcher
					+ presenceMode);
		} else {
			L.i("liweiping", "get child form case");
			holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
					+ presenceMode);
		}
		holder.nickView.setText(roster.getAlias());

		holder.statusMsgView.setText(TextUtils.isEmpty(roster
				.getStatusMessage()) ? mContext.getString(R.string.status_offline) : roster.getStatusMessage());
		setViewImage(holder.onlineModeView, holder.headView, holder.statusView,
				roster.getStatusMode());

		convertView.setTag(R.id.xxx01, groupPosition);
		convertView.setTag(R.id.xxx02, childPosition);
		return convertView;
	}

	static class ViewHolder {
		ImageView headView;
		TextView nickView;
		ImageView statusView;
		ImageView onlineModeView;
		TextView statusMsgView;

	}

	protected void setViewImage(ImageView online, ImageView head, ImageView v,
			String value) {
		int presenceMode = Integer.parseInt(value);
		int statusDrawable = getIconForPresenceMode(presenceMode);
		if (statusDrawable == -1) {
			v.setVisibility(View.INVISIBLE);
			head.setImageResource(R.drawable.login_default_avatar_offline);
			online.setImageDrawable(null);
			return;
		}
		head.setImageResource(R.drawable.login_default_avatar);
		online.setImageResource(R.drawable.terminal_icon_ios_online);
		v.setImageResource(statusDrawable);

	}

	private int getIconForPresenceMode(int presenceMode) {
		return StatusMode.values()[presenceMode].getDrawableId();
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getTreeHeaderState(int groupPosition, int childPosition) {
		final int childCount = getChildrenCount(groupPosition);
		if (childPosition == childCount - 1) {
			mPullToRefreshScrollView.setMode(Mode.DISABLED);
			return PINNED_HEADER_PUSHED_UP;
		} else if (childPosition == -1
				&& !mIphoneTreeView.isGroupExpanded(groupPosition)) {
			mPullToRefreshScrollView.setMode(Mode.PULL_FROM_START);
			return PINNED_HEADER_GONE;
		} else {
			L.i("liweiping", "groupPosition = " + groupPosition
					+ ", childPosition = " + childPosition);
			// 第一组第一个,可以下拉刷新
			if (groupPosition == 0 && childPosition == -1) {
				mPullToRefreshScrollView.setMode(Mode.PULL_FROM_START);
			} else {
				mPullToRefreshScrollView.setMode(Mode.DISABLED);
			}
			return PINNED_HEADER_VISIBLE;
		}
	}

	@Override
	public void configureTreeHeader(View header, int groupPosition,
			int childPosition, int alpha) {
		Group group = getGroup(groupPosition);
		((TextView) header.findViewById(R.id.group_name)).setText(TextUtils
				.isEmpty(group.getGroupName()) ? mContext
				.getString(R.string.default_group) : group.getGroupName());
		((TextView) header.findViewById(R.id.online_count)).setText(group
				.getMembers());
	}

	@Override
	public void onHeadViewClick(int groupPosition, int status) {
		// TODO Auto-generated method stub
		groupStatusMap.put(groupPosition, status);
	}

	@Override
	public int getHeadViewClickStatus(int groupPosition) {
		if (groupStatusMap.containsKey(groupPosition)) {
			return groupStatusMap.get(groupPosition);
		} else {
			return IphoneTreeView.COLLAPSED;
		}
	}

	public class Group {
		private String groupName;
		private String members;

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public String getMembers() {
			return members;
		}

		public void setMembers(String members) {
			this.members = members;
		}

	}

	public class Roster {
		private String jid;
		private String alias;
		private String statusMode;
		private String statusMessage;

		public String getJid() {
			return jid;
		}

		public void setJid(String jid) {
			this.jid = jid;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getStatusMode() {
			return statusMode;
		}

		public void setStatusMode(String statusMode) {
			this.statusMode = statusMode;
		}

		public String getStatusMessage() {
			return statusMessage;
		}

		public void setStatus_message(String statusMessage) {
			this.statusMessage = statusMessage;
		}

	}

	@Override
	public int getChildCount(int groupPosition) {
		// TODO Auto-generated method stub
		return getChildrenCount(groupPosition);
	}
}
