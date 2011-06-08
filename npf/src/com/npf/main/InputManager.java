package com.npf.main;

import android.content.Context;
import com.npf.data.DataCache;

public class InputManager {

	private DataCache dbcache;
	private Context ctx;
	private String source;
	private String destination;
	private static InputManager _instance;
	
	public InputManager(Context c) {
		ctx = c;
		dbcache = DataCache.getInstance(ctx);
	}
	
	public static InputManager getInstance(Context c) {
		if (_instance == null) {
			_instance = new InputManager(c);
		}
		return _instance;
	}
	
	public void setSourceLocation(String src) {
		source = src;
	}
	public void setDestinationLocation(String dest) {
		destination = dest;
	}
	public String getSourceLocation() { return source;}
	public String getDestinationLocation() {return destination;}
}
