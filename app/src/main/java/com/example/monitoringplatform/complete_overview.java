package com.example.monitoringplatform;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.recyclerviewproject.parameter_item;
import com.example.application.recyclerviewproject.roomOverview_item;
import com.example.monitoringplatform.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;


public class complete_overview extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private parameterAdapter mAdapter;
    private ArrayList<parameter_item> rList = new ArrayList<>();
    private String room_name;
    private String room_ID;
    private String platform_ID;
    private Float MRT;
    private TextView clothing_value;
    private TextView met_value;
    private TextView PMV_value;
    private TextView PPD_value;
    private TextView PMV_text;
    private ImageView PMV_image;
    private static DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_overview);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayoutOverviewRoom);
        TextView title_room= findViewById(R.id.myroom);
        setTitle("My Overview");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        clothing_value= findViewById(R.id.clovalue);
        met_value= findViewById(R.id.metvalue);
        PMV_value= findViewById(R.id.PMVvalue);
        PMV_text= findViewById(R.id.PMVtext);
        PPD_value= findViewById(R.id.PPDvalue);
        PMV_image=findViewById(R.id.PMVstatus);

        SharedPreferences currentdetails = complete_overview.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        room_name = currentdetails.getString("room_name", "");
        room_ID = currentdetails.getString("room_ID", "");
        platform_ID = currentdetails.getString("platform_ID", "");
        getInfo();

        title_room.setText(room_name);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        onRestart();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView_overview);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.room_settings:
                                Intent intent_settings=new Intent(complete_overview.this, rooms_settings.class);
                                startActivity(intent_settings);
                                break;
                            case R.id.graph:
                                Intent intent_tips=new Intent(complete_overview.this, MainActivity2.class);
                                startActivity(intent_tips);
                                break;
                            case R.id.network:
                                Intent intent_network=new Intent(complete_overview.this, network.class);
                                startActivity(intent_network);
                                break;

                        }
                        return false;
                    }
                });



    }
    public void startView(){
        mRecyclerView=findViewById(R.id.parameter_view);
        mRecyclerView.setHasFixedSize(true);

        mAdapter=new parameterAdapter(rList);

        // setting grid layout manager to implement grid view.
        // in this method '2' represents number of columns to be displayed in grid view.
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);


    }
    @Override
    protected void onRestart() {
        super.onRestart();
        rList.clear();
        getInfo();

    }
    public void setValue(Float clo, Float met,Float pmv,Float ppd){
        clothing_value.setText(String.valueOf(clo)+" clo");
        met_value.setText(String.valueOf(met)+" met");
        PMV_value.setText(String.valueOf(df.format(pmv)));
        PPD_value.setText(String.valueOf(df.format(ppd))+"%");
        String status=overview.computePMV(pmv);
        setcolor(status);
        PMV_text.setText(status);
        setImage(pmv);

    }
    public void setcolor(String pmv){
        if (pmv.equals("WARM")){
            PMV_text.setTextColor(Color.parseColor("#FFFF9800"));
            PMV_value.setTextColor(Color.parseColor("#FFFF9800"));

        }else if (pmv.equals("GOOD")){
            PMV_text.setTextColor(Color.parseColor("#5CC615"));
            PMV_value.setTextColor(Color.parseColor("#5CC615"));
        }else if(pmv.equals("HOT")){
            PMV_text.setTextColor(Color.parseColor("#FFE91E63"));
            PMV_value.setTextColor(Color.parseColor("#FFE91E63"));

        }
        else if(pmv.equals("COOL")){
            PMV_text.setTextColor(Color.parseColor("#FF01BCAA"));
            PMV_value.setTextColor(Color.parseColor("#FF01BCAA"));

        }
        else if(pmv.equals("COLD")){
            PMV_text.setTextColor(Color.parseColor("#FF0146BC"));
            PMV_value.setTextColor(Color.parseColor("#FF0146BC"));

        }

    }
    public void setImage(Float pmv){
        if(pmv<-0.5){
            PMV_image.setImageResource(R.drawable.cold_icon);
        }else if(pmv>0.5){
            PMV_image.setImageResource(R.drawable.hot_icon);
        }else{
            PMV_image.setImageResource(R.drawable.good_icon);
        }
    }
    public  int setIcon(String name){
        if(name.equals("temperature")){
            return R.drawable.ic_baseline_thermostat_24;
        }else if(name.equals("humidity")){
            return R.drawable.ic_baseline_humidity;

        }else if(name.equals("MRT")){
            return R.drawable.ic_round_blur_on_24;

        }else if(name.equals("wind")){
            return R.drawable.ic_baseline_air_24;
        }else if(name.equals("emissivity")){
            return R.drawable.ic_emissivity;
        }else if(name.equals("diameter")){
            return R.drawable.ic_diameter;
        }
        else{
            return R.drawable.rasp;
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createList(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONArray parameters = (array.getJSONObject(i).getJSONArray("parameters"));

            for (int j = 0; j < parameters.length(); j++){
                String param_name;
                Float param_value;
                String param_unit;
                String timestamp;
                if(parameters.getJSONObject(j).getString("parameter").equals("temperature_g")){
                    param_name="MRT";
                    param_value=MRT;
                }else{
                    param_name=parameters.getJSONObject(j).getString("parameter");
                    param_value=(BigDecimal.valueOf(parameters.getJSONObject(j).getDouble("value")).floatValue());

                }
                if(parameters.getJSONObject(j).getString("unit").equals("null")){
                    param_unit="";
                }else {
                    param_unit = parameters.getJSONObject(j).getString("unit");
                }
                int icon=setIcon(param_name);
                if(parameters.getJSONObject(j).has("timestamp")){
                    double tmp=parameters.getJSONObject(j).getDouble("timestamp");
                    timestamp = LocalDateTime.ofEpochSecond(Double.valueOf(tmp).longValue(), 0, ZoneOffset.ofHours(2)).toString();
                }
                else{
                    timestamp="";
                }
                rList.add(new parameter_item(icon,param_name,String.valueOf(df.format(param_value))+param_unit,timestamp));


            }
        }
        startView();



    }
    public void getInfo(){
        SharedPreferences userdetails = complete_overview.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Util.getPlatformInfo(serverURL, platform_ID, room_ID, complete_overview.this, new Util.ResponseCallback(){

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onRespSuccess(String result) throws JSONException {
                JSONObject object = new JSONObject(result.replace("/","."));
                Float clo=(BigDecimal.valueOf(object.getDouble("Icl_clo")).floatValue());
                Float met=(BigDecimal.valueOf(object.getDouble("M_met")).floatValue());
                Float PMV=(BigDecimal.valueOf(object.getDouble("PMV")).floatValue());
                Float PPD=(BigDecimal.valueOf(object.getDouble("PPD")).floatValue());
                setValue(clo,met,PMV,PPD);

                MRT=(BigDecimal.valueOf(object.getDouble("MRT")).floatValue());
                JSONArray devices=(object.getJSONArray("devices"));
                createList(devices);

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(complete_overview.this,result,Toast.LENGTH_SHORT).show();

            }
        });


    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}