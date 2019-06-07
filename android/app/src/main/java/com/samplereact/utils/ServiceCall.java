package com.samplereact.utils;

import android.util.Log;

import com.samplereact.functionality.ActivityFunctionality;
import com.samplereact.network.OkHttpNetworkServiceImplementation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import static com.facebook.react.views.textinput.ReactTextInputManager.TAG;
import static com.samplereact.functionality.ActivityFunctionality.activity_jsonform;
import static com.samplereact.functionality.ActivityFunctionality.summary_id;
import static com.samplereact.functionality.StepCountsCalculation.formattedDate;
import static com.samplereact.functionality.StepCountsCalculation.steps_jsonform;

public class ServiceCall {

    private OkHttpNetworkServiceImplementation okHttpNetworkServiceImplementation = OkHttpNetworkServiceImplementation.getInstance();

    public void makenetworkcall (){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", "Jvbapple");
            jsonObject.put("password", "health2112");
            String info = okHttpNetworkServiceImplementation.loginRequest("https://app.jvbwellness.com/api/users/login/", jsonObject.toString(), new HashMap<String, String>());
            Log.e(TAG, " login response: " + info);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("user", "402");
            jsonObject2.put("belong_to", formattedDate);
            jsonObject2.put("data", steps_jsonform);
            jsonObject2.put("summary_id", summary_id);
            String infor1 = okHttpNetworkServiceImplementation.protocolRequest("https://app.jvbwellness.com/apple/users/datasteps", jsonObject2.toString(), new HashMap<String, String>());
            Log.e(TAG, "steps data sending response: " + infor1);

            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("user", "402");
            jsonObject3.put("belong_to", formattedDate);
            jsonObject3.put("data", activity_jsonform);
            jsonObject3.put("summary_id", summary_id);
            String infor2 = okHttpNetworkServiceImplementation.protocolRequestActivity("https://app.jvbwellness.com/apple/users/dataactivities", jsonObject3.toString(), new HashMap<String, String>());
            Log.e(TAG, "activity data sending response: " + infor2);


        } catch (
                JSONException j) {
            j.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
    }


