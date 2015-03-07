package cz.kubaspatny.opendays;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.oauth.AuthConstants;


public class MainActivity extends ActionBarActivity {

    TextView mTokenResponse;
    String refreshToken;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.get_token) {
            Toast.makeText(this, "Getting token...", Toast.LENGTH_SHORT).show();

            new AsyncTask<Void, Void, String>(){

                Exception exception = null;
                AccessToken accessToken = null;

                @Override
                protected String doInBackground(Void... params) {

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
                    String postData;
                    if(refreshToken == null){
                        postData = "username=login4&password=login4&client_id=android&client_secret=android&grant_type=password";
                    } else {
                        postData = "grant_type=refresh_token&client_id=android&client_secret=android&refresh_token=" + refreshToken;
                    }

                    String url = "http://resttime-kubaspatny.rhcloud.com/oauth/token";
                    String token = null;

                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(mediaType, postData);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        token = response.body().string();
                    } catch (Exception e){
                        Log.d("getTokenAsyncTask", e.getMessage());
                        exception = e;
                    }

                    Gson gson = new Gson();
                    accessToken = gson.fromJson(token, AccessToken.class);
                    refreshToken = accessToken.getRefreshToken().getValue();

                    return token;

                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);

                    if(exception != null){
                        mTokenResponse.setText(exception.getMessage());
                        return;
                    }

                    if(accessToken != null){
                        mTokenResponse.setText("Access Token: " + accessToken.getValue() + "\n" +
                                               "Type: " + accessToken.getTokenType() + "\n" +
                                               "Expires in: " + accessToken.getExpiresIn() + "\n" +
                                               "Refresh token: " + accessToken.getRefreshToken().getValue()
                        );
                        return;
                    }

                    mTokenResponse.setText(s);
                }

                @Override
                protected void onCancelled(String s) {
                    super.onCancelled(s);
                    mTokenResponse.setText("There was an error!");
                }
            }.execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final String STATE_DIALOG = "state_dialog";
    private static final String STATE_INVALIDATE = "state_invalidate";
    private AccountManager mAccountManager;
    private AlertDialog mAlertDialog;
    private boolean mInvalidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTokenResponse = (TextView) findViewById(R.id.token_response);


        mAccountManager = AccountManager.get(this);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAccount(AuthConstants.ACCOUNT_TYPE, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountPicker(AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, false);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTokenForAccountCreateIfNeeded(AuthConstants.ACCOUNT_TYPE, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountPicker(AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            }
        });
        if (savedInstanceState != null) {
            boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
            boolean invalidate = savedInstanceState.getBoolean(STATE_INVALIDATE);
            if (showDialog) {
                showAccountPicker(AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, invalidate);
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
            outState.putBoolean(STATE_INVALIDATE, mInvalidate);
        }
    }
    /**
     * Add new account to the account manager
     * @param accountType
     * @param authTokenType
     */
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d("udinic", "AddNewAccount Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }, null);
    }
    /**
     * Show all the accounts registered on the account manager. Request an auth token upon user select.
     * @param authTokenType
     */
    private void showAccountPicker(final String authTokenType, final boolean invalidate) {
        mInvalidate = invalidate;
        final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }
// Account picker
            mAlertDialog = new AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(invalidate)
                        invalidateAuthToken(availableAccounts[which], authTokenType);
                    else
                        getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                }
            }).create();
            mAlertDialog.show();
        }
    }
    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }
    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    mAccountManager.invalidateAuthToken(account.type, authtoken);
                    showMessage(account.name + " invalidated");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }
    /**
     * Get an auth token for the account.
     * If not exist - add it and then return its auth token.
     * If one exist - return its auth token.
     * If more than one exists - show a picker and return the select account's auth token.
     * @param accountType
     * @param authTokenType
     */
    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            showMessage(((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL"));
                            Log.d("udinic", "GetTokenForAccount Bundle is " + bnd);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }
    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
