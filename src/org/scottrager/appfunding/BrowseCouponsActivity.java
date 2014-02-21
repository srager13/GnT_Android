package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scottrager.appfunding.LocationService.LocationServiceBinder;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BrowseCouponsActivity extends FragmentActivity {
	
    public static final String TAG = "browsecoupons";
    public static final String SIDEBAR_ANIM_TAG = "sidebaranimation";
    public static final String POSITION_TAG = "positiontag";
    public static final String LOCATION_TAG = "locationtag";

    public static boolean CREATE_FILES_MANUALLY = false;
	private static final String PREFS_FILE = "GiveAndTakePrefs";
    public String[] myFiles;

    private ArrayList<CouponObject> coupons;
    private ArrayList<CouponObject> usedCoupons;
    private ArrayList<CouponObject> unsyncedCoupons;
	
	LocationServiceBinder binder;
	
	private LocationService locationService;
	//private LocationListener locationListener;
	private Location currentLocation;
	private boolean useLocations = false;
	
	// if true, button to right of search bar executes search, otherwise, it clears the search
	//   button text(background) will be set to proper value
	private boolean executeSearch;
    
    private static SortByValueEnum sortByValue = SortByValueEnum.SORT_BY_NEAREST;
	
	private DBAdapter db;
	private int use_coup_request_Code = 1;
	
	private int endOffset;
	private boolean expanded;
	private boolean mBound = false;
	
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

			Log.d(LocationService.LOCATION_SERVICE, "in BrowseCouponsActivity: bound to location service");
			refreshActivity();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	    	mBound = false;
			Log.d(LocationService.LOCATION_SERVICE, "in BrowseCouponsActivity: bind to location service disconnected");
	    }
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.browse_coupons_w_sidebars);

		coupons = new ArrayList<CouponObject>();
		usedCoupons = new ArrayList<CouponObject>();
		unsyncedCoupons = new ArrayList<CouponObject>();
		
		db = new DBAdapter(this);
		
		endOffset = 100;
		expanded = false;

		executeSearch = true;
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
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_browse_coupons, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver( locationReceiver );

		Log.d(TAG, "In onPause()");
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		
		if( locationReceiver != null )
		{
			IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION);
			registerReceiver(locationReceiver, intentFilter);
		}
		
		refreshActivity();
	}
	
	private void refreshActivity() {
		
		TryToGetLocation();
		
		refreshCouponListFromDB();

		Button getNewCoupsButton = (Button) findViewById(R.id.getNewCouponsButton);
		getNewCoupsButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(BrowseCouponsActivity.TAG, "Should go to search coupons activity.");				
				onGetNewCouponsButtonClicked();
			}
		});
		
		if( useLocations )
		{
			sortByNearest( findViewById(R.id.sort_by_nearest_button));
		}
		else
		{
			sortByEndingSoon( findViewById(R.id.sort_by_ending_soon_button));
		}
		drawCouponList();
		CallSyncCoupons();
		CallSyncRecvdCoupons();
	}
	
	private void TryToGetLocation() {
		
		if( mBound )
		{
			Location newLocation = locationService.getCurrentLocation();
			if( newLocation != null )
			{
				Log.d(LocationService.LOCATION_SERVICE, "in BrowseCouponsActivity.TryToGetLocation: getCurrentLocation not null");
				currentLocation = new Location(locationService.getCurrentLocation());
				useLocations = true;
			}
			else
			{
				Log.d(LocationService.LOCATION_SERVICE, "in BrowseCouponsActivity.TryToGetLocation: getCurrentLocation = null");
				useLocations = false;
			}
		}
		else
		{
			Log.d(LocationService.LOCATION_SERVICE, "in BrowseActivity.TryToGetLocation: mBound is false");
		}
	}
	
	public void onGetNewCouponsButtonClicked() {
		Intent intent = new Intent( this, SearchNewCouponsActivity.class );
    	startActivity( intent );	
	}
	
	public void useCouponConfirmed( int position ) {
		
		if( position == -1 )
		{
			Log.d( TAG, "ERROR: Got a confirmation of using coupon in position -1 in useCouponConfirmed() in BrowseCouponsActivity.java ");
			return;
		}
		// user clicked "yes" in alert dialog asking if they want to use the coupon
		//   we need to remove the coupon from the list

		Log.d(BrowseCouponsActivity.TAG, "in useCouponConfirmed()...position = "+position);
		
		// mark coupon used in database...this function updates the DATE_USED field
		db.open();
		if( !db.markCouponUsed(coupons.get(position).getRowId()) )
		{
			Log.d(BrowseCouponsActivity.TAG, "markCouponUsed returned an error");
			db.close();
		}
		else
		{
			db.close();
			CallSyncCoupons();
		}
		
		coupons.remove(position);
		
		drawCouponList();
	}
	
	public void refreshCouponListFromDB() {
		Log.d(TAG, "in refreshCouponListFromDB()");
		TryToGetLocation();
		db.open();
		Cursor c = db.getAllUnusedCoupons();

		coupons.clear();

		if( c.moveToFirst() ) // at least one row was returned
		{
			do
			{
				Log.d(BrowseCouponsActivity.TAG, "rowId = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));	
				Log.d(BrowseCouponsActivity.TAG, "company name = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)));
				Log.d(BrowseCouponsActivity.TAG, "coupon details = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)));
				
				boolean favorite;
				int fav = c.getInt(c.getColumnIndex(DBAdapter.KEY_FAVORITE));
				if( fav == 0 )
				{
					favorite = false;
				}
				else
				{
					favorite = true;
				}
				Log.d(BrowseCouponsActivity.TAG, "favorites = " + fav );
				Log.d(BrowseCouponsActivity.TAG, "Exp Date = "+c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)));
				Log.d(BrowseCouponsActivity.TAG, "Logo = "+c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)));
				Log.d(BrowseCouponsActivity.TAG, "Date Used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));

				double distance = getDistance( 1.0, 1.0 );
				if( useLocations )
				{
					Log.d(LOCATION_TAG, "Finding location for coupon (useLocations = true)");
				
					float results[] = {(float) 0.0, (float) 0.0, (float) 0.0};
					Log.d(TAG, "Current latitude: " + currentLocation.getLatitude() + ", Current longitude: " + currentLocation.getLongitude() );
					
					Location.distanceBetween( currentLocation.getLatitude(), currentLocation.getLongitude(), 
							c.getFloat(c.getColumnIndex(DBAdapter.KEY_LATITUDE)), c.getFloat(c.getColumnIndex(DBAdapter.KEY_LONGITUDE)), results );
					distance = results[0];
				}
				else
				{
					Log.d(BrowseCouponsActivity.TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
					distance = -1.0;
				}					
				Log.d(LOCATION_TAG, "Distance = "+distance);
				
				//String name, String exp_date, String pic, String detail, 
				//       boolean fav, int rowid, double dist, int date_used
				CouponObject newCoup = new CouponObject( c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)),
						favorite, 
						c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)),
						distance,
						0); // can simply put 0 for date_used since query only returned coups with this value
				coupons.add(newCoup);
			}while( c.moveToNext() );
		}
		else
		{
			Log.d(BrowseCouponsActivity.TAG, "No coupons in database.");
		}
		db.close();		
	}
	public void refreshCouponListFromDBFilteredByCompany( String CompanyName ) {
		Log.d(TAG, "in refreshCouponListFromDB()");
		TryToGetLocation();
		db.open();
		Cursor c = db.getAllUnusedCouponsOfCompany( CompanyName );

		coupons.clear();

		if( c.moveToFirst() ) // at least one row was returned
		{
			do
			{
				Log.d(BrowseCouponsActivity.TAG, "rowId = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));	
				Log.d(BrowseCouponsActivity.TAG, "company name = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)));
				Log.d(BrowseCouponsActivity.TAG, "coupon details = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)));
				
				boolean favorite;
				int fav = c.getInt(c.getColumnIndex(DBAdapter.KEY_FAVORITE));
				if( fav == 0 )
				{
					favorite = false;
				}
				else
				{
					favorite = true;
				}
				Log.d(BrowseCouponsActivity.TAG, "favorites = " + fav );
				Log.d(BrowseCouponsActivity.TAG, "Exp Date = "+c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)));
				Log.d(BrowseCouponsActivity.TAG, "Logo = "+c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)));
				Log.d(BrowseCouponsActivity.TAG, "Date Used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));

				double distance = getDistance( 1.0, 1.0 );
				if( useLocations )
				{
					Log.d(LOCATION_TAG, "Finding location for coupon (useLocations = true)");
				
					float results[] = {(float) 0.0, (float) 0.0, (float) 0.0};
					Log.d(TAG, "Current latitude: " + currentLocation.getLatitude() + ", Current longitude: " + currentLocation.getLongitude() );
					
					Location.distanceBetween( currentLocation.getLatitude(), currentLocation.getLongitude(), 
							c.getFloat(c.getColumnIndex(DBAdapter.KEY_LATITUDE)), c.getFloat(c.getColumnIndex(DBAdapter.KEY_LONGITUDE)), results );
					distance = results[0];
				}
				else
				{
					Log.d(BrowseCouponsActivity.TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
					distance = -1.0;
				}					
				Log.d(LOCATION_TAG, "Distance = "+distance);
				
				//String name, String exp_date, String pic, String detail, 
				//       boolean fav, int rowid, double dist, int date_used
				CouponObject newCoup = new CouponObject( c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)),
						c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)),
						favorite, 
						c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)),
						distance,
						0); // can simply put 0 for date_used since query only returned coups with this value
				coupons.add(newCoup);
			}while( c.moveToNext() );
		}
		else
		{
			Log.d(BrowseCouponsActivity.TAG, "No coupons in database.");
		}
		db.close();	
	}
	
	public void drawCouponList() {
		
		switch( sortByValue )
		{
			case SORT_BY_NAME:
				Log.d(TAG,  "Sorting by Name");
				Collections.sort( coupons, CouponComparator.descending( CouponComparator.getComparator(CouponComparator.NAME_SORT) ) );
				break;
			case SORT_BY_NEAREST:
				Log.d(TAG,  "Sorting by Nearest");
				Collections.sort( coupons, CouponComparator.descending( CouponComparator.getComparator(CouponComparator.NEAREST_SORT, CouponComparator.NAME_SORT) ) );
				break;
			case SORT_BY_EXP_DATE:
				Log.d(TAG,  "Sorting by Exp Date");
				Collections.sort( coupons, CouponComparator.descending( CouponComparator.getComparator(CouponComparator.EXP_DATE_SORT, CouponComparator.NAME_SORT) ) );
				break;
			default:
				break;
		}

		// TODO: Fix search suggestions and implement searching function
		ArrayList<String> names = new ArrayList<String>();
		for( int i = 0; i < coupons.size(); i++ )
		{
			//if( !names.contains(coupons.get(i).getCouponName()) )
			//{
				names.add(coupons.get(i).getCouponName());
			//}
		}
		CouponListArrayAdapter adapter = new CouponListArrayAdapter(this, coupons, names, true);
		TextView noCoupsMessage = (TextView)findViewById(R.id.noCouponsMessage);
		Button getNewCoupsButton = (Button)findViewById(R.id.getNewCouponsButton);

		if( coupons.isEmpty() )
		{
			Log.d(TAG, "No coupons to draw");
			ListView list = (ListView)findViewById(R.id.couponList);
			list.setAdapter(null);
			noCoupsMessage.setText(R.string.no_coupons_message);
			noCoupsMessage.bringToFront();
			noCoupsMessage.setVisibility(View.VISIBLE);
			getNewCoupsButton.setVisibility(View.VISIBLE);
			getNewCoupsButton.setClickable(true);
			getNewCoupsButton.bringToFront();
			return;
		}
		else
		{
			noCoupsMessage.setVisibility(View.GONE);
			getNewCoupsButton.setVisibility(View.GONE);
		}
		
		ListView list = (ListView)findViewById(R.id.couponList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    	String imageName = coupons.get(position).getCouponPic();
		    	String companyName = coupons.get(position).getCouponName();
		    	String couponDetail = coupons.get(position).getCouponDetail();

		        Intent coupDisplayIntent = new Intent(v.getContext(), CouponDisplayActivity.class);
		        Bundle b = new Bundle();
		        b.putString("couponPic", imageName);
		        b.putInt("position", position);
		        b.putString("companyName", companyName);
		        b.putString("couponDetail", couponDetail);
		        b.putBoolean("usable", true);
		        coupDisplayIntent.putExtras(b);

		        startActivityForResult( coupDisplayIntent, use_coup_request_Code );
		    }
		});
		
		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.coupon_search_text_box );
		ArrayAdapter<String> searchSuggAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		editBox.setAdapter(searchSuggAdapter);
		
		return;
	}
	
	public void onActivityResult( int requestCode, int resultCode, Intent data ) 
	{
		Log.d(BrowseCouponsActivity.TAG, "Entered OnActivityResult():");
		//super.onActivityResult( requestCode,  resultCode,  data );
		if( requestCode == use_coup_request_Code )
		{
			if( resultCode == RESULT_OK )
			{
				int pos = data.getIntExtra("position", 0);
				int rowId = -1;
				Log.d(BrowseCouponsActivity.POSITION_TAG, "OnActivityResult() in BrowseCouponsActivity...position = "+pos);
				if( pos != -1 )
				{
					rowId = coupons.get(pos).getRowId();
					Log.d(BrowseCouponsActivity.POSITION_TAG, "OnActivityResult() in BrowseCouponsActivity...rowId = "+rowId);
					useCouponConfirmed( pos );
				}
			}
		}
		//refreshCouponListFromDB();
		//drawCouponList();
	}
	
	public void sortByNearest( View view ) {
		if( !useLocations )
		{
			displayCannotFindLocToast();
			sortByEndingSoon( view );
			return;
		}
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_ending_soon_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_az_button);
		b2.setSelected(false);
		Button b3 = (Button) findViewById(R.id.go_to_map_button);
		b3.setSelected(false);
		
		sortByValue = SortByValueEnum.SORT_BY_NEAREST;
		drawCouponList();
	}
	
	public void onSearchButtonClick( View view ) {

		EditText searchBox = (EditText) findViewById(R.id.coupon_search_text_box);
		Button searchButton = (Button)findViewById(R.id.exec_coup_search_button);
		
		if( executeSearch )
		{
			String companyName = searchBox.getText().toString();
		
			// change execute search button to clear
			searchButton.setText("Clear");
			executeSearch = false;
		
			refreshCouponListFromDBFilteredByCompany( companyName );
		}
		else // clearing previously done search
		{
			// change execute search button to clear
			searchButton.setText("Go");
			executeSearch = false;			
			// clear text in search box 
			searchBox.setText("");

			refreshCouponListFromDB();
		}
		drawCouponList();
	}
	
	public void sortByEndingSoon( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_nearest_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_az_button);
		b2.setSelected(false);
		Button b3 = (Button) findViewById(R.id.go_to_map_button);
		b3.setSelected(false);

		sortByValue = SortByValueEnum.SORT_BY_EXP_DATE;
		drawCouponList();
	}
	
	public void sortAlphabetically( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_ending_soon_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_nearest_button);
		b2.setSelected(false);
		Button b3 = (Button) findViewById(R.id.go_to_map_button);
		b3.setSelected(false);

		sortByValue = SortByValueEnum.SORT_BY_NAME;
		drawCouponList();
	}
	
	public void goToMap( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_ending_soon_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_nearest_button);
		b2.setSelected(false);
		Button b3 = (Button) findViewById(R.id.sort_by_az_button);
		b3.setSelected(false);
		Intent intent = new Intent( this, MapActivity.class );
		startActivity(intent);
	}
	
	public double getDistance( double loc1, double loc2 )
	{
		return 0.0;
	}
	
	public void displayCannotFindLocToast() {
		Toast.makeText(getApplicationContext(), "Cannot find current location.", Toast.LENGTH_LONG).show();
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void onLaunchSidebarButtonClick( View view ) {
		Log.d(SIDEBAR_ANIM_TAG, "in onLaunchSidebarButtonClick");
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		int width;
		if( android.os.Build.VERSION.SDK_INT > 12 )
		{
			display.getSize(size);
			width = size.x;
		}
		else
		{
			width = display.getWidth();
		}
		Log.d(SIDEBAR_ANIM_TAG, "width = "+width);
		LinearLayout optionsSidebarView = (LinearLayout)findViewById(R.id.options_sidebar);
		if( optionsSidebarView == null )
		{
			Log.d(SIDEBAR_ANIM_TAG, "null value for optionsSidebarView");
			return;
		}
		ScrollView mainView = (ScrollView)findViewById(R.id.browse_coupons_mainpage);
		//RelativeLayout mainView = (RelativeLayout)findViewById(R.id.browse_coupons_mainpage);
		if( mainView == null )
		{
			Log.d(SIDEBAR_ANIM_TAG, "null value for mainView");
			return;
		}
		Log.d(SIDEBAR_ANIM_TAG, "got layouts");
		
		if(optionsSidebarView.getVisibility() == View.INVISIBLE) { 
			Log.d(SIDEBAR_ANIM_TAG, "trying to make sidebar visible and scroll main view");
		    optionsSidebarView.setVisibility(View.VISIBLE);
		    // this is set to 260 because that is the width of the sidebar layout
		    //    defined in options_sidebar_layout.xml
		    int xPos = optionsSidebarView.getRight();
		    mainView.scrollTo(-(xPos), 0);
		    //mainView.smoothScrollTo(-xPos, 0);
		    expanded = true;
		}
		else
		{
			Log.d(SIDEBAR_ANIM_TAG, "trying to make sidebar invisible again and scroll main view back");
			mainView.scrollTo(0,0);
//			mainView.smoothScrollTo(0,0);
			optionsSidebarView.setVisibility(View.INVISIBLE);
			expanded = false;
		}
	}

	protected void applyTransformation(float interpolatedTime, Transformation t) {
		Log.d(SIDEBAR_ANIM_TAG, "in applyTransformation");
	    int newOffset;
	    if(expanded) {
	        newOffset = 0;
	        newOffset = (int)(endOffset*(1-interpolatedTime));
	    } else {
	        newOffset = (int)(endOffset*(interpolatedTime));
	    }
//	    view.scrollTo(-newOffset, 0);
	    Log.d( TAG, "newOffset = " + newOffset );
	}
	
	public void onClickMoreInfo( View view ) {
    	Uri gntPage = Uri.parse("http://www.mygiveandtake.com/");
    	Intent intent = new Intent( Intent.ACTION_VIEW, gntPage );
    	
    	// Verify it resolves
    	PackageManager packageManager = getPackageManager();
    	List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    	boolean isIntentSafe = activities.size() > 0;
    	if ( isIntentSafe )
    		startActivity( intent );
	}
	
	public void onClickLogOut( View view ) {

		// store valid login in and pw
		SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString( LoginActivity.USERNAME, "" );
		editor.putString( LoginActivity.PASSWORD_HASH, "");
		editor.putBoolean(LoginActivity.LOGGED_IN, false);
		editor.commit();
		
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	
	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// received new location from location service
			Log.d(TAG, "In BrowseCouponsActivity BroadcastReceiver...got new location");
			if( mBound )
			{
				TryToGetLocation();
			}
		}
		
	};
	
	private void CallSyncCoupons() 
	{
		if( IsConnected() ) {
			// go ahead and push coupon use back to server database
			db.open();
			Cursor c = db.getUsedCoupons();
			
			usedCoupons.clear();
			if( c.moveToFirst() ) // at least one row was returned
			{
				do
				{					
					boolean favorite;
					int fav = c.getInt(c.getColumnIndex(DBAdapter.KEY_FAVORITE));
					if( fav == 0 )
					{
						favorite = false;
					}
					else
					{
						favorite = true;
					}
					CouponObject newCoup = new CouponObject( "",//c.getString(c.getColumnIndex(KEY_COUPON_NAME)),
							"",//c.getString(c.getColumnIndex(KEY_EXP_DATE)),
							"",//c.getString(c.getColumnIndex(KEY_FILE_URL)),
							"",//c.getString(c.getColumnIndex(KEY_DETAILS_NAME)),
							favorite, 
							c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)),
							0, //distance,
							c.getInt( c.getColumnIndex(DBAdapter.KEY_DATE_USED)  ) ); // can simply put 0 for date_used since query only returned coups with this value
					usedCoupons.add(newCoup);
					
					//Log.d(TAG, "USED COUPON: event_id = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));
					//Log.d(TAG, "USED COUPON: date_used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));
				}while( c.moveToNext() );
				db.close();
				CouponObject[] usedCoups = new CouponObject[usedCoupons.size()];
				usedCoups = usedCoupons.toArray(usedCoups);
				new SyncUsedCoupons().execute(usedCoups);
			}
			else
			{
				db.close();
				Log.d(BrowseCouponsActivity.TAG, "No used coupons in database.");
			}
		}
		else
		{
			// sync later
		}
	}	
	private void CallSyncRecvdCoupons() 
	{
		if( IsConnected() ) {
			// go ahead and push coupon use back to server database
			db.open();
			Cursor c = db.getUnsyncedCoupons();
			
			unsyncedCoupons.clear();
			if( c.moveToFirst() ) // at least one row was returned
			{
				do
				{					
					CouponObject newCoup = new CouponObject( "",//c.getString(c.getColumnIndex(KEY_COUPON_NAME)),
							"",//c.getString(c.getColumnIndex(KEY_EXP_DATE)),
							"",//c.getString(c.getColumnIndex(KEY_FILE_URL)),
							"",//c.getString(c.getColumnIndex(KEY_DETAILS_NAME)),
							false, 
							c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)),
							0, //distance,
							0 ); // can simply put 0 for date_used since query only returned coups with this value
					unsyncedCoupons.add(newCoup);
					
					Log.d(TAG, "UNSYNCED COUPON: event_id = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));
				}while( c.moveToNext() );
				db.close();
				CouponObject[] unsyncedCoups = new CouponObject[unsyncedCoupons.size()];
				unsyncedCoups = unsyncedCoupons.toArray(unsyncedCoups);
				new SyncRecvdCoupons().execute(unsyncedCoups);
			}
			else
			{
				db.close();
				Log.d(BrowseCouponsActivity.TAG, "No unsynced coupons in database.");
			}
		}
		else
		{
			// sync later
		}
	}
	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}
	
	private class SyncUsedCoupons extends AsyncTask<CouponObject, Integer, Boolean>{
		
		@Override
		protected Boolean doInBackground( CouponObject...couponObjects ) {
			Log.d(TAG, "In SyncUsedCoupons: ");
			for( int i = 0; i < couponObjects.length; i++ )
			{
				Log.d(TAG, "Coupon event_id = "+couponObjects[i].getRowId()+", date_used = "+couponObjects[i].getDateUsed());
			}
			SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_FILE, 0);
			JSONArray coupons = new JSONArray();
			JSONObject user = new JSONObject();
			try {
				user.put("username", prefs.getString(LoginActivity.USERNAME, ""));
				for( int i = 0; i < couponObjects.length; i++ )
				{
					JSONObject coup = new JSONObject();
					coup.put("event_id", couponObjects[i].getRowId());
					coup.put("was_favorite", couponObjects[i].getFavorite());
					coup.put("date_used", couponObjects[i].getDateUsed());
					coupons.put(coup);
				}
				user.put("coupons", coupons);
			} catch (JSONException e1 ) {
				e1.printStackTrace();
			}
			
			try {
			// need to send json object "user" to server  
			HttpParams httpParams = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams,
	                10000);
	        HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost("http://166.78.251.32/gnt/update_used_coupons.php");
		        request.setHeader("json", user.toString());
		        //Log.d(TAG, "JSON Object = "+user.toString());
		        request.getParams().setParameter("jsonpost", user);
		        HttpResponse response = client.execute(request);
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream is = entity.getContent();
		            BufferedInputStream in;
					if( is != null )
					{
						Log.d(TAG, "got input stream from urlConnection");
						in = new BufferedInputStream(is);
					}
					else
					{
						Log.d(TAG, "urlConnection.getInputStream() failed");
						return null;
					}
			        Log.d(TAG, "got input stream");
		         
		            ByteArrayBuffer baf = new ByteArrayBuffer(500);
		            int current = 0;
		            while( (current = in.read()) != -1 ) {
		            	baf.append((byte) current);
		            	//Log.d(TAG, "byte = "+current);
		            }
		            in.close();
		            Log.d(TAG, "BAF: "+baf.toString());
		            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));
		            //JSONObject json = new JSONObject(new String(baf.toString()));

		            Log.d(TAG, "Created JSON return object");
