package com.example.monitoringplatform.myfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.monitoringplatform.R;
import com.example.monitoringplatform.Util;
import com.example.monitoringplatform.homepage;
import com.example.monitoringplatform.mqtt_sub;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    Map<String, ?> allEntries;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        SharedPreferences status = getActivity().getSharedPreferences("status", MODE_PRIVATE);
        List<String> settingsList= new ArrayList<>();
        settingsList.add("name");
        settingsList.add("warning");
        settingsList.add("platform_name");
        settingsList.add("inactive_time");
        settingsList.add("location");
        Gson gson_out= new Gson();
        String json_out= gson_out.toJson(settingsList);
        SharedPreferences userdetails = getActivity().getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = userdetails.edit();
        editor.putString("profileSettings", json_out);
        editor.apply();
        update(rootKey);

    }
    public void createPref(String rootKey){

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean isInt=false;
        boolean isFloat=false;
        boolean isBool=false;
        String parameter_value=null;
        if(key.equals("warning")){
            isBool=true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences currentdetails = getActivity().getSharedPreferences("currentdetails",MODE_PRIVATE);
            parameter_value=String.valueOf(prefs.getBoolean(key,false));
           // Toast.makeText(getContext(),currentdetails.getString("platform_ID",""),Toast.LENGTH_SHORT).show();

            Intent intent=new Intent(this.getActivity(), mqtt_sub.class);
            intent.putExtra("platform_ID",currentdetails.getString("platform_ID",""));
            intent.putExtra("subscribe",prefs.getBoolean(key,false));
            this.getActivity().startService(intent);

        }else{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            parameter_value=prefs.getString(key,"");
        }

        SharedPreferences userdetails = this.getActivity().getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        String profilesURL = userdetails.getString("profilesURL", "");
        if(key.equals("inactive_time")){
            isInt=true;
        }

        if(!key.equals("name")) {
            try {
                Util.postParameter(getContext(), profilesURL, "/", "/setParameter/", key, parameter_value, isInt, isFloat, isBool, new Util.PostCallback() {
                    @Override
                    public void onRespSuccess(JSONObject result) {
                        Toast.makeText(getActivity(), "Ok", Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onRespError(String result) {
                        Toast.makeText(getActivity(), "Can't save settings", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void update(String rootKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences currentdetails = getActivity().getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        SharedPreferences userdetails = getActivity().getSharedPreferences("userdetails", MODE_PRIVATE);
        String profilesURL=userdetails.getString("profilesURL","");
        Gson gson = new Gson();
        String jsonS = userdetails.getString("profileSettings", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> profileSettings = gson.fromJson(jsonS, type);

        for (int i = 0; i < profileSettings.size(); i++) {
            int finalI = i;
            if(!profileSettings.get(i).equals("name")) {
                Util.getPlatformInfo(profilesURL, platform_ID, profileSettings.get(i), getActivity(), new Util.ResponseCallback() {

                    @Override
                    public void onRespSuccess(String result) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if(profileSettings.get(finalI).equals("warning")){
                            editor.putBoolean(profileSettings.get(finalI), Boolean.parseBoolean(result));
                            editor.apply();
                            createPref(rootKey);

                        }else {

                            editor.putString(profileSettings.get(finalI), result);
                            editor.apply();
                            createPref(rootKey);
                        }



                    }

                    @Override
                    public void onRespError(String result) {
                        //Toast.makeText(getActivity(), profileSettings.get(finalI), Toast.LENGTH_SHORT).show();

                    }
                });
            }

        }

    }

}