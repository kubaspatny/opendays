package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.ui.activity.BaseActivity;
import cz.kubaspatny.opendays.util.AccountUtil;
import cz.kubaspatny.opendays.util.ConnectionUtils;
import cz.kubaspatny.opendays.util.TimeUtil;

/**
 * Created by Kuba on 13/3/2015.
 */
public class SyncHelper {

    private final static String TAG = SyncHelper.class.getSimpleName();
    private final static String LAST_SYNC = "cz.kubaspatny.opendays.sync.last_sync";

    public final static String LARGE_SYNC = "cz.kubaspatny.opendays.sync.large_sync";
    public final static String SMALL_SYNC = "cz.kubaspatny.opendays.sync.small_sync";

    private Context mContext;

    public SyncHelper(Context context) {
        mContext = context;
    }

    public static void requestManualSync(Context context, Account account){
        Bundle extras = new Bundle();
        extras.putBoolean(LARGE_SYNC, true);
        requestManualSync(context, account, extras);
    }

    public static void requestManualSync(Context context, Account account, Bundle bundle) {
        if (account != null) {
            Log.d(TAG, "requestManualSync > requesting sync for " + account.name);
            if(bundle == null) bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            // If refresh token expired, the automatic sync was disabled.
            // Enable it now to show the error message again.
            enableSync(context, account);

            ContentResolver.requestSync(account, AppConstants.AUTHORITY, bundle);
        } else {
            Log.d(TAG, "requestManualSync > cannot request sync without account!");
        }
    }

