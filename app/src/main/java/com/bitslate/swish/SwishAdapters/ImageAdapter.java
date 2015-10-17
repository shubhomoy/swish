package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bitslate.swish.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{

    ArrayList<String> list;
    Context context;
    int width;

    public ImageAdapter(Context c, ArrayList<String> list) {
        this.context = c;
        this.list = list;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        ImageViewHolder holder = new ImageViewHolder(v, width);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Glide.with(context).load(list.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public ImageViewHolder(View itemView, int width) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
            this.imageView.getLayoutParams().height=width;
        }
    }
}
