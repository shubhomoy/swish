package com.bitslate.swish;

import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class ShowHotelActivity extends AppCompatActivity {

    SwishPreferences prefs;
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
    Toolbar toolbar;
    ImageButton map;
    Button suggestBtn;


    void instantiate() {
        prefs = new SwishPreferences(this);
        hotelNameTv = (TextView)findViewById(R.id.hotel_name_tv);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        hotelImage = (ImageView)findViewById(R.id.hotel_image);
        contactTv = (TextView)findViewById(R.id.contact_tv);
        locationTv = (TextView)findViewById(R.id.location_tv);
        item = (LinearLayout)findViewById(R.id.list_item);
        removeBtn = (TextView)findViewById(R.id.remove_btn);
        suggestBtn = (Button)findViewById(R.id.suggest_btn);
        original_price = (TextView)findViewById(R.id.original_price_tv);
        minimum_price = (TextView)findViewById(R.id.minimum_price_tv);
        discount = (TextView)findViewById(R.id.discount_tv);
        showFacilitiesBtn = (Button)findViewById(R.id.show_facilities_btn);
        map = (ImageButton)findViewById(R.id.map_btn);
        ratingBar.setEnabled(false);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hotel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);
        instantiate();
        try {
            FileReader reader = new FileReader(Environment.getExternalStorageDirectory().toString()+"/SwishData/"+prefs.getTripId()+"/hotel.txt");
            String response = "", current = null;
            BufferedReader bufferedReader = new BufferedReader(reader);
            while((current = bufferedReader.readLine())!=null) {
                response += current;
            }
            Gson gson = new Gson();
            final Hotel hotel = gson.fromJson(response, Hotel.class);
            Log.d("option", hotel.id);
            hotelNameTv.setText(hotel.name);
            ratingBar.setRating(hotel.rating);
            original_price.setText(hotel.op);
            minimum_price.setText(hotel.mp);
            discount.setText(hotel.discount+" Discount");
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

            final String finalFacility = facility;
            showFacilitiesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowHotelActivity.this);
                    builder.setTitle("Facilities");
                    View v = LayoutInflater.from(ShowHotelActivity.this).inflate(R.layout.hotel_details_dialog, null);
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
            contactTv.setText(Html.fromHtml(contact));
            String location = hotel.loc.full+"\n"+hotel.loc.location+"\n"+hotel.loc.pin+"\n"+hotel.loc.state;
            locationTv.setText(location);
            if(hotel.img != null) {
                if (hotel.img.size() > 0) {
                    hotelImage.setVisibility(View.VISIBLE);
                    Glide.with(ShowHotelActivity.this).load(hotel.img.get(0).l).placeholder(R.drawable.loading).into(hotelImage);
                } else {
                    hotelImage.setVisibility(View.GONE);
                }
            }else{
                hotelImage.setVisibility(View.GONE);
            }
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowHotelActivity.this);
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

                            hotel.remove(prefs.getTripId());
                            setResult(RESULT_OK);
                            finish();
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

            map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:" + hotel.loc.lat + "," + hotel.loc.lon + " (" + hotel.loc.location + ")"));
                    startActivity(intent);
                }
            });

            suggestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ShowHotelActivity.this, FriendListActivity.class);
                    intent.putExtra("hotel_id", hotel.id);
                    startActivity(intent);
                }
            });

        } catch (FileNotFoundException e) {
            Toast.makeText(ShowHotelActivity.this, "No hotel found", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
