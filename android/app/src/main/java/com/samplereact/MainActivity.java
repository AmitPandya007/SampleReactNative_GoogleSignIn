package com.samplereact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

public class MainActivity extends ReactActivity {

    private static final String TAG = MainActivity.class.getSimpleName() + "==> ";

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "SampleReact";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //To get google fit username
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    //Take all data You Want
                    //String identifier = acct.getId() + "";
                    // Save this JWT in global String :
                    String displayName = acct.getDisplayName() + "";
                    Log.e(TAG, "google username : " + displayName);
                    //After You got your data add this to clear the priviously selected mail
                    //mGoogleApiClient.clearDefaultAccountAndReconnect();
                }
            }
        }
    }
}