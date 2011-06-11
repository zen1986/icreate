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
			int buss_id = bussCursor.getInt(bussCursor.getColumnIndexOrThrow("_id"));
			ArrayList<Integer> buss_neib = new ArrayList<Integer>();
			ArrayList<Integer> bus_ids = new ArrayList<Integer>();
			//need to get adjacent bus stops that a bus can reach
			//get the buses first
			Cursor busCursor = db.fetchBusesForBusstop(buss_id);
			while (busCursor.moveToNext()) {
				//for each bus
				int bus_id = busCursor.getInt(busCursor.getColumnIndexOrThrow("_id"));
				bus_ids.add(bus_id);
				//from its route, get the sequence no. of current bus stop
				//add 1 to sequence no. to get next bus stop
				Cursor c = db.fetchRouteSeq(bus_id, buss_id);
				if (!c.moveToFirst()) continue;
				int nextseq = c.getInt(c.getColumnIndexOrThrow("seq"))+1;
				c.close();
				c = db.fetchBusstopBySeqAndBus(nextseq, bus_id);
				
				if (c.getCount()==0) {
					//in case the current bus stop is the last one in its route
					if (db.isBusLoop(bus_id)) {
						//if the bus runs in loop, then return the first bus stop 
						c.close();
						c = db.fetchBusstopBySeqAndBus(0, bus_id);
					}
					else {
						c.close();
						c = null;
					}
				}
				if (c!=null) {
					c.moveToFirst();
					int next_busstop_id = c.getInt(c.getColumnIndexOrThrow("busstop_id"));
					if (!buss_neib.contains(next_busstop_id)) buss_neib.add(next_busstop_id);
					c.close();
				}
			}
			int [] neibs = new int[buss_neib.size()];
			for (int i=0;i<buss_neib.size();i++) neibs[i]=buss_neib.get(i);
			int [] buses = new int[bus_ids.size()];
			for (int i=0;i<bus_ids.size();i++) buses[i]=bus_ids.get(i);
			busstop_nodes.add(new MapNode(bussCursor, neibs,buses));
			busCursor.close();
		}
		bussCursor.close();
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
			
			totalLocations+=locs.length;
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
				int locLen = n.locations.length;
				System.arraycopy(n.locations, 0, locations, count, locLen);
				count+=locLen;
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
	
	public MapNode getNearestBusstop(double lat, double lng, ArrayList<MapNode> candidates) {
		double min_dist = Double.MAX_VALUE;
		MapNode min_node=null;
		ArrayList<MapNode> nodes;
		if (candidates == null) nodes = busstop_nodes;
		else nodes = candidates;
		
		for (MapNode busstop: nodes) {
			double dist = busstop.distance(lat, lng);
			if (dist < min_dist) {
				min_dist = dist;
				min_node = busstop;
			}
		}
		return min_node;
	}
	
	public int reachable(MapNode srcBuss, MapNode dstBuss) {
		
		if (srcBuss._id == dstBuss._id) return 0;
		
		for (int srcBus:srcBuss.buses) {
			for (int dstBus:dstBuss.buses) {
				if (srcBus == dstBus) {
					if (db.isBusLoop(srcBus)) return srcBus;
					//get seq number
					Cursor seq_src_cur = db.fetchRouteSeq(srcBus, srcBuss._id);
					Cursor seq_dst_cur = db.fetchRouteSeq(srcBus, dstBuss._id);
					int seq_src = seq_src_cur.getInt(seq_src_cur.getColumnIndexOrThrow("seq"));
					int seq_dst = seq_dst_cur.getInt(seq_dst_cur.getColumnIndexOrThrow("_id"));
					seq_src_cur.close();
					seq_dst_cur.close();
					if (seq_dst>seq_src) return srcBus;
				}
			}
		}
		
		return -1;
	}
	
	public void findTransferBusstop(MapNode srcBuss, MapNode dstBuss, MapPath path) {
		ArrayList<MapNode> candidates = new ArrayList<MapNode>();
		for (MapNode buss:busstop_nodes) {
			if (reachable(srcBuss, buss)>0 && reachable(buss, dstBuss)>0) candidates.add(buss); 
		}
		
		double[] midpt = new double[2];
		midpt[0] = (srcBuss.latitude + dstBuss.latitude)/2;
		midpt[1] = (srcBuss.longitude + dstBuss.longitude)/2;
		
		MapNode nearest = getNearestBusstop(midpt[0], midpt[1], candidates);
		path.firstBus = reachable(srcBuss, nearest);
		path.secondBus = reachable(nearest, dstBuss);
		path.path.add(nearest);
	}
}
