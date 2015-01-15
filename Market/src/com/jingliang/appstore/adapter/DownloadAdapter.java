package com.jingliang.appstore.adapter;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jingliang.appstore.R;
import com.jingliang.appstore.download.DownloadManager;
import com.jingliang.appstore.download.DownloadManager.ManagerCallBack;
import com.jingliang.appstore.download.DownloadService;
import com.jingliang.appstore.entity.DownloadInfo;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;

public class DownloadAdapter extends BaseAdapter {

	private Context mContext;
	private DownloadManager mDownloadManager;

	public DownloadAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		mDownloadManager = DownloadService.getDownloadManager(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDownloadManager.getDownloadInfoListCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDownloadManager.getDownloadInfo(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return mDownloadManager.getDownloadInfo(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		final DownloadInfo mDownloadInfo = mDownloadManager
				.getDownloadInfo(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_download, null);
			holder.bindView(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.setData(mDownloadInfo);
		return convertView;
	}

	class DownloadCallback extends RequestCallBack<File> {

		private ViewHolder mHolder;

		public DownloadCallback(ViewHolder holader) {
			super();
			this.mHolder = holader;
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			mHolder.progress.setMax((int) total / 1000);
			mHolder.progress.setProgress((int) current / 1000);
			mHolder.button.setText(mContext.getString(R.string.pause));
			super.onLoading(total, current, isUploading);
		}

		@Override
		public void onSuccess(ResponseInfo<File> arg0) {
			mHolder.button.setText(mContext.getString(R.string.complete));
			mHolder.progress.setMax(100);
			mHolder.progress.setProgress(100);
		}

		@Override
		public void onFailure(HttpException arg0, String arg1) {
			// TODO Auto-generated method stub
			mHolder.button.setText(mContext.getString(R.string.retry));
		}

	}

	class ViewHolder {

		boolean isBindView = false;
		ImageView image;
		TextView name;
		ProgressBar progress;
		Button button;

		public void setData(DownloadInfo downloadinfo) {
			// if (!isBindView) {
			// return;
			// }
			if (downloadinfo == null) {
				return;
			}
			final DownloadInfo mDownloadInfo = downloadinfo;
			name.setText(mDownloadInfo.getFileName());
			HttpHandler<File> httpHanlder = mDownloadInfo.getHandler();
			final HttpHandler.State state;
			if (httpHanlder != null) {
				state = mDownloadInfo.getState();
				Log.d("jingliang", "state = " + state);
				switch (mDownloadInfo.getState()) {
				case WAITING:
					button.setText(mContext.getString(R.string.warting));
					break;
				case STARTED:
					button.setText(mContext.getString(R.string.pause));
					break;
				case LOADING:
					button.setText(mContext.getString(R.string.pause));
					break;
				case FAILURE:
					button.setText(mContext.getString(R.string.retry));
					break;
				default:
					break;
				}
				DownloadManager.ManagerCallBack callback = (ManagerCallBack) httpHanlder
						.getRequestCallBack();
				callback.setBaseCallBack(new DownloadCallback(this));
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Log.d("jingliang", "onClcik " + state);
						switch (state) {
						case WAITING:
						case STARTED:
						case LOADING:
							try {
								mDownloadManager.stopDownload(mDownloadInfo);
								button.setText(mContext
										.getString(R.string.resume));
							} catch (DbException e) {
								LogUtils.e(e.getMessage(), e);
							}
							break;
						case CANCELLED:
						case FAILURE:
							try {
								mDownloadManager.resumeDownload(mDownloadInfo,
										new DownloadCallback(ViewHolder.this));
								button.setText(mContext
										.getString(R.string.pause));
							} catch (DbException e) {
								LogUtils.e(e.getMessage(), e);
							}

							break;
						default:
							break;
						}
						DownloadAdapter.this.notifyDataSetChanged();
					}
				});
			}
		}

		public void bindView(View view) {
			// isBindView = true;
			image = (ImageView) view.findViewById(R.id.download_icon);
			name = (TextView) view.findViewById(R.id.download_name);
			progress = (ProgressBar) view.findViewById(R.id.download_progress);
			button = (Button) view.findViewById(R.id.download_button);
		}
	}

}
