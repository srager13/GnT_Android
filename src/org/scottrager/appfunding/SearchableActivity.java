package org.scottrager.appfunding;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class SearchableActivity extends Activity {

public static final String TAG = "searchactivity";
	
	public SearchableActivity( ) {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_search_results);
	    
	    Log.d(TAG, "in onCreate of SearchableActivity()");
	    
	    handleIntent(getIntent());
	}

	@Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }
}