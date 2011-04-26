package com.wirelessnetworks.cloudshare;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

public class CloudShare extends Activity implements Runnable {
    private Button mSearchNetwork, mCreateNetwork, mAbout;
    private boolean registrationKeyExists;
    private SharedPreferences registrationPreference;
    
    /*  C2DM Error Handling
    private static Handler c2dmError = new Handler() {
    	public void handleMessage(Message msg) {
    		CloudShare.createAlert("C2DM Error", CloudShare.this.getString(R.string.c2dm_dialog), Settings.ACTION_SYNC_SETTINGS, );
    	}
    }; */
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        
        registrationPreference = this.getSharedPreferences(this.getString(R.string.registration_preference), Context.MODE_PRIVATE);
        registrationKeyExists = registrationPreference.contains(this.getString(R.string.registration_key));
        
        Thread main = new Thread(this);
        main.start();
    }
    
    public static void createAlert (String title, String dialog, final String broadcast, final Context context) {
    	AlertDialog alertDialog = new AlertDialog.Builder(context).create();
    	alertDialog.setTitle(title);
    	alertDialog.setMessage(dialog);
    	alertDialog.setButton("Configure", new DialogInterface.OnClickListener() {
		     public void onClick(DialogInterface dialog, int which) {
		    	 Intent accountSettings = new Intent (broadcast);
		    	 context.sendBroadcast (accountSettings);
		     } 
	    }); 
    	alertDialog.setButton("Not Now", new DialogInterface.OnClickListener() {
		     public void onClick(DialogInterface dialog, int which) {
		    	 dialog.cancel();
		     } 
	    });
    	alertDialog.show();
    }
    
    public void run() {
        /********** PERFORM INITIAL BACKGROUND LOADING WHILE SPLASH SCREEN NOT YET UP
         **********
         **********/
    	Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", this.getString(R.string.senderID));
        startService(registrationIntent);
    	
        mHandler.sendEmptyMessage(0);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
         	/********* SET UP THE MAIN UI/DONE LAST AFTER CHANGING CONTENT VIEW 
        	 *********
        	 *********			Thaddeus Diamond 03-02-2011 */
        	
        	setContentView(R.layout.main);
        	
        	// The following represent the buttons, classes to launch and layouts
            Button[] buttons = { mSearchNetwork, mCreateNetwork, mAbout };
            Class<?>[] classes = { FindNetworks.class, CreateNetwork.class, AboutUs.class };
            int[] layouts = { R.id.launch_search, R.id.launch_create, R.id.launch_about };
            
            // The following are final variables accessible inside listeners
            final ArrayList<Button> button_list = new ArrayList<Button>();
            final Intent[] programs = new Intent[classes.length];
            
            // The following single for loop attaches buttons to intents via listeners
            for (int i = 0; i < programs.length; i++ ) {
            	programs[i] = new Intent(CloudShare.this, classes[i]);
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