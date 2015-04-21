package com.way.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NoScrollScrollView extends ScrollView {
	private boolean canScroll = false;

	public NoScrollScrollView(Context context) {
		super(context);
	}

	public NoScrollScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (!canScroll)
			return false;
		return super.onInterceptTouchEvent(event);
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (canScroll)
				return super.onTouchEvent(event);
		default:
			break;
		}
		return canScroll;
	}

	public void setCanScroll(boolean canScroll) {
		this.canScroll = canScroll;
	}
}