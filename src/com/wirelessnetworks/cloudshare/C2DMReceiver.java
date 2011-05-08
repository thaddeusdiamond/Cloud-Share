// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
// 
// C2DM Receiver Class
//
// - Class extends BroadcastReceiver and has been registered in the Android
//	 Manifest with a specific Intent Filter that will enable the class to
//	 'catch' all messages sent from the Google Servers.
// - Broadcast receiver cannot interact with the main UI and we solved this
//	 by starting transparent activities.
//
// ============================================================================ 

package com.wirelessnetworks.cloudshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
	Intent alertIntent;
	
	// We must override this function by default since this it what gets called
	// automatically when a new message from C2DM arrives
	@Override
	public void onReceive(Context context, Intent intent) {
		// Registration intent received
		if (intent.getAction().equals(
				"com.google.android.c2dm.intent.REGISTRATION")) {
	        handleRegistration(context, intent);
	    // New message received, delegate to specific function
	    } else if (intent.getAction().equals(
	    		"com.google.android.c2dm.intent.RECEIVE")) {
	        handleMessage(context, intent);
	    }
	 }

	// Function to extract registration information
	// Various data is logged in the Eclipse console in order to debug faster
	private void handleRegistration(Context context, Intent intent) {
	    String registration = intent.getStringExtra("registration_id");
	    // Registration failure
	    if (intent.getStringExtra("error") != null) {
		    Log.d("c2dm", "registration failed");
		    String error = intent.getStringExtra("error");
		    // What error is it?
		    if(error.equals("SERVICE_NOT_AVAILABLE")){
		    	Log.d("c2dm", "SERVICE_NOT_AVAILABLE");
		    // ACCOUNT_MISSING token is issued when the phone has not been
		    // registered with a specific Google account. Such an account is 
		    // needed in order for C2DM to work.
		    }else if(error.equals("ACCOUNT_MISSING")){
		    	alertIntent = new Intent ();
		    	alertIntent.setClass (context, CloudShareAlert.class);
		    	alertIntent.setAction (CloudShareAlert.class.getName());
		    	alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		    			Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	alertIntent.putExtra("title",
		    			context.getString(R.string.c2dm_dialog_title));
		    	alertIntent.putExtra("dialog",
		    			context.getString(R.string.c2dm_dialog_msg));
		    	alertIntent.putExtra("action", Settings.ACTION_ADD_ACCOUNT);
		    	// To notify the user of this error, we launch a transparent
		    	// activity that displays a dialog giving the user the option
		    	// to register an account now or later.
		    	context.startActivity (alertIntent);
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
	    	
	    // Registration is successful, add the registration ID to the shared
	    // preferences. This registration is done once - every time the
	    // application starts we check the shared preferences for a registration
	    // ID and only if one doesn't exist do we register the phone.
	    } else if (registration != null) {
	    	Editor editor = context.getSharedPreferences(context.getString
	    			(R.string.registration_preference),
	    			Context.MODE_PRIVATE).edit();
            editor.putString(context.getString(R.string.registration_key),
            		registration);
    		editor.commit();
	    }
	}

	// Method used to process an incoming message. In order to communicate
	// with the main UI we send another broadcast that will be caught in
	// the main UI. All the necessary data is placed in the intent.
	private void handleMessage(Context context, Intent intent)
	{
		Bundle extras = intent.getExtras();
		String type = (String) extras.get("type");
		Intent newMsgIntent = new Intent ();
		
		if (type.equals("broadcast")) {
			newMsgIntent.putExtra("message", (String) extras.get("message"))
						.putExtra("u_unique_id", (String) extras.get("u_unique_id"))
						.putExtra("user", (String) extras.get("user"))
						.setAction("com.wirelessnetworks.cloudshare.NEW_MESSAGE");
		} else if (type.equals("join")) {
			newMsgIntent.putExtra("u_unique_id", (String) extras.get("u_unique_id"))
				.putExtra("message", (String) extras.get("message"))
				.setAction("com.wirelessnetworks.cloudshare.USER_JOINED");
		} else if (type.equals("leave")) {
			newMsgIntent.putExtra("u_unique_id", (String) extras.get("u_unique_id"))
				.putExtra("message", (String) extras.get("message"))
				.setAction("com.wirelessnetworks.cloudshare.USER_LEFT");
		}

		context.sendBroadcast (newMsgIntent);
	}
}
