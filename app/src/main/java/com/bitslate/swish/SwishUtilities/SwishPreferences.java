package com.bitslate.swish.SwishUtilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.bitslate.swish.SwishObjects.User;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class SwishPreferences {
    Context context;
    SharedPreferences prefs;
    public SwishPreferences(Context c) {
        this.context = c;
        prefs = context.getSharedPreferences("Swish", Context.MODE_PRIVATE);
    }

    public void insertTripId(int id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("trip_id", id);
        editor.commit();
    }

    public int getTripId(){return prefs.getInt("trip_id", 0);}

    public void setInitialization(boolean b) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_time", b);
        editor.commit();
    }

    public void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", accessToken);
        editor.commit();
    }

    public String getAccessToken() {return prefs.getString("access_token", null);}
    public void setUser(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("id", user.id);
        editor.putString("fb_id", user.fb_id);
        editor.putString("email", user.email);
        editor.putString("fname", user.fname);
        editor.putString("lname", user.lname);
        editor.commit();
    }
    public User getUser() {
        User user = new User();
        user.id = prefs.getInt("id", 0);
        user.email = prefs.getString("email", null);
        user.fname = prefs.getString("fname", null);
        user.lname = prefs.getString("lname", null);
        user.fb_id = prefs.getString("fb_id", null);
        return user;
    }
    public boolean getInitialization(){return prefs.getBoolean("first_time", false);}
}
