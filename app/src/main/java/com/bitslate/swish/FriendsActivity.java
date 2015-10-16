package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.SwishAdapters.FbUserAdapter;
import com.bitslate.swish.SwishAdapters.FriendsAdapter;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class FriendsActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    TextView noResult;
    LinearLayoutManager linearLayoutManager;
    ArrayList<User> list;
    FriendsAdapter adapter;
    SwishPreferences prefs;
    FloatingActionButton add;
    TextView text;

    void instantiate() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends Status");
        progressDialog = new ProgressDialog(this);
        noResult = (TextView) findViewById(R.id.no_result_tv);
        noResult.setVisibility(View.GONE);
        text = (TextView) findViewById(R.id.text);
        text.setText("Friends who are joining in this trip");
        prefs = new SwishPreferences(this);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        progressDialog.setMessage("Please wait");
        list = new ArrayList<User>();
        adapter = new FriendsAdapter(this, list, prefs.getTripId());
        recyclerView.setAdapter(adapter);
        add = (FloatingActionButton) findViewById(R.id.add);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        instantiate();
        loadFriends();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(FriendsActivity.this, AddFriendActivity.class), 1);
            }
        });
    }

    void loadFriends() {
        progressDialog.show();
        String url = Config.SWISH_API_URL + "/friends/status?plan_id=" + prefs.getTripId();
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    if (jsonArray.length() > 0) {
                        JSONObject result = jsonArray.getJSONObject(0);
                        JSONArray users = new JSONArray(result.getString("users"));
                        list.removeAll(list);
                        list.clear();
                        for (int i = 0; i < users.length(); i++) {
                            User user = gson.fromJson(users.getJSONObject(i).toString(), User.class);
                            Log.d("option", users.getJSONObject(i).toString());
                            list.add(user);
                        }
                        adapter.notifyDataSetChanged();
                        if (list.size() == 0) {
                            noResult.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noResult.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(FriendsActivity.this, "There are no plans", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(FriendsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    if (list.size() == 0) {
                        noResult.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                progressDialog.dismiss();
                if (list.size() == 0) {
                    noResult.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }, this);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.help:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(LayoutInflater.from(this).inflate(R.layout.status_help, null));
                builder.setTitle("Help");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadFriends();

    }
}
