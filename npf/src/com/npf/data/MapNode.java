package com.npf.data;

import android.database.Cursor;

public class MapNode implements Comparable<MapNode> {
	public final double latitude;
	public final double longitude;
	public final double texu;
	public final double texv;
	public final String name;
	public double g,h;
	public double distanceToSrc;       //meter
	public double timeToSrc;           //minute
	public final int[] neighbors;  //store _id   building's neighbours are the building adjacent to it
									   //            bus stop's neighbours are those adjacent and at low stream
	public final int _id;
	public final int[] buses;
	public final boolean isBusStop;
	public final String[] locations;     //the locations this map node contains

	private final int EARTH_RADIUS = 6371000;
	
	
	//for building node
	public MapNode(Cursor c, int[] _neighbors, String[] _locations) {
		_id = c.getInt(c.getColumnIndexOrThrow("_id"));
		name = c.getString(c.getColumnIndexOrThrow("name"));
		latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
		longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));
		texu = c.getDouble(c.getColumnIndexOrThrow("texu"));
		texv = c.getDouble(c.getColumnIndexOrThrow("texv"));
		neighbors = _neighbors;
		locations = _locations;
		buses = null;
		isBusStop = false;
	}
	//for bus stop node
	public MapNode(Cursor c, int[] _neighbors, int[] _buses) {
		_id = c.getInt(c.getColumnIndexOrThrow("_id"));
		name = c.getString(c.getColumnIndexOrThrow("name"));
		latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
		longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));
		texu = c.getDouble(c.getColumnIndexOrThrow("texu"));
		texv = c.getDouble(c.getColumnIndexOrThrow("texv"));
		neighbors = _neighbors;
		isBusStop = true;
		locations = null;
		buses = _buses;
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
	
	public double distance(double lat, double lng) {
		double dLat = Math.toRadians(lat-latitude);
		double dLon = Math.toRadians(lng-longitude);
		double lat1 = Math.toRadians(latitude);
		double lat2 = Math.toRadians(lat);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		
		return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}
}
