package cz.kubaspatny.opendays.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.domainobject.StationWrapper;

/**
 * Created by Kuba on 15/3/2015.
 */
public class RouteGuideArrayAdapter extends ArrayAdapter<StationWrapper> {

    private static class ViewHolder {
        TextView stationName;
        TextView stationLocation;
        TextView stationTimeLimit;
        TextView stationRelocationTime;
        View relocationView;
    }

    public RouteGuideArrayAdapter(Context context, List<StationWrapper> objects) {
        super(context, R.layout.routeguide_row, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        StationWrapper stationWrapper = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.routeguide_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            viewHolder.stationLocation = (TextView) convertView.findViewById(R.id.stationLocation);
            viewHolder.stationTimeLimit = (TextView) convertView.findViewById(R.id.stationTimeLimit);
            viewHolder.stationRelocationTime = (TextView) convertView.findViewById(R.id.relocationTime);
            viewHolder.relocationView = convertView.findViewById(R.id.relocation_container);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.stationName.setText(stationWrapper.station.getName());
        viewHolder.stationLocation.setText(stationWrapper.station.getLocation());
        viewHolder.stationTimeLimit.setText("Time limit: " + stationWrapper.station.getTimeLimit());
        viewHolder.stationRelocationTime.setText(stationWrapper.station.getRelocationTime() + "-minute walk");

        if(position == (getCount() - 1)){
            viewHolder.relocationView.setVisibility(View.GONE);
        } else {
            viewHolder.relocationView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

}