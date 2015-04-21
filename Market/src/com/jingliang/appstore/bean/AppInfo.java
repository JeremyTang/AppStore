package com.jingliang.appstore.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author tjl
 * @category application entity
 */

// {
// "name": "Splashtop",
// "path":
// "http://pool.apk.aptoide.com/sen5/com-splashtop-remote-pad-v2-67232-8142493-47490609a318557bf7247aaff700ac40.apk",
// "ver": "2.4.9.4",
// "vercode": 67232,
// "apkid": "com.splashtop.remote.pad.v2",
// "icon":
// "http://mirror.apk04.aptoide.com/apks/4/aptoide-f63c6f2461f65f32b6d144d6d2ff982e/sen5/icons/90878a115061ce50743738b537ebdb0d.png",
// "date": "2015-01-05",
// "md5h": "47490609a318557bf7247aaff700ac40",
// "sz": 20303
// },
public class AppInfo implements Parcelable {

	private String name;
	private String downloadPath;
	private String versionName;
	private int versionCode;
	private String packageName;
	private String iconPath;
	private String date;
	private String md5;
	private long size;

	public AppInfo() {
	}

	public AppInfo(Parcel arg0) {
		name = arg0.readString();
		downloadPath = arg0.readString();
		versionName = arg0.readString();
		versionCode = arg0.readInt();
		packageName = arg0.readString();
		iconPath = arg0.readString();
		date = arg0.readString();
		md5 = arg0.readString();
		size = arg0.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(name);
		dest.writeString(downloadPath);
		dest.writeString(versionName);
		dest.writeInt(versionCode);
		dest.writeString(packageName);
		dest.writeString(iconPath);
		dest.writeString(date);
		dest.writeString(md5);
		dest.writeLong(size);
	}

	public final static Parcelable.Creator<AppInfo> CREATOR = new Creator<AppInfo>() {

		@Override
		public AppInfo createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new AppInfo(arg0);
		}

		@Override
		public AppInfo[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new AppInfo[arg0];
		}
	};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "AppInfo [name=" + name + ", downloadPath=" + downloadPath
				+ ", versionName=" + versionName + ", versionCode="
				+ versionCode + ", packageName=" + packageName + ", iconPath="
				+ iconPath + ", date=" + date + ", md5=" + md5 + ", size="
				+ size + "]";
	}

}
