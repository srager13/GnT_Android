package org.scottrager.appfunding;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;


public class SplashScreenActivity extends Activity {

	public static final String PREFS_FILE = "GiveAndTakePrefs";
	public static final String TAG = "splashscreen";
	
	public static final String USERNAME = "Username";
	public static final String PASSWORD_HASH = "PasswordHash";
	public static final String SUCCESS = "success";
	
	private Thread mSplashThread;
    private DBAdapter db = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splashscreen);
		
		// necessary call to avoid problems with HTTP requests in older OS versions ( < FroYo)
		disableConnectionReuseIfNecessary();

		SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
	    SharedPreferences.Editor editor = prefs.edit();

		db = new DBAdapter(this);
		db.open();
		Cursor c = db.getAllUnusedCoupons();
		if( c.moveToFirst() ) // at least one row was returned
		{
			Log.d(TAG, "cursor returned at least one row.");
			editor.putInt("numCoups", 1);
		}
		else
		{
			Log.d(TAG, "cursor was empty.");
			editor.putInt("numCoups", 0);
		}
 		db.close();
 		
    	final int numCoups = prefs.getInt("numCoups", 0);
    	Log.d( TAG, "numCoups = "+numCoups);

    	//TODO::This should be changed, right?
	    editor.putBoolean("firstOpen", false);
	    editor.commit();

        final SplashScreenActivity sPlashScreen = this;
        
        // The thread to wait for splash screen events
        mSplashThread =  new Thread(){
            @Override
            public void run(){

                try {
                    synchronized(this){
                        // Wait given period of time or exit on touch
                    	wait(2000);
                    	Log.d(TAG, "calling SyncData().execute()");
                        //new SyncData().execute( "huh" );
                    	SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
                	    boolean isLoggedIn = prefs.getBoolean(LoginActivity.LOGGED_IN, false);
                	    if( !isLoggedIn )
                	    {
                    		Log.d(TAG, "Not Valid Login");
                    		Intent intent = new Intent(sPlashScreen, LoginActivity.class);
                    		startActivity(intent);	
                    		finish();
                    		
                	    } // Always go to main activity (numCoups test not working for some reason
                	    else //if( numCoups > 0 )
                            {	
                	    		Log.d(TAG, "Login was valid and numCoups > 0");
                            	// Run next activity
                            	Intent intent = new Intent();
                            	intent.setClass(sPlashScreen, MainActivity.class);
                            	startActivity(intent);
                            }
                           /* else
                            {
                	    		Log.d(TAG, "Login was valid and numCoups <= 0");
                            	Intent intent = new Intent();
                            	intent.setClass(sPlashScreen, SearchNewCouponsActivity.class);
                            	startActivity(intent);
                            }*/

                            finish();
                    }
                }
                catch(InterruptedException ex){                    
                }
            }
        };
        
        mSplashThread.start();
        
	}
	
	private void disableConnectionReuseIfNecessary() {
	    // HTTP connection reuse which was buggy pre-froyo
	    if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
	        System.setProperty("http.keepAlive", "false");
	    }
	}

    
    /**
     * Processes splash screen touch events
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
            synchronized(mSplashThread){
                mSplashThread.notifyAll();
            }
        }
        return true;
    } 

    private class SyncData extends AsyncTask<String, Void, Object> {
         protected Object doInBackground(String... args) {
             Log.d(TAG, "Background thread starting");
             
             DBAdapter db = new DBAdapter(getBaseContext());
             db.open();

     		Cursor c = db.getAllUnusedCoupons();
     		if( c.moveToFirst() ) // at least one row was returned
     		{
     			int numCoups = 0;
     			do
     			{
     				Log.d(BrowseCouponsActivity.TAG, "Company: "+c.getString(0));
     				numCoups++;
     			}while( c.moveToNext() );

     			SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
     	    	Log.d( TAG, "numCoups = "+numCoups);

     		    SharedPreferences.Editor editor = prefs.edit();
     		    editor.putInt("numCoups", numCoups);
     		    editor.commit();
     		}
     		
     		db.close();
             // This is where you would do all the work of downloading your data
             //SystemClock.sleep(5000);

             return "replace this with your data object";
         }

         protected void onPostExecute(Object result) {
             // Pass the result data back to the main activity
             //SplashScreenActivity.this.data = result;

             //if (SplashScreenActivity.this.pd != null) {
            //	 SplashScreenActivity.this.pd.dismiss();
           //  }
        	 Log.d(TAG, "onPostExecute() in SyncData Task");
         }
    }
}
