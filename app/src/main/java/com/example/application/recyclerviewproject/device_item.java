package com.example.application.recyclerviewproject;

public class device_item {
    private int mImageResource;
    private String mText1;
    private String mText2;
    private String mText3;

    public device_item(int imageResource, String text1,String text2,String text3) {
        mImageResource = imageResource;
        mText1 = text1;
        mText2 = text2;
        mText3 = text3;
    }

    public int getImageResource() {
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }
    public String getText3() {
        return mText3;
    }
    public String getText2() {
        return mText2;
    }

}