package com.bitslate.swish.SwishFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.BusSearchResultActivity;
import com.bitslate.swish.FlightSearchResultActivity;
import com.bitslate.swish.FriendsActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SearchBusActivity;
import com.bitslate.swish.SearchFlightActivity;
import com.bitslate.swish.SearchHotelActivity;
import com.bitslate.swish.ShowHotelActivity;
import com.bitslate.swish.SwishObjects.Bus;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class PreviewFragment extends Fragment {

    SwishDatabase dbAdapter;
    String itinerary;
    TextView flightsTv;
    TextView busesTv;
    TextView hotelTv;
    TextView dateTv;
    SwishPreferences prefs;
    com.getbase.floatingactionbutton.FloatingActionButton addFlight, addBus, addHotel, addPhoto;
    FloatingActionsMenu addBtn;
    ProgressDialog progressDialog;
    File image_file;
    String timeStamp;
    Button askBtn;

    int loadedEntities = 0;
    final int CAMERA_CAPTURE_TAG = 5;

    void initialize(View v) {

        dbAdapter = new SwishDatabase(getActivity());
        flightsTv = (TextView)v.findViewById(R.id.flights_tv);
        dateTv = (TextView)v.findViewById(R.id.date_tv);
        busesTv = (TextView)v.findViewById(R.id.buses_tv);
        hotelTv = (TextView)v.findViewById(R.id.hotels_tv);
        prefs = new SwishPreferences(getActivity());
        addFlight = (FloatingActionButton)v.findViewById(R.id.add_flight);
        addBus = (FloatingActionButton)v.findViewById(R.id.add_bus);
        addBtn = (FloatingActionsMenu)v.findViewById(R.id.add_btn);
        addHotel = (FloatingActionButton)v.findViewById(R.id.add_hotel);
        addPhoto = (FloatingActionButton)v.findViewById(R.id.add_photo);
        askBtn = (Button)v.findViewById(R.id.ask_btn);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching trip plan");
        progressDialog.show();
    }

    void loadList() {
        dbAdapter.open();
        PlanItem planItem = dbAdapter.findItinery(prefs.getTripId());
        if (planItem != null) {
            dateTv.setText(planItem.name);
        }
        ArrayList<Flight> list = dbAdapter.findFlights(prefs.getTripId(), new ArrayList<Flight>());
        flightsTv.setText("Flights (" + list.size() + ")");
        ArrayList<Bus> list2 = dbAdapter.findBuses(prefs.getTripId(), new ArrayList<Bus>());
        busesTv.setText("Buses (" + list2.size() + ")");
        dbAdapter.close();
        try {
            FileReader reader = new FileReader(Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId() + "/hotel.txt");
            hotelTv.setText("Hotels (1)");
        } catch (FileNotFoundException e) {

        }
    }

    void dismissProgressDialog() {
        if (loadedEntities >= 3) {
            progressDialog.dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.preview_fragment, container, false);
        initialize(v);
        loadList();

        fetchHotel();
        fetchRemoteFlights();
        fetchRemoteBuses();

        flightsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FlightSearchResultActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });
        busesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BusSearchResultActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });
        hotelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowHotelActivity.class);
                intent.putExtra("what", "plan");
                intent.putExtra("itinerary", itinerary);
                startActivityForResult(intent, 0);
            }
        });

        addFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                Intent newIntent = new Intent(getActivity(), SearchFlightActivity.class);
                startActivityForResult(newIntent, 0);
            }
        });

        addBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                Intent newIntent = new Intent(getActivity(), SearchBusActivity.class);
                startActivityForResult(newIntent, 0);
            }
        });

        addHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBtn.collapse();
                startActivityForResult(new Intent(getActivity(), SearchHotelActivity.class), 0);
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        askBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.ask_dialog, null);
                final EditText question = (EditText)v.findViewById(R.id.ask_question);
                builder.setTitle("Ask for hotels");
                builder.setMessage("Ask your friends to suggested some hotels");
                builder.setView(v);
                builder.setPositiveButton("Ask", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(question.getText().toString().trim().length()>0) {
                            sendQuestion(question.getText().toString());
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

        return v;
    }

    void sendQuestion(final String question) {
        String url = Config.SWISH_API_URL+"/ask";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), "Sent", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection Timeout", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("question", question);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("id", String.valueOf(prefs.getUser().fb_id));
                headers.put("accessToken", prefs.getAccessToken());
                return headers;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    void openCamera() {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = new File(Environment.getExternalStorageDirectory(), "SwishData/" + prefs.getTripId() + "/photos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        image_file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SwishData/" + prefs.getTripId() + "/photos/img_" + timeStamp + ".jpg");
        Uri uri = Uri.fromFile(image_file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        addBtn.collapse();
        startActivityForResult(intent, CAMERA_CAPTURE_TAG);
    }

    void fetchHotel() {
        String url = Config.CORE_URL + "/storage/swish/" + prefs.getTripId() + "/hotel.txt";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FileWriter writer = null;
                File dir = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId());
                dir.mkdirs();
                try {
                    writer = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId() + "/hotel.txt");
                    writer.write(response.toString());
                    writer.flush();
                    writer.close();
                    hotelTv.setText("Hotels (1)");
                } catch (IOException e) {
                    Log.d("option", "unable to write file");
                    hotelTv.setText("Hotels (0)");
                }
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId() + "/hotel.txt");
                if (file.exists())
                    file.delete();
                file = new File(Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId());
                if (file.isDirectory())
                    file.delete();
                hotelTv.setText("Hotels (0)");
                loadedEntities++;
                dismissProgressDialog();
            }
        }, getActivity());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    void fetchRemoteFlights() {
        String url = Config.SWISH_API_URL + "/flights/" + prefs.getTripId() + "/fetch";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllFlightsOfTrip(prefs.getTripId());
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Flight flight = gson.fromJson(jsonArray.getJSONObject(i).toString(), Flight.class);
                        dbAdapter.addNewFlight(flight, prefs.getTripId());
                    }
                    ArrayList<Flight> list = dbAdapter.findFlights(prefs.getTripId(), new ArrayList<Flight>());
                    flightsTv.setText("Flights (" + list.size() + ")");
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                dbAdapter.close();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection Timeout", Toast.LENGTH_LONG).show();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, getActivity());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    void fetchRemoteBuses() {
        String url = Config.SWISH_API_URL + "/buses/" + prefs.getTripId() + "/fetch";
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dbAdapter.open();
                dbAdapter.removeAllBusesOfTrip(prefs.getTripId());
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Bus bus = gson.fromJson(jsonArray.getJSONObject(i).toString(), Bus.class);
                        dbAdapter.addNewBus(bus, prefs.getTripId());
                    }
                    ArrayList<Bus> list2 = dbAdapter.findBuses(prefs.getTripId(), new ArrayList<Bus>());
                    busesTv.setText("Buses (" + list2.size() + ")");
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                dbAdapter.close();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection Timeout", Toast.LENGTH_LONG).show();
                loadedEntities++;
                dismissProgressDialog();
            }
        }, getActivity());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }

    class UploadPictureHttp extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Uploading");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String res = null;
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 60000);
            HttpConnectionParams.setSoTimeout(params, 60000);
            HttpClient client = new DefaultHttpClient(params);
            HttpPost post = new HttpPost(Config.SWISH_API_URL + "/upload");
            post.setHeader("id", prefs.getUser().fb_id);
            post.setHeader("accessToken", prefs.getAccessToken());
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            try {
                entity.addPart("image_name", new StringBody(strings[0]));
                entity.addPart("plan_id", new StringBody(String.valueOf(prefs.getTripId())));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            entity.addPart("image", new FileBody(new File(strings[1])));
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
            if (s != null) {
                Toast.makeText(getActivity(), "Picture uploaded", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Connection Timeout", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAMERA_CAPTURE_TAG)
            loadList();
        else if(requestCode == CAMERA_CAPTURE_TAG && resultCode == Activity.RESULT_OK){
            if(image_file!=null && image_file.exists()){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Upload");
                builder.setMessage("Do you want to upload this picture?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new UploadPictureHttp().execute("img_" + timeStamp + ".jpg", Environment.getExternalStorageDirectory().toString() + "/SwishData/" + prefs.getTripId() + "/photos/img_" + timeStamp + ".jpg");
                    }
                });
                builder.create().show();
            }
        }
    }

}
