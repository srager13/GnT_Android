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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;



public class PreviewCouponsActivity extends FragmentActivity {
	
    public static final String TAG = "previewcoupons";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_PW_HASH = "pw_hash";
	public static final String KEY_COUPONID = "coupon_id";
	public static final String KEY_COUPON_NAME = "coupon_name";
	public static final String KEY_DETAILS_NAME = "coupon_details";
	public static final String KEY_EXP_DATE = "exp_date";
	public static final String KEY_FILE_URL = "file_url";
	public static final String KEY_DATE_USED = "date_used";
	public static final String KEY_FAVORITE = "favorite";
    public static boolean CREATE_FILES_MANUALLY = false;
    public String[] myFiles;
	static final int BOOK_PURCHASED = 1;

    private ArrayList<CouponObject> coupons;

	private int coupon_book_id = -1;
	private int coupon_book_value = 0;
	private int coupon_book_cost = 0;
	
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_preview_coupons);

		coupons = new ArrayList<CouponObject>();		
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		if( b != null )
		{
			coupon_book_id = b.getInt("couponBookId");
			coupon_book_value = b.getInt("couponBookValue");
			coupon_book_cost = b.getInt("couponBookCost");
			Log.d(TAG, "should get coupons for coupon book "+coupon_book_id);
			
			if( IsConnected() )
				new getCoupons().execute(coupon_book_id);
			else
				DisplayNoConnectionToast();
		}
		else
		{
			Log.d(TAG, "ERROR: bundle from intent = null");
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_preview_coupons, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in onResume()");
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
		CouponListArrayAdapter adapter = new CouponListArrayAdapter(this, coupons, names, false);

		if( coupons.isEmpty() )
		{
			Toast.makeText(this, "No Coupons to show.\nPlease try again.", Toast.LENGTH_LONG).show();
			return;
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
		        b.putBoolean("usable", false);
		        coupDisplayIntent.putExtras(b);

		        startActivity( coupDisplayIntent );
		    }
		});
		
		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.coupon_search_text_box );
		ArrayAdapter<String> searchSuggAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		editBox.setAdapter(searchSuggAdapter);
		
		return;
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "In onActivityResult() of PreviewCouponsActivity.");
	    // Check which request we're responding to
	    if (requestCode == BOOK_PURCHASED) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	// user bought a coupon book, so we can kill this activity
	        	Log.d(TAG, "\t book was purchased, so finishing");
	        	this.setResult(RESULT_OK);
	        	finish();
	        }
	    }
	}

	public void sortByNearest( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_ending_soon_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_az_button);
		b2.setSelected(false);
		
		
	}
	
	public void sortByEndingSoon( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_nearest_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_az_button);
		b2.setSelected(false);
	}
	
	public void sortAlphabetically( View view ) {
		view.setSelected(true);
		Button b1 = (Button) findViewById(R.id.sort_by_ending_soon_button);
		b1.setSelected(false);
		Button b2 = (Button) findViewById(R.id.sort_by_nearest_button);
		b2.setSelected(false);
	}
	
	public void goToMap( View view ) {
		Intent intent = new Intent( this, MapActivity.class );
		startActivity(intent);
	}
	
	
	public double getDistance( double loc1, double loc2 )
	{
		return 0.0;
	}
	
	public void onClickBuyButton ( View view ) {
		
		Log.d(TAG, "trying to buy book "+coupon_book_id);
		
		Intent intent = new Intent( this, EnterSellerIdDialog.class );
		intent.putExtra("couponBookId", coupon_book_id);
		intent.putExtra("coupondBookValue", coupon_book_value);
		intent.putExtra("couponBookCost", coupon_book_cost);
		
		// TODO:: start activity for result and finish this activity if sale completes?
		startActivityForResult(intent, BOOK_PURCHASED);

		return;
		
	}
	
	
	private class getCoupons extends AsyncTask<Integer, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(Integer... params) {
		
    	coupons.clear();
		//TODO::Need to make sure arguments are safe
		Log.d(TAG, "Trying to get coupons for book = "+coupon_book_id+" to preview");
		JSONObject info = new JSONObject();
		try
		{
			info.put("couponBookId", coupon_book_id);

		} catch (JSONException e1 ) {
			e1.printStackTrace();
		}
		

		SharedPreferences prefs = getSharedPreferences( MainActivity.PREFS_FILE, 0);
		String username = prefs.getString(MainActivity.USERNAME, "");
		
		try {
		// need to send json object "user" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,
                10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_coupons_for_book_to_preview.php");
		   
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("couponBookId", String.valueOf(coupon_book_id)));
	    nameValuePairs.add(new BasicNameValuePair("username", username));
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
		        //Log.d(TAG, "got input stream");
	         
	            ByteArrayBuffer baf = new ByteArrayBuffer(500);
	            int current = 0;
	            while( (current = in.read()) != -1 ) {
	            	baf.append((byte) current);
	            	//Log.d(TAG, "byte = "+current);
	            }
	            in.close();
	            Log.d(TAG, "BAF: "+baf.toString());
	            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));

	            Log.d(TAG, "Created JSON return object");
	            if( json.getString("error").equals("none") )
	            {
	            	Log.d(TAG, "Successfully got coupons");
	        		JSONArray coups = null;
	    	    	try {
	    	    		String error = json.getString("error");
	    	    		if( error.equals("none"))
	    	    		{
	    	    			Log.d(TAG, "No error returned from json call");
	    	    		}
	    	    		else
	    	    		{
	    	    			Log.d(TAG, "Error value returned from json call to get coupons: "+error);
	    	    		}
	    	    		//network
	    	    		coups = json.getJSONArray("coupons");

	    	    	    // looping through All Coupons
	    	    	    for(int i = 0; i < coups.length(); i++){
	    	    	        JSONObject c = coups.getJSONObject(i);
	    	    	        Log.d(TAG, "Trying to insert coupon: "+
	    	    	        		"coupon_name="+c.getString("coupon_name") );

	    	    	        //public CouponObject( String name, String exp_date, String pic, String detail, 
	    	    	        //						boolean fav, int rowid, double dist, int date_used ) 
	    	    	        // don't have event_id for any of these because they are not bought yet
	    	    	        CouponObject newCoupon = new CouponObject( c.getString("coupon_name"), c.getString("exp_date"),
	    	    	        		 									c.getString("file_url"), c.getString("coupon_details"),
	    	    	        		 									false, 0, 0.0, 0 );
	    	    	        coupons.add(newCoupon);
//	    	    	        db.insertCoupon( 0, c.getString("coupon_name"), c.getString("coupon_details"), 
//	    	    	        		c.getString("exp_date"), c.getString("file_url"), 40.780, -77.855);

	    	    	    }
	    	    	} catch (JSONException e) {
	        	    e.printStackTrace();
	        	    return false;
	    	    	}
	            }
	            else
	            {
	            	Log.d(TAG, "Error updating database in server trying to get coupons for book to preview.");
	            	return false;
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere trying to sync used coupons.");
			t.printStackTrace();
		}

 		return true;
    	}

		@Override
		protected void onPreExecute()
		{
            dialog= new ProgressDialog(PreviewCouponsActivity.this);
            dialog.setIndeterminate(true);
//            dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_dialog_anim));
            dialog.setCancelable(false);
            dialog.setMessage("Getting Coupons...");
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
    		drawCouponList();
    	}
	}
}
