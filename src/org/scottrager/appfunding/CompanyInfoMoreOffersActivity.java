package org.scottrager.appfunding;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CompanyInfoMoreOffersActivity extends Activity {

	public static final String TAG = "companyinfomoreoffers";
	private String companyName = "";
//	int position = -1;
    private ArrayList<CouponObject> coupons;
    boolean useLocations;
	private DBAdapter db;
	private int use_coup_request_Code = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_company_info_more_offers);

		coupons = new ArrayList<CouponObject>();
		useLocations = false;
		db = new DBAdapter(this);
		
		   String imageName = "";
		   companyName = "";
		   Intent intent = getIntent();
		   Bundle b = intent.getExtras();
		   if( b != null )
		   {
			   imageName = b.getString("couponPic");
			   companyName = b.getString("companyName");
//			   couponDetail = b.getString("couponDetail");
//			   position = b.getInt("position");
//			   usable = b.getBoolean("usable");
		   }
		   //Log.d(TAG, "position = "+position+");
		   Log.d(TAG, "company = "+companyName+", imageName = "+imageName);

		   int logoId = getResources().getIdentifier("drawable/" + imageName, "drawable", getPackageName());

		   ImageView imageView = (ImageView)findViewById(R.id.company_info_logo);
		   imageView.setImageDrawable( getResources().getDrawable(logoId) );
		    
		   TextView compName = (TextView)findViewById(R.id.company_info_name_title);
		   compName.setText( companyName );
		   
		   TextView compAddr = (TextView)findViewById(R.id.company_info_address_1);
		   compAddr.setText("123 Somewhere Lane");
		   TextView compAddr_2 = (TextView)findViewById(R.id.company_info_address_2);
		   compAddr_2.setText("Pittsburgh, PA 15237");
		   
	}

	@Override
	public void onResume() {
		super.onResume();

	   refreshCouponListFromDBFilteredByCompany( companyName );
	   drawCouponList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_company_info_more_offers,
				menu);
		return true;
	}
	
	public void refreshCouponListFromDBFilteredByCompany( String CompanyName ) {
		Log.d(TAG, "in refreshCouponListFromDB()");
		//TryToGetLocation();
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
				Log.d(TAG, "favorites = " + fav );
				Log.d(TAG, "Exp Date = "+c.getString(c.getColumnIndex(DBAdapter.KEY_EXP_DATE)));
				Log.d(TAG, "Logo = "+c.getString(c.getColumnIndex(DBAdapter.KEY_FILE_URL)));
				Log.d(TAG, "Date Used = "+c.getInt(c.getColumnIndex(DBAdapter.KEY_DATE_USED)));

				double distance = -1.0; // getDistance( 1.0, 1.0 );
				if( useLocations )
				{
/*					Log.d(LOCATION_TAG, "Finding location for coupon (useLocations = true)");
				
					float results[] = {(float) 0.0, (float) 0.0, (float) 0.0};
					Log.d(TAG, "Current latitude: " + currentLocation.getLatitude() + ", Current longitude: " + currentLocation.getLongitude() );
					
					Location.distanceBetween( currentLocation.getLatitude(), currentLocation.getLongitude(), 
							c.getFloat(c.getColumnIndex(DBAdapter.KEY_LATITUDE)), c.getFloat(c.getColumnIndex(DBAdapter.KEY_LONGITUDE)), results );
					distance = results[0];
*/
				}
				else
				{
					Log.d(TAG, "Setting distance for coupon to 0.0 (useLocations = false)");
					distance = -1.0;
				}					
//				Log.d(LOCATION_TAG, "Distance = "+distance);
				
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
		
		ListView list = (ListView)findViewById(R.id.comp_info_coupon_list);
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
		
		return;
	}
	
	public void onActivityResult( int requestCode, int resultCode, Intent data ) 
	{
		Log.d(TAG, "Entered OnActivityResult():");
		//super.onActivityResult( requestCode,  resultCode,  data );
		if( requestCode == use_coup_request_Code )
		{
			if( resultCode == RESULT_OK )
			{
				int pos = data.getIntExtra("position", 0);
				int rowId = -1;
				Log.d(TAG, "OnActivityResult() in BrowseCouponsActivity...position = "+pos);
				if( pos != -1 )
				{
					rowId = coupons.get(pos).getRowId();
					Log.d(TAG, "OnActivityResult() in BrowseCouponsActivity...rowId = "+rowId);
					useCouponConfirmed( pos );
				}
			}
		}
		//refreshCouponListFromDB();
		//drawCouponList();
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
			Log.d(BrowseCouponsActivity.TAG, "markCouponUsed returned an error");
			db.close();
		}
		else
		{
			db.close();
			//CallSyncCoupons();
		}
		
		coupons.remove(position);
		
		drawCouponList();
	}

	public void goToMap( View view ) {
		Log.d(TAG, "Trying to launch map.");
		Intent intent = new Intent( this, MapActivity.class );
		startActivity(intent);
	}
	
	public void goToNavigate( View view ) {
		Log.d(TAG, "Trying to launch map.");
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+"111 Plaza Drive, Johnstown, PA 15905"));
		startActivity(intent);
	}
	  
	public void clickBackButton( View view ) {
		finish();
	}
}
