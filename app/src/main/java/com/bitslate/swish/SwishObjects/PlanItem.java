package com.bitslate.swish.SwishObjects;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bitslate.swish.SwishAdapters.RequestAdapter;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.MyApplication;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class PlanItem {
    public int id;
    public String name;
    public String created_at;
    public int user_id;
    public Pivot pivot;
    public User creator;

    public class Pivot{
        public int user_id;
        public int plan_id;
        public int status = 0;
    }

    public void accept(final int friend_id, final RequestAdapter adapter){
        final SwishPreferences prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/request/"+id+"/accept";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pivot.status = 1;
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", "Cant accept");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", String.valueOf(friend_id));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", prefs.getUser().fb_id);
                return headers;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    public void decline(final int friend_id, final RequestAdapter adapter){
        final SwishPreferences prefs = new SwishPreferences(MyApplication.getAppContext());
        String url = Config.SWISH_API_URL+"/request/"+id+"/decline";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pivot.status = -1;
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", "Cant decline");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", String.valueOf(friend_id));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", prefs.getUser().fb_id);
                return headers;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
