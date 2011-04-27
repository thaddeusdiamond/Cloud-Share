package com.wirelessnetworks.cloudshare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FindNetworks extends ListActivity {

	private HttpResponse response;
	
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
            }

    		// OUT_OF_SERVICE or TEMPORARILY_UNAVAILABLE need to be handled
            public void onStatusChanged(String provider, int status, Bundle extras) {
            	if (status == LocationProvider.OUT_OF_SERVICE)
            		;
            	else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            		;
            }

            // UNNECESSARY
            public void onProviderEnabled(String provider) {}
            
            
            public void onProviderDisabled(String provider) {
            	alertIntent = new Intent ();
		    	alertIntent.setClass (getApplicationContext(), CloudShareAlert.class);
		    	alertIntent.setAction (CloudShareAlert.class.getName());
		    	alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		    			Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	alertIntent.putExtra("title", "GPS Error");
		    	//alertIntent.putExtra("dialog", getApplicationContext().getString(R.string.c2dm_dialog));
		    	alertIntent.putExtra("action", Settings.ACTION_ADD_ACCOUNT);
		    	startActivity (alertIntent);
            }
        };
          

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        // ----------------------------------------------------------------------------
	}
	
	@SuppressWarnings("unchecked")
	private void processHTTPResponse(HttpResponse response) throws IllegalStateException, IOException, JSONException, NoSuchAlgorithmException {
		try {
			// Parse the xml input resulting from a refresh post
			StringBuilder results = new StringBuilder();
			InputStream xml_input = response.getEntity().getContent();
			BufferedReader xml_reader = new BufferedReader(new InputStreamReader(xml_input), 8192);
			String result = null;
			while ((result = xml_reader.readLine()) != null) {
				results.append(result);
			}
			xml_reader.close();
			
			
			StringReader reader = new StringReader( results.toString() );
			InputSource inputSource = new InputSource( reader );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(inputSource);
		    reader.close();
		    
		    //Beginning information
		    doc.getDocumentElement().normalize();
		    NodeList networks = doc.getElementsByTagName("network");
		    
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
		    
		    // Now create a simple cursor adapter and set it to display
	        ArrayAdapter network_name_adapter = 
	        	    new ArrayAdapter(this, R.layout.network_item, R.id.network_title, network_names);
	        setListAdapter(network_name_adapter);
		    
		    ListView lv = getListView();
	        lv.setTextFilterEnabled(true);

	        lv.setOnItemClickListener(new OnItemClickListener() {
	          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        	
	            // When clicked, launch the view activity
	            Intent network_main = new Intent(getApplicationContext(), NetworkMain.class);
	            
	            startActivity(network_main);
	          }
	        });
		    
		} catch (Exception e) {
			Log.v("PARSE ERROR", e.getMessage());
		}
	}
	
	
}
