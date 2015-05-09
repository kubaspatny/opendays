package cz.kubaspatny.opendays.gcm;

import android.accounts.Account;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.sync.SyncHelper;
import cz.kubaspatny.opendays.ui.activity.MainActivity;
import cz.kubaspatny.opendays.util.AccountUtil;

import static cz.kubaspatny.opendays.app.AppConstants.*;

/**
 * Created by Kuba on 14/3/2015.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle

            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if  (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Log.d("GcmIntentService", "Received GCM notification.");

                Account account = AccountUtil.getAccount(getBaseContext());
                if(account == null) return;
                int type = Integer.parseInt(extras.getString(EXTRA_NOTIFICATION_TYPE, "-1"));

                switch(type){
                    case TYPE_SYNC_ALL:
                        SyncHelper.requestManualSync(getBaseContext(), account);
                        Log.d("GcmIntentService", "SYNC_ALL");
                        break;
                    case TYPE_SYNC_ROUTE:
                        syncRoute(Long.parseLong(extras.getString(EXTRA_ROUTE_ID, "-1")), account);
                        Log.d("GcmIntentService", "SYNC_ROUTE");
                        break;
                    case TYPE_LOCATION_UPDATE:
                        sendLocationUpdateNotification(
                                Long.parseLong(extras.getString(EXTRA_ROUTE_ID, "-1")),
                                Long.parseLong(extras.getString(EXTRA_STATION_ID, "-1")),
                                extras.getString(EXTRA_UPDATE_TYPE, ""),
                                Boolean.parseBoolean(extras.getString(EXTRA_GROUP_BEFORE, "false")),
                                Boolean.parseBoolean(extras.getString(EXTRA_GROUP_AFTER, "false"))
                        );
                        Log.d("GcmIntentService", "LOCATION_UPDATE");
                        break;
                    default:
                        Log.d("GcmIntentService", "default: " + type);
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendLocationUpdateNotification(Long routeId, Long stationId, String updateType, boolean groupBefore, boolean groupAfter){
        // TODO:
        if(!groupBefore && !groupAfter) return;

        String routeName = getRouteName(routeId);
        String string1 = groupBefore ? getString(R.string.BEFORE) : getString(R.string.AFTER);
        String string2 = null;

        switch(updateType){
            case "CHECKIN":
                string2 = getString(R.string.CHECKIN);
                break;
            case "CHECKOUT":
                string2 = getString(R.string.CHECKOUT);
                break;
            case "SKIP":
                string2 = getString(R.string.SKIP);
                break;
        }

        String string3 = getStationName(stationId);

        sendNotification(routeName, getString(R.string.notification_loc_update, string1, string2, string3));
    }

    private String getRouteName(Long routeId){

        String[] projectionRoute = {DataContract.Route._ID,
                DataContract.Route.COLUMN_NAME_ROUTE_ID,
                DataContract.Route.COLUMN_NAME_ROUTE_NAME};

        Cursor cursor = getBaseContext().getContentResolver().query(DataContract.Route.CONTENT_URI,
                projectionRoute,
                DataContract.Route.COLUMN_NAME_ROUTE_ID + "=?",
                new String[]{routeId + ""},
                null);
        cursor.moveToFirst();
        String routeName = null;
        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){
            routeName = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Route.COLUMN_NAME_ROUTE_NAME));
            break;
        }
        cursor.close();

        return routeName;
    }

    private String getStationName(Long stationId){

        String[] projectionStation = {DataContract.Station._ID,
                DataContract.Station.COLUMN_NAME_STATION_ID,
                DataContract.Station.COLUMN_NAME_STATION_NAME};

        Cursor cursor = getBaseContext().getContentResolver().query(DataContract.Station.CONTENT_URI,
                projectionStation,
                DataContract.Station.COLUMN_NAME_STATION_ID + "=?",
                new String[]{stationId + ""},
                null);
        cursor.moveToFirst();
        String stationName = null;
        while(cursor.getCount() != 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()){
            stationName = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.Station.COLUMN_NAME_STATION_NAME));
            break;
        }
        cursor.close();

        return stationName;
    }

    private void syncRoute(Long routeId, Account account){
        Log.d("GcmIntentService", "syncRoute(" + routeId + ")");

        Bundle b = new Bundle();
        SyncHelper.requestManualSync(getBaseContext(), account, b);
    }

    private void sendNotification(String title, String message) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_walk_grey600_24dp) // TODO: change to white icon
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
