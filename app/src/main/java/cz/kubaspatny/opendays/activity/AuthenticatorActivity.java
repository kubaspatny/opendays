package cz.kubaspatny.opendays.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cz.kubaspatny.opendays.MainActivity;
import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.oauth.AuthConstants;
import cz.kubaspatny.opendays.oauth.AuthServer;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private final String TAG = this.getClass().getSimpleName();

    public final static String ARG_ACCOUNT_TYPE = AccountManager.KEY_ACCOUNT_TYPE;
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public static final String KEY_ERROR_CODE = "ERR_CODE";
    public final static String PARAM_USER_PASS = "USER_PASS";
    public final static String ARG_START_MAIN = "START_MAIN";

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        mAccountManager = AccountManager.get(getBaseContext());
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null){
            mAuthTokenType = AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS;
        }

        if (accountName != null) { // reprompting user for credentials
            ((EditText) findViewById(R.id.username)).setText(accountName);
        }

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        final String userName = ((TextView) findViewById(R.id.username)).getText().toString();
        final String userPass = ((TextView) findViewById(R.id.password)).getText().toString();
        final TextView loginError = (TextView) findViewById(R.id.loginError);

        final String accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        new AsyncTask<String, Void, Intent>() {
            @Override
            protected Intent doInBackground(String... params) {
                Log.d(TAG, "Authenticating using user credentials.");

                String authtoken = null;
                Bundle data = new Bundle();

                try {
                    AccessToken accessToken = AuthServer.obtainAccessToken(userName, userPass, mAuthTokenType);
                    authtoken = accessToken.getValue();
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                    data.putString(PARAM_USER_PASS, accessToken.getRefreshToken().getValue());
                } catch (LoginException e){
                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                    data.putInt(KEY_ERROR_CODE, e.getCode());
                } catch (Exception e) {
                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {

                    int error_code = intent.getIntExtra(KEY_ERROR_CODE, 999);

                    if(error_code == 400){
                        // display: Wrong username or password
                        loginError.setVisibility(View.VISIBLE);
                    } else {
                        // display: Oops! Couldn't log in. Check your internet connection.
                        Toast.makeText(AuthenticatorActivity.this, "Oops! Couldn't log in. Check internet connection.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "login > onPostExecute > " + error_code + " > " + intent.getStringExtra(KEY_ERROR_MESSAGE));
                    }

                } else {
                    finishLogin(intent);
                }
            }

        }.execute();
    }

    private void finishLogin(Intent intent) {
        Log.d(TAG, "finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);

        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "finishLogin -> addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d(TAG, "finishLogin -> setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        if (getIntent().getBooleanExtra(ARG_START_MAIN, false)){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        finish();
    }
}
