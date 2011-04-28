package com.wirelessnetworks.cloudshare;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class LeaveNetwork extends Service {

	private Intent mIntent;
	private String mRegistrationKey;
	
	private final int TIMEOUT = 10;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		// GET REGISTRATION ID AND VIEWS
		SharedPreferences regPreference = getSharedPreferences(getString(R.string.registration_preference), Context.MODE_PRIVATE);
	    mRegistrationKey = regPreference.getString(getString (R.string.registration_key), null);
		mIntent = intent;
		
		// SEND A POST REQUEST TO LEAVE AND KEEP RE-TRYING UNTIL YOU GET A SUCCESS TAG
		int i = 0;
		while (i++ < TIMEOUT) {
			HttpResponse response = CloudShareUtils.postData("leave", new String[] {"nid", "u_name", "u_registration_id", "u_platform", "u_unique_id", "latitude", "longitude" }, 
					new String[] { mIntent.getStringExtra("network_id"), "bogus", mRegistrationKey, "Android", mIntent.getStringExtra("u_unique_id"), mIntent.getStringExtra("latitude"), mIntent.getStringExtra("longitude") });
			String result = null;
			try {
				result = CloudShareUtils.checkErrors(response);
            } catch (Exception e) {
				Log.v("LEAVE ERROR", "UNABLE TO LEAVE NETWORK");         	 
            }
            
            if (result != null) 
            	break;
		}
		
		stopSelf();
		return Activity.RESULT_OK;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onDestroy () {
		super.onDestroy();
	}
	
	
}
