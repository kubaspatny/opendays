package cz.kubaspatny.opendays.gcm;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.kubaspatny.opendays.sync.SyncEndpoint;
import cz.kubaspatny.opendays.util.AccountUtil;

/**
 * Created by Kuba on 14/3/2015.
 */
public class GcmUtil {

    private final static String TAG = GcmUtil.class.getSimpleName();

    public static void registerDevice(Context context, Account account, String registrationId) throws Exception{
        Log.d(TAG, "loadGuidedGroups");
        SyncEndpoint.registerDevice(account, AccountUtil.getAccessToken(context, account), registrationId);
    }


}
