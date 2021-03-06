package com.bitslate.swish.SwishObjects;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.MyApplication;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.VolleySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class User {
    public int id;
    public String fb_id;
    public String email;
    public String fname;
    public String lname;
    public ArrayList<PlanItem> plans;
    public PlanItem.Pivot pivot;
    public ArrayList<Suggestions> suggestions;
    SwishPreferences prefs;

    public class Suggestions{
        public String hotel_id;
    }

    public void addPlan(final int planId) {
        prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/friend/add";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("option", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("friend_id", String .valueOf(id));
                params.put("plan_id", String.valueOf(planId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", String.valueOf(prefs.getUser().fb_id));
                return headers;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    public void removePlan(final int planId) {
        prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/friend/remove";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("option", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("friend_id", String .valueOf(id));
                params.put("plan_id", String.valueOf(planId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", String.valueOf(prefs.getUser().fb_id));
                return headers;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
