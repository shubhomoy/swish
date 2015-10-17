package com.bitslate.swish.SwishFragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishAdapters.ChatAdapter;
import com.bitslate.swish.SwishObjects.Chat;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<Chat> list;
    ChatAdapter adapter;
    EditText chatMessage;
    ImageButton sendBtn;
    SwishPreferences prefs;
    ProgressDialog progressDialog;
    TextView dateTv;
    SwishDatabase database;


    void instantiate(View v) {
        recyclerView = (RecyclerView)v.findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        list = new ArrayList<Chat>();
        chatMessage = (EditText)v.findViewById(R.id.chat_message);
        sendBtn = (ImageButton)v.findViewById(R.id.send_btn);
        dateTv = (TextView)v.findViewById(R.id.date_tv);
        prefs = new SwishPreferences(getActivity());
        adapter = new ChatAdapter(getActivity(), list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        progressDialog = new ProgressDialog(getActivity());
        database = new SwishDatabase(getActivity());
        database.open();
        PlanItem planItem = database.findItinery(prefs.getTripId());
        if (planItem != null) {
            dateTv.setText(planItem.name);
        }
        database.close();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        instantiate(v);
        fetchChats();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatMessage.getText().toString().trim().length() > 0) {
                    sendMessage(chatMessage.getText().toString());
                    chatMessage.setText("");
                }
            }
        });

        return v;
    }

    void fetchChats() {
        progressDialog.setMessage("Fetching messages");
        String url = Config.SWISH_API_URL+"/chats?plan_id="+prefs.getTripId();
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(response.getString("data"));
                    for(int i=0; i<jsonArray.length(); i++) {
                        Chat chat = gson.fromJson(jsonArray.getJSONObject(i).toString(), Chat.class);
                        list.add(chat);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection timeout", Toast.LENGTH_LONG).show();
            }
        }, getActivity());
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);

    }

    void sendMessage(final String message) {
        String url = Config.SWISH_API_URL+"/send/message";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Chat chat = gson.fromJson(jsonObject.getString("data"), Chat.class);
                    list.add(chat);

                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                Toast.makeText(getActivity(), "Connection timeout", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("id", String.valueOf(prefs.getUser().fb_id));
                headers.put("accessToken", prefs.getAccessToken());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("plan_id", String.valueOf(prefs.getTripId()));
                params.put("message", message);
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }
}
