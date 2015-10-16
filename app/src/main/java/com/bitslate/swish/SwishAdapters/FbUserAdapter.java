package com.bitslate.swish.SwishAdapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitslate.swish.AddFriendActivity;
import com.bitslate.swish.FriendsActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class FbUserAdapter extends RecyclerView.Adapter<FbUserAdapter.FbUserViewHolder> {

    ArrayList<User> list;
    Context context;
    int planId;
    SwishDatabase database;
    SwishPreferences prefs;

    public FbUserAdapter(Context context, ArrayList<User> list, int planId) {
        this.list = list;
        this.context = context;
        this.planId = planId;
        database = new SwishDatabase(context);
        prefs = new SwishPreferences(context);
    }

    @Override
    public FbUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        FbUserViewHolder holder = new FbUserViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FbUserViewHolder holder, int position) {
        final User obj = list.get(position);
        Glide.with(context).load("https://graph.facebook.com/" + obj.fb_id + "/picture").into(holder.imageView);
        holder.name.setText(obj.fname);
        if (obj.plans.size() == 0) {
            holder.status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_grey_outline));
        } else if (obj.plans.size() > 0) {
            holder.status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));
        }

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.open();
                if (obj.plans.size() == 0) {
                    obj.plans.add(database.findItinery(planId));
                    obj.addPlan(planId);
                } else {
                    obj.plans.remove(0);
                    obj.removePlan(planId);
                }
                database.close();
                FbUserAdapter.this.notifyDataSetChanged();
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FbUserViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView name;
        ImageView status;
        LinearLayout item;

        public FbUserViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            status = (ImageView) itemView.findViewById(R.id.status);
            item = (LinearLayout) itemView.findViewById(R.id.item);
        }
    }
}
