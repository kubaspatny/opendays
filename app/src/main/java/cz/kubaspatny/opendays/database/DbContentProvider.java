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

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME, GUIDEDGROUPS);
        uriMatcher.addURI(AUTHORITY, DataContract.GuidedGroups.TABLE_NAME + "/#", GUIDEDGROUP_ID);
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

        switch(uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
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
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
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

        notifyChange(uri);
        return deletedRows;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case GUIDEDGROUPS:
                // no additional arguments needed
                break;
            case GUIDEDGROUP_ID:
                String id = uri.getPathSegments().get(1);
                selection = DataContract.GuidedGroups._ID + "=" + id +
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

//        if (!DataContract.hasCallerIsSyncAdapterParameter(uri)) {
//            Context context = getContext();
//            context.getContentResolver().notifyChange(uri, null);
//            Log.d("DbContentProvider", "notifyChange > NOTIFIED.");
//        } else {
//            Log.d("DbContentProvider", "notifyChange > DID NOT NOTIFY.");
//        }
    }

}
