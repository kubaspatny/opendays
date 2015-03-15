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
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
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

        if(groups == null) return;

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

        for(GroupDto g : groups){
            loadRoute(g.getRoute().getId().toString());
        }

    }

    public void loadRoute(String routeId) throws Exception {
        Log.d(TAG, "loadRoute(" + routeId + ")");

        Account account = AccountUtil.getAccount(mContext);
        RouteDto route = SyncEndpoint.getRoute(account, AccountUtil.getAccessToken(mContext, account), routeId);
        List<GroupDto> groups = SyncEndpoint.getRouteGroups(account, AccountUtil.getAccessToken(mContext, account), routeId);

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        if(route == null) return;

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.Route.CONTENT_URI))
                .withSelection(DataContract.Route.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId}).build());

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.Station.CONTENT_URI))
                .withSelection(DataContract.Station.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId}).build());

        batch.add(ContentProviderOperation.newDelete(
                DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI))
                .withSelection(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID + "=?", new String[]{routeId}).build());

        // TODO if there's route 'Where ROUTE_ID = 123' -> update.. otherwise insert

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
        // TODO: get stations 'where routeId = 123' -> update if possible
        //                                          -> otherwise add or delete
        // TODO: add custom ordering parameter ((s.getPosition - user.startPos) % station.size) + 1
        for(StationDto s : route.getStations()){ // TODO: what if there are no stations?? NPE?
            values = new ContentValues();
            values.put(DataContract.Station.COLUMN_NAME_ROUTE_ID, route.getId());
            values.put(DataContract.Station.COLUMN_NAME_STATION_ID, s.getId());
            values.put(DataContract.Station.COLUMN_NAME_STATION_NAME, s.getName());
            values.put(DataContract.Station.COLUMN_NAME_STATION_LOCATION, s.getLocation());
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

            batch.add(ContentProviderOperation.newInsert(
                    DataContract.addCallerIsSyncAdapterParameter(DataContract.GroupLocations.CONTENT_URI)).withValues(values).build());

        }

        mContentResolver.applyBatch(AppConstants.AUTHORITY, batch);

    }


}