    public static void requestManualUploadSync(Context context, Account account) {
        if (account != null) {
            Log.d(TAG, "requestManualUploadSync > requesting sync for " + account.name);
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);

            // If refresh token expired, the automatic sync was disabled.
            // Enable it now to show the error message again.
            // If refresh token expired, the automatic sync was disabled.
            // Enable it now to show the error message again.
            enableSync(context, account);

            ContentResolver.requestSync(account, AppConstants.AUTHORITY, bundle);
        } else {
            Log.d(TAG, "requestManualUploadSync > cannot request sync without account!");
        }
    }

    public void performSync(SyncResult syncResult, Account account, Bundle extras) {

        Intent intent = new Intent(AppConstants.KEY_SYNC_STATUS);
        intent.putExtra(AppConstants.KEY_SYNC_STATUS_CODE, AppConstants.SYNC_STATUS_CODE_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        final boolean largeSync = extras.getBoolean(LARGE_SYNC, false);
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        Log.d(TAG, "Performing sync for account: " + account);

        try {

            if(uploadOnly){
                // upload
                doUploadSync();
            } else {
                Log.d(TAG, "isLargeSyncTime: " + isLargeSyncTime());

                if(largeSync || isLargeSyncTime()) {
                    Log.d(TAG, "Doing large sync.");
                    doLargeSync(account);
                } else {
                    Log.d(TAG, "Doing small sync.");
                    doSmallSync(account);
                }
            }

            recalculateSyncPeriod(account);

        } catch (LoginException | AuthenticatorException ex) {
            Log.d(TAG, "Login exception.");
        } catch(OperationCanceledException e) {
            Log.d(TAG, "Previous sync canceled.");
        } catch(Throwable throwable) {
            Log.d(TAG, "Throwable exception.");
            throwable.printStackTrace();
            syncResult.stats.numIoExceptions++;
        } finally {
            intent = new Intent(AppConstants.KEY_SYNC_STATUS);
            intent.putExtra(AppConstants.KEY_SYNC_STATUS_CODE, AppConstants.SYNC_STATUS_CODE_END);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            // TODO: if the activity is paused -> then goes active -> still showing progressbar
        }

    }

    private void doLargeSync(Account account) throws Exception { // TODO: get rid of account
        if(!ConnectionUtils.isConnected(mContext)) {
            Log.d(TAG, "doSync: Not connected to internet. Cannot sync.");
            return;
        }

        setLargeSyncTime();

        DataFetcher fetcher = new DataFetcher(mContext);
        fetcher.loadGuidedGroups(account);
        fetcher.loadManagedRoutes(account);

    }

    private void doSmallSync(Account account) throws Exception {

        String[] projectionGuidedGroups = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP};

        String[] projectionManagedRoutes = {DataContract.ManagedRoutes._ID,
                DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_ID,
                DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP};

        Cursor cursor = mContext.getContentResolver().query(DataContract.GuidedGroups.CONTENT_URI, projectionGuidedGroups, null, null, null);
        cursor.moveToFirst();

        DateTime after = DateTime.now().withTime(23, 59, 59, 0).minusDays(1);
        DateTime before = DateTime.now().withTime(0, 0, 1, 0).plusDays(1);

        DateTime routeTime;
        Map<Long, Long> routes = new HashMap<>();

        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){

            routeTime = TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP)));

            if(routeTime.isAfter(after) && routeTime.isBefore(before)){
                routes.put(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID))
                );
            }

            cursor.moveToNext();
        }

        cursor.close();
        cursor = mContext.getContentResolver().query(DataContract.ManagedRoutes.CONTENT_URI, projectionManagedRoutes, null, null, null);
        cursor.moveToFirst();

        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){

            Long routeId = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_ID));
            if(routes.containsKey(routeId)) {
                cursor.moveToNext();
                continue;
            }

            routeTime = TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP)));

            if(routeTime.isAfter(after) && routeTime.isBefore(before)){
                routes.put(
                        routeId,
                        null
                );
            }

            cursor.moveToNext();
        }

        cursor.close();

        Log.d(TAG, "doSmallSync, sync #routes: " + routes.size());

        DataFetcher fetcher = new DataFetcher(mContext);
        for(Map.Entry<Long, Long> e : routes.entrySet()){
            try {
                fetcher.loadRoute(e.getKey(), e.getValue());
            } catch (Exception ex){
                Log.d(TAG, "Error fetching route: " + ex.getLocalizedMessage());
            }
        }

    }

    private void doRouteSync(Account account, Long routeId){
        if(routeId == null || routeId <= 0) return;

        String[] projectionGuidedGroups = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP};

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(DataContract.GuidedGroups.CONTENT_URI,
                    projectionGuidedGroups,
                    DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID + "=?",
                    new String[]{routeId + ""},
                    null);

            cursor.moveToFirst();
            Long groupId = null;
            while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){
                groupId = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID));
            }

            DataFetcher fetcher = new DataFetcher(mContext);

            fetcher.loadRoute(routeId, groupId);
        } catch (Exception e){
            Log.e(TAG, "Exception while doRouteSync: " + e.getLocalizedMessage(), e);
        } finally {
            if(cursor != null) cursor.close();
        }

    }

    private void doUploadSync() throws Exception {
        if(!ConnectionUtils.isConnected(mContext)) {
            Log.d(TAG, "doUploadSync: Not connected to internet. Cannot sync.");
            return;
        }

        Log.d(TAG, "Performing doUploadSync");

        DataFetcher fetcher = new DataFetcher(mContext);
        fetcher.uploadLocationUpdates();
        fetcher.uploadGroupSizes();

    }

    private void setLargeSyncTime(){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(LAST_SYNC, DateTime.now().toInstant().toString());
        editor.commit();
    }

    private boolean isLargeSyncTime(){
        SharedPreferences prefs = getSharedPreferences();
        String timestamp = prefs.getString(LAST_SYNC, "");

        if(TextUtils.isEmpty(timestamp)) return true;
        DateTime time = TimeUtil.parseTimestamp(timestamp);
        return time.isBefore(DateTime.now().minusMinutes(5));
    }

    public static boolean isLargeSyncTime(Context context){
        SharedPreferences prefs = context.getSharedPreferences(BaseActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String timestamp = prefs.getString(LAST_SYNC, "");

        if(TextUtils.isEmpty(timestamp)) return true;
        DateTime time = TimeUtil.parseTimestamp(timestamp);
        return time.isBefore(DateTime.now().minusMinutes(5));
    }

    private SharedPreferences getSharedPreferences(){
        return mContext.getSharedPreferences(BaseActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public static void cancelSync(Context context, Account account){
        if (account != null) {
            if (isSyncPending(context, account)) ContentResolver.cancelSync(account, AppConstants.AUTHORITY);
        }
    }

    public static boolean isSyncPending(Context context, Account account){
        if (account != null) {
            boolean pending = ContentResolver.isSyncPending(account, AppConstants.AUTHORITY);
            boolean active = ContentResolver.isSyncActive(account, AppConstants.AUTHORITY);

            return (pending || active);
        }

        return false;
    }

    public static void disableSync(Context context, Account account){
        Log.d(TAG, "Disabling sync.");
        ContentResolver.setIsSyncable(account, AppConstants.AUTHORITY, 0);
        ContentResolver.setSyncAutomatically(account, AppConstants.AUTHORITY, false);
    }

    public static void enableSync(Context context, Account account){
        Log.d(TAG, "Enabling sync.");
        ContentResolver.setIsSyncable(account, AppConstants.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, AppConstants.AUTHORITY, true);
    }

    private void recalculateSyncPeriod(Account account){

        boolean routeSoon = false;

        String[] projectionGuidedGroups = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP};

        String[] projectionManagedRoutes = {DataContract.ManagedRoutes._ID,
                DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_ID,
                DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP};

        Cursor cursor = mContext.getContentResolver().query(DataContract.GuidedGroups.CONTENT_URI, projectionGuidedGroups, null, null, null);
        cursor.moveToFirst();

        DateTime after = DateTime.now().minusHours(1);
        DateTime before = DateTime.now().plusHours(1);

        DateTime routeTime;

        while(!routeSoon && cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){

            routeTime = TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP)));

            if(routeTime.isAfter(after) && routeTime.isBefore(before)){
                routeSoon = true;
                break;
            }

            cursor.moveToNext();
        }

        cursor.close();
        cursor = mContext.getContentResolver().query(DataContract.ManagedRoutes.CONTENT_URI, projectionManagedRoutes, null, null, null);
        cursor.moveToFirst();

        while(!routeSoon && cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){

            routeTime = TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP)));

            if(routeTime.isAfter(after) && routeTime.isBefore(before)){
                routeSoon = true;
                break;
            }

            cursor.moveToNext();
        }

        cursor.close();

        List<PeriodicSync> syncs = ContentResolver.getPeriodicSyncs(account, AppConstants.AUTHORITY);

        if(routeSoon){
            if(syncs.isEmpty() || syncs.get(0).period != AppConstants.MINUTE_SYNC_PERIOD){
                Log.d(TAG, "Setting minute sync period.");
                ContentResolver.addPeriodicSync(account, AppConstants.AUTHORITY, Bundle.EMPTY, AppConstants.MINUTE_SYNC_PERIOD);
            }
        } else {
            if(syncs.isEmpty() || syncs.get(0).period != AppConstants.HOUR_SYNC_PERIOD){
                Log.d(TAG, "Setting hour sync period.");
                ContentResolver.addPeriodicSync(account, AppConstants.AUTHORITY, Bundle.EMPTY, AppConstants.HOUR_SYNC_PERIOD);
            }
        }

    }

}
