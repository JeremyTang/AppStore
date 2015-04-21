package com.way.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.way.exception.XXAdressMalformedException;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;
import com.way.ui.view.ChangeLog;
import com.way.util.DialogUtil;
import com.way.util.L;
import com.way.util.PreferenceConstants;
import com.way.util.PreferenceUtils;
import com.way.util.T;
import com.way.util.XMPPHelper;
import com.way.xx.R;

public class LoginActivity extends FragmentActivity implements
		IConnectionStatusCallback, TextWatcher {
	public static final String LOGIN_ACTION = "com.way.action.LOGIN";
	private static final int LOGIN_OUT_TIME = 0;
	private Button mLoginBtn;
	private EditText mAccountEt;
	private EditText mPasswordEt;
	private CheckBox mAutoSavePasswordCK;
	private CheckBox mHideLoginCK;
	private CheckBox mUseTlsCK;
	private CheckBox mSilenceLoginCK;
	private XXService mXxService;
	private Dialog mLoginDialog;
	private ConnectionOutTimeProcess mLoginOutTimeProcess;
	private String mAccount;
	private String mPassword;
	private View mTipsViewRoot;
	private TextView mTipsTextView;
	private Animation mTipsAnimation;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_OUT_TIME:
				if (mLoginOutTimeProcess != null
						&& mLoginOutTimeProcess.running)
					mLoginOutTimeProcess.stop();
				if (mLoginDialog != null && mLoginDialog.isShowing())
					mLoginDialog.dismiss();
				T.showShort(LoginActivity.this, R.string.timeout_try_again);
				break;

			default:
				break;
			}
		}

	};
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(LoginActivity.this);
			// 开始连接xmpp服务器
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(LoginActivity.this, XXService.class));
		bindXMPPService();
		setContentView(R.layout.loginpage);
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (TextUtils.equals(PreferenceUtils.getPrefString(this,
				PreferenceConstants.APP_VERSION, ""),
				getString(R.string.app_version))
				&& !TextUtils.isEmpty(PreferenceUtils.getPrefString(this,
						PreferenceConstants.ACCOUNT, ""))) {
			mTipsViewRoot.setVisibility(View.GONE);
		} else {
			mTipsViewRoot.setVisibility(View.VISIBLE);
			PreferenceUtils.setPrefString(this,
					PreferenceConstants.APP_VERSION,
					getString(R.string.app_version));
		}
		if (mTipsTextView != null && mTipsAnimation != null)
			mTipsTextView.startAnimation(mTipsAnimation);
		ChangeLog cl = new ChangeLog(this);
		if (cl.firstRun()) {
			cl.getFullLogDialog().show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mTipsTextView != null && mTipsAnimation != null)
			mTipsTextView.clearAnimation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
		if (mLoginOutTimeProcess != null) {
			mLoginOutTimeProcess.stop();
			mLoginOutTimeProcess = null;
		}
	}

	private void initView() {
		mTipsAnimation = AnimationUtils.loadAnimation(this, R.anim.connection);
		mAutoSavePasswordCK = (CheckBox) findViewById(R.id.auto_save_password);
		mHideLoginCK = (CheckBox) findViewById(R.id.hide_login);
		mSilenceLoginCK = (CheckBox) findViewById(R.id.silence_login);
		mUseTlsCK = (CheckBox) findViewById(R.id.use_tls);
		mTipsViewRoot = findViewById(R.id.login_help_view);
		mTipsTextView = (TextView) findViewById(R.id.pulldoor_close_tips);
		mAccountEt = (EditText) findViewById(R.id.account_input);
		mPasswordEt = (EditText) findViewById(R.id.password);
		mLoginBtn = (Button) findViewById(R.id.login);
		String account = PreferenceUtils.getPrefString(this,
				PreferenceConstants.ACCOUNT, "");
		String password = PreferenceUtils.getPrefString(this,
				PreferenceConstants.PASSWORD, "");
		if (!TextUtils.isEmpty(account))
			mAccountEt.setText(account);
		if (!TextUtils.isEmpty(password))
			mPasswordEt.setText(password);
		mAccountEt.addTextChangedListener(this);
		mLoginDialog = DialogUtil.getLoginDialog(this);
		mLoginOutTimeProcess = new ConnectionOutTimeProcess();
	}

	public void onLoginClick(View v) {
		mAccount = mAccountEt.getText().toString();
		mAccount = splitAndSaveServer(mAccount);
		mPassword = mPasswordEt.getText().toString();
		if (TextUtils.isEmpty(mAccount)) {
			T.showShort(this, R.string.null_account_prompt);
			return;
		}
		if (TextUtils.isEmpty(mPassword)) {
			T.showShort(this, R.string.password_input_prompt);
			return;
		}
		if (mLoginOutTimeProcess != null && !mLoginOutTimeProcess.running)
			mLoginOutTimeProcess.start();
		if (mLoginDialog != null && !mLoginDialog.isShowing())
			mLoginDialog.show();
		if (mXxService != null) {
			mXxService.Login(mAccount, mPassword);
		}
	}

	private String splitAndSaveServer(String account) {
		if (!account.contains("@"))
			return account;
		String customServer = PreferenceUtils.getPrefString(this,
				PreferenceConstants.CUSTOM_SERVER, "");
		String[] res = account.split("@");
		String userName = res[0];
		String server = res[1];
		// check for gmail.com and other google hosted jabber accounts
		if ("gmail.com".equals(server) || "googlemail.com".equals(server)
				|| PreferenceConstants.GMAIL_SERVER.equals(customServer)) {
			// work around for gmail's incompatible jabber implementation:
			// send the whole JID as the login, connect to talk.google.com
			userName = account;

		}
		PreferenceUtils.setPrefString(this, PreferenceConstants.Server, server);
		return userName;
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			L.i(LoginActivity.class, "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			L.e(LoginActivity.class, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		L.i(LoginActivity.class, "[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XXService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		try {
			XMPPHelper.verifyJabberID(s);
			mLoginBtn.setEnabled(true);
			mAccountEt.setTextColor(Color.parseColor("#ff333333"));
		} catch (XXAdressMalformedException e) {
			mLoginBtn.setEnabled(false);
			mAccountEt.setTextColor(Color.RED);
		}
	}

	private void save2Preferences() {
		boolean isAutoSavePassword = mAutoSavePasswordCK.isChecked();
		boolean isUseTls = mUseTlsCK.isChecked();
		boolean isSilenceLogin = mSilenceLoginCK.isChecked();
		boolean isHideLogin = mHideLoginCK.isChecked();
		PreferenceUtils.setPrefString(this, PreferenceConstants.ACCOUNT,
				mAccount);// 帐号是一直保存的
		if (isAutoSavePassword)
			PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD,
					mPassword);
		else
			PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD,
					"");

		PreferenceUtils.setPrefBoolean(this, PreferenceConstants.REQUIRE_TLS,
				isUseTls);
		PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SCLIENTNOTIFY,
				isSilenceLogin);
		if (isHideLogin)
			PreferenceUtils.setPrefString(this,
					PreferenceConstants.STATUS_MODE, PreferenceConstants.XA);
		else
			PreferenceUtils.setPrefString(this,
					PreferenceConstants.STATUS_MODE,
					PreferenceConstants.AVAILABLE);
	}

	// 登录超时处理线程
	class ConnectionOutTimeProcess implements Runnable {
		public boolean running = false;
		private long startTime = 0L;
		private Thread thread = null;

		ConnectionOutTimeProcess() {
		}

		public void run() {
			while (true) {
				if (!this.running)
					return;
				if (System.currentTimeMillis() - this.startTime > 20 * 1000L) {
					mHandler.sendEmptyMessage(LOGIN_OUT_TIME);
				}
				try {
					Thread.sleep(10L);
				} catch (Exception localException) {
				}
			}
		}

		public void start() {
			try {
				this.thread = new Thread(this);
				this.running = true;
				this.startTime = System.currentTimeMillis();
				this.thread.start();
			} finally {
			}
		}

		public void stop() {
			try {
				this.running = false;
				this.thread = null;
				this.startTime = 0L;
			} finally {
			}
		}
	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		// TODO Auto-generated method stub
		if (mLoginDialog != null && mLoginDialog.isShowing())
			mLoginDialog.dismiss();
		if (mLoginOutTimeProcess != null && mLoginOutTimeProcess.running) {
			mLoginOutTimeProcess.stop();
			mLoginOutTimeProcess = null;
		}
		if (connectedState == XXService.CONNECTED) {
			save2Preferences();
			startActivity(new Intent(this, MainActivity.class));
			finish();
		} else if (connectedState == XXService.DISCONNECTED)
			T.showLong(LoginActivity.this, getString(R.string.request_failed)
					+ reason);
	}
}
