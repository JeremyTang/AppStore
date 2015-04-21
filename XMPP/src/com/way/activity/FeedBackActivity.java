package com.way.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.way.ui.swipeback.SwipeBackActivity;
import com.way.util.T;
import com.way.xx.R;

public class FeedBackActivity extends SwipeBackActivity {
	private EditText mFeedBackEt;
	private Button mSendBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_back_view);
		mFeedBackEt = (EditText) findViewById(R.id.fee_back_edit);
		mSendBtn = (Button) findViewById(R.id.feed_back_btn);
		mSendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = mFeedBackEt.getText().toString();
				if (!TextUtils.isEmpty(content)) {
					Intent intent = new Intent(Intent.ACTION_SENDTO);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, "推聊Android客户端 - 信息反馈");
					intent.putExtra(Intent.EXTRA_TEXT, content);
					intent.setData(Uri.parse("mailto:way.ping.li@gmail.com"));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					FeedBackActivity.this.startActivity(intent);
				} else {
					T.showShort(FeedBackActivity.this, "请输入一点点内容嘛！");
				}
			}
		});
	}
}
