package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import cz.kubaspatny.opendays.activity.BaseActivity;
import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.net.SyncEndpoint;
import cz.kubaspatny.opendays.oauth.AuthConstants;
import cz.kubaspatny.opendays.oauth.AuthServer;

/**
 * Created by Kuba on 11/3/2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    private final AccountManager mAccountManager;
    private final ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mAccountManager = AccountManager.get(context);
        this.mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.d(TAG, "Sync for account: " + account.name);

        try {
            loadGuidedGroups(account);
        } catch (LoginException e){
            Log.d(TAG, "loadGuidedGroups -> loginException: " + e.getLocalizedMessage());
            syncResult.stats.numAuthExceptions++;
        } catch (AuthenticatorException e){
            Log.d(TAG, "loadGuidedGroups -> loginException: " + e.getLocalizedMessage());
            syncResult.stats.numAuthExceptions++;
        } catch (IOException e){
            Log.d(TAG, "loadGuidedGroups -> IOException: " + e.getLocalizedMessage());
            syncResult.stats.numIoExceptions++;
        } catch (Exception e){
            Log.d(TAG, "loadGuidedGroups -> Exception: " + e.getLocalizedMessage());
            // send error broadcast
        }

    }

    private void loadGuidedGroups(Account account) throws Exception{

        Log.d(TAG, "loadGuidedGroups");



        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        List<GroupDto> groups = SyncEndpoint.getGroups(account, getAccessToken(account), 0, 25);

        if(groups != null){
            batch.add(ContentProviderOperation.newDelete(DbContentProvider.CONTENT_URI).build());
//            mContentResolver.delete(DbContentProvider.CONTENT_URI, null, null); // delete previous groups
        }

        for(GroupDto g : groups){
            ContentValues values = new ContentValues();
            values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID, g.getId());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION, g.getStartingPosition());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ACTIVE, g.isActive());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID, g.getRoute().getId());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME, g.getRoute().getName());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR, g.getRoute().getHexColor());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_INFORMATION, g.getRoute().getInformation());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP, g.getRoute().getDate().toInstant().toString());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_EVENT_ID, g.getRoute().getEvent().getId());
            values.put(DataContract.GuidedGroups.COLUMN_NAME_EVENT_NAME, g.getRoute().getEvent().getName());
            mContentResolver.insert(DbContentProvider.CONTENT_URI, values);
            batch.add(ContentProviderOperation.newInsert(DbContentProvider.CONTENT_URI).withValues(values).build());
        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

    }

    private String getAccessToken(Account account) throws LoginException, NetworkErrorException, IOException, AuthenticatorException, OperationCanceledException {

        String oldToken = mAccountManager.peekAuthToken(account, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);

        if (oldToken != null) {
            mAccountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE, oldToken);
        }

        String token = mAccountManager.blockingGetAuthToken(account, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, true);

        if(token == null || TextUtils.isEmpty(token)){
            throw new LoginException("Couldn't obtain access token.", 999);
        }

        return token;
    }


}
