package com.npf.data;

import java.util.ArrayList;

public class MapPath {
	
	public ArrayList<MapNode> path;
	public double distance;
	public double time;
	public boolean bBus;
	public int firstBus;
	public int secondBus;
	
	
	public MapPath(boolean b) {
		bBus =b;
		distance = 0.0;
		time = 0.0;
		firstBus = 0;
		secondBus = 0;
		path = new ArrayList<MapNode>();
	}
}
