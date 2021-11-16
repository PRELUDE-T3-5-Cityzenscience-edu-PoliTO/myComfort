package com.example.monitoringplatform.add_platform;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.monitoringplatform.R;
import com.example.monitoringplatform.Util;

import org.json.JSONException;

public class new_platform extends AppCompatActivity {
    private String platform_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_platform);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button next=findViewById(R.id.button_next);
        TextView enterPlatform=findViewById(R.id.newplatformID);
        TextView platformIDerror=findViewById(R.id.IDerror);
        TextView existingErr=findViewById(R.id.existingerror);
        platformIDerror.setVisibility(View.GONE);
        existingErr.setVisibility(View.GONE);
        enterPlatform.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                next.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                enableSubmitIfReady(enterPlatform,next);


            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                platform_ID=enterPlatform.getText().toString();
                SharedPreferences userdetails = new_platform.this.getSharedPreferences("userdetails", MODE_PRIVATE);
                String profilesURL=userdetails.getString("profilesURL","");
                Util.getPlatformInfo(profilesURL, "checkExisting", platform_ID, new_platform.this, new Util.ResponseCallback() {
                    @Override
                    public void onRespSuccess(String result) throws JSONException {
                        if (result.equals("false")){
                            platformIDerror.setVisibility(View.GONE);
                            existingErr.setVisibility(View.GONE);
                            platformIDerror.setVisibility(View.VISIBLE);
                        }else{
                            Util.getPlatformInfo(profilesURL, "checkRegistered", platform_ID, new_platform.this, new Util.ResponseCallback() {
                                @Override
                                public void onRespSuccess(String result) throws JSONException {
                                    if (result.equals("true")){
                                        platformIDerror.setVisibility(View.GONE);
                                        existingErr.setVisibility(View.GONE);
                                        existingErr.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        platformIDerror.setVisibility(View.GONE);
                                        existingErr.setVisibility(View.GONE);
                                        Intent intent=new Intent(new_platform.this, new_platform_form.class);
                                        intent.putExtra("platform_ID", platform_ID);
                                        startActivity(intent);
                                        finish();

                                    }
                                }

                                @Override
                                public void onRespError(String result) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onRespError(String result) {


                    }
                });
            }
        });

    }
    public void enableSubmitIfReady(TextView enterPlatform, Button next) {

        boolean isReady = enterPlatform.getText().toString().length() > 5;
        next.setEnabled(isReady);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}