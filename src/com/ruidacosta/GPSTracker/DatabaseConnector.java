package com.ruidacosta.GPSTracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.StringBuilderPrinter;

import java.util.*;

/**
 * Created by bubum on 25/10/2015.
 */
public class DatabaseConnector extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "GPSTracker";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_POINTS = "points";
    public static final String COLUMN_POINTS_TIMESTAMP = "timestamp";
    public static final String COLUMN_POINTS_LATITUDE = "latitude";
    public static final String COLUMN_POINTS_LONGITUDE = "longitude";
    public static final String COLUMN_POINTS_ALTITUDE = "altitude";
    public static final String COLUMN_POINTS_ACCURACY = "accuracy";
    public static final String COLUMN_POINTS_BEARING = "bearing";
    public static final String COLUMN_POINTS_SPEED = "speed";

    public static final String CREATE_TABLE_POINTS = "CREATE TABLE IF NOT EXISTS " + TABLE_POINTS + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", " + COLUMN_POINTS_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ", " + COLUMN_POINTS_LATITUDE + " REAL"
            + ", " + COLUMN_POINTS_LONGITUDE + " REAL"
            + ", " + COLUMN_POINTS_ALTITUDE + " REAL"
            + ", " + COLUMN_POINTS_ACCURACY + " REAL"
            + ", " + COLUMN_POINTS_BEARING + " REAL"
            + ", " + COLUMN_POINTS_SPEED + " REAL"
            + ")";

    public static final String TABLE_ROUTES = "routes";
    public static final String COLUMN_ROUTES_NAME = "name";

    public static final String CREATE_TABLE_ROUTES = "CREATE TABLE IF NOT EXISTS " + TABLE_ROUTES + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", " + COLUMN_ROUTES_NAME + " TEXT"
            + ")";

    public static final String TABLE_ROUTE_POINTS = "route_points";
    public static final String COLUMN_ROUTE_POINTS_ROUTE = "route";
    public static final String COLUMN_ROUTE_POINTS_POINT = "point";

    public static final String CREATE_TABLE_ROUTE_POINTS = "CREATE TABLE IF NOT EXISTS " + TABLE_ROUTE_POINTS + " ("
            + COLUMN_ROUTE_POINTS_ROUTE + " INTEGER"
            + "," + COLUMN_ROUTE_POINTS_POINT + " INTEGER"
            + "," + "PRIMARY KEY (" + COLUMN_ROUTE_POINTS_ROUTE + "," + COLUMN_ROUTE_POINTS_POINT + ")"
            + "," + "FOREIGN KEY (" + COLUMN_ROUTE_POINTS_ROUTE + ") REFERENCES " + TABLE_ROUTES + "(_id)"
            + "," + "FOREIGN KEY (" + COLUMN_ROUTE_POINTS_POINT + ") REFERENCES " + TABLE_POINTS + "(_id)"
            + ")";

    private SQLiteDatabase database;

    public DatabaseConnector(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_POINTS);
        sqLiteDatabase.execSQL(CREATE_TABLE_ROUTES);
        sqLiteDatabase.execSQL(CREATE_TABLE_ROUTE_POINTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
        switch (old_version) {
            case 1:
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE_POINTS);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
                this.onCreate(sqLiteDatabase);
            default:
                break;
        }
    }

    public void open() {
        this.database = this.getWritableDatabase();
    }

    public void close() {
        this.database.close();
    }

    public long insertRoute(String route_name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROUTES_NAME, route_name);
        return this.database.insert(DatabaseConnector.TABLE_ROUTES,null,values);

//        String sqlStatement = "INSERT INTO ? (?) VALUES (?)";
//        this.database.execSQL(sqlStatement,new String[]{ DatabaseConnector.TABLE_ROUTES, DatabaseConnector.COLUMN_ROUTES_NAME, route_name});
//        Cursor cursor = this.database.query(DatabaseConnector.TABLE_ROUTES, new String[]{ "max(" + DatabaseConnector.COLUMN_ROUTES_ROUTEID + ")" }, null, null, null,null,null);
//        cursor.close();
//        return 0;
    }

    public void insertPoint(long route_id, double latitude, double longitude, double altitude, float accuracy, float bearing, float speed) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_POINTS_LATITUDE, latitude);
        values.put(COLUMN_POINTS_LONGITUDE, longitude);
        values.put(COLUMN_POINTS_ALTITUDE, altitude);
        values.put(COLUMN_POINTS_ACCURACY, accuracy);
        values.put(COLUMN_POINTS_BEARING, bearing);
        values.put(COLUMN_POINTS_SPEED, speed);
        long point_id = this.database.insert(TABLE_POINTS, null, values);

        values = new ContentValues();
        values.put(COLUMN_ROUTE_POINTS_ROUTE, route_id);
        values.put(COLUMN_ROUTE_POINTS_POINT, point_id);
        this.database.insert(TABLE_ROUTE_POINTS, null, values);
    }

    /*
    public String[] getRoutes() {
        ArrayList<String> routes = new ArrayList<String>();
        Cursor cursor = database.query(DatabaseConnector.TABLE_ROUTES, new String[]{COLUMN_ROUTES_NAME},null,null,null,null,null);
        while (cursor.moveToNext()) {
            routes.add(cursor.getString(cursor.getColumnIndex(DatabaseConnector.COLUMN_ROUTES_NAME)));
        }
        return routes.toArray(new String[routes.size()]);
    }
    */

    public Cursor getRoutes() {
        return database.query(DatabaseConnector.TABLE_ROUTES, new String[]{"_id",COLUMN_ROUTES_NAME},null,null,null,null,null);
    }

    public String getRouteById(long id) {
        Cursor cursor = database.query(DatabaseConnector.TABLE_ROUTES,new String[]{COLUMN_ROUTES_NAME},"_id=?",new String[]{Long.toString(id)},null,null,null,"1");
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(DatabaseConnector.COLUMN_ROUTES_NAME));
    }

    public String getPointsByRouteId(long id) {
        String result = "";
        String query = "SELECT "
                + COLUMN_POINTS_TIMESTAMP + ", "
                + COLUMN_POINTS_LATITUDE + ", "
                + COLUMN_POINTS_LONGITUDE + ", "
                + COLUMN_POINTS_ALTITUDE + ", "
                + COLUMN_POINTS_ACCURACY + ", "
                + COLUMN_POINTS_BEARING + ", "
                + COLUMN_POINTS_SPEED
                + " FROM " + TABLE_ROUTE_POINTS + " a, " + TABLE_POINTS + " b"
                + " WHERE a." + COLUMN_ROUTE_POINTS_POINT + " = b._id"
                + " AND a." + COLUMN_ROUTE_POINTS_ROUTE + " = ?";
        Cursor cursor = database.rawQuery(query,new String[] { Long.toString(id)});

        while (cursor.moveToNext())
        {
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_TIMESTAMP)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_LATITUDE)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_LONGITUDE)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_ALTITUDE)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_ACCURACY)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_BEARING)) + ", ";
            result += cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_SPEED)) + "\n";
        }

        return result;
    }
}
