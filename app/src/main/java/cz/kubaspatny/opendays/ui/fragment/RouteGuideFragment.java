package cz.kubaspatny.opendays.ui.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.adapter.RouteGuideArrayAdapter;
import cz.kubaspatny.opendays.alarm.AlarmBroadcastReceiver;
import cz.kubaspatny.opendays.alarm.AlarmUtil;
import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.domainobject.StationWrapper;
import cz.kubaspatny.opendays.domainobject.UserDto;
import cz.kubaspatny.opendays.ui.widget.fab.FloatingActionButton;
import cz.kubaspatny.opendays.ui.widget.fab.FloatingActionsMenu;
import cz.kubaspatny.opendays.util.TimeUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link RouteGuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteGuideFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = RouteGuideFragment.class.getSimpleName();
    private final static String ARG_ROUTE_ID = "RouteInfoFragment.routeId";
    private final static String ARG_GROUP_ID = "RouteInfoFragment.groupId";
    private final static int STATION_LOADER = 10;
    private final static int GROUPS_LOADER = 11;
    private final static int LATEST_LOCATION_LOADER = 12;

    private final static int INTERVAL_TO_REDRAW_UI = 1000;

    private String mRouteId;
    private String mGroupId;
    private ListView mListView;
    private View mEmptyView;
    private View mLoadingView;
    private RouteGuideArrayAdapter adapter;
    boolean mDestroyed = false;

    public static RouteGuideFragment newInstance(String routeId, String groupId) {
        RouteGuideFragment fragment = new RouteGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeId);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    public RouteGuideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRouteId = getArguments().getString(ARG_ROUTE_ID);
            mGroupId = getArguments().getString(ARG_GROUP_ID);
            Log.d(TAG, "Guiding group: " + mGroupId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_route_guide, container, false);
        mLoadingView = fragmentView.findViewById(R.id.route_guide_stations_loading);
        mEmptyView = fragmentView.findViewById(R.id.empty_state);
        mListView = (ListView) fragmentView.findViewById(R.id.route_guide_stations);
        mListView.setEmptyView(mEmptyView);

        final FloatingActionsMenu fam = (FloatingActionsMenu) fragmentView.findViewById(R.id.FAM);
        FloatingActionButton addGroupSizeButton = (FloatingActionButton) fragmentView.findViewById(R.id.fab_groupsize);
        addGroupSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

                builder.setTitle("Set group size")
                        .setMessage("[number picker here]")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("CANCEL", null)
                        .show();

                fam.collapse();

            }
        });

        FloatingActionButton addLocationUpdateButton = (FloatingActionButton) fragmentView.findViewById(R.id.fab_locationupdate);
        addLocationUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationUpdateDialog();
                fam.collapse();
            }
        });


        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        getActivity().getSupportLoaderManager().initLoader(STATION_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(GROUPS_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(LATEST_LOCATION_LOADER, null, this);
        adapter = new RouteGuideArrayAdapter(getActivity(), new ArrayList<StationWrapper>());
        mListView.setAdapter(adapter);
        setTimerToUpdateUI();

        return fragmentView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(id == STATION_LOADER){
            String[] projection = {DataContract.Station._ID,
                    DataContract.Station.COLUMN_NAME_STATION_ID,
                    DataContract.Station.COLUMN_NAME_ROUTE_ID,
                    DataContract.Station.COLUMN_NAME_STATION_NAME,
                    DataContract.Station.COLUMN_NAME_STATION_LOCATION,
                    DataContract.Station.COLUMN_NAME_STATION_STATUS,
                    DataContract.Station.COLUMN_NAME_STATION_TIME_LIMIT,
                    DataContract.Station.COLUMN_NAME_STATION_TIME_RELOCATION,
                    DataContract.Station.COLUMN_NAME_STATION_SEQ_POSITION
            };

            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.Station.TABLE_NAME).build(),
                    projection,
                    DataContract.Station.COLUMN_NAME_ROUTE_ID + "=?",
                    new String[]{mRouteId},
                    DataContract.Station.COLUMN_NAME_STATION_SEQ_POSITION + " asc");

            return cursorLoader;
        } else if(id == GROUPS_LOADER){

            String[] projection = {DataContract.GroupLocations._ID,
                    DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID,
                    DataContract.GroupLocations.COLUMN_NAME_STATION_ID,
                    DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE,
                    DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_ID,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_STATUS,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION
            };

            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.GroupLocations.TABLE_NAME).build(),
                    projection,
                    DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID + "=?",
                    new String[]{mRouteId},
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION + " asc");

            return cursorLoader;

        } else if(id == LATEST_LOCATION_LOADER){

            String[] projection = {DataContract.GroupLocations._ID,
                    DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID,
                    DataContract.GroupLocations.COLUMN_NAME_STATION_ID,
                    DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE,
                    DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_ID,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION

                    // TODO: when there's no update -> local, nor remote -> where to get starting position
                    // TODO: set to only return the latest (desc by timestamp)
                    // TODO: where group id == my id (where to get that)?? -> from intent?

            };

            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.GroupLocations.TABLE_NAME).build(),
                    projection,
                    DataContract.GroupLocations.COLUMN_NAME_GROUP_ID + "=?",
                    new String[]{mGroupId},
                    DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP + " desc LIMIT 1"); // " desc LIMIT 1"

            return cursorLoader;



        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch(loader.getId()){
            case STATION_LOADER:
                processStations(data);
                Log.d(TAG, "Finished load: STATION_LOADER");
                break;
            case GROUPS_LOADER:
                Log.d(TAG, "Finished load: GROUPS_LOADER");
                processGroups(data);
                break;
            case LATEST_LOCATION_LOADER:
                Log.d(TAG, "Finished load: LATEST_LOCATION_LOADER");
                processLatestLocation(data);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, but we don't
        // have any reference to it anyways
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportLoaderManager().destroyLoader(STATION_LOADER);
        getActivity().getSupportLoaderManager().destroyLoader(GROUPS_LOADER);
        getActivity().getSupportLoaderManager().destroyLoader(LATEST_LOCATION_LOADER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
    }

    private List<StationDto> stations = null;
    private HashMap<Long, List<GroupDto>> groups = null;
    private LocationUpdateDto latestLocation = null;

    private void processStations(final Cursor cursor){

        //TODO: add expection handling

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<StationDto> stationsList = new ArrayList<StationDto>();

                cursor.moveToFirst();
                while(!cursor.isAfterLast() && !cursor.isBeforeFirst() && cursor.getCount() != 0){

                    StationDto s = new StationDto();
                    s.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_ID)));
                    s.setName(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_NAME)));
                    s.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_LOCATION)));
                    s.setClosed(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_STATUS)));
                    s.setTimeLimit(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_TIME_LIMIT)));
                    s.setRelocationTime(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_TIME_RELOCATION)));
                    s.setSequencePosition(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_SEQ_POSITION)));

                    stationsList.add(s);
                    cursor.moveToNext();

                }

                stations = stationsList;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                loadData();
            }

        }.execute();
    }

    private void processGroups(final Cursor cursor){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<Long, List<GroupDto>> groupMap = new HashMap<Long, List<GroupDto>>();

                cursor.moveToFirst();
                while(!cursor.isAfterLast() && !cursor.isBeforeFirst() && cursor.getCount() != 0){

                    GroupDto g = new GroupDto();
                    g.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID)));

                    Long stationId = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_STATION_ID));

                    if(stationId == null || stationId == 0){ // Group hasn't sent any location updates yet..
                        cursor.moveToNext();
                        continue;
                    }

                    LocationUpdateDto update = new LocationUpdateDto();
                    update.setStation(new StationDto(stationId));
                    update.setType(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE)));
                    update.setTimestamp(TimeUtil.parseTimestamp(
                            cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP))));
                    g.setLatestLocationUpdate(update);

                    g.setGuide(new UserDto(
                            cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE))));
                    g.setActive(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_STATUS)));
                    g.setStartingPosition(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION)));

                    if(groupMap.containsKey(stationId)){
                        groupMap.get(stationId).add(g);
                    } else {
                        List<GroupDto> groupsAtStation = new ArrayList<GroupDto>();
                        groupsAtStation.add(g);
                        groupMap.put(stationId, groupsAtStation);
                    }

                    cursor.moveToNext();

                }

                groups = groupMap;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                loadData();
            }

        }.execute();
    }

    private void processLatestLocation(Cursor cursor){

        cursor.moveToFirst();

        if(cursor.isBeforeFirst() || cursor.isAfterLast() || cursor.getCount() == 0) return;

        Long group = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID));
        String guide = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_GUIDE));
        int seq_position = cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_SEQ_POSITION));
        Long route = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_ROUTE_ID));
        Long station = cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_STATION_ID));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_LOCATION_UPDATE_TYPE));

        LocationUpdateDto locationUpdateDto = new LocationUpdateDto();
        GroupDto groupDto = new GroupDto(group);
        groupDto.setRoute(new RouteDto(route));
        groupDto.setGuide(new UserDto(guide));
        groupDto.setStartingPosition(seq_position);
        locationUpdateDto.setGroup(groupDto);

        if(time == null || type == null){
            locationUpdateDto.setType(LocationUpdateDto.LocationUpdateType.EMPTY);
        } else {
            locationUpdateDto.setType(LocationUpdateDto.LocationUpdateType.parseLocationUpdateType(type));
            locationUpdateDto.setStation(new StationDto(station));
            locationUpdateDto.setTimestamp(TimeUtil.parseTimestamp(time));
        }

        latestLocation = locationUpdateDto;
        loadData();

    }

    private void loadData(){
        if(stations == null || groups == null || latestLocation == null) return;

        // TODO: do in async task!!!
        List<StationWrapper> stationWrappers = new ArrayList<>();

        StationWrapper stationWrapper;
        for(StationDto s : stations){

            stationWrapper = new StationWrapper();
            stationWrapper.station = s;

            if(groups.containsKey(s.getId())){
                stationWrapper.groups = groups.get(s.getId());
            } else {
                stationWrapper.groups = Collections.emptyList();
            }

            stationWrappers.add(stationWrapper);

        }

        adapter.clear();
        adapter.addAll(stationWrappers);
        adapter.notifyDataSetChanged();

        mLoadingView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);

        // TODO: enable FAM if disabled

    }

    private void showLocationUpdateDialog(){

        Long routeId = Long.parseLong(mRouteId);
        final int groupId = (int) Long.parseLong(mGroupId);
        int stationLimit = -1;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LocationUpdateDto updateDto = new LocationUpdateDto();
        updateDto.setGroup(latestLocation.getGroup());

        if(!latestLocation.isEmpty()){
            final int index = adapter.getPosition(new StationWrapper(latestLocation.getStation(), null));
            LocationUpdateDto.LocationUpdateType type = latestLocation.getType();
            Log.d(TAG, "Currently at: " + adapter.getItem(index).station.getName());

            if(type == LocationUpdateDto.LocationUpdateType.CHECKIN){
                updateDto.setStation(new StationDto(adapter.getItem(index).station.getId()));
                updateDto.setType(LocationUpdateDto.LocationUpdateType.CHECKOUT);

                builder.setTitle(adapter.getItem(index).station.getName());
                builder.setPositiveButton("CHECK OUT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendLocationUpdate(updateDto);
                        AlarmUtil.cancelAlarm(getActivity(), groupId);
                    }
                });

            } else if(type == LocationUpdateDto.LocationUpdateType.CHECKOUT || type == LocationUpdateDto.LocationUpdateType.SKIP){
                if(index + 1 >= adapter.getCount()){
                    Log.d(TAG, "No stations left!");
                    builder.setTitle("No stations left");
                    builder.setMessage("This dialog shouldn't have been shown!");
                } else {
                    Log.d(TAG, "Next station: " + adapter.getItem(index + 1).station.getName());

                    updateDto.setStation(new StationDto(adapter.getItem(index + 1).station.getId()));

                    builder.setTitle(adapter.getItem(index + 1).station.getName());
                    builder.setPositiveButton("CHECK IN", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateDto.setType(LocationUpdateDto.LocationUpdateType.CHECKIN);
                            sendLocationUpdate(updateDto);
                            AlarmUtil.setAlarm(getActivity(),
                                    groupId,
                                    adapter.getItem(index + 1).station.getTimeLimit() * 60 / 2,
                                    adapter.getItem(index + 1).station.getName(),
                                    "Your time is 1/2 gone!",
                                    true);
                        }
                    });

                    builder.setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateDto.setType(LocationUpdateDto.LocationUpdateType.SKIP);
                            sendLocationUpdate(updateDto);
                        }
                    });
                }
            }
        } else {
            Log.d(TAG, "No location updates yet. The first location: " + adapter.getItem(0).station.getName());
            updateDto.setStation(new StationDto(adapter.getItem(0).station.getId()));

            builder.setTitle(adapter.getItem(0).station.getName());
            builder.setPositiveButton("CHECK IN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Clicked CHECK IN");
                    updateDto.setType(LocationUpdateDto.LocationUpdateType.CHECKIN);
                    sendLocationUpdate(updateDto);

                    AlarmUtil.setAlarm(getActivity(),
                            groupId,
                            adapter.getItem(0).station.getTimeLimit() * 60 / 2,
                            adapter.getItem(0).station.getName(),
                            "Your time is 1/2 gone!",
                            true);
                }
            });

            builder.setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Clicked SKIP");
                    updateDto.setType(LocationUpdateDto.LocationUpdateType.SKIP);
                    sendLocationUpdate(updateDto);
                }
            });
        }

        builder.show();

    }

    private void sendLocationUpdate(LocationUpdateDto update){

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
            getActivity().getContentResolver().applyBatch(AppConstants.AUTHORITY, batch);
        } catch (Exception e){
            Toast.makeText(getActivity(), "Couldn't add a location update.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void setTimerToUpdateUI() {
        new UpdateUIRunnable(this, new Handler()).scheduleNextRun();
    }

    boolean hasBeenDestroyed() {
        return mDestroyed;
    }

    static final class UpdateUIRunnable implements Runnable {

        final WeakReference<RouteGuideFragment> weakRefToParent;
        final Handler handler;

        public UpdateUIRunnable(RouteGuideFragment fragment, Handler handler) {
            weakRefToParent = new WeakReference<RouteGuideFragment>(fragment);
            this.handler = handler;
        }

        public void scheduleNextRun() {
            handler.postDelayed(this, INTERVAL_TO_REDRAW_UI);
        }

        @Override
        public void run() {
            RouteGuideFragment fragment = weakRefToParent.get();

            if (fragment == null || fragment.hasBeenDestroyed()) {
                Log.d("UIUpdateRunnable", "Killing updater -> fragment has been destroyed.");
                return;
            }

            if (fragment.adapter != null) {
                try {
                    fragment.adapter.forceUpdate();
                } finally {
                    // schedule again
                    this.scheduleNextRun();
                }
            }
        }
    }





}
