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
import org.w3c.dom.Element;

public class CloudShareUtils {

	private static String route_url = "http://cloudshareroute.appspot.com";
	
	public static HttpResponse postData(String path, String[] parameters, String[] values) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(route_url + "/" + path);

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
	
	public static String[] getDOMresults(Element parent, String[] fields) {
		String[] child_values = new String[fields.length];
		
		//Pull out the item's information
		for (int i = 0; i < fields.length; i++)
			child_values[i] = getTagValue(fields[i], parent);
		
		return child_values;
	}
	
	// Get a single tag from a DOM element
	private static String getTagValue(String tag, Element el){
	    return el.getElementsByTagName(tag).item(0).getChildNodes().item(0).getNodeValue();    
	 }
	
}
