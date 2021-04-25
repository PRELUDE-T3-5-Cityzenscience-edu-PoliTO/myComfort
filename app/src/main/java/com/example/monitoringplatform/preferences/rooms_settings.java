package com.example.monitoringplatform.preferences;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.application.recyclerviewproject.room_item;
import com.example.monitoringplatform.R;
import com.example.monitoringplatform.Util;
import com.example.monitoringplatform.adapters.roomAdapter;
import com.example.monitoringplatform.myfragments.RoomsSettingsFragment;
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveRoomsList();

    }
    public void removeItem(int position){
        rList.remove(position);
        mAdapter.notifyItemRemoved(position);
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

            @Override
            public void onDeleteClick(int position) {
                deleteDialog(position);
            }
        });

    }
    public void deleteDialog(int position){
        String room_ID = rList.get(position).getText2();
        String room_name = rList.get(position).getText1();
        AlertDialog.Builder builder = new AlertDialog.Builder(rooms_settings.this,R.style.MyAlertDialog);
        builder.setTitle("Delete Room").
                setMessage("Are you sure you want to delete "+room_name+"?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences userdetails = rooms_settings.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                        String profilesURL=userdetails.getString("profilesURL","");
                        try {
                            Util.deleteElement(rooms_settings.this, profilesURL, "/removeRoom/", platform_ID+'/'+room_ID, new Util.DeleteCallback() {
                                @Override
                                public void onRespSuccess(JSONObject result) throws JSONException {
                                    if(result.getBoolean("result")){
                                        removeItem(position);
                                        Gson gsonDict = new Gson();
                                        String jsonDict = userdetails.getString("rooms_dict_ID", "");
                                        Type typeDict = new TypeToken<Map<String, String>>() {
                                        }.getType();
                                        Map<String, String> rooms_dict_ID = gsonDict.fromJson(jsonDict, typeDict);
                                        rooms_dict_ID.remove(room_ID);
                                        Gson gson_out = new Gson();
                                        String json_out = gson_out.toJson(rooms_dict_ID);

                                        Gson gsonDict2 = new Gson();
                                        String jsonDict2 = userdetails.getString("rooms_dict", "");
                                        Type typeDict2 = new TypeToken<Map<String, String>>() {
                                        }.getType();
                                        Map<String, String> rooms_dict = gsonDict2.fromJson(jsonDict2, typeDict2);
                                        rooms_dict.remove(room_name);
                                        Gson gson_out2 = new Gson();
                                        String json_out2 = gson_out2.toJson(rooms_dict);


                                        SharedPreferences.Editor editor = userdetails.edit();
                                        editor.putString("rooms_dict_ID", json_out);
                                        editor.putString("rooms_dict", json_out);
                                        editor.apply();
                                    }

                                }

                                @Override
                                public void onRespError(String result) {
                                    Toast.makeText(rooms_settings.this,result,Toast.LENGTH_SHORT).show();

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
                createDict();
                createList();
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(rooms_settings.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }
    public void createDict(){
        SharedPreferences userdetails = rooms_settings.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("rooms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> rooms_dict_old = gsonDict.fromJson(jsonDict, typeDict);
        Map<String, String> rooms_dict = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : rooms_dict_old.entrySet()) {
            rooms_dict.put(entry.getValue(),entry.getKey());
        }
        Gson gson_out= new Gson();
        String json_out= gson_out.toJson(rooms_dict);
        SharedPreferences.Editor editor = userdetails.edit();
        editor.putString("rooms_dict_ID", json_out);
        editor.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
