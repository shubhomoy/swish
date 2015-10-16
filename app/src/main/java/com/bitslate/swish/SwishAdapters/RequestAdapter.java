package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    Context context;
    ArrayList<PlanItem> list;
    SwishPreferences prefs;

    public RequestAdapter(Context c, ArrayList<PlanItem> list) {
        this.context = c;
        this.list = list;
        prefs = new SwishPreferences(context);
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);
        RequestViewHolder holder = new RequestViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RequestViewHolder holder, int position) {
        final PlanItem planItem = list.get(position);
        holder.tripName.setText(planItem.name);
        holder.name.setText(planItem.creator.fname);
        Glide.with(context).load("https://graph.facebook.com/"+planItem.creator.fb_id+"/picture").into(holder.imageView);
        if(planItem.pivot.status == 0) {
            holder.status.setText("Pending");
        }else if(planItem.pivot.status == 1) {
            holder.status.setText("Accepted");
        }else{
            holder.status.setText("Declined");
        }

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.status.setText("Please wait...");
                planItem.accept(prefs.getUser().id, RequestAdapter.this);
            }
        });

        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.status.setText("Please wait...");
                planItem.decline(prefs.getUser().id, RequestAdapter.this);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView tripName;
        TextView name;
        ImageView imageView;
        TextView status;
        Button accept;
        Button decline;

        public RequestViewHolder(View itemView) {
            super(itemView);
            tripName = (TextView)itemView.findViewById(R.id.trip_name_tv);
            name = (TextView)itemView.findViewById(R.id.name);
            imageView = (ImageView)itemView.findViewById(R.id.image);
            status = (TextView)itemView.findViewById(R.id.status);
            accept = (Button)itemView.findViewById(R.id.accept);
            decline = (Button)itemView.findViewById(R.id.decline);
        }
    }
}
