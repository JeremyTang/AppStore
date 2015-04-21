package com.jingliang.appstore.fragment;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.jingliang.appstore.ApKInfoActivity;
import com.jingliang.appstore.R;
import com.jingliang.appstore.adapter.AppJsonAdapter;
import com.jingliang.appstore.http.HttpManager;
import com.jingliang.appstore.http.HttpSource;
import com.jingliang.appstore.utils.IntentData;
import com.jingliang.appstore.xlistview.XListView;
import com.jingliang.appstore.xlistview.XListView.IXListViewListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * 
 * @author tjl 　应用界面
 */
public class Market extends Fragment implements IXListViewListener,
		OnItemClickListener, Runnable {

	private static final String tag = Market.class.getSimpleName();

	private static final int MESSAGE_LOAD_MORE = 1;
	private static final int MESSAGE_REFRESH = 2;

	private View mView;
	private XListView xListView;
	private View loadView;
	private TextView errorText;
	//
	private JSONArray mJsonArray;
	private AppJsonAdapter mAdapter;

	// 网络请求结果
	private AsyncHttpResponseHandler mResponsehandler = new JsonHttpResponseHandler() {
		public void onSuccess(int statusCode, org.apache.http.Header[] headers,
				org.json.JSONObject response) {
			try {
				mJsonArray = response.getJSONArray("listing");
				mAdapter = new AppJsonAdapter(getActivity(), mJsonArray);
				xListView.setAdapter(mAdapter);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};

		public void onFailure(int statusCode, org.apache.http.Header[] headers,
				Throwable throwable, org.json.JSONObject errorResponse) {
			loadView.setVisibility(View.GONE);
			errorText.setVisibility(View.VISIBLE);
			xListView.setEmptyView(errorText);
		};

	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_LOAD_MORE:
				xListView.stopLoadMore();
				break;
			case MESSAGE_REFRESH:
				xListView.stopRefresh();
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(tag, "onCreateView()");
		mView = inflater.inflate(R.layout.fragment_apps, container, false);
		xListView = (XListView) mView.findViewById(R.id.apps_listview);
		loadView = mView.findViewById(R.id.loading_view);
		errorText = (TextView) mView.findViewById(R.id.error_text);
		loadView.setVisibility(View.VISIBLE);
		xListView.setEmptyView(loadView);
		xListView.setAdapter(mAdapter);
		xListView.setPullLoadEnable(false);
		xListView.setXListViewListener(this, 0);
		xListView.setOnItemClickListener(this);
		return mView;
	}

	@Override
	public void onPause() {
		Log.d(tag, "onPause()");
		super.onPause();
	}

	@Override
	public void onResume() {
		if (mJsonArray != null) {
			mAdapter = new AppJsonAdapter(getActivity(), mJsonArray);
			xListView.setAdapter(mAdapter);
		} else {
			HttpManager.get(HttpSource.URL_LIST, mResponsehandler);
		}
		super.onResume();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		Log.d(tag, "onAttach()");
		super.onAttach(activity);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(tag, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		Log.d(tag, "onDetach()");
		super.onDetach();
	}

	// 下拉刷新
	@Override
	public void onRefresh(int id) {
		// TODO Auto-generated method stub
		Log.d(tag, "onRefresh");
		mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, 1000);
	}

	// 加载更多
	@Override
	public void onLoadMore(int id) {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessageDelayed(MESSAGE_LOAD_MORE, 1000);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ApKInfoActivity.class);
		try {
			intent.putExtra(
					IntentData.APKID,
					mJsonArray.getJSONObject(position - 1).getString(
							IntentData.APKID));
			intent.putExtra(
					IntentData.VERSION,
					mJsonArray.getJSONObject(position - 1).getString(
							IntentData.VERSION));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startActivity(intent);
	}

	@Override
	public void run() {
		// ConnectionManager.newInstance(getActivity()).sendGetRequest(
		// HttpManager.URL_LIST);
	}
}
