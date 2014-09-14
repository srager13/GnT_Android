package org.scottrager.appfunding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.flurry.android.FlurryAgent;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;



//Instances of this class are fragments representing a single
//object in our collection.
public class CouponListFragment extends Fragment {
	 public static final String ARG_OBJECT = "object";
	 public static final String TAG = "browsecoupons";
	
	 private DBAdapter db;
	 private ArrayList<CouponObject> coupons;
	 private boolean useLocations = false;
	 private static SortByValueEnum sortByValue = SortByValueEnum.SORT_BY_NEAREST;

	private int use_coup_request_Code = 1;
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater,
	         ViewGroup container, Bundle savedInstanceState) {

	     coupons = new ArrayList<CouponObject>();
	     db = new DBAdapter(getActivity());
	     
	     // The last two arguments ensure LayoutParams are inflated
	     // properly.
	     View rootView = inflater.inflate( R.layout.coupon_list, container, false );
	     Bundle args = getArguments();
	     switch( args.getInt(ARG_OBJECT) )
	     {
		     case 1:
		    	 sortByValue = SortByValueEnum.SORT_BY_NEAREST;
		    	 break;
		     case 2:
		    	 sortByValue = SortByValueEnum.SORT_BY_EXP_DATE;
		    	 break;
		     case 3:
		    	 sortByValue = SortByValueEnum.SORT_BY_NEAREST;
		    	 break;
	     }
	     
	     
	     return rootView;
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
						Log.d(BrowseCouponsActivity.TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
						distance = -1.0;
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
				Log.d(BrowseCouponsActivity.TAG, "No coupons in database.");
			}
			db.close();		
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

			// TODO: Fix search suggestions and implement searching function
			ArrayList<String> names = new ArrayList<String>();
			for( int i = 0; i < coupons.size(); i++ )
			{
				//if( !names.contains(coupons.get(i).getCouponName()) )
				//{
					names.add(coupons.get(i).getCouponName());
				//}
			}
			CouponListArrayAdapter adapter = new CouponListArrayAdapter(getActivity(), coupons, names, true);
			TextView noCoupsMessage = (TextView)getActivity().findViewById(R.id.noCouponsMessage);
			Button getNewCoupsButton = (Button)getActivity().findViewById(R.id.getNewCouponsButton);

			if( coupons.isEmpty() )
			{
				Log.d(TAG, "No coupons to draw");
				ListView list = (ListView)getActivity().findViewById(R.id.couponList);
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
			
			ListView list = (ListView)getActivity().findViewById(R.id.couponList);
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
			        
			        // Capture author info & user status
			        Map<String, String> couponParams = new HashMap<String, String>();
			 
			        couponParams.put("companyName", companyName); 
			        couponParams.put("couponDetail", couponDetail); 
			        FlurryAgent.logEvent("couponViewed", couponParams);

			        startActivityForResult( coupDisplayIntent, use_coup_request_Code );
			    }
			});
			
//			AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.coupon_search_text_box );
//			ArrayAdapter<String> searchSuggAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
//			editBox.setAdapter(searchSuggAdapter);
			
			return;
		}

		public double getDistance( double loc1, double loc2 )
		{
			return 0.0;
		}
	 
}