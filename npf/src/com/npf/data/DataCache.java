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
		loadNeighbors();
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
	private void loadNeighbors() {
		for (MapNode n:nodes) {
			Cursor c = db.fetchNodeNeighbor(n._id);
			while (c.moveToNext()) {
				int nid = c.getInt(c.getColumnIndexOrThrow("node2"));
				n.neighbors.add(nid);
			}
		}
	}
	
	public String[] getLocationNames() {
		return locations;
	}
	
	public int getNodeCount() {
		return nodes.size();
	}
	
	public MapNode getNodeByName(String name) {
		Iterator<MapNode> i = nodes.iterator();
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n.name.equals(name)) return n;
		}
		return null;
	}
	public MapNode getNodeById(int id) {
		Iterator<MapNode> i = nodes.iterator();
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n._id == id) return n;
		}
		return null;
	}
	
	public int getNodeIdx(int id) {
		Iterator<MapNode> i = nodes.iterator();
		int count=0;
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n._id == id) return count;
			count++;
		}
		return -1;
	}
	
	public void resetNodeVars() {
		for (MapNode n:nodes) {
			n.resetPathVar();
		}
	}
	
	public MapNode getNodeByIdx(int idx) {
		return nodes.get(idx);
	}
}
