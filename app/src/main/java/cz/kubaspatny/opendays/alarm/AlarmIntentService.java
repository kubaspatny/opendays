package cz.kubaspatny.opendays.alarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.ui.activity.MainActivity;

/**
 * IntentService to handle Alarm Intents.
 */
public class AlarmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 2;
    private NotificationManager mNotificationManager;

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("AlarmIntentService", "onHandleIntent");

        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            sendNotification(extras.getString(AlarmUtil.MESSAGE), !extras.getBoolean(AlarmUtil.REPEAT, true));

            // if REPEAT == true, then only 1/2 time is exhausted
            // therefore set another alarm for the same time
            if(extras.getBoolean(AlarmUtil.REPEAT, false)){
                AlarmUtil.setAlarm(this,
                        extras.getInt(AlarmUtil.ID),
                        extras.getInt(AlarmUtil.SECONDS),
                        extras.getString(AlarmUtil.STATION),
                        getString(R.string.end_time_notification),
                        false);
            }

        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        AlarmBroadcastReceiver.completeWakefulIntent(intent);

    }

    /**
     * Displays notification in the system's notification tray.
     * @param msg Message text
     * @param annoy Whether to set annoying vibrations or not
     */
    private void sendNotification(String msg, boolean annoy) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_walk_grey600_24dp) // TODO: change to white
                        .setDefaults(NotificationCompat.DEFAULT_SOUND)
                        .setContentTitle(getString(R.string.time_limit))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        if(annoy){
            mBuilder.setVibrate(new long[]{200l, 500l, 200l, 500l, 200l, 1000l, 200l, 1000l, 200l, 1000l, 200l, 1000l, 200l});
        } else {
            mBuilder.setVibrate(new long[]{200l, 500l, 200l, 500l, 200l});
        }

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
