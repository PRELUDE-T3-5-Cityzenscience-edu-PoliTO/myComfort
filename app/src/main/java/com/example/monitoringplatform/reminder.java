package com.example.monitoringplatform;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.monitoringplatform.ui.login.login;

import static android.content.Context.MODE_PRIVATE;

public class reminder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences status = context.getSharedPreferences("status", MODE_PRIVATE);
        Intent notificationIntent;
        if(status.getBoolean("login",false)){
            notificationIntent=new Intent(context, homepage.class);
        } else{
            notificationIntent=new Intent(context, login.class);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"feedback")
                .setSmallIcon(R.drawable.ic_tips)
                .setContentTitle("Feedback reminder")
                .setContentText("Send your feedback about your Thermal Comfort!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager= NotificationManagerCompat.from(context);
        notificationManager.notify(200,builder.build());

    }
}
