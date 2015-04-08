package cz.kubaspatny.opendays.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.adapter.RouteGuideArrayAdapter;
import cz.kubaspatny.opendays.alarm.AlarmUtil;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.domainobject.StationWrapper;
import cz.kubaspatny.opendays.domainobject.UserDto;
import cz.kubaspatny.opendays.sync.DataFetcher;
import cz.kubaspatny.opendays.ui.widget.fab.FloatingActionButton;
import cz.kubaspatny.opendays.ui.widget.fab.FloatingActionsMenu;
import cz.kubaspatny.opendays.util.ConnectionUtils;
import cz.kubaspatny.opendays.util.DbUtil;
import cz.kubaspatny.opendays.util.StationComparator;
import cz.kubaspatny.opendays.util.TimeUtil;

import static cz.kubaspatny.opendays.util.ToastUtil.*;

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
    private final static String ARG_GROUP_START_POS = "RouteInfoFragment.groupStartingPosition";
    public final static String ARG_VIEW_ONLY = "RouteInfoFragment.viewOnly";
    private final static int STATION_LOADER = 10;
    private final static int GROUPS_LOADER = 11;
    private final static int LATEST_LOCATION_LOADER = 12;

    private final static int INTERVAL_TO_REDRAW_UI = 1000;

    private String mRouteId;
    private String mGroupId;
    private int mGroupStartingPosition;
    private boolean mViewOnly;
    private UpdateUIRunnable mUpdateUIRunnable;

    private ListView mListView;
    private View mEmptyView;
    private View mLoadingView;
    private FloatingActionsMenu mFam;
    private FloatingActionButton mChangeStartPosButton;
    private RouteGuideArrayAdapter adapter;
    boolean mDestroyed = false;

    public static RouteGuideFragment newInstance(String routeId, String groupId, int groupStartingPosition) {
        RouteGuideFragment fragment = new RouteGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeId);
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_GROUP_START_POS, groupStartingPosition);
        args.putBoolean(ARG_VIEW_ONLY, false);
        fragment.setArguments(args);
        return fragment;
    }

    public static RouteGuideFragment newInstance(String routeId){
        RouteGuideFragment fragment = new RouteGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeId);
        args.putString(ARG_GROUP_ID, 0 + "");
        args.putInt(ARG_GROUP_START_POS, 1);
        args.putBoolean(ARG_VIEW_ONLY, true);
        fragment.setArguments(args);
        return fragment;
    }

    public RouteGuideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mRouteId = getArguments().getString(ARG_ROUTE_ID);
            mGroupId = getArguments().getString(ARG_GROUP_ID);
            mGroupStartingPosition = getArguments().getInt(ARG_GROUP_START_POS);
            mViewOnly = getArguments().getBoolean(ARG_VIEW_ONLY);
            Log.d(TAG, "Guiding group: " + mGroupId + ", starting at: " + mGroupStartingPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_route_guide, container, false);
        mLoadingView = fragmentView.findViewById(R.id.route_guide_stations_loading);
        mEmptyView = fragmentView.findViewById(R.id.empty_state);
        mListView = (ListView) fragmentView.findViewById(R.id.route_guide_stations);
        mListView.setEmptyView(mEmptyView);

        if(!mViewOnly){
            View footer = inflater.inflate(R.layout.list_empty_space_footer, mListView, false);
            mListView.addFooterView(footer);
            mFam = (FloatingActionsMenu) fragmentView.findViewById(R.id.FAM);
            FloatingActionButton addGroupSizeButton = (FloatingActionButton) fragmentView.findViewById(R.id.fab_groupsize);
            addGroupSizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddGroupSizeDialog();
                    mFam.collapse();
                }
            });

            FloatingActionButton addLocationUpdateButton = (FloatingActionButton) fragmentView.findViewById(R.id.fab_locationupdate);
            addLocationUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLocationUpdateDialog();
                    mFam.collapse();
                }
            });

            mChangeStartPosButton = (FloatingActionButton) fragmentView.findViewById(R.id.fab_starting_position);
            mChangeStartPosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showStartLocationDialog();
                    mFam.collapse();
                }
            });

            mFam.setVisibility(View.GONE);
        }

        getActivity().getSupportLoaderManager().initLoader(STATION_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(GROUPS_LOADER, null, this);
        if(!mViewOnly) getActivity().getSupportLoaderManager().initLoader(LATEST_LOCATION_LOADER, null, this);
        adapter = new RouteGuideArrayAdapter(getActivity(), new ArrayList<StationWrapper>());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StationWrapper stationWrapper = (StationWrapper) mListView.getItemAtPosition(position);
                showStationInfoDialog(stationWrapper.station);
            }
        });

