package com.bitslate.swish.SwishUtilities;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class SwishRequest extends JsonObjectRequest {

    SwishPreferences prefs;

    public SwishRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, Context context) {
        super(method, url, jsonRequest, listener, errorListener);
        prefs = new SwishPreferences(context);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("accessToken", prefs.getAccessToken());
        headers.put("id", prefs.getUser().fb_id);
        return headers;
    }
}
