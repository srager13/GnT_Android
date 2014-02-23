package org.scottrager.appfunding;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

public class LocationService extends Service {
	
	public static final String LOCATION_SERVICE = "locationservice";
	
	public static final String BROADCAST_NEW_LOCATION = "newlocation";
	
	private LocationManager locationMgr;
	private LocationListener locationListener;
	private Location currentLocation;
	
	//for mock locations
	public LocationClient mLocationClient;
	
	private final IBinder mBinder = new LocationServiceBinder();

	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int LOCATION_UPDATE_DELAY = 1000 * 3; // time to wait before receiving new location
	
	private boolean locationAvailable;
	
	public class LocationServiceBinder extends Binder
	{
		LocationService getService() {
			return LocationService.this;
		}

	}
	
	public LocationService() {		
	}
	
	@Override
	public void onCreate( ) { 
	  
		Log.d(LOCATION_SERVICE, "In onCreate of LocationService.");
		if( !(locationAvailable = setUpLocMgrIfNeeded()) )
		{
			Log.d(LOCATION_SERVICE, "Failed to set up location manager in LocationService.");
		}
		else
		{
			Log.d(LOCATION_SERVICE, "Successfully set up location manager in LocationService.");
			// first get last known location to use until updated location arrives
			Location lastKnown = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if( lastKnown != null )
			{
				Log.d(LOCATION_SERVICE, "Set current location to last known location");
				currentLocation = new Location( lastKnown );
				//currentLocation.set(lastKnown);
				locationAvailable = true;
			}
			else
			{
				Log.d(LOCATION_SERVICE, "Last known location is not null.");
			}
			// Define a listener that responds to location updates
			locationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
			    	Log.d(LocationService.LOCATION_SERVICE, "in LocationService:  onLocationChanged(): new location? Broadcast it!");
			      // Called when a new location is found by the network location provider.
			    	//if( isBetterLocation(location, currentLocation) )
			    	//{
			    	//	makeUseOfNewLocation(location);
			    	//}
			    	currentLocation = new Location(location);
			    	//currentLocation.set(location);
			    	locationAvailable = true;
			    	
			    	BroadcastNewLocation();
			    }
	
			    public void onStatusChanged(String provider, int status, Bundle extras) {
			    	Log.d(LocationService.LOCATION_SERVICE, "in LocationService:  onStatusChanged(): provider = "+provider);
			    	switch( status )
			    	{
				    	case LocationProvider.OUT_OF_SERVICE:
				    	{
					    	Log.d(LocationService.LOCATION_SERVICE, "\tStatus = OUT_OF_SERVICE");
					    	break;
				    	}
				    	case LocationProvider.TEMPORARILY_UNAVAILABLE:
				    	{
					    	Log.d(LocationService.LOCATION_SERVICE, "\tStatus = TEMPORARILY_UNAVAILABLE");
					    	break;
				    	}
				    	case LocationProvider.AVAILABLE:
				    	{
					    	Log.d(LocationService.LOCATION_SERVICE, "\tStatus = AVAILABLE");
					    	break;
				    	}
			    	}
			    }
	
			    public void onProviderEnabled(String provider) {
			    	Log.d(LOCATION_SERVICE, "in onProviderEnabled(): provider = "+provider);
			    }
	
			    public void onProviderDisabled(String provider) {
			    	Log.d(LOCATION_SERVICE, "in onProviderDisabled(): provider = "+provider);
			    }

			  };
	
			  registerLocationListener();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public Location getCurrentLocation() {
		if( locationAvailable )
		{
			Log.d(LOCATION_SERVICE, "in getCurrentLocation: locationAvailable = true");
			return currentLocation;
		}
		else
		{
			Log.d(LOCATION_SERVICE, "in getCurrentLocation: locationAvailable = false");
			return null;
		}
	}
	
	private boolean setUpLocMgrIfNeeded() {
		Log.d(LOCATION_SERVICE, "Setting up Location Manager.");
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (locationMgr == null) {
			Log.d(LOCATION_SERVICE, "locationMgr was null.");
	        locationMgr = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	        // Check if we were successful in obtaining the map.
	        if (locationMgr != null) {
				Log.d(LOCATION_SERVICE, "locationMgr not null anymore.");
	            // The Map is verified. It is now safe to manipulate the map.
	        	return true;
	        }
	        return false;
	    }
	    return true;
	}

	private void registerLocationListener() {
		if( locationMgr == null )
		{
			Log.d(LOCATION_SERVICE, "locationMgr was null in registerLocationListener()");
			return;
		}
		// Register the listener with the Location Manager to receive location updates
		locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_DELAY, 10, locationListener);
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_DELAY, 100, locationListener);
		Log.d(LOCATION_SERVICE, "Location Service registered to get location updates from NETWORK_PROVIDER and GPS_PROVIDER");
	}
	
	private void BroadcastNewLocation() {
		Intent intent = new Intent();
		intent.setAction(BROADCAST_NEW_LOCATION);
		sendBroadcast(intent);
	}
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
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
	
	
//	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// send broadcast to activity
//			Log.d(LOCATION_SERVICE, "In locationRecevier:  sending broadcast of new location.");
//			
//		}
//		
//		
//	};
}
