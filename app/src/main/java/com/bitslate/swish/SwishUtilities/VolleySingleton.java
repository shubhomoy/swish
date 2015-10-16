package com.bitslate.swish.SwishUtilities;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by shubhomoy on 18/9/15.
 */
public class VolleySingleton {
    private static VolleySingleton instance = null;
    private RequestQueue requestQueue;
    private VolleySingleton() {
        requestQueue = Volley.newRequestQueue(MyApplication.getAppContext());
    }
    public static VolleySingleton getInstance() {
        if(instance == null) {
            instance = new VolleySingleton();
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
