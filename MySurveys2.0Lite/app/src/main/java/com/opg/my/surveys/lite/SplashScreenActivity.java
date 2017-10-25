package com.opg.my.surveys.lite;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.opg.my.surveys.lite.common.Util;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {

    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);
        mContext = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // Create a Timer
        Timer RunSplash = new Timer();

        // Task to do when the timer ends
        TimerTask ShowSplash = new TimerTask() {
            @Override
            public void run() {
                // Close SplashScreenActivity.class
                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra("Splash","Splash");
                startActivity(intent);
                finish();
            }
        };

        // Start the timer
        RunSplash.schedule(ShowSplash, 200);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
