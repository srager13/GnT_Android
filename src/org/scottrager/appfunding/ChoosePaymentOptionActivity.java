package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class ChoosePaymentOptionActivity extends Activity {

	public final String TAG = "choosepaymentoption";
	private DBAdapter db;
	private int coupon_book_id;
	private int coupon_book_cost;
	private String seller_id;
	
	static final int VALID_CASH_CODE = 1;
	static final int VALID_CREDIT_CARD = 1;
	
	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_payment_option);

		Intent i = getIntent();
		Bundle b = i.getExtras();
		coupon_book_id = b.getInt("couponBookId");
		coupon_book_cost = b.getInt("couponBookCost");
		seller_id = b.getString("sellerId");
		db = new DBAdapter(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_choose_payment_option, menu);
		return true;
	}
	
	
	public void onChooseCreditCardPay( View view ) {
		Log.d(TAG, "chose to pay with credit card");

		if( !IsConnected() )
		{
			DisplayNoConnectionToast();
			return;
		}

		Intent intent = new Intent( this, EnterCreditCardInfoDialog.class );
		intent.putExtra("couponBookCost", coupon_book_cost);
		intent.putExtra("sellerId", seller_id);

		startActivityForResult(intent, VALID_CREDIT_CARD);
	}
	
	public void onChooseCashPay( View view ) {
		Log.d(TAG, "chose to pay with cash");
		Log.d(TAG, "coupon book cost = "+coupon_book_cost);
		Log.d(TAG, "seller id = "+seller_id);
		
		if( !IsConnected() )
		{
			DisplayNoConnectionToast();
			return;
		}

		Intent intent = new Intent( this, EnterCashCodeDialog.class );
		intent.putExtra("couponBookCost", coupon_book_cost);
		intent.putExtra("sellerId", seller_id);
		
		startActivityForResult(intent, VALID_CASH_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == VALID_CASH_CODE || requestCode == VALID_CREDIT_CARD) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	            // The user entered a valid cash code
	    		new addCoupons().execute();	        	
	        }
	    }
	}
	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}
	private void DisplayNoConnectionToast() {
		Toast.makeText(getApplicationContext(), "No network connection detected.\nPlease try again later.", Toast.LENGTH_LONG).show();
	}
	private class addCoupons extends AsyncTask<String, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {


		SharedPreferences prefs = getSharedPreferences( MainActivity.PREFS_FILE, 0);
		String username = prefs.getString(MainActivity.USERNAME, "");
		db.open();
    
		Log.d(TAG, "Trying to get coupons for book = "+coupon_book_id+", user="+username);

		try {
		// need to send json object "user" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,
                10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_coupons_for_book.php");
		   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	       nameValuePairs.add(new BasicNameValuePair("username", username));
	       nameValuePairs.add(new BasicNameValuePair("couponBookId", String.valueOf(coupon_book_id)));
	       request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

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
	            	JSONArray coupons = null;
		    		try {
		    			Log.d(TAG, "No error returned from json call");
		    			coupons = json.getJSONArray("coupons");

			    	    // looping through All Coupons
			    	    for(int i = 0; i < coupons.length(); i++){
			    	        JSONObject c = coupons.getJSONObject(i);
			    	        Log.d(TAG, "Trying to insert coupon: event_id="+c.getInt("event_id")
			    	        		+", company_id="+c.getInt("company_id")+", coupon_id="+c.getInt("coupon_id"));

			    	        // first we make sure we have the info for all of the coupons in the book
			    	        //   if not, we try to fill that info into the local database from the web server
			    	        int company_id = c.getInt("company_id");
			    	        int location_id = c.getInt("location_id");
			    	        int coupon_id = c.getInt("coupon_id");
			    	        if( !db.companyInDatabase( company_id ) )
			    	        {
		    	        		if( !getCompany( company_id ) )
		    	        		{
		    	        			Log.d(TAG, "getCompany() returned false...Error retreiving JSON");
		    	        		}	
			    	        }
			    	        if( !db.locationInDatabase( company_id, location_id ) )
		    	        		if( !getLocation( company_id, location_id ) )
		    	        		{
		    	        			Log.d(TAG, "getLocation() returned false...Error retreiving JSON");
		    	        		}
			    	        if( !db.couponInDatabase(coupon_id) )
			    	        	if( !getCoupon( coupon_id ) )
			    	        	{
		    	        			Log.d(TAG, "getCoupon() returned false...Error retreiving JSON");
		    	        		}
			    	        
			    	        //	public int insertCouponEvent( int event_id, int coupon_id )
			    	        db.insertCouponEvent( c.getInt("event_id"), c.getInt("coupon_id"));
//			    	        db.insertCouponEvent(c.getInt("event_id"), c.getString("coupon_name"), c.getString("coupon_details"), 
//			    	        		c.getString("exp_date"), c.getString("file_url"), 40.780, -77.855);

		    		}
			    	    db.close();		    		
		    		} catch (JSONException e) {
		    			e.printStackTrace();
	    	    	return false;
		    		}
	            }
	            else
	            {
	            	Log.d(TAG, "JSON request returned the following error: "+json.getString("error"));
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
	        else
	        {
	        	Log.d(TAG, "Entity was null");
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere.");
			t.printStackTrace();
			return false;
		}

 		goToBrowseCoupons();
 		return true;
    	}


		@Override
		protected void onPreExecute()
		{
            dialog= new ProgressDialog(ChoosePaymentOptionActivity.this);
            dialog.setIndeterminate(true);
//            dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_dialog_anim));
            dialog.setCancelable(false);
            dialog.setMessage("Downloading Coupons...");
            dialog.show();
		}
	     
    	@Override
		protected void onPostExecute( Boolean result )
    	{
    		dialog.dismiss();
    		if( !result )
    		{
    			Toast.makeText(getApplicationContext(), "An unknown error has occurred.\nPlease try again.", Toast.LENGTH_LONG).show();
    		}
    	}
	}
	
    private void goToBrowseCoupons() {
    	Intent intent = new Intent( this, BrowseCouponsActivity.class );
    	startActivity(intent);
    	
    	this.setResult(RESULT_OK);
    	finish();
    }
    
    private boolean getCoupon( int coupon_id )
    {
    	//TODO::Need to make sure arguments are safe
		Log.d(TAG, "Trying to get coupon with id = "+coupon_id);
    	JSONObject info = new JSONObject();
		try {
			info.put("couponId", coupon_id);
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
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_coupon_with_id.php");
	        request.setHeader("json", info.toString());
	        request.getParams().setParameter("jsonpost", info);
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
					return false;
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
		    		try {
		    			Log.d(TAG, "No error returned from json call");
		    			 db.insertCoupon( coupon_id, json.getInt("company_id"), json.getString("coupon_details"), json.getString("exp_date"), json.getInt("favorite"));
			    	   // db.close();		    		
		    		} catch (JSONException e) {
		    			e.printStackTrace();
	    	    	return false;
		    		}
	            }
	            else
	            {
	            	Log.d(TAG, "JSON request returned the following error: "+json.getString("error"));
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
	        else
	        {
	        	Log.d(TAG, "Entity was null");
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere.");
			t.printStackTrace();
			return false;
		}
		
    	return true;
    }
    private boolean getCompany( int company_id )
    {
    	//TODO::Need to make sure arguments are safe
		Log.d(TAG, "Trying to get company with id = "+company_id);
    	JSONObject info = new JSONObject();
		try {
			info.put("companyId", company_id);
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
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_company_with_id.php");
	        request.setHeader("json", info.toString());
	        request.getParams().setParameter("jsonpost", info);
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
					return false;
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
		    		try {

		    	        Log.d(TAG, "Trying to insert company: company_id="+company_id+
		    	        		"company_name="+json.getString("company_name") );

		    	        //	public int insertCompany( int company_id, String company_name, String file_url)
		    	        db.insertCompany( company_id, json.getString("company_name"), json.getString("file_url"));
			    	   // db.close();		    		
		    		} catch (JSONException e) {
		    			e.printStackTrace();
	    	    	return false;
		    		}
	            }
	            else
	            {
	            	Log.d(TAG, "JSON request returned the following error: "+json.getString("error"));
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
	        else
	        {
	        	Log.d(TAG, "Entity was null");
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere.");
			t.printStackTrace();
			return false;
		}
		
    	return true;
    }
    private boolean getLocation( int company_id, int location_id )
    {
    	//TODO::Need to make sure arguments are safe
		Log.d(TAG, "Trying to get location with id = "+location_id);
    	JSONObject info = new JSONObject();
		try {
			info.put("locationId", location_id);
			info.put("companyId", company_id);
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
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_location_with_id.php");
	        request.setHeader("json", info.toString());
	        request.getParams().setParameter("jsonpost", info);
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
					return false;
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
		    		try {
		    	        Log.d(TAG, "Trying to insert location: location_id="+location_id );

		    	        //public int insertLocation( int company_id, int location_id, String addr_line_1, 
		    	        //                            String addr_line_2, double latitude, double longitude )
		    	       if( db.insertLocation( company_id, location_id, json.getString("addr_line_1"), 
		    	        		json.getString("addr_line_2"), json.getDouble("latitude"), json.getDouble("longitude") ) == -1 )
		    	       {
		    	    	   Log.d(TAG, "Error inserting location into database.");
		    	       }
			    	   // db.close();		    		
		    		} catch (JSONException e) {
		    			e.printStackTrace();
	    	    	return false;
		    		}
	            }
	            else
	            {
	            	Log.d(TAG, "JSON request returned the following error: "+json.getString("error"));
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
	        else
	        {
	        	Log.d(TAG, "Entity was null");
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere.");
			t.printStackTrace();
			return false;
		}
		
    	return true;
    }

}
