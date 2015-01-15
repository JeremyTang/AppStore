package com.jingliang.appstore.net;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class NetController {
	private static AsyncHttpClient mHttpClient = null;

	static {
		mHttpClient = new AsyncHttpClient();
		mHttpClient.setTimeout(NetManager.DEFAULT_TIMEOUT);
	}

	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		mHttpClient.get(url, responseHandler);
	}

}
