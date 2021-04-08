package com.example.monitoringplatform;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView get_response_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button get_request_button=findViewById(R.id.get_data);
        get_response_text=findViewById(R.id.get_respone_data);
        get_request_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendGetRequest();

            }
        });
        Button dashboard_button=findViewById(R.id.dashboard);
        dashboard_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent=new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);

            }
        });
    }


    private void sendGetRequest() {
        //get working now
        //let's try post and send some data to server
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "http://192.168.1.130:8083/Monitoring-Platform/server/MP-A00003";
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        get_response_text.setText(response.toString());
                    }
                },  new Response.ErrorListener()  {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        get_response_text.setText("Failed");
                        Log.e("error", "Error at sign in : " + error.getMessage());

                    }
                });

        queue.add(JSONreq);
    }
}