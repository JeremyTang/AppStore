package com.jingliang.appstore.fragment;

import java.io.File;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jingliang.appstore.R;
import com.jingliang.appstore.adapter.DownloadAdapter;
import com.jingliang.appstore.download.DownloadManager;
import com.jingliang.appstore.download.DownloadManager.ManagerCallBack;
import com.jingliang.appstore.download.DownloadService;
import com.jingliang.appstore.entity.DownloadInfo;
import com.jingliang.appstore.xlistview.XListView;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class Downloader extends Fragment implements Runnable {

	private static final String tag = Downloader.class.getSimpleName();
	private static final int MESSAGE_REFRESH = 1;
	private static final int MESSAGE_SUCCESS = 2;
	private static final int MESSAGE_ERROR = 3;

	private View mView;

	private XListView xListView;
	private ProgressBar mLoadingView;
	private TextView messageText;
	private DownloadManager mDownloadManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mDownloadManager = DownloadService.getDownloadManager(getActivity());
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_download, container, false);
		xListView = (XListView) mView.findViewById(R.id.download_listview);
		mLoadingView = (ProgressBar) mView.findViewById(R.id.download_loading);
		messageText = (TextView) mView.findViewById(R.id.download_error);
		xListView.setPullLoadEnable(false);
		xListView.setPullRefreshEnable(false);
		xListView.setEmptyView(mLoadingView);
		return mView;
	}

	public void onResume() {
		if (mDownloadManager.getDownloadInfoListCount() > 0) {
			// mHandler.removeCallbacks(this);
			// mHandler.postAtTime(this, 100);
			xListView.setAdapter(new DownloadAdapter(getActivity()));
		} else {
			mHandler.sendEmptyMessageDelayed(MESSAGE_ERROR, 500);
		}
		super.onResume();
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mLoadingView.setVisibility(View.GONE);
			switch (msg.what) {
			case MESSAGE_REFRESH:
				messageText.setText(messageText.getText() + "\n"
						+ msg.obj.toString() + " --- total = " + msg.arg1
						+ " --- current = " + msg.arg2);
				xListView.setEmptyView(messageText);
				break;
			case MESSAGE_SUCCESS:

				xListView.setEmptyView(messageText);
				break;
			case MESSAGE_ERROR:
				xListView.setEmptyView(messageText);
				messageText.setText(getString(R.string.net_error));
				break;
			}
		};
	};

	@Override
	public void run() {
		Log.d(tag, "Thread run");
		int count = mDownloadManager.getDownloadInfoListCount();
		for (int i = 0; i < count; i++) {
			final DownloadInfo mDownloadInfo = mDownloadManager
					.getDownloadInfo(i);
			HttpHandler<File> handler = mDownloadInfo.getHandler();
			if (handler != null) {
				DownloadManager.ManagerCallBack callback = (ManagerCallBack) handler
						.getRequestCallBack();
				callback.setBaseCallBack(new RequestCallBack<File>() {

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						Message msg = mHandler.obtainMessage();
						msg.what = MESSAGE_REFRESH;
						msg.obj = mDownloadInfo.getFileName();
						msg.arg1 = (int) total;
						msg.arg2 = (int) current;
						msg.sendToTarget();
						super.onLoading(total, current, isUploading);
					}

					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						// TODO Auto-generated method stub
						Message msg = mHandler.obtainMessage();
						msg.what = MESSAGE_REFRESH;
						msg.obj = mDownloadInfo;
						msg.sendToTarget();
					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						// TODO Auto-generated method stub

					}
				});
			}
		}

	}
}
