package com.example.monitoringplatform;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.application.recyclerviewproject.platform_item;
import com.example.monitoringplatform.adapters.platformAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class my_platforms extends AppCompatActivity {
    private ArrayList<platform_item> rList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private platformAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Map<String, String> map = new HashMap<String, String>();
    private int mflag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_platforms);
        getPlatforms();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    public void removeItem(int position){
        rList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    public void startView(){
        mRecyclerView=findViewById(R.id.recyclerView_platforms);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(this);
        mAdapter=new platformAdapter(rList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new platformAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                rebootDialog(position);

            }

            @Override
            public void onDeleteClick(int position) {
                deleteDialog(position);

            }
        });

    }
    public void rebootDialog(int position){
        String[] result =rList.get(position).getText2().split("\n",2);
        String plat_ID=result[0];
        String plat_name = rList.get(position).getText1();
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Util.getPlatformInfo(serverURL, plat_ID, "local_IP", my_platforms.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                //Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(my_platforms.this);
                builder.setTitle(plat_name);
                builder.setMessage("You can reboot or stop your platform if you need...\n" +
                        "Please, make sure you are in the same local network.");

                // add the buttons
                builder.setPositiveButton("Reboot", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCommand(result, "/reboot", new CommandCallback() {
                            @Override
                            public void onRespSuccess(JSONObject result) throws JSONException {
                                Toast.makeText(my_platforms.this,"Rebooting...",Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onRespError(String result) {
                                Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Power off", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCommand(result, "/poweroff", new CommandCallback() {
                            @Override
                            public void onRespSuccess(JSONObject result) throws JSONException {
                                //Toast.makeText(my_platforms.this,"Shutdown...",Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onRespError(String result) {
                                //Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

            }
        });



    }
    public void deleteDialog(int position){
        String[] result =rList.get(position).getText2().split("\n",2);
        String plat_ID=result[0];
        String plat_name = rList.get(position).getText1();

        AlertDialog.Builder builder = new AlertDialog.Builder(my_platforms.this,R.style.MyAlertDialog);
        builder.setTitle("Delete Platform").
                setMessage("Are you sure you want to delete "+plat_name+"?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences userdetails = my_platforms.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                        String profilesURL=userdetails.getString("profilesURL","");
                        String clientsURL=userdetails.getString("clientsURL","");
                        try {
                            Util.deleteElement(my_platforms.this, profilesURL, "/removeProfile/", plat_ID, new Util.DeleteCallback() {
                                @Override
                                public void onRespSuccess(JSONObject result) throws JSONException {
                                    if(result.getBoolean("result")) {

                                        String username=userdetails.getString("username","");
                                        String password=userdetails.getString("password","");
                                        deletePlatformClient(clientsURL,"/removePlatform/"+plat_ID,username,password, new Util.LoginCallback() {
                                            @Override
                                            public void onLoginSuccess(JSONObject result) {
                                                try {
                                                    if (result.getBoolean("result")){
                                                        removeItem(position);
                                                        Gson gsonDict = new Gson();
                                                        String jsonDict = userdetails.getString("platforms_dict", "");
                                                        Type typeDict = new TypeToken<Map<String, String>>() {
                                                        }.getType();
                                                        Map<String, String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
                                                        platforms_dict.remove(plat_ID);
                                                        Gson gson_out = new Gson();
                                                        String json_out = gson_out.toJson(platforms_dict);
                                                        SharedPreferences.Editor editor = userdetails.edit();
                                                        editor.putString("platforms_dict", json_out);
                                                        editor.apply();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                            @Override
                                            public void onLoginError(String result) {
                                                Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }

                                }

                                @Override
                                public void onRespError(String result) {
                                    Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder.create();
        alert11.show();


    }
    public void getPlatforms(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("platforms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
        int platformsSize=platforms_dict.size();
        for (Map.Entry<String, String> entry : platforms_dict.entrySet()) {
            Util.getPlatformInfo(serverURL, entry.getKey(), "creation_date", my_platforms.this, new Util.ResponseCallback() {
                @Override
                public void onRespSuccess(String result) throws JSONException {
                    map.put(entry.getKey(),result);
                    mflag++;
                    buildList(platformsSize);
                }

                @Override
                public void onRespError(String result) {
                    //Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();
                    map.put(entry.getKey(),"None");
                    mflag++;
                    buildList(platformsSize);
                }
            });
        }


    }
    public void buildList(int platformsSize){
        if (mflag==platformsSize){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
                Gson gsonDict = new Gson();
                String jsonDict = userdetails.getString("platforms_dict", "");
                Type typeDict = new TypeToken<Map<String,String>>() {
                }.getType();
                Map<String,String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);

                rList.add(new platform_item(R.drawable.ic_platform, platforms_dict.get(entry.getKey()), entry.getKey()+"\n"+"Creation date: "+entry.getValue()));
            }
            startView();

        }
    }
    public void deletePlatformClient(String url, String uri,String username, String pass, final Util.LoginCallback delCallback) {
        String final_url=Util.setURL(url,uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.DELETE, final_url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (delCallback != null) {
                            delCallback.onLoginSuccess(response);
                        }
                    }
                },  new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    if (delCallback != null) {
                        delCallback.onLoginError(error.getMessage());
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
        AppSingleton.getInstance(my_platforms.this).addToRequestQueue(JSONreq);
    }
    public void sendCommand(String url,String command, final CommandCallback commandCallback){
        String final_url=url+command;
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.GET, final_url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (commandCallback != null) {
                            try {
                                commandCallback.onRespSuccess(response);
                            } catch (JSONException e) {
                                //e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (commandCallback != null) {
                    commandCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(my_platforms.this).addToRequestQueue(JSONreq);

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    public interface CommandCallback {

        void onRespSuccess(JSONObject result) throws JSONException;

        void onRespError(String result);

    }
}
