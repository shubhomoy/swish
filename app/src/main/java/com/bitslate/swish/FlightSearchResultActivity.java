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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.SwishAdapters.FlightAdapter;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.bumptech.glide.Glide;
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
 * Created by shubhomoy on 18/9/15.
 */
public class FlightSearchResultActivity extends AppCompatActivity {

    RecyclerView searchListLv;
    FlightAdapter adapter;
    ArrayList<Flight> searchList;
    Toolbar toolbar;
    public static FloatingActionButton done;
    public static Animation animationZoomOut;
    SwishDatabase dbAdapter;
    Intent intent;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    void instantiate() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding flight");
        intent = getIntent();
        dbAdapter = new SwishDatabase(this);
        done = (FloatingActionButton)findViewById(R.id.done);
        toolbar= (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Flight list");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchListLv = (RecyclerView)findViewById(R.id.recyclerview);
        searchListLv.setHasFixedSize(true);
        searchListLv.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<Flight>();
        adapter = new FlightAdapter(this, searchList, intent.getStringExtra("what"));
        searchListLv.setAdapter(adapter);
        animationZoomOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
        prefs = new SwishPreferences(this);
    }



    void loadList() {
        if(intent.getStringExtra("what").equals("plan")) {
            dbAdapter.open();
            searchList = dbAdapter.findFlights(prefs.getTripId(), searchList);
            adapter.notifyDataSetChanged();
            dbAdapter.close();
        }else{
            File file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/flightsearch.txt");
            String respponse = "", current;
            BufferedReader br = null;
            try {
                FileReader reader = new FileReader(file);
                br = new BufferedReader(reader);
                while((current = br.readLine()) != null) {
                    respponse+=current;
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
                for(int i=0; i<onwardflights.length(); i++) {
                    Flight flight = gson.fromJson(onwardflights.get(i).toString(), Flight.class);
                    flight.timestamp = intent.getStringExtra("date");
                    searchList.add(flight);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {

            }
        }
        if(searchList.size()==0) {
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

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    final Flight flight = searchList.get(adapter.selected);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(FlightSearchResultActivity.this);
                    builder.setTitle("You have selected");
                    View v = LayoutInflater.from(FlightSearchResultActivity.this).inflate(R.layout.flight_list_item, null);
                    TextView airlineTv = (TextView)v.findViewById(R.id.airline_tv);
                    TextView sourceToDestTv = (TextView)v.findViewById(R.id.source_dest_tv);
                    TextView arrTimeTv = (TextView)v.findViewById(R.id.arrtime_tv);
                    TextView deptTimeTv = (TextView)v.findViewById(R.id.deptime_tv);
                    TextView priceTv = (TextView)v.findViewById(R.id.price_tv);
                    TextView durationTv = (TextView)v.findViewById(R.id.duration_tv);
                    TextView removeBtn = (TextView)v.findViewById(R.id.remove_btn);
                    TextView departure = (TextView)v.findViewById(R.id.departure_date_tv);
                    ImageView logo = (ImageView)v.findViewById(R.id.airline_logo);
                    if(flight.airline.equals("Air India")) {
                        Glide.with(FlightSearchResultActivity.this).load(Config.AIRINDIA_LOGO_URL).into(logo);
                    }else if(flight.airline.equals("Vistara")){
                        Glide.with(FlightSearchResultActivity.this).load(Config.VISTARA_LOGO_URL).into(logo);
                    }else if(flight.airline.equals("Jet Airways")){
                        Glide.with(FlightSearchResultActivity.this).load(Config.JETAIRWAYS_LOGO_URL).into(logo);
                    }else if(flight.airline.equals("spicejet")){
                        Glide.with(FlightSearchResultActivity.this).load(Config.SPICEJET_LOGO_URL).into(logo);
                    }else if(flight.airline.equals("IndiGo")){
                        Glide.with(FlightSearchResultActivity.this).load(Config.INDIGO_LOGO_URL).into(logo);
                    }else if(flight.airline.equals("goair")){
                        Glide.with(FlightSearchResultActivity.this).load(Config.GOAIR_LOGO_URL).into(logo);
                    }

                    removeBtn.setVisibility(View.GONE);

                    departure.setText(intent.getStringExtra("date"));
                    airlineTv.setText(flight.airline);
                    arrTimeTv.setText(flight.arrtime);
                    deptTimeTv.setText(flight.deptime);
                    durationTv.setText(flight.duration);
                    priceTv.setText("Rs "+flight.fare.totalfare);
                    if(flight.stops.equals("0") || flight.stops.isEmpty()){
                        sourceToDestTv.setText(flight.origin+" to "+flight.destination + " (0 stops)");
                    }else if(flight.onwardflights!=null) {
                        sourceToDestTv.setText(flight.origin+" to "+flight.onwardflights.get(flight.onwardflights.size()-1).destination + " ("+flight.stops+" stops)");
                    }
                    builder.setView(v);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            insertRemoteFlight(flight, prefs.getTripId());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            searchList.get(adapter.selected).itemColor = "#ffffff";
                            adapter.selected = -1;
                            FlightSearchResultActivity.done.setAnimation(animationZoomOut);
                            FlightSearchResultActivity.done.startAnimation(animationZoomOut);
                            adapter.notifyDataSetChanged();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                }catch (ArrayIndexOutOfBoundsException e) {

                }
            }
        });
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

    void insertRemoteFlight(final Flight flight, final int trip_id) {
        progressDialog.show();
        String url = Config.SWISH_API_URL+"/flight/new";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Flight item = gson.fromJson(jsonObject.getString("data"), Flight.class);
                    dbAdapter.open();
                    dbAdapter.addNewFlight(item, prefs.getTripId());
                    dbAdapter.close();
                } catch (JSONException e) {
                    Toast.makeText(FlightSearchResultActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
                Toast.makeText(FlightSearchResultActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
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
                params.put("timestamp", flight.timestamp);
                if(flight.stops.equals("0") || flight.stops.isEmpty()){
                    params.put("origin", flight.origin);
                    params.put("destination", flight.destination);
                }else if(flight.onwardflights!=null) {
                    params.put("origin", flight.origin);
                    params.put("destination", flight.onwardflights.get(flight.onwardflights.size()-1).destination);
                }
                params.put("deptime", flight.deptime);
                params.put("arrtime", flight.arrtime);
                params.put("duration", flight.duration);
                params.put("flightno", flight.flightno);
                params.put("seatingclass", flight.seatingclass);
                params.put("stops", flight.stops);
                params.put("airline", flight.airline);
                params.put("totalfare_db", flight.fare.totalfare);
                params.put("plan_id", String.valueOf(trip_id));
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
