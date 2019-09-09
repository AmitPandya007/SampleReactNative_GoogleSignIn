package com.samplereact.nativeSection;

import android.app.Activity;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.samplereact.functionality.Steps_Activity_Calculation;
import com.samplereact.network.CheckNetworkConnection;

public class NativeModule_GoogleFit extends ReactContextBaseJavaModule implements
        ActivityEventListener,
        LifecycleObserver,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_OAUTH = 1;
    public static Boolean isOn = false;
    private static String TAG = "NativeModule: ";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    private String GOOGLE_FIT_PACKAGE = "com.google.android.apps.fitness";

    public NativeModule_GoogleFit(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public void initialize() {
        super.initialize();
        CheckNetworkConnection networkConnection = new CheckNetworkConnection(getReactApplicationContext(),
                false, GOOGLE_FIT_PACKAGE);
        if (networkConnection.isNetworkAvailable(getReactApplicationContext())) {
            mApiClient = googleFitBuild(this, this, getReactApplicationContext());
        } else {
            Toast.makeText(getReactApplicationContext(), "No Internet. Please check your Internet.", Toast.LENGTH_SHORT).show();
        }
    }

    @ReactMethod
    public void getStatus(
            Callback successCallback) {
        successCallback.invoke(null, isOn);
    }

    @ReactMethod
    public void turnOn() {
        isOn = true;
        System.out.println("NativeModule_GoogleFit is turn ON");
        CheckNetworkConnection networkConnection = new CheckNetworkConnection(getReactApplicationContext(),
                false, GOOGLE_FIT_PACKAGE);
        if (networkConnection.isNetworkAvailable(getReactApplicationContext())) {
            mApiClient = googleFitBuild(this, this, getReactApplicationContext());
            googleFitConnect(getCurrentActivity(), mApiClient);
        } else {
            Toast.makeText(getReactApplicationContext(), "No Internet. Please check your Internet.", Toast.LENGTH_SHORT).show();
        }
    }

    private GoogleApiClient googleFitBuild
            (GoogleApiClient.ConnectionCallbacks connectionCallbacks,
             GoogleApiClient.OnConnectionFailedListener failedListener,
             Context applicationContext) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .requestScopes(new Scope(Scopes.FITNESS_LOCATION_READ))
                .requestScopes(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .build();

        return new GoogleApiClient.Builder(applicationContext)
                //without GOOGLE_SIGN_IN_API, RESULT_CANCELED is always the output
                //The new version of google Fit requires that the user authenticates with gmail account
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SENSORS_API)
                .build();
    }

    //runs an automated Google Fit connect sequence
    private void googleFitConnect(final Activity activity, final GoogleApiClient mGoogleApiClient) {
        Log.d(TAG, "google fit connect called");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.d(TAG, "Google API connected");
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    activity.startActivityForResult(signInIntent, 1);

                    Steps_Activity_Calculation stepCounts = new Steps_Activity_Calculation(mApiClient, getReactApplicationContext());
                    stepCounts.readStepountData();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }

    @ReactMethod
    public void turnOff() {
        isOn = false;
        System.out.println("NativeModule_GoogleFit is turn OFF");
    }

    @Override
    public String getName() {
        return "NativeModule_GoogleFit";
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, " Google client is connected. ");
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, " Google client connection is suspended due to code: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(getCurrentActivity(), REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "On connectionFailed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, " authInProgress");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String displayName = acct.getDisplayName() + "";
                Log.e(TAG, "google username  : " + displayName);
                //After You got your data add this to clear the priviously selected mail
                //mGoogleApiClient.clearDefaultAccountAndReconnect();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }


}