//		            String temp = json.getString("error");
//		            Log.d(TAG, "error string returned = "+temp);
		            if( json.getString("error").equals("none") )
		            //if( json.getBoolean("success") )
		            {
		            	db.open();
		            	Log.d(TAG, "Successful update...should delete from local database.");
		            	for( int i = 0; i < couponObjects.length; i++ )
						{ 
							int rowId = couponObjects[i].getRowId();
							Log.d(TAG, "Trying to delete coupon with event_id = "+rowId);
							
							if( !db.deleteCoupon(rowId) )
							{
								Log.d(TAG, "Failure updating local database (deleting coupon that was used and synced)");
							}
							else
							{
								Log.d(TAG, "Succesfully updated local database: deleted coupon with event_id = "+rowId);
							}
						}
		            	db.close();
		            }
		            else
		            {
		            	Log.d(TAG, "Error updating database in server trying to sync used coupons.");
		            }
		            //String result = EntityUtils.toString(entity);
		            //Log.d(TAG, "Read from server:" + result);
		        }
			} catch (Throwable t) {
				Log.d(TAG,  "Error in the Http Request somewhere trying to sync used coupons.");
				t.printStackTrace();
			}
			return false;
		}

		
	}
	
	
	private class SyncRecvdCoupons extends AsyncTask<CouponObject, Integer, Boolean>{
		
		@Override
		protected Boolean doInBackground( CouponObject...couponObjects ) {
			Log.d(TAG, "In SyncRecvdCoupons: ");
			SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_FILE, 0);
			JSONArray coupons = new JSONArray();
			JSONObject user = new JSONObject();
			try {
				user.put("username", prefs.getString(LoginActivity.USERNAME, ""));
				for( int i = 0; i < couponObjects.length; i++ )
				{
					JSONObject coup = new JSONObject();
					coup.put("event_id", couponObjects[i].getRowId());
					coupons.put(coup);
				}
				user.put("coupons", coupons);
			} catch (JSONException e1 ) {
				e1.printStackTrace();
			}
			
			try {
			// need to send json object "user" to server  
			HttpParams httpParams = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams,
	                10000);
	        HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost("http://166.78.251.32/gnt/update_synced_coupons.php");
			//StringEntity se = new StringEntity(user.toString());
			//request.setEntity(se);
			//request.setEntity(new ByteArrayEntity(user.toString().getBytes("UTF8")));
		        request.setHeader("json", user.toString());
		        request.getParams().setParameter("jsonpost", user);
		        HttpResponse response = client.execute(request);
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream is = entity.getContent();
		            BufferedInputStream in;
					if( is != null )
					{
						Log.d(TAG, "got input stream from urlConnection");
						in = new BufferedInputStream(is);
					}
					else
					{
						Log.d(TAG, "urlConnection.getInputStream() failed");
						return null;
					}
			        Log.d(TAG, "got input stream");
		         
		            ByteArrayBuffer baf = new ByteArrayBuffer(500);
		            int current = 0;
		            while( (current = in.read()) != -1 ) {
		            	baf.append((byte) current);
		            	//Log.d(TAG, "byte = "+current);
		            }
		            in.close();
		            Log.d(TAG, "BAF: "+baf.toString());
		            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));

		            if( json.getString("error").equals("none") )
		            {
		            	db.open();
		            	Log.d(TAG, "Successful update...should delete from local database.");
		            	for( int i = 0; i < couponObjects.length; i++ )
						{
							int rowId = couponObjects[i].getRowId();
							Log.d(TAG, "Trying to delete coupon with event_id = "+rowId);
							
							if( !db.markCouponSynced(rowId) )
							{
								Log.d(TAG, "Failure updating local database (not able to tell server that coupon was recieved)");
							}
							else
							{
								Log.d(TAG, "Succesfully updated local database: marked received: coupon with event_id = "+rowId);
							}
						}
		            	db.close();
		            }
		            else
		            {
		            	Log.d(TAG, "Error updating database in server.");
		            }
		            //String result = EntityUtils.toString(entity);
		            //Log.d(TAG, "Read from server:" + result);
		        }
			} catch (Throwable t) {
				Log.d(TAG,  "Error in the Http Request somewhere.");
				t.printStackTrace();
			}
			return false;
		}	
	}
}
