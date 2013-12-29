package com.example.mapspot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nosfistis on 4/12/2013.
 */
public class DatabaseHandler {
    private static final String APPTAG = "MapSpot";
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_DESCRIPTION,
            MySQLiteHelper.COLUMN_CATEGORY,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE};

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

        long insertID = database.insert(MySQLiteHelper.MARKERS_TABLE, null, values);

        Cursor cursor = database.query(MySQLiteHelper.MARKERS_TABLE,
                null, MySQLiteHelper.COLUMN_ID + " = " + insertID, null,
                null, null, null);
        cursor.moveToFirst();
        MapMarker newMarker = cursorToMarker(cursor);
        return newMarker;
    }

    public int deleteMarker(long id) {
        int result = database.delete(MySQLiteHelper.MARKERS_TABLE, MySQLiteHelper.COLUMN_ID + " = " + id, null);
        return result;
    }

    public List<MapMarker> getAllMarkers() {
        List<MapMarker> markers = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.MARKERS_TABLE, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MapMarker marker = cursorToMarker(cursor);
            markers.add(marker);
            cursor.moveToNext();
        }
        return markers;
    }

    private MapMarker cursorToMarker(Cursor cursor) {
        MapMarker newMarker = new MapMarker(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_CATEGORY)),
                cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        return newMarker;
    }

    private class MySQLiteHelper extends SQLiteOpenHelper {
        private static final String MARKERS_TABLE = "marker_details";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_TITLE = "title";
        private static final String COLUMN_DESCRIPTION = "description";
        private static final String COLUMN_CATEGORY = "category";
        private static final String COLUMN_LATITUDE = "latitude";
        private static final String COLUMN_LONGITUDE = "longitude";
        private static final String DATABASE_NAME = "mapspot.db";
        private static final int DATABASE_VERSION = 1;
        private final String DATABASE_CREATE = "create table " + MARKERS_TABLE + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_TITLE + " varchar(255) not null, "
                + COLUMN_DESCRIPTION + " text, "
                + COLUMN_CATEGORY + " enum('recreation', 'custom location', 'gas station', 'food and drinks', 'supermarket') not null, "
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
            db.execSQL(DATABASE_CREATE);
            database = db;
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
            db.execSQL("DROP TABLE IF EXISTS " + MARKERS_TABLE);
            onCreate(db);
        }
    }
}
