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
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monitoringplatform.ui.login.login;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;

public class homepage extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public int Firstflag=0;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // register all the ImageButtons with their appropriate IDs
        ImageButton backB = (ImageButton) findViewById(R.id.backB);
        ImageButton logOutB = (ImageButton) findViewById(R.id.logOutB);
        //ImageButton profileB = (ImageButton) findViewById(R.id.profileB);

        // register all the Buttons with their appropriate IDs
        Button myplatforms = findViewById(R.id.myplatforms);
        Button editProfileB = findViewById(R.id.editProfileB);

        //
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);

        // register all the card views with their appropriate IDs
        CardView myOverview = (CardView) findViewById(R.id.myOverview);
        CardView myGraphs = (CardView) findViewById(R.id.myGraphs);
        CardView learnCard = (CardView) findViewById(R.id.learnCard);
        CardView interestsCard = (CardView) findViewById(R.id.interestsCard);
        CardView helpCard = (CardView) findViewById(R.id.helpCard);
        CardView settingsCard = (CardView) findViewById(R.id.settingsCard);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        onResume();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


        myOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(homepage.this,"My overview",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(homepage.this, MainActivity.class);
                startActivity(intent);
            }
        });
        myGraphs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(homepage.this,"My overview",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(homepage.this, MainActivity2.class);
                startActivity(intent);
            }
        });
        editProfileB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                Intent intent= new Intent(homepage.this,profile_settings.class);
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
    public void onResume() {
        super.onResume();
        updateName();
        updatePlatformName();

    }
    public void updateName(){
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        SharedPreferences currentdetails = homepage.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        Util.getPlatformInfo(profilesURL, platform_ID, "name", homepage.this, new Util.ResponseCallback() {
            @Override
            public void onRespSuccess(String result) throws JSONException {
                final TextView username = (TextView) findViewById(R.id.textView2);
                username.setText("WELCOME "+result);
                Util.saveData(homepage.this,"userdetails","name",result);

            }

            @Override
            public void onRespError(String result) {

            }
        });
    }
    public void createSpinner(){
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonN = userdetails.getString("platforms_name", "");
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> platforms_nameList = gson.fromJson(jsonN, type);
        Gson gsonID = new Gson();
        String jsonID = userdetails.getString("platforms", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        List<String> platformsList = gsonID.fromJson(jsonID, typeID);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, platforms_nameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String text=parent.getItemAtPosition(position).toString();
                if (Firstflag<=1) {
                    Firstflag++;
                }
                String current_ID=platformsList.get(position);
                Util.saveData(homepage.this,"currentdetails","platform_ID",current_ID);
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
        SharedPreferences currentdetails = homepage.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Gson gsonID = new Gson();
        String jsonID = userdetails.getString("platforms", "");
        Type typeID = new TypeToken<List<String>>() {
        }.getType();
        List<String> platformsList = gsonID.fromJson(jsonID, typeID);
        int position = platformsList.indexOf(platform_ID);
        Util.getPlatformInfo(profilesURL, platform_ID, "platform_name",homepage.this, new Util.ResponseCallback() {

            @Override
            public void onRespSuccess(String result) {

                Gson gson = new Gson();
                String jsonN = userdetails.getString("platforms_name", "");
                Type type = new TypeToken<List<String>>() {
                }.getType();
                List<String> platforms_nameList = gson.fromJson(jsonN, type);
                platforms_nameList.set(position,result.replace("\"",""));
                Gson gson_out= new Gson();
                String json_out= gson_out.toJson(platforms_nameList);
                SharedPreferences.Editor editor = userdetails.edit();
                editor.putString("platforms_name", json_out);
                editor.commit();
                createSpinner();

            }

            @Override
            public void onRespError(String result) {
                Toast.makeText(homepage.this,result,Toast.LENGTH_SHORT).show();

            }
        });

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

            default:
                return false;
        }
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
    public void performLogout(){
        Intent intent=new Intent(homepage.this, login.class);
        startActivity(intent);
        SharedPreferences currentdetails = homepage.this.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_c = currentdetails.edit();
        editor_c.clear();
        editor_c.commit();

        SharedPreferences userdetails = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor_u = userdetails.edit();
        editor_u.clear();
        editor_u.commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(homepage.this);
        SharedPreferences.Editor editor_p = prefs.edit();
        editor_p.clear();
        editor_p.commit();

        finish();

    }
}