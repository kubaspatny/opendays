package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.GroupSizeDto;
import cz.kubaspatny.opendays.domainobject.GroupStartingPosition;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.util.AccountUtil;
import cz.kubaspatny.opendays.util.TimeUtil;

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

    public Map<Long, Long> loadGuidedGroups(Account account) throws Exception{
        Log.d(TAG, "loadGuidedGroups");

        List<GroupDto> groups = SyncEndpoint.getGroups(account, AccountUtil.getAccessToken(mContext, account), 0, 100); // TODO: Add parameters from bundle
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        if(groups == null) return new HashMap<>();

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.GuidedGroups.CONTENT_URI)).build());

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
            batch.add(ContentProviderOperation.newInsert(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.GuidedGroups.CONTENT_URI)).withValues(values).build());
        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

        HashMap<Long, Long> result = new HashMap<>();

        for(GroupDto g : groups){ // TODO: loadRoute only if g.getDate <= 1 day || lastBigSync >= 4 hours
            result.put(g.getRoute().getId(), g.getId());
        }

        return result;

    }

    public Map<Long, Long> loadManagedRoutes(Account account) throws Exception{
        Log.d(TAG, "loadGuidedGroups");

        List<RouteDto> routes = SyncEndpoint.getManagedRoutes(account, AccountUtil.getAccessToken(mContext, account), 0, 100); // TODO: Add parameters from bundle
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        if(routes == null) return new HashMap<>();

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.ManagedRoutes.CONTENT_URI)).build());

        for(RouteDto r : routes){
            ContentValues values = new ContentValues();
            values.put(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_ID, r.getId());
            values.put(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_NAME, r.getName());
            values.put(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_COLOR, r.getHexColor());
            values.put(DataContract.ManagedRoutes.COLUMN_NAME_ROUTE_TIMESTAMP, r.getDate().toInstant().toString());
            batch.add(ContentProviderOperation.newInsert(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.ManagedRoutes.CONTENT_URI)).withValues(values).build());
        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

        Map<Long, Long> result = new HashMap<>();
        for(RouteDto r : routes){
            result.put(r.getId(), null);
        }
        return result;
    }

    public void loadRoute(Long routeId, Long groupId) throws Exception {
        Log.d(TAG, "loadRoute(" + routeId + ")");

        Account account = AccountUtil.getAccount(mContext);
        RouteDto route = SyncEndpoint.getRoute(account, AccountUtil.getAccessToken(mContext, account), routeId.toString());
        List<GroupDto> groups = SyncEndpoint.getRouteGroups(account, AccountUtil.getAccessToken(mContext, account), routeId.toString());

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        if(route == null) return;

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.Route.CONTENT_URI))
                .withSelection(DataContract.Route.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId.toString()}).build());

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.Station.CONTENT_URI))
                .withSelection(DataContract.Station.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId.toString()}).build());

        if(groupId != null){
            batch.add(ContentProviderOperation.newDelete(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI))
                    .withSelection(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID + "=? AND " +
                            DataContract.GroupLocations.COLUMN_NAME_GROUP_ID + " !=?", new String[]{routeId.toString(), groupId.toString()}).build());
        } else {
            batch.add(ContentProviderOperation.newDelete(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI))
                    .withSelection(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId.toString()}).build());
        }

        // ROUTE TABLE
        ContentValues values = new ContentValues();
        values.put(DataContract.Route.COLUMN_NAME_ROUTE_ID, route.getId());
        values.put(DataContract.Route.COLUMN_NAME_ROUTE_NAME, route.getName());
        values.put(DataContract.Route.COLUMN_NAME_ROUTE_COLOR, route.getHexColor());
        values.put(DataContract.Route.COLUMN_NAME_ROUTE_INFORMATION, route.getInformation());
        values.put(DataContract.Route.COLUMN_NAME_ROUTE_TIMESTAMP, route.getDate().toInstant().toString());

        values.put(DataContract.Route.COLUMN_NAME_EVENT_ID, route.getEvent().getId());
        values.put(DataContract.Route.COLUMN_NAME_EVENT_NAME, route.getEvent().getName());
        values.put(DataContract.Route.COLUMN_NAME_EVENT_INFORMATION, route.getEvent().getInformation());
        batch.add(ContentProviderOperation.newInsert(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.Route.CONTENT_URI)).withValues(values).build());

        // STATION TABLE
        for(StationDto s : route.getStations()){
            values = new ContentValues();
            values.put(DataContract.Station.COLUMN_NAME_ROUTE_ID, route.getId());
            values.put(DataContract.Station.COLUMN_NAME_STATION_ID, s.getId());
            values.put(DataContract.Station.COLUMN_NAME_STATION_NAME, s.getName());
            values.put(DataContract.Station.COLUMN_NAME_STATION_LOCATION, s.getLocation());
            values.put(DataContract.Station.COLUMN_NAME_STATION_INFORMATION, s.getInformation());
            values.put(DataContract.Station.COLUMN_NAME_STATION_SEQ_POSITION, s.getSequencePosition());
            values.put(DataContract.Station.COLUMN_NAME_STATION_TIME_LIMIT, s.getTimeLimit());
            values.put(DataContract.Station.COLUMN_NAME_STATION_TIME_RELOCATION, s.getRelocationTime());
            values.put(DataContract.Station.COLUMN_NAME_STATION_STATUS, s.isClosed());
            batch.add(ContentProviderOperation.newInsert(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.Station.CONTENT_URI)).withValues(values).build());
        }

        // GROUP LOCATIONS TABLE
        for(GroupDto g : groups){
            values = new ContentValues();
            values.put(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID, route.getId());

            if(g.getLatestLocationUpdate() != null){
                values.put(DataContract.GroupLocations.COLUMN_NAME_STATION_ID, g.getLatestLocationUpdate().getStation().getId());
                values.put(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE, g.getLatestLocationUpdate().getType().toString());
                values.put(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP, g.getLatestLocationUpdate().getTimestamp().toInstant().toString());
            }

            values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID, g.getId());
            values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE, g.getGuide().getUsername());
            values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_STATUS, g.isActive());
            values.put(DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION, g.getStartingPosition());

            if(groupId != null && g.getId().equals(groupId)){

                Cursor countCursor = mContentResolver.query(DataContract.GroupLocations.CONTENT_URI,
                        new String[]{DataContract.GroupLocations.COLUMN_NAME_GROUP_ID},
                        DataContract.GroupLocations.COLUMN_NAME_GROUP_ID + " =?",
                        new String[]{groupId.toString()},
                        null);

                int count = countCursor.getCount();
                countCursor.close();

                if(count > 0){ // update
                    String timestamp = g.getLatestLocationUpdate()==null ? "0" : g.getLatestLocationUpdate().getTimestamp().toInstant().toString();

                    batch.add(ContentProviderOperation.newUpdate(
                            DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI))
                            .withValues(values)
                            .withSelection(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID + "=? AND ("
                                    + DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + " IS NULL OR "
                                    + DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + " <?)", new String[]{groupId.toString(), timestamp})
                            .build());
                } else { // insert
                    batch.add(ContentProviderOperation.newInsert(
                            DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI)).withValues(values).build());
                }

            } else {
                batch.add(ContentProviderOperation.newInsert(
                        DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI)).withValues(values).build());
            }

        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

    }

    public void uploadLocationUpdates() throws Exception {

        String[] projection = {DataContract.LocationUpdates._ID,
                DataContract.LocationUpdates.COLUMN_NAME_GROUP_ID,
                DataContract.LocationUpdates.COLUMN_NAME_TIMESTAMP,
                DataContract.LocationUpdates.COLUMN_NAME_LOCATION_UPDATE_TYPE,
                DataContract.LocationUpdates.COLUMN_NAME_STATION_ID};

        Cursor cursor = mContentResolver.query(DataContract.LocationUpdates.CONTENT_URI, projection, null, null, null);
        cursor.moveToFirst();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        Account account = AccountUtil.getAccount(mContext);

        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){
            // upload

            LocationUpdateDto update = new LocationUpdateDto();
            update.setTimestamp(TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.LocationUpdates.COLUMN_NAME_TIMESTAMP))));
            update.setType(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.LocationUpdates.COLUMN_NAME_LOCATION_UPDATE_TYPE)));
            update.setGroup(new GroupDto(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.LocationUpdates.COLUMN_NAME_GROUP_ID))
                    ));
            update.setStation(new StationDto(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.LocationUpdates.COLUMN_NAME_STATION_ID))
            ));

            try {
                SyncEndpoint.uploadLocationUpdates(account, AccountUtil.getAccessToken(mContext, account), update);
                batch.add(ContentProviderOperation.newDelete(DataContract.addCallerIsSyncAdapterParameter(DataContract.LocationUpdates.CONTENT_URI))
                        .withSelection(DataContract.LocationUpdates._ID + "=?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DataContract.LocationUpdates._ID))})
                        .build());
            } catch (Exception e){
                // do nothing
                // Location update will be send with another sync
            } finally {
                cursor.moveToNext();
            }

        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

    }

    public void uploadGroupSizes() throws Exception {

        String[] projection = {DataContract.GroupSizes._ID,
                DataContract.GroupSizes.COLUMN_NAME_GROUP_ID,
                DataContract.GroupSizes.COLUMN_NAME_TIMESTAMP,
                DataContract.GroupSizes.COLUMN_NAME_GROUP_SIZE};

        Cursor cursor = mContentResolver.query(DataContract.GroupSizes.CONTENT_URI, projection, null, null, null);
        cursor.moveToFirst();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        Account account = AccountUtil.getAccount(mContext);

        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){

            GroupSizeDto size = new GroupSizeDto();
            size.setTimestamp(TimeUtil.parseTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupSizes.COLUMN_NAME_TIMESTAMP))));
            size.setGroupId(cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupSizes.COLUMN_NAME_GROUP_ID)));
            size.setSize(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.GroupSizes.COLUMN_NAME_GROUP_SIZE)));

            try {
                SyncEndpoint.uploadGroupSize(account, AccountUtil.getAccessToken(mContext, account), size);
                batch.add(ContentProviderOperation.newDelete(DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupSizes.CONTENT_URI))
                        .withSelection(DataContract.LocationUpdates._ID + "=?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupSizes._ID))})
                        .build());
            } catch (Exception e){
                // do nothing
                // Size will be send with another sync
            } finally {
                cursor.moveToNext();
            }

        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

    }

    public void updateStartingPosition(String groupId, int startingPosition) throws Exception {

        GroupStartingPosition g = new GroupStartingPosition();
        g.setGroupId(Long.parseLong(groupId));
        g.setStartingPosition(startingPosition);

        Account account = AccountUtil.getAccount(mContext);
        SyncEndpoint.updateStartingPosition(account,
                AccountUtil.getAccessToken(mContext, account),
                g);

    }

}
