package cz.kubaspatny.opendays;

import android.accounts.Account;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.exception.ErrorCodeException;
import cz.kubaspatny.opendays.oauth.AuthConstants;
import cz.kubaspatny.opendays.oauth.AuthServer;
import cz.kubaspatny.opendays.sync.SyncEndpoint;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FetcherTest {

    @Test
    public void testGetGroups() throws Exception {

        AccessToken token = AuthServer.obtainAccessToken("login4", "login4", null);
        Account account = new Account("login4", AuthConstants.ACCOUNT_TYPE);
        int count = -1;

        try {
            count = SyncEndpoint.getGroupsCount(account, token.getValue());
            Log.d("FetcherTest", "count = " + count);
        } catch (ErrorCodeException e){
            // no groups founds
            count = 0;
        }

        Assert.assertTrue(count >= 0);

        try {
            List<GroupDto> groups = SyncEndpoint.getGroups(account, token.getValue(), 0, count);
            Assert.assertNotNull(groups);
            Assert.assertEquals(count, groups.size());
        } catch (Exception e){
            if(count == 0){
                // SUCCESS
            } else {
                throw e;
            }
        }

    }

    @Test
    public void testGetManagedRoutes() throws Exception {

        AccessToken token = AuthServer.obtainAccessToken("login4", "login4", null);
        Account account = new Account("login4", AuthConstants.ACCOUNT_TYPE);
        int count = -1;

        try {
            count = SyncEndpoint.getManagedRoutesCount(account, token.getValue());
            Log.d("FetcherTest", "count = " + count);
        } catch (ErrorCodeException e){
            // no groups founds
            count = 0;
        }

        Assert.assertTrue(count >= 0);

        try {
            List<RouteDto> routes = SyncEndpoint.getManagedRoutes(account, token.getValue(), 0, count);
            Assert.assertNotNull(routes);
            Assert.assertEquals(count, routes.size());
        } catch (Exception e){
            if(count == 0){
                // SUCCESS
            } else {
                throw e;
            }
        }

    }
}
