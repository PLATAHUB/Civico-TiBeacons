/**
w* This file was auto-generated by the Titanium Module SDK helper for Android
* Appcelerator Titanium Mobile
* Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
* Licensed under the terms of the Apache Public License
* Please see the LICENSE included with this distribution for details.
*
*/
package com.civico.tibeacons;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

@Kroll.module(name="Beacons", id="com.civico.tibeacons")
public class BeaconsModule extends KrollModule {

    public static final String TAG = "TiAPI";
    public static String API_URL;

    public BeaconsModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
    }

    @Kroll.method
    public void start(String apiUrl) {
        BeaconsModule.API_URL = apiUrl;
        
        //Set a shared preference for boot start service
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TiApplication.getInstance().getApplicationContext());
        prefs.edit().putString("API_URL", BeaconsModule.API_URL);
        
        Log.d(TAG, "Android version: "+Build.VERSION.SDK_INT);
        TiApplication.getInstance().getAppInfo().getVersion();
        if(Build.VERSION.SDK_INT >= 18 /*Build.VERSION_CODES.JELLY_BEAN_MR2*/){
	        try {
	            Intent intent = new Intent(TiApplication.getInstance(), BeaconService.class);
	            TiApplication.getInstance().startService(intent);
	            Log.d(TAG, "Started Civico-TiBeacons Module!");
	        } catch (Exception e) {
	            Log.e(TAG, e.toString());
	        }
        }else{
        	Log.e(TAG, "Not going to start Beacons Service because the device it's incompatible. Sorry :(");
        }
    }

    public static String getApiUrl() {

        if (BeaconsModule.API_URL != null) {
            return BeaconsModule.API_URL;
        }

        return  TiApplication.getInstance().getAppProperties().getString("api.backend.url", null);
    }
}
