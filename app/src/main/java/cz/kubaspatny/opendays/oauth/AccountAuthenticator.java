package cz.kubaspatny.opendays.oauth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import cz.kubaspatny.opendays.ui.activity.AuthenticatorActivity;
import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.domainobject.AccessToken;

/**
 * Created by Kuba on 6/3/2015.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private final String TAG = this.getClass().getSimpleName();

    private final Context mContext;

    public AccountAuthenticator(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {

        Log.d(TAG, "accountType: " + accountType);

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    /**
     *
     * @throws NetworkErrorException throws in case of network error while obtaining refresh token
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "getAuthToken");

        if (!authTokenType.equals(AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "Invalid authTokenType.");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String refreshToken = accountManager.getUserData(account, AuthConstants.REFRESH_TOKEN);
            if (refreshToken != null) {
                try {
                    Log.d(TAG, "Obtaining access token using existing refresh token.");
                    AccessToken accessToken = AuthServer.refreshAccessToken(account.name, refreshToken, authTokenType);

                    if(accessToken != null){
                        Log.d(TAG, "Received access & refresh token!");

                        if(accessToken.getRefreshToken() != null && !TextUtils.isEmpty(accessToken.getRefreshToken().getValue())){
                            accountManager.setUserData(account, AuthConstants.REFRESH_TOKEN, accessToken.getRefreshToken().getValue());
                        }

                        authToken = accessToken.getValue();
                    }

                } catch (NetworkErrorException e) {
                    Log.d(TAG, "Refresh failed due to network error! " + e.getLocalizedMessage());
                    throw new NetworkErrorException(e);

                } catch (Exception e) {
                    Log.d(TAG, "getAuthToken > " + e.getLocalizedMessage());
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            Log.d(TAG, "Returning access token.");
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        Log.d(TAG, "Still no token! Going to prompt user for credentials!");
        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        Log.d(TAG, "Disabling sync.");
        disableSync(account);

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    private static void disableSync(Account account){
        boolean pending = ContentResolver.isSyncPending(account, AppConstants.AUTHORITY);
        boolean active = ContentResolver.isSyncActive(account, AppConstants.AUTHORITY);

        if (pending || active) ContentResolver.cancelSync(account, AppConstants.AUTHORITY);
        ContentResolver.setSyncAutomatically(account, AppConstants.AUTHORITY, false);
        ContentResolver.removePeriodicSync(account, AppConstants.AUTHORITY, Bundle.EMPTY);
        ContentResolver.setIsSyncable(account, AppConstants.AUTHORITY, 0);
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType)){
            return AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        }

        return authTokenType;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }
}
