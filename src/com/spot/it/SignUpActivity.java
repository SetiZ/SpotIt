package com.spot.it;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Activity which displays a login screen to the user.
 */
public class SignUpActivity extends Activity {
	// UI references.
	private EditText usernameView;
	private EditText passwordView;
	private EditText passwordAgainView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_signup);

		// Set up the signup form.
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);
		passwordAgainView = (EditText) findViewById(R.id.passwordagain);

		// Set up the submit button click handler
		findViewById(R.id.logingo).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {

						if (!isMatching(passwordView, passwordAgainView)) {
							Toast.makeText(SignUpActivity.this,
									"password missmatch", Toast.LENGTH_LONG)
									.show();
							return;
						}

						// Set up a progress dialog
						final ProgressDialog dlg = new ProgressDialog(
								SignUpActivity.this);
						dlg.setTitle("Please wait.");
						dlg.setMessage("Signing up.  Please wait.");
						dlg.show();

						// Set up a new Parse user
						ParseUser user = new ParseUser();
						user.setUsername(usernameView.getText().toString());
						user.setPassword(passwordView.getText().toString());
						// Call the Parse signup method
						user.signUpInBackground(new SignUpCallback() {

							@Override
							public void done(ParseException e) {
								dlg.dismiss();
								if (e != null) {
									// Show the error message
									Toast.makeText(SignUpActivity.this,
											e.getMessage(), Toast.LENGTH_LONG)
											.show();
								} else {
									// Start an intent for the dispatch activity
									Intent intent = new Intent(
											SignUpActivity.this,
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

	/*
	 * private boolean isEmpty(EditText etText) { if
	 * (etText.getText().toString().trim().length() > 0) { return false; } else
	 * { return true; } }
	 */
	private boolean isMatching(EditText etText1, EditText etText2) {
		if (etText1.getText().toString().equals(etText2.getText().toString())) {
			return true;
		} else {
			return false;
		}
	}

}
