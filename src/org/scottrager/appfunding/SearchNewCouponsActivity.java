package org.scottrager.appfunding;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
		searchSuggestions.add("Ferndale Hockey");
		searchSuggestions.add("Ferndale Football");
		searchSuggestions.add("Ferndale Baseball");
		searchSuggestions.add("Pittsburgh Charity X");
		searchSuggestions.add("Pittsburgh Charity Y");
		searchSuggestions.add("Pittsburgh Charity Z");
		searchSuggestions.add("Girl Scouts Troop 32");
		
		AutoCompleteTextView editBox = (AutoCompleteTextView) findViewById( R.id.search_text_box );
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, searchSuggestions );
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
}
