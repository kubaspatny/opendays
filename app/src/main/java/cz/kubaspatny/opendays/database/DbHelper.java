package cz.kubaspatny.opendays.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cz.kubaspatny.opendays.database.DataContract.*;

/**
 * Helper class used for database and table creation.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "OpenDays.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_GUIDEDGROUPS =
            "CREATE TABLE " + GuidedGroups.TABLE_NAME + " (" +
                    GuidedGroups._ID + " INTEGER PRIMARY KEY," +
                    GuidedGroups.COLUMN_NAME_GROUP_ID + INT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION + INT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_GROUP_ACTIVE + INT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_ROUTE_NAME + TEXT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_ROUTE_COLOR + TEXT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_ROUTE_INFORMATION + TEXT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_EVENT_ID + INT_TYPE + COMMA_SEP +
                    GuidedGroups.COLUMN_NAME_EVENT_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_GUIDEDGROUPS = "DROP TABLE IF EXISTS " + GuidedGroups.TABLE_NAME;

    private static final String SQL_CREATE_MANAGED_ROUTES =
            "CREATE TABLE " + ManagedRoutes.TABLE_NAME + " (" +
                    ManagedRoutes._ID + " INTEGER PRIMARY KEY," +
                    ManagedRoutes.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    ManagedRoutes.COLUMN_NAME_ROUTE_NAME + TEXT_TYPE + COMMA_SEP +
                    ManagedRoutes.COLUMN_NAME_ROUTE_COLOR + TEXT_TYPE + COMMA_SEP +
                    ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_MANAGED_ROUTES = "DROP TABLE IF EXISTS " + ManagedRoutes.TABLE_NAME;

    private static final String SQL_CREATE_ROUTE =
            "CREATE TABLE " + Route.TABLE_NAME + " (" +
                    Route._ID + " INTEGER PRIMARY KEY," +
                    Route.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_ROUTE_NAME + TEXT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_ROUTE_COLOR + TEXT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_ROUTE_INFORMATION + TEXT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_ROUTE_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_EVENT_ID + INT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_EVENT_NAME + TEXT_TYPE + COMMA_SEP +
                    Route.COLUMN_NAME_EVENT_INFORMATION + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ROUTE = "DROP TABLE IF EXISTS " + Route.TABLE_NAME;

    private static final String SQL_CREATE_STATION =
            "CREATE TABLE " + Station.TABLE_NAME + " (" +
                    Station._ID + " INTEGER PRIMARY KEY," +
                    Station.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_ID + INT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_NAME + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_LOCATION + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_INFORMATION + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_SEQ_POSITION + INT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_TIME_LIMIT + INT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_TIME_RELOCATION + INT_TYPE + COMMA_SEP +
                    Station.COLUMN_NAME_STATION_STATUS + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_STATION = "DROP TABLE IF EXISTS " + Station.TABLE_NAME;

    private static final String SQL_CREATE_GROUPLOCATIONS =
            "CREATE TABLE " + GroupLocations.TABLE_NAME + " (" +
                    GroupLocations._ID + " INTEGER PRIMARY KEY," +
                    GroupLocations.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_STATION_ID + INT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE + TEXT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_GROUP_ID + INT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_GROUP_GUIDE + TEXT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_GROUP_STATUS + TEXT_TYPE + COMMA_SEP +
                    GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_GROUPLOCATIONS = "DROP TABLE IF EXISTS " + GroupLocations.TABLE_NAME;

    private static final String SQL_CREATE_LOCATIONUPDATES =
            "CREATE TABLE " + LocationUpdates.TABLE_NAME + " (" +
                    LocationUpdates._ID + " INTEGER PRIMARY KEY," +
                    LocationUpdates.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    LocationUpdates.COLUMN_NAME_STATION_ID + INT_TYPE + COMMA_SEP +
                    LocationUpdates.COLUMN_NAME_GROUP_ID + INT_TYPE + COMMA_SEP +
                    LocationUpdates.COLUMN_NAME_ROUTE_ID + INT_TYPE + COMMA_SEP +
                    LocationUpdates.COLUMN_NAME_LOCATION_UPDATE_TYPE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_LOCATIONUPDATES = "DROP TABLE IF EXISTS " + LocationUpdates.TABLE_NAME;

    private static final String SQL_CREATE_GROUPSIZES =
            "CREATE TABLE " + GroupSizes.TABLE_NAME + " (" +
                    GroupSizes._ID + " INTEGER PRIMARY KEY," +
                    GroupSizes.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    GroupSizes.COLUMN_NAME_GROUP_ID + INT_TYPE + COMMA_SEP +
                    GroupSizes.COLUMN_NAME_GROUP_SIZE + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_GROUPSIZES = "DROP TABLE IF EXISTS " + GroupSizes.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Runs scripts to create tables when a new database has been created.
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GUIDEDGROUPS);
        db.execSQL(SQL_CREATE_ROUTE);
        db.execSQL(SQL_CREATE_STATION);
        db.execSQL(SQL_CREATE_GROUPLOCATIONS);
        db.execSQL(SQL_CREATE_LOCATIONUPDATES);
        db.execSQL(SQL_CREATE_GROUPSIZES);
        db.execSQL(SQL_CREATE_MANAGED_ROUTES);
    }

    /**
     * Runs scripts to delete and recreate tables when the database version has been upgraded.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_GUIDEDGROUPS);
        db.execSQL(SQL_DELETE_ROUTE);
        db.execSQL(SQL_DELETE_STATION);
        db.execSQL(SQL_DELETE_GROUPLOCATIONS);
        db.execSQL(SQL_DELETE_LOCATIONUPDATES);
        db.execSQL(SQL_DELETE_GROUPSIZES);
        db.execSQL(SQL_DELETE_MANAGED_ROUTES);
        onCreate(db);
    }

    /**
     * Runs scripts to delete and recreate tables when the database version has been downgraded.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Clears user data in all tables by deleting them and recreating them afterwards.
     */
    public void clearUserData(){
        onUpgrade(getWritableDatabase(), 0, 0);
    }

}
