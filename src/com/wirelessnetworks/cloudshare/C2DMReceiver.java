package com.wirelessnetworks.cloudshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
	        handleRegistration(context, intent);
	    } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
	        handleMessage(context, intent);
	    }
	 }

	private void handleRegistration(Context context, Intent intent) {
	    String registration = intent.getStringExtra("registration_id");
	    if (intent.getStringExtra("error") != null) {
	        // Registration failed, should try again later.
		    Log.d("c2dm", "registration failed");
		    String error = intent.getStringExtra("error");
		    if(error.equals("SERVICE_NOT_AVAILABLE")){
		    	Log.d("c2dm", "SERVICE_NOT_AVAILABLE");
		    }else if(error.equals("ACCOUNT_MISSING")){
		    	Log.d("CONTEXT", context.toString());
		    	CloudShare.c2dmError.sendEmptyMessage(0);
		    	//CloudShare.createAlert("C2DM Error", context.getString(R.string.c2dm_dialog), Settings.ACTION_SYNC_SETTINGS, context);
		    	Log.d("c2dm", "ACCOUNT_MISSING");
		    }else if(error.equals("AUTHENTICATION_FAILED")){
		    	Log.d("c2dm", "AUTHENTICATION_FAILED");
		    }else if(error.equals("TOO_MANY_REGISTRATIONS")){
		    	Log.d("c2dm", "TOO_MANY_REGISTRATIONS");
		    }else if(error.equals("INVALID_SENDER")){
		    	Log.d("c2dm", "INVALID_SENDER");
		    }else if(error.equals("PHONE_REGISTRATION_ERROR")){
		    	Log.d("c2dm", "PHONE_REGISTRATION_ERROR");
		    }
		// Confirmation that un-registration was successful
	    } else if (intent.getStringExtra("unregistered") != null) {
	    	Log.d("c2dm", "unregistered");

	    } else if (registration != null) {
	    	Editor editor =
                context.getSharedPreferences(context.getString (R.string.registration_preference), Context.MODE_PRIVATE).edit();
            editor.putString(context.getString(R.string.registration_key), registration);
    		editor.commit();
	       // Send the registration ID to the 3rd party site that is sending the messages.
	       // This should be done in a separate thread.
	       // When done, remember that all registration is done.
	    }
	}

	private void handleMessage(Context context, Intent intent)
	{
		//Do whatever you want with the message
	}


}
