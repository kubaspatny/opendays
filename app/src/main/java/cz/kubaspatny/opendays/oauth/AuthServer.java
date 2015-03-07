package cz.kubaspatny.opendays.oauth;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.exception.LoginException;

/**
 * Created by Kuba on 6/3/2015.
 */
public class AuthServer {

    private final static String TAG = AuthServer.class.getSimpleName();

    public static AccessToken obtainAccessToken(String username, String password, String authType) throws Exception {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        StringBuilder postData = new StringBuilder();
        postData.append("grant_type=").append("password").append("&");
        postData.append("client_id=").append("android").append("&");
        postData.append("client_secret=").append("android").append("&");
        postData.append("username=").append(username).append("&");
        postData.append("password=").append(password).append("&");

        String url = "http://resttime-kubaspatny.rhcloud.com/oauth/token";
        String token = null;

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(mediaType, postData.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG, "Response code is not 200! It is " + response.code());
                throw new LoginException("Error obtaining new access token [" + response.body().string() + "]", response.code());
            }

            token = response.body().string();
        } catch (LoginException e){
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new LoginException(e.getMessage(), 999);
        }

        return new Gson().fromJson(token, AccessToken.class);
    }

    public static AccessToken refreshAccessToken(String username, String refreshToken, String authType) throws Exception {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        StringBuilder postData = new StringBuilder();
        postData.append("grant_type=").append("refresh_token").append("&");
        postData.append("client_id=").append("android").append("&");
        postData.append("client_secret=").append("android").append("&");
        postData.append("refresh_token=").append(refreshToken);

        String url = "http://resttime-kubaspatny.rhcloud.com/oauth/token";
        String token = null;

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(mediaType, postData.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG, "refreshAccessToken > " + refreshToken);
                Log.d(TAG, "refreshAccessToken > " + response.code() + " > " + response.body().string());
                throw new Exception("Error refreshing access token [" + response.body().string() + "]");
            }

            token = response.body().string();
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
        }

        return new Gson().fromJson(token, AccessToken.class);
    }

}
