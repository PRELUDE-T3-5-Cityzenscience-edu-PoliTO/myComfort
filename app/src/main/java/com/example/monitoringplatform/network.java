package com.example.monitoringplatform;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.application.recyclerviewproject.device_item;
import com.example.monitoringplatform.adapters.deviceAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class network extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<device_item> rList = new ArrayList<>();
    private String platform_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        setTitle("My Network");
        retrieveDevicesList();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    public void startView(){
        mRecyclerView=findViewById(R.id.Devices);
        mRecyclerView.setHasFixedSize(true);

        deviceAdapter mAdapter=new deviceAdapter(rList);

        // setting grid layout manager to implement grid view.
        // in this method '2' represents number of columns to be displayed in grid view.
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        /*
        mAdapter.setOnItemClickListener(new roomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openRoomSettings(position);

            }
        });

         */

    }
    public void retrieveDevicesList(){
        SharedPreferences currentdetails = network.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        platform_ID = currentdetails.getString("platform_ID", "");
        String room_ID = currentdetails.getString("room_ID", "");
        SharedPreferences userdetails = network.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Util.getPlatformInfo(profilesURL, platform_ID, "preferences"+'/'+room_ID, network.this, new Util.ResponseCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onRespSuccess(String result) throws JSONException {
                JSONObject obj = new JSONObject(result);
                List<String> devices_list= new ArrayList<>();

                JSONArray arrayDevices = obj.getJSONArray("devices");
                //Toast.makeText(network.this,arrayDevices.toString(),Toast.LENGTH_LONG).show();

                for (int i = 0, l = arrayDevices.length(); i < l; i++) {
                    devices_list.add(arrayDevices.getString(i));
                }
                SharedPreferences devicesdetails = network.this.getSharedPreferences("devicesdetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = devicesdetails.edit();
                Gson gson_out= new Gson();
                String json_out= gson_out.toJson(devices_list);
                editor.putString("devices", json_out);
                editor.commit();
                buildRoomsList();
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(network.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void buildRoomsList() throws JSONException {
        SharedPreferences devicesdetails = network.this.getSharedPreferences("devicesdetails", MODE_PRIVATE);
        Gson gsonID = new Gson();
        String jsonID = devicesdetails.getString("devices", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        List<String> devices = gsonID.fromJson(jsonID, typeID);
        for (int i = 0; i < devices.size(); i++) {
            JSONObject device = new JSONObject(devices.get(i));
            String name=device.getString("device_ID");
            JSONArray p = new JSONArray(device.getString("parameters"));
            String param=getParams(p);
            double last=device.getDouble("last_update");
            LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
                    Double.valueOf(last).longValue(), 0, ZoneOffset.ofHours(2));
            Resources resources = network.this.getResources();
            int resourceId = resources.getIdentifier(name.toLowerCase(), "drawable",
                    network.this.getPackageName());
            rList.add(new device_item(resourceId,name,param,localDateTime.toString()));
        }
        startView();

    }
    public String getParams(JSONArray array) throws JSONException {
        List<String> output= new ArrayList<>();

        JSONObject parameter= new JSONObject(array.getString(0));
        output.add(parameter.getString("parameter")+" (" +parameter.getString("unit")+")");


        return output.toString().replace("[","").replace("]","");
    }

    @Override
    public boolean onSupportNavigateUp() {
        SharedPreferences devicesdetails = network.this.getSharedPreferences("devicesdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = devicesdetails.edit();
        editor.clear();
        editor.commit();
        finish();
        return true;
    }

}
