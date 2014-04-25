package com.spot.it;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LogInActivity extends Activity {

	EditText username;
	EditText password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		Button go = (Button) findViewById(R.id.logingo);

		go.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Set up a progress dialog
				final ProgressDialog dlg = new ProgressDialog(
						LogInActivity.this);
				dlg.setTitle("Please wait.");
				dlg.setMessage("Logging in...");
				dlg.show();
				// Call the Parse login method
				ParseUser.logInInBackground(username.getText().toString(),
						password.getText().toString(), new LogInCallback() {

							@Override
							public void done(ParseUser user, ParseException e) {
								if (e != null) {
									// Show the error message
									Toast.makeText(LogInActivity.this,
											e.getMessage(), Toast.LENGTH_LONG)
											.show();
								} else {
									// Start an intent for the dispatch activity
									Intent intent = new Intent(
											LogInActivity.this,
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
