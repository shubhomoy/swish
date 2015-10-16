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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 18/9/15.
 */
public class Flight {
    public String timestamp;
    public String origin;
    public String destination;
    public String deptime;
    public String arrtime;
    public String duration;
    public String flightno;;
    public String seatingclass;
    public String stops;
    public String seatsavailable;
    public String airline;
    public FlightPrice PricingSolution;
    public FlightFare fare;
    public ArrayList<Flight> onwardflights;
    public String totalfare_db;

    public class FlightPrice {
        public String TotalPrice;
    }
    public class FlightFare{
        public String totalfare;
    }

    public String itemColor = "#ffffff";

    public void removeFlight(final int trip_id) {
        final SwishPreferences prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/flight/remove";
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
                params.put("airline", airline);
                params.put("flightno", flightno);
                params.put("origin", origin);
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
