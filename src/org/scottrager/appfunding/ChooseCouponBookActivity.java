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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseCouponBookActivity extends FragmentActivity {

	private final String TAG = "choosecouponbook";
	
	static final int BOOK_PURCHASED = 1;
	
	private ArrayList<String> couponBookNames;
	private ArrayList<Integer> couponBookPrices;
	private ArrayList<String> couponBookValues;
	private ArrayList<Integer> couponBookNumbers;
	private int selectedBookPosition = -1;
	
	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_coupon_book);

		Log.d(TAG, "in onCreate() of ChooseCouponBookActivity");

		couponBookNames = new ArrayList<String>();
		couponBookPrices = new ArrayList<Integer>();
		couponBookValues = new ArrayList<String>();
		couponBookNumbers = new ArrayList<Integer>();

		String chosenGroup = "";
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		if( b != null )
		{
		   chosenGroup = b.getString("chosenGroup");
		   if( !IsConnected() )
		   {
				Toast.makeText(getApplicationContext(), "No network connection detected.  Please try again later.", Toast.LENGTH_LONG).show();
		   }
		   new GetCouponBooks().execute(chosenGroup);
		}
		else
		{
			new GetCouponBooks().execute("None");
		}

		Log.d(TAG, "setting chosen group text");
		TextView group = (TextView) findViewById( R.id.chosen_group );
		group.setText(chosenGroup);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_choose_coupon_book, menu);
		return true;
	}
	
	public void updateCouponBookList(Boolean result) {
		if( !result )
		{
			Toast.makeText(this, "Error retrieving coupon books.\nPlease try again.", Toast.LENGTH_LONG).show();
		}
		Log.d(TAG, "getting adapter");
		CouponBookArrayAdapter adapter = new CouponBookArrayAdapter(this, couponBookNames, 
				couponBookPrices, couponBookValues, couponBookNumbers);
		ListView list = (ListView)findViewById(R.id.coupon_book_choices);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		    	selectedBookPosition = position;
		    	
		    }
		});
	}
	
	public void BuyBook( View view ) {
		Log.d(TAG, "in BuyBook()");
		
		int position = selectedBookPosition;
		if( position == -1 )
		{
			Toast.makeText(this, "Please select a coupon book!", Toast.LENGTH_SHORT ).show();
			return;
		}
		int coupon_book_id = couponBookNumbers.get(position);
		
		Log.d(TAG, "trying to buy book "+coupon_book_id+", with cost = "+couponBookPrices.get(position));
		
		Intent intent = new Intent( this, EnterSellerIdDialog.class );
		intent.putExtra("couponBookId", coupon_book_id);
		intent.putExtra("couponBookCost", couponBookPrices.get(position));
		
		startActivityForResult(intent, BOOK_PURCHASED);

		return;
		
	}
	
	public void PreviewBook( View view ) {
		Log.d(TAG, "in PreviewBook()");
		
//		ListView list = (ListView) findViewById(R.id.coupon_book_choices);
		int position = selectedBookPosition;// list.getSelectedItemPosition();
		if( position == -1 )
		{
			Toast.makeText(this, "Please select a coupon book!", Toast.LENGTH_SHORT ).show();
			return;
		}
		Log.d(TAG, "position of selected item in list = "+position);
		int coupon_book_id = couponBookNumbers.get(position);
		Log.d(TAG, "coupon book id of selected item in list = "+coupon_book_id);
		Intent intent = new Intent( this, PreviewCouponsActivity.class );
		intent.putExtra("couponBookId", coupon_book_id);
		Log.d(TAG, "trying to preview book "+coupon_book_id);
		intent.putExtra("couponBookValue", couponBookValues.get(position));
		intent.putExtra("couponBookCost", couponBookPrices.get(position));
		startActivityForResult(intent, BOOK_PURCHASED);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "In onActivityResult() of ChooseCouponBookActivity.");
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

	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}

	private class GetCouponBooks extends AsyncTask<String, Integer, Boolean>{
		
		@Override
		protected Boolean doInBackground( String...org_name ) {
			Log.d(TAG, "In GetCouponBooks: org_name = "+org_name[0]);
			
//			JSONObject json_char_name = new JSONObject();
//			try {
//				json_char_name.put("charity_name", org_name[0]);
//				
//			} catch (JSONException e1 ) {
//				e1.printStackTrace();
//			}
			
			try {
			// need to send json object "user" to server  
			HttpParams httpParams = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams,
	                10000);
	        HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_coupon_books.php");
			   
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		    nameValuePairs.add(new BasicNameValuePair("charity_name", org_name[0]));
		    request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
		        HttpResponse response = client.execute(request);
		        HttpEntity entity = response.getEntity();
		        
		        String result = EntityUtils.toString(entity);
		        Log.d(TAG, "result = "+result);
		        JSONObject json = new JSONObject( result );
	            JSONArray couponBooks = null;

	            if( json.getString("error").equals("none") )
	            {
	            	couponBooks = json.getJSONArray("coupon_books");
	            	   
    	    	    // looping through All Coupon Books
    	    	    for(int i = 0; i < couponBooks.length(); i++){
    	    	        JSONObject c = couponBooks.getJSONObject(i);
    	    	         
    	    	        // Storing each json item in variable
    	    	        int coupon_book_id = c.getInt("coupon_book_id");
    	    	        int cost = c.getInt("cost");
    	    	        int value = c.getInt("value");
    	    	        Log.d(TAG, "Coupon Book Id = "+coupon_book_id);
    	    	        Log.d(TAG, "Coupon Book Cost = "+cost);
    	    	        Log.d(TAG, "Coupon Book Value = "+value);  
    	    	        
    	    	        couponBookNumbers.add(coupon_book_id);
    	    			couponBookNames.add("Coupon Book "+coupon_book_id);
    	    			couponBookPrices.add(cost);
    	    			couponBookValues.add(" - Over $"+value+" in Total Value!");
    	    	    }
	            }
	            else
	            {
	            	Log.d(TAG, "Error getting coupon books from server.");
	            	return false;
	            }
		        
			} catch (Throwable t) {
				Log.d(TAG,  "Error in the Http Request somewhere.");
				Toast.makeText(getApplicationContext(), "Network connection error.  Please try again later.", Toast.LENGTH_LONG).show();
				t.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPreExecute()
		{
            dialog= new ProgressDialog(ChooseCouponBookActivity.this);
            dialog.setIndeterminate(true);
//            dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_dialog_anim));
            dialog.setCancelable(false);
            dialog.setMessage("Getting Coupon Books...");
            dialog.show();
		}
	     
		@Override
        protected void onPostExecute(Boolean result) {
        	dialog.dismiss();
        	
        	if( result )
        	{
        		updateCouponBookList(result);
        	}
        }
	
	}

}
