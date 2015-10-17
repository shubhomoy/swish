package com.bitslate.swish;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitslate.swish.SwishAdapters.CityAdapter;
import com.bitslate.swish.SwishObjects.City;
import com.bitslate.swish.SwishObjects.HotelPrice;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class SearchHotelActivity extends AppCompatActivity {

    EditText cityEt;
    EditText checkinEt;
    EditText checkoutEt;
    ListView cityList;
    SwishDatabase dbAdapter;
    SwishPreferences prefs;
    CityAdapter adapter;
    ArrayList<City> list;
    Toolbar toolbar;
    String cityId = null;
    City city = null;
    int year, month, day;
    ProgressDialog progressDialog;
    Calendar calendar;
    String checindate = null, checkoutdate = null;

    public static ArrayList<String> hotelIds;
    public static ArrayList<HotelPrice> hotelPrice;

    public static Activity activity;
    boolean citySelected = false;
    boolean checkInSelected = false;
    boolean checkOutSelected = false;

    String hotel_ids;

    void instantiate() {
        activity = this;
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search hotels");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cityEt = (EditText)findViewById(R.id.city_et);
        checkinEt = (EditText)findViewById(R.id.check_in_et);
        checkoutEt = (EditText)findViewById(R.id.check_out_et);
        cityList = (ListView) findViewById(R.id.city_list);
        dbAdapter = new SwishDatabase(this);
        prefs = new SwishPreferences(this);
        list = new ArrayList<City>();
        adapter = new CityAdapter(this, list);
        cityList.setAdapter(adapter);
        progressDialog = new ProgressDialog(SearchHotelActivity.this);
        progressDialog.setMessage("Searching hotels");
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hotelIds = new ArrayList<String>();
        hotelPrice = new ArrayList<HotelPrice>();
    }

    private DatePickerDialog.OnDateSetListener checkInDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year  = selectedYear;
            month = selectedMonth;
            day   = selectedDay;
            checkinEt.setText(String.valueOf(day)+" - "+String.valueOf(month+1)+" - "+String.valueOf(year));
            if((month+1)/10 != 0){
                checindate = String.valueOf(year)+String.valueOf(month+1)+String.valueOf(day);
            }else{
                checindate = String.valueOf(year)+"0"+String.valueOf(month+1)+String.valueOf(day);
            }
            checkInSelected = true;
        }
    };
    private DatePickerDialog.OnDateSetListener checkOutDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year  = selectedYear;
            month = selectedMonth;
            day   = selectedDay;
            checkoutEt.setText(String.valueOf(day)+" - "+String.valueOf(month+1)+" - "+String.valueOf(year));
            if((month+1)/10 != 0){
                checkoutdate = String.valueOf(year)+String.valueOf(month+1)+String.valueOf(day);
            }else{
                checkoutdate = String.valueOf(year)+"0"+String.valueOf(month+1)+String.valueOf(day);
            }
            checkOutSelected = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_hotel);
        instantiate();
        dbAdapter.open();
        cityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                cityList.setVisibility(View.VISIBLE);
                citySelected = false;
                if (charSequence.toString().isEmpty()) {
                    cityList.setVisibility(View.GONE);
                } else {
                    list = dbAdapter.findCity(charSequence.toString(), list);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                city = list.get(i);
                cityEt.setText(city.name);
                cityList.setVisibility(View.GONE);
                citySelected = true;
            }
        });

        checkinEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(SearchHotelActivity.this, checkInDateListener, year, month, day).show();
                }
            }
        });

        checkoutEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(SearchHotelActivity.this, checkOutDateListener, year, month, day).show();
            }
        });

        checkoutEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(SearchHotelActivity.this, checkOutDateListener, year, month, day).show();
                }
            }
        });

        checkinEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(SearchHotelActivity.this, checkInDateListener, year, month, day).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.search:
                if(citySelected && checkInSelected && checkOutSelected) {
                    hotelIds.removeAll(hotelIds);
                    hotelIds.clear();
                    progressDialog.show();
                    String url = "http://developer.goibibo.com/api/cyclone/?app_id="+Config.GOIBIBI_APP_ID+"&app_key="+Config.GOIBIBO_APP_KEY+"&city_id="+city.id+"&check_in="+checindate+"&check_out="+checkoutdate;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null
                            , new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject data = new JSONObject(response.getString("data"));
                                Iterator<String> iterator = data.keys();
                                Gson gson = new Gson();
                                while(iterator.hasNext()) {
                                    hotel_ids = iterator.next();
                                    hotelIds.add(hotel_ids);
                                    HotelPrice price = gson.fromJson(data.getString(hotel_ids), HotelPrice.class);
                                    hotelPrice.add(price);
                                }
                            } catch (JSONException e) {
                                Log.d("option", "parse err");
                            }
                            progressDialog.dismiss();
                            Intent i = new Intent(SearchHotelActivity.this, HotelSearchResultActivity.class);
                            i.putExtra("what", "search");
                            i.putExtra("date", checkinEt.getText().toString());
                            startActivity(i);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SearchHotelActivity.this, "Unable to connect", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
                }else{
                    Toast.makeText(SearchHotelActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
