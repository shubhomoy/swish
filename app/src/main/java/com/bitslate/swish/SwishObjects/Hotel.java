package com.bitslate.swish.SwishObjects;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.MyApplication;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by shubhomoy on 22/9/15.
 */
public class Hotel implements Serializable{
    public String name;
    public float rating;
    public ArrayList<Image> img;
    public Facility facilities;
    public Contact contact;
    public Location loc;

    public String op;
    public String mp;
    public String discount;

    public String itemColor = "#ffffff";
    public class Facility {
        public ArrayList<String> all;
    }

    public class Image {
        public String l;
    }

    public class Contact {

        public ArrayList<String> web;

        public ArrayList<String> email;

        public ArrayList<String> phone;

    }

    public class Location {
        public String full;
        public String pin;
        public String state;
        public String location;
        public String lon;
        public String lat;
    }

    public void remove(int trip_id) {
        String url = Config.SWISH_API_URL+"/removehotel?plan_id="+trip_id;
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("option", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
            }
        }, MyApplication.getAppContext());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }
}
