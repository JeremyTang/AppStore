package com.jingliang.appstore.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

@SuppressWarnings("unused")
public class ConnectionManager {

	private static final String tag = ConnectionManager.class.getSimpleName();

	private Context mContext;
	private static ConnectionManager mManager = null;

	public static ConnectionManager newInstance(Context context) {
		if (mManager == null) {
			mManager = new ConnectionManager(context);
		}
		return mManager;
	}

	private ConnectionManager(Context context) {
		this.mContext = context;
	}

	/*** 　参数配置 ***********************************************************/

	private URLConnection openConnection(String url) throws IOException {
		Log.d(tag, "Request Url = " + url);
		URL mUrl = new URL(url);
		return mUrl.openConnection();
	}

	private URLConnection configGetConnection(URLConnection conn) {
		conn.setUseCaches(true);
		conn.setConnectTimeout(ConnectData.TIME_OUT);
		conn.setReadTimeout(ConnectData.TIME_OUT);
		return conn;
	}

	private URLConnection configPostConnection(URLConnection conn) {
		conn.setDoInput(true);
		conn.setDoOutput(true);
		return conn;
	}

	/*** 　请求配置 ***********************************************************/
	public void get(String url) {
		URLConnection conn = null;
		InputStream is = null;
		BufferedReader br = null;
		try {
			conn = openConnection(url);
			conn = configGetConnection(conn);
			conn.connect();
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			String str;
			while ((str = br.readLine()) != null) {
				Log.d(tag, str);
			}
			br.close();
			is.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
