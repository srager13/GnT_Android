package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

import com.flurry.android.FlurryAgent;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class BrowseCouponsActivity extends FragmentActivity {

    public static final String TAG = "browsecoupons";
    public static final String SIDEBAR_ANIM_TAG = "sidebaranimation";
    public static final String POSITION_TAG = "positiontag";
    public static final String LOCATION_TAG = "locationtag";

    private static final String PREFS_FILE = "GiveAndTakePrefs";
    public String[] myFiles;
    private ArrayList<String> navigationOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mViewPager;    
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    CouponListPagerAdapter mCouponListPagerAdapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.browse_coupons_w_sidebars);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
		  actionBar.setHomeButtonEnabled(true);
		}
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	    // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
	    mCouponListPagerAdapter =
                new CouponListPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.browseCouponsPager);
        mViewPager.setAdapter(mCouponListPagerAdapter);
	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction arg1) {
	            // show the given tab		
				 // When the tab is selected, switch to the
	            // corresponding page in the ViewPager.
				Log.d(BrowseCouponsActivity.TAG, "tab.getPosition in onTabSelected() (in BCA.java) = "+tab.getPosition());
				//Log.d(CouponListFragment.TAG, "tab.getPosition in onTabSelected() (in BCA.java) = "+tab.getPosition());
	            mViewPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction arg1) {
				// refresh data?
				Log.d(BrowseCouponsActivity.TAG, "in onTabReselected() (in BCA.java), tab.getPosition = "+tab.getPosition());
				//Log.d(CouponListFragment.TAG, "in onTabReselected() (in BCA.java), tab.getPosition = "+tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	            // hide the given tab				
			}
	    };

	    // Add 3 tabs, specifying the tab's text and TabListener
	    actionBar.addTab( actionBar.newTab().setText("Near Me").setTabListener(tabListener) );
	    actionBar.addTab( actionBar.newTab().setText("Ending Soon").setTabListener(tabListener) );
	    actionBar.addTab( actionBar.newTab().setText("A-Z").setTabListener(tabListener) );
	    
	    mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getActionBar().setSelectedNavigationItem(position);
	                }
	            });

		actionBar.show();
		
		navigationOptions = new ArrayList<String>();
		navigationOptions.add("Options:");
		navigationOptions.add("Get New Coupons");
		navigationOptions.add("Share on FB");
		navigationOptions.add("Settings");
		navigationOptions.add("G and T Info");
		navigationOptions.add("Logout");
		NavDrawerArrayAdapter adapter = new NavDrawerArrayAdapter(this, navigationOptions);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.browse_coupons_w_sidebars);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(adapter);
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.options_row, navigationOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new NavDrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.back_button,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.cancel  /* "close drawer" description */) {
            
/** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            	super.onDrawerClosed(view);
                getActionBar().setIcon(R.drawable.app_logo);
            }

/** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            	super.onDrawerOpened(drawerView);
                getActionBar().setIcon(R.drawable.back_button);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	
		// if a bundle is passed in, then this activity was launched by mapActivity infoWindo click
		//   so we need to start with coupons filtered
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		if( b != null )
		{
//			filterCompany = b.getString("companyName");
//			executeSearch = false;

			// change execute search button to clear
			// TODO?
//			EditText searchBox = (EditText) findViewById(R.id.coupon_search_text_box);
//			searchBox.setText(filterCompany);
//			Button searchButton = (Button)findViewById(R.id.exec_coup_search_button);
//			searchButton.setText("Clear");
//			executeSearch = false;
		}
	}
	
    
	@Override
	public void onStart() {
		Log.d(TAG, "In onStart");
		super.onStart();
		
		FlurryAgent.onStartSession(this, "J9WHX3VYHPRX8K756WTJ");
	}
	@Override
	public void onStop() {
		Log.d(TAG, "in onStop of BrowseCouponsActivity");
		super.onStop();
		
		FlurryAgent.onEndSession(this);
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

//	private class SearchWidgetClearListener implements SearchView.OnCloseListener {
//		public boolean onClose()
//		{
//			refreshCouponListFromDB();
//			drawCouponList();
//			return true;
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse_coupons_w_sidebars, menu);
		
		// TODO:: need to create the callback to handle a search action
//		MenuItem searchItem = menu.findItem(R.id.action_search);
	    //SearchView searchView = (SearchView) MenuItemCompat.getActionView(R.id.action_search);
	    // Configure the search info and add any event listeners

		// Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	    // Assumes current activity is the searchable activity
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    //searchView.setIconifiedByDefault(false); // Do not icon-ify the widget; expand it by default
	    searchView.setOnCloseListener(
	    		new SearchView.OnCloseListener() {
	    			@Override
	    			public boolean onClose()
	    			{
	    				Log.d(TAG, "In onClose of OnCloseListener() for the searchView Widget");
//	    				executeSearch = true;
	    				// TODO:  Pop up a separate fragment/activity with search results?
	    				return true;
	    			}
	    		});

	    MenuItem sBar = menu.findItem(R.id.action_search);
	    if( sBar != null )
	    {
	    	MenuItemCompat.setOnActionExpandListener(sBar,  
	    			
	    		new OnActionExpandListener() {
					@Override
					public boolean onMenuItemActionCollapse(MenuItem item) {
						//
						Log.d(TAG, "Search view collapsed.  Clear search.");
//						executeSearch = true;
						return true;
					}
		
					@Override
					public boolean onMenuItemActionExpand(MenuItem item) {
						//
						Log.d(TAG, "Search view expanded.");
						return true;
					}
	    	} );
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
        
		// Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        
	    switch (item.getItemId()) {
	    // action with ID open left drawer was selected
	    case android.R.id.home:
	    //case R.id.action_open_left_drawer:
		    Toast.makeText(this, "Launch Sidebar", Toast.LENGTH_SHORT).show();
			Log.d(SIDEBAR_ANIM_TAG, "in onLaunchSidebarButtonClick");
	        mDrawerLayout = (DrawerLayout) findViewById(R.id.browse_coupons_w_sidebars);
	        mDrawerLayout.openDrawer(Gravity.LEFT);
	      break;
	    // action with ID search was selected
		case R.id.action_search:
		  //Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show();
			// nothing to do here. also, listener set above can also handle opening the search view
		      break;
	    default:
	      break;
	    }

	    return true;
	  }
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "In onPause()");
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
    	setIntent(intent);
    	
    	Log.d(TAG, "in onNewIntent");
    	
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    	     String query = intent.getStringExtra(SearchManager.QUERY);

    	    Log.d(TAG, "Searched for "+query);
    	    	
//    	    if( executeSearch )
//	  		{
//    	    	Log.d(TAG, "executeSearch was true");
//	  			filterCompany = query;
//	  		
//	  			// change execute search button to clear
//	  			//searchButton.setText("Clear");
//	  			executeSearch = false; // not sure if we need this any more - need to figure out how to "clear" searches
//	  		
//	  			// TODO: how to implement search?
//	  		}
//	  		else // clearing previously done search
//	  		{
//
//    	    	Log.d(TAG, "executeSearch was false");
//	  			// change execute search button to clear
//	  			//searchButton.setText("Go");
//	  			executeSearch = true;			
//	  			// clear text in search box 
//	  			//searchBox.setText("");
//	  
//	  		}
    	    

    	   }
	}
	
	private class NavDrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	Log.d(TAG, "Clicked on item number "+position+" in the navigation drawer");
	    	Log.d(TAG, "Text on item = "+navigationOptions.get(position).toString());
	        //selectItem(position);
	    	switch (position)
	    	{
	    		case 0:
	    			break;
	    		case 1:
	    			break;
	    		case 2:
					Log.d(BrowseCouponsActivity.TAG, "Should go to search coupons activity.");				
					onGetNewCouponsButtonClicked(null);
	    			break;
	    		case 3:
	    			break;
	    		case 4:
	    			break;
	    		case 5:
	    			break;
	    		default:
	    			Log.d(BrowseCouponsActivity.TAG, "Error: navigation drawer item listener hit default case");
			}
	    }
	}
	
	
	public void onGetNewCouponsButtonClicked(View view) { // warning: do not use the "view" paramater being passed in. It is given as null when option in nav drawer chosen
		Intent intent = new Intent( this, SearchNewCouponsActivity.class );
    	startActivity( intent );	
	}
	
	

	
	
	public void displayCannotFindLocToast() {
		Toast.makeText(getApplicationContext(), "Cannot find current location.", Toast.LENGTH_LONG).show();
	}
	
	public void onLaunchSidebarButtonClick( View view ) {
		Log.d(SIDEBAR_ANIM_TAG, "in onLaunchSidebarButtonClick");
        mDrawerLayout = (DrawerLayout) findViewById(R.id.browse_coupons_w_sidebars);
        mDrawerLayout.openDrawer(Gravity.LEFT);
	}
	

	public void onLaunchCategoriesClick( View view ) {
		Log.d(SIDEBAR_ANIM_TAG, "in onLaunchCategoriesClick");
		mDrawerLayout = (DrawerLayout) findViewById(R.id.browse_coupons_w_sidebars);
        //mDrawerLayout.openDrawer(R.id.right_drawer);
		mDrawerLayout.openDrawer(Gravity.RIGHT);
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

		// forget valid login in and pw
		SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString( LoginActivity.USERNAME, "" );
		editor.putString( LoginActivity.PASSWORD_HASH, "");
		editor.putBoolean(LoginActivity.LOGGED_IN, false);
		editor.putBoolean(LoginActivity.STAY_LOGGED, false);
		editor.commit();
		
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}
	
    public static class CouponListPagerAdapter extends FragmentStatePagerAdapter {
        public CouponListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return CouponListFragment.newInstance(position);
        }
    }

    public static class CouponListFragment extends ListFragment {

        public static final String TAG = "couponlistfragment";
        int mNum;
        private ArrayList<CouponObject> coupons;
        private ArrayList<CouponObject> usedCoupons;
        private ArrayList<CouponObject> unsyncedCoupons;
        
   	 	private DBAdapter db;
   	 	private Random r;
   		LocationServiceBinder binder;
   		
   		private LocationService locationService;
   		//private LocationListener locationListener;
   		private Location currentLocation;
   		private boolean useLocations = false;

    	private int use_coup_request_Code = 1;

   		// if true, button to right of search bar executes search, otherwise, it clears the search
   		//   button text(background) will be set to proper value
   		private boolean executeSearch;
   		private String filterCompany;

   	 	private static SortByValueEnum sortByValue = SortByValueEnum.SORT_BY_NEAREST;
        
        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static CouponListFragment newInstance(int num) {
        	CouponListFragment f = new CouponListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            Log.d(TAG, "in onCreate() of CouponListFragment");
            
            db = new DBAdapter(getActivity());
            coupons = new ArrayList<CouponObject>();        
            usedCoupons = new ArrayList<CouponObject>();
    		unsyncedCoupons = new ArrayList<CouponObject>();

            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
	   	     switch( mNum )
	   	     {
	   		     case 0:
	   		    	 sortByValue = SortByValueEnum.SORT_BY_NEAREST;
	   			     Log.d("couponlistfragment", "Setting sortByValue to SORT_BY_NEAREST (in onCreate)");
	   		    	 break;
	   		     case 1:
	   		    	 sortByValue = SortByValueEnum.SORT_BY_EXP_DATE;
	   			     Log.d("couponlistfragment", "Setting sortByValue to SORT_BY_EXP_DATE (in onCreate)");
	   		    	 break;
	   		     case 2:
	   		    	 sortByValue = SortByValueEnum.SORT_BY_NAME;
	   			     Log.d("couponlistfragment", "Setting sortByValue to SORT_BY_NAME (in onCreate)");
	   		    	 break;
	   	     }
            r = new Random();
            
            refreshCouponListFromDB();
        }

    	@Override
    	public void onStart() {
    		Log.d(TAG, "In onStart");
    		super.onStart();
    		
//    		Intent locService = new Intent(getBaseContext(), LocationService.class);
//    		bindService(locService, mConnection, Context.BIND_AUTO_CREATE);
    		
    	}
    	
    	@Override
    	public void onStop() {
    		Log.d(TAG, "in onStop of BrowseCouponsActivity");
    		super.onStop();
 //   		unbindService(mConnection);
    		
    	}
    	    	
    	@Override
    	public void onResume() {
    		super.onResume();		
    		
//    		if( locationReceiver != null )
//    		{
//    			IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION);
//    			registerReceiver(locationReceiver, intentFilter);
//    		}
    		
    		refreshActivity();
    	}
        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.coupon_list, container, false);

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
            ArrayList<String> names = new ArrayList<String>();
			for( int i = 0; i < coupons.size(); i++ )
			{
				//if( !names.contains(coupons.get(i).getCouponName()) )
				//{
					names.add(coupons.get(i).getCouponName());
				//}
			}
			CouponListArrayAdapter adapter = new CouponListArrayAdapter(getActivity(), coupons, names, true);
			setListAdapter(adapter);
            
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
        public void drawCouponList() {
    		
    		Log.d(TAG, "in drawCouponList()");
    		
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

            ArrayList<String> names = new ArrayList<String>();
			for( int i = 0; i < coupons.size(); i++ )
			{
				//if( !names.contains(coupons.get(i).getCouponName()) )
				//{
					names.add(coupons.get(i).getCouponName());
				//}
			}
			CouponListArrayAdapter adapter = new CouponListArrayAdapter(getActivity(), coupons, names, true);
			setListAdapter(adapter);
			
//    		// TODO: Fix search suggestions and implement searching function
//    		ArrayList<String> names = new ArrayList<String>();
//    		for( int i = 0; i < coupons.size(); i++ )
//    		{
//    			//if( !names.contains(coupons.get(i).getCouponName()) )
//    			//{
//    				names.add(coupons.get(i).getCouponName());
//    			//}
//    		}
//    		Context context = getActivity();
//    		CouponListArrayAdapter adapter = new CouponListArrayAdapter(context, coupons, names, true);
//    		TextView noCoupsMessage = (TextView)getView().findViewById(R.id.noCouponsMessage);
//    		Button getNewCoupsButton = (Button)getView().findViewById(R.id.getNewCouponsButton);

    		if( coupons.isEmpty() )
    		{
    			Log.d(TAG, "No coupons to draw");
//    			ListView list = (ListView)getView().findViewById(R.id.couponList);
//    			list.setAdapter(null);
//    			noCoupsMessage.setText(R.string.no_coupons_message);
//    			noCoupsMessage.bringToFront();
//    			noCoupsMessage.setVisibility(View.VISIBLE);
//    			getNewCoupsButton.setVisibility(View.VISIBLE);
//    			getNewCoupsButton.setClickable(true);
//    			getNewCoupsButton.bringToFront();
    			return;
    		}
    		// TODO:: What to do when no coupons?
//    		else
//    		{
//    			noCoupsMessage.setVisibility(View.GONE);
//    			getNewCoupsButton.setVisibility(View.GONE);
//    		}
    		
//    		ListView list = (ListView)getView().findViewById(R.id.couponList);
//    		list.setAdapter(adapter);
//    		list.setOnItemClickListener(new OnItemClickListener() {
//    		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//    		    	String imageName = coupons.get(position).getCouponPic();
//    		    	String companyName = coupons.get(position).getCouponName();
//    		    	String couponDetail = coupons.get(position).getCouponDetail();
//
//    		        Intent coupDisplayIntent = new Intent(v.getContext(), CouponDisplayActivity.class);
//    		        Bundle b = new Bundle();
//    		        b.putString("couponPic", imageName);
//    		        b.putInt("position", position);
//    		        b.putString("companyName", companyName);
//    		        b.putString("couponDetail", couponDetail);
//    		        b.putBoolean("usable", true);
//    		        coupDisplayIntent.putExtras(b);
//    		        
//    		        // Capture author info & user status
//    		        Map<String, String> couponParams = new HashMap<String, String>();
//    		 
//    		        couponParams.put("companyName", companyName); 
//    		        couponParams.put("couponDetail", couponDetail); 
//    		        FlurryAgent.logEvent("couponViewed", couponParams);
//
//    		        startActivityForResult( coupDisplayIntent, use_coup_request_Code );
//    		    }
//    		});
    		
//    		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.coupon_search_text_box );
//    		ArrayAdapter<String> searchSuggAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
//    		editBox.setAdapter(searchSuggAdapter);
    		
    		return;
    	}

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("CouponList", "Item clicked: " + id);
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
	        
	        // Capture author info & user status
	        Map<String, String> couponParams = new HashMap<String, String>();
	 
	        couponParams.put("companyName", companyName); 
	        couponParams.put("couponDetail", couponDetail); 
	        FlurryAgent.logEvent("couponViewed", couponParams);

	        startActivityForResult( coupDisplayIntent, use_coup_request_Code );

        }

    	public void onActivityResult( int requestCode, int resultCode, Intent data ) 
    	{
    		Log.d(TAG, "Entered OnActivityResult():");
    		Log.d(TAG, "requestCode = "+requestCode+", resultCode = "+resultCode);
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
    			else
    			{
    				Log.d(TAG, "resultCode != RESULT_OK");
    			}
    		}
    		refreshCouponListFromDB();
    		drawCouponList();
    		
    	}
    	
    	public void useCouponConfirmed( int position ) {
    		
    		if( position == -1 )
    		{
    			Log.d( TAG, "ERROR: Got a confirmation of using coupon in position -1 in useCouponConfirmed() in BrowseCouponsActivity.java ");
    			return;
    		}
    		// user clicked "yes" in alert dialog asking if they want to use the coupon
    		//   we need to remove the coupon from the list

    		Log.d(TAG, "in useCouponConfirmed()...position = "+position);
    		
    		// mark coupon used in database...this function updates the DATE_USED field
    		db.open();
    		if( !db.markCouponUsed(coupons.get(position).getRowId()) )
    		{
    			Log.d(TAG, "markCouponUsed returned an error");
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
		
    	public double getDistance( double loc1, double loc2 )
    	{
    		return 0.0;
    	}
    	
    	private void refreshActivity() {
    		TryToGetLocation();
    		
    		if( !executeSearch && filterCompany != null )
    		{
    			if( !refreshCouponListFromDBFilteredByCompany(filterCompany) )
    			{
    				// TODO: report a toast error - none found?
    				refreshCouponListFromDB();
    			}
    		}
    		else
    		{
    			refreshCouponListFromDB();
    		}
    		
    		drawCouponList();
    		CallSyncCoupons();
    		CallSyncRecvdCoupons();
    	}
        
        public void refreshCouponListFromDB() {
			Log.d(TAG, "in refreshCouponListFromDB()");
			//TryToGetLocation();
			db.open();
			Cursor c = db.getAllUnusedCoupons();

			coupons.clear();

			if( c.moveToFirst() ) // at least one row was returned
			{
				do
				{
					Log.d(TAG, "rowId = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));	
					Log.d(TAG, "company name = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)));
					Log.d(TAG, "coupon details = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)));
					
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
					Log.d("couponlistfragment", "favorites = " + fav );
					Log.d("couponlistfragment", "Exp Date = "+c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)));
					Log.d("couponlistfragment", "Logo = "+c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)));
					Log.d("couponlistfragment", "Date Used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));

					//double distance = getDistance( 1.0, 1.0 );
					double distance = 2000.0*r.nextDouble();
					if( false )//useLocations )
					{
						Log.d(BrowseCouponsActivity.LOCATION_TAG, "Finding location for coupon (useLocations = true)");
					
					//	float results[] = {(float) 0.0, (float) 0.0, (float) 0.0};
					//	Log.d(TAG, "Current latitude: " + currentLocation.getLatitude() + ", Current longitude: " + currentLocation.getLongitude() );
						
//						Location.distanceBetween( currentLocation.getLatitude(), currentLocation.getLongitude(), 
//								c.getFloat(c.getColumnIndex(DBAdapter.KEY_LATITUDE)), c.getFloat(c.getColumnIndex(DBAdapter.KEY_LONGITUDE)), results );
//						distance = results[0];
						distance = 1.0;
					}
					else
					{
						//Log.d(CouponListFragment.TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
						distance = -1.0;
						distance = 2000.0*r.nextDouble();
					}					
					Log.d(BrowseCouponsActivity.LOCATION_TAG, "Distance = "+distance);
					
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
				Log.d("couponlistfragment", "No coupons in database.");
			}
			db.close();		
		}
    	// returns true if successful...returns false if unable to find company that it should be filtering by
    	public boolean refreshCouponListFromDBFilteredByCompany( String CompanyName ) {
    		Log.d(TAG, "in refreshCouponListFromDBFilteredByCompany(" + CompanyName + ")");
    		TryToGetLocation();
    		db.open();
    		Cursor c = db.getAllUnusedCouponsOfCompany( CompanyName );

    		coupons.clear();

    		if( c.moveToFirst() ) // at least one row was returned
    		{
    			do
    			{
    				Log.d(TAG, "rowId = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_EVENTID)));	
    				Log.d(TAG, "company name = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COMPANY_NAME)));
    				Log.d(TAG, "coupon details = "+c.getString(c.getColumnIndex(DBAdapter.KEY_COUPON_DETAILS)));
    				
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
    				Log.d(TAG, "favorites = " + fav );
    				Log.d(TAG, "Exp Date = "+c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)));
    				Log.d(TAG, "Logo = "+c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)));
    				Log.d(TAG, "Date Used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));

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
    					Log.d(TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
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
    			Log.d(TAG, "No coupons in database.");
    			return false;
    		}
    		db.close();	
    		return true;
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
    				Log.d(TAG, "No used coupons in database.");
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
    				Log.d(TAG, "No unsynced coupons in database.");
    			}
    		}
    		else
    		{
    			// sync later
    		}
    	}
    	
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
    	private boolean IsConnected() {

    		ConnectivityManager conMgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
    		
    		return networkInfo != null && networkInfo.isConnected();
    		
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
    	

    	private class SyncUsedCoupons extends AsyncTask<CouponObject, Integer, Boolean>{
    		
    		@Override
    		protected Boolean doInBackground( CouponObject...couponObjects ) {
    			Log.d(TAG, "In SyncUsedCoupons: ");
    			for( int i = 0; i < couponObjects.length; i++ )
    			{
    				Log.d(TAG, "Coupon event_id = "+couponObjects[i].getRowId()+", date_used = "+couponObjects[i].getDateUsed());
    			}
    			SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_FILE, 0);
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
//    		            String temp = json.getString("error");
//    		            Log.d(TAG, "error string returned = "+temp);
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
    			SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_FILE, 0);
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
    
   

}
