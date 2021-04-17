package com.example.monitoringplatform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.application.recyclerviewproject.device_item;
import com.example.application.recyclerviewproject.roomOverview_item;
import com.example.application.recyclerviewproject.room_item;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class overview extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ArrayList<roomOverview_item> rList = new ArrayList<>();
    private String platform_ID;
    private List<String> rooms= new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("0.00");
    private Map<String, String> rooms_dict = new HashMap<String, String>();
    int myFlag=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        SharedPreferences currentdetails = getSharedPreferences("currentdetails", MODE_PRIVATE);
        String title=currentdetails.getString("platform_name","");
        createDict();
        setTitle(title);
        retrieveRoomsList();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.person:
                                Intent intent_prof=new Intent(overview.this, profile_settings.class);
                                startActivity(intent_prof);
                                break;
                            case R.id.tips:
                                Intent intent_tips=new Intent(overview.this, tips.class);
                                startActivity(intent_tips);
                                break;
                            case R.id.network:
                                Intent intent_network=new Intent(overview.this, network.class);
                                startActivity(intent_network);
                                break;

                        }
                        return false;
                    }
                });


    }
    public void createDict(){
        SharedPreferences userdetails = overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("rooms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> rooms_dict_old = gsonDict.fromJson(jsonDict, typeDict);
        for (Map.Entry<String, String> entry : rooms_dict_old.entrySet()) {
            rooms_dict.put(entry.getValue(),entry.getKey());
        }
    }
    public void startView(){
        mRecyclerView=findViewById(R.id.Rooms_overview);
        mRecyclerView.setHasFixedSize(true);

        roomOverviewAdapter mAdapter=new roomOverviewAdapter(rList);

        // setting grid layout manager to implement grid view.
        // in this method '2' represents number of columns to be displayed in grid view.
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }
    public void buildRoom(JSONArray mylist,String room_ID,Float pmv) throws JSONException {
        String room=null;
        myFlag--;
        if(myFlag==0){
            String temp = null;
            String hum=null;
            String wind=null;
            for (int i = 0; i < mylist.length(); i++) {
                if(mylist.getJSONObject(i).getString("parameter").equals("temperature")){
                    String temp_value= String.valueOf(df.format(BigDecimal.valueOf(mylist.getJSONObject(i).getDouble("value")).floatValue()));
                    String temp_unit=mylist.getJSONObject(i).getString("unit");
                    temp=temp_value+"°"+temp_unit;

                }
                if(mylist.getJSONObject(i).getString("parameter").equals("humidity")){
                    String hum_value= String.valueOf(df.format(BigDecimal.valueOf(mylist.getJSONObject(i).getDouble("value")).floatValue()));
                    String hum_unit=mylist.getJSONObject(i).getString("unit");
                    hum=hum_value+hum_unit;

                }
                if(mylist.getJSONObject(i).getString("parameter").equals("wind")){
                    String wind_value= String.valueOf(df.format(BigDecimal.valueOf(mylist.getJSONObject(i).getDouble("value")).floatValue()));
                    String wind_unit=mylist.getJSONObject(i).getString("unit");
                    wind=wind_value+" "+wind_unit;

                }

            }
            for (Map.Entry<String, String> entry : rooms_dict.entrySet()) {
                if(entry.getKey().equals(room_ID)) {
                    room = entry.getValue();
                }
            }
            String PMV=computePMV(pmv);
            rList.add(new roomOverview_item(R.drawable.ic_baseline_room_preferences_24,
                    R.drawable.ic_baseline_thermostat_24,R.drawable.ic_baseline_humidity,
                    R.drawable.ic_baseline_air_24,room,temp,hum,wind,PMV));
            myFlag=3;
            startView();

        }


    }
    public String computePMV(Float pmv){
        if(pmv>=-0.5 && pmv<=0.5){
            return("GOOD");
        }else if(pmv>0.5 && pmv<=2){
            return("WARM");
        }else if(pmv>2){
            return("HOT");
        }else if(pmv<-0.5 && pmv>=-2){
            return("COOL");
        }else if(pmv<-2){
            return("COLD");
        }else{
            return "NONE";
        }
    }

    public void retrieveRoomsList(){
        SharedPreferences currentdetails = overview.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Util.getPlatformInfo(serverURL, platform_ID, "rooms", overview.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                JSONArray array = new JSONArray(result);
                Map<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONArray resultList = new JSONArray();
                    JSONObject object = array.getJSONObject(i);
                    String room_ID=object.getString("room_ID");
                    rooms.add(room_ID);
                    Float pmv=(BigDecimal.valueOf(object.getDouble("PMV")).floatValue());
                    Util.getPlatformInfo(serverURL, platform_ID, room_ID+"?parameter=temperature", overview.this, new Util.ResponseCallback(){

                        @Override
                        public void onRespSuccess(String result) throws JSONException {
                            JSONObject obj = new JSONObject(result);
                            resultList.put(obj);
                            buildRoom(resultList,room_ID,pmv);

                        }

                        @Override
                        public void onRespError(String result) {
                            Toast.makeText(overview.this,result+"temperature error",Toast.LENGTH_SHORT).show();

                        }
                    });
                    Util.getPlatformInfo(serverURL, platform_ID, object.getString("room_ID")+"?parameter=humidity", overview.this, new Util.ResponseCallback(){

                        @Override
                        public void onRespSuccess(String result) throws JSONException {
                            JSONObject obj = new JSONObject(result);
                            resultList.put(obj);
                            buildRoom(resultList,room_ID,pmv);

                        }

                        @Override
                        public void onRespError(String result) {
                            Toast.makeText(overview.this,result+"humidity error",Toast.LENGTH_SHORT).show();

                        }
                    });
                    Util.getPlatformInfo(serverURL, platform_ID, object.getString("room_ID")+"?parameter=wind", overview.this, new Util.ResponseCallback(){

                        @Override
                        public void onRespSuccess(String result) throws JSONException {
                            JSONObject obj = new JSONObject(result);
                            resultList.put(obj);
                            buildRoom(resultList,room_ID,pmv);

                        }

                        @Override
                        public void onRespError(String result) {
                            Toast.makeText(overview.this,result+ "wind error",Toast.LENGTH_SHORT).show();

                        }
                    });



                }
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(overview.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}