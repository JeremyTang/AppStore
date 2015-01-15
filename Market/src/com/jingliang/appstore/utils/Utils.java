package com.jingliang.appstore.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class Utils {

	public static String getIcomImageUrl(String url) {

		return "";
	}

	public static List<ResolveInfo> queryResolveInfo(Context context) {
		List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
		PackageManager pm = context.getPackageManager();
		Intent queryIntent = new Intent(Intent.ACTION_MAIN);
		queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolves = pm.queryIntentActivities(queryIntent, 0);
		for (ResolveInfo r : resolves) {

		}
		return mApps;
	}
	
	public static String convertAppSize(long size){
		
		return "";
	}
}
