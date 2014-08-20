package com.civico.tibeacons;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

public class BeaconService extends Service{

	int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used

    //Estimote vars
  	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
  	private static final Region CIVICO_ESTIMOTE_BEACONS = new Region("CivRegion", null, 24842, null);
  	private BeaconManager beaconManager;
	//Notification vars

	private int CIV_NOTIFICATION_ID = 24842;
	private boolean ableToNotify = false;

	@Override
    public void onCreate() {
		Log.e(BeaconsModule.TAG, "Created Civico-TiBeacons Service!");
		
		// The service is being created
    	beaconManager = new BeaconManager( TiApplication.getInstance().getApplicationContext() );

    	//Beacon region monitoring
    	if(beaconManager.hasBluetooth()){
    		if(beaconManager.isBluetoothEnabled()){
    			addMonitoringListener();
    		}
    	}
	}

	private void addMonitoringListener() {
		beaconManager.setMonitoringListener(new MonitorListener(this));
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		// Initial bluetooth validator
		if (beaconManager.hasBluetooth()) {
    		if (beaconManager.isBluetoothEnabled() == false) {
    			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    			mBluetoothAdapter.enable();
    			Toast.makeText(TiApplication.getInstance(), "Cívico ha activado el bluetooth.", Toast.LENGTH_SHORT).show();
    		}
    	}
		// setBackgroundScanPeriod(long scanPeriodMillis, long waitTimeMillis)
		// scanPeriodMillis - How long to perform Bluetooth Low Energy scanning?
		// waitTimeMillis - How long to wait until performing next scanning?
		// beaconManager.setBackgroundScanPeriod(250, 750);
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override public void onServiceReady() {
				try {
					if(beaconManager.checkPermissionsAndService()){
						beaconManager.startMonitoring(CIVICO_ESTIMOTE_BEACONS);
						Log.e(BeaconsModule.TAG, "Started Civico-TiBeacons Service!");
					}
				} catch (RemoteException e) {
					Log.e(BeaconsModule.TAG, "Monitoring is unavailable, Civico-TiBeacons Service :(", e);
				}
		    }
		});
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    	beaconManager.disconnect();
    }
    
    public void ableToNotify(boolean b) {
    	ableToNotify = b;
    }
    
    public boolean ableToNotify() {
    	return ableToNotify;
    }
}
