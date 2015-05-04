package dev.virco.onmyway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONArrayCallback;
import com.facebook.GraphResponse;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.squareup.picasso.Picasso;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.drm.DrmStore.RightsStatus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ListMenuItemView;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends ActionBarActivity  implements OnCheckedChangeListener{

	ProgressDialog progressDialog;
	AddFriendsDialog addFriendsDialog;
	
	ListView listView;
	static Switch mySwitch;
	
	ParseQueryAdapter<ParseObject> parseaAdapter;
	Bundle friendIdsBundle;
	
	ActivityRecognitionManager manager;
	static BroadcastReceiver broadcastReceiver;
	
	static final String FRIENDS_ID_BUNDLE = "friendsidbundle";
	static final String SWITCH_POSITION = "switchposition";
	public static final String SHAREDPREFERENCES_FRIENDS = "friendsbundle";
	boolean getUpdates = false;
	
	public static void toggleSwitchoff(Context context) {
		if (mySwitch.isChecked()) {
			mySwitch.setChecked(false);
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			
			editor.putBoolean(SWITCH_POSITION, mySwitch.isChecked());
			editor.commit();
			
			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
			manager.unregisterReceiver(broadcastReceiver);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		getSupportActionBar().setTitle("OnMyWay");
		
		listView = (ListView) findViewById(R.id.list_activity_listview);
		mySwitch = (Switch) findViewById(R.id.list_activity_switch);
		
		friendIdsBundle = new Bundle();
		
		parseaAdapter = new ParseQueryAdapter<ParseObject>(this, new ParseQueryAdapter.QueryFactory<ParseObject>() {

			@Override
			public ParseQuery<ParseObject> create() {
				
				ParseRelation<ParseObject> relation = ParseUser.getCurrentUser().getRelation(OMWConstants.ParseUser.FRIENDS);
				return relation.getQuery();
			}
		}) {
			@Override
			public View getItemView(ParseObject object, View v, ViewGroup parent) {
				if (v == null) {
				    v = View.inflate(getContext(), R.layout.list_activity_item, null);
				}
				super.getItemView(object, v, parent);
				

				ImageView profilePic =  (ImageView) v.findViewById(R.id.list_activity_item_imageview);
				TextView nameTextView = (TextView) v.findViewById(R.id.list_activity_item_textview_name);
				CheckBox checkBox = (CheckBox) v.findViewById(R.id.list_activity__item_checkbox);
				checkBox.setTag(object);
				checkBox.setOnCheckedChangeListener(ListActivity.this);
				 
				if (object.getString(OMWConstants.ParseUser.PROFILE_PIC) == null) {
					 Picasso.with(getContext())
						.load(R.drawable.profile_pic)
						.into(profilePic);
				 } else {
					 Picasso.with(getContext())
						.load(object.getString(OMWConstants.ParseUser.PROFILE_PIC))
						.into(profilePic);
				 }
				 
				 nameTextView.setText(object.getString(OMWConstants.ParseUser.FIST_NAME));
				
				return super.getItemView(object, v, parent);
			}
		};
		
		listView.setAdapter(parseaAdapter);
		
		if (savedInstanceState != null) {
			friendIdsBundle = savedInstanceState.getBundle(FRIENDS_ID_BUNDLE);
			getUpdates = savedInstanceState.getBoolean(SWITCH_POSITION);
			mySwitch.setEnabled(getUpdates);
		}
		
		manager = new ActivityRecognitionManager(this);
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Object tmp = intent.getParcelableExtra(ActivityRecognitionIntentService.RECOGNITION_RESULT);
				addUpdate((ActivityRecognitionResult)tmp);
			}
			
		};
		
		mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					manager.start();
					getUpdates = true;
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					
					editor.putBoolean(SWITCH_POSITION, mySwitch.isChecked());
					editor.commit();
					toastMessage("Pushes ON");
					LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ListActivity.this);
					manager.registerReceiver(broadcastReceiver, new IntentFilter(ActivityRecognitionIntentService.BROADCAST_UPDATE));
				} else {
					manager.stop();
					toggleSwitchoff(ListActivity.this);
					getUpdates = false;
					toastMessage("Pushes OFF");
				}
				
			}
		});
	}
	
	private void addUpdate(ActivityRecognitionResult result) {
		DetectedActivity activity = result.getMostProbableActivity();
    	
    	Utils.showActivityNotification(this, activity);
    	String msg = Utils.parseActivity(activity);
    	
		Utils.logActivities(result);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        switch(requestCode){
        case ActivityRecognitionManager.FIX_PLAY_SERVICES:
        	if(resultCode == Activity.RESULT_OK){	// Play Services are available now, yey!
        		if(getUpdates){
        			manager.start();
        		}else{
        			manager.stop();
        		}
        	}
        }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(FRIENDS_ID_BUNDLE, friendIdsBundle);
		outState.putBoolean(SWITCH_POSITION, mySwitch.isChecked());
		toastMessage(mySwitch.isChecked() ? "On" : "Off");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		manager.registerReceiver(broadcastReceiver, new IntentFilter(ActivityRecognitionIntentService.BROADCAST_UPDATE));
	}
	
	@Override
	public void onPause(){
		super.onPause();
		//LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		//manager.unregisterReceiver(broadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.menu_list_add) {
			(new AddFriendsAsyncTask()).execute();
			return true;
		}
		else if (id == R.id.menu_list_logout) {
			ParseUser.logOut();
			startActivity(new Intent(this, MainActivity.class));
			finish();
			return true;
		} else if (id == R.id.menu_list_test) {
			Utils.showNotification(this, "Test", "sending a test");
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refresh() {
		parseaAdapter.loadObjects();
	}
	
	
	private class AddFriendsAsyncTask extends AsyncTask<Void, Void, List<ParseUser>> {
		
		List<String> friendIds;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(ListActivity.this);
			friendIds = new ArrayList<String>();
			progressDialog.setMessage("Gathering Friends...");
			progressDialog.show();
		}

		@Override
		protected List<ParseUser> doInBackground(Void... params) {

			GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphJSONArrayCallback() {
				
				@Override
				public void onCompleted(JSONArray objects, GraphResponse response) {
					Log.d("omw", objects.toString());
					for (int i = 0; i < objects.length(); i++) {
						try {
							friendIds.add(objects.getJSONObject(i).getString("id"));
							Log.d("omw", friendIds.get(i));
						} catch (JSONException e) {
							Log.e("omw", e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});
			
			request.executeAndWait();
			
			ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
			friendQuery.whereContainedIn(OMWConstants.ParseUser.FACEBOOK_ID, friendIds);
			
			try {
				return friendQuery.find();
			} catch (ParseException e) {
				return null;
			}
			
		}
		
		@Override
		protected void onPostExecute(List<ParseUser> result) {
			super.onPostExecute(result);
			if (progressDialog != null) progressDialog.dismiss();
			progressDialog = null;
			if (result != null) {
				if (result.size() > 0) {
					addFriendsDialog = (new AddFriendsDialog(result));
					addFriendsDialog.show(getSupportFragmentManager(), "dialog");
				}
			}
		}
		
	}
	
	private class AddFriendsDialog extends DialogFragment {
		
		ListView listView;
		Button button;
		List<ParseUser> userList;
		AddFriendsAdapter dialogAdapter;
		ArrayList<Boolean> checkedList;
		ParseRelation<ParseObject> relation;
		
		public AddFriendsDialog(List<ParseUser> userlist) {
			this.userList = userlist;
			checkedList = new ArrayList<Boolean>(userlist.size());
			for (int i = 0; i < checkedList.size(); i++) {
				checkedList.add(false);
			}
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
			relation = ParseUser.getCurrentUser().getRelation(OMWConstants.ParseUser.FRIENDS);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View v = inflater.inflate(R.layout.add_friends_dialog_fragment, container, false);
			listView = (ListView) v.findViewById(R.id.add_friends_dialog_fragment_listview);
			button = (Button) v.findViewById(R.id.add_friends_dialog_fragment_button);
			
			dialogAdapter = new AddFriendsAdapter(getActivity(), R.layout.add_friends_dialog_item, userList);
			listView.setAdapter(dialogAdapter);
			
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
	
                    for(int i = 0; i < dialogAdapter.mCheckStates.size(); i++) {
                        if(dialogAdapter.mCheckStates.get(i)) {
                        	ParseUser friend = userList.get(i);
                        	relation.add(friend);
                        	(new SaveNewFriendsAsyncTask()).execute();
                        }
                    }
                    
                    
				}		
			});
			
			return v;
		}
	}
	
	private class SaveNewFriendsAsyncTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(ListActivity.this);
			progressDialog.setMessage("Adding Friends...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				ParseUser.getCurrentUser().save();
			} catch (ParseException e) {
				Log.e("omw", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (progressDialog != null) progressDialog.dismiss();
			progressDialog = null;
			refresh();
			if (addFriendsDialog != null) {
				addFriendsDialog.dismiss(); 
				addFriendsDialog = null;
			}
		}
		
	}
	public void toastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	public static void toastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ParseUser friend = (ParseUser) buttonView.getTag();
		if (isChecked) {
			friendIdsBundle.putString(friend.getObjectId(), friend.getObjectId());
			toastMessage("Added: " + friend.getString(OMWConstants.ParseUser.FIST_NAME));
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			
			editor.putStringSet(SHAREDPREFERENCES_FRIENDS, friendIdsBundle.keySet());
			
			editor.commit();
		} else {
			if (friendIdsBundle.containsKey(friend.getObjectId())) {
				friendIdsBundle.remove(friend.getObjectId());
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				
				editor.putStringSet(SHAREDPREFERENCES_FRIENDS, friendIdsBundle.keySet());
				
				editor.commit();
			}
		}
		
	}
}
