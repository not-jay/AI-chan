package com.xtouchme.AIchan.sms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.xtouchme.AIchan.R;

public class SMSPreferences extends PreferenceActivity {

	public static final String KEY_REPLY_MESSAGE = "reply_message";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_CUSTOM_REASON = "custom_reason";
	public static final String KEY_CUSTOM_FORMAT = "reply_format";
	public static final String KEY_REPLY_TIMEOUT = "reply_timeout";
	
	public class ReplyValues {
		public static final String ASLEEP	= "Asleep";
		public static final String BUSY		= "Busy";
		public static final String IN_CLASS	= "In class";
		public static final String CUSTOM	= "Custom Reply";
		public static final String C_FORMAT	= "[%reason%]";
	}
	public class TimeoutOptions {
		public static final String FIFTEEN_SECONDS			= "15 Seconds";
		public static final String THIRTY_SECONDS			= "30 Seconds";
		public static final String A_MINUTE					= "1 Minute";
		public static final String TWO_MINUTES_AND_A_HALF	= "2 Minutes and 30 Seconds";
		public static final String FIVE_MINUTES				= "5 Minutes";
		public static final String TEN_MINUTES				= "10 Minutes";
	}
	public class TimeoutValues {
		public static final String FIFTEEN_SECONDS			= "15000";
		public static final String THIRTY_SECONDS			= "30000";
		public static final String A_MINUTE					= "60000";
		public static final String TWO_MINUTES_AND_A_HALF	= "150000";
		public static final String FIVE_MINUTES				= "300000";
		public static final String TEN_MINUTES				= "600000";
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.sms_prefs);
		
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				Preference replyMessage = findPreference(KEY_REPLY_MESSAGE);
				Preference userName = findPreference(KEY_USER_NAME);
				Preference customReason = findPreference(KEY_CUSTOM_REASON);
				Preference customFormat = findPreference(KEY_CUSTOM_FORMAT);
				boolean isCustom = false;
				Preference replyTimeout = findPreference(KEY_REPLY_TIMEOUT);
				
				String value = sharedPreferences.getString(KEY_REPLY_MESSAGE, "");
				if(value.equals(ReplyValues.ASLEEP.toLowerCase())) value = ReplyValues.ASLEEP;
				if(value.equals(ReplyValues.BUSY.toLowerCase())) value = ReplyValues.BUSY;
				if(value.equals(ReplyValues.IN_CLASS.toLowerCase())) value = ReplyValues.IN_CLASS;
				if(value.equals(ReplyValues.CUSTOM.split(" ")[0].toLowerCase())) {
					value = ReplyValues.CUSTOM;
					isCustom = true;
				}
				replyMessage.setSummary(value);
				
				value = sharedPreferences.getString(KEY_USER_NAME, "Not set (default: blank)");
				userName.setSummary(value);
				
				value = sharedPreferences.getString(KEY_CUSTOM_REASON, "Not set (default: blank)");
				customReason.setSummary(value);
				customReason.setEnabled(isCustom);
				
				value = sharedPreferences.getString(KEY_CUSTOM_FORMAT, AsyncAlarm.DEFAULT_IDLE_MESSAGE);
				customFormat.setSummary(value);
				
				value = sharedPreferences.getString(KEY_REPLY_TIMEOUT, "");
				if(value.equals(TimeoutValues.FIFTEEN_SECONDS)) value = TimeoutOptions.FIFTEEN_SECONDS;
				if(value.equals(TimeoutValues.THIRTY_SECONDS)) value = TimeoutOptions.THIRTY_SECONDS;
				if(value.equals(TimeoutValues.A_MINUTE)) value = TimeoutOptions.A_MINUTE;
				if(value.equals(TimeoutValues.TWO_MINUTES_AND_A_HALF)) value = TimeoutOptions.TWO_MINUTES_AND_A_HALF;
				if(value.equals(TimeoutValues.FIVE_MINUTES)) value = TimeoutOptions.FIVE_MINUTES;
				if(value.equals(TimeoutValues.TEN_MINUTES)) value = TimeoutOptions.TEN_MINUTES;
				replyTimeout.setSummary(value);
			}
		};
		r.run();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SMSPreferences.this);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				r.run();
				Toast.makeText(SMSPreferences.this, "'"+key+"' have been changed", Toast.LENGTH_SHORT).show();
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	}

}
