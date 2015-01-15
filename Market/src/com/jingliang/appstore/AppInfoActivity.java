package com.jingliang.appstore;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jingliang.appstore.net.NetController;
import com.jingliang.appstore.net.NetManager;
import com.jingliang.appstore.utils.IntentData;
import com.loopj.android.http.JsonHttpResponseHandler;

public class AppInfoActivity extends Activity {

	private static final String tag = "appinfo";

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
		Log.d(tag, NetManager.URL_APKINFO + "/" + packageName + "/" + version
				+ "/" + NetManager.TYPE);
		NetController.get(NetManager.URL_APKINFO + "/" + packageName + "/"
				+ version + "/" + NetManager.TYPE,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						// TODO Auto-generated method stub
						messageText.setText(response.toString());
						Log.d(tag, messageText.getText().toString());
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
