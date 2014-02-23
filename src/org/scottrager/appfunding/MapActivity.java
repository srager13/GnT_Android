package org.scottrager.appfunding;

import java.util.ArrayList;
import java.util.HashMap;

import org.scottrager.appfunding.LocationService.LocationServiceBinder;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMarkerClickListener {

	public final static String TAG = "mapactivity";
//	private static final int TWO_MINUTES = 1000 * 60 * 2;
//	private static final int LOCATION_UPDATE_DELAY = 1000 * 3; // time to wait before receiving new location
	private GoogleMap map;
//	private OnInfoWindowClickListener onInfoWindowClickListener;
//	private LocationManager locationMgr;
//	private LocationListener locationListener;
	private Location currentLocation = null;
	//private Time lastLocUpdateTime;
	LocationServiceBinder binder;
	
	private LocationService locationService;
	private boolean mBound = false;

	private DBAdapter db;
	private ArrayList<MarkerObject> markers;
	private HashMap<String, Integer> markerMap;  // maps company name to position in markers arraylist
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		
		db = new DBAdapter(this);
		markers = new ArrayList<MarkerObject>();
		markerMap = new HashMap<String, Integer>();
		 
		try {
		     MapsInitializer.initialize(this);
		 } catch (GooglePlayServicesNotAvailableException e) {
		     e.printStackTrace();
		 }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// register to receive new location updates from location service
		if( locationReceiver != null )
		{
			IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION);
			registerReceiver(locationReceiver, intentFilter);
		}
		
		// get a handle to the GoogleMap object and location manager
		setUpMapIfNeeded();

		if( !IsConnected() )
		{
			Toast.makeText(getApplicationContext(), "Cannot load Google Maps with no data connection.", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG, "In onPause()");
		// unregister from receiving location updates
		unregisterReceiver(locationReceiver);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "In onStart");
		super.onStart();
		Intent locService = new Intent(this, LocationService.class);
		bindService(locService, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		unbindService(mConnection);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}
	
	private void centerMap(Location loc) {
		LatLng newLoc = new LatLng( loc.getLatitude(), loc.getLongitude() );
		//LatLng newLoc = new LatLng( 40.7914, -77.8586 );
		CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLng(newLoc);
		if( cameraUpdate != null )
		{
			map.animateCamera( cameraUpdate);
		}
		else
		{
			displayCannotFindLocToast();
			finish();
		}
	}
	
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
		Log.d(TAG, "in setUpMapIfNeeded()");
	    if (map == null) {
	        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	        
	        // Check if we were successful in obtaining the map. 
	        if (map != null) {
	            // The Map is verified. It is now safe to manipulate the map.
		        map.setInfoWindowAdapter( customInfoWindow );  // customInfoWindow defined below
		        // set up what to do when info window is clicked
		        map.setOnInfoWindowClickListener( customWindowClickListener ); // customWindowClickListener
		        map.setMyLocationEnabled(true);

	        }
	        else
	        {
	    		Toast.makeText(getApplicationContext(), "Map Currently Unavailable.", Toast.LENGTH_LONG).show();
	        	finish();
	        }
	    }
	}	
	InfoWindowAdapter customInfoWindow = new InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            int pos = markerMap.get(marker.getTitle());
            
            Log.d(MapActivity.TAG, "in customInfoWindow: position of "+marker.getTitle()+" = "+ pos);
            String name = markers.get(pos).getCouponName();
            Log.d(MapActivity.TAG, "getCouponName returns "+name);

            View window = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            TextView compName = ((TextView) window.findViewById(R.id.marker_company_name));
            compName.setText( name );
            
             TextView compAddr1 = ((TextView) window.findViewById(R.id.marker_address_1));
             if (compAddr1 != null)
             {
            	 compAddr1.setText("Company Addr 1");
             }

             TextView compAddr2 = ((TextView) window.findViewById(R.id.marker_address_2));
             if (compAddr2 != null)
             {
            	 compAddr2.setText("Company Addr 2");
             }

             TextView numOffers = ((TextView) window.findViewById(R.id.marker_num_offers));
             if (numOffers != null)
             {
            	 int numberOffers = markers.get(pos).getNumOffers();
            	 if( numberOffers == 1 )
            		 numOffers.setText(""+markers.get(pos).getNumOffers()+" Offer");
            	 else
            		 numOffers.setText(""+markers.get(pos).getNumOffers()+" Offers");
             }

            return window;
        }

        @Override
        public View getInfoContents(Marker marker) {
            // this method is not called if getInfoWindow(Marker) does not
            // return null
            return null;
        }
    };

    OnInfoWindowClickListener customWindowClickListener = new OnInfoWindowClickListener() {
    	public void onInfoWindowClick(Marker marker) {
    		Log.d(TAG, "Clicked on info window of"+marker.getTitle());
    		//startActivity( new Intent( getBaseContext(), BrowseCouponsActivity.class ));
    		return;
    	}
    };
	
	private void makeUseOfNewLocation(Location location) {
		Log.d(MapActivity.TAG, "In makeUseOfNewLocation().");
		if( location == null )
		{
			Log.d(MapActivity.TAG, "location is null");
			displayCannotFindLocToast();
			return;
		}
		centerMap(location);
		
		placeMarkersForNearbyPlaces(location);
	}
	
	public void displayCannotFindLocToast() {
		Toast.makeText(getApplicationContext(), "Cannot find current location.", Toast.LENGTH_LONG).show();
	}

	private void placeMarkersForNearbyPlaces(Location location) {
		Log.d(MapActivity.TAG, "In placeMarkersForNearbyPlaces().");
		
		if( location == null )
		{
			Log.d(MapActivity.TAG, "location is null");
			displayCannotFindLocToast();
			return;
		}

		markers.clear();
		markerMap.clear();
		
/*		if( db == null )
			Log.d(TAG, "db is null!!");
		Cursor c1 = db.getAllUnusedCoupons();
		Log.d(TAG, "number of unused coupons = "+c1.getCount());
		// TODO:: Get locations and numbers of coupons from database
		Cursor c = db.getLocations();
		
		if( c.moveToFirst() )
		{
			do
			{
				int companyId = c.getInt(c.getColumnIndex(DBAdapter.KEY_COMPANYID));
				Cursor c2 = db.getCompanyNameFromId(companyId);
				String companyName = c2.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME));
				String companyFileURL = c2.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL));
				if( companyName == null || companyFileURL == null )
				{
					Log.d(TAG, "ERROR:  Could not find name or file url for company with id = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_COMPANYID)));

					continue;
				}
				double latitude = c.getDouble(c.getColumnIndex(DBAdapter.KEY_LATITUDE));
				double longitude = c.getDouble(c.getColumnIndex(DBAdapter.KEY_LONGITUDE));
				float results[] = new float[3];
				Location.distanceBetween( location.getLatitude(), location.getLongitude(), latitude, longitude, results );
				double distance = results[0];
				int numCoupons = db.getNumCouponsFromId(companyId);

				// String name, String exp_date, String compName, String detail, int eventid, double dist, 
				//        int num_offers, double latitudeIn, double longitudeIn 
				
				MarkerObject m = new MarkerObject( companyName, "0", companyFileURL, "", 1, distance, numCoupons, latitude, longitude );
				markers.add(m);
				markerMap.put( companyName, 0 );
			}while( c.moveToNext() );
		}
*/		
		
/*		MarkerObject marker2 = new MarkerObject( "McDonald's", "0", "mcdonalds", "", 1, 0.0, 1, 
				currentLocation.getLatitude()+0.01, currentLocation.getLongitude()-0.01);
		markers.add(marker2);
		markerMap.put("McDonald's", 0);
		
		MarkerObject marker1 = new MarkerObject( "Sheetz", "0", "sheetz", "", 1, 0.0, 2, 
				currentLocation.getLatitude()+0.01, currentLocation.getLongitude()+0.01);
		markers.add(marker1);
		markerMap.put("Sheetz", 1);
		*/

		Log.d(MapActivity.TAG, "markers.size() = "+markers.size());
		for(int i = 0; i < markers.size(); i++ )
		{			
			final double lat = markers.get(i).getLatitude();
			final double longi = markers.get(i).getLongitude();
			String title = markers.get(i).getCouponName();
			Log.d(MapActivity.TAG, "Setting marker for "+title);
			Log.d(MapActivity.TAG, "Lat = "+lat+", Long = "+longi);

			LatLng loc = new LatLng( lat, longi );
			Marker m = map.addMarker(new MarkerOptions()
			.position(loc)
			.title( title )
			.draggable(false));
			m.showInfoWindow();

			Log.d(MapActivity.TAG, "Set marker for "+markers.get(i).getCouponName());
		}
		
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		Log.d(MapActivity.TAG, "Clicked on marker");
		return true;
	}
	
	private void getCurrentLocation() {

		if( mBound )
		{
			Location currentLoc = locationService.getCurrentLocation();
			if( currentLoc != null )
			{			
				Log.d(LocationService.LOCATION_SERVICE, "in MapActivity in getCurrentLocation");
				currentLocation = new Location(currentLoc);
				makeUseOfNewLocation(currentLocation);
			}
		}
	}

	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	    	binder = (LocationServiceBinder)service;
	    	locationService = binder.getService();
	    	mBound = true;

			Log.d(LocationService.LOCATION_SERVICE, "in MapActivity: bound to location service");
			getCurrentLocation();

	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	    	mBound = false;
			Log.d(LocationService.LOCATION_SERVICE, "in MainActivity: bind to location service disconnected");
	    }
	};
	
	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// received new location from location service
			Log.d(TAG, "In MapActivity's BroadcastReceiver...got new location");
			if( mBound )
			{
				getCurrentLocation();
			}
		}
	};

}
