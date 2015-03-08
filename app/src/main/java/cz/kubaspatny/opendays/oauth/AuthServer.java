package cz.kubaspatny.opendays.oauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.kubaspatny.opendays.domainobject.AccessToken;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.exception.LoginException;
import cz.kubaspatny.opendays.json.DateTimeSerializer;

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

    public static List<GroupDto> getGroups(Activity activity, AccountManager mAccountManager) throws Exception {

        String url = "http://resttime-kubaspatny.rhcloud.com/api/v1/user/login4/groups?page=0&pageSize=25";
        String json;

        OkHttpClient client = new OkHttpClient();
        final Request.Builder requestBuilder = new Request.Builder()
                .url(url);


        Account[] accounts = mAccountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            String oldToken = mAccountManager.peekAuthToken(accounts[0], AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS);
            if (oldToken != null) {
                mAccountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE, oldToken);
            }
        }

        AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(AuthConstants.ACCOUNT_TYPE, AuthConstants.AUTHTOKEN_TYPE_FULL_ACCESS, null, activity, null, null, null, null);
        Bundle bundle = future.getResult();
        String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        requestBuilder.addHeader("Authorization", "Bearer " + token);
        Log.d(TAG, "getGroups > token: " + token);

        Request request = requestBuilder.build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG, "Response code is not 200! It is " + response.code());
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                Log.d(TAG, "Error loading groups! " + errorMessage.getMessage());

                throw new Exception("Error loading groups! " + response.body().string());
            }

            json = response.body().string();

        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getMessage());
        }

        Log.d(TAG, "JSON: " + json);

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeSerializer()).create();
        return Arrays.asList(gson.fromJson(json, GroupDto[].class));
    }

    class ErrorMessage {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
