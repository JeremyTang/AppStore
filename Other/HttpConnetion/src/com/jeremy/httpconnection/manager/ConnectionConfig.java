package com.jeremy.httpconnection.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

import com.jeremy.httpconnection.data.ConnectSource;

public class ConnectionConfig {

	private static final String tag = ConnectionConfig.class.getSimpleName();

	/*** 　参数配置 ***********************************************************/

	private URLConnection openConnection(String url) throws IOException {
		Log.d(tag, "Request Url = " + url);
		URL mUrl = new URL(url);
		return mUrl.openConnection();
	}

	private URLConnection configGetConnection(URLConnection conn) {
		conn.setUseCaches(true);
		conn.setConnectTimeout(ConnectSource.TIME_OUT);
		conn.setReadTimeout(ConnectSource.TIME_OUT);
		return conn;
	}

	private URLConnection configPostConnection(URLConnection conn) {
		conn.setDoInput(true);
		conn.setDoOutput(true);
		return conn;
	}
	
	public void post(String url){
		
	}
	
	public void post(String url,int para){
		
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
			e.printStackTrace();
		}
	}
}
