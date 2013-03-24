package com.xtouchme.AIchan.sms;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.xtouchme.AIchan.Log;

public class AsyncAlarm extends AsyncTask<Void, Void, Void> {
	
	private Context context;
	private long milliseconds;
	private long lastTime;
	private String id;
	private boolean isRunning, isDone;
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;
	
	private String name;
	private String reason;
	private String formattedFinalMessage;
	public static final String DEFAULT_IDLE_MESSAGE = "My master is [%reason%] at the moment.\n"+
													  "I'll notify him of your message.\n\n" +
													  "With regards, \n"+
													  "- Ai-chan";
	private final String MESSAGE_DELIVERED 	  		= "A message has been sent notifying them of your current status\n\n" +
													  "With regards,\n" +
													  "- Ai-chan";
	
	public AsyncAlarm(String id, Context context) {
		//this(300000, id, context); //Real Deal
		this(15000, id, context); //Test
	}

	public AsyncAlarm(long milli, String id, Context context) {
		this.milliseconds = milli;
		this.id = id;
		this.context = context;
		isRunning = false;
		isDone = false;
		
		name = "";
		reason = "busy";
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isDone() {
		return isDone;
	}

	public String getID() {
		return id;
	}

	public void setNameAndReason(String name, String reason) {
		this.name = name;
		this.reason = reason;
	}
	
	private String formatReplyString(String stringToFormat) {
		String message = "";
		
		if(stringToFormat.contains("[%name%]") || stringToFormat.contains("[%reason%]")) {
			message = stringToFormat.replaceAll("\\[%name%\\]", name).replaceAll("\\[%reason%\\]", reason);
		}
		
		return message;
	}
	
	private String getCustomMessage() {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SMSPreferences.KEY_CUSTOM_FORMAT, DEFAULT_IDLE_MESSAGE);
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		isRunning = false;
		isDone = true;
	}

	@Override
	protected Void doInBackground(Void... params) {
		isRunning = true;

		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AI-chan");

		wakeLock.acquire();
		lastTime = SystemClock.elapsedRealtime();
		while (milliseconds >= 0) {
			if (isCancelled())
				break;

			long time = SystemClock.elapsedRealtime();
			milliseconds -= time - lastTime;
			lastTime = time;
			SystemClock.sleep(50);
		}
		wakeLock.release();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		isRunning = false;
		isDone = true;
		String number = id;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String reason = prefs.getString(SMSPreferences.KEY_REPLY_MESSAGE, "");
		if(reason.equals(SMSPreferences.ReplyValues.CUSTOM.split(" ")[0].toLowerCase()))
			reason = prefs.getString(SMSPreferences.KEY_CUSTOM_REASON, "");
		setNameAndReason(prefs.getString(SMSPreferences.KEY_USER_NAME, ""), reason);
		formattedFinalMessage = formatReplyString(getCustomMessage());
		Log.v("%s", formattedFinalMessage);

		wakeLock.acquire();

		// Send message
		Log.v("Sending idle response message to %s...", number);
		SmsManager aiSender = SmsManager.getDefault();
		aiSender.sendMultipartTextMessage(number, null, aiSender.divideMessage(formattedFinalMessage), null, null);
		// Add to message thread
		ContentValues values = new ContentValues();
		values.put("address", number);
		values.put("body", MESSAGE_DELIVERED);
		context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
		// TODO Create/modify notification on bar

		wakeLock.release();
	}
}
