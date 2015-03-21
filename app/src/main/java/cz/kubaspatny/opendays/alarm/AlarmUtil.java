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

    public final static String MESSAGE = "cz.kubaspatny.opendays.alarm.AlarmUtil.message";
    public final static String SECONDS = "cz.kubaspatny.opendays.alarm.AlarmUtil.seconds";
    public final static String REPEAT = "cz.kubaspatny.opendays.alarm.AlarmUtil.repeat";
    public final static String STATION = "cz.kubaspatny.opendays.alarm.AlarmUtil.station";
    public final static String ID = "cz.kubaspatny.opendays.alarm.AlarmUtil.id";

    public static void setAlarm(Context context, int id, int seconds, String station, String message, boolean repeat){

        if(seconds <= 0) return;

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent;
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(REPEAT, repeat);
        intent.putExtra(SECONDS, seconds);
        intent.putExtra(STATION, station);
        intent.putExtra(ID, id);
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
