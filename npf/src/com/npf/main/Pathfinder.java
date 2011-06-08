package com.npf.main;

import java.util.ArrayList;
import java.util.PriorityQueue;

import com.npf.data.DataCache;
import com.npf.data.MapNode;

public class Pathfinder {
	
	private ArrayList<MapNode> closedset;
	private PriorityQueue<MapNode> openset;
	private MapNode[] camefrom;
	private DataCache dbcache;
	private ArrayList<MapNode> path;
	private final int WALK_SPEED_PER_MIN = 70;
	private final int BUS_SPEED_PER_MIN = 500;
	private final int EARTH_RADIUS = 6371000;
	
	public Pathfinder(MapNode src, MapNode dest) {
		dbcache = DataCache.getInstance(null);
		closedset = new ArrayList<MapNode>();
		openset = new PriorityQueue<MapNode>();
		camefrom = new MapNode[dbcache.getNodeCount()];
		path = new ArrayList<MapNode>();
		
		resetNodeVars();
		findPath(src, dest);
	}
	
	private void resetNodeVars() {
		dbcache.resetNodeVars();
	}
	
	private double heuristic(MapNode n1, MapNode n2) {
		return travelTime(n1, n2);
	}
	
	private void reconstructPath(MapNode n) {
		path.add(n);
		MapNode from = camefrom[dbcache.getNodeIdx(n._id)];
		if (from==null) { //reach the start node
			return;
		}
		else {
			reconstructPath(from);
		}
	}
	
	private double distance(MapNode n1, MapNode n2) {
		double dLat = Math.toRadians(n2.latitude-n1.latitude);
		double dLon = Math.toRadians(n2.longitude - n1.longitude);
		double lat1 = Math.toRadians(n1.latitude);
		double lat2 = Math.toRadians(n2.latitude);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		
		return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}
	
	private double travelTime(MapNode n1, MapNode n2) {
		double dist = distance(n1, n2); //meters
		double time;
		if (n1.isBusStop && n2.isBusStop) {
			time = dist/BUS_SPEED_PER_MIN;
		}
		else {
			time = dist/WALK_SPEED_PER_MIN;
		}
		return time;
	}
	
	public ArrayList<MapNode> getPath() {
		return path;
	}
	
	private boolean findPath(MapNode src, MapNode dst) {
		openset.add(src);
		src.g = 0.0;
		src.h = heuristic(src, dst);
		
		while (!openset.isEmpty()) {
			MapNode x = openset.poll();
			if (x._id == dst._id)  {
				reconstructPath(dst);
				return true;
			}
			closedset.add(x);
			for(int yid:x.neighbors) {
				MapNode y = dbcache.getNodeById(yid);
				if (closedset.contains(y)) continue;
				double tentative_g_score = x.g + travelTime(x,y);
				boolean tentative_better;
				if (!openset.contains(y)) {
					tentative_better=true;
				}
				else if (tentative_g_score<y.g) {
					tentative_better=true;
				}
				else {
					tentative_better=false;	
				}
				if (tentative_better==true) {
					camefrom[dbcache.getNodeIdx(y._id)] = x;
					y.h = heuristic(y, dst);
					y.g = tentative_g_score;
				}
				if (!openset.contains(y)) {
					openset.add(y);
				}
			}
		}
		return false;
	}
}
