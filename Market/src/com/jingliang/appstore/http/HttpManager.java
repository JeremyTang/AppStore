package com.jingliang.appstore.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class HttpManager {
	private static AsyncHttpClient mHttpClient = null;

	static {
		mHttpClient = new AsyncHttpClient();
		mHttpClient.setTimeout(HttpSource.DEFAULT_TIMEOUT);
	}

	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		mHttpClient.get(url, responseHandler);
	}


}
