package com.wirelessnetworks.cloudshare;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.widget.Button;
import android.widget.Toast;

public class NetworkMain extends Activity implements Runnable{
	private Intent mIntent, alertIntent;
	private String mResult, network_name, num_members, created_at,
		latitude, longitude, network_id, android_id;
	private Document mDoc;
	private Button send;
	private String [] member_names, member_locations,
		member_fields = {"name", "latitude", "longitude"};
	private NodeList networks, members;
	private Node network, member;
	private Element network_element;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location mLocation;
	private Toast outOfService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		send = (Button) findViewById(R.id.send);
		mIntent = getIntent();
		mResult = mIntent.getStringExtra("networkXml");
		
		// Acquire initial location
        // --------------------------------------------------------------------------
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        latitude = Double.toString(mLocation.getLatitude());
        longitude = Double.toString(mLocation.getLongitude());
        
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              latitude = Double.toString(location.getLatitude());
              longitude = Double.toString(location.getLongitude());
            }

    		// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
            public void onStatusChanged(String provider, int status, Bundle extras) {
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
        // ----------------------------------------------------------------------------
		
		Thread main = new Thread(this);
		main.start();
	}

	// Do all the parsing and setting up here and in the handler
	@Override
	public void run() {
		// Begin parsing data
		// -----------------------------------------------------------
		mDoc = CloudShareUtils.getDOMbody(mResult);
		networks = mDoc.getElementsByTagName("network");
		network = networks.item(0);
		network_element = (Element) network;
		
		num_members = CloudShareUtils.getTagValue("num_members", network_element);
		network_name = CloudShareUtils.getTagValue("name", network_element);
		created_at = CloudShareUtils.getTagValue("created", network_element);
		network_id = CloudShareUtils.getTagValue("id", network_element);
		android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		
		members = network_element.getElementsByTagName("member");
		if (members.getLength() > 0) {
			member_names = new String[members.getLength()];
			member_locations = new String[members.getLength()];
			for (int i = 0; i < members.getLength(); i++) {
				member = members.item(i);
				if (member.getNodeType() == Node.ELEMENT_NODE) {
					Element member_element = (Element) member;
					String[] information = CloudShareUtils.getDOMresults(member_element, member_fields);
					// NEED TO ADD TO LIST HERE
					member_names[i] = information[0];
					try {
						member_locations[i] = CloudShareUtils.reverseLocation(getApplicationContext(), information[1], information[2]);
					} catch (Exception e) {
						member_locations[i] = "Location Unavailable";
					}
				}
			}
		}
		// Done parsing
		// ----------------------------------------------------------
		
		mHandler.sendEmptyMessage (0);
	}
	
	private Handler mHandler = new Handler () {
		public void handleMessage (Message msg) {
			setContentView(R.layout.network_main);
		}
	};
	
	@Override
	protected void onPause () {
		super.onPause();
		locationManager.removeUpdates(locationListener);
		return;
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 100, locationListener);
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		Intent leaveIntent = new Intent (NetworkMain.this, LeaveNetwork.class);
		leaveIntent.putExtra("network_id", network_id);
		leaveIntent.putExtra("u_unique_id", android_id);
		leaveIntent.putExtra("latitude", latitude);
		leaveIntent.putExtra("longitude", longitude);
		startService(leaveIntent);
	}
	
}
