package com.example.monitoringplatform.add_platform;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.monitoringplatform.AppSingleton;
import com.example.monitoringplatform.R;
import com.example.monitoringplatform.Util;
import com.example.monitoringplatform.homepage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class new_platform_form_location extends AppCompatActivity {
    private String platform_name;
    private String plat_ID;
    private String platform_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_platform_form_location);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras.containsKey("platform_ID")) {
            plat_ID = i.getStringExtra("platform_ID");
        }
        if(extras.containsKey("platform_name")) {
            platform_name = i.getStringExtra("platform_name");
        }

        Button next=findViewById(R.id.button_commit);
        TextView enterPlatform=findViewById(R.id.newplatformLocation);
        enterPlatform.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                next.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                enableSubmitIfReady(enterPlatform,next);


            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                platform_location=enterPlatform.getText().toString();
                List myList = new ArrayList();
                SharedPreferences userdetails = new_platform_form_location.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                String profilesURL=userdetails.getString("profilesURL","");
                String clientsURL=userdetails.getString("clientsURL","");
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("platform_ID", plat_ID);
                    jsonBody.put("platform_name", platform_name);
                    jsonBody.put("location", platform_location);
                    jsonBody.put("inactive_time", 1800);
                    jsonBody.put("preferences",myList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(new_platform_form_location.this,plat_ID+" " +platform_name+" " + platform_location,Toast.LENGTH_SHORT).show();
                try {
                    Util.putElement(new_platform_form_location.this,profilesURL,"/insertProfile/","",jsonBody, new Util.PutCallback() {
                        @Override
                        public void onResponseSuccess(JSONObject result) throws JSONException {
                            String username=userdetails.getString("username","");
                            String password=userdetails.getString("password","");

                            JSONObject obj = new JSONObject();
                            obj.put("platform_ID",plat_ID);
                            putPlatformClient(clientsURL,"/insertProfile",obj,username,password, new Util.LoginCallback() {
                                @Override
                                public void onLoginSuccess(JSONObject result) {
                                    try {
                                        if(result.getBoolean("result")){
                                            Gson gsonDict = new Gson();
                                            String jsonDict = userdetails.getString("platforms_dict", "");
                                            Type typeDict = new TypeToken<Map<String,String>>() {
                                            }.getType();
                                            Map<String,String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
                                            platforms_dict.put(plat_ID,platform_name);
                                            Gson gson_out= new Gson();
                                            String json_out= gson_out.toJson(platforms_dict);
                                            SharedPreferences.Editor editor = userdetails.edit();
                                            editor.putString("platforms_dict", json_out);
                                            editor.apply();

                                            openNew();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onLoginError(String result) {
                                    Toast.makeText(new_platform_form_location.this,result,Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                        @Override
                        public void onResponseError(String result) {
                            Toast.makeText(new_platform_form_location.this,result,Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void openNew(){
        Intent intent=new Intent(getApplicationContext(), homepage.class);
        startActivity(intent);
        finish();
    }
    public void enableSubmitIfReady(TextView enterPlatform, Button next) {

        boolean isReady = enterPlatform.getText().toString().length() > 0;
        next.setEnabled(isReady);
    }
    public void putPlatformClient(String url, String uri, JSONObject obj,String username, String pass, final Util.LoginCallback putCallback) {
        String final_url=Util.setURL(url,uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.PUT, final_url, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (putCallback != null) {
                            putCallback.onLoginSuccess(response);
                        }
                    }
                },  new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    if (putCallback != null) {
                        putCallback.onLoginError(error.getMessage());
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String credentials = username+ ":" + pass;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                return headers;
            }

        };
        AppSingleton.getInstance(new_platform_form_location.this).addToRequestQueue(JSONreq);
    }
}