// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
//
// Cloud Share Alert
//
// - 'Generic' alert dialog class that is called from places in the application
// 	 that do not have access to the main UI.
// - The UI here is transparent so to the user it looks like only an alert
//   dialog was called.
//
// ============================================================================

package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class CloudShareAlert extends Activity {
	Intent data;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		data = getIntent ();
		// Extract data needed for the alert dialog from the intent that
		// started the activity
		createAlert (data.getStringExtra("title"), data.getStringExtra("dialog"),
				data.getStringExtra("action"));
	}
	
	public void createAlert (String title, String dialog, final String action) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	// Depending on whether we specify an action for the positive button
    	// we can create two types of alert dialogs. One type that actually
    	// does something when the positive button and another that simply
    	// serves as a notice to the user.
    	if (action.length() > 0) {
    		builder.setTitle(title)
    			.setMessage(dialog)
    			.setPositiveButton("Configure", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					startActivity (new Intent (action));
    					finish ();
    				} 
		    }) 
	    	.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int which) {
			    	 dialog.cancel();
			    	 finish ();
			     } 
		    });
    	} else {
    		builder.setTitle(title)
				.setMessage(dialog)
				.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
				     public void onClick(DialogInterface dialog, int which) {
				    	 dialog.cancel();
				    	 finish ();
				     } 
			    });
    	}
    	builder.create().show();
    }
}
