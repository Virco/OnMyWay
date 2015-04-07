package dev.virco.onmyway;

import com.facebook.FacebookSdk;
import com.parse.Parse;

import android.app.Application;

public class OnMyWayApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		FacebookSdk.sdkInitialize(this);
		Parse.initialize(this, getApplicationID(), getClientKey());
	}
	
	private String getApplicationID() {
		return "8BpoWf3TmVCdQVlkPCjHLaYdIk4rYjUSLgl0oyQO";
	}
	
	private String getClientKey() {
		return "qSJMSjhu76kU2ufJcjLMUfcIrCLdoIlT55e3Knck";
	}
}
