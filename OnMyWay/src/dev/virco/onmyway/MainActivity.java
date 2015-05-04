package dev.virco.onmyway;


import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class MainActivity extends ActionBarActivity {
	
	Button loginButton;
	ProgressDialog dialog = null;
	static AccessToken token = null;
	ArrayList<String> permissions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setTitle("OnMyWay");
		loginButton = (Button) findViewById(R.id.main_activity_login_button);
		permissions = new ArrayList<String>();
		permissions.add("user_friends");
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				v.setEnabled(false);
				ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, permissions, new LogInCallback() {
					
					@Override
					public void done(ParseUser user, ParseException e) {
						if (user == null) {
							Log.d("omw", "Uh oh. The user cancelled the Facebook login.");
						    toastMessage("Facebook Login Canceled.");
						    v.setEnabled(true);
						    
						} else if (user.isNew()) {
							(new LinkFacebookToParseUserAsyncTask())
								.execute();
						    Log.d("omw", "User signed up and logged in through Facebook!");
						    
						} else {
							startListActivity();
							Log.d("omw", "User logged in through Facebook!");
						}
					}
				});
			}
		});
		
		if (ParseUser.getCurrentUser() != null) startListActivity();
	}
	
	public class LinkFacebookToParseUserAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Creating new account...");
			dialog.show();
			token = AccessToken.getCurrentAccessToken();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			GraphRequest request = GraphRequest.newMeRequest(token, new GraphJSONObjectCallback() {
				
				@Override
				public void onCompleted(JSONObject object, GraphResponse response) {
					Log.d("omw", object.toString());
					String fbID = null;
					String fullName = null;
					String firstName = null;
					String picURL = null;
					try {
						fbID = object.getString(OMWConstants.Facebook.ID);
						fullName = object.getString(OMWConstants.Facebook.FULL_NAME);
						firstName = object.getString(OMWConstants.Facebook.FIRST_NAME);
						picURL = object.getJSONObject("picture").getJSONObject("data").getString("url");
					} catch (JSONException e) {
						Log.e("omw", e.getMessage());
						toastMessage(e.getMessage());
					}
					
					ParseUser currentUser = ParseUser.getCurrentUser();
					currentUser.put(OMWConstants.ParseUser.FACEBOOK_ID, fbID);
					currentUser.put(OMWConstants.ParseUser.FIST_NAME, firstName);
					currentUser.put(OMWConstants.ParseUser.FULL_NAME, fullName);
					currentUser.put(OMWConstants.ParseUser.PROFILE_PIC, picURL);
					try {
						currentUser.save();
					} catch (ParseException e) {
						Log.e("omw", e.getMessage());
						toastMessage(e.getMessage());
					}
				}
			});
			 
			Bundle parameters = new Bundle();
			parameters.putString("fields", "id,first_name,name,picture.width(320)");
			request.setParameters(parameters);
			request.executeAndWait();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog != null) dialog.dismiss();
			startListActivity();
		}
		
	}
	
	public void startListActivity() {
		if (dialog != null ) dialog.dismiss();
		startActivity(new Intent(this, ListActivity.class));
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) return true;
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
	}
	
	public void toastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
