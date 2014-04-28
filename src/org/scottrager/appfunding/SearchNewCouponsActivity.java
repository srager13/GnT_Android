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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

public class SearchNewCouponsActivity extends Activity {

    public static final String TAG = "searchnewcoupons";
//    private ProgressDialog progressDialog;
    //private ArrayList<String> searchResults;
    private ArrayList<String> searchSuggestions;
    ProgressDialog dialog;
    
    public final boolean USE_TEST_URL = true;
    static final int BOOK_PURCHASED = 1;
    public final String TEST_URL = "http://www.scottrager.org/app_search_results.html";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_new_coupons);
		
		searchSuggestions = new ArrayList<String>();
		new getCharities().execute();

		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.search_text_box );
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchNewCouponsActivity.this, android.R.layout.simple_list_item_1, searchSuggestions );
		editBox.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_search_new_coupons, menu);
		return true;
	}
	
	public void executeNoGroup( View view ) {
		Log.d(TAG, "Choosing to continue without selecting any groups");		

//		Intent chooseBookIntent = new Intent();
//		chooseBookIntent.setClass(this, ChooseCouponBookActivity.class);
//		Intent chooseBookIntent = new Intent( "org.scottrager.appfunding.ChooseCouponBookActivity" );
		Intent chooseBookIntent = new Intent( this, ChooseCouponBookActivity.class);
        Bundle b = new Bundle();
        b.putString("chosenGroup", "None");
        chooseBookIntent.putExtras(b);

		Log.d(TAG, "calling startActivity( chooseBookIntent, BOOK_PURCHASED )");

        if( IsConnected() )
        {
        	startActivityForResult( chooseBookIntent, BOOK_PURCHASED );
        }
        else
        {
        	DisplayNoConnectionToast();
        }
	}
	
	public void startExecuteSearch( View view ) {
		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById(R.id.search_text_box);
		String group = editBox.getText().toString();
		Log.d(TAG, "Choosing group: "+group);
		
		Intent chooseBookIntent = new Intent( this, ChooseCouponBookActivity.class );
        Bundle b = new Bundle();
        b.putString("chosenGroup", group);
        chooseBookIntent.putExtras(b);

        if( IsConnected() )
        {
        	startActivityForResult( chooseBookIntent, BOOK_PURCHASED );
        }
        else
        {
        	DisplayNoConnectionToast();
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "In onActivityResult() of SearchNewCouponsActivity.");
	    // Check which request we're responding to
	    if (requestCode == BOOK_PURCHASED) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	// user bought a coupon book, so we can kill this activity
	        	Log.d(TAG, "\t book was purchased, so finishing");
	        	finish();
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
	
	private class getCharities extends AsyncTask<String, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {
    
		Log.d(TAG, "Trying to get charities for search suggestions");

		try {  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,
                10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/get_all_charities.php");

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
	         
	            ByteArrayBuffer baf = new ByteArrayBuffer(1000);
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
	            	JSONArray charities = null;
		    		try {
		    			Log.d(TAG, "No error returned from json call");
		    			charities = json.getJSONArray("charities");

			    	    // looping through All Charities
			    	    for(int i = 0; i < charities.length(); i++){
			    	        JSONObject c = charities.getJSONObject(i);
			    	        Log.d(TAG, "charity: id="+c.getInt("charity_id")
			    	        		+", name="+c.getString("name"));
			    	        
			    	        searchSuggestions.add(c.getString("name"));
			    	    }	    		
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


		@Override
		protected void onPreExecute()
		{
            dialog= new ProgressDialog(SearchNewCouponsActivity.this);
            dialog.setIndeterminate(true);
//            dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_dialog_anim));
            dialog.setCancelable(false);
            dialog.setMessage("Getting Charities...");
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
	
	
}
