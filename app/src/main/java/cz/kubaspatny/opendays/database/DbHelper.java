package cz.kubaspatny.opendays.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cz.kubaspatny.opendays.database.DataContract.*;

/**
 * Created by Kuba on 8/3/2015.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "OpenDays.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
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

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + GuidedGroups.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
