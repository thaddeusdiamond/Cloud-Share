// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
//
// NetworkMain
//
// - This is the Activity representing the UI for being within a network
//
// ============================================================================

package com.wirelessnetworks.cloudshare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkMain extends Activity implements Runnable {
	private Intent mIntent, alertIntent, leaveIntent;
	private String mResult, network_name, num_members, created_at,
		latitude, longitude, network_id, android_id, mMessage, bestProvider;
	
	private ArrayList<String[]> mMessages = new ArrayList<String[]>();
	private ArrayList<String[]> mMembers = new ArrayList<String[]>();
	private MemberAdapter mMemberAdapter;
	
	private EditText mMessageInput;
	private LinearLayout mChatArea;
	Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, EEE MMM dd");
	
	private Document mDoc;
	private String [] member_fields = {"id", "name", "latitude", "longitude"};
	private NodeList networks, members;
	private Node network, member;
	private Element network_element;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location mLocation;
	private Toast outOfService;
	
	private static final double LOC_OFFSET = 0.001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		mIntent = getIntent();
		mResult = mIntent.getStringExtra("networkXml");
		
		// Setup/register messageReceiver
		// -----------------------------------------------------------------------------
		IntentFilter mIntentFilter = new IntentFilter ("com.wirelessnetworks.cloudshare.NEW_MESSAGE");
		registerReceiver(messageReceiver, mIntentFilter);
		mIntentFilter = new IntentFilter ("com.wirelessnetworks.cloudshare.USER_JOINED");
		registerReceiver(messageReceiver, mIntentFilter);
		mIntentFilter = new IntentFilter ("com.wirelessnetworks.cloudshare.USER_LEFT");
		registerReceiver(messageReceiver, mIntentFilter);
		
		// Acquire initial location
        // --------------------------------------------------------------------------
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        bestProvider = locationManager.getBestProvider(criteria, true);
        
        mLocation = locationManager.getLastKnownLocation(bestProvider);
        latitude = Double.toString(mLocation.getLatitude());
        longitude = Double.toString(mLocation.getLongitude());
        
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              if ((Math.abs(location.getLatitude() - Double.parseDouble(latitude)) > LOC_OFFSET) ||
            	  (Math.abs(location.getLatitude() - Double.parseDouble(latitude)) > LOC_OFFSET)) {
            	  finish();
            	  
              	alertIntent = new Intent ();
		    	alertIntent.setClass (getApplicationContext(), CloudShareAlert.class);
		    	alertIntent.setAction (CloudShareAlert.class.getName());
		    	alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	alertIntent.putExtra("title", "Out of Network Range");
		    	alertIntent.putExtra("dialog", "You have wandered too far away from the network.  Please select a new one to join."); 
		    	alertIntent.putExtra("action", "");
		    	startActivity (alertIntent);
              }
            
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
		
		//Start main thread
        Thread main = new Thread(this);
		main.start();
	}

	// Do all the parsing and setting up here and in the handler
	@Override
	public void run() {
		// Begin parsing data
		// -----------------------------------------------------------------------------
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
			for (int i = 0; i < members.getLength(); i++) {
				member = members.item(i);
				if (member.getNodeType() == Node.ELEMENT_NODE) {
					Element member_element = (Element) member;
					String[] information = CloudShareUtils.getDOMresults(member_element, member_fields);
					// NEED TO ADD TO LIST HERE
					String[] memberInformation = new String[] {information[0], information[1], CloudShareUtils.reverseLocation(getApplicationContext(), information[2], information[3]) };
					mMembers.add(memberInformation);
				}
			}
		}
		// ----------------------------------------------------------
		
		// Done parsing, go set up UI
		mHandler.sendEmptyMessage(0);
	}
	
	private Handler mHandler = new Handler () {
		public void handleMessage (Message msg) {
			switch(msg.what) {
			case 0:
				setContentView(R.layout.network_main);
				
				// Find all view layouts
				TextView header_view = (TextView) findViewById(R.id.header_text);
				header_view.setText(header_view.getText() + " - " + network_name);
				TextView num_members_view = (TextView) findViewById(R.id.num_members);
				num_members_view.setText(num_members + " User" + (Integer.parseInt(num_members) != 1 ? "s" : "") + " Active");
				TextView created_at_view = (TextView) findViewById(R.id.network_created);
				created_at_view.setText(created_at);
	
				mChatArea = (LinearLayout) findViewById(R.id.network_chat_area);
				mMessageInput = (EditText) findViewById(R.id.messageInput);
				if (msg.obj != null)
					mMessageInput.setText((String) msg.obj);
				Button send = (Button) findViewById(R.id.send);
				
				// Set a listener for when we want to send a message
				send.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mMessage = mMessageInput.getText().toString();
						// Only send non-null messages
						if (mMessage.length() > 0) {
							mMessages.add(new String[] {"You", sdf.format(cal.getTime()), mMessage});
							mMessageInput.setText("");
							
				        	createNewChat("You", sdf.format(cal.getTime()), mMessage);
							mHandler.sendEmptyMessage(2);
							new SendMessage().execute();
						}
						
						// Hide the keyboard
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mMessageInput.getWindowToken(), 0);
					}
				});
				
				ListView lv = (ListView) findViewById(R.id.members_list);
		        lv.setTextFilterEnabled(true);
		        
		        //  After rotation add back all messages
		        for (int i = 0; i < mMessages.size(); i++) {
		        	String[] messageContents = mMessages.get(i);
		        	createNewChat(messageContents[0], messageContents[1], messageContents[2]);
		        }
				mHandler.sendEmptyMessage(2);
		        
		        mMemberAdapter = new MemberAdapter(getApplicationContext(), R.id.member_name, mMembers);
		        lv.setAdapter(mMemberAdapter);
		        break;
		        
			case 1:
				// Server error toast
				Toast.makeText(getApplicationContext(), "There may have been an error sending your message", Toast.LENGTH_SHORT).show();
				break;
				
			case 2:
				// Asynchronous scroll to bottom (after view loads)
				((ScrollView) findViewById(R.id.network_chat_scroll)).scrollTo(0, mChatArea.getHeight() + 50);
				break;
			}
		}
	};
	
	// Create a new chat (simple view inflater)
	private void createNewChat(String sender, String timestamp, String content) {
		View newChat = getLayoutInflater().inflate(R.layout.chat, null);
		((TextView) newChat.findViewById(R.id.chatSender)).setText(sender);
		((TextView) newChat.findViewById(R.id.chatTimestamp)).setText(timestamp);
		((TextView) newChat.findViewById(R.id.chatContent)).setText(content);
		mChatArea.addView(newChat);
	}
	
	// Class to extend sending messages (queuing handled, therefore stopping the possibility of multi-thread exhaustion)
	private class SendMessage extends AsyncTask<String, Integer, Long> {
		@Override
		protected Long doInBackground(String...strings) {
			// Send out a broadcast
			HttpResponse response = CloudShareUtils.postData("broadcast", new String[] {"nid", "u_unique_id", "u_platform", "message", "latitude", "longitude"},
				new String[] {network_id, android_id, "Android", mMessage, Double.toString(mLocation.getLatitude()), Double.toString(mLocation.getLongitude())});
			String result;
			try {
				result = CloudShareUtils.checkErrors(response);
			} catch (Exception e) {
				mHandler.sendEmptyMessage(1);
			}
			return (long) 0;
		}
	}
	
	// Custom list adapter for members (to use an arraylist<string>)
	private class MemberAdapter extends ArrayAdapter<String[]> {

		private ArrayList<String[]> mMembers;
	    
        public MemberAdapter(Context context, int textViewResourceId, ArrayList<String[]> mMembers) {
                super(context, textViewResourceId, mMembers);
                this.mMembers = mMembers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.member_item, null);
                }
                String[] memberInformation = mMembers.get(position);
                String m_name = memberInformation[1];
            	String m_location = memberInformation[2];
                
                if (m_name != null) {
                        TextView name = (TextView) v.findViewById(R.id.member_name);
                        TextView location = (TextView) v.findViewById(R.id.member_location);
                        
                        if (name != null)
                        	name.setText(m_name);
                        if (location != null)
                        	location.setText(m_location);
                }
                return v;
        }
}
	
	/* LOCATION LIFECYCLE PROTECTION */
	@Override
	protected void onPause () {
		super.onPause();
		locationManager.removeUpdates(locationListener);
		return;
	}
	
	@Override
	protected void onResume () {
		super.onResume();
        locationManager.requestLocationUpdates(bestProvider, 120000, 100, locationListener);
	}
	/********************************/
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		// Leave the network when you destroy yourself
		leaveIntent = new Intent (NetworkMain.this, LeaveNetwork.class);
		leaveIntent.putExtra("network_id", network_id);
		leaveIntent.putExtra("u_unique_id", android_id);
		leaveIntent.putExtra("latitude", latitude);
		leaveIntent.putExtra("longitude", longitude);
		startService(leaveIntent);
		unregisterReceiver(messageReceiver);
	}

	/*				HACK TO PREVENT LEAVING NETWORK ON ROTATION				*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		String currentMessage = mMessageInput.getText().toString();
		super.onConfigurationChanged(newConfig);
		
		Message m = new Message();
		m.what = 0;
		m.obj = currentMessage;
		mHandler.sendMessage(m);
	}
    
	/*				HACK TO UPDATE THE UI FROM A C2DM RECEIVER				*/
	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	String action = intent.getAction();
        	
        	String sender_id = (String) extras.get("u_unique_id");
        	// Only display the message if it was sent by someone other than yourself
        	// Your message gets displayed in the onClickListener of the 'Send' button
        	if (!(sender_id.equals(android_id))) {
	        	// User received a new message
        		if (action.equals("com.wirelessnetworks.cloudshare.NEW_MESSAGE")) {
        			mMessages.add(new String[] {(String) extras.getString("user"), sdf.format(cal.getTime()), (String) extras.get("message")});
					createNewChat((String) extras.getString("user"), sdf.format(cal.getTime()), (String) extras.get("message"));
					mHandler.sendEmptyMessage(2);
	        	
				// User joins the network
	        	} else if (action.equals("com.wirelessnetworks.cloudshare.USER_JOINED")) {
	        		Document doc = CloudShareUtils.getDOMbody(extras.getString("message"));
	        		Element member_element = (Element) doc.getElementsByTagName("member").item(0);
	        		String[] information = CloudShareUtils.getDOMresults(member_element, member_fields);
					String[] memberInformation = new String[] {information[0], information[1], CloudShareUtils.reverseLocation(getApplicationContext(), information[2], information[3]) };
					mMembers.add(memberInformation);
					TextView num_members_view = (TextView) findViewById(R.id.num_members);
					num_members_view.setText(mMembers.size() + " User" + (mMembers.size() != 1 ? "s" : "") + " Active");
					mMemberAdapter.notifyDataSetChanged();
					mMemberAdapter.notifyDataSetChanged();
	    		
				// User leaves the network
	        	} else if (action.equals("com.wirelessnetworks.cloudshare.USER_LEFT")) {
	    			Document doc = CloudShareUtils.getDOMbody(extras.getString("message"));
	        		Element member_element = (Element) doc.getElementsByTagName("member").item(0);
	        		String[] information = CloudShareUtils.getDOMresults(member_element, member_fields);
					for (int i = 0; i < mMembers.size(); i++) {
	        			String[] memberInfo = mMembers.get(i);
	        			if (memberInfo[0].equals(information[0]))
	        				mMembers.remove(i);
	        		}
					TextView num_members_view = (TextView) findViewById(R.id.num_members);
					num_members_view.setText(mMembers.size() + " User" + (mMembers.size() != 1 ? "s" : "") + " Active");
					mMemberAdapter.notifyDataSetChanged();
	        	}
        	}
        }
    };

}
