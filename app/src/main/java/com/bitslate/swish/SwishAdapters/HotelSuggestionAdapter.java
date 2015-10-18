package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bitslate.swish.HotelSearchResultActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishObjects.HotelPrice;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by shubhomoy on 18/10/15.
 */
public class HotelSuggestionAdapter extends RecyclerView.Adapter<HotelSuggestionAdapter.HotelSuggestionViewHolder> {

    Context context;
    ArrayList<Hotel> list;
    AlertDialog.Builder builder;

    public HotelSuggestionAdapter(Context c, ArrayList<Hotel> list){
        this.context = c;
        this.list = list;
        this.builder = new AlertDialog.Builder(context);
    }

    @Override
    public HotelSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.hotel_list_item, parent, false);
        HotelSuggestionViewHolder holder = new HotelSuggestionViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(HotelSuggestionViewHolder holder, int position) {
        final Hotel hotel = list.get(position);
        holder.hotelNameTv.setText(hotel.name);
        holder.ratingBar.setRating(hotel.rating);
        String facility = "";
        int i;
        if(hotel.facilities != null ) {
            if(hotel.facilities.all !=null) {
                for (i = 0; i < hotel.facilities.all.size(); i++)
                    facility += "* "+hotel.facilities.all.get(i) + "\n";
            }else{
                facility += "Facilitites N/A";
            }
        }else{
            facility += "Facilitites N/A";
        }
        //holder.facilityTv.setText(facility);
        final String finalFacility = facility;
        holder.showFacilitiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Facilities");
                View v = LayoutInflater.from(context).inflate(R.layout.hotel_details_dialog, null);
                TextView details = (TextView)v.findViewById(R.id.detail_text);
                details.setText(finalFacility);
                builder.setView(v);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
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

        holder.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:" + hotel.loc.lat + "," + hotel.loc.lon + " (" + hotel.loc.location + ")"));
                context.startActivity(intent);
            }
        });

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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HotelSuggestionViewHolder extends RecyclerView.ViewHolder{

        TextView hotelNameTv;
        RatingBar ratingBar;
        ImageView hotelImage;
        TextView contactTv;
        TextView locationTv;
        LinearLayout item;
        TextView original_price;
        TextView minimum_price;
        TextView discount;
        TextView removeBtn;
        Button showFacilitiesBtn;
        ImageButton map;

        public HotelSuggestionViewHolder(View itemView) {
            super(itemView);
            hotelNameTv = (TextView)itemView.findViewById(R.id.hotel_name_tv);
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBar);
            hotelImage = (ImageView)itemView.findViewById(R.id.hotel_image);
            contactTv = (TextView)itemView.findViewById(R.id.contact_tv);
            locationTv = (TextView)itemView.findViewById(R.id.location_tv);
            item = (LinearLayout)itemView.findViewById(R.id.list_item);
            removeBtn = (TextView)itemView.findViewById(R.id.remove_btn);
            original_price = (TextView)itemView.findViewById(R.id.original_price_tv);
            minimum_price = (TextView)itemView.findViewById(R.id.minimum_price_tv);
            discount = (TextView)itemView.findViewById(R.id.discount_tv);
            showFacilitiesBtn = (Button)itemView.findViewById(R.id.show_facilities_btn);
            map = (ImageButton)itemView.findViewById(R.id.map_btn);
            ratingBar.setEnabled(false);

            original_price.setVisibility(View.GONE);
            minimum_price.setVisibility(View.GONE);
            discount.setVisibility(View.GONE);
            removeBtn.setVisibility(View.GONE);
        }
    }
}
