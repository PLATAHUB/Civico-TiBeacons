package com.civico.tibeacons;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.NotificationManager;

public class MonitorListener implements BeaconManager.MonitoringListener {

    private BeaconService service;
    private NotificationManager notificationManager;
    private Bitmap Large_Icon = null;
    private TiApplication app;
    private int CIV_NOTIFICATION_ID = 24842;
    
    public MonitorListener(BeaconService service) {
        this.app = TiApplication.getInstance();
        this.service = service;
        this.notificationManager = (NotificationManager) this.service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onExitedRegion(Region region) {
        Log.d(BeaconsModule.TAG, "onExitedRegion, Civico-TiBeacons Service!");
        service.ableToNotify(true);
    }

    @Override
    public void onEnteredRegion(Region region, List<Beacon> beacons) {
        if(!beacons.isEmpty()){
            Log.d(BeaconsModule.TAG, "onEnteredRegion, Civico-TiBeacons Beacon Minor: " + beacons.get(0).getMinor());
            try {
                requestOffer( beacons.get(0) );
                } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void requestOffer(final Beacon beacon) throws MalformedURLException {
        // Make a request and get an offer based a beacon data
        AsyncHttpClient client = new AsyncHttpClient();
        URL url = new URL(BeaconsModule.getApiUrl() + "/beacons/brands/" + beacon.getMinor());
        if(BeaconsModule.getApiUrl() == null){
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TiApplication.getInstance().getApplicationContext());
        	url = new URL(prefs.getString("API_URL", null) + "/beacons/brands/" + beacon.getMinor());
        }
        client.get( url.toString(), null, new ResponseHandler(beacon));
    }

    private void generateNotification(String tittle, String message, Bitmap large_Icon, String app_id, JSONObject offer){
    	try{
    		notificationManager.cancel(CIV_NOTIFICATION_ID);
            //Notification generation process
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service)
            .setContentTitle("Civico tiene una oferta y no que mas decir.")
            .setContentText(tittle)
            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
            .setLights(Color.parseColor("#524cff"), 350, 150)
            .setDefaults(Notification.DEFAULT_ALL)
            .setStyle( new NotificationCompat.BigTextStyle().bigText(tittle+"\n"+message) )
            .setSmallIcon( TiRHelper.getApplicationResource("drawable.noticon") );
            
            Bitmap lg_icon = BitmapFactory.decodeResource( app.getApplicationContext().getResources(), TiRHelper.getApplicationResource("drawable.appicon") );
            //If theres no image, only display the clasical notification
            notificationBuilder.setLargeIcon(lg_icon);

            //Intent creation process
            Intent intent = service.getPackageManager().getLaunchIntentForPackage( app_id );
            intent.setAction(Intent.ACTION_SEND);
            Bundle bundleData = new Bundle();
            bundleData.putString("offer", offer.toString());
            intent.putExtras(bundleData);

            PackageManager packageManager = service.getPackageManager();
			List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
			boolean isIntentSafe = activities.size() > 0;
			if( isIntentSafe == true ){
				 intent.addCategory("android.intent.category.LAUNCHER");
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			     int requestID = (int) System.currentTimeMillis();
			     PendingIntent contentIntent = PendingIntent.getActivity( TiApplication.getInstance().getApplicationContext(), requestID, intent, PendingIntent.FLAG_ONE_SHOT);
				 notificationBuilder.setContentIntent(contentIntent);
			}

            //Show the notification
            Notification notification = notificationBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(CIV_NOTIFICATION_ID, notification);

            //And the vibrate
            Vibrator v = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(350);

            service.ableToNotify(false);
        }catch(Exception e){
        	Log.e(BeaconsModule.TAG, e.toString());
        }
    }

    private class ResponseHandler extends JsonHttpResponseHandler {
        private Beacon beacon;

        public ResponseHandler(Beacon beacon) {
            this.beacon = beacon;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if(response != null){
                    JSONArray mOffers = response.getJSONArray("offers");
                    if(mOffers.length() > 0){
                        Random rand = new Random();
                        int mPos = rand.nextInt( mOffers.length() );
                        final JSONObject tOffer = mOffers.getJSONObject(mPos);
                        final String tittle = tOffer.getString("name");
                        final String content_text = tOffer.getString("description");
                        //String img_url = tOffer.getString("image");
                        // For optimization change the dimension to 24x24 factor by code for now to test image times
                        //int nott_dimension = (int)((getResources().getDisplayMetrics().density+1.0f) * 24);
                        //img_url = img_url.replace("h_500", "h_"+nott_dimension);
                        //img_url = img_url.replace("w_500", "w_"+nott_dimension);
                        //Log.d("Civico", tittle+'\n'+content_text+'\n'+img_url);
                        /* Get the image of the Offer
                        * Right now it's too heavy the load of this resource, I'm going to comment it and put the
                        * default app icon instead to get a quicker notification. Also it's probably to change the
                        * cloudinary with a better size than 500x500 probably something like 50x50 instead.

                        AsyncHttpClient img_request = new AsyncHttpClient();
                        img_request.get( img_url, new FileAsyncHttpResponseHandler( TiApplication.getInstance().getApplicationContext() ) {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File response) {
                                // Do something with the file `response`
                                Log.d( "Civico", response.toString() );
                                Large_Icon = BitmapFactory.decodeFile(response.getPath());
                                generateNotification(tittle, content_text, Large_Icon, TiApplication.getInstance().getAppInfo().getId(), tOffer);
                            }

                            @Override
                            public void onFailure(int arg0, Header[] arg1,Throwable arg2, File arg3) {
                                //Do another type of notification without the image
                                generateNotification(tittle,
                                content_text,
                                null,
                                TiApplication.getInstance().getAppInfo().getId(),
                                tOffer);
                            }
                        });
                        */
                        if(service.ableToNotify()){
                            generateNotification(tittle, content_text, null, app.getAppInfo().getId(), tOffer);
                        }
                    }
                }
                } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
