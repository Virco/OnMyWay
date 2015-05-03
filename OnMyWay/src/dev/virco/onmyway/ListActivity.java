package dev.virco.onmyway;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONArrayCallback;
import com.facebook.GraphResponse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.internal.view.menu.ListMenuItemView;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends FragmentActivity {
	
	TextView textView;
	ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		textView = (TextView) findViewById(R.id.textView1);
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
		return super.onOptionsItemSelected(item);
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
			progressDialog.dismiss();
			if (result != null) {
				if (result.size() > 0) {
					(new AddFriendsDialog(result)).show(getSupportFragmentManager(), "dialog");
				}
			}
		}
		
	}
	
	private class AddFriendsDialog extends DialogFragment {
		
		ListView listView;
		Button button;
		List<ParseUser> userList;
		AddFriendsAdapter adapter;
		ArrayList<Boolean> checkedList;
		
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
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View v = inflater.inflate(R.layout.add_friends_dialog_fragment, container, false);
			listView = (ListView) v.findViewById(R.id.add_friends_dialog_fragment_listview);
			button = (Button) v.findViewById(R.id.add_friends_dialog_fragment_button);
			
			adapter = new AddFriendsAdapter(getActivity(), R.layout.add_friends_dialog_item, userList);
			listView.setAdapter(adapter);
			
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
	
                    for(int i = 0; i < adapter.mCheckStates.size(); i++) {
                        if(adapter.mCheckStates.get(i)) {
                        	toastMessage(userList.get(i).getString(OMWConstants.ParseUser.FULL_NAME));
                        }

                    }
				}		
			});
			
			return v;
		}
	}
	public void toastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
