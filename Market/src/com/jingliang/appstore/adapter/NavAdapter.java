package com.jingliang.appstore.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingliang.appstore.R;
import com.jingliang.appstore.entity.NavInfo;

public class NavAdapter extends BaseAdapter {

	private Context mContext;
	private List<NavInfo> mNavs;
	private static final int PADDING = 20;

	private int index = -1;

	static class ViewHolder {
		ImageView icon;
		TextView title;
	}

	public NavAdapter(Context mContext, List<NavInfo> mNavs) {
		super();
		this.mContext = mContext;
		this.mNavs = mNavs;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mNavs.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mNavs.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setSelector(int position) {
		this.index = position;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		final NavInfo nav = mNavs.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_nav, null);
			holder.icon = (ImageView) convertView.findViewById(R.id.nav_icon);
			holder.title = (TextView) convertView.findViewById(R.id.nav_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == index) {
			convertView
					.setBackgroundResource(R.drawable.bg_folder_list_item_checked);
		} else {
			convertView.setBackgroundResource(R.drawable.selector_nav);
		}
		convertView.setPadding(PADDING, PADDING, PADDING, PADDING);
		holder.icon.setImageDrawable(mContext.getResources().getDrawable(
				nav.getIcon()));
		holder.title.setText(nav.getTitle());
		return convertView;
	}

}
