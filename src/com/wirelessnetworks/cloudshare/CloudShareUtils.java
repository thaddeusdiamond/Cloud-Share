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

public class CloudShareUtils {

	public static HttpResponse postData(String[] parameters, String[] values) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

	    try {
	        // Add data to be sent
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        for (int i = 0; i < parameters.length; i++)
	        	nameValuePairs.add(new BasicNameValuePair(parameters[i], values[i]));
        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        return httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
		return null;
	} 
	
}
