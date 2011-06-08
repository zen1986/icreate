package com.npf.main;


import com.npf.util.Receiver;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	public Cursor cursor;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    intent = new Intent().setClass(this, NPFMapActivity.class);

	    spec = tabHost.newTabSpec("map").setIndicator("Map",
	                      res.getDrawable(R.drawable.npf_tab_placeholder))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, NPFStartActivity.class);
	    spec = tabHost.newTabSpec("input").setIndicator("Locate",
	                      res.getDrawable(R.drawable.npf_tab_placeholder))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTabByTag("input");
	    
	    Receiver.th = tabHost;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void onDestroy(Bundle savedInstanceState) {
	}
}

