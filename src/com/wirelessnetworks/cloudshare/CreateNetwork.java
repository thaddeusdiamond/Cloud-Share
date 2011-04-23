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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;

public class CreateNetwork extends Activity {

	Editable networkNameVal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_network);
		
		// Prompt user to choose a name for the new network
		// --------------------------------------------------------------------
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.new_network_alert_title);
		alert.setMessage(R.string.new_network_alert_msg);

		// Set an EditText view to get user input 
		final EditText networkNameBox = new EditText(this);
		alert.setView(networkNameBox);

		alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  networkNameVal = networkNameBox.getText();
		  	postData ();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Kill activity
			finish ();
		  }
		});

		alert.show();
	}

	public void postData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

	    try {
	        // Add data to be sent
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("n_name", "12345"));
	        nameValuePairs.add(new BasicNameValuePair("u_name", "AndDev is Cool!"));
	        nameValuePairs.add(new BasicNameValuePair("latitude", "AndDev is Cool!"));
	        nameValuePairs.add(new BasicNameValuePair("longitude", "AndDev is Cool!"));
	        nameValuePairs.add(new BasicNameValuePair("u_mac_address", "AndDev is Cool!"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	} 
	
}
