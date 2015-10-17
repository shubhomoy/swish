package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishObjects.HotelPrice;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by shubhomoy on 22/9/15.
 */
public class HotelSearchResultActivity extends AppCompatActivity{


    RecyclerView recyclerView;
    Toolbar toolbar;
    ArrayList<Hotel> list;
    HotelAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    public static FloatingActionButton done;
    public static Animation animationZoomOut;
    SwishDatabase dbAdapter;
    SwishPreferences prefs;
    Intent intent;
    boolean allLoaded = false;
    ProgressDialog progressDialog;

    int i = 0, size =4;
    String hotel_id;
    void loadList() {
        if(intent.getStringExtra("what").equals("search")){
            String ids = "%5B";
            for(; i<SearchHotelActivity.hotelIds.size()-1 && i<size; i++) {
                ids += SearchHotelActivity.hotelIds.get(i)+"%2C+";
            }
            ids += SearchHotelActivity.hotelIds.get(i)+"%5D";
            String url = "http://developer.goibibo.com/api/voyager/?app_id="+ Config.GOIBIBI_APP_ID+"&app_key="+Config.GOIBIBO_APP_KEY+"&method=hotels.get_hotels_data&id_list="+ids+"&id_type=_id";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject data = new JSONObject(response.getString("data"));
                                Gson gson = new Gson();
                                Iterator<String> iterator = data.keys();
                                if(!iterator.hasNext()) {
                                    allLoaded = true;
                                }
                                while(iterator.hasNext()) {
                                    hotel_id = iterator.next();
                                    JSONObject hotelData = new JSONObject(data.getString(hotel_id));
                                    Hotel hotel = gson.fromJson(hotelData.getString("hotel_data_node"), Hotel.class);
                                    JSONObject meta = new JSONObject(hotelData.getString("hotel_geo_node"));
                                    JSONObject location = new JSONObject(meta.getString("location"));
                                    hotel.loc.lat = location.getString("lat");
                                    hotel.loc.lon = location.getString("long");
                                    list.add(hotel);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.d("option", "ex");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("option", error.toString());
                }
            });
            VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
        }
    }

    void instantiate() {
        intent = getIntent();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hotel list");
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<Hotel>();
        done = (FloatingActionButton)findViewById(R.id.done);
        adapter = new HotelAdapter(this, list, SearchHotelActivity.hotelPrice, intent.getStringExtra("what"));
        recyclerView.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        animationZoomOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
        dbAdapter = new SwishDatabase(this);
        prefs = new SwishPreferences(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding hotel");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search_result);
        instantiate();
        loadList();
        if(intent.getStringExtra("what").equals("plan"))
            done.setVisibility(View.GONE);

        if(intent.getStringExtra("what").equals("search")){
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (((linearLayoutManager.findLastVisibleItemPosition() + 1) >= linearLayoutManager.getItemCount()) && !allLoaded) {
                        size += 4;
                        loadList();
                    }
                }
            });
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.selected>-1){
                    Hotel hotel = list.get(adapter.selected);
                    HotelPrice price = SearchHotelActivity.hotelPrice.get(adapter.selected);
                    hotel.op = price.op;
                    hotel.mp = price.mp;
                    hotel.discount = String.valueOf(Float.parseFloat(hotel.op) - Float.parseFloat(hotel.mp));
                    Gson gson = new Gson();
                    String res = gson.toJson(hotel);
                    try {
                        File dir = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId());
                        if(!dir.exists())
                            dir.mkdir();
                        FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
                        writer.write(res);
                        writer.close();
                        new UploadFile().execute();
                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UploadFile extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String res = null;
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 60000);
            HttpConnectionParams.setSoTimeout(params, 60000);
            HttpClient client = new DefaultHttpClient(params);
            HttpPost post = new HttpPost(Config.CORE_URL + "/uploadhotel");
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("hotel", new FileBody(new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt")));
            try {
                entity.addPart("plan_id", new StringBody(String.valueOf(prefs.getTripId())));
                entity.addPart("filename", new StringBody("hotel.txt"));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            post.setEntity(entity);
            try {
                HttpResponse response = client.execute(post);
                res = EntityUtils.toString(response.getEntity());
                return res;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("option", s);
            if (progressDialog.isShowing()) progressDialog.dismiss();
            if(s!=null){
                if(SearchHotelActivity.activity!=null)
                    SearchHotelActivity.activity.finish();
                finish();
            }else{
                Toast.makeText(HotelSearchResultActivity.this,"Connection Timeout",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
