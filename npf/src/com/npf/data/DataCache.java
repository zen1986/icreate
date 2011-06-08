package com.npf.data;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;



public class DataCache {

	private DataBaseHelper db;
	private static DataCache _instance;
	private Context ctx;
	private ArrayList<MapNode> nodes;
	private String[] locations;
	
	public DataCache(Context c) {
		ctx = c;
		db = new DataBaseHelper(ctx);
		loadObjects();
	}
	
	public static DataCache getInstance(Context c) {
		if (_instance == null) {
			_instance = new DataCache(c);
		}
		return _instance;
	}
	
	private void loadObjects() {
		loadNodes();
		loadLocations();
	}
	
	private void loadNodes() {
		nodes = new ArrayList<MapNode>();
		Cursor c = db.fetchAllMapNodes();
		while (c.moveToNext()) {
			nodes.add(new MapNode(c));
		}
		c.close();
	}
	
	private void loadLocations() {
		locations = new String[nodes.size()];
		Iterator<MapNode> i = nodes.iterator();
		int k=0;
		while (i.hasNext()) {
			MapNode n = i.next();
			locations[k]=n.name;
			k++;
		}
	}
	
	public String[] getLocationNames() {
		return locations;
	}
	
	public MapNode getNodeByName(String name) {
		Iterator<MapNode> i = nodes.iterator();
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n.name.equals(name)) return n;
		}
		return null;
	}
}
