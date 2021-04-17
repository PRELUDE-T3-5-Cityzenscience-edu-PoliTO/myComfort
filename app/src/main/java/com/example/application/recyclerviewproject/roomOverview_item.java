package com.example.application.recyclerviewproject;

public class roomOverview_item {
    private int mIconRoom;
    private int mIconTemperature;
    private int mIconHumidity;
    private int mIconWind;
    private String mRoom;
    private String mTemp;
    private String mHum;
    private String mWind;
    private String mPmv;


    public roomOverview_item(int IconRoom,int iconTemperature,int iconHumidity,int iconWind,String room, String temp, String hum,String wind, String pmv) {
        mIconRoom=IconRoom;
        mIconTemperature=iconTemperature;
        mIconHumidity=iconHumidity;
        mIconWind=iconWind;

        mRoom=room;
        mTemp=temp;
        mHum=hum;
        mWind=wind;
        mPmv=pmv;
    }

    public int getIconRoom() {
        return mIconRoom;
    }
    public int getIconTemperature() {
        return mIconTemperature;
    }
    public int getIconHumidity() {
        return mIconHumidity;
    }
    public int getIconWind() {
        return mIconWind;
    }

    public String getRoom() {
        return mRoom;
    }
    public String getTemp() {
        return mTemp;
    }
    public String getHum() {
        return mHum;
    }
    public String getWind() {
        return mWind;
    }
    public String getPmv() {
        return mPmv;
    }
}