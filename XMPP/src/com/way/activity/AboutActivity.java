package com.way.activity;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.way.ui.swipeback.SwipeBackActivity;
import com.way.ui.view.ChangeLog;
import com.way.xx.R;

public class AboutActivity extends SwipeBackActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView tv = (TextView) findViewById(R.id.app_information);
		Linkify.addLinks(tv, Linkify.ALL);
	}

	public void showChangeLog(View view) {
		new ChangeLog(this).getFullLogDialog().show();
	}
}
