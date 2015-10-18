package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishAdapters.SuggestAdapter;
import com.bitslate.swish.SwishObjects.User;
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
 * Created by shubhomoy on 18/10/15.
 */
public class FriendListActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    LinearLayoutManager linearLayoutManager;
    ArrayList<User> list;
    SwishPreferences prefs;
    FloatingActionButton add;
    TextView text;
    SuggestAdapter adapter;

    void instantiate() {
        text = (TextView)findViewById(R.id.text);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        add = (FloatingActionButton)findViewById(R.id.add);
        add.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Suggest to");
        progressDialog = new ProgressDialog(this);
        prefs = new SwishPreferences(this);
        progressDialog.setMessage("Please wait");
        list = new ArrayList<User>();
        adapter = new SuggestAdapter(this, list, getIntent().getStringExtra("hotel_id"));
        recyclerView.setAdapter(adapter);
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
        progressDialog.show();
        String url = Config.SWISH_API_URL + "/getAllFriends";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject =  new JSONObject(response.getString("data"));
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));
                    if (jsonArray.length() > 0) {
                        list.removeAll(list);
                        list.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            User user = gson.fromJson(jsonArray.getJSONObject(i).toString(), User.class);
                            //Log.d("option", users.getJSONObject(i).toString());
                            list.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(FriendListActivity.this, "There are no plans", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(FriendListActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                progressDialog.dismiss();

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
