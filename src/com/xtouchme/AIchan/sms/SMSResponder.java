package com.xtouchme.AIchan.sms;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

import com.xtouchme.AIchan.Log;

public class SMSResponder extends BroadcastReceiver {

	public static final String SENT_INTENT = "SMS_SENT";
	public static final String BUNDLE_KEY = "com.xtouchme.AIchan.alarmID";

	private static ArrayList<AsyncAlarm> timers;
	
	/*TODO add and implement options
		   register receiver manually on app (done)
		   on screen off, check for unread messages and start alarms (50/50)
	*/
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("Intent recieved is %s", intent.getAction());
		
		//If it doesn't exist, create an empty list
		//On a successful load, remove all alarms that are done
		if(timers == null) timers = new ArrayList<AsyncAlarm>();
		if(!timers.isEmpty()) cleanUp();
		Log.v("Clean up complete, %d running alarms", timers.size());
		
		//TODO delegate alarm cancellation to other classes
		//Add to the list of shit that cancels the alarms, 'SMS_SENT'
		//Probably call it 'AlarmCanceller' ? (done)
		//For future reference...
		/*
		if(intent.getAction().equals(USER_PRESENT)) { //"android.intent.action.USER_PRESENT"
			//Cancel all alarms
			Log.v("User input detected! Cancelling all alarms");
			cancelAllAlarms();
		} else { //It would be 'android.provider.Telephony.SMS_RECEIVED'
		*/
		Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
		
		if(bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			String id = "";
			messages = new SmsMessage[pdus.length];
			for(int x = 0; x < messages.length; x++) {
				messages[x] = SmsMessage.createFromPdu((byte[])pdus[x]);
				id = messages[x].getDisplayOriginatingAddress();
				Log.v("Message recieved from %s: %s", id, messages[x].getDisplayMessageBody());
			}

			//TODO exlcusions
			//if(!exclusions.contain(id)) {
				createAndRunAlarms(context, id);
		}
		//}
	}

	private void createAndRunAlarms(Context context, String id) {
		//Check if there's an alarm for this id, if so, do nothing
		//Otherwise, add the alarm
		boolean shouldAdd = true;
		long milli = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(SMSPreferences.KEY_REPLY_TIMEOUT, "0"));
		
		if(timers.isEmpty()) {
			Log.v("Adding alarm '%s'...", id);
			createAlarm(milli, id, context);
		} else {
			for(AsyncAlarm alarm : timers) {
				if(alarm.getID().equals(id)) shouldAdd = false;
			}
			if(shouldAdd) {
				Log.v("Adding alarm '%s'...", id);
				createAlarm(milli, id, context);
			} else Log.v("Alarm '%s' is already running...", id);
		}
		
		//Run any alarm that hasn't started and isn't done
		for(AsyncAlarm alarm : timers) {
			if(!alarm.isRunning() && !alarm.isDone() && 
			   !(alarm.getStatus().equals(AsyncTask.Status.RUNNING) || alarm.getStatus().equals(AsyncTask.Status.FINISHED))) {
				Log.v("Starting alarm id '%s'", alarm.getID());
				try{
					alarm.execute();
				} catch(IllegalStateException e) {
					Log.e("Alarm '%s' is already running or has finished", alarm.getID());
				}
			}
		}
	}
	
	private void createAlarm(long milli, String id, Context context) {
		if(milli <= 0) {
			Log.v("Alarm has been set for the default 5 minutes");
			timers.add(new AsyncAlarm(id, context));
		}
		else {
			Log.v("Alarm has been set for %s milliseconds", String.valueOf(milli));
			timers.add(new AsyncAlarm(milli, id, context));
		}
	}
	
	private void cleanUp() {
		int x = 0;
		do {
			if(timers.get(x).isDone()) {
				Log.v("Cleaned up '%s'", timers.get(x).getID());
				timers.remove(x);
				x--;
			}
			x++;
		} while(x < timers.size());
	}

	public static void cancelAlarmByID(String id) {
		if(checkForID(id)) {
			for(AsyncAlarm alarm : timers) {
				if(alarm.getID().equals(id)) {
					Log.v("Stopping alarm '%s'", id);
					alarm.cancel(true);
					break;
				}
			}
		} else
			Log.v("No alarm found");
	}
	
	public static boolean checkForID(String id) {
		for(AsyncAlarm alarm : timers) {
			if(alarm.getID().equals(id))
				return true;
		}
		return false;
	}
	
	public static void cancelAllAlarms() {
		if(timers == null) {
			Log.w("This might not end well... cancelling 'cancel' action, no action would be taken anyway");
			return;
		}
		for(AsyncAlarm alarm : timers) {
			Log.v("Cancelling alarm '%s'", alarm.getID());
			alarm.cancel(true);
		}
	}
	
}
