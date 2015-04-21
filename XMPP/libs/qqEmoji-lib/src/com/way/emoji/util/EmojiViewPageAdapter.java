package com.way.emoji.util;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class EmojiViewPageAdapter extends PagerAdapter {
	private List<GridView> mViews;

	public EmojiViewPageAdapter(List<GridView> lv) {
		this.mViews = lv;
	}

	@Override
	public int getCount() {
		return mViews.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViews.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View v = mViews.get(position);
		container.addView(v);
		return v;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return (view == object);
	}

}
