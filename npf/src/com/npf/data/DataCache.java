package com.npf.data;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;



public class DataCache {

	private DataBaseHelper db;
	private static DataCache _instance;
	private Context ctx;
	private ArrayList<MapNode> building_nodes;
	private ArrayList<MapNode> busstop_nodes;
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
		loadBuildingNodes();
		loadBusNodes();
	}
	
	private void loadBusNodes(){
		busstop_nodes = new ArrayList<MapNode>();
		Cursor bussCursor = db.fetchAllBusstopNodes();
		
		while (bussCursor.moveToNext()) {
			//need to get adjacent bus stops that a bus can reach
		}
	}
	
	private void loadBuildingNodes() {
		building_nodes = new ArrayList<MapNode>();
		
		//load building nodes first
		Cursor buildingCursor = db.fetchAllBuildingNodes();
		
		while (buildingCursor.moveToNext()) {
			int id = buildingCursor.getInt(buildingCursor.getColumnIndexOrThrow("_id"));
			String name = buildingCursor.getString(buildingCursor.getColumnIndexOrThrow("name"));
			
			//get building neighbours
			Cursor neib = db.fetchNodeNeighbor(id);
			int[] nids = new int[neib.getCount()];
			int i=0;
			while (neib.moveToNext()) {nids[i++] = neib.getInt(neib.getColumnIndexOrThrow("node2"));}
			
			//get locations
			Cursor loc = db.fetchLocationByBuildingName(name);
			String[] locs = new String[loc.getCount()];
			int k=0;
			while (loc.moveToNext()) {locs[k++]=loc.getString(loc.getColumnIndexOrThrow("name"));}
			
			building_nodes.add(new MapNode(buildingCursor, nids, locs));
			
			neib.close();
			loc.close();
		}
		buildingCursor.close();
	}

	public String[] getLocationNames() {
		if (locations==null) {
			Iterator<MapNode> i = building_nodes.iterator();
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
		return building_nodes.size();
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
		Iterator<MapNode> i = building_nodes.iterator();
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n.name.equals(name)) return n;
		}
		return null;
		
	}
	public MapNode getNodeById(int id) {
		Iterator<MapNode> i = building_nodes.iterator();
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n._id == id) return n;
		}
		return null;
	}
	
	public int getNodeIdx(int id) {
		Iterator<MapNode> i = building_nodes.iterator();
		int count=0;
		while (i.hasNext()) {
			MapNode n = i.next();
			if (n._id == id) return count;
			count++;
		}
		return -1;
	}
	
	public void resetNodeVars() {
		for (MapNode n:building_nodes) {
			n.resetPathVar();
		}
	}
	
	public MapNode getNodeByIdx(int idx) {
		return building_nodes.get(idx);
	}
}
