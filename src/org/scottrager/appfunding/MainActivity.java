package org.scottrager.appfunding;

import java.util.List;

import org.scottrager.appfunding.LocationService.LocationServiceBinder;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "mainactivity";
	public static final String PREFS_FILE = "GiveAndTakePrefs";
	public static final String USERNAME = "Username";
	public static final String PASSWORD_HASH = "PasswordHash";
	public static final String LOCATION_TAG = "locationtag";

	LocationServiceBinder binder;
	
	private LocationService locationService;
	private Location currentLocation = null;
	private boolean mBound = false;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        if( !isNetworkAvailable() )
        {
			Toast.makeText(getApplicationContext(), "No Network Available.", Toast.LENGTH_LONG).show();
        }

//    	SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
//    	boolean firstOpen = prefs.getBoolean("firstOpen", true);        
//    	//firstOpen = true;
//    	if( firstOpen )
//    	{
//		//	Toast.makeText(getApplicationContext(), "First Time Open.", Toast.LENGTH_LONG).show();
//    		Log.d(TAG, "First time opened.");
//
//		    SharedPreferences.Editor editor = prefs.edit();
//		    editor.putBoolean("firstOpen", false);
//		    editor.commit();
//    		
//    	}
//    	else
//    	{
//		//	Toast.makeText(getApplicationContext(), "Not first time opened.", Toast.LENGTH_LONG).show();
//    		Log.d(TAG, "Not first time opened.");
//    	}
    	
    }
	@Override
	public void onStart() {
		Log.d(TAG, "In onStart");
		super.onStart();
		Intent locService = new Intent(this, LocationService.class);
		bindService(locService, mConnection, Context.BIND_AUTO_CREATE);
		
		FlurryAgent.onStartSession(this, "J9WHX3VYHPRX8K756WTJ");
	}
	@Override
	public void onStop() {
		super.onStop();
		unbindService(mConnection);
		
		FlurryAgent.onEndSession(this);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
		
    	// if user does not want to stay logged in, forget login in and pw
		SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		
		if( !prefs.getBoolean(LoginActivity.STAY_LOGGED, false) )
    	{
			editor.putString( LoginActivity.USERNAME, "" );
			editor.putString( LoginActivity.PASSWORD_HASH, "");
			editor.putBoolean(LoginActivity.LOGGED_IN, false);
			editor.commit();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void searchNewCoupons( View view ) {
    	Intent intent = new Intent( this, SearchNewCouponsActivity.class );
    	startActivity( intent );
    	return;
    }
    
    public void browseCoupons( View view ) {

    	//check to make sure user is logged in
        SharedPreferences prefs =  getSharedPreferences( PREFS_FILE, 0);
    	if( prefs.getBoolean(LoginActivity.LOGGED_IN, false) )
    	{
    		Intent intent = new Intent( this, BrowseCouponsActivity.class );
    		startActivity( intent );
    		return;
    	}
    	else
    	{
    		Intent intent = new Intent();
    		intent.setClass(this, LoginActivity.class);
    		startActivity(intent);
    		finish();
    	}
    }
    
    public void goToWebpage( View view ) {
    	Uri gntPage = Uri.parse("http://www.mygiveandtake.com/fundraise-with-gt");
    	Intent intent = new Intent( Intent.ACTION_VIEW, gntPage );
    	
    	// Verify it resolves
    	PackageManager packageManager = getPackageManager();
    	List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    	boolean isIntentSafe = activities.size() > 0;
    	if ( isIntentSafe )
    		startActivity( intent );
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) 
          getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
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

			Log.d(LocationService.LOCATION_SERVICE, "in MainActivity: bound to location service");

			Location currentLoc = locationService.getCurrentLocation();
			if( currentLoc != null )
			{
				currentLocation = new Location(currentLoc);
			}
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	    	mBound = false;
			Log.d(LocationService.LOCATION_SERVICE, "in MainActivity: bind to location service disconnected");
	    }
	};
   
}


