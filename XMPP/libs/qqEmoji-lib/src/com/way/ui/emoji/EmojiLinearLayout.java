package com.way.ui.emoji;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.way.emoji.util.EmojiAdapter;
import com.way.emoji.util.EmojiUtil;
import com.way.emoji.util.EmojiViewPageAdapter;
import com.way.gifface.R;
import com.way.ui.pagerindicator.CirclePageIndicator;

public class EmojiLinearLayout extends LinearLayout implements
		OnItemClickListener {
	// 横屏时
	private static final int LAND_COLUMN = 11;// 11列
	private static final int LAND_ROW = 2;// 2行
	// 竖屏时
	private static final int PORT_COLUMN = 7;// 7列
	private static final int PORT_ROW = 3;// 3行
	private ViewPager mPager;
	private int mViewPagerNum;

	private OnEmojiClickedListener mOnEmojiClickedListener;

	public void setOnEmojiClickedListener(
			OnEmojiClickedListener onEmojiClickedListener) {
		mOnEmojiClickedListener = onEmojiClickedListener;
	}

	public interface OnEmojiClickedListener {
		void onEmojiClicked(String emoji);
	}

	public EmojiLinearLayout(Context context) {
		super(context);
	}

	public EmojiLinearLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mPager = (ViewPager) findViewById(R.id.child_pager);
		mViewPagerNum = getEmojiSize();
		List<GridView> lv = new ArrayList<GridView>();
		for (int i = 0; i < mViewPagerNum; ++i)
			lv.add(getGridView(LayoutInflater.from(getContext()), i));
		EmojiViewPageAdapter adapter = new EmojiViewPageAdapter(lv);
		mPager.setAdapter(adapter);
		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
	}

	private int getEmojiSize() {
		int size = EmojiUtil.getInstance().getFaceMap().size();
		if (size % PORT_COLUMN * PORT_ROW == 0)// 刚好被整除
			return size / (PORT_COLUMN * PORT_ROW);
		return (size / (PORT_COLUMN * PORT_ROW)) + 1;
	}

	private GridView getGridView(LayoutInflater inflater, int i) {
		GridView gv = (GridView) inflater.inflate(R.layout.emoji_grid, null);
		gv.setAdapter(new EmojiAdapter(getContext(), i, PORT_COLUMN * PORT_ROW));
		gv.setOnTouchListener(forbidenScroll());
		gv.setOnItemClickListener(this);
		return gv;
	}

	// 防止乱pageview乱滚动
	private OnTouchListener forbidenScroll() {
		return new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String selected = (String) view.getTag(view.getId());
		if (TextUtils.isEmpty(selected))
			return;
		if (mOnEmojiClickedListener != null) {
			mOnEmojiClickedListener.onEmojiClicked(selected);
		}
		Log.i("liweiping", "onItemClick = " + selected);
	}
}
