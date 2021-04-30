package com.example.monitoringplatform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.recyclerviewproject.roomOverview_item;
import com.example.monitoringplatform.adapters.roomOverviewAdapter;
import com.example.monitoringplatform.preferences.notification;
import com.example.monitoringplatform.preferences.profile_settings;
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
    private roomOverviewAdapter mAdapter;
    private ArrayList<roomOverview_item> rList = new ArrayList<>();
    private String platform_ID;
    private List<String> rooms= new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("0.00");
    private Map<String, String> rooms_dict = new HashMap<String, String>();
    private TextView nodata;
    private ProgressBar loadingProgressBar;
    int myFlag=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        SharedPreferences currentdetails = getSharedPreferences("currentdetails", MODE_PRIVATE);
        String title=currentdetails.getString("platform_name","");
        setTitle(title);
        loadingProgressBar= findViewById(R.id.loading2);
        loadingProgressBar.setVisibility(View.GONE);
        onRestart();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nodata=findViewById(R.id.NoData);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayoutOverview);
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
                            case R.id.mysettings:
                                Intent intent_network=new Intent(overview.this, notification.class);
                                startActivity(intent_network);
                                break;

                        }
                        return false;
                    }
                });
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        onRestart();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


    }
    @Override
    protected void onRestart() {
        super.onRestart();
        loadingProgressBar.setVisibility(View.VISIBLE);
        checkRoomInfo();

    }

    public void openRoomOverview(int position){
        String room_name = rList.get(position).getRoom();
        String room_ID = null;
        SharedPreferences userdetails = overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("rooms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> rooms_dict_old = gsonDict.fromJson(jsonDict, typeDict);
        for (Map.Entry<String, String> entry : rooms_dict_old.entrySet()) {
            if(entry.getKey().equals(room_name)){
                room_ID=entry.getValue();
                break;
            }
        }
        Util.saveData(overview.this,"currentdetails","room_ID",room_ID);
        Util.saveData(overview.this,"currentdetails","room_name",room_name);
        Intent intent= new Intent(overview.this, complete_overview.class);
        startActivity(intent);


    }
    public void startView(){
        mRecyclerView=findViewById(R.id.Rooms_overview);
        mRecyclerView.setHasFixedSize(true);

        mAdapter=new roomOverviewAdapter(rList);

        // setting grid layout manager to implement grid view.
        // in this method '2' represents number of columns to be displayed in grid view.
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        loadingProgressBar.setVisibility(View.GONE);
        mAdapter.setOnItemClickListener(new roomOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openRoomOverview(position);


            }
        });

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
    public void checkRoomInfo(){
        SharedPreferences currentdetails = overview.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Util.getPlatformInfo(profilesURL, platform_ID, "preferences", overview.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                List<String> roomsList = new ArrayList<>();
                List<String> rooms_nameList = new ArrayList<>();
                JSONArray array = new JSONArray(result);
                Map<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    roomsList.add(object.getString("room_ID"));
                    rooms_nameList.add(object.getString("room_name"));
                    map.put(object.getString("room_name"),object.getString("room_ID"));
                }
                SharedPreferences.Editor editor = userdetails.edit();
                Gson gson_out= new Gson();
                String json_out= gson_out.toJson(roomsList);
                editor.putString("rooms_ID", json_out);
                editor.commit();
                Gson gson_out2= new Gson();
                String json_out2= gson_out2.toJson(rooms_nameList);
                editor.putString("rooms_name", json_out2);
                editor.commit();
                Gson gson_out3= new Gson();
                String json_out3= gson_out3.toJson(map);
                editor.putString("rooms_dict", json_out3);
                editor.commit();
                createDict();
                retrieveRoomsList();
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(overview.this,result,Toast.LENGTH_SHORT).show();

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
        Gson gson_out= new Gson();
        String json_out= gson_out.toJson(rooms_dict);
        SharedPreferences.Editor editor = userdetails.edit();
        editor.putString("rooms_dict_ID", json_out);
        editor.commit();
    }
    public static String computePMV(Float pmv){
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
        rList.clear();
        SharedPreferences currentdetails = overview.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Util.getPlatformInfo(serverURL, platform_ID, "rooms", overview.this, new Util.ResponseCallback() {

            @Override
            public void onRespSuccess(String result) throws JSONException {
                nodata.setVisibility(View.GONE);
                JSONArray array = new JSONArray( result.replace("/","."));
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
                nodata.setVisibility(View.VISIBLE);
                //Toast.makeText(overview.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}