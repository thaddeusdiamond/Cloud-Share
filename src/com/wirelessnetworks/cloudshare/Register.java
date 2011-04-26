package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class Register extends Activity {
	Intent data;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView (R.layout.loading);
		data = getIntent ();
		createAlert (data.getStringExtra("title"), data.getStringExtra("dialog"),
				data.getStringExtra("action"));
	}
	
	
	private void createAlert (String title, String dialog, final String action) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    	builder.create().show();
    }

}
