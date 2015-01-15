package com.jingliang.appstore.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingliang.appstore.entity.AppInfo;

public class JSONUtil {

	public static AppInfo parserAppInfo(JSONObject obj) throws JSONException {
		AppInfo app = null;
		if (obj != null) {
			app = new AppInfo();
			app.setName(obj.getString("name"));
			app.setDate(obj.getString("data"));
			app.setDownloadPath(obj.getString("path"));
			app.setIconPath(obj.getString("icon"));
			app.setMd5(obj.getString("md5h"));
			app.setPackageName(obj.getString("apkid"));
			app.setSize(obj.getLong("size"));
			app.setVersionCode(obj.getInt("vercode"));
			app.setVersionName(obj.getString("ver"));
		}
		return app;
	}

}
