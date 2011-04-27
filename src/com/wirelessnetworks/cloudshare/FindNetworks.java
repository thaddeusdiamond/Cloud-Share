package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class FindNetworks extends Activity {

	private Location mLocation = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Intent alertIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_network);
		
		// Acquire initial location
        // --------------------------------------------------------------------------
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              // HERE YOU ARE GOING TO GET RID OF LISTENER BECAUSE LOCATION WAS FOUND
              mLocation = location;
              locationManager.removeUpdates(locationListener);
            }

    		// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            // UNNECESSARY
            public void onProviderEnabled(String provider) {}
            
            
            public void onProviderDisabled(String provider) {
            	alertIntent = new Intent ();
		    	alertIntent.setClass (getApplicationContext(), CloudShareAlert.class);
		    	alertIntent.setAction (CloudShareAlert.class.getName());
		    	alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		    			Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	alertIntent.putExtra("title", "GPS Error");
		    	alertIntent.putExtra("dialog", getApplicationContext().getString(R.string.c2dm_dialog));
		    	alertIntent.putExtra("action", Settings.ACTION_ADD_ACCOUNT);
		    	startActivity (alertIntent);
            }
          };
	}

}
