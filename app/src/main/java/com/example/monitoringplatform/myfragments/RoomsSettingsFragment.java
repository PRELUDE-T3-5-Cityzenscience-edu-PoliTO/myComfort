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

public class RoomsSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    final List<String> mList = new ArrayList<>();
    private int myFlag;
    Context mContext;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = getActivity().getApplicationContext();
        update(rootKey);

    }
    public void createPref(String rootKey){
        myFlag--;

        if (myFlag<=0) {
            setPreferencesFromResource(R.xml.rooms_preferences, rootKey);
            mList.add("room_ID");
            mList.add("room_name");
            savelist();
        }
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
    public void onStop(){
        super.onStop();

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("room_ID")){
        }else{
            try {
                postUp(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    public static boolean containsWhiteSpace( String testCode){
        if(testCode != null){
            for(int i = 0; i < testCode.length(); i++){
                if(Character.isWhitespace(testCode.charAt(i))){
                    return true;
                }
            }
        }
        return false;
    }
    public void postUp(String key) throws JSONException {
        boolean isFloat=false;
        boolean isInt=false;
        boolean isBool=false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String parameter_value=prefs.getString(key,"");
        SharedPreferences userdetails = this.getActivity().getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        String serverURL = userdetails.getString("serverURL", "");
        String profilesURL = userdetails.getString("profilesURL", "");
        SharedPreferences currentdetails = this.getActivity().getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String room_ID = currentdetails.getString("room_ID", "");
        if(key.equals("room_name")){
            if(containsWhiteSpace(parameter_value)){
                Toast.makeText(getActivity(),"Spaces are not allowed.",Toast.LENGTH_SHORT).show();
            }
            Gson gsonDict = new Gson();
            String jsonDict = userdetails.getString("rooms_dict_ID", "");
            Type typeDict = new TypeToken<Map<String,String>>() {
            }.getType();
            Map<String,String> rooms_dict = gsonDict.fromJson(jsonDict, typeDict);
            for (Map.Entry<String, String> entry : rooms_dict.entrySet()) {
                if(entry.getKey().equals(room_ID)){
                    entry.setValue(parameter_value);
                    Gson gson_out= new Gson();
                    String json_out= gson_out.toJson(rooms_dict);
                    SharedPreferences.Editor editor = userdetails.edit();
                    editor.putString("rooms_dict_ID", json_out);
                    editor.commit();
                    break;
                }
            }
            try {
                Util.postParameter(getContext(),profilesURL,"/"+room_ID,"/setRoomParameter/",key, parameter_value.replace(" ",""),false,false,false,new Util.PostCallback() {
                    @Override
                    public void onRespSuccess(JSONObject result) {
                        Toast.makeText(getActivity(),"Ok",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onRespError(String result) {
                        Toast.makeText(getActivity(),"Can't save settings",Toast.LENGTH_LONG).show();

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else {
            if (key.equals("Icl_clo") || key.equals("M_met")) {
                isFloat = true;
            }

            Util.postParameter(mContext, serverURL, "/" + room_ID, "/setParameter/",key, parameter_value, isInt, isFloat, isBool, new Util.PostCallback() {
                @Override
                public void onRespSuccess(JSONObject result) {
                    Toast.makeText(mContext, "Ok", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onRespError(String result) {
                    Toast.makeText(mContext, "Can't save settings", Toast.LENGTH_LONG).show();

                }
            });
        }

    }


    public void update(String rootKey) {

        myFlag=mList.size();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences currentdetails = getActivity().getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        String room_ID = currentdetails.getString("room_ID", "");
        String room_name = currentdetails.getString("room_name", "");
        SharedPreferences userdetails = getActivity().getSharedPreferences("userdetails", MODE_PRIVATE);

        String serverURL = userdetails.getString("serverURL", "");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("room_ID", room_ID);
        editor.putString("room_name", room_name);
        mList.add("Icl_clo");
        mList.add("M_met");
        //editor.apply();
        for (String param : mList) {
            Util.getRoomInfo(serverURL, platform_ID, room_ID, param, getContext(), new Util.ResponseCallback() {

                @Override
                public void onRespSuccess(String result){
                    editor.putString(param, result);
                    editor.apply();
                    createPref(rootKey);
                }

                @Override
                public void onRespError(String result) {
                    //Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    editor.putString(param, "0");
                    editor.apply();
                    createPref(rootKey);


                }
            });

            }

        }
    //potrebbe non servire più
    public void savelist(){
        SharedPreferences userdetails = getActivity().getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = userdetails.edit();
        Gson gson= new Gson();
        String json_out= gson.toJson(mList);
        editor.putString("edited", json_out);
        editor.commit();
    }


}