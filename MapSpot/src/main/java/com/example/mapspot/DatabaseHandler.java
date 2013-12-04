package com.example.mapspot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by Nosfistis on 4/12/2013.
 */
public class DatabaseHandler {
	private static final String APPTAG = "MapSpot";
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	public DatabaseHandler(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public MapMarker createMarker(String title, String description, String category, double latitude, double longitude) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_CATEGORY, category);
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);

		long insertID = database.insert(MySQLiteHelper.TABLE_NAME, null, values);

		Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
				null, MySQLiteHelper.COLUMN_ID + " = " + insertID, null,
				null, null, null);
		cursor.moveToFirst();
		MapMarker newMarker = new MapMarker(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)),
				cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION)),
				cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_CATEGORY)),
				cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LATITUDE)),
				cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LONGITUDE)));
		return newMarker;
	}

	public void deleteMarker(MapMarker marker) {
		// TODO: implementation deleteMarker() from database
	}

	public List<MapMarker> getAllMarkers() {
		// TODO: implementation getAllMarkers() from database
		return null;
	}

	private class MySQLiteHelper extends SQLiteOpenHelper {
		private static final String TABLE_NAME = "marker_details";
		private static final String COLUMN_ID = "_id";
		private static final String COLUMN_TITLE = "title";
		private static final String COLUMN_DESCRIPTION = "description";
		private static final String COLUMN_CATEGORY = "category";
		private static final String COLUMN_LATITUDE = "latitude";
		private static final String COLUMN_LONGITUDE = "longitude";
		// TODO: Preset categories
		private static final String DATABASE_NAME = "mapspot.db";
		private static final int DATABASE_VERSION = 1;
		private final String DATABASE_CREATE = "create table " + TABLE_NAME + "("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_TITLE + " varchar(255) not null, "
				+ COLUMN_DESCRIPTION + " text, "
				+ COLUMN_CATEGORY + " varchar(40) not null, "
				+ COLUMN_LATITUDE + " double not null, "
				+ COLUMN_LONGITUDE + " double not null"
				+ ");";

		/**
		 * Create a helper object to create, open, and/or manage a database.
		 * This method always returns very quickly.  The database is not actually
		 * created or opened until one of {@link #getWritableDatabase} or
		 * {@link #getReadableDatabase} is called.
		 *
		 * @param context to use to open or create the database
		 */
		public MySQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Called when the database is created for the first time. This is where the
		 * creation of tables and the initial population of the tables should happen.
		 *
		 * @param db The database.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			database.execSQL(DATABASE_CREATE);
		}

		/**
		 * Called when the database needs to be upgraded. The implementation
		 * should use this method to drop tables, add tables, or do anything else it
		 * needs to upgrade to the new schema version.
		 * The SQLite ALTER TABLE documentation can be found
		 * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
		 * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
		 * you can use ALTER TABLE to rename the old table, then create the new table and then
		 * populate the new table with the contents of the old table.
		 * This method executes within a transaction.  If an exception is thrown, all changes
		 * will automatically be rolled back.
		 *
		 * @param db         The database.
		 * @param oldVersion The old database version.
		 * @param newVersion The new database version.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(APPTAG,
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
