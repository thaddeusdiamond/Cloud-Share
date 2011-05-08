// ============================================================================
// CS 434; 05/08/11; Thaddeus Diamond, Jonathan MacMillan, Anton Petrov
// 
// About Us
//
//	- Activity that is brought to the front of the main UI when the About Us
//	  button is clicked. The activity sets up its own UI which contains
//	  information about the project and the team.
//
// ============================================================================



package com.wirelessnetworks.cloudshare;

import android.app.Activity;
import android.os.Bundle;

public class AboutUs extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
	}

}
