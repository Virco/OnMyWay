package dev.virco.onmyway;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

import android.app.Application;

public class OnMyWayApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, getApplicationID(), getClientKey());
		ParseFacebookUtils.initialize(this);
		// Save the current Installation to Parse.
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}
	
	private String getApplicationID() {
		return "8BpoWf3TmVCdQVlkPCjHLaYdIk4rYjUSLgl0oyQO";
	}
	
	private String getClientKey() {
		return "qSJMSjhu76kU2ufJcjLMUfcIrCLdoIlT55e3Knck";
	}
}
