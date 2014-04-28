package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private static final String PREFS_FILE = "GiveAndTakePrefs";
	public static final String HAS_ACCOUNT = "HasAccount";
	public static final String LOGGED_IN = "LoggedIn";
	public static final String USERNAME = "Username";
	public static final String PASSWORD_HASH = "PasswordHash";
	public static final String SUCCESS = "success";
	
	public static final String TAG = "loginactivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	public void onClickSubmit ( View view ) {

		Log.d(TAG, "Clicked Login");
		if( IsConnected() ) {
	    	String username = ((EditText)findViewById(R.id.UserNameBox)).getText().toString();
	    	String passwordHash = ((EditText)findViewById(R.id.PasswordBox)).getText().toString();
		new ValidateLogin().execute(username, passwordHash);
		}
		else
		{
			Toast.makeText(getApplicationContext(), "No Internet Access Currently Available.  Please Try Again.", Toast.LENGTH_LONG).show();
		}
	}

	public void onClickRegisterNew ( View view ) {
    	String username = ((EditText)findViewById(R.id.UserNameBox)).getText().toString();
    	String passwordHash = ((EditText)findViewById(R.id.PasswordBox)).getText().toString();
    	Intent intent = new Intent(view.getContext(), RegisterNewUserActivity.class);
        Bundle b = new Bundle();
        b.putString("username", username);
        b.putString("passwordHash", passwordHash);
        intent.putExtras(b);
        startActivity(intent); //TODO:  start activity for result and only finish if registration is continued
        finish();
	}
	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}

    private class ValidateLogin extends AsyncTask<String, Integer, Long> {
    	

    	@Override
    	protected Long doInBackground(String... params) {
    		
    		Log.d(TAG, "New ValidateLogin task");
    	//TODO::Need to make sure arguments are safe
    	String username = params[0];
    	String passwordHash = params[1];
    	Log.d(TAG, "Username = "+username);
    	Log.d(TAG, "Password = "+passwordHash);
		try {
		// need to send json object "loginInfo" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/validate_login.php");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	       nameValuePairs.add(new BasicNameValuePair("username", username));
	       nameValuePairs.add(new BasicNameValuePair("pwHash", passwordHash));
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
	            int byteCount = 0;
	            while( (current = in.read()) != -1 && ++byteCount < 500 ) {
	            	baf.append((byte) current);
	            	Log.d(TAG, "byte = "+(char)current);
	            }
	            in.close();
	            Log.d(TAG, "BAF: "+baf.toByteArray().toString());
	            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));

	            //if( json.getBoolean(SUCCESS) )
	            if( json.getString("success").equals("true") )
	            {
	            	return (long) 1;
	            }
	            else
	            {
	            	Log.d(TAG, "Error validating login.");
	            	return null;
	            }
	            //String result = EntityUtils.toString(entity);
	            //Log.d(TAG, "Read from server:" + result);
	        }
		} catch (Throwable t) {
			Log.d(TAG,  "Error in the Http Request somewhere.");
			t.printStackTrace();
		}
    	return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Long result) {
    		if( result != null )
    		{
    			Log.d(TAG, "Valid username and pw...saving and continuing");
    			// store valid login in and pw
    			SharedPreferences prefs = getSharedPreferences( PREFS_FILE, 0);
    			SharedPreferences.Editor editor = prefs.edit();
    			editor.putString( USERNAME, ((EditText)findViewById(R.id.UserNameBox)).getText().toString() );
    			editor.putString( PASSWORD_HASH, ((EditText)findViewById(R.id.PasswordBox)).getText().toString() );
    			editor.putBoolean(HAS_ACCOUNT, true);
    			editor.putBoolean(LOGGED_IN, true);
    			editor.commit();
    			
    		// launch into main activity and finish this one
            	Intent intent = new Intent();
            	intent.setClass(getBaseContext(), MainActivity.class);
            	startActivity(intent);
            	finish();
    		}
    		else
    		{
    			Log.d(TAG, "Invalid username and pw");
    			Toast.makeText(getApplicationContext(), "Username/Password Not Recognized.\nPlease Try Again.", Toast.LENGTH_LONG).show();
    		}
    	}
    }
}
