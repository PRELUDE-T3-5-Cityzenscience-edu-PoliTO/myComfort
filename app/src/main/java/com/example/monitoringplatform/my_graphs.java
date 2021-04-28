package com.example.monitoringplatform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoringplatform.ui.login.login;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class my_graphs extends AppCompatActivity {
    private WebView webView;
    private String grafanaURI;
    private String apiURL;
    private boolean isHome;
    private TextView nodata;
    private static final String desktop_mode = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras.containsKey("isHome")) {
            isHome=i.getBooleanExtra("isHome",true);
        }
        nodata=findViewById(R.id.NoDataGraphs);
        webView=(WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
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
            apiURL = Util.getProperty("service_catalog",my_graphs.this);
            grafanaURI = Util.getProperty("grafanaURI", my_graphs.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(isHome){
            Util.getService(my_graphs.this, apiURL, grafanaURI, "grafanaURL", new Util.ServiceCallback() {
                @Override
                public void onReqSuccess(JSONObject result) {
                    SharedPreferences userdetails = my_graphs.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                    String grafanaURL=userdetails.getString("grafanaURL","");

                    SharedPreferences currentdetails = my_graphs.this.getSharedPreferences("currentdetails", MODE_PRIVATE);
                    String platform_ID=currentdetails.getString("platform_ID","");

                    Util.getPlatformInfo(grafanaURL, "home", platform_ID, my_graphs.this, new Util.ResponseCallback() {
                        @Override
                        public void onRespSuccess(String result) throws JSONException {
                            if (savedInstanceState == null)
                            {
                                nodata.setVisibility(View.GONE);
                                webView.loadUrl(result);
                            }


                        }

                        @Override
                        public void onRespError(String result) {
                            nodata.setVisibility(View.VISIBLE);

                        }
                    });

                }

                @Override
                public void onReqError(String result) {

                }
            });

        }else{
            Util.getService(my_graphs.this,apiURL,grafanaURI,"grafanaURL", new Util.ServiceCallback() {
                @Override
                public void onReqSuccess(JSONObject result) {
                    SharedPreferences userdetails = my_graphs.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                    String grafanaURL=userdetails.getString("grafanaURL","");

                    SharedPreferences currentdetails = my_graphs.this.getSharedPreferences("currentdetails", MODE_PRIVATE);
                    String platform_ID=currentdetails.getString("platform_ID","");
                    String room_ID=currentdetails.getString("room_ID","");
                    Util.getRoomInfo(grafanaURL,"dashboard",platform_ID,room_ID,my_graphs.this, new Util.ResponseCallback() {
                        @Override
                        public void onRespSuccess(String result) throws JSONException {

                            if (savedInstanceState == null)
                            {
                                nodata.setVisibility(View.GONE);
                                webView.loadUrl(result);
                            }

                            //Toast.makeText(my_graphs.this,result,Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRespError(String result) {
                            nodata.setVisibility(View.VISIBLE);

                        }
                    });

                }

                @Override
                public void onReqError(String result) {

                }
            });

        }

        //webView.loadUrl("http://192.168.1.130:3000/d/MP-A00003room_X2/mp-a00003_room_x2?orgId=1&from=now-24h&to=now%2B2h");
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView_graph);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.backHome:
                                Intent intent_settings=new Intent(my_graphs.this, homepage.class);
                                startActivity(intent_settings);
                                finish();
                                break;

                        }
                        return false;
                    }
                });


    }
    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
    @Override
    public void onBackPressed(){
        if (webView.canGoBack()){
            webView.goBack();
        }
        else{
            super.onBackPressed();
        }
    }
}
