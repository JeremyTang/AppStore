package com.jeremy.httpconnection.callback;

public interface ConnectionCallBack {
	void onSuccess();

	void onFailure();

	void onProgress();
}
