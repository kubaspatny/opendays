package cz.kubaspatny.opendays.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
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
        LinearLayout groupsAtStation;
        LinearLayout groupsAfterStation;
    }

    public RouteGuideArrayAdapter(Context context, List<StationWrapper> objects) {
        super(context, R.layout.routeguide_row, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        StationWrapper stationWrapper = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.routeguide_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            viewHolder.stationLocation = (TextView) convertView.findViewById(R.id.stationLocation);
            viewHolder.stationTimeLimit = (TextView) convertView.findViewById(R.id.stationTimeLimit);
            viewHolder.stationRelocationTime = (TextView) convertView.findViewById(R.id.relocationTime);
            viewHolder.relocationView = convertView.findViewById(R.id.relocation_container);
            viewHolder.groupsAtStation = (LinearLayout) convertView.findViewById(R.id.groups_at_station);
            viewHolder.groupsAfterStation = (LinearLayout) convertView.findViewById(R.id.groups_after_station);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.stationName.setText(stationWrapper.station.getName());
        viewHolder.stationLocation.setText(stationWrapper.station.getLocation());
        viewHolder.stationTimeLimit.setText("Time limit: " + stationWrapper.station.getTimeLimit());
        viewHolder.stationRelocationTime.setText(stationWrapper.station.getRelocationTime() + "-minute walk");

        viewHolder.groupsAtStation.removeAllViews();
        viewHolder.groupsAfterStation.removeAllViews();

        for(GroupDto g : stationWrapper.groups){
            View groupRow = inflater.inflate(R.layout.group, parent, false);
            ((TextView)groupRow.findViewById(R.id.group_guide)).setText(g.getGuide().getUsername());
            ((TextView)groupRow.findViewById(R.id.group_time)).setText(g.getLatestLocationUpdate().getTimestamp().toString("HH:mm"));

            if(g.getLatestLocationUpdate().getType() == LocationUpdateDto.LocationUpdateType.CHECKIN){
                viewHolder.groupsAtStation.addView(groupRow);
            } else {
                viewHolder.groupsAfterStation.addView(groupRow);
            }

        }

        if(position == (getCount() - 1)){
            viewHolder.relocationView.setVisibility(View.GONE);
        } else {
            viewHolder.relocationView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

}