package cz.kubaspatny.opendays.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import static cz.kubaspatny.opendays.app.AppConstants.*;

/**
 * Created by Kuba on 8/3/2015.
 */
public class DbContentProvider extends ContentProvider {

    private DbHelper dbHelper;
    private static final int GUIDEDGROUPS = 100;
    private static final int GUIDEDGROUP_ID = 101;

    private static final int ROUTES = 200;
    private static final int ROUTE_ID = 201;

    private static final int STATIONS = 300;
    private static final int STATION_ID = 301;

    private static final int GROUPLOCATIONS = 400;
    private static final int GROUPLOCATION_ID = 401;

    private static final int LOCATIONUPDATES = 500;
    private static final int LOCATIONUPDATE_ID = 501;



    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME, GUIDEDGROUPS);
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME + "/#", GUIDEDGROUP_ID);

        uriMatcher.addURI(AUTHORITY, DataContract.Route.TABLE_NAME, ROUTES);
        uriMatcher.addURI(AUTHORITY, DataContract.Route.TABLE_NAME + "/#", ROUTE_ID);

        uriMatcher.addURI(AUTHORITY, DataContract.Station.TABLE_NAME, STATIONS);
        uriMatcher.addURI(AUTHORITY, DataContract.Station.TABLE_NAME + "/#", STATION_ID);

        uriMatcher.addURI(AUTHORITY, DataContract.GroupLocations.TABLE_NAME, GROUPLOCATIONS);
        uriMatcher.addURI(AUTHORITY, DataContract.GroupLocations.TABLE_NAME + "/#", GROUPLOCATION_ID);

        uriMatcher.addURI(AUTHORITY, DataContract.LocationUpdates.TABLE_NAME, LOCATIONUPDATES);
        uriMatcher.addURI(AUTHORITY, DataContract.LocationUpdates.TABLE_NAME + "/#", LOCATIONUPDATE_ID);
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
            case GUIDEDGROUPS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.GuidedGroups.TABLE_NAME;
            case GUIDEDGROUP_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.GuidedGroups.TABLE_NAME;

            case ROUTES:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.Route.TABLE_NAME;
            case ROUTE_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.Route.TABLE_NAME;

            case STATIONS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.Station.TABLE_NAME;
            case STATION_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.Station.TABLE_NAME;

            case GROUPLOCATIONS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.GroupLocations.TABLE_NAME;
            case GROUPLOCATION_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.GroupLocations.TABLE_NAME;

            case LOCATIONUPDATES:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DataContract.LocationUpdates.TABLE_NAME;
            case LOCATIONUPDATE_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DataContract.LocationUpdates.TABLE_NAME;

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                id = database.insert(DataContract.GuidedGroups.TABLE_NAME, null, contentValues);
                break;
            case ROUTES:
                id = database.insert(DataContract.Route.TABLE_NAME, null, contentValues);
                break;
            case STATIONS:
                id = database.insert(DataContract.Station.TABLE_NAME, null, contentValues);
                break;
            case GROUPLOCATIONS:
                id = database.insert(DataContract.GroupLocations.TABLE_NAME, null, contentValues);
                break;
            case LOCATIONUPDATES:
                id = database.insert(DataContract.LocationUpdates.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        notifyChange(uri);
        return Uri.parse(CONTENT_URI + "/" + id);

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DataContract.GuidedGroups.TABLE_NAME);

        String id;

        switch(uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
                id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.GuidedGroups._ID + "=" + id);
                break;

            case ROUTES:
                // no additional arguments needed
                break;
            case ROUTE_ID:
                id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.Route._ID + "=" + id);
                break;

            case STATIONS:
                // no additional arguments needed
                break;
            case STATION_ID:
                id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.Station._ID + "=" + id);
                break;

            case GROUPLOCATIONS:
                // no additional arguments needed
                break;
            case GROUPLOCATION_ID:
                id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.GroupLocations._ID + "=" + id);
                break;

            case LOCATIONUPDATES:
                // no additional arguments needed
                break;
            case LOCATIONUPDATE_ID:
                id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(DataContract.LocationUpdates._ID + "=" + id);
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
        String id;

        switch (uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.GuidedGroups._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case ROUTES:
                // no additional arguments needed
                break;
            case ROUTE_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.Route._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case STATIONS:
                // no additional arguments needed
                break;
            case STATION_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.Station._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case GROUPLOCATIONS:
                // no additional arguments needed
                break;
            case GROUPLOCATION_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.GroupLocations._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case LOCATIONUPDATES:
                // no additional arguments needed
                break;
            case LOCATIONUPDATE_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.LocationUpdates._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        int deletedRows = database.delete(DataContract.GuidedGroups.TABLE_NAME,
                selection,
                selectionArgs);

        notifyChange(uri);
        return deletedRows;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String id;

        switch (uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.GuidedGroups._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case ROUTES:
                // no additional arguments needed
                break;
            case ROUTE_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.Route._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case STATIONS:
                // no additional arguments needed
                break;
            case STATION_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.Station._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case GROUPLOCATIONS:
                // no additional arguments needed
                break;
            case GROUPLOCATION_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.GroupLocations._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            case LOCATIONUPDATES:
                // no additional arguments needed
                break;
            case LOCATIONUPDATE_ID:
                id = uri.getPathSegments().get(1);
                selection = DataContract.LocationUpdates._ID + "=" + id +
                        (TextUtils.isEmpty(selection) ? "" : "AND (" + selection + ")");
                break;

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        int updatedRows = database.update(DataContract.GuidedGroups.TABLE_NAME,
                contentValues, selection, selectionArgs);

        notifyChange(uri);
        return updatedRows;

    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private void notifyChange(Uri uri) {
        // We only notify changes if the caller is not the sync adapter.
        // The sync adapter has the responsibility of notifying changes (it can do so
        // more intelligently than we can -- for example, doing it only once at the end
        // of the sync instead of issuing thousands of notifications for each record).

        boolean syncToNetwork = !DataContract.hasCallerIsSyncAdapterParameter(uri);
        getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);

    }

}
