package cz.kubaspatny.opendays.net;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import cz.kubaspatny.opendays.domainobject.ErrorMessage;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.exception.ErrorCodeException;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.json.DateTimeSerializer;
import cz.kubaspatny.opendays.oauth.AuthConstants;

/**
 * Created by Kuba on 11/3/2015.
 */
public class SyncEndpoint {

    public final static String TAG = SyncEndpoint.class.getSimpleName();

    public static List<GroupDto> getGroups(Account account, String accessToken, int page, int pageSize) throws Exception {

        String url = "http://resttime-kubaspatny.rhcloud.com/api/v1/user/" + account.name + "/groups?page=" + page + "&pageSize=" + pageSize;
        String json;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG + ".getGroups", "Response code " + response.code());
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                Log.d(TAG, "Error loading groups! " + errorMessage.getMessage());

                throw new ErrorCodeException("Error loading groups! " + response.body().string(), response.code());
            }

            json = response.body().string();

        } catch (UnknownHostException e) {
            throw new NetworkErrorException(e.getLocalizedMessage());
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        Log.d(TAG, "Parsing json.");
        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeSerializer()).create();
        return Arrays.asList(gson.fromJson(json, GroupDto[].class));
    }

}
