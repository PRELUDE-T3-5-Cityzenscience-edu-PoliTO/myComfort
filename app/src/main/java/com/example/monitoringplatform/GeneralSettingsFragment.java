package com.example.monitoringplatform;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;


import com.google.gson.Gson;

import java.util.Calendar;


import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class GeneralSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SwitchPreferenceCompat myswitch;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        createNotificationChannel();
        myswitch= (SwitchPreferenceCompat) findPreference("feedback_notification");

    }
    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name="feedbackChannel";
            String description="Channel feedback";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("feedback",name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Intent intent = new Intent(getActivity(),reminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alertManager= (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        boolean parameter_value=prefs.getBoolean(key,false);
        SharedPreferences status = getActivity().getSharedPreferences("status", MODE_PRIVATE);
        SharedPreferences.Editor editor = status.edit();
        editor.putBoolean("feedback_notification",parameter_value);
        editor.commit();

        if (parameter_value){
            //myswitch.setChecked(true);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 04);

            // setRepeating() lets you specify a precise custom interval--in this case,
            // 20 minutes.
            alertManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
            1000 * 60*60*4, pendingIntent);
        }
        else{
            //myswitch.setChecked(false);
            alertManager.cancel(pendingIntent);
        }


    }


}