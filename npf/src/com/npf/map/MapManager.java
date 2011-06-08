package com.npf.map;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.npf.data.DataBaseHelper;
import com.npf.main.R;
import com.npf.util.OverlayMarker;
import com.npf.util.OverlayPath;

public class MapManager {
	private static MapManager _instance;
	
	private Bitmap mBitmap;
	private Bitmap bmMarker;
	private ImageZoomView mapView;

	private DynamicZoomControl mZoomControl;
	private LongPressZoomListener mZoomListener;
	private Context ctx;

    private ArrayList<OverlayMarker> markers;
    private ArrayList<OverlayPath> paths;
    private DataBaseHelper myDbHelper;
    private Bitmap bmOverlay;
    
	private MapManager() {
        markers = new ArrayList<OverlayMarker>();
        paths = new ArrayList<OverlayPath>();
	}
	
	public static synchronized MapManager getInstance() {
		if (_instance == null) {
			Log.i("NPFdebug", "Singleton intialized");
			_instance = new MapManager();
		}
		return _instance;
	}
	
	public static void setInstance(MapManager _ins){
		_instance = _ins;
	}
	
	public Cursor getNode(String name) {
		return myDbHelper.fetchMapNode(name);
	}
	
	public Cursor getNodes() {
		return myDbHelper.fetchAllMapNodes();
	}
	
	
	public void setBitmaps(Bitmap _bm, Bitmap _marker) {
		if (mBitmap==null) {
			mBitmap = _bm;
			if (bmOverlay==null) bmOverlay = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());
		}
		if (bmMarker==null) bmMarker = _marker;
	}
    public void resetMarkers() {
    	markers.clear();
    }
    public void resetPaths() {
    	paths.clear();
    }
	public void setMarker(Bitmap m){
		bmMarker = m;
	}
	
    public void addMarker(int x, int y) {
    	markers.add(new OverlayMarker(x,y));
    }
    
    public void addPath(int srcx, int srcy, int dstx, int dsty) {
    	paths.add(new OverlayPath(new OverlayMarker(srcx, srcy), new OverlayMarker(dstx, dsty)));
    }
	public void setMapView(Context _ctx, ImageZoomView _mapview) {
		ctx=_ctx;
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
		return mBitmap;
	}

    public ArrayList<OverlayPath> getPaths() {
    	return paths;
    }
    public ArrayList<OverlayMarker> getMarkers() {
    	return markers;
    }

    public Bitmap getMarkerBm() {
    	return bmMarker;
    }
    public void resetZoomState(){
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.5f);
        mZoomControl.getZoomState().setZoom(1f);
        mZoomControl.getZoomState().notifyObservers();
        mapView.postInvalidate();
    }
    
    public void destroy() {


    }
	/*
	 * calculate path between given 2 points
	 * */
    //public void calcPath(MapNode start, MapNode end) {
	//	resetMarkers();
	//	resetPaths();
	//}
	
	/*
	 * locate a point on map
	 * */
	public void locatePoint(int x, int y) {
		addMarker(x,y);
	}
	
	public void loadData(Context _ctx) {
		myDbHelper = new DataBaseHelper(_ctx);
 
        try {
        	myDbHelper.createDataBase();
        	Log.i("NPFdebug", "Done setup DataBase");
        } catch (IOException ioe) {
 
        	throw new Error("Unable to create database");
        }
        try {
        	myDbHelper.openDataBase();
        }catch(SQLException sqle){
 
        	throw sqle;
        }
	}
	
	public void closeDb() {
		myDbHelper.close();
	}
}
