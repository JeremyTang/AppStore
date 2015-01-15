package com.jingliang.appstore.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingliang.appstore.R;
import com.jingliang.appstore.download.DownloadService;
import com.jingliang.appstore.utils.MarketManager;

public class AppJsonAdapter extends BaseAdapter {

	private Context mContext;
	private JSONArray mJson;

	public AppJsonAdapter(Context mContext, JSONArray json) {
		super();
		this.mContext = mContext;
		this.mJson = json;
	}

	@Override
	public int getCount() {
		return mJson.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return mJson.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setDataChange(JSONArray apps) {
		this.mJson = apps;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		MaketViewHolder holder = null;
		if (view == null) {
			holder = new MaketViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.item_app,
					null);
			holder.icon = (ImageView) view.findViewById(R.id.app_icon);
			holder.download = (Button) view.findViewById(R.id.app_download);
			holder.name = (TextView) view.findViewById(R.id.app_name);
			view.setTag(holder);
		} else {
			holder = (MaketViewHolder) view.getTag();
		}
		try {
			final JSONObject mCurrentJson = (JSONObject) mJson.get(position);
			holder.name.setText(mCurrentJson.getString("name"));
			holder.download.setOnClickListener(new DowanloadListener(
					mCurrentJson));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;
	}

	public class MaketViewHolder {
		ImageView icon;
		TextView name;
		Button download;
	}

	public class DowanloadListener implements OnClickListener {

		private JSONObject obj;

		public DowanloadListener(JSONObject obj) {
			super();
			this.obj = obj;
		}

		public void onClick(View v) {
			try {
				DownloadService.getDownloadManager(mContext).addNewDownload(
						obj.getString("path").trim(),// 文件下载地址
						obj.getString("name"),// 查询名字
						MarketManager.DOWNLOAD_DIR + obj.getString("name")
								+ ".apk",// 文件保存地址
						true, true, null);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
