package cz.kubaspatny.opendays.gcm;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import cz.kubaspatny.opendays.sync.SyncEndpoint;
import cz.kubaspatny.opendays.util.AccountUtil;

/**
 * Utility class for operations with Google Cloud Messaging (GCM).
 */
public class GcmUtil {

    private final static String TAG = GcmUtil.class.getSimpleName();

    /**
     * Uploads given GCM registationId to the applications server.
     * @param account current user's account
     * @param registrationId GCM registration ID
     */
    public static void registerDevice(Context context, Account account, String registrationId) throws Exception{
        Log.d(TAG, "loadGuidedGroups");
        SyncEndpoint.registerDevice(account, AccountUtil.getAccessToken(context, account), registrationId);
    }

}
