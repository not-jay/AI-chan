package com.xtouchme.AIchan.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xtouchme.AIchan.Log;

public class AlarmCanceller extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("User input detected! Cancelling all alarms");
		SMSResponder.cancelAllAlarms();
	}

}
