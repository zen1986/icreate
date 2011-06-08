package com.npf.main;

import com.npf.map.ImageZoomView;
import com.npf.map.MapManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NPFMapActivity extends Activity {
	

    private MapManager mm;

    private ImageZoomView mZoomView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mm=MapManager.getInstance();
        setContentView(R.layout.map);
        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mm.setMapView(NPFMapActivity.this, mZoomView);
        Log.i("NPFdebug", "Map Activity onCreate");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mm.destroy();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d("NPFdebug", "Activity Resumed");
    }
}

