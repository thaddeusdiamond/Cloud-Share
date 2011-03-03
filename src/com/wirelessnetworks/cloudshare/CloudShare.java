package com.wirelessnetworks.cloudshare;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CloudShare extends Activity implements Runnable {
    private Button mSearchNetwork, mCreateNetwork, mAbout;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        
        Thread main = new Thread(this);
        main.start();
    }
    
    public void run() {
        /********** PERFORM INITIAL BACKGROUND LOADING WHILE SPLASH SCREEN NOT YET UP
         **********
         **********/
    	try { Thread.sleep(3000); }
    	catch (InterruptedException e) { Log.e("LOADING", "Loading Failed, Thread Interrupted"); };
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