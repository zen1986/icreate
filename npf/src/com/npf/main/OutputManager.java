package com.npf.main;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.npf.data.DataCache;
import com.npf.data.MapNode;
import com.npf.main.R;
import com.npf.map.DynamicZoomControl;
import com.npf.map.ImageZoomView;
import com.npf.map.LongPressZoomListener;

public class OutputManager {
	private static OutputManager _instance;
	
	private Bitmap bmMap;
	private Bitmap bmMarker;
	private ImageZoomView mapView;

	private DynamicZoomControl mZoomControl;
	private LongPressZoomListener mZoomListener;
	private Context ctx;

    private DataCache dbcache;
    private Bitmap bmOverlay;
    private InputManager im;
    private ArrayList<MapNode> markers;
    
	private OutputManager(Context t) {
		ctx = t;
		dbcache = DataCache.getInstance(t);
		im = InputManager.getInstance(t);
		markers = new ArrayList<MapNode>();
        bmMap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.nusmap2);
        bmMarker = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.pin); 
        bmOverlay = Bitmap.createBitmap(bmMap.getWidth(), bmMap.getHeight(), bmMap.getConfig());
        loadMarkers();
	}
	
	public static synchronized OutputManager getInstance(Context t) {
		if (_instance == null) {
			_instance = new OutputManager(t);
		}
		return _instance;
	}
	
	public static void setInstance(OutputManager _ins){
		_instance = _ins;
	}

	public void initView(ImageZoomView _mapview) {
		mapView = _mapview;
		mZoomControl = new DynamicZoomControl();
		mZoomListener = new LongPressZoomListener(ctx);
        mZoomListener.setZoomControl(mZoomControl);
        mapView.setZoomState(mZoomControl.getZoomState());
        mapView.setOnTouchListener(mZoomListener);
        mZoomControl.setAspectQuotient(mapView.getAspectQuotient());  
        resetZoomState();
	}
	
	public Bitmap getOverlay() {
		return bmOverlay;
	}
	
	public Bitmap getBitmap() {
		return bmMap;
	}

    public Bitmap getMarkerBm() {
    	return bmMarker;
    }
    public void loadMarkers() {
    	markers.add(dbcache.getNodeByName(im.getSourceLocation()));
    	markers.add(dbcache.getNodeByName(im.getSourceLocation()));
    }
    
    public ArrayList<MapNode> getMarkers() {
    	return markers;
    }
    
    public void resetZoomState(){
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.5f);
        mZoomControl.getZoomState().setZoom(1f);
        mZoomControl.getZoomState().notifyObservers();
        mapView.postInvalidate();
    }
}
