package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitslate.swish.BusSearchResultActivity;
import com.bitslate.swish.FlightSearchResultActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.Bus;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 19/9/15.
 */
public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusHolder> {

    Context context;
    ArrayList<Bus> list;
    public int selected = -1;
    Animation animationZoomIn, animationZoomOut;
    String what;
    SwishDatabase dbAdapter;
    SwishPreferences prefs;

    public BusAdapter(Context c, ArrayList<Bus> list, String what) {
        this.context = c;
        this.list = list;
        prefs = new SwishPreferences(context);
        dbAdapter = new SwishDatabase(context);
        animationZoomIn = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_in);
        animationZoomOut = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_out);
        BusSearchResultActivity.done.startAnimation(animationZoomOut);
        this.what = what;
    }

    @Override
    public BusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.bus_list_item, parent, false);
        BusHolder holder = new BusHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BusHolder holder, final int position) {
        final Bus bus = list.get(position);
        holder.travelNameTv.setText(bus.TravelsName);
        holder.typeTv.setText(bus.BusType);
        holder.deptTimeTv.setText(bus.DepartureTime);
        holder.durationTv.setText(bus.duration);
        holder.priceTv.setText(bus.fare.totalfare);
        holder.sourceToDestTv.setText(bus.origin+" to "+bus.destination);
        holder.item.setBackgroundColor(Color.parseColor(bus.itemColor));
        if(what.equals("search")) {
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selected == position) {
                        selected = -1;
                        bus.itemColor = "#ffffff";
                        BusSearchResultActivity.done.setAnimation(animationZoomOut);
                        BusSearchResultActivity.done.startAnimation(animationZoomOut);
                    } else {
                        if (selected != -1)
                            list.get(selected).itemColor = "#ffffff";
                        selected = position;
                        bus.itemColor = "#c5cae9";
                        if (BusSearchResultActivity.done.getAnimation() == animationZoomOut || BusSearchResultActivity.done.getAnimation() ==
                                BusSearchResultActivity.animationZoomOut) {
                            BusSearchResultActivity.done.setAnimation(animationZoomIn);
                            BusSearchResultActivity.done.startAnimation(animationZoomIn);
                        }
                    }
                    BusAdapter.this.notifyDataSetChanged();
                }
            });
        }else{
            holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Remove Bus");
                    builder.setMessage("Remove this bus from the plan?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dbAdapter.open();
                            dbAdapter.removeBus(bus, prefs.getTripId());
                            bus.removeBus(prefs.getTripId());
                            list.remove(position);
                            dbAdapter.close();
                            BusAdapter.this.notifyDataSetChanged();
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class BusHolder extends RecyclerView.ViewHolder{

        LinearLayout item;
        TextView travelNameTv;
        TextView sourceToDestTv;
        TextView typeTv;
        TextView deptTimeTv;
        TextView priceTv;
        TextView durationTv;
        TextView removeBtn;

        public BusHolder(View itemView) {
            super(itemView);
            travelNameTv = (TextView)itemView.findViewById(R.id.travelsname_tv);
            sourceToDestTv = (TextView)itemView.findViewById(R.id.source_dest_tv);
            typeTv = (TextView)itemView.findViewById(R.id.type_tv);
            deptTimeTv = (TextView)itemView.findViewById(R.id.deptime_tv);
            priceTv = (TextView)itemView.findViewById(R.id.price_tv);
            durationTv = (TextView)itemView.findViewById(R.id.duration_tv);
            removeBtn = (TextView)itemView.findViewById(R.id.remove_btn);
            item = (LinearLayout)itemView.findViewById(R.id.list_item);
            if(what.equals("search"))
                removeBtn.setVisibility(View.GONE);
        }
    }
}
