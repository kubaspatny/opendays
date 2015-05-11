package cz.kubaspatny.opendays.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.database.DataContract;

/**
 * Cursor adapter for displaying stations of a route.
 */
public class RouteGuideAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;

    public RouteGuideAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.routeguide_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView stationName = (TextView) view.findViewById(R.id.stationName);
        TextView stationLocation = (TextView) view.findViewById(R.id.stationLocation);
        TextView stationTimeLimit = (TextView) view.findViewById(R.id.stationTimeLimit);
        TextView stationRelocationTime = (TextView) view.findViewById(R.id.relocationTime);

        String stationNameString = cursor.getString(cursor.getColumnIndex(DataContract.Station.COLUMN_NAME_STATION_NAME));
        String stationLocationString = cursor.getString(cursor.getColumnIndex(DataContract.Station.COLUMN_NAME_STATION_LOCATION));
        int stationTimeLimitValue = cursor.getInt(cursor.getColumnIndex(DataContract.Station.COLUMN_NAME_STATION_TIME_LIMIT));
        int stationRelocationTimeValue = cursor.getInt(cursor.getColumnIndex(DataContract.Station.COLUMN_NAME_STATION_TIME_RELOCATION));

        stationName.setText(stationNameString);
        stationLocation.setText(stationLocationString);
        stationTimeLimit.setText("Time limit: " + stationTimeLimitValue);
        stationRelocationTime.setText(stationRelocationTimeValue + "-minute walk");

        if(cursor.isLast()){
            View relocationContainer = view.findViewById(R.id.relocation_container);
            relocationContainer.setVisibility(View.GONE);
        }

    }
}
