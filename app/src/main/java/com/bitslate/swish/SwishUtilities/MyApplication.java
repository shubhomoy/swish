package com.bitslate.swish.SwishUtilities;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;

/**
 * Created by shubhomoy on 18/9/15.
 */
public class MyApplication extends Application{
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
