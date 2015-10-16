package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bitslate.swish.FlightSearchResultActivity;
import com.bitslate.swish.HotelSearchResultActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by shubhomoy on 22/9/15.
 */
public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelHolder> {

    Context context;
    ArrayList<Hotel> list;
    String what;
    SwishDatabase dbAdapter;
    SwishPreferences prefs;
    public int selected = -1;
    Animation animationZoomIn, animationZoomOut;

    public HotelAdapter(Context context, ArrayList<Hotel> list, String what) {
        this.context = context;
        this.list = list;
        this.what = what;
        prefs = new SwishPreferences(context);
        dbAdapter = new SwishDatabase(context);
        animationZoomIn = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_in);
        animationZoomOut = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.zoom_out);
        HotelSearchResultActivity.done.setAnimation(animationZoomOut);
        HotelSearchResultActivity.done.startAnimation(animationZoomOut);
    }

    @Override
    public HotelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.hotel_list_item, parent, false);
        HotelHolder holder = new HotelHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(HotelHolder holder, final int position) {
        final Hotel hotel = list.get(position);
        holder.hotelNameTv.setText(hotel.name);
        holder.ratingBar.setRating(hotel.rating);
        String facility = "";
        int i;
        if(hotel.facilities != null ) {
            if(hotel.facilities.all !=null) {
                for (i = 0; i < hotel.facilities.all.size(); i++)
                    facility += hotel.facilities.all.get(i) + "\n";
            }else{
                facility += "Facilitites N/A";
            }
        }else{
            facility += "Facilitites N/A";
        }
        holder.facilityTv.setText(facility);
        String contact = "<b><font color='#000'>Web</font></b><br/>";
        if(hotel.contact.web != null) {
            for (i = 0; i < hotel.contact.web.size(); i++)
                contact += "<a href='"+hotel.contact.web.get(i) + "'>"+hotel.contact.web.get(i)+"</a><br />";
        }else
            contact += "N/A<br/>";
        contact += "<br /><b><font color='#000'>Email</font></b><br />";

        if(hotel.contact.email !=null) {
            for (i = 0; i < hotel.contact.email.size(); i++)
                contact += hotel.contact.email.get(i) + "<br />";
        }else{
            contact += "N/A<br />";
        }
        contact += "<br /><b><font color='#000'>Phone</font></b><br />";
        if(hotel.contact.phone != null) {
            for (i = 0; i < hotel.contact.phone.size(); i++)
                contact += hotel.contact.phone.get(i) + "<br />";
        }else{
            contact += "N/A<br />";
        }
        holder.contactTv.setText(Html.fromHtml(contact));
        String location = hotel.loc.full+"\n"+hotel.loc.location+"\n"+hotel.loc.pin+"\n"+hotel.loc.state;
        holder.locationTv.setText(location);
        if(hotel.img != null) {
            if (hotel.img.size() > 0) {
                holder.hotelImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(hotel.img.get(0).l).placeholder(R.drawable.loading).into(holder.hotelImage);
            } else {
                holder.hotelImage.setVisibility(View.GONE);
            }
        }else{
            holder.hotelImage.setVisibility(View.GONE);
        }
        holder.item.setBackgroundColor(Color.parseColor(hotel.itemColor));

        if(what.equals("search")) {
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selected == position) {
                        selected = -1;
                        hotel.itemColor = "#ffffff";
                        HotelSearchResultActivity.done.setAnimation(animationZoomOut);
                        HotelSearchResultActivity.done.startAnimation(animationZoomOut);
                    }else{
                        if(selected!=-1)
                            list.get(selected).itemColor = "#ffffff";
                        selected = position;
                        hotel.itemColor = "#c5cae9";
                        if(HotelSearchResultActivity.done.getAnimation() == animationZoomOut || HotelSearchResultActivity.done.getAnimation() ==
                                HotelSearchResultActivity.animationZoomOut) {
                            HotelSearchResultActivity.done.setAnimation(animationZoomIn);
                            HotelSearchResultActivity.done.startAnimation(animationZoomIn);
                        }
                    }
                    HotelAdapter.this.notifyDataSetChanged();
                }
            });
        }else{
            holder.item.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Remove hotel");
                    builder.setMessage("Remove this hotel from the plan?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
                            if(file.exists())
                                file.delete();
                            file = new File(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId());
                            if(file.isDirectory())
                                file.delete();
                            list.remove(position);
                            hotel.remove(prefs.getTripId());
                            HotelAdapter.this.notifyDataSetChanged();
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

    public class HotelHolder extends RecyclerView.ViewHolder{
        TextView hotelNameTv;
        RatingBar ratingBar;
        ImageView hotelImage;
        TextView facilityTv;
        TextView contactTv;
        TextView locationTv;
        LinearLayout item;
        TextView removeBtn;

        public HotelHolder(View itemView) {
            super(itemView);
            hotelNameTv = (TextView)itemView.findViewById(R.id.hotel_name_tv);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBar);
            hotelImage = (ImageView)itemView.findViewById(R.id.hotel_image);
            facilityTv = (TextView)itemView.findViewById(R.id.facility_tv);
            contactTv = (TextView)itemView.findViewById(R.id.contact_tv);
            locationTv = (TextView)itemView.findViewById(R.id.location_tv);
            item = (LinearLayout)itemView.findViewById(R.id.list_item);
            removeBtn = (TextView)itemView.findViewById(R.id.remove_btn);
            ratingBar.setEnabled(false);

            if(what.equals("search"))
                removeBtn.setVisibility(View.GONE);
        }
    }
}
