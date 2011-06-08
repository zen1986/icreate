package com.npf.data;

import java.util.ArrayList;

import android.database.Cursor;

public class MapNode implements Comparable<MapNode> {
	public double latitude;
	public double longitude;
	public double texu;
	public double texv;
	public String name;
	public double g,h;
	public double distanceToSrc;    //meter
	public double timeToSrc;        //minute
	public ArrayList<Integer> neighbors;//store _id
	public int _id;
	public boolean isBusStop;
	
	public MapNode(Cursor c) {
		_id = c.getInt(c.getColumnIndexOrThrow("_id"));
		name = c.getString(c.getColumnIndexOrThrow("name"));
		latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
		longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));
		texu = c.getDouble(c.getColumnIndexOrThrow("texu"));
		texv = c.getDouble(c.getColumnIndexOrThrow("texv"));
		neighbors = new ArrayList<Integer>();
		if (c.getInt(c.getColumnIndexOrThrow("isbusstop"))==1) isBusStop = true;
		else isBusStop = false;
	}
	
	public void resetPathVar() {
		g = 0.0;
		h = 0.0;
		distanceToSrc = 0.0;
		timeToSrc = 0.0;
	}
	
	public double getF() {
		return g+h;
	}

	@Override
	public int compareTo(MapNode n) {
		return (int) ((g+h-n.getF()) * 1000);
	}
	
}
