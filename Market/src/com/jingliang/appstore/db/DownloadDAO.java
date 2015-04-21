package com.jingliang.appstore.db;

import java.util.ArrayList;
import java.util.List;

import com.jingliang.appstore.bean.DownloadInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DownloadDAO {

	public static DBHelper mData = null;
	public static DownloadDAO mDao = null;

	public static DownloadDAO newInstance(Context context) {
		if (mDao == null) {
			mDao = new DownloadDAO(context);
		}
		return mDao;
	}

	private DownloadDAO(Context context) {
		super();
		mData = DBHelper.newInstance(context);
	}

	public boolean insert(DownloadInfo download) {
		SQLiteDatabase db = mData.getWritableDatabase();
		db.beginTransaction();// 开启事物机制能提高SQLite的存储效率
		// 每一次操作数据库就相当于操作了一次磁盘，开启事物之后相当于中操作了一次磁盘，提高效率（这里只进行了一次操作有点多余）
		boolean result = true;
		try {
			ContentValues values = new ContentValues();
			values.put(DBSource.DownloadColumn.NAME, download.getFileName());
			values.put(DBSource.DownloadColumn.PATH, download.getFileSavePath());
			values.put(DBSource.DownloadColumn.STATE, download.getState()
					.value());
			values.put(DBSource.DownloadColumn.URL, download.getDownloadUrl());
			if (db.insert(DBSource.DownloadColumn.TABLE_NAME, null, values) < 0) {
				result = false;
			}
			if (result) {
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		} finally {
			db.endTransaction();
			db.close();
		}
		return result;
	}

	public boolean insert(List<DownloadInfo> list) {
		if (list == null) {
			return false;
		}
		SQLiteDatabase db = mData.getWritableDatabase();
		db.beginTransaction();// 开启事物机制能提高SQLite的存储效率
		boolean result = true;
		try {
			SQLiteStatement ssm = db
					.compileStatement(DBSource.DownloadColumn.TABLE_INSERT);// 效率最高的方法
			for (DownloadInfo download : list) {
				ssm.bindString(1, download.getFileName());
				ssm.bindString(2, download.getFileSavePath());
				ssm.bindString(3, download.getDownloadUrl());
				ssm.bindLong(4, download.getState().value());
				if (ssm.executeInsert() < 0) {
					result = false;
				}
			}
			if (result) {
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		} finally {
			db.endTransaction();
			db.close();
		}
		return result;
	}

	public long delete(DownloadInfo download) {
		SQLiteDatabase db = mData.getWritableDatabase();
		long result = 0;
		result = db.delete(DBSource.DownloadColumn.TABLE_NAME,
				DBSource.DownloadColumn.ID + " = ?",
				new String[] { download.getId() + "" });
		db.close();
		return result;
	}

	public void update(DownloadInfo download) {
		SQLiteDatabase db = mData.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBSource.DownloadColumn.NAME, download.getFileName());
		values.put(DBSource.DownloadColumn.PATH, download.getFileSavePath());
		values.put(DBSource.DownloadColumn.STATE, download.getState().value());
		values.put(DBSource.DownloadColumn.URL, download.getDownloadUrl());
		db.update(DBSource.DownloadColumn.TABLE_NAME, values,
				DBSource.DownloadColumn.ID + " = ?",
				new String[] { download.getId() + "" });
	}

	public List<DownloadInfo> selectDownload() {
		SQLiteDatabase db = mData.getReadableDatabase();
		List<DownloadInfo> list = new ArrayList<DownloadInfo>();
		Cursor c = db.query(DBSource.DownloadColumn.TABLE_NAME, null, null,
				null, null, null, DBSource.DownloadColumn.ID);
		if (c != null) {
			while (c.moveToNext()) {
				// DownloadInfo downloadInfo = new DownloadInfo();
			}
		}
		db.close();
		return list;
	}
}
