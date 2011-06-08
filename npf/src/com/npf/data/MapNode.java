package com.npf.data;

import java.util.ArrayList;

import android.database.Cursor;

public class MapNode {
	public double latitude;
	public double longitude;
	public double texu;
	public double texv;
	public String name;
	public ArrayList<MapNode> neighbors;
	
	public MapNode(Cursor c) {
		name = c.getString(c.getColumnIndexOrThrow("name"));
		latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
		longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));
		texu = c.getDouble(c.getColumnIndexOrThrow("texu"));
		texv = c.getDouble(c.getColumnIndexOrThrow("texv"));
	}
	
}
