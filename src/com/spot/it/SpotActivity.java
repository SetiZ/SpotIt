package com.spot.it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

public class SpotActivity extends Activity {

	public SpotActivity() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check if there is current user info
		if (ParseUser.getCurrentUser() != null) {
			// Start an intent for the logged in activity
			startActivity(new Intent(this, MainActivity.class));
		} else {
			// Start and intent for the logged out activity
			startActivity(new Intent(this, IntroActivity.class));
		}
	}
}
