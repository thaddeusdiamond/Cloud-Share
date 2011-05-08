// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
// 
// CloudShare Splash Screen Class
//
// - This is the activity that the user first sees when the application starts.
// - All the work in setting up the UI is done in a handler.
// - Checks if the phone has already been registered.
// 
// ============================================================================


package com.wirelessnetworks.cloudshare;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;

public class CloudShare extends Activity implements Runnable {
    private Button mSearchNetwork, mCreateNetwork, mAbout;
    private boolean regKeyExists;
    private SharedPreferences regPreference;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        
        // Check whether a C2DM registration exists
        regPreference = this.getSharedPreferences(this.getString
        		(R.string.registration_preference), Context.MODE_PRIVATE);
        regKeyExists = regPreference.contains
        		(this.getString(R.string.registration_key));
                
        Thread main = new Thread(this);
        main.start();
    }
    @Override
    public void onRestart() {
    	super.onRestart();
    	// The registration is done here as well because in the case that the
    	// user opts not to register and then comes back to the application at
    	// a later time, onRestart will be called instead of onCreate and
    	// we need to give the user the opportunity to register
    	if (!regKeyExists) {
    		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
    		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
    		registrationIntent.putExtra("sender", this.getString(R.string.senderID));
    		startService(registrationIntent);
    	}
    	return;
    }
    
    public void run() {
    	// Perform initial loading before setting up the main UI
    	if (!regKeyExists) {
    		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
    		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
    		registrationIntent.putExtra("sender", this.getString(R.string.senderID));
    		startService(registrationIntent);
    	}
        mHandler.sendEmptyMessage(0);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	// Handler code is executed in the main UI's thread and this is where
        	// we setup the activity's content
        	
        	setContentView(R.layout.main);
        	
        	// The following represent the buttons, classes to launch and layouts
            Button[] buttons = { mSearchNetwork, mCreateNetwork, mAbout };
            Class<?>[] classes = { FindNetwork.class, CreateNetwork.class, AboutUs.class };
            int[] layouts = { R.id.launch_search, R.id.launch_create, R.id.launch_about };
            
            // The following are final variables accessible inside listeners
            final ArrayList<Button> button_list = new ArrayList<Button>();
            final Intent[] programs = new Intent[classes.length];
            
            // The following single for loop attaches buttons to intents via listeners
            for (int i = 0; i < programs.length; i++ ) {
            	programs[i] = new Intent(CloudShare.this, classes[i]);
            	// Add data to intent
            	programs[i].putExtra("androidId", Secure.getString(getContentResolver(),
            			Secure.ANDROID_ID));
            	buttons[i] = (Button) findViewById(layouts[i]);
            	button_list.add(buttons[i]);					// Add each button
            	
            	buttons[i].setOnClickListener(new View.OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					Button clicked = (Button) v;			// See which was clicked
    					// Once button clicked, start the activity it corresponds to
    					startActivity(programs[button_list.indexOf(clicked)]);
    				}
    			});
            }
        }
    };
}