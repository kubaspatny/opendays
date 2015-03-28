package cz.kubaspatny.opendays.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import cz.kubaspatny.opendays.gcm.GcmUtil;
import cz.kubaspatny.opendays.oauth.AuthConstants;
import cz.kubaspatny.opendays.util.AccountUtil;

import static cz.kubaspatny.opendays.util.ToastUtil.*;

public class BaseActivity extends ActionBarActivity implements OnAccountsUpdateListener {

    private final static String TAG = BaseActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String SENDER_ID = "721103451094";


    private AccountManager mAccountManager;
    GoogleCloudMessaging gcm;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {

        for (Account account : accounts) {
            if (AuthConstants.ACCOUNT_TYPE.equals(account.type)) {
                return;
            }
        }

        // No account of our type found -> start the authenticator activity
        Intent intent = new Intent(this, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AuthConstants.ACCOUNT_TYPE);
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);
        intent.putExtra(AuthenticatorActivity.ARG_START_MAIN, true);
        startActivity(intent);
        finish();

    }

    public boolean requireLogin(){
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Watch to make sure the account still exists.
        if(requireLogin()) mAccountManager.addOnAccountsUpdatedListener(this, null, true);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if (regid.isEmpty()) {
                Log.d(TAG, "register in background...");
                registerInBackground(this, AccountUtil.getAccount(this));
            } else {
                Log.d(TAG, "already registered: " + regid);
            }

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    @Override
    protected void onPause() {
        if(requireLogin()) mAccountManager.removeOnAccountsUpdatedListener(this);
        super.onPause();
    }

    public AccountManager getAccountManager() {
        return mAccountManager;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(BaseActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final Context context, final Account account) {
        new AsyncTask<Void, Void, String>() {

            Exception e = null;

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    GcmUtil.registerDevice(context, account, regid);

                    storeRegistrationId(context, regid);
                } catch (IOException e) {
                    this.e = e;
                    msg = "Error :" + e.getMessage();

                    // TODO:
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                } catch (Exception e){
                    this.e = e;
                    msg = "Error registering for GCM.";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if(e == null){
                    success(context, msg);
                } else {
                    error(context, msg);
                }
            }

        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public void clearRegistrationId(Context context){
        final SharedPreferences prefs = getGCMPreferences(context);
        Log.i(TAG, "Clearing regId.");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, "");
        editor.commit();
    }


}
