package com.wirelessnetworks.cloudshare;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.json.JSONException;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FindNetwork extends Activity implements Runnable {

	private Toast tempUnavailable, outOfService;
	
	private HttpResponse response;
	
	private Location mLocation = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Intent alertIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_networks);
		
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
              response = CloudShareUtils.postData("detect", new String[] { "latitude", "longitude" }, new String[] { Double.toString(mLocation.getLatitude()), Double.toString(mLocation.getLongitude()) });
              try {
				processHTTPResponse(response);
              } catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			  
			  ImageView image = (ImageView) findViewById(R.id.loading_image);
			  image.setVisibility(View.GONE);
			  ListView lv = (ListView) findViewById(R.id.network_list);
		      lv.setVisibility(View.VISIBLE);
		        
            }

    		/// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
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
        
        Thread main = new Thread(this);
        main.start();
	}
	
	public void run() {
		try { Thread.sleep(10000); }
		catch (InterruptedException e) { Log.v("THREAD", "Sleep Thread Error"); }
		
		if (mLocation == null)
			mHandler.sendEmptyMessage(0);
	}
	
	private void processHTTPResponse(HttpResponse response) throws IllegalStateException, IOException, JSONException, NoSuchAlgorithmException {
		try {
			String result = CloudShareUtils.parseHttpResponse(response);
			Document doc = CloudShareUtils.getDOMbody(result);
		    
		    //Beginning information
		    doc.getDocumentElement().normalize();
		    NodeList networks = doc.getElementsByTagName("network");
		    
		    if (networks.getLength() > 0) {
		    	String[] network_ids = new String[networks.getLength()]; 
			    String[] network_names = new String[networks.getLength()];
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
			    	    network_createds[i] = information[5];
			    	}
			    }
			    
			    ListView lv = (ListView) findViewById(R.id.network_list);
		        lv.setTextFilterEnabled(true);
		        
		        NetworkAdapter n_adapter = new NetworkAdapter(this, R.id.network_created, network_ids, network_names, network_createds);
		        lv.setAdapter(n_adapter);
		        lv.setOnItemClickListener(new OnItemClickListener() {
		          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        	
		            // When clicked, launch the view activity (AND HANDLE JOINING... PERHAPS A JOIN SERVICE???)
		            Intent network_main = new Intent(getApplicationContext(), NetworkMain.class);
		            network_main.putExtra("network_id", ((TextView) view.findViewById(R.id.network_id)).getText());
		            startActivity(network_main);
		          }
		        });
		        
		    } else {
		    	mHandler.sendEmptyMessage(1);
		    }
		    
		} catch (Exception e) {
			Log.v("PARSE ERROR", e.getMessage());
		}
	}
	
	private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	TextView text = (TextView) findViewById(R.id.join_error_msg);
			ImageView image = (ImageView) findViewById(R.id.loading_image);
			if (msg.what == 1)
				text.setText("No networks were found in your area.  Please create one from the home page, or try again later.");
			text.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);
			
			locationManager.removeUpdates(locationListener);
        }
	};
	
	private class NetworkAdapter extends ArrayAdapter<String> {

		private String[] network_ids; 
	    private String[] network_names;
	    private String[] network_createds;
	    
        public NetworkAdapter(Context context, int textViewResourceId, String[] network_ids, String[] network_names, String[] network_createds) {
                super(context, textViewResourceId, network_ids);
                this.network_ids = network_ids;
                this.network_names = network_names;
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
                String n_id = network_ids[position];
                String n_created = network_createds[position];
                
                if (n_name != null) {
                        TextView name = (TextView) v.findViewById(R.id.network_title);
                        TextView created = (TextView) v.findViewById(R.id.network_created);
                        TextView id = (TextView) v.findViewById(R.id.network_id);
                        
                        if (name != null)
                        	name.setText(n_name);
                        if (id != null) 
                        	id.setText(n_id);
                        if (created != null)
                        	created.setText(n_created);
                }
                return v;
        }
}
}
