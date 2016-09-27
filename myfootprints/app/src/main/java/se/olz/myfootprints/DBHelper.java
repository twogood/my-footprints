package se.olz.myfootprints;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Coordinates.db";
    public static final String TABLE_NAME = "positions";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_SESSION = "session";
    public static final String COLUMN_NAME_TIMESTAMP = "accessedTimestamp";
    public static final String COLUMN_NAME_LATITUDE = "latitude";
    public static final String COLUMN_NAME_LONGITUDE= "longitude";
    public static final String COLUMN_NAME_OCCURANCE= "occurance";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String INT_TYPE = " INT";
    private static final String LONG_TYPE = " LONG";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_SESSION + LONG_TYPE + COMMA_SEP +
                    COLUMN_NAME_TIMESTAMP + LONG_TYPE + COMMA_SEP +
                    COLUMN_NAME_LATITUDE + DOUBLE_TYPE + COMMA_SEP +
                    COLUMN_NAME_LONGITUDE + DOUBLE_TYPE + COMMA_SEP +
                    COLUMN_NAME_OCCURANCE + INT_TYPE + " )";
    public static final String TAG = DBHelper.class.getSimpleName();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void clean() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    private static double roundDown4(double d) {
        return (long) (d * 1e4) / 1e4;
    }

    private int findOccurance(double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {
                valueOf(latitude),
                valueOf(longitude)
        };
        Cursor res = db.query(TABLE_NAME, new String[] {COLUMN_NAME_ID}, COLUMN_NAME_LATITUDE+" = ? AND "+COLUMN_NAME_LONGITUDE+" = ?", args, null, null, null);
        int id = -1;
        if (res.getCount() > 0) {
            res.moveToLast();
            id = res.getInt(res.getColumnIndex(COLUMN_NAME_ID));
            res.close();
        }
        return id;
    }

    public boolean insertCoordinates(long session, long accessedTimestamp, double latitude, double longitude)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        latitude = roundDown4(latitude);
        longitude = roundDown4(longitude);
        int id = findOccurance(latitude, longitude);
        if (id > -1) {
            db.execSQL("UPDATE "+TABLE_NAME+" SET "+COLUMN_NAME_OCCURANCE+" = "+COLUMN_NAME_OCCURANCE+" + 1 WHERE "+COLUMN_NAME_ID+" = " + id);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME_SESSION, session);
            contentValues.put(COLUMN_NAME_TIMESTAMP, accessedTimestamp);
            contentValues.put(COLUMN_NAME_LATITUDE, latitude);
            contentValues.put(COLUMN_NAME_LONGITUDE, longitude);
            contentValues.put(COLUMN_NAME_OCCURANCE, 1);
            db.insert(TABLE_NAME, null, contentValues);
        }

        return true;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }

    public ArrayList<CoordinatesContainer> getAllEntries() {
        ArrayList<CoordinatesContainer> CoordinateEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + TABLE_NAME, null );
        res.moveToFirst();

        int id, occurance;
        long session, accessedTimestamp;
        double latitude, longitude;

        while(!res.isAfterLast()){
            id = res.getInt(res.getColumnIndex(COLUMN_NAME_ID));
            session = res.getLong(res.getColumnIndex(COLUMN_NAME_SESSION));
            accessedTimestamp = res.getLong(res.getColumnIndex(COLUMN_NAME_TIMESTAMP));
            latitude = res.getDouble(res.getColumnIndex(COLUMN_NAME_LATITUDE));
            longitude = res.getDouble(res.getColumnIndex(COLUMN_NAME_LONGITUDE));
            occurance = res.getInt(res.getColumnIndex(COLUMN_NAME_OCCURANCE));
            CoordinatesContainer row = new CoordinatesContainer(id, session, accessedTimestamp, latitude, longitude, occurance);
            CoordinateEntries.add(row);
            res.moveToNext();
        }
        res.close();
        return CoordinateEntries;
    }
}