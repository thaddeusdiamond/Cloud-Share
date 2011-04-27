package com.wirelessnetworks.cloudshare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateNetwork extends Activity {

	private String networkName, username;
	private Button createNetwork;
	private Toast networkNull, usernameNull, locationNull;
	HttpResponse response;
	
	private Location mLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double longitude = null, latitude = null;
    private Intent alertIntent;
    
	
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
				
				if (networkName.length() == 0) {
					networkNull = Toast.makeText(getApplicationContext(),
							R.string.networkNull, Toast.LENGTH_SHORT);
					networkNull.show();
					return;
				}
				if (username.length() == 0) {
					usernameNull = Toast.makeText(getApplicationContext(),
							R.string.usernameNull, Toast.LENGTH_SHORT);
					usernameNull.show();
					return;
				}
				if ((longitude == null) || (latitude == null)) {
					locationNull = Toast.makeText(getApplicationContext(),
							R.string.location_null, Toast.LENGTH_SHORT);
					locationNull.show();
					return;
				}
				postData ();
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
              // HERE YOU ARE GOING TO GET RID OF LISTENER BECAUSE LOCATION WAS FOUND
              mLocation = location;
              latitude = mLocation.getLatitude();
              longitude = mLocation.getLongitude();
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

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        // ----------------------------------------------------------------------------

		
	}

	public void postData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

	    try {
	        // Add data to be sent
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("n_name", networkName));
	        nameValuePairs.add(new BasicNameValuePair("u_name", username));
	        nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(122.344443)));
	        nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(-22.434344)));
	        nameValuePairs.add(new BasicNameValuePair("u_uniqueid", "sosos"));
	        nameValuePairs.add(new BasicNameValuePair("u_platform", "Android"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	} 
	
}
