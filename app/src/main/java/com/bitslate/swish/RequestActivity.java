package com.bitslate.swish;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishAdapters.RequestAdapter;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class RequestActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    TextView noResult;
    LinearLayoutManager linearLayoutManager;
    SwishPreferences prefs;
    FloatingActionButton add;
    ArrayList<PlanItem> list;
    RequestAdapter adapter;
    TextView text;

    void instantiate() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trip Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        noResult = (TextView)findViewById(R.id.no_result_tv);
        text = (TextView)findViewById(R.id.text);
        text.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
        prefs = new SwishPreferences(this);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        progressDialog.setMessage("Fetching requests");
        progressDialog.show();
        add = (FloatingActionButton)findViewById(R.id.add);
        add.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list = new ArrayList<PlanItem>();
        adapter = new RequestAdapter(this, list);
        recyclerView.setAdapter(adapter);
        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        instantiate();
        loadRequests();
    }

    void loadRequests() {
        String url = Config.SWISH_API_URL+"/requests";
        SwishRequest swishRequest = new SwishRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        PlanItem planItem = gson.fromJson(jsonArray.getJSONObject(i).toString(), PlanItem.class);
                        if(planItem.creator.id == prefs.getUser().id)continue;
                        list.add(planItem);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(RequestActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("option", error.toString());
                Toast.makeText(RequestActivity.this, "Connection Timeout", Toast.LENGTH_LONG).show();
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
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
