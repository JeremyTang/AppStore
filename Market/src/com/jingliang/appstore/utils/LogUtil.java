package com.jingliang.appstore.utils;

import android.util.Log;

public class LogUtil {

	public static final String TAG_LOG = "MarketLog";

	public static void logDebug(String message) {
		Log.d(TAG_LOG, message);
	}

	public static void logError(String message) {
		Log.e(TAG_LOG, message);
	}

}
