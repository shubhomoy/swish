package com.bitslate.swish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bitslate.swish.SwishFragments.SignupFragment;
import com.bitslate.swish.SwishUtilities.SwishPreferences;

/**
 * Created by shubhomoy on 13/10/15.
 */
public class SignupActivity extends AppCompatActivity {

    SignupFragment signupFragment;
    SwishPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);
        prefs = new SwishPreferences(this);

        if(prefs.getAccessToken() != null ){
            startActivity(new Intent(this, PlanListActivity.class));
            finish();
        }else{
            signupFragment = new SignupFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, signupFragment).commit();
        }
    }
}
