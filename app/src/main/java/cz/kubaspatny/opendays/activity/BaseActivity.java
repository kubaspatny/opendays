package cz.kubaspatny.opendays.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.oauth.AuthConstants;

public class BaseActivity extends ActionBarActivity implements OnAccountsUpdateListener {

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mAccountManager = AccountManager.get(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

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

    }

    @Override
    protected void onPause() {
        if(requireLogin()) mAccountManager.removeOnAccountsUpdatedListener(this);

        super.onPause();
    }

}
