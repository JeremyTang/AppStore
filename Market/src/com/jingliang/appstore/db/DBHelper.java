package com.jingliang.appstore.db;

import com.jingliang.appstore.utils.LogUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static DBHelper mDB = null;

	public static DBHelper newInstance(Context context) {
		if (mDB == null) {
			mDB = new DBHelper(context);
		}
		return mDB;
	}

	private DBHelper(Context context) {
		super(context, DBSource.DB_NAME, null, DBSource.DB_VERSION, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DBSource.DownloadColumn.TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		LogUtil.logDebug("Version = " + oldVersion + " ---- " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS+ " + DBSource.DownloadColumn.TABLE_NAME);
		onCreate(db);
	}

}
