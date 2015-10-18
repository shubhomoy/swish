package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.SwishAdapters.ItineraryPlanAdapter;
import com.bitslate.swish.SwishObjects.PlanItem;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlanListActivity extends AppCompatActivity {

    FloatingActionButton newPlanBtn;
    Toolbar toolbar;
    SwishDatabase dbAdapter;
    ArrayList<PlanItem> list;
    RecyclerView recyclerView;
    ItineraryPlanAdapter adapter;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    void instantiate() {
        progressDialog = new ProgressDialog(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Swish");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newPlanBtn = (FloatingActionButton) findViewById(R.id.new_plan_btn);
        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Config.ITINERARY = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        dbAdapter = new SwishDatabase(this);
        list = new ArrayList<PlanItem>();
        adapter = new ItineraryPlanAdapter(this, list);
        recyclerView.setAdapter(adapter);
        prefs = new SwishPreferences(this);
        if(!prefs.getInitialization()) {
            new LoadCities().execute();
        }
    }

    class LoadCities extends AsyncTask<Void, String, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PlanListActivity.this);
            progressDialog.setMessage("Initializing cities. This may take a few minutes.");
            progressDialog.setCancelable(false);
            progressDialog.show();
            dbAdapter.open();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("city_list.csv")));
                String response;
                int i = 0;

                while((response = reader.readLine())!=null){
                    if(i!=0) {
                        String[] items = response.split(",");
                        items[0] = items[0].substring(1, items[0].length()-1);
                        items[1] = items[1].substring(1, items[1].length() - 1);
                        dbAdapter.insertCity(items[0], items[1]);
                    }
                    i++;
                }

            } catch (IOException e) {
                Log.d("option", "unable");
            }finally {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            prefs.setInitialization(true);
            dbAdapter.close();
            loadList();
            loadRemotePlans();
        }
    }

    void loadList() {
        list.removeAll(list);
        list.clear();
        dbAdapter.open();
        list = dbAdapter.getAllPlans(list);
        dbAdapter.close();
        adapter.notifyDataSetChanged();

        if (list.size() == 0)
            recyclerView.setVisibility(View.GONE);
        else
            recyclerView.setVisibility(View.VISIBLE);
    }

    void loadRemotePlans() {
        String url = Config.SWISH_API_URL+"/plans";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllPlans();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        PlanItem planItem = gson.fromJson(jsonArray.getJSONObject(i).toString(), PlanItem.class);
                        try{
                            if(planItem.pivot.status == 0 || planItem.pivot.status == -1)continue;
                        }catch (NullPointerException e){

                        }
                        list.add(planItem);
                        dbAdapter.insertNewItinerary(planItem);
                    }
                } catch (JSONException e) {

                }
                dbAdapter.close();
                loadList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(PlanListActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);
        instantiate();
        if(prefs.getInitialization()) {
            loadList();
            loadRemotePlans();
        }
        newPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlanListActivity.this);
                View v = LayoutInflater.from(PlanListActivity.this).inflate(R.layout.new_plan_dialog, null);
                final EditText tripNameEt = (EditText) v.findViewById(R.id.trip_name_tv);
                tripNameEt.setHint("Trip on " + Config.ITINERARY);
                builder.setView(v);
                builder.setTitle("Trip name");
                builder.setMessage("Give a name to your new trip.");
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (tripNameEt.getText().toString().trim().length() > 0) {
                            createNewPlan(tripNameEt.getText().toString());
                        } else {
                            createNewPlan("Trip on " + Config.ITINERARY);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    void createNewPlan(final String planName) {
        progressDialog.setMessage("Creating trip");
        progressDialog.show();
        String url = Config.SWISH_API_URL+"/plan/new";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                int id;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Gson gson = new Gson();
                    PlanItem item = gson.fromJson(jsonObject.getString("data"), PlanItem.class);
                    dbAdapter.open();
                    id = dbAdapter.insertNewItinerary(item);
                    dbAdapter.close();
                    loadList();
                    prefs.insertTripId(id);
                    Intent intent = new Intent(PlanListActivity.this, PreviewActivity.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    Toast.makeText(PlanListActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("option", error.toString());
                Toast.makeText(PlanListActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("name", planName);
                params.put("user_id", String.valueOf(prefs.getUser().id));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification:
                startActivityForResult(new Intent(PlanListActivity.this, RequestActivity.class), 1);
                break;
            case R.id.suggestions:
                startActivity(new Intent(this, ViewSuggestionsActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadRemotePlans();
    }
}
