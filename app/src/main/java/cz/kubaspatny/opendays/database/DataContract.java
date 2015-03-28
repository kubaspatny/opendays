package cz.kubaspatny.opendays.database;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * Created by Kuba on 8/3/2015.
 */
public class DataContract {

    public DataContract() {
    }

    public static abstract class GuidedGroups implements BaseColumns {

        public static final String TABLE_NAME = "guided_groups";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_GROUP_ID = "groupid";
        public static final String COLUMN_NAME_GROUP_STARTING_POSITION = "group_starting_position";
        public static final String COLUMN_NAME_GROUP_ACTIVE = "group_active";
        public static final String COLUMN_NAME_ROUTE_ID = "routeid";
        public static final String COLUMN_NAME_ROUTE_NAME = "route_name";
        public static final String COLUMN_NAME_ROUTE_COLOR = "route_color";
        public static final String COLUMN_NAME_ROUTE_INFORMATION = "route_information";
        public static final String COLUMN_NAME_ROUTE_TIMESTAMP = "route_timestamp";
        public static final String COLUMN_NAME_EVENT_ID = "eventid";
        public static final String COLUMN_NAME_EVENT_NAME = "event_name";
        // TODO: remove unused columns, to get rid of data duplication

    }

    public static abstract class Route implements BaseColumns {

        public static final String TABLE_NAME = "route";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_ROUTE_ID = "routeid";
        public static final String COLUMN_NAME_ROUTE_NAME = "route_name";
        public static final String COLUMN_NAME_ROUTE_COLOR = "route_color";
        public static final String COLUMN_NAME_ROUTE_INFORMATION = "route_information";
        public static final String COLUMN_NAME_ROUTE_TIMESTAMP = "route_timestamp";

        public static final String COLUMN_NAME_EVENT_ID = "eventid";
        public static final String COLUMN_NAME_EVENT_NAME = "event_name";
        public static final String COLUMN_NAME_EVENT_INFORMATION = "event_information";

    }

    public static abstract class Station implements BaseColumns {

        public static final String TABLE_NAME = "station";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_ROUTE_ID = "routeid";

        public static final String COLUMN_NAME_STATION_ID = "stationid";
        public static final String COLUMN_NAME_STATION_NAME = "station_name";
        public static final String COLUMN_NAME_STATION_LOCATION = "station_location";
        public static final String COLUMN_NAME_STATION_SEQ_POSITION = "station_seq_position";
        public static final String COLUMN_NAME_STATION_TIME_LIMIT = "station_time_limit";
        public static final String COLUMN_NAME_STATION_TIME_RELOCATION = "station_time_relocation";
        public static final String COLUMN_NAME_STATION_STATUS = "station_status"; // OPENED, CLOSED

    }

    /**
     * This table represents other groups locations. Current user's
     * location update queue is in table LocationUpdates.
     */
    public static abstract class GroupLocations implements BaseColumns {

        public static final String TABLE_NAME = "grouplocations";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_ROUTE_ID = "routeid";
        public static final String COLUMN_NAME_STATION_ID = "stationid";
        public static final String COLUMN_NAME_LOCATION_UPDATE_TYPE = "location_update_type"; // CHECKIN, CHECKOUT, SKIP
        public static final String COLUMN_NAME_LOCATION_UPDATE_TIMESTAMP = "location_update_timestamp";
        public static final String COLUMN_NAME_GROUP_ID = "groupid";
        public static final String COLUMN_NAME_GROUP_GUIDE = "group_guide";
        public static final String COLUMN_NAME_GROUP_STATUS = "group_status";
        public static final String COLUMN_NAME_GROUP_SEQ_POSITION = "group_seq_position";

    }

    public static abstract class LocationUpdates implements BaseColumns {

        public static final String TABLE_NAME = "locationupdates";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_ROUTE_ID = "routeid";
        public static final String COLUMN_NAME_GROUP_ID = "groupid";
        public static final String COLUMN_NAME_STATION_ID = "stationid";
        public static final String COLUMN_NAME_LOCATION_UPDATE_TYPE = "location_update_type"; // CHECKIN, CHECKOUT, SKIP
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    }

    public static abstract class GroupSizes implements BaseColumns {

        public static final String TABLE_NAME = "groupsizes";
        public static final Uri CONTENT_URI = DbContentProvider.CONTENT_URI.buildUpon().path(TABLE_NAME).build();

        public static final String COLUMN_NAME_GROUP_ID = "groupid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_GROUP_SIZE = "groupsize";

    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

}
