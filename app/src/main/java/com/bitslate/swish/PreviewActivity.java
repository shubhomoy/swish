package com.bitslate.swish;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.bitslate.swish.SwishAdapters.PreviewPageAdapter;
import com.bitslate.swish.SwishUtilities.SwishDatabase;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class PreviewActivity extends AppCompatActivity {

    public static Toolbar toolbar;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    ViewPager pager;
    PreviewPageAdapter adapter;

    void instantiate() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your itinerary Plan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pager = (ViewPager)findViewById(R.id.pager);
        adapter = new PreviewPageAdapter(this, getSupportFragmentManager());
        pager.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        instantiate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_plan_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_friends:
                Intent intent = new Intent(this, FriendsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (addBtn.isExpanded())
//            addBtn.collapse();
//    }
}
