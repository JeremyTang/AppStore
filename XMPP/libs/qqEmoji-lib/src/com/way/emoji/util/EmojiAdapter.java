package com.way.emoji.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.way.gifface.R;

public class EmojiAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
	private Context mContext;
	private int mSize;
	private int mCurrentPage;
	private Map<String, String> mFaceMap;
	private List<String> mEmojiKeys = new ArrayList<String>();
	private List<String> mEmojiValues = new ArrayList<String>();

	public EmojiAdapter(Context context, int currentPage, int size) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mSize = size;
		mCurrentPage = currentPage;
		mFaceMap = EmojiUtil.getInstance().getFaceMap();
		initData();
	}

	private void initData() {
		for (Map.Entry<String, String> entry : mFaceMap.entrySet()) {
			mEmojiValues.add(entry.getValue());
			mEmojiKeys.add(entry.getKey());
		}
	}

	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public Object getItem(int position) {
		int count = getCount() * mCurrentPage + position;
		if (count < mFaceMap.size())
			return mEmojiKeys.get(count);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (convertView == null) {
			rowView = mLayoutInflater.inflate(R.layout.emoji_cell, null);
			ViewHolder viewHolder = new ViewHolder((ImageView) rowView);
			rowView.setTag(viewHolder);
		}

		ViewHolder viewHolder = (ViewHolder) rowView.getTag();
		int count = getCount() * mCurrentPage + position;
		if (count < mFaceMap.size()) {
			int resId = mContext.getResources().getIdentifier(
					EmojiUtil.STATIC_FACE_PREFIX + mEmojiValues.get(count),
					"drawable", mContext.getPackageName());
			viewHolder.imageView.setImageResource(resId);
			viewHolder.imageView.setTag(viewHolder.imageView.getId(),
					mEmojiKeys.get(count));
		} else {
			viewHolder.imageView
					.setBackgroundResource(android.R.color.transparent);
		}
		return rowView;
	}

	static class ViewHolder {
		public ImageView imageView;

		public ViewHolder(ImageView imageView) {
			this.imageView = imageView;
		};
	}
}
