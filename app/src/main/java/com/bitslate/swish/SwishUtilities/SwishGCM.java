package com.bitslate.swish.SwishUtilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class SwishGCM {
    Context context;
    Activity activity;

    public SwishGCM(Context c){
        this.context=c;
        this.activity=(Activity)context;
    }

    private int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("Swish",
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt("app_version", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            SharedPreferences.Editor editor=prefs.edit();
            editor.putString(PROPERTY_REG_ID,"");
            editor.commit();
            return "";
        }
        return registrationId;
    }
    public static GoogleCloudMessaging gcm;
    public static String regid;
    public static String SENDER_ID = "992551267775";



    public class RegisterInBg extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regid = gcm.register(SENDER_ID);
                msg = "1";

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                Log.d("option", "REGISTERATION ID IS : " + regid);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the registration ID - no need to register again.
                SharedPreferences prefs = context.getSharedPreferences("Swish", Context.MODE_PRIVATE);
                int appVersion = getAppVersion(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_REG_ID, regid);
                editor.putInt("app_version", appVersion);
                editor.commit();
            } catch (IOException ex) {
                msg = "0";
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.isEmpty() || s.equals("0")){

            }
        }
    }
}
