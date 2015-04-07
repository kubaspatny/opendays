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
 * Created by Kuba on 21/3/2015.
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
            sendNotification(extras.getString(AlarmUtil.MESSAGE));


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

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND)
                        .setContentTitle(getString(R.string.time_limit))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
