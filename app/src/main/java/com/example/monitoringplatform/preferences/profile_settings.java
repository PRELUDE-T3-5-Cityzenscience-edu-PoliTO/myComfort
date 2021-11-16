package com.example.monitoringplatform.preferences;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.monitoringplatform.R;
import com.example.monitoringplatform.myfragments.SettingsFragment;

public class profile_settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        setTitle("Edit profile");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}

