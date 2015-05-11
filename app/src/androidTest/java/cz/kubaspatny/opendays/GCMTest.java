package cz.kubaspatny.opendays;

import android.accounts.Account;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.oauth.AuthConstants;
import cz.kubaspatny.opendays.oauth.AuthServer;
import cz.kubaspatny.opendays.sync.SyncEndpoint;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class GCMTest {

    @Test
    public void testGCMRegistration() throws Exception {
        AccessToken token = AuthServer.obtainAccessToken("login7", "login7", null);
        Account account = new Account("login7", AuthConstants.ACCOUNT_TYPE);

        SyncEndpoint.registerDevice(account, token.getValue(), System.nanoTime() + "");
    }

    @Test(expected = Exception.class)
    public void testGCMRegistrationWrongCredentials() throws Exception {
        Account account = new Account("login7", AuthConstants.ACCOUNT_TYPE);
        SyncEndpoint.registerDevice(account, "wrongtoken", System.nanoTime() + "");
    }

}
