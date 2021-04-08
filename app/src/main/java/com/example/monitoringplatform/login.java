package com.example.monitoringplatform;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class login extends AppCompatActivity {

    private String apiURL=null;
    private String clientsURI=null;
    private String clientsURL;
    private String profilesURI=null;
    private String profilesURL;
    private String user_ID;
    private List<String> platforms= new ArrayList<>();
    private String inputName;
    private String inputPass;
    private String serverURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //List<String> myist= new ArrayList<>();
        //Util.saveData(login.this,"userdetails","platforms_name",myist.toString().replace("[","").replace("]",""));
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.GONE);
        try {
            apiURL = Util.getProperty("service_catalog",login.this);
            clientsURI = Util.getProperty("clientsURI",login.this);
            profilesURI = Util.getProperty("profilesURI",login.this);
            serverURI = Util.getProperty("serverURI",login.this);

        } catch (IOException e) {
            e.printStackTrace();
        }
        loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                inputName=usernameEditText.getText().toString();
                inputPass=passwordEditText.getText().toString();
                if (inputName.isEmpty() || inputPass.isEmpty()){
                    Toast.makeText(login.this,"Please enter all details",Toast.LENGTH_SHORT).show();
                }else{
                    Util.getService(login.this,apiURL, clientsURI, "clientsURL", new Util.ServiceCallback() {
                        @Override
                        public void onReqSuccess(JSONObject result) {
                            SharedPreferences userdetails = login.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                            clientsURL=userdetails.getString("clientsURL","");
                            getLoginReq(clientsURL, "/login", inputName, inputPass, new Util.LoginCallback() {
                                @Override
                                public void onLoginSuccess(JSONObject result) {
                                    getServer();
                                    parseJSON(result);
                                    Toast.makeText(login.this,"Welcome back "+user_ID,Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(login.this, homepage.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onLoginError(String result) {
                                    Toast.makeText(login.this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
                                    loadingProgressBar.setVisibility(View.GONE);

                                }
                            });

                        }

                        @Override
                        public void onReqError(String result) {
                            Toast.makeText(login.this, "Service catalog failed!", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
        });
    }

    public void getServer(){
        Util.getService(login.this,apiURL, serverURI, "serverURL", new Util.ServiceCallback() {
            @Override
            public void onReqSuccess(JSONObject result) {

            }

            @Override
            public void onReqError(String result) {
                Toast.makeText(login.this,result,Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void parseJSON(JSONObject object){
        List<String> myList= new ArrayList<>();
        List<String> edited= new ArrayList<>();
        try {
            this.user_ID=object.getString("user_ID");
            JSONObject temp= new JSONObject(object.toString());
            JSONArray jsonArray = temp.getJSONArray("catalog_list");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject plat = jsonArray.getJSONObject(i);
                String plat_ID = plat.getString("catalog_ID");
                this.platforms.add(plat_ID);
                Util.getService(login.this,login.this.apiURL, profilesURI, "profilesURL", new Util.ServiceCallback() {
                    @Override
                    public void onReqSuccess(JSONObject result) {
                        SharedPreferences userdetails = login.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                        profilesURL=userdetails.getString("profilesURL","");
                        getPlatformName(profilesURL, plat_ID, new Util.ResponseCallback() {

                            @Override
                            public void onRespSuccess(String result) {
                                myList.add(result);
                                SharedPreferences sharedPrefs = login.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                Gson gsonN = new Gson();

                                String jsonN = gsonN.toJson(myList);

                                editor.putString("platforms_name", jsonN);
                                editor.commit();
                            }

                            @Override
                            public void onRespError(String result) {
                                Toast.makeText(login.this,result,Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onReqError(String result) {
                        Toast.makeText(login.this,result,Toast.LENGTH_SHORT).show();

                    }
                });


            }
            SharedPreferences sharedPrefs = login.this.getSharedPreferences("userdetails", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();

            Util.saveData(login.this,"userdetails","username",this.user_ID);
            Util.saveData(login.this,"userdetails","name",this.user_ID);
            Util.saveData(login.this,"userdetails","edited",edited.toString());
            Util.saveData(login.this,"currentdetails","platform_ID",this.platforms.get(0));
            Gson gson = new Gson();
            String json = gson.toJson(this.platforms);
            editor.putString("platforms", json);
            editor.commit();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public  void getPlatformName(String api,String uri,final Util.ResponseCallback responseCallback){
        String final_uri="/"+uri+"/platform_name";
        String final_url = Util.setURL(api, final_uri);
        StringRequest JSONreq = new StringRequest(Request.Method.GET, final_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onRespSuccess(response.replace("\"",""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(login.this).addToRequestQueue(JSONreq);

    }


    public void getLoginReq(String url, String uri, String username, String pass, final Util.LoginCallback loginCallback) {
        String final_url=Util.setURL(url,uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.GET, final_url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (loginCallback != null) {
                            loginCallback.onLoginSuccess(response);
                        }
                    }
                },  new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    if (loginCallback != null) {
                        loginCallback.onLoginError(error.getMessage());
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
        AppSingleton.getInstance(login.this).addToRequestQueue(JSONreq);
    }


}