package com.npf.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TabHost;

public class Receiver extends BroadcastReceiver{
	public static TabHost th;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BroadcastReceiver","Received");
		th.setCurrentTabByTag("map");
	}
}
