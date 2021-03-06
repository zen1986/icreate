package com.npf.logic;

import java.util.ArrayList;

import com.npf.data.DataCache;
import com.npf.data.MapNode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class GPSLocator {

	private LocationManager locationManager;
	private LocationListener locationListener;
	private String locationProvider = LocationManager.GPS_PROVIDER;
	private Location bestKnownLocation;
	private Context ctx;
	private DataCache dbcache;
	private int NEARBY_THRESHOLD = 200;
	
	public GPSLocator(Context c) {
		ctx = c;
		dbcache = DataCache.getInstance(ctx);
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      setBestKnownLocation(location);
		    }
	
		    public void onStatusChanged(String provider, int status, Bundle extras) {}
	
		    public void onProviderEnabled(String provider) {}
	
		    public void onProviderDisabled(String provider) {}
		  };
	}
	
	public void setBestKnownLocation(Location loc) {
		if (bestKnownLocation==null) {
			bestKnownLocation = getLastKnownLocation();
		}
		
		if (isBetterLocation(loc, bestKnownLocation)) {
			bestKnownLocation = loc;
		}
	}
	
	public Location getBestKnowLocation() {
		return bestKnownLocation;
	}
	
	public void startUpdate() {

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	}
	
	public void stopUpdate() {
		locationManager.removeUpdates(locationListener);
	}
	
	public Location getLastKnownLocation() {
		return locationManager.getLastKnownLocation(locationProvider);
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	private String[] getNearbyLocations() {
		int count = dbcache.getNodeCount();
		ArrayList<String> names = new ArrayList<String>();
		for (int i=0;i<count;i++) {
			MapNode n = dbcache.getNodeByIdx(i);
			double dist = n.distance(bestKnownLocation.getLatitude(), bestKnownLocation.getLongitude());
			if (dist < NEARBY_THRESHOLD){
				for (String name: n.locations) {
					names.add(ComUtil.decode(name));  //decode to show user-friendly values
				}
			}
			Log.i("NPFdebug","Checking node "+n.name+" distance "+dist);
		}
		if (names.size()>0) {
			String[] n = new String[names.size()];
		
			names.toArray(n);
			return  n;
		}
		else {
			String[] n = new String[]{"You are not able to be located."};
			return n;
		}
	}
	
	public Dialog getDialog(final EditText et) {
		final CharSequence[] items = getNearbyLocations();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Pick nearby location");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	et.setText(items[item]);
		    }
		});
		AlertDialog n =  builder.create();
		return n;
	}
}
