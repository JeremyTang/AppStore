package com.way.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class TouchDispatchView extends RelativeLayout {
	private boolean isInterceptTouches = true;

	public TouchDispatchView(Context context) {
		super(context);
	}

	public TouchDispatchView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchDispatchView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		return isInterceptTouches;
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (isInterceptTouches) {
			int count = getChildCount();
			if (count >= 0) {
				for (int i = 0; i < count; ++i) {
					View childView = getChildAt(i);
					float oldX = event.getX();
					float oldY = event.getY();
					float x = event.getX() - childView.getLeft();
					float y = event.getY() - childView.getTop();
					if (((y >= 0.0F) && (x >= 0.0F))
							|| ((0xFF & event.getAction()) != MotionEvent.ACTION_DOWN))
						event.setLocation(x, y);
					try {
						childView.dispatchTouchEvent(event);
						event.setLocation(oldX, oldY);
					} catch (ArrayIndexOutOfBoundsException e) {
						return false;
					}
				}
			}
		}
		return isInterceptTouches;
	}

	public void setInterceptTouches(boolean isInterceptTouches) {
		this.isInterceptTouches = isInterceptTouches;
	}
}
