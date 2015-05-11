package cz.kubaspatny.opendays;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.oauth.AuthServer;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenTest {

    @Test
    public void obtainAccessToken() throws Exception {
        AccessToken token = AuthServer.obtainAccessToken("login4", "login4", null);
        Assert.assertNotNull(token);
        Assert.assertFalse(TextUtils.isEmpty(token.getValue()));
        Assert.assertFalse(TextUtils.isEmpty(token.getRefreshToken().getValue()));
    }

    @Test(expected=LoginException.class)
    public void obtainAccessTokenWrongCredentials() throws Exception {
        AccessToken token = AuthServer.obtainAccessToken("login4", "wrongpassword", null);
    }

    @Test
    public void refreshToken() throws Exception {
        String refreshToken = AuthServer.obtainAccessToken("login4", "login4", null).getRefreshToken().getValue();

        AccessToken token = AuthServer.refreshAccessToken("login4", refreshToken, null);
        Assert.assertNotNull(token);
        Assert.assertFalse(TextUtils.isEmpty(token.getValue()));
        Assert.assertFalse(TextUtils.isEmpty(token.getRefreshToken().getValue()));
    }

    @Test
    public void refreshExpiredToken() throws Exception {
        AccessToken token = AuthServer.refreshAccessToken("login4", "notcorrecttoken", null);
        Assert.assertNull(token);
    }

}
