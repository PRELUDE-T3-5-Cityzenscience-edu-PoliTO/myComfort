package com.example.monitoringplatform.add_platform;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.monitoringplatform.R;

public class new_platform_form extends AppCompatActivity {
    private String platform_name;
    private String plat_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_platform_form);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras.containsKey("platform_ID")) {
            plat_ID = i.getStringExtra("platform_ID");
        }
        Button next=findViewById(R.id.button_next2);
        TextView enterPlatform=findViewById(R.id.newplatformName);
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
                platform_name=enterPlatform.getText().toString();
                Intent intent=new Intent(new_platform_form.this, new_platform_form_location.class);
                intent.putExtra("platform_ID",plat_ID);
                intent.putExtra("platform_name", platform_name);
                startActivity(intent);
                finish();


            }
        });
    }
    public void enableSubmitIfReady(TextView enterPlatform, Button next) {

        boolean isReady = enterPlatform.getText().toString().length() > 0;
        next.setEnabled(isReady);
    }
}