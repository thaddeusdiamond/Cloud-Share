package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NetworkMain extends Activity implements Runnable{
	Intent mIntent;
	String mResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		mIntent = getIntent();
		mResult = mIntent.getStringExtra("network_xml");
		
		Thread main = new Thread(this);
		main.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage (0);
	}
	
	private Handler mHandler = new Handler () {
		public void handleMessage (Message msg) {
			setContentView(R.layout.network_main);
		}
	};
}
