package com.bitslate.swish.SwishObjects;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 19/9/15.
 */
public class Bus {
    public String origin;
    public String DepartureTime;
    public String duration;
    public String destination;
    public String BusType;
    public String TravelsName;
    public BusFare fare;
    public String totalfare_db;

    public class BusFare{
        public String totalfare;
    }

    public String itemColor = "#ffffff";

    public void removeBus(final int trip_id) {
        final SwishPreferences prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/bus/remove";
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", String.valueOf(prefs.getUser().fb_id));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("plan_id", String.valueOf(trip_id));
                params.put("DepartureTime", DepartureTime);
                params.put("BusType", BusType);
                params.put("origin", origin);
                params.put("TravelsName", TravelsName);
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
