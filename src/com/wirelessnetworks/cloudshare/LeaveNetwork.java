package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.os.Bundle;

public class LeaveNetwork extends Activity implements Runnable {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		
		// GET PARAMETERS
		
		Thread main = new Thread(this);
		main.start();
	}
	
	public void run() {
		// SEND A POST REQUEST TO LEAVE AND KEEP RE-TRYING UNTIL YOU GET A SUCCESS TAG
	}
	
}
