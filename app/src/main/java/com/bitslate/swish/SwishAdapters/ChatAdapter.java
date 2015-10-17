package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.Chat;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    Context context;
    ArrayList<Chat> list;

    public ChatAdapter(Context c, ArrayList<Chat> list) {
        this.context = c;
        this.list = list;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        ChatViewHolder holder = new ChatViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        Chat chat = list.get(position);
        holder.message.setText(chat.message);
        holder.username.setText(chat.user.fname);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        TextView message;

        public ChatViewHolder(View itemView) {
            super(itemView);
            username = (TextView)itemView.findViewById(R.id.username);
            message = (TextView)itemView.findViewById(R.id.message);
        }
    }
}
