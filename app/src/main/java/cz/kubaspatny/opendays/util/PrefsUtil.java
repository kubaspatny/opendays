package cz.kubaspatny.opendays.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import cz.kubaspatny.opendays.ui.activity.BaseActivity;

/**
 * Utility class for working with Android's Shared Preferences.
 */
public class PrefsUtil {

    private static final String TAG = PrefsUtil.class.getSimpleName();
    private static final String GROUPS_COUNT_REMOTE = "groupsCountRemote";
    private static final String GROUPS_COUNT_CACHED = "groupsCountCached";
    private static final String MANAGEDROUTES_COUNT_REMOTE = "managedRoutesCountRemote";
    private static final String MANAGEDROUTES_COUNT_CACHED = "managedRoutesCountCached";

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(BaseActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Sets the number of groups available at the remote server.
     */
    public static void setRemoteGroupsCount(Context context, int count){
        Log.d(TAG, "Setting remote group count: " + count);
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(GROUPS_COUNT_REMOTE, count);
        editor.commit();
    }

    public static int getRemoteGroupsCount(Context context){
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(GROUPS_COUNT_REMOTE, -1);
    }

    /**
     * Sets the number of groups cached on the device.
     */
    public static void setCachedGroupsCount(Context context, int count){
        Log.d(TAG, "Setting cached group count: " + count);
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(GROUPS_COUNT_CACHED, count);
        editor.commit();
    }

    public static int getCachedGroupsCount(Context context){
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(GROUPS_COUNT_CACHED, -1);
    }

    /**
     * Sets the number of managed routes available at the remote server.
     */
    public static void setRemoteManagedRoutesCount(Context context, int count){
        Log.d(TAG, "Setting remote managed route count: " + count);
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(MANAGEDROUTES_COUNT_REMOTE, count);
        editor.commit();
    }

    public static int getRemoteManagedRoutesCount(Context context){
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(MANAGEDROUTES_COUNT_REMOTE, -1);
    }

    /**
     * Sets the number of managed routes cached on the device.
     */
    public static void setCachedManagedRoutesCount(Context context, int count){
        Log.d(TAG, "Setting cached managed route count: " + count);
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(MANAGEDROUTES_COUNT_CACHED, count);
        editor.commit();
    }

    public static int getCachedManagedRoutesCount(Context context){
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(MANAGEDROUTES_COUNT_CACHED, -1);
    }

}
