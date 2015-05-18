package cz.kubaspatny.opendays.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.TimeZone;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link RouteInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final static String ARG_ROUTE_ID = "RouteInfoFragment.routeId";
    private final static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    private String mRouteId;

    private View mInformationError;
    private View mInformationLoading;
    private View mInformationContainer;

    private TextView mEventInfo;
    private TextView mRouteInfo;
    private TextView mRouteTime;

    public static RouteInfoFragment newInstance(String routeId) {
        RouteInfoFragment fragment = new RouteInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_ID, routeId);
        fragment.setArguments(args);
        return fragment;
    }

    public RouteInfoFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_route_info, container, false);
        mInformationError = fragmentView.findViewById(R.id.information_error);
        mInformationLoading = fragmentView.findViewById(R.id.information_loading);
        mInformationContainer = fragmentView.findViewById(R.id.information_container);

        mEventInfo = (TextView) fragmentView.findViewById(R.id.eventInformation);
        mRouteInfo = (TextView) fragmentView.findViewById(R.id.routeInformation);
        mRouteTime = (TextView) fragmentView.findViewById(R.id.routeTime);

        mInformationLoading.setVisibility(View.VISIBLE);

        getActivity().getSupportLoaderManager().initLoader(1, null, this);

        return fragmentView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {DataContract.Route._ID,
                DataContract.Route.COLUMN_NAME_ROUTE_COLOR,
                DataContract.Route.COLUMN_NAME_ROUTE_NAME,
                DataContract.Route.COLUMN_NAME_ROUTE_TIMESTAMP,
                DataContract.Route.COLUMN_NAME_ROUTE_INFORMATION,
                DataContract.Route.COLUMN_NAME_EVENT_NAME,
                DataContract.Route.COLUMN_NAME_EVENT_INFORMATION
        };

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.Route.TABLE_NAME).build(),
                projection,
                DataContract.Route.COLUMN_NAME_ROUTE_ID + "=?",
                new String[]{mRouteId},
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateUI(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        updateUI(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportLoaderManager().destroyLoader(1);
    }

    private void updateUI(Cursor cursor){

        if(cursor == null) return;
        if(cursor.getCount() == 0){
            mInformationLoading.setVisibility(View.GONE);
            mInformationContainer.setVisibility(View.GONE);
            mInformationError.setVisibility(View.VISIBLE);
        } else {
            cursor.moveToFirst();
            mEventInfo.setText(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Route.COLUMN_NAME_EVENT_INFORMATION)));
            mRouteInfo.setText(cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Route.COLUMN_NAME_ROUTE_INFORMATION)));

            String time = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Route.COLUMN_NAME_ROUTE_TIMESTAMP));
            DateTime dateTime = dateTimeFormatter.parseDateTime(time);
            dateTime = dateTime.toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));
            mRouteTime.setText(dateTime.toString("dd.MM.yyyy HH:mm"));

            mInformationLoading.setVisibility(View.GONE);
            mInformationError.setVisibility(View.GONE);
            mInformationContainer.setVisibility(View.VISIBLE);
        }

    }

}
