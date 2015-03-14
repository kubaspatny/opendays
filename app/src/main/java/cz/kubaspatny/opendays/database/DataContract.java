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
