package dev.virco.onmyway;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AddFriendsAdapter extends ArrayAdapter<ParseUser> implements OnCheckedChangeListener{
	
	List<ParseUser> userList;
	Context context;
	int resource;
	MyBooleanArray mCheckStates;
	
	public AddFriendsAdapter(Context context, int resourse, List<ParseUser> objects) {
		super(context, resourse, objects);
		
		this.context = context;
		this.userList = objects;
		this.resource = resourse;
		mCheckStates = new MyBooleanArray(userList.size());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
			holder = new ViewHolder();
			
			holder.profilePic = (ImageView) convertView.findViewById(R.id.add_friends_dialog_item_pic);
			holder.nameTextView = (TextView) convertView.findViewById(R.id.add_friends_dialog_item_textview_name);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.add_friends_dialog_item_checkbox);

			convertView.setTag(holder);
		}
		
		holder = (ViewHolder) convertView.getTag();
		ImageView profilePic = holder.profilePic;
		TextView nameTextView = holder.nameTextView;
		CheckBox checkBox = holder.checkBox;
		ParseUser user = userList.get(position);
		
		checkBox.setTag(position);
		holder.checkBox.setOnCheckedChangeListener(this);
		
		if (user.getString(OMWConstants.ParseUser.PROFILE_PIC) == null) {
			Picasso.with(context)
			.load(R.drawable.profile_pic)
			.into(profilePic);
		} else {
			Picasso.with(context)
			.load(user.getString(OMWConstants.ParseUser.PROFILE_PIC))
			.into(profilePic);
		}
		
		nameTextView.setText(user.getString(OMWConstants.ParseUser.FULL_NAME));
		
		
		return convertView;
	}
	
	  public boolean isChecked(int position) {
	        return mCheckStates.get(position);
	    }

	    public void setChecked(int position, boolean isChecked) {
	        mCheckStates.set(position, isChecked);

	    }

	    public void toggle(int position) {
	        setChecked(position, !isChecked(position));
	    }
	
	static class ViewHolder {
		ImageView profilePic;
		TextView nameTextView;
		CheckBox checkBox;
	}
	
	public class MyBooleanArray extends ArrayList<Boolean> {
		
		public MyBooleanArray(int length) {
			super(length);
			for (int i = 0; i < length; i++) add(false);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		   mCheckStates.set((Integer) buttonView.getTag(), isChecked);    
		
	}
}
