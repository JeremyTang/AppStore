package com.way.ui.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.way.emoji.util.AnimatedGifDrawable;
import com.way.emoji.util.AnimatedImageSpan;
import com.way.emoji.util.EmojiUtil;
import com.way.gifface.R;

/**
 * @author way
 */
public class EmojiTextView extends TextView {
	private static final String TAG = "liweiping";
	private static final String START_CHAR = "[";
	private static final String END_CHAR = "]";
	private boolean mIsDynamic;

	public EmojiTextView(Context context) {
		super(context);
		init(null);
	}

	public EmojiTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public EmojiTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		if (attrs == null) {
			mIsDynamic = false;
		} else {
			TypedArray a = getContext().obtainStyledAttributes(attrs,
					R.styleable.Emojicon);
			mIsDynamic = a.getBoolean(R.styleable.Emojicon_isDynamic, false);
			a.recycle();
		}
		setText(getText());
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		SpannableString content = new SpannableString(text);
		emotifySpannable(content);
		super.setText(content, BufferType.SPANNABLE);
	}

	/**
	 * Work through the contents of the string, and replace any occurrences of
	 * [icon] with the imageSpan
	 * 
	 * @param spannable
	 */
	private void emotifySpannable(Spannable spannable) {
		int length = spannable.length();
		int position = 0;
		int tagStartPosition = 0;
		int tagLength = 0;
		StringBuilder buffer = new StringBuilder();
		boolean inTag = false;

		if (length <= 0)
			return;

		do {
			String c = spannable.subSequence(position, position + 1).toString();

			if (!inTag && c.equals(START_CHAR)) {
				buffer = new StringBuilder();
				tagStartPosition = position;
				// Log.d(TAG, "   Entering tag at " + tagStartPosition);

				inTag = true;
				tagLength = 0;
			}

			if (inTag) {
				buffer.append(c);
				tagLength++;

				// Have we reached end of the tag?
				if (c.equals(END_CHAR)) {
					inTag = false;

					String tag = buffer.toString();
					int tagEnd = tagStartPosition + tagLength;
					// start by liweiping for 去除首部有多个“[”符号
					int lastIndex = tag.lastIndexOf(START_CHAR);
					if (lastIndex > 0) {
						tagStartPosition = tagStartPosition + lastIndex;
						tag = tag.substring(lastIndex, tag.length());
					}
					// end by liweiping for
					// Log.d(TAG, "Tag: " + tag + ", started at: "
					// + tagStartPosition + ", finished at " + tagEnd
					// + ", length: " + tagLength);

					if (mIsDynamic) {
						DynamicDrawableSpan imageSpan = getDynamicImageSpan(tag);
						if (imageSpan != null)
							spannable.setSpan(imageSpan, tagStartPosition,
									tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else {
						ImageSpan imageSpan = getImageSpan(tag);
						if (imageSpan != null)
							spannable.setSpan(imageSpan, tagStartPosition,
									tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}

			position++;
		} while (position < length);
	}

	private ImageSpan getImageSpan(String content) {
		String idStr = EmojiUtil.getInstance().getFaceId(content);
		Resources resources = getContext().getResources();
		int id = resources.getIdentifier(EmojiUtil.STATIC_FACE_PREFIX + idStr,
				"drawable", getContext().getPackageName());
		if (id > 0) {
			try {
				int size = Math.round(getTextSize()) + 10;
				Drawable emoji = getContext().getResources().getDrawable(id);
				emoji.setBounds(0, 0, size, size);
				ImageSpan imageSpan = new ImageSpan(emoji,
						ImageSpan.ALIGN_BASELINE);
				return imageSpan;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private DynamicDrawableSpan getDynamicImageSpan(String content) {
		String idStr = EmojiUtil.getInstance().getFaceId(content);
		Resources resources = getContext().getResources();
		int id = resources.getIdentifier(EmojiUtil.DYNAMIC_FACE_PREFIX + idStr,
				"drawable", getContext().getPackageName());
		if (id > 0) {
			try {
				AnimatedImageSpan imageSpan = new AnimatedImageSpan(
						new AnimatedGifDrawable(getResources(),
								Math.round(getTextSize()) + 10, getResources()
										.openRawResource(id),
								new AnimatedGifDrawable.UpdateListener() {
									@Override
									public void update() {
										// update the textview
										EmojiTextView.this.postInvalidate();
									}
								}));
				return imageSpan;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
