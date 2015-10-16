package com.bitslate.swish;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.SwishAdapters.BusAdapter;
import com.bitslate.swish.SwishObjects.Bus;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 19/9/15.
 */
public class BusSearchResultActivity extends AppCompatActivity {

    RecyclerView searchListLv;
    ArrayList<Bus> searchList;
    BusAdapter adapter;
    Toolbar toolbar;
    SwishDatabase dbAdapter;
    Intent intent;
    public static FloatingActionButton done;
    public static Animation animationZoomOut;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    void instantiate() {
        intent = getIntent();
        dbAdapter = new SwishDatabase(this);
        done = (FloatingActionButton) findViewById(R.id.done);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bus list");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchListLv = (RecyclerView) findViewById(R.id.recyclerview);
        searchListLv.setHasFixedSize(true);
        searchListLv.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<Bus>();
        adapter = new BusAdapter(this, searchList, intent.getStringExtra("what"));
        searchListLv.setAdapter(adapter);
        animationZoomOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
        prefs = new SwishPreferences(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Bus");
    }

    void loadList() {
        if (intent.getStringExtra("what").equals("plan")) {
            dbAdapter.open();
            searchList = dbAdapter.findBuses(prefs.getTripId(), searchList);
            adapter.notifyDataSetChanged();
            dbAdapter.close();
            fetchRemote(prefs.getTripId());
        } else {
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData/bussearch.txt");
            String respponse = "", current;
            BufferedReader br = null;
            try {
                FileReader reader = new FileReader(file);
                br = new BufferedReader(reader);
                while ((current = br.readLine()) != null) {
                    respponse += current;
                }
            } catch (FileNotFoundException e) {
                Log.d("option", "Unable to read");
            } catch (IOException e) {

            }
            try {
                JSONObject jsonObject = new JSONObject(respponse);
                JSONObject data = new JSONObject(jsonObject.getString("data"));
                JSONArray onwardflights = new JSONArray(data.getString("onwardflights"));
                Gson gson = new Gson();
                for (int i = 0; i < onwardflights.length(); i++) {
                    Bus bus = gson.fromJson(onwardflights.get(i).toString(), Bus.class);
                    searchList.add(bus);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {

            }
        }
        if (searchList.size() == 0) {
            searchListLv.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search_result);
        instantiate();
        loadList();
        if(intent.getStringExtra("what").equals("plan"))
            done.setVisibility(View.GONE);
        try {
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bus bus = searchList.get(adapter.selected);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BusSearchResultActivity.this);
                    builder.setTitle("You have selected");
                    View v = LayoutInflater.from(BusSearchResultActivity.this).inflate(R.layout.bus_list_item, null);
                    TextView travelNameTv = (TextView) v.findViewById(R.id.travelsname_tv);
                    TextView sourceToDestTv = (TextView) v.findViewById(R.id.source_dest_tv);
                    TextView typeTv = (TextView) v.findViewById(R.id.type_tv);
                    TextView deptTimeTv = (TextView) v.findViewById(R.id.deptime_tv);
                    TextView priceTv = (TextView) v.findViewById(R.id.price_tv);
                    TextView durationTv = (TextView) v.findViewById(R.id.duration_tv);
                    TextView removeBtn = (TextView) v.findViewById(R.id.remove_btn);
                    removeBtn.setVisibility(View.GONE);

                    travelNameTv.setText(bus.TravelsName);
                    typeTv.setText(bus.BusType);
                    deptTimeTv.setText(bus.DepartureTime);
                    durationTv.setText(bus.duration);
                    priceTv.setText("Rs " + bus.fare.totalfare);
                    sourceToDestTv.setText(bus.origin + " to " + bus.destination);

                    builder.setView(v);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            insertRemoteBus(bus, prefs.getTripId());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            searchList.get(adapter.selected).itemColor = "#ffffff";
                            adapter.selected = -1;
                            BusSearchResultActivity.done.setAnimation(animationZoomOut);
                            BusSearchResultActivity.done.startAnimation(animationZoomOut);
                            adapter.notifyDataSetChanged();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                }
            });
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void fetchRemote(final int trip_id) {
        String url = Config.SWISH_API_URL+"/buses/"+trip_id+"/fetch";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllBusesOfTrip(trip_id);
                searchList.removeAll(searchList);
                searchList.clear();
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        Bus bus = gson.fromJson(jsonArray.getJSONObject(i).toString(), Bus.class);
                        dbAdapter.addNewBus(bus, prefs.getTripId());
                    }
                    searchList = dbAdapter.findBuses(prefs.getTripId(), searchList);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(BusSearchResultActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
                dbAdapter.close();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(BusSearchResultActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }


    void insertRemoteBus(final Bus bus, final int trip_id) {
        progressDialog.show();
        String url = Config.SWISH_API_URL+"/bus/new";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Bus item = gson.fromJson(jsonObject.getString("data"), Bus.class);
                    dbAdapter.open();
                    dbAdapter.addNewBus(item, prefs.getTripId());
                    dbAdapter.close();
                    if(SearchBusActivity.activity != null)
                        SearchBusActivity.activity.finish();
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(BusSearchResultActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }

                if (SearchFlightActivity.activity != null) {
                    SearchFlightActivity.activity.finish();
                }
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("option", error.toString());
                Toast.makeText(BusSearchResultActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
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
                params.put("DepartureTime", bus.DepartureTime);
                params.put("origin", bus.origin);
                params.put("destination", bus.destination);
                params.put("BusType", bus.BusType);
                params.put("TravelsName", bus.TravelsName);
                params.put("duration", bus.duration);
                params.put("totalfare_db", bus.fare.totalfare);
                params.put("plan_id", String.valueOf(trip_id));
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
