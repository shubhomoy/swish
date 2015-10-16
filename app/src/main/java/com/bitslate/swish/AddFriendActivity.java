package com.bitslate.swish;

import android.app.ProgressDialog;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishAdapters.FbUserAdapter;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class AddFriendActivity extends AppCompatActivity{

    Toolbar toolbar;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    TextView noResult;
    LinearLayoutManager linearLayoutManager;
    ArrayList<User> list;
    FbUserAdapter adapter;
    SwishPreferences prefs;
    FloatingActionButton add;
    TextView text;
    public static boolean selected = false;

    void instantiate() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add friends");
        progressDialog = new ProgressDialog(this);
        noResult = (TextView)findViewById(R.id.no_result_tv);
        text = (TextView)findViewById(R.id.text);
        text.setText("Add your facebook friends");
        noResult.setVisibility(View.GONE);
        prefs = new SwishPreferences(this);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        progressDialog.setMessage("Fetching friends");
        progressDialog.show();
        list = new ArrayList<User>();
        adapter = new FbUserAdapter(this, list, prefs.getTripId());
        recyclerView.setAdapter(adapter);
        add = (FloatingActionButton)findViewById(R.id.add);
        add.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        instantiate();
        loadFriends();
    }

    void loadFriends() {
        String url = Config.SWISH_API_URL+"/friends?plan_id="+prefs.getTripId();
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONObject object = new JSONObject(response.getString("data"));
                    JSONArray jsonArray = new JSONArray(object.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        User user = gson.fromJson(jsonArray.getString(i), User.class);
                        list.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    if(list.size() == 0){
                        noResult.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    if(list.size() == 0){
                        noResult.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    Toast.makeText(AddFriendActivity.this, "Connection timeout", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                if(list.size() == 0){
                    noResult.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                Log.d("option", error.toString());
            }
        }, this);
        swishRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
