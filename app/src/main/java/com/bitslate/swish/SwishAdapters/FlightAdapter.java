package com.bitslate.swish.SwishAdapters;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitslate.swish.FlightSearchResultActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by shubhomoy on 19/9/15.
 */
public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightHolder> implements Animation.AnimationListener{

    Context context;
    ArrayList<Flight> list;
    public int selected = -1;
    Animation animationZoomIn, animationZoomOut;
    String what;
    SwishDatabase dbAdapter;
    SwishPreferences prefs;

    public FlightAdapter(Context c, ArrayList<Flight> list, String what) {
        this.context = c;
        this.list = list;
        this.what = what;
        prefs = new SwishPreferences(context);
        dbAdapter = new SwishDatabase(context);
        animationZoomIn = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_in);
        animationZoomOut = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_out);
        FlightSearchResultActivity.done.setAnimation(animationZoomOut);
        FlightSearchResultActivity.done.startAnimation(animationZoomOut);
    }

    @Override
    public FlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.flight_list_item, parent, false);
        FlightHolder holder = new FlightHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FlightHolder holder, final int position) {
        final Flight flight = list.get(position);
        holder.airlineTv.setText(flight.airline);
        holder.arrTimeTv.setText(flight.arrtime);
        holder.deptTimeTv.setText(flight.deptime);
        holder.durationTv.setText("Duration "+flight.duration);
        holder.departureDate.setText(flight.timestamp);

        holder.priceTv.setText("Rs " + flight.fare.totalfare);
        if(flight.stops.equals("0") || flight.stops.isEmpty()){
            holder.sourceToDestTv.setText(flight.origin+" to "+flight.destination + " (0 stops)");
        }else if(flight.onwardflights!=null) {
            try{
                holder.sourceToDestTv.setText(flight.origin+" to "+flight.onwardflights.get(flight.onwardflights.size()-1).destination + " ("+flight.stops+" stops)");
            }catch (ArrayIndexOutOfBoundsException e){
                holder.sourceToDestTv.setText(flight.origin+" to "+flight.destination+ " ("+flight.stops+" stops)");
            }
        }
        holder.item.setBackgroundColor(Color.parseColor(flight.itemColor));
        if(what.equals("search")) {
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selected == position) {
                        selected = -1;
                        flight.itemColor = "#ffffff";
                        FlightSearchResultActivity.done.setAnimation(animationZoomOut);
                        FlightSearchResultActivity.done.startAnimation(animationZoomOut);
                    }else{
                        if(selected!=-1)
                            list.get(selected).itemColor = "#ffffff";
                        selected = position;
                        flight.itemColor = "#c5cae9";
                        if(FlightSearchResultActivity.done.getAnimation() == animationZoomOut || FlightSearchResultActivity.done.getAnimation() ==
                                FlightSearchResultActivity.animationZoomOut) {
                            FlightSearchResultActivity.done.setAnimation(animationZoomIn);
                            FlightSearchResultActivity.done.startAnimation(animationZoomIn);
                        }
                    }
                    FlightAdapter.this.notifyDataSetChanged();
                }
            });
        }else{
            holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Remove flight");
                    builder.setMessage("Remove this flight from the plan?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dbAdapter.open();
                            dbAdapter.removeFlight(flight, prefs.getTripId());
                            flight.removeFlight(prefs.getTripId());
                            list.remove(position);
                            dbAdapter.close();
                            FlightAdapter.this.notifyDataSetChanged();
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

    @Override
    public void onAnimationStart(Animation animation) {
        if(animation == animationZoomIn)
            FlightSearchResultActivity.done.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == animationZoomOut)
            FlightSearchResultActivity.done.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    public class FlightHolder extends RecyclerView.ViewHolder{

        LinearLayout item;
        TextView airlineTv;
        TextView sourceToDestTv;
        TextView arrTimeTv;
        TextView deptTimeTv;
        TextView priceTv;
        TextView durationTv;
        TextView removeBtn;
        TextView departureDate;

        public FlightHolder(View itemView) {
            super(itemView);
            airlineTv = (TextView)itemView.findViewById(R.id.airline_tv);
            sourceToDestTv = (TextView)itemView.findViewById(R.id.source_dest_tv);
            arrTimeTv = (TextView)itemView.findViewById(R.id.arrtime_tv);
            deptTimeTv = (TextView)itemView.findViewById(R.id.deptime_tv);
            priceTv = (TextView)itemView.findViewById(R.id.price_tv);
            durationTv = (TextView)itemView.findViewById(R.id.duration_tv);
            removeBtn = (TextView)itemView.findViewById(R.id.remove_btn);
            departureDate = (TextView)itemView.findViewById(R.id.departure_date_tv);
            item = (LinearLayout)itemView.findViewById(R.id.list_item);
            if(what.equals("search"))
                removeBtn.setVisibility(View.GONE);
        }
    }
}
