package cz.kubaspatny.opendays.ui.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.adapter.RouteGuideArrayAdapter;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.domainobject.StationWrapper;
import cz.kubaspatny.opendays.domainobject.UserDto;
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
    private final static int STATION_LOADER = 10;
    private final static int GROUPS_LOADER = 11;

    private String mRouteId;
    private ListView mListView;
    private View mEmptyView;
    private View mLoadingView;
    private RouteGuideArrayAdapter adapter;


    public static RouteGuideFragment newInstance(String routeId) {
        RouteGuideFragment fragment = new RouteGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeId);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_route_guide, container, false);
        mLoadingView = fragmentView.findViewById(R.id.route_guide_stations_loading);
        mEmptyView = fragmentView.findViewById(R.id.empty_state);
        mListView = (ListView) fragmentView.findViewById(R.id.route_guide_stations);

        mListView.setEmptyView(mEmptyView);

        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        getActivity().getSupportLoaderManager().initLoader(STATION_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(GROUPS_LOADER, null, this);
        adapter = new RouteGuideArrayAdapter(getActivity(), new ArrayList<StationWrapper>());
        mListView.setAdapter(adapter);

        return fragmentView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader:" + id);

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

        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d(TAG, "onLoadFinished:" + loader.getId());

        if(loader.getId() == STATION_LOADER){
            processStations(data);
        } else if (loader.getId() == GROUPS_LOADER){
            processGroups(data);
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
    }

    private List<StationDto> stations = null;
    private HashMap<Long, List<GroupDto>> groups = null;

    private void processStations(final Cursor cursor){

        //TODO: add expection handling

        Log.d(TAG, "processStations");

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

        Log.d(TAG, "processStations");

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

    private void loadData(){
        if(stations == null || groups == null) return;

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

        Log.d(TAG, "loadData: set visibility");

        stations = null;
        groups = null;

    }



}
