package cz.kubaspatny.opendays;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.kubaspatny.opendays.ui.activity.MainActivity;
import cz.kubaspatny.opendays.util.ConnectionUtils;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConnectivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testIsConnected() throws Exception {
        Context context = mActivityRule.getActivity().getBaseContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
        Assert.assertEquals(connected, ConnectionUtils.isConnected(context));
    }
}
