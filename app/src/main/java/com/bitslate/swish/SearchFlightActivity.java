package com.bitslate.swish;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.SwishAdapters.AirportAdapter;
import com.bitslate.swish.SwishObjects.Airport;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by shubhomoy on 18/9/15.
 */
public class SearchFlightActivity extends AppCompatActivity {

    EditText fromEt;
    EditText toEt;
    EditText departureDateEt;
    TextView fromSearchTv, toSearchTv;
    ProgressDialog progressDialog;
    ListView fromList, toList;
    ArrayList<Airport> list;
    String fromCode="", toCode="", fromCity="", toCity="";
    Calendar calendar;
    int year, month, day;
    Toolbar toolbar;
    public static Activity activity = null;

    void instantiate() {
        activity = this;
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search Flights");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        fromEt = (EditText)findViewById(R.id.from_et);
        toEt = (EditText)findViewById(R.id.to_et);
        fromSearchTv = (TextView)findViewById(R.id.from_search_tv);
        toSearchTv = (TextView)findViewById(R.id.to_search_tv);
        departureDateEt = (EditText)findViewById(R.id.departure_date_et);
        fromList = (ListView)findViewById(R.id.from_list);
        toList = (ListView)findViewById(R.id.to_list);
        progressDialog = new ProgressDialog(this);
    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year  = selectedYear;
            month = selectedMonth;
            day   = selectedDay;
            departureDateEt.setText(String.valueOf(day)+" - "+String.valueOf(month+1)+" - "+String.valueOf(year));

        }
    };

    void search(){
        progressDialog.setMessage("Searching flights...");
        progressDialog.show();
        String url = "http://developer.goibibo.com/api/search/?app_id=" + Config.GOIBIBI_APP_ID + "&app_key=" + Config.GOIBIBO_APP_KEY + "&format=json&source=" + fromCode + "&destination=" + toCode + "&dateofdeparture=" + String.valueOf(year)+String.valueOf(month+1)+String.valueOf(day) + "&seatingclass=E&adults=1&children=0&infants=0";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        File file = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData", "flightsearch.txt");
                        try {
                            FileWriter writer = new FileWriter(file);
                            writer.write(response.toString());
                            writer.flush();
                            writer.close();
                            Intent i = new Intent(SearchFlightActivity.this, FlightSearchResultActivity.class);
                            i.putExtra("what", "search");
                            i.putExtra("date", departureDateEt.getText().toString());
                            startActivity(i);
                        } catch (IOException e) {
                            Log.d("option", "Unable to write");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchFlightActivity.this);
                builder.setTitle("Retry");
                builder.setMessage("Network slow. Retry?");
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        search();
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flight);
        instantiate();

        fromEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(fromCity)) {
                    fromSearchTv.setVisibility(View.VISIBLE);
                    VolleySingleton.getInstance().getRequestQueue().cancelAll("from_search");
                        String url = "https://airport.api.aero/airport/match/" + charSequence.toString() + "?user_key=" + Config.IATA_API_KEY;
                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    response = response.substring(response.indexOf("(") + 1);
                                    response = response.substring(0, response.length() - 1);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray jsonArray = new JSONArray(jsonObject.getString("airports"));
                                        Gson gson = new Gson();
                                        list = new ArrayList<Airport>();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            Airport airport = gson.fromJson(jsonArray.get(i).toString(), Airport.class);
                                            list.add(airport);
                                        }
                                        AirportAdapter airportAdapter = new AirportAdapter(SearchFlightActivity.this, list);
                                        fromList.setAdapter(airportAdapter);
                                        fromList.setVisibility(View.VISIBLE);
                                        fromSearchTv.setVisibility(View.GONE);
                                        fromList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                fromCity = list.get(i).city;
                                                fromEt.setText(list.get(i).city);
                                                fromList.setVisibility(View.GONE);
                                                fromSearchTv.setVisibility(View.GONE);
                                                fromCode = list.get(i).code;
                                            }
                                        });
                                    } catch (JSONException e) {

                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            fromSearchTv.setVisibility(View.GONE);
                        }
                    });
                    jsonObjectRequest.setTag("from_search");
                    VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        toEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(toCity)) {
                    toSearchTv.setVisibility(View.VISIBLE);
                    VolleySingleton.getInstance().getRequestQueue().cancelAll("to_search");
                    String url = "https://airport.api.aero/airport/match/" + charSequence.toString() + "?user_key=" + Config.IATA_API_KEY;
                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    response = response.substring(response.indexOf("(") + 1);
                                    response = response.substring(0, response.length() - 1);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray jsonArray = new JSONArray(jsonObject.getString("airports"));
                                        Gson gson = new Gson();
                                        list = new ArrayList<Airport>();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            Airport airport = gson.fromJson(jsonArray.get(i).toString(), Airport.class);
                                            list.add(airport);
                                        }
                                        AirportAdapter airportAdapter = new AirportAdapter(SearchFlightActivity.this, list);
                                        toList.setAdapter(airportAdapter);
                                        toList.setVisibility(View.VISIBLE);
                                        toSearchTv.setVisibility(View.GONE);
                                        toList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                toCity = list.get(i).city;
                                                toEt.setText(list.get(i).city);
                                                toList.setVisibility(View.GONE);
                                                toSearchTv.setVisibility(View.GONE);
                                                toCode = list.get(i).code;
                                            }
                                        });
                                    } catch (JSONException e) {

                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("option", error.toString());
                            toSearchTv.setVisibility(View.GONE);
                        }
                    });
                    jsonObjectRequest.setTag("to_search");
                    VolleySingleton.getInstance().getRequestQueue().add(jsonObjectRequest);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        departureDateEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(SearchFlightActivity.this, pickerListener, year, month, day).show();
                }
            }
        });

        departureDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(SearchFlightActivity.this, pickerListener, year, month, day).show();
            }
        });

    }

    boolean checkInput() {
        if(fromEt.getText().toString().trim().length()>0 && toEt.getText().toString().trim().length()>0 && departureDateEt.getText().toString().trim().length()>0)
            return true;
        else
            return false;
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
                if(checkInput()) {
                    search();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
