package com.npf.main;

import com.npf.map.ImageZoomView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NPFMapActivity extends Activity {
	

    private OutputManager mm;

    private ImageZoomView mZoomView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mm=OutputManager.getInstance(this);
        setContentView(R.layout.map);
        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mm.initView(mZoomView);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }
}

