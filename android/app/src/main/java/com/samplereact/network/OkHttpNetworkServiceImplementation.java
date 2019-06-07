package com.samplereact.network;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import com.samplereact.functionality.ActivityFunctionality;
import com.samplereact.functionality.StepCountsCalculation;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;

import okhttp3.Headers;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpNetworkServiceImplementation {
    //avoid creating several instances, should be singleton
    private static OkHttpClient client = null;
    private static OkHttpNetworkServiceImplementation instance = null;
    private int status;
    //Network Request Format
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Headers headerbuild;

    private OkHttpNetworkServiceImplementation() {
        // Exists only to defeat instantiation.
    }

    public static synchronized OkHttpNetworkServiceImplementation getInstance() {
        if (instance == null) {
            instance = new OkHttpNetworkServiceImplementation();
        }
        return instance;
    }

    @SuppressLint("LongLogTag")
    public String protocolRequest(String url, String jsonData, HashMap<String, String> header) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setHeaderbuild(header);

        client = new OkHttpClient.Builder()
                .cookieJar(StepCountsCalculation.cookieHelper.cookieJar())
                .build();

        //Post JSON Request And Response
        RequestBody body = RequestBody.create(JSON, jsonData);
        Log.d("steps Post Resquest:", jsonData);
        Request request = new Request.Builder().url(url).headers(headerbuild).post(body).build();
        Response response = client.newCall(request).execute();

        //Set Response Status
        this.status = response.code();
        String responseData = response.body().string();

        //Print Reponse
        Log.d("steps API Status Code : ", status + "");
        Log.d("steps API Response Body: ", responseData);
        return responseData;
    }

    @SuppressLint("LongLogTag")
    public String protocolRequestActivity(String url, String jsonData, HashMap<String, String> header) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setHeaderbuild(header);

        client = new OkHttpClient.Builder()
                .cookieJar(StepCountsCalculation.cookieHelper.cookieJar())
                .build();

        //Post JSON Request And Response
        RequestBody body = RequestBody.create(JSON, jsonData);
        Log.d("Activity Post Resquest:", jsonData);
        Request request = new Request.Builder().url(url).headers(headerbuild).post(body).build();
        Response response = client.newCall(request).execute();

        //Set Response Status
        this.status = response.code();
        String responseData = response.body().string();

        //Print Reponse
        Log.d("Activity API Status Code : ", status + "");
        Log.d("Activity API Response Body: ", responseData);
        return responseData;
    }

    public String loginRequest(String url, String jsonData, HashMap<String, String> header) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();


        setHeaderbuild(header);
        //Post JSON Request And Response
        RequestBody body = RequestBody.create(JSON, jsonData);
        Log.d("Login Post Resquest:", jsonData);
        Request request = new Request.Builder().url(url).headers(headerbuild).post(body).build();
        Response response = client.newCall(request).execute();

        //Set Response Status
        this.status = response.code();
        String responseData = response.body().string();

        List<HttpCookie> cookieList = cookieManager.getCookieStore().getCookies();

        for (int i = 0; i < cookieList.size(); i++) {
            StepCountsCalculation.cookieHelper.setCookie("https://app.jvbwellness.com/", cookieList.get(i).getName(), cookieList.get(i).getValue());
        }

        //Print Reponse
        Log.d("cookieList : ", cookieList + "");
        Log.d("Status Code : ", status + "");
        Log.d("Response Body: ", responseData);
        return responseData;
    }


    private void setHeaderbuild(HashMap<String, String> header) {
        //Default Header
        //header.put("ngo", "Phase-1");
        header.put("Accept", "application/json");
        header.put("Content-type", "application/json");

        this.headerbuild = Headers.of(header);
    }
}
