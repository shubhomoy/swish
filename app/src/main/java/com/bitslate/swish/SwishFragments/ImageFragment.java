package com.bitslate.swish.SwishFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishAdapters.ImageAdapter;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class ImageFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ImageAdapter adapter;
    ArrayList<String> list;
    SwishPreferences prefs;
    SwishDatabase database;
    TextView dateTv;

    void instantiate(View v) {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView)v.findViewById(R.id.recyclerview);
        dateTv = (TextView)v.findViewById(R.id.date_tv);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<String>();
        adapter = new ImageAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);
        prefs = new SwishPreferences(getActivity());
        database = new SwishDatabase(getActivity());
        database.open();
        PlanItem planItem = database.findItinery(prefs.getTripId());
        if (planItem != null) {
            dateTv.setText(planItem.name);
        }
        database.close();
    }

    void fetchImages() {
        String url = Config.SWISH_API_URL+"/images?plan_id="+prefs.getTripId();
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        list.add(Config.CORE_URL+"/storage/swish/"+prefs.getTripId()+"/"+obj.getString("image_name"));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection timeout", Toast.LENGTH_LONG).show();
            }
        }, getActivity());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.images_fragment, container, false);
        instantiate(v);
        fetchImages();
        return v;
    }
}
