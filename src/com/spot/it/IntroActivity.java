package com.spot.it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

public class IntroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		// Log in button click handler
		((Button) findViewById(R.id.logIn))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// Starts an intent of the log in activity
						startActivity(new Intent(IntroActivity.this,
								LogInActivity.class));
					}
				});

		// Sign up button click handler
		((Button) findViewById(R.id.signUp))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// Starts an intent of the log in activity
						startActivity(new Intent(IntroActivity.this,
								SignUpActivity.class));
					}
				});

		// Log in with twitter
		((Button) findViewById(R.id.twitterButton))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						ParseTwitterUtils.logIn(IntroActivity.this,
								new LogInCallback() {
									@Override
									public void done(ParseUser user,
											ParseException err) {
										if (user == null) {
											Log.d("MyApp",
													"Uh oh. The user cancelled the Twitter login.");
										} else if (user.isNew()) {
											Log.d("MyApp",
													"User signed up and logged in through Twitter!");
											Intent intent = new Intent(
													IntroActivity.this,
													SpotActivity.class);
											intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
													| Intent.FLAG_ACTIVITY_NEW_TASK);
											startActivity(intent);
										} else {
											Log.d("MyApp",
													"User logged in through Twitter!");
											Intent intent = new Intent(
													IntroActivity.this,
													SpotActivity.class);
											intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
													| Intent.FLAG_ACTIVITY_NEW_TASK);
											startActivity(intent);
										}
									}
								});
					}
				});
	}

}
