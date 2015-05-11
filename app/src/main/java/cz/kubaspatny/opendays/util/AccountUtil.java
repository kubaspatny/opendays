package cz.kubaspatny.opendays.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;

import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.oauth.AuthConstants;

/**
 * Created by Kuba on 13/3/2015.
 */
public class AccountUtil {

    /**
     * Returns current user's account.
     */
    public static Account getAccount(final Context context){
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);

        if(accounts.length == 0){
            return null;
        } else {
            return accounts[0];
        }
    }

    /**
     * Removes current user's account.
     */
    public static void removeAccount(final Context context){
        AccountManager accountManager = AccountManager.get(context);
        accountManager.removeAccount(getAccount(context), null, null);
    }

    /**
     * Obtains an access token using current user's account.
     */
    public static String getAccessToken(Context context, Account account) throws LoginException, NetworkErrorException, IOException, AuthenticatorException, OperationCanceledException {

        AccountManager accountManager = AccountManager.get(context);
        String oldToken = accountManager.peekAuthToken(account, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);

        if (oldToken != null) {
            accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE, oldToken);
        }

        String token = accountManager.blockingGetAuthToken(account, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, true);

        if(token == null || TextUtils.isEmpty(token)){
            throw new LoginException("Couldn't obtain access token.", 999);
        }

        return token;
    }

}
