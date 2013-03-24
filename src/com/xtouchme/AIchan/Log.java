package com.xtouchme.AIchan;

public class Log {

	private static final String TAG = "AI-chan";
	private static final boolean DEBUG = true;

	public static void v(String msg, Object... args) {
		if(!DEBUG) return;
		android.util.Log.v(TAG, String.format(msg, args));
	}
	
	public static void v(String msg) {
		if(!DEBUG) return;
		android.util.Log.v(TAG, msg);
	}

	public static void w(String msg, Object... args) {
		android.util.Log.w(TAG, String.format(msg, args));
	}
	
	public static void w(String msg) {
		android.util.Log.w(TAG, msg);
	}
	
	public static void e(String msg, Object... args) {
		android.util.Log.e(TAG, String.format(msg, args));
	}
	
	public static void e(String msg) {
		android.util.Log.e(TAG, msg);
	}
	
}
