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
import com.jingliang.appstore.bean.DownloadInfo;
import com.jingliang.appstore.download.DownloadManager;
import com.jingliang.appstore.download.DownloadManager.ManagerCallBack;
import com.jingliang.appstore.download.DownloadService;
import com.jingliang.appstore.utils.Utils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 *
 * @author tjl 　下载列表刷新适配器
 */
public class DownloadAdapter extends BaseAdapter {

	private static final String tag = "Download";

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
		holder.bindSource(mDownloadInfo);
		return convertView;
	}

	class ViewHolder {
		ImageView image;
		TextView name;
		TextView message;
		ProgressBar progress;
		Button button;

		public void bindSource(DownloadInfo downloadinfo) {
			if (downloadinfo == null) {
				return;
			}
			final DownloadInfo mDownloadInfo = downloadinfo;
			name.setText(mDownloadInfo.getFileName());
			final HttpHandler.State mState = mDownloadInfo.getState();
			final HttpHandler<File> mHandler = mDownloadInfo.getHandler();
			Log.d(tag, "mState = " + mState);
			switch (mState) {
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
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						mDownloadManager.removeDownload(mDownloadInfo);
					} catch (DbException e) {
						e.printStackTrace();
					}
					DownloadAdapter.this.notifyDataSetChanged();
				}
			});
			if (mHandler != null) {
				DownloadManager.ManagerCallBack callback = (ManagerCallBack) mHandler
						.getRequestCallBack();
				callback.setBaseCallBack(new DownloadCallback(ViewHolder.this));
			}

		}

		public void bindView(View view) {
			image = (ImageView) view.findViewById(R.id.download_icon);
			name = (TextView) view.findViewById(R.id.download_name);
			progress = (ProgressBar) view.findViewById(R.id.download_progress);
			button = (Button) view.findViewById(R.id.download_button);
			message = (TextView) view.findViewById(R.id.download_message);
		}
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
			mHolder.message.setText(Utils
					.convertDownloadMessage(total, current));
			super.onLoading(total, current, isUploading);
		}

		@Override
		public void onSuccess(ResponseInfo<File> arg0) {
			mHolder.progress.setMax(100);
			mHolder.progress.setProgress(100);
			mHolder.message.setText(mContext.getString(R.string.complete));
		}

		@Override
		public void onFailure(HttpException arg0, String arg1) {
			mHolder.button.setText(mContext.getString(R.string.retry));
			mHolder.message.setText(mContext.getString(R.string.failure));
		}

	}

}
