package cz.kubaspatny.opendays.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Kuba on 8/3/2015.
 */
public class DbContentProvider extends ContentProvider {

    private DbHelper dbHelper;
    private static final int ALL_GUIDEDGROUPS = 1;
    private static final int SINGLE_GUIDEDGROUP = 2;

    private static final String AUTHORITY = "cz.kubaspatny.opendays.dbcontentprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DataContract.GuidedGroups.TABLE_NAME);
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME, ALL_GUIDEDGROUPS);
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME + "/#", SINGLE_GUIDEDGROUP);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return false;
    }

    /**
     * Should follow these rules:
     * vnd.android.cursor.dir/vnd.com.example.provider.table1 --- dir if more than one row
     * vnd.android.cursor.item/vnd.com.example.provider.table1 --- item if only one row
     *
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {

        switch(uriMatcher.match(uri)){
            case ALL_GUIDEDGROUPS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.GuidedGroups.TABLE_NAME;
            case SINGLE_GUIDEDGROUP:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.GuidedGroups.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)){
            case ALL_GUIDEDGROUPS:
                id = database.insert(DataContract.GuidedGroups.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DataContract.GuidedGroups.TABLE_NAME);

        switch(uriMatcher.match(uri)){
            case ALL_GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case SINGLE_GUIDEDGROUP:
                String id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.GuidedGroups._ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case ALL_GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case SINGLE_GUIDEDGROUP:
                String id = uri.getPathSegments().get(1);
                selection = DataContract.GuidedGroups._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        int deletedRows = database.delete(DataContract.GuidedGroups.TABLE_NAME,
                selection,
                selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case ALL_GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case SINGLE_GUIDEDGROUP:
                String id = uri.getPathSegments().get(1);
                selection = DataContract.GuidedGroups._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        int updatedRows = database.update(DataContract.GuidedGroups.TABLE_NAME,
                contentValues, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return updatedRows;

    }

}
