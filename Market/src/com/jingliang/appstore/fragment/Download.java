package com.jingliang.appstore.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jingliang.appstore.R;
import com.jingliang.appstore.adapter.DownloadAdapter;
import com.jingliang.appstore.download.DownloadService;
import com.jingliang.appstore.xlistview.XListView;

public class Download extends Fragment {

	private static final String tag = Download.class.getSimpleName();
	private static final int MESSAGE_ERROR = 3;

	private View mView;
	private XListView xListView;
	private ProgressBar mLoadingView;
	private TextView messageText;
	private DownloadAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		if (DownloadService.getDownloadManager(getActivity())
				.getDownloadInfoListCount() > 0) {
			Log.d(tag, "Adapter = " + (mAdapter != null));
			if (mAdapter == null) {
				mAdapter = new DownloadAdapter(getActivity());
			}
			xListView.setAdapter(mAdapter);
		} else {
			mHandler.sendEmptyMessageDelayed(MESSAGE_ERROR, 500);
		}
		super.onResume();
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mLoadingView.setVisibility(View.GONE);
			switch (msg.what) {
			case MESSAGE_ERROR:
				xListView.setEmptyView(messageText);
				messageText.setText(getString(R.string.net_error));
				break;
			}
		};
	};

}
