package cz.kubaspatny.opendays.sync;

import android.accounts.Account;
import android.accounts.NetworkErrorException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import cz.kubaspatny.opendays.domainobject.ErrorMessage;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.domainobject.GroupSizeDto;
import cz.kubaspatny.opendays.domainobject.GroupStartingPosition;
import cz.kubaspatny.opendays.domainobject.LocationUpdateDto;
import cz.kubaspatny.opendays.domainobject.RouteDto;
import cz.kubaspatny.opendays.exception.ErrorCodeException;
import cz.kubaspatny.opendays.json.DateTimeSerializer;

import static cz.kubaspatny.opendays.app.AppConstants.*;

/**
 * Created by Kuba on 11/3/2015.
 */
public class SyncEndpoint {

    public final static String TAG = SyncEndpoint.class.getSimpleName();

    public static List<GroupDto> getGroups(Account account, String accessToken, int page, int pageSize) throws Exception {

        String url = HOST + API_V1 + "user/" + account.name + "/groups?page=" + page + "&pageSize=" + pageSize;
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

    public static List<RouteDto> getManagedRoutes(Account account, String accessToken, int page, int pageSize) throws Exception {

        String url = HOST + API_V1 + "user/" + account.name + "/managedroutes?page=" + page + "&pageSize=" + pageSize;
        String json;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG + ".getManagedRoutes", "Response code " + response.code());
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                Log.d(TAG, "Error loading managed routes! " + errorMessage.getMessage());

                throw new ErrorCodeException("Error loading managed routes! " + response.body().string(), response.code());
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
        return Arrays.asList(gson.fromJson(json, RouteDto[].class));
    }

    public static RouteDto getRoute(Account account, String accessToken, String routeId) throws Exception {

        String url = HOST + API_V1 + "route/" + routeId;
        String json;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG + ".getRoute", "Response code " + response.code());
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                Log.d(TAG, "Error loading route! " + errorMessage.getMessage());

                throw new ErrorCodeException("Error loading route! " + response.body().string(), response.code());
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
        return gson.fromJson(json, RouteDto.class);

    }

    public static List<GroupDto> getRouteGroups(Account account, String accessToken, String routeId) throws Exception {

        String url = HOST + API_V1 + "route/" + routeId + "/groups";
        String json;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG + ".getRouteGroups", "Response code " + response.code());
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

    // TODO: make methods in syncendpoint return http codes
    public static void registerDevice(Account account, String accessToken, String registrationId) throws Exception {

        String url = HOST + API_V1 + "gcm/android-device";
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, registrationId);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                Log.d(TAG + "registerDevice", "Response code " + response.code());
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                Log.d(TAG, "Registering device for GCM! " + errorMessage.getMessage());

                throw new ErrorCodeException("Error registering device for GCM! " + response.body().string(), response.code());
            }

        } catch (UnknownHostException e) {
            throw new NetworkErrorException(e.getLocalizedMessage());
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    public static void uploadLocationUpdates(Account account, String accessToken, LocationUpdateDto updateDto) throws Exception {

        String url = HOST + API_V1 + "group/locationUpdate";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeSerializer()).create();
        RequestBody body = RequestBody.create(mediaType, gson.toJson(updateDto));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                throw new ErrorCodeException("Error uploading location update! " + response.body().string(), response.code());
            }

        } catch (UnknownHostException e) {
            throw new NetworkErrorException(e.getLocalizedMessage());
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    public static void uploadGroupSize(Account account, String accessToken, GroupSizeDto sizeDto) throws Exception {

        String url = HOST + API_V1 + "group/groupSize";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeSerializer()).create();
        RequestBody body = RequestBody.create(mediaType, gson.toJson(sizeDto));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                throw new ErrorCodeException("Error uploading group size! " + response.body().string(), response.code());
            }

        } catch (UnknownHostException e) {
            throw new NetworkErrorException(e.getLocalizedMessage());
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    public static void updateStartingPosition(Account account, String accessToken, GroupStartingPosition startingPosition) throws Exception {
        String url = HOST + API_V1 + "group/startingPosition";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeSerializer()).create();
        RequestBody body = RequestBody.create(mediaType, gson.toJson(startingPosition));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if(response.code() != 200){
                ErrorMessage errorMessage = new Gson().fromJson(response.body().string(), ErrorMessage.class);
                throw new ErrorCodeException("Error updating starting position! " + response.body().string(), response.code());
            }

        } catch (UnknownHostException e) {
            throw new NetworkErrorException(e.getLocalizedMessage());
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

}
