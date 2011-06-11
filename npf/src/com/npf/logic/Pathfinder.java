package com.npf.logic;

import java.util.ArrayList;
import java.util.PriorityQueue;

import com.npf.data.DataCache;
import com.npf.data.MapNode;
import com.npf.data.MapPath;

public class Pathfinder {
	
	/*
	 * Here I separate the calculation of route that taking a bus and walking
	 *
	 * for walking, i just use all node(including bus stops) in calculation
	 * g and h values are calculated similarly using walking speed
	 * the distance for g is exact, while for h is estimated
	 * the outcome is a route with distance and time
	 * 
	 * for taking bus, i first get the nearest bus stops between start node and end node respectively
	 * then use get the buses that travel between this bus stop
	 * the outcome is a route that walk from start node to first bus stop and take bus to last bus stop and walk to destination
	 * the distance and time are also calculated
	 * 
	 *
	 **/
	
	private ArrayList<MapNode> closedset;
	private PriorityQueue<MapNode> openset;
	private MapNode[] camefrom;
	private DataCache dbcache;
	private MapPath busPath;
	private MapPath walkPath;
	
	public Pathfinder(MapNode src, MapNode dest) {
		dbcache = DataCache.getInstance(null);
		
		//initialise helper variables
		closedset = new ArrayList<MapNode>();
		openset = new PriorityQueue<MapNode>();
		camefrom = new MapNode[dbcache.getNodeCount()];
		walkPath = new MapPath(true);
		busPath = new MapPath(false);
		
		resetNodeVars();
		findWalkPath(src, dest);
		findBusPath(src, dest);
	}
	
	private void resetNodeVars() {
		dbcache.resetNodeVars();
	}
	public MapPath getWalkPath() {
		return walkPath;
	}
	
	public MapPath getBusPath() {
		return busPath;
	}
	
	///////////////////////////////////////walk path/////////////////////////////////////////////////
	////  walk path does not consider any bus stop
	////  g value of each node in the final path = distance from it to start node
	////  time can be obtain by dividing by walk speed
	/////////////////////////////////////////////////////////////////////////////////////////////////
	private double heuristic(MapNode n1, MapNode n2) {
		return getDist(n1, n2);
	}
	
	private void reconstructPath(MapNode n) {
		walkPath.path.add(n);
		MapNode from = camefrom[dbcache.getNodeIdx(n._id)];
		if (from==null) { //reach the start node
			return;
		}
		else {
			reconstructPath(from);
		}
	}
	
	private double getDist(MapNode n1, MapNode n2) {
		return n1.distance(n2.latitude, n2.longitude); //meters
	}
	
	private boolean findWalkPath(MapNode src, MapNode dst) {
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
				double tentative_g_score = x.g + getDist(x,y);
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


///////////////////////////////////end of walk path/////////////////////////////////////////////////////////


//////////////////////////////////Bus Route//////////////////////////////////////////////////////////////
///////// obtain nearest 2 bus stop first
///////// check bus between them
///////// account for any transit if necessary
///////// in any case, at most 1 transfer is needed to reach any 2 bus stop
//////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void findBusPath(MapNode src, MapNode dest) {
		MapNode nearestSrcBusstop = dbcache.getNearestBusstop(src.latitude,src.longitude,null);
		MapNode nearestDstBusStop = dbcache.getNearestBusstop(dest.latitude,dest.longitude,null);

		busPath.path.add(src);
		
		int commBus = dbcache.reachable(nearestSrcBusstop, nearestDstBusStop);
		if (commBus==0) {
			//already at dst bus stop
		}
		else if (commBus>0) {
			//take commBus from src to dest
			busPath.firstBus = commBus;
		}
		else {
			//no commBus
			//have to look for intermediate bus
			dbcache.findTransferBusstop(src, dest, busPath);
		}
		busPath.path.add(dest);
	}

}