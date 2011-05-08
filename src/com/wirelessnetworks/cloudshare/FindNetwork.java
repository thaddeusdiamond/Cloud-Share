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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FindNetwork extends Activity implements Runnable {

	private ProgressDialog mProgressDialog;
    private Toast outOfService;
	
	private HttpResponse response;
	private String mAndroidId;
	
	private Location mLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Intent alertIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_networks);
		
		fillNetworks();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		fillNetworks();
	}
	
	private void fillNetworks() {
		mAndroidId = getIntent().getStringExtra("androidId");
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Detecting Networks in Your Area");
		mProgressDialog.setMessage(getApplicationContext().getString(R.string.progress_dialog));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		
		// Acquire initial location
        // --------------------------------------------------------------------------
        // Acquire a reference to the system Location Manager
		mLocation = null;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              mLocation = location;
              locationManager.removeUpdates(locationListener);
              response = CloudShareUtils.postData("detect", new String[] { "latitude", "longitude" }, new String[] { Double.toString(mLocation.getLatitude()), Double.toString(mLocation.getLongitude()) });
              
              boolean found_networks = false;
              try {
				found_networks = processHTTPResponse(response);
              } catch (Exception e) {
            	mHandler.sendEmptyMessage(2);            	 
              }
			  
			  if (found_networks) {
				  TextView text = (TextView) findViewById(R.id.join_error_msg);
				  ListView lv = (ListView) findViewById(R.id.network_list);
				  text.setVisibility(View.GONE);
				  lv.setVisibility(View.VISIBLE);
			  }
			  mProgressDialog.dismiss(); 
            }

    		/// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
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
          

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
        // ----------------------------------------------------------------------------
        
        Thread main = new Thread(this);
        main.start();
	}
	
	public void run() {
		try { Thread.sleep(10000); }
		catch (InterruptedException e) { Log.v("THREAD", "Sleep Thread Error"); }
		
		if (mLocation == null)
			mHandler.sendEmptyMessage(0);
	}
	
	private boolean processHTTPResponse(HttpResponse response) throws Exception {
		boolean found_networks = false;
		try {
			String result = CloudShareUtils.parseHttpResponse(response);
			Document doc = CloudShareUtils.getDOMbody(result);
		    
		    //Beginning information
		    doc.getDocumentElement().normalize();
		    NodeList networks = doc.getElementsByTagName("network");
		    
		    if (networks.getLength() > 0) {
		    	String[] network_ids = new String[networks.getLength()]; 
			    String[] network_names = new String[networks.getLength()];
			    String[] network_num_users = new String[networks.getLength()];
			    String[] network_createds = new String[networks.getLength()];
			    
			    // Go through the networks and parse out
			    for (int i = 0; i < networks.getLength(); i++) {
			    	Node network = networks.item(i);
			    	if (network.getNodeType() == Node.ELEMENT_NODE) {
			    	    Element network_el = (Element) network;
			    	    String[] information = CloudShareUtils.getDOMresults(network_el, 
			    	    													 new String[] {"id", "name", "num_members", "latitude", "longitude", "created"});
			    	    // NEED TO ADD TO LIST HERE
			    	    network_ids[i] = information[0];
			    	    network_names[i] = information[1];
			    	    network_num_users[i] = information[2];
			    	    network_createds[i] = information[5];
			    	}
			    }
			    
			    ListView lv = (ListView) findViewById(R.id.network_list);
		        lv.setTextFilterEnabled(true);
		        
		        NetworkAdapter n_adapter = new NetworkAdapter(this, R.id.network_created, network_ids, network_names, network_num_users, network_createds);
		        lv.setAdapter(n_adapter);
		        lv.setOnItemClickListener(new OnItemClickListener() {
		          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        	// When clicked, launch the join the network activity (prompt for username)
		            Intent networkMain = new Intent(getApplicationContext(), JoinNetwork.class);
		            networkMain.putExtra("network_id", ((TextView) view.findViewById(R.id.network_id)).getText());
		            networkMain.putExtra("u_unique_id", mAndroidId);
		            networkMain.putExtra("latitude", Double.toString(mLocation.getLatitude()));
		            networkMain.putExtra("longitude", Double.toString(mLocation.getLongitude()));
		            startActivity(networkMain);
		          }
		        });
		        found_networks = true;
		    } else {
		    	mHandler.sendEmptyMessage(1);
		    }
		    
		} catch (Exception e) {
			throw e;
		}
		return found_networks;
	}
	
	private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	ListView lv = (ListView) findViewById(R.id.network_list);
			lv.setVisibility(View.GONE);
			TextView text = (TextView) findViewById(R.id.join_error_msg);
			if (msg.what == 1)
				text.setText("No networks were found in your area.  Please create one from the home page, or try again later.");
			else if (msg.what == 2)
				text.setText("There was an error communicating with the server.  Please make sure your internet is enabled and try again.");
			text.setVisibility(View.VISIBLE);
			
			mProgressDialog.dismiss(); 
		    locationManager.removeUpdates(locationListener);
        }
	};
	
	private class NetworkAdapter extends ArrayAdapter<String> {

		private String[] network_ids; 
	    private String[] network_names;
	    private String[] network_num_users;
	    private String[] network_createds;
	    
        public NetworkAdapter(Context context, int textViewResourceId, String[] network_ids, String[] network_names, 
        						String[] network_num_users, String[] network_createds) {
                super(context, textViewResourceId, network_ids);
                this.network_ids = network_ids;
                this.network_names = network_names;
                this.network_num_users = network_num_users;
                this.network_createds = network_createds;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.network_item, null);
                }
                String n_name = network_names[position];
                String n_num_users = network_num_users[position];
                String n_id = network_ids[position];
                String n_created = network_createds[position];
                
                if (n_name != null) {
                        TextView name = (TextView) v.findViewById(R.id.network_title);
                        TextView num_users = (TextView) v.findViewById(R.id.network_num_users);
                        TextView created = (TextView) v.findViewById(R.id.network_created);
                        TextView id = (TextView) v.findViewById(R.id.network_id);
                        
                        if (name != null)
                        	name.setText(n_name);
                        if (num_users != null)
                        	num_users.setText(n_num_users + " Active User" + (Integer.parseInt(n_num_users) > 1 ? "s" : ""));
                        if (id != null) 
                        	id.setText(n_id);
                        if (created != null)
                        	created.setText(n_created);
                }
                return v;
        }
}
}
