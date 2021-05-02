package com.example.monitoringplatform;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class mqtt_sub extends Service {
    private final MemoryPersistence persistence = new MemoryPersistence();
    private NotificationManagerCompat notificationManager;
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    @Override
    public void onCreate() {
        super.onCreate();
        mqttConnectOptions = createMqttConnectOptions();
        mqttAndroidClient = createMqttAndroidClient();
    }

    private MqttConnectOptions createMqttConnectOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        SharedPreferences userdetails = mqtt_sub.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String clientID=userdetails.getString("username","AndroidClient");
        String brokerURL=userdetails.getString("brokerURL","");

        mqttAndroidClient = new MqttAndroidClient(this, brokerURL, clientID, persistence);
        return mqttAndroidClient;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        System.out.println("Services started");
        connect(mqttAndroidClient,mqttConnectOptions);

        try{
            String topic=intent.getStringExtra("platform_ID");
            Boolean subFlag=intent.getBooleanExtra("subscribe",false);
            if(subFlag){
                subscribeToTopic(topic);
                Toast.makeText(this, "Warnings are now enabled", Toast.LENGTH_SHORT).show();
            }
            else{
                unSubscribeToTopic(topic);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }



        return START_STICKY;
    }

    private MqttAndroidClient connect(final MqttAndroidClient mqttAndroidClient, MqttConnectOptions mqttConnectOptions) {
        if(!mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("Connection was lost!");
                        Toast.makeText(mqtt_sub.this, "Connection lost", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                        //Toast.makeText(mqtt_sub.this,"Message Arrived!: " + topic + ": " + new String(message.getPayload()),Toast.LENGTH_SHORT).show();
                        JSONObject notif = new JSONObject(new String(message.getPayload()));
                        String platform_ID = notif.getString("platform_ID");
                        String room_name = notif.getString("room_name");
                        String message_text = notif.getString("message");
                        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
                        Gson gsonDict = new Gson();
                        String jsonDict = userdetails.getString("platforms_dict", "");
                        Type typeDict = new TypeToken<Map<String, String>>() {
                        }.getType();
                        Map<String, String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
                        String platform_name = platforms_dict.get(platform_ID);


                        notificationManager = NotificationManagerCompat.from(mqtt_sub.this);
                        Intent notificationIntent = new Intent(mqtt_sub.this, overview.class);

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent intent = PendingIntent.getActivity(mqtt_sub.this, 0,
                                notificationIntent, 0);

                        Notification notification = new NotificationCompat.Builder(mqtt_sub.this, "alerting")
                                .setSmallIcon(R.drawable.ic_round_fmd_bad_24)
                                .setContentTitle("Alerting: " + platform_name + " - " + room_name)
                                .setContentText(message_text)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setContentIntent(intent)
                                .build();
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(200, notification);


                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        System.out.println("Connection Success!");

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        //subscribeToTopic(topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        System.out.println("Connection Failure!");
                        System.out.println("throwable: " + exception.toString());
                    }

                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return mqttAndroidClient;

    }

    private void subscribeToTopic(String topic){

            try {
                mqttAndroidClient.subscribe("warning/"+topic+"/#",2);
                System.out.println("Subscribed to /warning/" + topic + "/#");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    private void unSubscribeToTopic(String topic){
        try {
            mqttAndroidClient.unsubscribe("warning/" + topic + "/#");
            System.out.println("Unsubscribed to /warning/" + topic + "/#");
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service destroyed by user.");
    }
    public static void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name="alertingChannel";
            String description="Alerting channel";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("alerting",name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}