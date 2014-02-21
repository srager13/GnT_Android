package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
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

		if( IsConnected() ) {
		new ValidateLogin().execute();
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
        startActivity(intent);
        finish();
	}
	
	private boolean IsConnected() {

		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
		
	}

    private class ValidateLogin extends AsyncTask<URL, Integer, Long> {
    	

    	@Override
    	protected Long doInBackground(URL... params) {
    	//TODO::Need to make sure arguments are safe
    	String username = ((EditText)findViewById(R.id.UserNameBox)).getText().toString();
    	String passwordHash = ((EditText)findViewById(R.id.PasswordBox)).getText().toString();
    	
    	JSONObject loginInfo = new JSONObject();
    	try {
        	loginInfo.put("user", username);
			loginInfo.put("pwHash", passwordHash);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Log.d(TAG, "Username = "+username);
    	Log.d(TAG, "Password = "+passwordHash);
		try {
		// need to send json object "loginInfo" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,
                10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/validate_login.php");
	        request.setHeader("json", loginInfo.toString());
	        request.getParams().setParameter("jsonpost", loginInfo);
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
	            	Log.d(TAG, "byte = "+current);
	            }
	            in.close();
	            //Log.d(TAG, "BAF: "+baf.toString());
	            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));

	            if( json.getBoolean(SUCCESS) )
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
    			Toast.makeText(getApplicationContext(), "Username/Password Not Recognized.\nPlease Try Again.", Toast.LENGTH_LONG).show();
    		}
    	}
    }
}
