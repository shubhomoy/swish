package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishObjects.Bus;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class PreviewActivity extends AppCompatActivity {

    Intent intent;
    SwishDatabase dbAdapter;
    String itinerary;
    TextView flightsTv;
    TextView busesTv;
    TextView hotelTv;
    Toolbar toolbar;
    TextView dateTv;
    SwishPreferences prefs;
    com.getbase.floatingactionbutton.FloatingActionButton addFlight, addBus, addHotel;
    FloatingActionsMenu addBtn;
    ProgressDialog progressDialog;

    int loadedEntities = 0;

    void instantiate() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your itinerary Plan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();
        dbAdapter = new SwishDatabase(this);
        flightsTv = (TextView)findViewById(R.id.flights_tv);
        dateTv = (TextView)findViewById(R.id.date_tv);
        busesTv = (TextView)findViewById(R.id.buses_tv);
        hotelTv = (TextView)findViewById(R.id.hotels_tv);
        prefs = new SwishPreferences(this);
        addFlight = (FloatingActionButton)findViewById(R.id.add_flight);
        addBus = (FloatingActionButton)findViewById(R.id.add_bus);
        addBtn = (FloatingActionsMenu)findViewById(R.id.add_btn);
        addHotel = (FloatingActionButton)findViewById(R.id.add_hotel);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching trip plan");
        progressDialog.show();
    }

    void loadList() {
        dbAdapter.open();
        PlanItem planItem = dbAdapter.findItinery(prefs.getTripId());
        if(planItem!=null){
            dateTv.setText(planItem.name);
        }
        ArrayList<Flight> list = dbAdapter.findFlights(prefs.getTripId(), new ArrayList<Flight>());
        flightsTv.setText("Flights (" + list.size() + ")");
        ArrayList<Bus> list2 = dbAdapter.findBuses(prefs.getTripId(), new ArrayList<Bus>());
        busesTv.setText("Buses (" + list2.size() + ")");
        dbAdapter.close();
        try {
            FileReader reader = new FileReader(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
            hotelTv.setText("Hotels (1)");
        } catch (FileNotFoundException e) {

        }
    }

    void dismissProgressDialog() {
        if(loadedEntities>=3){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        instantiate();

        loadList();

        fetchHotel();
        fetchRemoteFlights();
        fetchRemoteBuses();

        flightsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PreviewActivity.this, FlightSearchResultActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });
        busesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PreviewActivity.this, BusSearchResultActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });
        hotelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PreviewActivity.this, HotelSearchResultActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });

        addFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                Intent newIntent = new Intent(PreviewActivity.this, SearchFlightActivity.class);
                startActivityForResult(newIntent, 0);
            }
        });

        addBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                Intent newIntent = new Intent(PreviewActivity.this, SearchBusActivity.class);
                startActivityForResult(newIntent, 0);
            }
        });

        addHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                startActivityForResult(new Intent(PreviewActivity.this, SearchHotelActivity.class), 0);
            }
        });
    }

    void fetchHotel() {
        String url = Config.CORE_URL+"/storage/swish/"+prefs.getTripId()+"/hotel.txt";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FileWriter writer = null;
                File dir = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId());
                dir.mkdirs();
                try {
                    writer = new FileWriter(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
                    writer.write(response.toString());
                    writer.flush();
                    writer.close();
                    hotelTv.setText("Hotels (1)");
                } catch (IOException e) {
                    Log.d("option", "unable to write file");
                    hotelTv.setText("Hotels (0)");
                }
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                File file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
                if(file.exists())
                    file.delete();
                file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId());
                if(file.isDirectory())
                    file.delete();
                hotelTv.setText("Hotels (0)");
                loadedEntities++;
                dismissProgressDialog();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    void fetchRemoteFlights() {
        String url = Config.SWISH_API_URL+"/flights/"+prefs.getTripId()+"/fetch";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllFlightsOfTrip(prefs.getTripId());
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        Flight flight = gson.fromJson(jsonArray.getJSONObject(i).toString(), Flight.class);
                        dbAdapter.addNewFlight(flight, prefs.getTripId());
                    }
                    ArrayList<Flight> list = dbAdapter.findFlights(prefs.getTripId(), new ArrayList<Flight>());
                    flightsTv.setText("Flights (" + list.size() + ")");
                } catch (JSONException e) {
                    Toast.makeText(PreviewActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
                dbAdapter.close();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(PreviewActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    void fetchRemoteBuses() {
        String url = Config.SWISH_API_URL+"/buses/"+prefs.getTripId()+"/fetch";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllBusesOfTrip(prefs.getTripId());
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        Bus bus = gson.fromJson(jsonArray.getJSONObject(i).toString(), Bus.class);
                        dbAdapter.addNewBus(bus, prefs.getTripId());
                    }
                    ArrayList<Bus> list2 = dbAdapter.findBuses(prefs.getTripId(), new ArrayList<Bus>());
                    busesTv.setText("Buses (" + list2.size() + ")");
                } catch (JSONException e) {
                    Toast.makeText(PreviewActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
                dbAdapter.close();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(PreviewActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_plan_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_friends:
                Intent intent = new Intent(this, FriendsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(addBtn.isExpanded())
            addBtn.collapse();
    }
}
