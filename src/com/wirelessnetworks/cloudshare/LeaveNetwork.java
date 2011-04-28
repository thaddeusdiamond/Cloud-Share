package com.wirelessnetworks.cloudshare;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LeaveNetwork extends Activity implements Runnable {

	private Intent mIntent;
	private String mRegistrationKey;
	
	private final int TIMEOUT = 10;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		
		// GET REGISTRATION ID AND VIEWS
		SharedPreferences regPreference = getSharedPreferences(getString(R.string.registration_preference), Context.MODE_PRIVATE);
	    mRegistrationKey = regPreference.getString(getString (R.string.registration_key), null);
		mIntent = getIntent();
		
		Thread main = new Thread(this);
		main.start();
	}
	
	public void run() {
		// SEND A POST REQUEST TO LEAVE AND KEEP RE-TRYING UNTIL YOU GET A SUCCESS TAG
		HttpResponse response = CloudShareUtils.postData("leave", new String[] {"nid", "u_name", "u_registration_id", "u_platform", "u_unique_id", "latitude", "longitude" }, 
				new String[] { mIntent.getStringExtra("network_id"), "", mRegistrationKey, "Android", mIntent.getStringExtra("u_unique_id"), mIntent.getStringExtra("latitude"), mIntent.getStringExtra("longitude") });
		int i = 0;
		while (i < TIMEOUT) {
			String result = null;
			try {
				result = CloudShareUtils.checkErrors(response);
            } catch (Exception e) {
				Log.v("LEAVE ERROR", "UNABLE TO LEAVE NETWORK");         	 
            }
            
            if (result != null) {
            	mHandler.sendEmptyMessage(0);
            	return;
            }
		}
    	mHandler.sendEmptyMessage(1);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// FINISH ACTIVITY WITH POSITIVE RESULT
				setResult(RESULT_OK);
				break;
				
			case 1:
				// FINISH ACTIVITY WITH NEGATIVE RESULT
				setResult(RESULT_CANCELED);
				break;
			
			}
			finish();
		}
	};
	
}
