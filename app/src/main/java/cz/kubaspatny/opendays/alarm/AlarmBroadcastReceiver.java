package cz.kubaspatny.opendays.alarm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * BroadcastReceiver receiving alarm broadcast notifying station time limit exceeding.
 */
public class AlarmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmBroadcastReceiver", "Alarm broadcast received.");

        // Explicitly specify that AlarmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), AlarmIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
