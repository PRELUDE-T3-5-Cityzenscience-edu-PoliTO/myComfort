package com.example.monitoringplatform;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppSingleton {

    private static AppSingleton mInstance;
    private RequestQueue mRequesteQueue;
    private AppSingleton(Context context){
        mRequesteQueue= Volley.newRequestQueue(context.getApplicationContext());

    }
    public static synchronized AppSingleton getInstance(Context context){
        if (mInstance==null){
            mInstance=new AppSingleton(context);
        }
        return mInstance;
    }
    public RequestQueue getRequesteQueue(){
        return  mRequesteQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequesteQueue().add(req);
    }

}
