package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.User;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 14/10/15.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    ArrayList<User> list;
    Context context;
    int planId;

    public FriendsAdapter(Context context, ArrayList<User> list, int planId) {
        this.list = list;
        this.context = context;
        this.planId = planId;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        FriendViewHolder holder = new FriendViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        User obj = list.get(position);
        Glide.with(context).load("https://graph.facebook.com/"+obj.id+"/picture").into(holder.imageView);
        holder.name.setText(obj.fname);
        if(obj.pivot.status == 0) {
            holder.status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_grey));
        }else if(obj.pivot.status == 1){
            holder.status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));
        }else{
            holder.status.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView name;
        ImageView status;

        public FriendViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
            name = (TextView)itemView.findViewById(R.id.name);
            status = (ImageView)itemView.findViewById(R.id.status);
        }
    }
}
