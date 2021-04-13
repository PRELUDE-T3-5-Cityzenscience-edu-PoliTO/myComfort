package com.example.monitoringplatform;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;

public class notification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Intent intent = new Intent(notification.this,reminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(notification.this,0,intent,0);
        AlarmManager alertManager= (AlarmManager) getSystemService(ALARM_SERVICE);

        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 04);
        long mytime=System.currentTimeMillis();

        // setRepeating() lets you specify a precise custom interval--in this case,
        // 20 minutes.
        //alertManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                //1000 * 10, pendingIntent);
        alertManager.set(AlarmManager.RTC_WAKEUP,mytime+(1000*10),pendingIntent);
    }
}