package com.example.monitoringplatform;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.monitoringplatform.ui.login.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Properties;

import static android.content.Context.MODE_PRIVATE;

public class Util {
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }
    public static void  clearAll(Context context){

      
        SharedPreferences currentdetails = context.getSharedPreferences("currentdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor_c = currentdetails.edit();
        editor_c.clear();
        editor_c.apply();

        SharedPreferences userdetails = context.getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor_u = userdetails.edit();
        editor_u.clear();
        editor_u.commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor_p = prefs.edit();
        editor_p.clear();
        editor_p.commit();

        SharedPreferences status = context.getSharedPreferences("status", MODE_PRIVATE);
        SharedPreferences.Editor editor = status.edit();
        editor.putBoolean("login",false);
        editor.commit();

    }
    public static void saveData(Context context, String name_pref, String name, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(name_pref, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editData(editor,name,value);
        editor.commit();
    }
    public static void editData(SharedPreferences.Editor editor, String name, String value){
        editor.putString(name, value);
    }

    public static String setURL(String url, String uri){
        String output=url+uri;
        return output;
    }
    public static String buildURL(String IP,String service){
        String URL= IP + service;
        return URL;
    }
    public static void getPlatformInfo(String api, String uri, String key, Context context, final ResponseCallback responseCallback){
        String final_uri="/"+uri+"/"+key;
        String final_url = Util.setURL(api, final_uri);
        StringRequest JSONreq = new StringRequest(Request.Method.GET, final_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onRespSuccess(response.replace("\"",""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);

    }
    public static void getRoomInfo(String api, String uri, String plat, String room,  Context context, final ResponseCallback responseCallback){
        String final_uri="/"+uri+"/"+plat+"/"+room;
        String final_url = Util.setURL(api, final_uri);
        StringRequest JSONreq = new StringRequest(Request.Method.GET, final_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onRespSuccess(response.replace("\"",""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);

    }
    public static void getService(Context context, String api, String uri, String new_service, final Util.ServiceCallback serviceCallback){
        String final_url = Util.setURL(api+"/public", uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.GET, final_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String ip = null;
                        String service = null;
                        try {
                            ip = response.getString("IP_address");
                            service = response.getString("service");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String URL = Util.buildURL(ip, service);
                        Util.saveData(context,"userdetails",new_service,URL);
                        if (serviceCallback != null) {
                            serviceCallback.onReqSuccess(response);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (serviceCallback != null) {
                    serviceCallback.onReqError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);

    }
    public static void postParameter(Context context, String api, String extraParameter,String operation, String parameter, String parameter_value, boolean isInt,boolean isFloat, boolean isBool,final Util.PostCallback responseCallback) throws JSONException {
        SharedPreferences currentdetails = context.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        String final_uri=operation+platform_ID+extraParameter;
        String final_url = Util.setURL(api, final_uri);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("parameter", parameter);
        if (isFloat && parameter_value!=null){
            jsonBody.put("parameter_value", Double.parseDouble(parameter_value));
        }else {
            if(isInt){
                jsonBody.put("parameter_value", Integer.parseInt(parameter_value));
            }if(isBool){
                jsonBody.put("parameter_value", Boolean.parseBoolean(parameter_value));
            }
            else {
                jsonBody.put("parameter_value", parameter_value);
            }
        }
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.POST, final_url, jsonBody,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        responseCallback.onRespSuccess(response);

                    }
                },  new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    responseCallback.onRespError(error.getMessage());

                }
            }
        });
        if (JSONreq!=null) {
            AppSingleton.getInstance(context).addToRequestQueue(JSONreq);
        }

    }
    public static void putParameter(Context context, String api, String extraParameter,String operation, String parameter, String parameter_value, boolean isInt,boolean isFloat,final Util.PostCallback responseCallback) throws JSONException {
        SharedPreferences currentdetails = context.getSharedPreferences("currentdetails", Context.MODE_PRIVATE);
        String platform_ID = currentdetails.getString("platform_ID", "");
        String final_uri=operation+platform_ID+extraParameter;
        String final_url = Util.setURL(api, final_uri);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put(parameter, parameter_value);
        if (isFloat && parameter_value!=null){
            jsonBody.put("parameter_value", Double.parseDouble(parameter_value));
        }else {
            if(isInt){
                jsonBody.put("parameter_value", Integer.parseInt(parameter_value));
            }else {
                jsonBody.put("parameter_value", parameter_value);
            }
        }
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.PUT, final_url, jsonBody,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        responseCallback.onRespSuccess(response);

                    }
                },  new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    responseCallback.onRespError(error.getMessage());

                }
            }
        });
        if (JSONreq!=null) {
            AppSingleton.getInstance(context).addToRequestQueue(JSONreq);
        }

    }
    public static void putElement(Context context, String api,String operation, String extraParameter, JSONObject obj,final PutCallback responseCallback) throws JSONException {
        String final_uri=operation+extraParameter;
        String final_url = Util.setURL(api, final_uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.PUT, final_url,obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onResponseSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onResponseError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);


    }
    public static void deleteElement(Context context, String api,String operation, String extraParameter, final DeleteCallback responseCallback) throws JSONException {
        String final_uri=operation+extraParameter;
        String final_url = Util.setURL(api, final_uri);
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.DELETE, final_url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onRespSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);


    }
    public static void weatherReq(Context context, String url,String location,String APIKEY, final WeatherCallback responseCallback) {
        String cityQuery="?q="+location;
        String idQuery="&appid="+APIKEY;
        String metrics="&units=metric";
        JsonObjectRequest JSONreq = new JsonObjectRequest(Request.Method.GET, url+cityQuery+idQuery+metrics,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (responseCallback != null) {
                            try {
                                responseCallback.onRespSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(login.this, "Connection failed.", Toast.LENGTH_SHORT).show();
                if (responseCallback != null) {
                    responseCallback.onRespError(error.toString());
                }
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(JSONreq);

    }

    public interface LoginCallback {

        void onLoginSuccess(JSONObject result);

        void onLoginError(String result);
    }

    public interface ResponseCallback {

        void onRespSuccess(String result) throws JSONException;

        void onRespError(String result);

    }
    public interface ServiceCallback {
        void onReqSuccess(JSONObject result);

        void onReqError(String result);

    }
    public interface PostCallback {

        void onRespSuccess(JSONObject result);

        void onRespError(String result);

    }
    public interface PutCallback {

        void onResponseSuccess(JSONObject result) throws JSONException;

        void onResponseError(String result);

    }
    public interface WeatherCallback {

        void onRespSuccess(JSONObject result) throws JSONException;

        void onRespError(String result);

    }
    public interface DeleteCallback {

        void onRespSuccess(JSONObject result) throws JSONException;

        void onRespError(String result);

    }



}