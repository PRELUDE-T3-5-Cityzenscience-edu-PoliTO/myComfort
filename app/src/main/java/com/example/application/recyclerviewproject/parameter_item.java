package com.example.application.recyclerviewproject;

public class parameter_item {
    private int mIcon;
    private String mParameter;
    private String mValue;
    private String mTime;



    public parameter_item(int Icon, String parameter, String value, String time) {
        mIcon=Icon;
        mParameter=parameter;
        mValue=value;
        mTime=time;

    }

    public int getIcon() {
        return mIcon;
    }

    public String getParameter() {
        return mParameter;
    }
    public String getValue() {
        return mValue;
    }
    public String getTime() {
        return mTime;
    }
}