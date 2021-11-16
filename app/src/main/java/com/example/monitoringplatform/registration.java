package com.example.monitoringplatform;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.monitoringplatform.ui.login.login;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class registration extends AppCompatActivity {
    private WebView webView;
    private TextView nodata;
    private String apiURL;
    private String clientsURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nodata=findViewById(R.id.NoReg);
        webView=(WebView) findViewById(R.id.webview_reg);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedHttpAuthRequest(WebView view,
                                                  HttpAuthHandler handler, String host, String realm) {
                handler.proceed("superuser", "soComplicated");
            }
        });
        WebSettings ws = webView.getSettings();
        //Choose Mobile/Desktop client.
        //ws.setUserAgentString(desktop_mode);

        ws.setJavaScriptEnabled(true);
        ws.setAllowFileAccess(true);
        String TAG="html5";


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR) {
            try {
                Log.d(TAG, "Enabling HTML5-Features");
                Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
                m1.invoke(ws, Boolean.TRUE);

                Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
                m2.invoke(ws, Boolean.TRUE);

                Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
                m3.invoke(ws, "/data/data/" + getPackageName() + "/databases/");

                Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
                m4.invoke(ws, 1024*1024*8);

                Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
                m5.invoke(ws, "/data/data/" + getPackageName() + "/cache/");

                Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
                m6.invoke(ws, Boolean.TRUE);

                Log.d(TAG, "Enabled HTML5-Features");
            }
            catch (NoSuchMethodException e) {
                Log.e(TAG, "Reflection fail", e);
            }
            catch (InvocationTargetException e) {
                Log.e(TAG, "Reflection fail", e);
            }
            catch (IllegalAccessException e) {
                Log.e(TAG, "Reflection fail", e);
            }
        }
        try {
            apiURL = Util.getProperty("service_catalog",registration.this);
            clientsURI = Util.getProperty("clientsURI",registration.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.getService(registration.this,apiURL, clientsURI, "clientsURL", new Util.ServiceCallback() {
            @Override
            public void onReqSuccess(JSONObject result) {
                SharedPreferences userdetails = registration.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                String clientsURL=userdetails.getString("clientsURL","");
                nodata.setVisibility(View.GONE);
                webView.loadUrl(clientsURL+"/reg");

            }

            @Override
            public void onReqError(String result) {
                nodata.setVisibility(View.VISIBLE);

            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}