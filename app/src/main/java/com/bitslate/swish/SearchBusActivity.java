package com.bitslate.swish;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.VolleySingleton;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by shubhomoy on 19/9/15.
 */
public class SearchBusActivity extends AppCompatActivity {

    EditText fromEt;
    EditText toEt;
    EditText departureDateEt;
    ProgressDialog progressDialog;
    ListView fromList, toList;
    Calendar calendar;
    int year, month, day;
    Toolbar toolbar;
    public static Activity activity = null;

    void instantiate() {
        activity = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search Buses");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        fromEt = (EditText) findViewById(R.id.from_et);
        toEt = (EditText) findViewById(R.id.to_et);
        departureDateEt = (EditText) findViewById(R.id.departure_date_et);
        fromList = (ListView) findViewById(R.id.from_list);
        toList = (ListView) findViewById(R.id.to_list);
        progressDialog = new ProgressDialog(this);
    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            departureDateEt.setText(String.valueOf(day) + " - " + String.valueOf(month + 1) + " - " + String.valueOf(year));

        }
    };

    boolean checkValidInput() {
        if (fromEt.getText().toString().trim().length() > 0 && toEt.getText().toString().trim().length() > 0 &&
                departureDateEt.getText().toString().trim().length() > 0) {
            return true;
        } else {
            Toast.makeText(this, "Something's missing", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bus);
        instantiate();

        departureDateEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(SearchBusActivity.this, pickerListener, year, month, day).show();
                }
            }
        });

        departureDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(SearchBusActivity.this, pickerListener, year, month, day).show();
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
                if (checkValidInput()) {
                    progressDialog.setMessage("Fetching...");
                    progressDialog.show();
                    String url = "http://developer.goibibo.com/api/bus/search/?app_id=" + Config.GOIBIBI_APP_ID + "&app_key=" + Config.GOIBIBO_APP_KEY + "&format=json&source=" + fromEt.getText().toString() + "&destination=" + toEt.getText().toString() + "&dateofdeparture=" + String.valueOf(year) + String.valueOf(month + 1) + String.valueOf(day);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    progressDialog.dismiss();
                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData", "bussearch.txt");
                                    try {
                                        FileWriter writer = new FileWriter(file);
                                        writer.write(response.toString());
                                        writer.flush();
                                        writer.close();
                                        Intent i = new Intent(SearchBusActivity.this, BusSearchResultActivity.class);
                                        i.putExtra("what", "search");
                                        startActivity(i);
                                    } catch (IOException e) {
                                        Log.d("option", "Unable to write");
                                    }
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
