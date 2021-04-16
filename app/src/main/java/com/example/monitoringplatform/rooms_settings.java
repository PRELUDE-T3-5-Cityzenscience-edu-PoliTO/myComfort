package com.example.monitoringplatform;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.application.recyclerviewproject.room_item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class rooms_settings extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private roomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> rooms= new ArrayList<>();
    private ArrayList<room_item> rList = new ArrayList<>();
    private String platform_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_settings);
        setTitle("Rooms setting");
        retrieveRoomsList();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
    public void openRoomSettings(int position){
        String room_ID = rList.get(position).getText2();
        String room_name = rList.get(position).getText1();
        Util.saveData(rooms_settings.this,"currentdetails","room_ID",room_ID);
        Util.saveData(rooms_settings.this,"currentdetails","room_name",room_name);
        setContentView(R.layout.activity_room_edit);
        setTitle("Edit preferences");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout2, new RoomsSettingsFragment())
                .commit();

    }
    public void startView(){
        mRecyclerView=findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(this);
        mAdapter=new roomAdapter(rList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new roomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openRoomSettings(position);

            }
        });

    }
    public void buildRoomsList(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonID = new Gson();
        String jsonID = userdetails.getString("rooms_ID", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        List<String> rooms_ID = gsonID.fromJson(jsonID, typeID);
        for (int i = 0; i < rooms.size(); i++) {
            rList.add(new room_item(R.drawable.ic_baseline_room_preferences_24, rooms.get(i), rooms_ID.get(i)));
        }
        startView();

    }
    public void createList(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonID = new Gson();
        String jsonID = userdetails.getString("rooms_name", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        rooms = gsonID.fromJson(jsonID, typeID);
        buildRoomsList();
    }
    public void retrieveRoomsList(){
        SharedPreferences currentdetails = rooms_settings.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = rooms_settings.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Util.getPlatformInfo(profilesURL, platform_ID, "preferences", rooms_settings.this, new Util.ResponseCallback() {
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
                createList();
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(rooms_settings.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
