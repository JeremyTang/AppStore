package com.jingliang.appstore.db;

public class DBSource {

	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "StoreDatabase";

	public static class DownloadColumn {
		public static final String TABLE_NAME = "download";
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String PATH = "path";
		public static final String URL = "url";
		public static final String STATE = "state";
		public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS  "
				+ TABLE_NAME
				+ " ("
				+ ID
				+ " INTEGER primary key AUTOINCREMENT,"
				+ NAME
				+ " TEXT,"
				+ PATH
				+ " TEXT,"
				+ URL
				+ " TEXT,"
				+ STATE
				+ " INTEGER ) ";

		public static final String TABLE_INSERT = "INSERT INTO " + TABLE_NAME
				+ " (" + NAME + " ," + PATH + " ," + URL + " ," + STATE
				+ "  ) values (?,?,?,?) ";
	}

}
