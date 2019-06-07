package com.samplereact.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import static com.facebook.react.views.textinput.ReactTextInputManager.TAG;
import static java.lang.annotation.ElementType.PACKAGE;

@SuppressWarnings("deprecation")
public class CheckNetworkConnection extends BroadcastReceiver {

    Context context;
    private boolean isConnected = false;
    private String PACKAGE;

    public CheckNetworkConnection(Context context, boolean isConnected, String PACKAGE) {
        this.context = context;
        this.isConnected = isConnected;
        this.PACKAGE = PACKAGE;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.v(TAG, "Receieved notification about network status");
        this.context = context;
        isNetworkAvailable(context);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if(!isConnected){
                            isConnected = true;
                            PackageManager pm = context.getPackageManager();
                            boolean app = isPackageInstalled(PACKAGE, pm);
                            if(app){
                                Log.d(TAG, " method return TURE");
                            }else {
                                Log.d(TAG, " method return FALSER");
                            }
                        }
                        return isConnected;
                    }
                }
            }
        }else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            isConnected = false;
        }
        return isConnected;
    }

    //to check if gogogle fit  app is instelled
    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;
        try {
            packageManager.getPackageInfo(packageName, 0);
//            StepCountCheck();
            Log.e(TAG, " app found in mobile");
        } catch (PackageManager.NameNotFoundException e) {
            GoogleFitInstallDialog();
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            found = false;
            Log.e(TAG, " app not found in mobile ----> ");
        }
        return found;
    }

    public void GoogleFitInstallDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Google fit data not found, Please install Google fit App first and try again.");
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
