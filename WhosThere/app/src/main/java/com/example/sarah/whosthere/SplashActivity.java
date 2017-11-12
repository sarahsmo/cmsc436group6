package com.example.sarah.whosthere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Represents the "Disclaimer thing that we talked about earlier once somebody logs in"
 */

public class SplashActivity extends AppCompatActivity {

    public final int SPLASH_DISPLAY_LENGTH = 5000; //in terms of milliseconds


    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_page);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent startActivityIntent = new Intent(SplashActivity.this, HomePage.class);
                startActivity(startActivityIntent);
                SplashActivity.this.finish();
            }
        }
        , SPLASH_DISPLAY_LENGTH);
    }

}
