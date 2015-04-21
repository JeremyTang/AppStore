package com.jingliang.appstore.utils;

public class Utils {

	public static String getIcomImageUrl(String url) {

		return "";
	}

	// public static List<ResolveInfo> queryResolveInfo(Context context) {
	// List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
	// PackageManager pm = context.getPackageManager();
	// Intent queryIntent = new Intent(Intent.ACTION_MAIN);
	// queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	// List<ResolveInfo> resolves = pm.queryIntentActivities(queryIntent, 0);
	// return mApps;
	// }

	public static String convertAppSize(long size) {

		return "";
	}

	public static String convertDownloadMessage(long total, long current) {
		String unit;
		String max;
		String progress;
		if ((total / 1000) >= 1024) {
			unit = "MB";
			max = getSecondString((float) total / 1000 / 1024);
			progress = getSecondString((float) current / 1000 / 1024);
		} else {
			max = getNoPointString(total / 1000);
			progress = getNoPointString(current / 1000);
			unit = "KB";
		}
		return progress + unit + " / " + max + unit;
	}

	public static String getSecondString(float value) {
		String number = String.valueOf(value);
		return number.substring(0, number.indexOf(".") + 2);
	}

	public static String getNoPointString(float value) {
		String number = String.valueOf(value);
		return number.substring(0, number.indexOf("."));
	}
}
