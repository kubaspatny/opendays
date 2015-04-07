package cz.kubaspatny.opendays.app;

/**
 * Created by Kuba on 11/3/2015.
 */
public class AppConstants {

    public static final String AUTHORITY = "cz.kubaspatny.opendays.provider";

    public static final String HOST = "http://resttime-kubaspatny.rhcloud.com/";
    public static final String API_V1 = "api/v1/";

    public static final String KEY_SYNC_STATUS = "cz.kubaspatny.opendays.sync_status";
    public static final String KEY_SYNC_STATUS_CODE = "cz.kubaspatny.opendays.sync_status_code";
    public static final int SYNC_STATUS_CODE_START = 1;
    public static final int SYNC_STATUS_CODE_END = 2;
    public static final int SYNC_STATUS_CODE_ERROR = 3;

    //NOTIFICATIONS
    public final static int TYPE_SYNC_ALL = 1;
    public final static int TYPE_SYNC_ROUTE = 2;
    public final static int TYPE_MESSAGE = 3;
    public final static int TYPE_LOCATION_UPDATE = 4;

    public final static long MINUTE_SYNC_PERIOD = 60;
    public final static long HOUR_SYNC_PERIOD = 60 * 60;

    public final static String EXTRA_ROUTE_ID = "routeId";
    public final static String EXTRA_GROUP_BEFORE = "groupBefore";
    public final static String EXTRA_GROUP_AFTER = "groupAfter";
    public final static String EXTRA_STATION_ID = "stationId";
    public final static String EXTRA_UPDATE_TYPE = "updateType";
    public final static String EXTRA_MESSAGE = "message";
    public final static String EXTRA_NOTIFICATION_TYPE = "notificationType";

}
