package com.tangledbytes.phoneblocker.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tangledbytes.phoneblocker.R;
import com.tangledbytes.phoneblocker.utils.BlockerSession;

import java.util.Random;

public class AppreciationActivity extends AppCompatActivity {
    public static final String TAG = "AppreciationActivity";
    private TextView tvProgressTitle;
    private Button btnFinish;
    private LinearProgressIndicator pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appreciation);
        setupViews();
        startLoading();
        new BlockerSession(this).setHasShownAppreciationActivity(true);
    }

    private void startLoading() {
        Handler handler = new Handler();
        Thread loader = new Thread(() -> {
            int progress = 0;
            while (progress <= 100) {
                try {
                    Thread.sleep(new Random().nextInt(150));
                     progress += 10;
                    int finalProgress = progress;
                    handler.post(() -> pbLoading.setProgressCompat(finalProgress, true));
                    if (progress == 30)
                        updateProgressTitle(R.string.finishing_up);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            updateProgressTitle(R.string.done);
            btnFinish.post(() -> btnFinish.setVisibility(View.VISIBLE));
        });
        loader.start();
    }


    private void setupViews() {
        pbLoading = findViewById(R.id.pb_loading);
        tvProgressTitle = findViewById(R.id.tv_progress_title);
        btnFinish = findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener((View view) -> showAd());
    }

    private void showAd() {
        finish();
    }

    private void updateProgressTitle(int resId) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        int animDuration = 600;
        fadeOut.setDuration(animDuration);
        fadeIn.setDuration(animDuration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvProgressTitle.startAnimation(fadeIn);
                tvProgressTitle.setText(resId);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        tvProgressTitle.startAnimation(fadeOut);
    }

    @Override
    public void onBackPressed() {
    }
}