//        setTimerToUpdateUI();

        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);

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
                    DataContract.Station.COLUMN_NAME_STATION_INFORMATION,
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
                Log.d(TAG, "Finished load: STATION_LOADER");
                processStations(data);
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
        if(!mViewOnly) getActivity().getSupportLoaderManager().destroyLoader(LATEST_LOCATION_LOADER);
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
        new AsyncTask<Void, Void, Void>(){

            Exception e = null;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ArrayList<StationDto> stationsList = new ArrayList<StationDto>();

                    cursor.moveToFirst();
                    while(!cursor.isAfterLast() && !cursor.isBeforeFirst() && cursor.getCount() != 0){

                        StationDto s = new StationDto();
                        s.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_ID)));
                        s.setName(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_NAME)));
                        s.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_LOCATION)));
                        s.setInformation(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_INFORMATION)));
                        s.setClosed(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_STATUS)));
                        s.setTimeLimit(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_TIME_LIMIT)));
                        s.setRelocationTime(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_TIME_RELOCATION)));
                        s.setSequencePosition(cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_SEQ_POSITION)));

                        stationsList.add(s);
                        cursor.moveToNext();

                    }

                    Collections.sort(stationsList, new StationComparator(mGroupStartingPosition, stationsList.size()));
                    stations = stationsList;
                } catch (Exception e){
                    Log.d(TAG, "process stations: " + e.getLocalizedMessage());
                    this.e = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(e == null) loadData();
            }

        }.execute();
    }

    private void processGroups(final Cursor cursor){
        new AsyncTask<Void, Void, Void>(){

            Exception e = null;

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    HashMap<Long, List<GroupDto>> groupMap = new HashMap<Long, List<GroupDto>>();

                    cursor.moveToFirst();
                    while(!cursor.isAfterLast() && !cursor.isBeforeFirst() && cursor.getCount() != 0){

                        GroupDto g = new GroupDto();
                        g.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DataContract.GroupLocations.COLUMN_NAME_GROUP_ID)));
                        g.setCurrentUser(g.getId().equals(Long.parseLong(mGroupId)));

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
                } catch (Exception e){
                    Log.d(TAG, "process groups: " + e.getLocalizedMessage());
                    this.e = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(e == null) loadData();
            }

        }.execute();
    }

    private void processLatestLocation(final Cursor cursor){
        new AsyncTask<Void, Void, Void>(){

            Exception e = null;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    cursor.moveToFirst();

                    if(cursor.isBeforeFirst() || cursor.isAfterLast() || cursor.getCount() == 0) return null;

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

                } catch (Exception e){
                    Log.d(TAG, "process latest location: " + e.getLocalizedMessage());
                    this.e = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(e == null) loadData();
            }
        }.execute();
    }

    private void loadData(){
        if(stations == null || groups == null || (!stations.isEmpty() && !mViewOnly && latestLocation == null)) return;
        if(stations.isEmpty()){
            mLoadingView.setVisibility(View.GONE);
            mFam.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }

        List<StationWrapper> stationWrappers = new ArrayList<>();

        StationWrapper stationWrapper;
        for(StationDto s : stations){

            stationWrapper = new StationWrapper();
            stationWrapper.station = s;

            if(groups.containsKey(s.getId())){
                List<GroupDto> groupList = groups.get(s.getId());
                for(GroupDto g : groupList){
                    g.computeLastStation(stations.size());
                }
                stationWrapper.groups = groupList;
            } else {
                stationWrapper.groups = Collections.emptyList();
            }

            stationWrappers.add(stationWrapper);

        }

        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(stationWrappers);
        adapter.notifyDataSetChanged();

        mLoadingView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);

        if(!mViewOnly){
            if(!isGuideStarted()){
                mChangeStartPosButton.setVisibility(View.VISIBLE);
            } else {
                mChangeStartPosButton.setVisibility(View.GONE);
            }

            if(isGuideDone()){
                mFam.setVisibility(View.GONE);
            } else {
                mFam.setVisibility(View.VISIBLE);
            }
        }

        setTimerToUpdateUI();

    }

    private boolean isGuideDone(){
        if(adapter.getCount() == 0) return true;

        if(!latestLocation.isEmpty()) {
            final int index = adapter.getPosition(new StationWrapper(latestLocation.getStation(), null));
            LocationUpdateDto.LocationUpdateType type = latestLocation.getType();

            if ((type == LocationUpdateDto.LocationUpdateType.CHECKOUT || type == LocationUpdateDto.LocationUpdateType.SKIP) && (index + 1 >= adapter.getCount())) {
                return true;
            }
        }

        return false;
    }

    private boolean isGuideStarted(){
        return !latestLocation.isEmpty();
    }

    private void showStartLocationDialog(){
        int current = mGroupStartingPosition;
        int count = adapter.getCount();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.change_starting_position));
        String[] types = new String[stations.size()];
        for(int i = 0; i < stations.size(); i++){
            types[i] = stations.get(i).getSequencePosition() + " - " + stations.get(i).getName();
        }

        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selected) {
                dialog.dismiss();

                if(ConnectionUtils.isConnected(getActivity())){
                    new UpdateStartPositionAsyncTask().execute(stations.get(selected).getSequencePosition());
                } else {
                    error(getActivity(), getString(R.string.no_internet));
                }

            }
        });

        builder.show();

    }

    private class UpdateStartPositionAsyncTask extends AsyncTask<Integer, Void, Void> {

        Exception e = null;
        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                new DataFetcher(getActivity()).updateStartingPosition(mGroupId, params[0]);

                mGroupStartingPosition = params[0];
                DbUtil.updateStartLocation(getActivity(), mGroupId, mGroupStartingPosition);
                Collections.sort(stations, new StationComparator(mGroupStartingPosition, stations.size()));

            } catch (Exception e){
                this.e = e;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(getActivity(), null, getString(R.string.updating_starting_position), true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.dismiss();

            if(e == null){
                loadData();
            } else {
                Log.e(TAG, "Error updating starting position!", e);

                if(e instanceof IOException){
                    error(getActivity(), getString(R.string.no_internet));
                } else {
                    error(getActivity(), getString(R.string.updating_starting_position_error));
                }

            }
        }
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
                builder.setPositiveButton(getString(R.string.location_update_CHECKOUT), new DialogInterface.OnClickListener() {
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
                    builder.setPositiveButton(getString(R.string.location_update_CHECKIN), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateDto.setType(LocationUpdateDto.LocationUpdateType.CHECKIN);
                            sendLocationUpdate(updateDto);
                            AlarmUtil.setAlarm(getActivity(),
                                    groupId,
                                    adapter.getItem(index + 1).station.getTimeLimit() * 60 / 2,
                                    adapter.getItem(index + 1).station.getName(),
                                    getString(R.string.half_time_notification),
                                    true);
                        }
                    });

                    builder.setNegativeButton(getString(R.string.location_update_SKIP), new DialogInterface.OnClickListener() {
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
            builder.setPositiveButton(getString(R.string.location_update_CHECKIN), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Clicked CHECK IN");
                    updateDto.setType(LocationUpdateDto.LocationUpdateType.CHECKIN);
                    sendLocationUpdate(updateDto);

                    AlarmUtil.setAlarm(getActivity(),
                            groupId,
                            adapter.getItem(0).station.getTimeLimit() * 60 / 2,
                            adapter.getItem(0).station.getName(),
                            getString(R.string.half_time_notification),
                            true);
                }
            });

            builder.setNegativeButton(getString(R.string.location_update_SKIP), new DialogInterface.OnClickListener() {
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

    private void showAddGroupSizeDialog(){

        RelativeLayout linearLayout = new RelativeLayout(getActivity());
        final NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numberPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numberPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(numberPicker, numberPickerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.group_size));
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder.setPositiveButton(getString(R.string.set),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DbUtil.addGroupSize(getActivity(), mGroupId, numberPicker.getValue());
                            }
                        });

        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showStationInfoDialog(StationDto station){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(station.getName());
        builder.setMessage(station.getInformation());
        builder.show();
    }

    private void sendLocationUpdate(LocationUpdateDto update){
        DbUtil.sendLocationUpdate(getActivity(), update);
    }

    private void setTimerToUpdateUI() {
        if(mUpdateUIRunnable == null){
            Log.d(TAG, "started UpdateUIRunnable!");
            mUpdateUIRunnable = new UpdateUIRunnable(this, new Handler());
            mUpdateUIRunnable.scheduleNextRun();
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(getArguments() != null){
            getArguments().putString(ARG_ROUTE_ID, mRouteId);
            getArguments().putString(ARG_GROUP_ID, mGroupId);
            getArguments().putInt(ARG_GROUP_START_POS, mGroupStartingPosition);
            getArguments().putBoolean(ARG_VIEW_ONLY, mViewOnly);
        }
    }
}