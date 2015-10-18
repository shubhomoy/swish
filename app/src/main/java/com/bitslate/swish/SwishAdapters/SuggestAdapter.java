package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 18/10/15.
 */
public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.SuggestViewHolder> {

    ArrayList<User> list;
    Context context;
    SwishPreferences prefs;
    String hotel_id;


    public SuggestAdapter(Context context, ArrayList<User> list, String hotel_id) {
        this.list = list;
        this.context = context;
        this.hotel_id = hotel_id;
        prefs = new SwishPreferences(context);
    }


    @Override
    public SuggestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        SuggestViewHolder holder = new SuggestViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(SuggestViewHolder holder, int position) {
        final User obj = list.get(position);
        Glide.with(context).load("https://graph.facebook.com/" + obj.fb_id + "/picture").into(holder.imageView);
        holder.name.setText(obj.fname);

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Suggest");
                builder.setMessage("Suggest this hotel to " + obj.fname + " ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        suggest(obj.id);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SuggestViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name;
        ImageView status;
        LinearLayout item;
        public SuggestViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            status = (ImageView) itemView.findViewById(R.id.status);
            item = (LinearLayout) itemView.findViewById(R.id.item);
            status.setVisibility(View.GONE);
        }
    }

    void suggest(int user_id) {
        String url = Config.SWISH_API_URL+"/suggest?user_id="+user_id+"&hotel_id="+hotel_id;
        SwishRequest swishRequest = new SwishRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(context, "Suggestion sent", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Connection timeout", Toast.LENGTH_LONG).show();
            }
        }, context);
        VolleySingleton.getInstance().getRequestQueue().add(swishRequest);
    }
}
