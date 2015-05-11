package cz.kubaspatny.opendays.util;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.ArrayList;

import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.sync.SyncHelper;

/**
 * Created by Kuba on 28/3/2015.
 */
public class DbUtil {

    /**
     * Saves location update to the database and triggers a synchronization request.
     */
    public static void sendLocationUpdate(Context context, LocationUpdateDto update){
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        ContentValues values = new ContentValues();
        values.put(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID, update.getGroup().getRoute().getId());

        values.put(DataContract.GroupLocations.COLUMN_NAME_STATION_ID, update.getStation().getId());
        values.put(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE, update.getType().toString());
        values.put(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP, DateTime.now().toInstant().toString());

        values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID, update.getGroup().getId());
        values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE, update.getGroup().getGuide().getUsername());
        values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_STATUS, true);
        values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION, update.getGroup().getStartingPosition());

        batch.add(ContentProviderOperation.newUpdate(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI))
                .withValues(values)
                .withSelection(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID + "=? AND ("
                        + DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + " IS NULL OR "
                        + DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + " <?)", new String[]{update.getGroup().getId().toString(), DateTime.now().toInstant().toString()})
                .build());

        values = new ContentValues();
        values.put(DataContract.LocationUpdates.COLUMN_NAME_ROUTE_ID, update.getGroup().getRoute().getId());
        values.put(DataContract.LocationUpdates.COLUMN_NAME_STATION_ID, update.getStation().getId());
        values.put(DataContract.LocationUpdates.COLUMN_NAME_GROUP_ID, update.getGroup().getId());
        values.put(DataContract.LocationUpdates.COLUMN_NAME_LOCATION_UPDATE_TYPE, update.getType().toString());
        values.put(DataContract.LocationUpdates.COLUMN_NAME_TIMESTAMP, DateTime.now().toInstant().toString());

        batch.add(ContentProviderOperation.newInsert(DataContract.LocationUpdates.CONTENT_URI)
                .withValues(values).build());

        try {
            context.getContentResolver().applyBatch(AppConstants.AUTHORITY, batch);
            SyncHelper.requestManualUploadSync(context, AccountUtil.getAccount(context));
        } catch (Exception e){
            Toast.makeText(context, "Couldn't add a location update.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Saves group size update to the database and triggers a synchronization request.
     */
    public static void addGroupSize(Context context, String groupId, int size){
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        ContentValues values = new ContentValues();
        values.put(DataContract.GroupSizes.COLUMN_NAME_GROUP_ID, groupId);
        values.put(DataContract.GroupSizes.COLUMN_NAME_GROUP_SIZE, size);
        values.put(DataContract.GroupSizes.COLUMN_NAME_TIMESTAMP, DateTime.now().toInstant().toString());

        batch.add(ContentProviderOperation.newInsert(DataContract.GroupSizes.CONTENT_URI)
                .withValues(values).build());

        try {
            context.getContentResolver().applyBatch(AppConstants.AUTHORITY, batch);
            SyncHelper.requestManualUploadSync(context, AccountUtil.getAccount(context));
        } catch (Exception e){
            Toast.makeText(context, "Couldn't add a group size.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Updates current user's starting position for given group.
     */
    public static void updateStartLocation(Context context, String groupId, int startingPosition){

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        ContentValues values = new ContentValues();
        values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION, startingPosition);

        batch.add(ContentProviderOperation.newUpdate(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.GuidedGroups.CONTENT_URI))
                .withValues(values)
                .withSelection(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID + "=?",
                        new String[]{groupId})
                .build());

        try {
            context.getContentResolver().applyBatch(AppConstants.AUTHORITY, batch);
        } catch (Exception e){
            Toast.makeText(context, "Couldn't change start location.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
