package dev.virco.onmyway;


import java.util.ArrayList;

import org.apache.http.client.protocol.RequestAddCookies;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.session.MediaSession.Token;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends Activity {
	
	Button loginButton;
	ProgressDialog dialog = null;
	static AccessToken token = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loginButton = (Button) findViewById(R.id.main_activity_login_button);
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				v.setEnabled(false);
				ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, new ArrayList<String>(), new LogInCallback() {
					
					@Override
					public void done(ParseUser user, ParseException e) {
						if (user == null) {
							Log.d("omw", "Uh oh. The user cancelled the Facebook login.");
						    toastMessage("Facebook Login Canceled.");
						    v.setEnabled(true);
						} else if (user.isNew()) {
							(new LinkFacebookToParseUserAsyncTask()).execute();
							//getFacebookIdInBackground();
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
					} catch (JSONException e) {
						Log.e("omw", e.getMessage());
						toastMessage(e.getMessage());
						//if (dialog != null) dialog.dismiss();
					}
					
					
					GraphResponse response2 = (new GraphRequest(
							token, 
							"/me/picture?width=320", 
							null, 
							HttpMethod.GET))
							.executeAndWait();
					
					
					JSONObject pictureObject = response2.getJSONObject();
					if (pictureObject != null) {
						try {
							pictureObject = pictureObject.getJSONObject("data");
							picURL = pictureObject.getString("url");
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.e("omw", e.getMessage());
							toastMessage(e.getMessage());
							//e.printStackTrace();
						}
						
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
						//e.printStackTrace();
					}
				}
			});
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

	private void getFacebookIdInBackground() {
		
	 GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
		
		@Override
		public void onCompleted(JSONObject object, GraphResponse response) {
			if (object != null) {
				Log.d("omw", object.toString());
				String fbID = null;
				String fullName = null;
				String firstName = null;
				try {
					fbID = object.getString(OMWConstants.Facebook.ID);
					fullName = object.getString(OMWConstants.Facebook.FULL_NAME);
					firstName = object.getString(OMWConstants.Facebook.FIRST_NAME);
				} catch (JSONException e) {
					Log.e("omw", e.getMessage());
					if (dialog != null) dialog.dismiss();
				}
				
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.put(OMWConstants.ParseUser.FACEBOOK_ID, fbID);
				currentUser.put(OMWConstants.ParseUser.FIST_NAME, firstName);
				currentUser.put(OMWConstants.ParseUser.FULL_NAME, fullName);
				currentUser.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						startListActivity();
					}
				});
			} else {
				if (dialog != null) dialog.dismiss();
				toastMessage("Couldn't create account details.");
			}
		}
	 });
	 
	 Bundle parameters = new Bundle();
	 parameters.putString("fields", "id,name,first_name");
	 request.setParameters(parameters);
	 request.executeAsync();
	 
	}
	
	public void startListActivity() {
		if (dialog != null ) dialog.dismiss();
		toastMessage("StartListActivity :)");
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
