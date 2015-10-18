package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitslate.swish.SwishAdapters.HotelAdapter;
import com.bitslate.swish.SwishAdapters.HotelSuggestionAdapter;
import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by shubhomoy on 18/10/15.
 */
public class ViewSuggestionsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;
    ArrayList<Hotel> list;
    HotelSuggestionAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    public static FloatingActionButton done;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    void instantiate() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hotel Suggestions");
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<Hotel>();
        done = (FloatingActionButton)findViewById(R.id.done);
        done.setVisibility(View.GONE);
        adapter = new HotelSuggestionAdapter(this, list);
        recyclerView.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefs = new SwishPreferences(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search_result);
        instantiate();

        load();
    }

    void load() {
        String url = Config.SWISH_API_URL+"/getAllSuggestions";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                try {
                    User user = gson.fromJson(response.getString("data"), User.class);
                    String ids = "%5B";
                    int i;
                    for(i=0; i<user.suggestions.size()-1; i++) {
                        ids += user.suggestions.get(i).hotel_id+"%2C+";
                    }
                    ids += user.suggestions.get(i).hotel_id+"%5D";
                    loadHotels(ids);
                } catch (JSONException e) {
                    Toast.makeText(ViewSuggestionsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewSuggestionsActivity.this, "Connection timeout", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    void loadHotels(String ids) {
        Log.d("option", ids);
        String url = "http://developer.goibibo.com/api/voyager/?app_id="+ Config.GOIBIBI_APP_ID+"&app_key="+Config.GOIBIBO_APP_KEY+"&method=hotels.get_hotels_data&id_list="+ids+"&id_type=_id";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("option", response.toString());
                        try {
                            JSONObject data = new JSONObject(response.getString("data"));
                            Gson gson = new Gson();
                            Iterator<String> iterator = data.keys();
                            while(iterator.hasNext()) {
                                String hotel_id = iterator.next();
                                JSONObject hotelData = new JSONObject(data.getString(hotel_id));
                                Hotel hotel = gson.fromJson(hotelData.getString("hotel_data_node"), Hotel.class);
                                JSONObject meta = new JSONObject(hotelData.getString("hotel_geo_node"));
                                JSONObject location = new JSONObject(meta.getString("location"));
                                hotel.loc.lat = location.getString("lat");
                                hotel.loc.lon = location.getString("long");
                                list.add(hotel);
                                Log.d("option", hotel.name);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.d("option", "ex");
                        }
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                progressDialog.dismiss();
            }
        });
        VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
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
}
