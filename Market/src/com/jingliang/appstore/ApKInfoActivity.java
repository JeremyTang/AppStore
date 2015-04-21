package com.jingliang.appstore;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jingliang.appstore.http.HttpManager;
import com.jingliang.appstore.http.HttpSource;
import com.jingliang.appstore.utils.IntentData;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ApKInfoActivity extends Activity {

	private static final String tag = "ApkInfo";

	private TextView messageText;

	private String packageName;
	private String version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appinfo);
		Intent intent = getIntent();
		messageText = (TextView) findViewById(R.id.app_message);

		packageName = intent.getStringExtra(IntentData.APKID);
		version = intent.getStringExtra(IntentData.VERSION);
		Log.d(tag, HttpSource.URL_APKINFO + "/" + packageName + "/" + version
				+ "/" + HttpSource.TYPE);
		HttpManager.get(HttpSource.URL_APKINFO + "/" + packageName + "/"
				+ version + "/" + HttpSource.TYPE,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						// TODO Auto-generated method stub
						messageText.setText(response.toString());
						super.onSuccess(statusCode, headers, response);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, JSONObject errorResponse) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, throwable,
								errorResponse);
						messageText.setText(getString(R.string.net_error));
					}

				});

	}
}
