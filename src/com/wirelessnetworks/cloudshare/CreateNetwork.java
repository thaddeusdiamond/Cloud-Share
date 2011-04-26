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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateNetwork extends Activity {

	private String networkName, username;
	private Button createNetwork;
	private Toast networkNull, usernameNull;
	HttpResponse response;
	
	
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
			}
		});
		postData ();
		
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
	        nameValuePairs.add(new BasicNameValuePair("u_uniqueid", CloudShare.macAddr));
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
