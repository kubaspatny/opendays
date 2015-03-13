package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.net.ConnectionUtils;
import cz.kubaspatny.opendays.util.AccountUtil;

/**
 * Created by Kuba on 13/3/2015.
 */
public class SyncHelper {

    private final static String TAG = SyncHelper.class.getSimpleName();

    private Context mContext;

    public SyncHelper(Context context) {
        mContext = context;
    }

    public static void requestManualSync(Account account) {
        if (account != null) {
            Log.d(TAG, "requestManualSync > requesting sync for " + account.name);
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            ContentResolver.setSyncAutomatically(account, AppConstants.AUTHORITY, true);
            ContentResolver.setIsSyncable(account, AppConstants.AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(account, AppConstants.AUTHORITY);
            boolean active = ContentResolver.isSyncActive(account, AppConstants.AUTHORITY);

            if (pending || active) ContentResolver.cancelSync(account, AppConstants.AUTHORITY);

            ContentResolver.requestSync(account, AppConstants.AUTHORITY, bundle);
        } else {
            Log.d(TAG, "requestManualSync > cannot request sync without account!");
        }
    }

    public void performSync(SyncResult syncResult, Account account, Bundle extras) {

        Intent intent = new Intent(AppConstants.KEY_SYNC_STATUS);
        intent.putExtra(AppConstants.KEY_SYNC_STATUS_CODE, AppConstants.SYNC_STATUS_CODE_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        Log.d(TAG, "Performing sync for account: " + account);

        try {
            doSync(account);
        } catch (LoginException | AuthenticatorException ex) {
            syncResult.stats.numAuthExceptions++;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            syncResult.stats.numIoExceptions++;
        } finally {
            intent = new Intent(AppConstants.KEY_SYNC_STATUS);
            intent.putExtra(AppConstants.KEY_SYNC_STATUS_CODE, AppConstants.SYNC_STATUS_CODE_END);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

    }

    private void doSync(Account account) throws Exception {
        if(!ConnectionUtils.isConnected(mContext)) {
            Log.d(TAG, "doSync: Not connected to internet. Cannot sync.");
            return;
        }

        new DataFetcher(mContext).loadGuidedGroups(account);
    }

}
