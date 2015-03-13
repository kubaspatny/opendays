package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.util.AccountUtil;

/**
 * Created by Kuba on 13/3/2015.
 */
public class DataFetcher {

    private final static String TAG = DataFetcher.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    public DataFetcher(Context mContext) {
        this.mContext = mContext;
        this.mContentResolver = mContext.getContentResolver();
    }

    public void loadGuidedGroups(Account account) throws Exception{
        Log.d(TAG, "loadGuidedGroups");

        List<GroupDto> groups = SyncEndpoint.getGroups(account, AccountUtil.getAccessToken(mContext, account), 0, 100); // TODO: Add parameters from bundle
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        if(groups != null){
            batch.add(ContentProviderOperation.newDelete(DataContract.GuidedGroups.CONTENT_URI).build());

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
                batch.add(ContentProviderOperation.newInsert(DataContract.GuidedGroups.CONTENT_URI).withValues(values).build());
            }

            mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

        }

    }

}
