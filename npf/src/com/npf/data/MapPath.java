package com.npf.data;

import java.util.ArrayList;

public class MapPath {
	
	public ArrayList<MapNode> path;
	public double distance;
	public double time;
	public boolean bBus;
	
	
	public MapPath(boolean b) {
		bBus =b;
		distance = 0;
		time = 0;
		path = new ArrayList<MapNode>();
	}
	
	public void addNode(MapNode n) {
		path.add(n);
	}
	
	public void addDistance(double d) {
		distance += d;
	}
	
}
