package com.civico.tibeacons;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class EstimoteBroadcastReceiver extends BroadcastReceiver {
	
	public EstimoteBroadcastReceiver() {
        super();
    }
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(BeaconsModule.TAG, "Starting bradcast bakground service after boot.");
		Log.d(BeaconsModule.TAG, "Android version: "+Build.VERSION.SDK_INT);
        TiApplication.getInstance().getAppInfo().getVersion();
        if(Build.VERSION.SDK_INT >= 18 /*Build.VERSION_CODES.JELLY_BEAN_MR2*/){
	        try {
	        	Intent startServiceIntent = new Intent(context, BeaconService.class);
	    		context.startService(startServiceIntent);
	            Log.d(BeaconsModule.TAG, "Started Civico-TiBeacons Module!");
	        } catch (Exception e) {
	            Log.e(BeaconsModule.TAG, e.toString());
	        }
        }else{
        	Log.e(BeaconsModule.TAG, "Not going to start Beacons Service because the device it's incompatible. Sorry :(");
        }
	}

}
