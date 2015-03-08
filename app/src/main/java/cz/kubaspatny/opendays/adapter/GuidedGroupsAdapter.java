package cz.kubaspatny.opendays.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
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
import cz.kubaspatny.opendays.widget.CircleView;

/**
 * Created by Kuba on 8/3/2015.
 */
public class GuidedGroupsAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;
    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    public GuidedGroupsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.guided_groups_list_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView routeName = (TextView) view.findViewById(R.id.routeName);
        TextView routeTime = (TextView) view.findViewById(R.id.routeTime);
        CircleView cirle = (CircleView) view.findViewById(R.id.circle);

        String routeNameString = cursor.getString(cursor.getColumnIndex(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME));
        String routeTimeString = cursor.getString(cursor.getColumnIndex(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP));
        String circleColorString = cursor.getString(cursor.getColumnIndex(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR));

        DateTime dateTime = dateTimeFormatter.parseDateTime(routeTimeString);
        dateTime = dateTime.toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));

        routeName.setText(routeNameString);
        routeTime.setText(dateTime.toString("dd.MM.yyyy HH:mm"));
        cirle.setCircleColor(circleColorString);

    }
}
