package com.jingliang.appstore.http;

public class HttpSource {

	// 响应超时
	public static final int DEFAULT_TIMEOUT = 10000;
	//
	public static final String URL_BASE = "http://webservices.aptoide.com/webservices/";
	// 应用列表
	public static final String CATEGORY_LIST = "2/listRepository";
	// 应用详情
	public static final String CATEGORY_APPINFO = "getApkInfo";
	// repo
	public static final String REPO = "sen5";
	// 返回类型
	public static final String TYPE = "json";
	// 应用列表
	public static final String URL_LIST = URL_BASE + CATEGORY_LIST + "/" + REPO
			+ "/" + TYPE;
	public static final String URL_PICTRUE_BASE = "";
	//
	public static final String URL_APKINFO = URL_BASE + CATEGORY_APPINFO + "/"
			+ REPO;

	// 转换图片ＵＲＬ
	public static String convertIconUri(String str) {
		String icon = str.substring(str.lastIndexOf("/") + 1, str.length());
		return "http://pool.apk.aptoide.com/sen5/" + icon;
	}
}
