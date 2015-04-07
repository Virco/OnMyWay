package dev.virco.onmyway;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import android.app.Application;

public class OnMyWayApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, getApplicationID(), getClientKey());
		ParseFacebookUtils.initialize(this);
	}
	
	private String getApplicationID() {
		return "8BpoWf3TmVCdQVlkPCjHLaYdIk4rYjUSLgl0oyQO";
	}
	
	private String getClientKey() {
		return "qSJMSjhu76kU2ufJcjLMUfcIrCLdoIlT55e3Knck";
	}
}
