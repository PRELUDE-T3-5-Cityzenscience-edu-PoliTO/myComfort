package com.example.monitoringplatform;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

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

public class my_platforms extends AppCompatActivity {
    private ArrayList<room_item> rList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private roomAdapter mAdapter;
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

    public void startView(){
        mRecyclerView=findViewById(R.id.recyclerView_platforms);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(this);
        mAdapter=new roomAdapter(rList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }
    public void getPlatforms(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String serverURL=userdetails.getString("serverURL","");
        Gson gsonID = new Gson();
        String jsonID = userdetails.getString("platforms", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        List<String> platforms_ID = gsonID.fromJson(jsonID, typeID);
        int platformsSize=platforms_ID.size();
        for (int i = 0; i < platforms_ID.size(); i++) {
            int finalI = i;
            Util.getPlatformInfo(serverURL, platforms_ID.get(i), "creation_date", my_platforms.this, new Util.ResponseCallback() {
                @Override
                public void onRespSuccess(String result) throws JSONException {
                    map.put(platforms_ID.get(finalI),result);
                    mflag++;
                    buildList(platformsSize);
                }

                @Override
                public void onRespError(String result) {
                    Toast.makeText(my_platforms.this,result,Toast.LENGTH_SHORT).show();

                }
            });
        }


    }
    public void buildList(int platformsSize){
        if (mflag==platformsSize){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
                Gson gsonID = new Gson();
                String jsonID = userdetails.getString("platforms", "");
                Type typeID = new TypeToken<List<String>>() {
                }.getType();
                List<String> platformsList = gsonID.fromJson(jsonID, typeID);
                int position = platformsList.indexOf(entry.getKey());
                Gson gson = new Gson();
                String jsonN = userdetails.getString("platforms_name", "");
                Type type = new TypeToken<List<String>>() {
                }.getType();
                List<String> platforms_nameList = gson.fromJson(jsonN, type);
                rList.add(new room_item(R.drawable.ic_platform, platforms_nameList.get(position), entry.getKey()+"\n"+"Creation date: "+entry.getValue()));
            }
            startView();

        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
