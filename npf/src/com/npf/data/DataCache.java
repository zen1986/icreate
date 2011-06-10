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
	private int totalLocations=0;
	
	public DataCache(Context c) {
		ctx = c;
		db = new DataBaseHelper(ctx);
		loadNodes();
	}
	
	public static DataCache getInstance(Context c) {
		if (_instance == null) {
			_instance = new DataCache(c);
		}
		return _instance;
	}
	
	private void loadNodes() {
		nodes = new ArrayList<MapNode>();
		Cursor cnodes = db.fetchAllMapNodes();
		
		while (cnodes.moveToNext()) {
			int id = cnodes.getInt(cnodes.getColumnIndexOrThrow("_id"));
			String name = cnodes.getString(cnodes.getColumnIndexOrThrow("name"));
			//get neighbours
			Cursor neib = db.fetchNodeNeighbor(id);
			ArrayList<Integer> nids = new ArrayList<Integer>();
			while (neib.moveToNext()) {
				int nid = neib.getInt(neib.getColumnIndexOrThrow("node2"));
				nids.add(nid);
			}
			Integer[] nids_int = new Integer[nids.size()];
			nids.toArray(nids_int);
			nids.clear();
			
			//get locations
			/*
			 * from location table get all locations that nodes have its containing building
			 */
			ArrayList<String> locs = new ArrayList<String>();
			Cursor loc = db.fetchLocationByBuildingName(name);
			while (loc.moveToNext()) {
				locs.add(loc.getString(loc.getColumnIndexOrThrow("name")));
			}
			String[] locs_str = new String[locs.size()];
			locs.toArray(locs_str);
			totalLocations+=locs.size();
			locs.clear();
			
			nodes.add(new MapNode(cnodes, nids_int, locs_str));
			
			neib.close();
			loc.close();
		}
		cnodes.close();
	}

	public String[] getLocationNames() {
		if (locations==null) {
			Iterator<MapNode> i = nodes.iterator();
			locations = new String[totalLocations];
			int count = 0;
			while (i.hasNext()) {
				MapNode n = i.next();
				int strLen = n.locations.length;
				System.arraycopy(n.locations, 0, locations, count, strLen);
				count+=strLen;
			}
		}
		return locations;
	}
	
	public int getNodeCount() {
		return nodes.size();
	}
	
	public MapNode getNodeByLocationName(String name) {
		Cursor c = db.fetchLocationByName(name);
		if (c.moveToFirst()){
			String nodeName = c.getString(c.getColumnIndexOrThrow("building"));
			return getNodeByName(nodeName);
		}
		return null;
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
