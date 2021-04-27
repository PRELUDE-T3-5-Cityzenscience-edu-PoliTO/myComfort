package com.example.monitoringplatform;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monitoringplatform.add_platform.new_platform;
import com.example.monitoringplatform.add_room.new_room_form_name;
import com.example.monitoringplatform.preferences.notification;
import com.example.monitoringplatform.preferences.profile_settings;
import com.example.monitoringplatform.preferences.rooms_devices;
import com.example.monitoringplatform.ui.login.login;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class homepage extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private String APIKEY;
    public int Firstflag=0;
    Boolean isAllFabsVisible;
    ImageView conditionImage;
    TextView tempExt;
    TextView humExt;
    TextView windExt;
    TextView locationText;
    private String apiURL="https://api.openweathermap.org/data/2.5/weather";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        onRestart();
        // register all the ImageButtons with their appropriate IDs
        ImageButton backB = (ImageButton) findViewById(R.id.backB);
        ImageButton logOutB = (ImageButton) findViewById(R.id.logOutB);
        //ImageButton profileB = (ImageButton) findViewById(R.id.profileB);

        // register all the Buttons with their appropriate IDs
        Button myplatforms = findViewById(R.id.myplatforms);
        Button editProfileB = findViewById(R.id.editProfileB);
        conditionImage=findViewById(R.id.conditionImage);
        tempExt=findViewById(R.id.temperature_valueExt);
        humExt=findViewById(R.id.humidity_valueExt);
        windExt=findViewById(R.id.wind_valueExt);
        locationText=findViewById(R.id.city);

        //
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);

        // register all the card views with their appropriate IDs
        CardView myOverview = (CardView) findViewById(R.id.myOverview);
        CardView myGraphs = (CardView) findViewById(R.id.myGraphs);
        CardView myNetwork = (CardView) findViewById(R.id.myNetwork);
        CardView interestsCard = (CardView) findViewById(R.id.interestsCard);
        CardView helpCard = (CardView) findViewById(R.id.helpCard);
        CardView mySettings = (CardView) findViewById(R.id.settingsCard);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        onRestart();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


        myOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(homepage.this,"My overview",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(homepage.this, overview.class);
                startActivity(intent);
            }
        });
        myGraphs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(homepage.this, my_graphs.class);
                intent.putExtra("isHome", true);
                //Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://192.168.1.130:3000/d-solo/MP-A00003room_X2/mp-a00003_room_x2?orgId=1&from=1618686844493&to=1618776844493&panelId=10"));
                //startActivity(browserIntent);
                startActivity(intent);
            }
        });
        myNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(homepage.this, rooms_devices.class);
                startActivity(intent);
            }
        });
        mySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(homepage.this,"My overview",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(homepage.this, notification.class);
                startActivity(intent);
            }
        });


        editProfileB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                Intent intent= new Intent(homepage.this, profile_settings.class);
                startActivity(intent);
            }
        });
        myplatforms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                Intent intent= new Intent(homepage.this,my_platforms.class);
                startActivity(intent);
            }
        });
        logOutB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitMenu(v);
            }
        });

        backB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    System.exit(0);
                }
        });
    }
    public void showExitMenu(View v){
        PopupMenu popupMenu= new PopupMenu(this,v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onRestart() {
        super.onRestart();
        getWeather();
        createFloating();
        updateName();
        updatePlatformName();

    }

    public void createFloating(){
        FloatingActionButton mAddFab = findViewById(R.id.add_fab);
        FloatingActionButton mAddPlatformFab = findViewById(R.id.add_platform_fab);
        FloatingActionButton mAddRoomFab = findViewById(R.id.add_room_fab);

        // Also register the action name text, of all the FABs.
        TextView addPlatformActionText = findViewById(R.id.add_platform_text);
        TextView addRoomActionText = findViewById(R.id.add_room_text);

        mAddPlatformFab.setVisibility(View.GONE);
        mAddRoomFab.setVisibility(View.GONE);
        addPlatformActionText.setVisibility(View.GONE);
        addRoomActionText.setVisibility(View.GONE);
        isAllFabsVisible = false;
        mAddFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {
                            mAddPlatformFab.show();
                            mAddRoomFab.show();
                            addPlatformActionText.setVisibility(View.VISIBLE);
                            addRoomActionText.setVisibility(View.VISIBLE);

                            isAllFabsVisible = true;
                        } else {

                            mAddPlatformFab.hide();
                            mAddRoomFab.hide();
                            addPlatformActionText.setVisibility(View.GONE);
                            addRoomActionText.setVisibility(View.GONE);

                            isAllFabsVisible = false;
                        }
                    }
                });
        mAddPlatformFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent newPlatformActivity= new Intent(homepage.this, new_platform.class);
                        startActivity(newPlatformActivity);
                        //finish();
                    }
                });
        mAddRoomFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent newRoomActivity= new Intent(homepage.this, new_room_form_name.class);
                        startActivity(newRoomActivity);

                    }
                });

    }
    public void updateName(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String user=userdetails.getString("name","");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(homepage.this);
        String result=prefs.getString("name",user);
        final TextView username = (TextView) findViewById(R.id.textView2);
        username.setText("WELCOME "+result);
        Util.saveData(homepage.this,"userdetails","name",result);
    }
    public void createSpinner(){
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("platforms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
        List<String> platforms_nameList = new ArrayList();
        List<String> platformsList = new ArrayList();
        SharedPreferences currentdetails = getSharedPreferences("currentdetails", MODE_PRIVATE);
        String current_name=currentdetails.getString("platform_name","");
        for (Map.Entry<String, String> entry : platforms_dict.entrySet()) {
            platforms_nameList.add(entry.getValue());
            platformsList.add(entry.getKey());

        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, platforms_nameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(platforms_nameList.indexOf(current_name));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String text=parent.getItemAtPosition(position).toString();
                if (Firstflag<=1) {
                    Firstflag++;
                }
                String current_ID=platformsList.get(position);
                String current_Name=platforms_nameList.get(position);
                Util.saveData(homepage.this,"currentdetails","platform_ID",current_ID);
                Util.saveData(homepage.this,"currentdetails","platform_name",current_Name);
                updateLocation(apiURL);
                /*
                if(Firstflag>1){

                    Toast.makeText(homepage.this, current_ID+" is now selected", Toast.LENGTH_SHORT).show();
                }
                 */

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void updatePlatformName() {
        //downloadList();

        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL = userdetails.getString("profilesURL", "");
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("platforms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> platforms_dict = gsonDict.fromJson(jsonDict, typeDict);
        
        for (Map.Entry<String, String> entry : platforms_dict.entrySet()) { 
            Util.getPlatformInfo(profilesURL, entry.getKey(), "platform_name", homepage.this, new Util.ResponseCallback() {

            @Override
            public void onRespSuccess(String result) {

                entry.setValue(result.replace("\"", ""));
                Gson gson_out = new Gson();
                String json_out = gson_out.toJson(platforms_dict);
                SharedPreferences.Editor editor = userdetails.edit();
                editor.putString("platforms_dict", json_out);
                editor.apply();
                createSpinner();

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this, result, Toast.LENGTH_SHORT).show();

            }
        });
    }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                logoutDialog();

                return true;
            case R.id.item2:
                Toast.makeText(this, "It should be implemented", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.feedback:
                retrieveRoomsList();
                return true;


            default:
                return false;
        }
    }
    public void selectRoom(){
        SharedPreferences userdetails = homepage.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gsonDict = new Gson();
        String jsonDict = userdetails.getString("rooms_dict", "");
        Type typeDict = new TypeToken<Map<String,String>>() {
        }.getType();
        Map<String,String> rooms_dict = gsonDict.fromJson(jsonDict, typeDict);
        Gson gsonName = new Gson();
        String jsonName = userdetails.getString("rooms_name", "");
        Type typeName = new TypeToken<List<String>>() {
        }.getType();
        List<String> roomsName = gsonName.fromJson(jsonName, typeName);
        String[] array_name = roomsName.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyFeedbackDialog);
        builder.setTitle("Select your room for feedback");
        builder.setItems(array_name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String myroom_name=array_name[which];
                String myroom=rooms_dict.get(myroom_name);
                feedbackDialog(myroom);
            }
        });
        builder.show();
    }
    public void feedbackDialog(String myroom){
        /*
        Dialog d=new Dialog(this);
        d.setTitle("My Feedback");
        d.setCancelable(false);
        d.setContentView(R.layout.dialog);
        d.show();
         */

        String[] comfort = {"Too cold","Cold", "Ok", "Hot","Too hot"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyFeedbackDialog);
        builder.setTitle("My Feedback");
        builder.setItems(comfort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(homepage.this,"Feedback sent.",Toast.LENGTH_SHORT).show();
                String myfeedback=comfort[which];
                try {
                    sendFeedback(myroom,myfeedback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }
    public void sendFeedback(String myroom,String myfeedback) throws JSONException {
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String feedbackURL=userdetails.getString("feedbackURL","");
        Util.putParameter(homepage.this,feedbackURL,"/"+myroom,"/newFeedback/","feedback", myfeedback.toLowerCase(),false,false,new Util.PostCallback() {
            @Override
            public void onRespSuccess(JSONObject result) {
                Toast.makeText(homepage.this,"Ok",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this,"Can't save settings",Toast.LENGTH_LONG).show();

            }
        });


    }
    public void logoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(homepage.this,R.style.MyAlertDialog);
        builder.setTitle("LogOut").
                setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        performLogout();
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
    public void retrieveRoomsList(){
        SharedPreferences currentdetails = homepage.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = homepage.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Util.getPlatformInfo(profilesURL, platform_ID, "preferences", homepage.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                List<String> roomsList = new ArrayList<>();
                List<String> rooms_nameList = new ArrayList<>();
                Map<String, String> map = new HashMap<String, String>();
                JSONArray array = new JSONArray(result);
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
                selectRoom();
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this,result,Toast.LENGTH_SHORT).show();

            }
        });

    }
    public void performLogout(){
        Intent intent=new Intent(homepage.this, login.class);
        startActivity(intent);
        SharedPreferences currentdetails = homepage.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_c = currentdetails.edit();
        editor_c.clear();
        editor_c.commit();

        SharedPreferences userdetails = homepage.this.getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor_u = userdetails.edit();
        editor_u.clear();
        editor_u.commit();
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(homepage.this);
        SharedPreferences.Editor editor_p = prefs.edit();
        editor_p.clear();
        editor_p.commit();

         */



        SharedPreferences status = homepage.this.getSharedPreferences("status", MODE_PRIVATE);
        SharedPreferences.Editor editor = status.edit();
        editor.putBoolean("login",false);
        editor.commit();
        finish();

    }
    public void getWeather(){
        try {
            APIKEY = Util.getProperty("apiKEY",homepage.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(homepage.this);
        String location=prefs.getString("location","");
        if(location.equals("")) {
            updateLocation(apiURL);
        }else{
            WeatherReq(apiURL,location);
        }


    }
    public void WeatherReq(String apiURL,String location) {
        Util.weatherReq(homepage.this, apiURL, location, APIKEY, new Util.WeatherCallback() {
            @Override
            public void onRespSuccess(JSONObject result) throws JSONException {
                parseCondition(result,location);

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this,result,Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void parseCondition(JSONObject result,String city) {
        try {
            String icon=result.getJSONArray("weather").getJSONObject(0).getString("icon");
            //Toast.makeText(homepage.this,icon,Toast.LENGTH_SHORT).show();
            String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";
            Picasso.with(homepage.this).load(iconUrl).into(conditionImage);
            Double temp=result.getJSONObject("main").getDouble("temp");
            Double humidity=result.getJSONObject("main").getDouble("humidity");
            Double wind=result.getJSONObject("wind").getDouble("speed");
            tempExt.setText("T: "+temp.toString()+" °C");
            humExt.setText("H: "+humidity.toString()+"%");
            windExt.setText("W: "+wind.toString()+"km/h");
            locationText.setText(city);


        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void updateLocation(String apiurl){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences currentdetails = getSharedPreferences("currentdetails", MODE_PRIVATE);
        String profilesURL = userdetails.getString("profilesURL", "");
        String plat_ID=currentdetails.getString("platform_ID","");
        Util.getPlatformInfo(profilesURL, plat_ID, "location", homepage.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                WeatherReq(apiurl,result);
            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this,result,Toast.LENGTH_SHORT).show();


            }
        });
    }




}