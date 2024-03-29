package cz.kubaspatny.opendays.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.StationWrapper;

/**
 * Array adapter displaying the guided route with its stations and other groups.
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
        CardView card;
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
            convertView = inflater.inflate(R.layout.routeguide_card_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            viewHolder.stationLocation = (TextView) convertView.findViewById(R.id.stationLocation);
            viewHolder.stationTimeLimit = (TextView) convertView.findViewById(R.id.stationTimeLimit);
            viewHolder.stationRelocationTime = (TextView) convertView.findViewById(R.id.relocationTime);
            viewHolder.relocationView = convertView.findViewById(R.id.relocation_container);
            viewHolder.groupsAtStation = (LinearLayout) convertView.findViewById(R.id.groups_at_station);
            viewHolder.groupsAfterStation = (LinearLayout) convertView.findViewById(R.id.groups_after_station);
            viewHolder.card = (CardView) convertView.findViewById(R.id.station_card);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.stationName.setText(stationWrapper.station.getName());

        // if station is closed -> gray it out
        if(stationWrapper.station.isClosed()){
            viewHolder.card.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_400));
            viewHolder.stationName.setTextColor(getContext().getResources().getColor(R.color.grey_600));
        } else {
            viewHolder.card.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
            viewHolder.stationName.setTextColor(getContext().getResources().getColor(R.color.grey_900));
        }

        viewHolder.stationLocation.setText(stationWrapper.station.getLocation());
        viewHolder.stationTimeLimit.setText(stationWrapper.station.getTimeLimit() + " min");
        viewHolder.stationRelocationTime.setText(getContext().getString(R.string.minute_walk, stationWrapper.station.getRelocationTime()));

        viewHolder.groupsAtStation.removeAllViews();
        viewHolder.groupsAfterStation.removeAllViews();

        // display groups
        for(GroupDto g : stationWrapper.groups){

            // Don't display the group, if the group has left/skipped its last station
            if(g.isAfterLast(stationWrapper.station.getSequencePosition(), g.getLatestLocationUpdate().getType())) continue;

            View groupRow = inflater.inflate(R.layout.group, parent, false);

            TextView groupName = (TextView) groupRow.findViewById(R.id.group_guide);
            groupName.setText(g.getGuide().getUsername());

            // set group icon:
            // current group    - GREEN
            // active group     - BLUE
            // inactive group   - GRAY
            if(g.isCurrentUser()){
                groupName.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_account_box_green_18dp), null, null, null);
            } else {
                if(g.isActive()){
                    groupName.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_account_box_blue_18dp), null, null, null);
                } else {
                    groupName.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_account_box_grey600_18dp), null, null, null);
                }
            }

            DateTime updateTime = g.getLatestLocationUpdate().getTimestamp();
            DateTime now = DateTime.now();
            long difSec = (now.getMillis() - updateTime.getMillis()) / 1000;

            long hours = (difSec / 3600);
            long minutes = (difSec % 3600) / 60;
            long seconds = difSec % 60;


            ((TextView)groupRow.findViewById(R.id.group_time)).setText(formatTime(getContext(), hours, minutes, seconds));

            // if last is CHECKIN -> display in the station box
            // if last is CHECKOUT, SKIP -> display after the station box
            if(g.getLatestLocationUpdate().getType() == LocationUpdateDto.LocationUpdateType.CHECKIN){
                viewHolder.groupsAtStation.addView(groupRow);
            } else {
                viewHolder.groupsAfterStation.addView(groupRow);
            }

        }

        return convertView;
    }

    public static String formatTime(Context context, long hours, long minutes, long seconds){

        if(hours >= 24) return context.getString(R.string.more_than_day);

        DateTime time = new DateTime().withTime((int)hours, (int)minutes, (int)seconds, 0);
        return time.toString("HH:mm:ss");

    }

    /**
     * Updates the list UI to set correct times.
     */
    public void forceUpdate(){
        notifyDataSetChanged();
    }


}