package com.example.rfidapp;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
public class SharedPreference extends Application {
    static final String DEVICE_NAME= "userName";
    static final String Tag_ID ="TagId";
    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String getTag_ID(Context ctx) {
        return getSharedPreferences(ctx).getString(Tag_ID, "");
    }
    public static void setTag_ID(Context ctx, String TagId)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        System.out.println("ssssssssssssssssssss"+TagId);
        editor.putString(Tag_ID, TagId);
        editor.commit();
    }
    public static void clearTagId(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }
    public static void setDeviceName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        System.out.println("ssssssssssssssssssss"+userName);
        editor.putString(DEVICE_NAME, userName);
        editor.commit();
    }
    public static String getDeviceName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(DEVICE_NAME, "");
    }
    public static void cleardevicename(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }

//    public static void clearUserName1(DialogInterface.OnClickListener onClickListener) {
//        SharedPreferences.Editor editor = getSharedPreferences(getSharedPreferences(this)).edit();
//        editor.clear(); //clear all stored data
//        editor.commit();
//    }
}