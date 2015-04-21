package com.jeremy.httpconnection.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

public class ConnectionManager {

	private static final String tag = ConnectionManager.class.getSimpleName();

	private Context mContext;
	private static ConnectionManager mManager = null;
	private ExecutorService mThreadPoll;

	public static ConnectionManager newInstance(Context context) {
		if (mManager == null) {
			mManager = new ConnectionManager(context);
		}
		return mManager;
	}

	private ConnectionManager(Context context) {
		this.mContext = context;
		mThreadPoll = Executors.newCachedThreadPool();
	}

}
