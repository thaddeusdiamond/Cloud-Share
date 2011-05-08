// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
//
// Cloud Share Utilities
//
// - Extensive library of functions to ease in development and let the activitys
//	 remain concise
//
// ============================================================================

package com.wirelessnetworks.cloudshare;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class JoinNetwork extends Activity {

	private Intent mIntent;
	private String mRegistrationKey, savedName = null;
	private SharedPreferences namePreference;
	
	private ProgressDialog mProgressDialog;
	private Toast usernameNull, serverError;
	private Button mButton;
	private TextView mTextView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_network);
		
		// GET REGISTRATION ID AND VIEWS
		SharedPreferences regPreference = getSharedPreferences(getString(R.string.registration_preference), Context.MODE_PRIVATE);
	    mRegistrationKey = regPreference.getString(getString (R.string.registration_key), null);
		mIntent = getIntent();
	    
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Joining the Network");
		mProgressDialog.setMessage(getString(R.string.progress_dialog));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		serverError = Toast.makeText(this, "There was an error joining this network, please try again", Toast.LENGTH_LONG);
		
		mTextView = (EditText) findViewById(R.id.username);
	    mButton = (Button) findViewById(R.id.join_network);
	    
		namePreference = getSharedPreferences(getString(R.string.name_preference), Context.MODE_PRIVATE);
		savedName = namePreference.getString(getString (R.string.saved_name), null);
		if (savedName != null && savedName.length() > 0) {
			mTextView.setText(savedName);
		}
	    
	    mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDialog.show();
				new Thread(new Runnable() {
					public void run() {
						String username = mTextView.getText().toString();
						if (username.length() < 1 || username.length() > 30) {
							usernameNull = Toast.makeText(getApplicationContext(), R.string.username_toast_null, Toast.LENGTH_LONG);
							usernameNull.show();
							return;
						}
						
						HttpResponse response = CloudShareUtils.postData("join", new String[] {"nid", "u_name", "u_registration_id", "u_platform", "u_unique_id", "latitude", "longitude" }, 
									new String[] { mIntent.getStringExtra("network_id"), username, mRegistrationKey, "Android", mIntent.getStringExtra("u_unique_id"), mIntent.getStringExtra("latitude"), mIntent.getStringExtra("longitude") });
						String result;
						try {
							result = CloudShareUtils.checkErrors(response);
			            } catch (Exception e) {
							mProgressDialog.dismiss();
			            	serverError.show();
							return;           	 
			            }
			            
			            Intent networkMain = new Intent(getApplicationContext(), NetworkMain.class);
			            networkMain.putExtra("networkXml", result);
			            startActivity(networkMain);

						Editor editor =
			                getApplicationContext().getSharedPreferences(getApplicationContext().getString (R.string.name_preference), Context.MODE_PRIVATE).edit();
			            editor.putString(getApplicationContext().getString(R.string.saved_name), username);
			    		editor.commit();
			    		
			            mProgressDialog.dismiss();
						finish ();
					}
				}).start();
				
			}
		});
	}
}
