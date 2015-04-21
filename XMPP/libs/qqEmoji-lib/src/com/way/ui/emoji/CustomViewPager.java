package com.way.ui.emoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CustomViewPager extends ViewPager {
	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		//Log.i("liweiping", "canScroll...");
		if (v != this && v instanceof ViewPager) {
			int currentItem = ((ViewPager) v).getCurrentItem();
			int countItem = ((ViewPager) v).getAdapter().getCount();
			if ((currentItem == (countItem - 1) && dx < 0)
					|| (currentItem == 0 && dx > 0)) {
				//Log.i("liweiping", "canScroll == false");
				return false;
			}
			//Log.i("liweiping", "canScroll == true");
			return true;
		}
		//Log.i("liweiping",
		//		"canScroll == " + super.canScroll(v, checkV, dx, x, y));
		return super.canScroll(v, checkV, dx, x, y);
	}
}
