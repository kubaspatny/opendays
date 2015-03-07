package cz.kubaspatny.opendays.oauth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Kuba on 6/3/2015.
 */
public class AuthenticationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }

}
