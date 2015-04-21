package com.way.emoji.util;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author way
 * 
 */

public class AnimatedGifDrawable extends AnimationDrawable {

	private int mCurrentIndex = 0;
	private UpdateListener mListener;

	public AnimatedGifDrawable(Resources res, int size, InputStream source,
			UpdateListener listener) {
		mListener = listener;
		GifDecoder decoder = new GifDecoder();
		decoder.read(source);

		// Iterate through the gif frames, add each as animation frame
		for (int i = 0; i < decoder.getFrameCount(); i++) {
			Bitmap bitmap = decoder.getFrame(i);
			BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
			// Explicitly set the bounds in order for the frames to display
			if(size > 0)
				drawable.setBounds(0, 0, size, size);
			else
				drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			addFrame(drawable, decoder.getDelay(i));
			if (i == 0) {
				// Also set the bounds for this container drawable
				if(size > 0)
					drawable.setBounds(0, 0, size, size);
				else
					drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			}
		}
	}

	/**
	 * Naive method to proceed to next frame. Also notifies listener.
	 */
	public void nextFrame() {
		mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
		if (mListener != null)
			mListener.update();
	}

	/**
	 * Return display duration for current frame
	 */
	public int getFrameDuration() {
		return getDuration(mCurrentIndex);
	}

	/**
	 * Return drawable for current frame
	 */
	public Drawable getDrawable() {
		return getFrame(mCurrentIndex);
	}

	/**
	 * Interface to notify listener to update/redraw Can't figure out how to
	 * invalidate the drawable (or span in which it sits) itself to force redraw
	 */
	public interface UpdateListener {
		void update();
	}

}
