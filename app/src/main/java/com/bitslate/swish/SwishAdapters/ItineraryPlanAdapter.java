package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitslate.swish.PreviewActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.PlanItem;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class ItineraryPlanAdapter extends RecyclerView.Adapter<ItineraryPlanAdapter.PlanItineraryHolder> {

    Context context;
    ArrayList<PlanItem> list;
    SwishPreferences prefs;
    SwishDatabase dbAdapter;

    public ItineraryPlanAdapter(Context context, ArrayList<PlanItem> list) {
        this.context = context;
        this.list = list;
        prefs = new SwishPreferences(context);
        dbAdapter = new SwishDatabase(context);
    }

    @Override
    public PlanItineraryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.itinerary_plan_list_item, parent, false);
        PlanItineraryHolder holder = new PlanItineraryHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(PlanItineraryHolder holder, final int position) {
        holder.textView.setText(list.get(position).name);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PreviewActivity.class);
                prefs.insertTripId(list.get(position).id);
                context.startActivity(intent);
            }
        });

        holder.item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to remove this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbAdapter.open();
                        dbAdapter.removePlan(list.get(position).id);
                        File file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+list.get(position).id);
                        if(file.isDirectory()){
                            String files[] = file.list();

                            for (String temp : files) {
                                //construct the file structure
                                File fileDelete = new File(file, temp);
                                fileDelete.delete();
                            }

                            //check the directory again, if empty then delete it
                            if (file.list().length == 0) {
                                file.delete();
                            }
                        }
                        list.remove(position);
                        dbAdapter.close();
                        ItineraryPlanAdapter.this.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PlanItineraryHolder extends RecyclerView.ViewHolder{

        TextView textView;
        LinearLayout item;

        public PlanItineraryHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.textview);
            item = (LinearLayout)itemView.findViewById(R.id.list_item);
        }
    }
}
