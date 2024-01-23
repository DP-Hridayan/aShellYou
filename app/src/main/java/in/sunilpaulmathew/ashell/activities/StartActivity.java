/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package in.sunilpaulmathew.ashell.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.ashell.R;
import in.sunilpaulmathew.ashell.utils.Commands;
import in.sunilpaulmathew.ashell.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 28, 2022
 */
public class StartActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(in.sunilpaulmathew.ashell.R.layout.activity_start);

        LinearLayoutCompat mMainLayout = findViewById(R.id.layout_main);
        MaterialCardView mStartCard = findViewById(R.id.start_card);
        MaterialTextView mAboutText = findViewById(R.id.about_text);

        if (Shizuku.pingBinder()) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstLaunch", true)) {
                Shizuku.requestPermission(0);
                mMainLayout.setVisibility(View.VISIBLE);
                mAboutText.setText(getString(R.string.app_summary));
            } else {
                loadUI(this);
            }
        } else {
            mMainLayout.setVisibility(View.VISIBLE);
            mAboutText.setText(getString(R.string.shizuku_unavailable_message));
            mAboutText.setTextColor(Color.RED);
            mStartCard.setVisibility(View.GONE);
        }

        mStartCard.setOnClickListener(v -> {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstLaunch", false).apply();
            loadUI(this);
        });

        mAboutText.setOnClickListener(v -> Utils.loadShizukuWeb(this));
    }

    private static void loadUI(Activity activity) {
        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            Commands.loadPackageInfo();
            new Handler(Looper.getMainLooper()).post(() -> {
                Intent aShellActivity = new Intent(activity, aShellActivity.class);
                activity.startActivity(aShellActivity);
                activity.finish();
            });
            if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
    }

}