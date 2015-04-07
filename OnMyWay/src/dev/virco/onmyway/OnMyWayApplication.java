package dev.virco.onmyway;

import com.facebook.FacebookSdk;

import android.app.Application;

public class OnMyWayApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		FacebookSdk.sdkInitialize(getApplicationContext());
	}
}
