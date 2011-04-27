package com.wirelessnetworks.cloudshare;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateNetwork extends Activity {

	private String networkName, username, androidId = null, registrationKey = null;
	private Button createNetwork;
	private Toast networkNull, usernameNull, locationNull, tempUnavailable, outOfService,
			androidIdNull, regKeyNull;
	HttpResponse response;
	
	private Location mLocation = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Intent alertIntent;
    private SharedPreferences regPreference;
    
	
	// MAKE SURE TO CHECK THAT ALL DATA IS VALID BEFORE POSTING
	// FETCH THE REGISTRATION KEY HERE
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_network);
		createNetwork = (Button) findViewById (R.id.create_network);
		createNetwork.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Extract information from the text edit boxes,
				// verify that is it is valid, do HTTP POST
				networkName = ((EditText) findViewById (R.id.networkName)).getText().toString();
				username = ((EditText) findViewById (R.id.username)).getText().toString();
				androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
				regPreference = getSharedPreferences(getApplicationContext().getString(R.string.registration_preference), Context.MODE_PRIVATE);
				registrationKey = regPreference.getString(getApplicationContext().getString (R.string.registration_key), null);
				if (networkName.length() == 0) {
					networkNull = Toast.makeText(getApplicationContext(),
							R.string.networkName_toast_null, Toast.LENGTH_SHORT);
					networkNull.show();
					return;
				}
				if (username.length() == 0) {
					usernameNull = Toast.makeText(getApplicationContext(),
							R.string.username_toast_null, Toast.LENGTH_SHORT);
					usernameNull.show();
					return;
				}
				if (mLocation == null) {
					locationNull = Toast.makeText(getApplicationContext(),
							R.string.location_toast_null, Toast.LENGTH_LONG);
					locationNull.show();
					return;
				}
				if (androidId == null) {
					androidIdNull = Toast.makeText(getApplicationContext(),
							R.string.androidid_toast_null, Toast.LENGTH_LONG);
					androidIdNull.show();
					finish ();
					return;
				}
				if ((registrationKey == null) || (registrationKey.length() == 0)){
					regKeyNull = Toast.makeText(getApplicationContext(), R.string.regkey_toast_null, Toast.LENGTH_LONG);
					regKeyNull.show();
					return;
				}
				response = CloudShareUtils.postData("create", new String[] {"n_name",  "u_name", "latitude", "longitude", "u_unique_id", "u_platform", "u_registration_id"},
						new String[] {networkName, username, Double.toString(mLocation.getLatitude()), Double.toString(mLocation.getLongitude()), androidId , "Android", registrationKey});
			}
		});

		// Acquire initial location
        // --------------------------------------------------------------------------
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              mLocation = location;
              locationManager.removeUpdates(locationListener);
            }

    		// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
            public void onStatusChanged(String provider, int status, Bundle extras) {
            	if (status == android.location.LocationProvider.TEMPORARILY_UNAVAILABLE) {
            		tempUnavailable = Toast.makeText(getApplicationContext(),
            				R.string.gps_toast_tempunavailable, Toast.LENGTH_LONG);
            		tempUnavailable.show();
            		return;
            	}
            	if (status == android.location.LocationProvider.OUT_OF_SERVICE) {
            		outOfService = Toast.makeText(getApplicationContext(),
            				R.string.gps_toast_outofservice, Toast.LENGTH_LONG);
            		outOfService.show();
            		finish ();
            		return;
            	}
            }

            // UNNECESSARY
            public void onProviderEnabled(String provider) {}
            
            
            public void onProviderDisabled(String provider) {
            	alertIntent = new Intent ();
		    	alertIntent.setClass (getApplicationContext(), CloudShareAlert.class);
		    	alertIntent.setAction (CloudShareAlert.class.getName());
		    	alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		    			Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	alertIntent.putExtra("title", getApplicationContext().getString(R.string.gps_dialog_title));
		    	alertIntent.putExtra("dialog", getApplicationContext().getString(R.string.gps_dialog_msg));
		    	alertIntent.putExtra("action", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		    	startActivity (alertIntent);
            }
          };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        // ----------------------------------------------------------------------------

		
	}
	
}
