package cz.kubaspatny.opendays.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Kuba on 21/3/2015.
 */
public class AlarmUtil {

    public static void setAlarm(Context context, int id, int seconds, String message){

        Log.d("Alarm util", "Setting alarm with message [" + message + "] in " + seconds + " seconds.");

        if(seconds <= 0) return;

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent;
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        Log.d("AlarmUtil", "putting intent extra msg=" + message);
        intent.putExtra("msg", message);
        alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= 19){
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + seconds * 1000,
                    alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + seconds * 1000,
                    alarmIntent);
        }

    }

    public static void cancelAlarm(Context context, int id){

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr.cancel(alarmIntent);

    }

}
