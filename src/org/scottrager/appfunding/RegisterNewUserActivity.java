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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterNewUserActivity extends Activity {

	private static final String ERROR = "error";
	public static final String TAG = "registernewuser";
	private static final String PREFS_FILE = "GiveAndTakePrefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register_new_user);
		
		String username = "";
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		if( b != null )
		{
			username = b.getString("username");
			((EditText)findViewById(R.id.UserNameBox)).setText(username);
			//position = b.getInt("position");  
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_register_new_user, menu);
		return true;
	}


	public void onClickRegisterNew ( View view ) {
		EditText pwBox = (EditText)findViewById(R.id.PasswordBox);
		EditText pwBox2 = (EditText)findViewById(R.id.PasswordConfirmBox);
		String username = ((EditText)findViewById(R.id.UserNameBox)).getText().toString();
    	String password = pwBox.getText().toString();
    	String password2 = pwBox2.getText().toString();
    	String firstname = ((EditText)findViewById(R.id.FirstNameBox)).getText().toString();
    	String lastname = ((EditText)findViewById(R.id.LastNameBox)).getText().toString();
    	String email = ((EditText)findViewById(R.id.EmailBox)).getText().toString();
    	
    	//TODO::Check all boxes for possible missing/wrong values
    	if( !password.equals(password2) )
    	{
    		Toast.makeText(getApplicationContext(), "Passwords do not match.\nPlease Try Again", Toast.LENGTH_LONG).show();
    		pwBox.setText("");
    		pwBox2.setText("");
    	}
    	else if( !IsConnected() )
    	{
    		DisplayNoConnectionToast();
    	}
    	else
    	{
    		boolean stayLogged = false;
    		final CheckBox checkBox = (CheckBox) findViewById(R.id.StayLoggedCheckbox);
            if (checkBox.isChecked()) {
                stayLogged = true;
            }
    		new RegisterNewUser(username, password, firstname, lastname, email, stayLogged).execute();
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
    private class RegisterNewUser extends AsyncTask<URL, Integer, Long> {
    	String un;
    	String pw;
    	String fn;
    	String ln;
    	String email;
    	boolean stayLogged;
    	
    	public RegisterNewUser( String un, String pw, String fn, String ln, String email, boolean sL )
    	{
    		super();
    		this.un = new String(un);
    		this.pw = new String(pw);
    		this.fn = new String(fn);
    		this.ln = new String(ln);
    		this.email = new String(email);
    		stayLogged = sL;
    	}
    	@Override
    	protected Long doInBackground(URL... params) {
    	//TODO::Need to make sure arguments are safe
    	Log.d( LoginActivity.TAG, "Attempting to register: username="+un+", password="+pw+", firstname="+fn+", lastname="+ln+", email="+email);
		
		try {
			// need to send json object "user" to server  
			HttpParams httpParams = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams,
	                10000);
	        HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost("http://166.78.251.32/gnt/register_new_customer.php");

		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			       nameValuePairs.add(new BasicNameValuePair("username", un));
			       nameValuePairs.add(new BasicNameValuePair("password", pw));
			       nameValuePairs.add(new BasicNameValuePair("firstName", fn));
			       nameValuePairs.add(new BasicNameValuePair("lastName", ln));
			       nameValuePairs.add(new BasicNameValuePair("email", email));
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
		            	Log.d(TAG, "byte = "+current);
		            }
		            in.close();
		            Log.d(TAG, "BAF: "+baf.toString());
		            JSONObject json = new JSONObject(new String(baf.toByteArray(), "utf-8"));

		            String ErrorValue;
	            	try{
	            		ErrorValue = json.getString(ERROR);
	            		if( ErrorValue.equals("none") )
	            		{
	            			Log.d(TAG, "Successful registration.");
	            			return (long) 1;
	            		}
	            		else
	            		{
	            			Log.d(TAG, "Error value from register_new_user.php = "+ErrorValue);
	            		}
	            	} catch (JSONException e) {
	            		e.printStackTrace();
	            	}
		            
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
    			editor.putString( LoginActivity.USERNAME, ((EditText)findViewById(R.id.UserNameBox)).getText().toString() );
    			editor.putString( LoginActivity.PASSWORD_HASH, ((EditText)findViewById(R.id.PasswordBox)).getText().toString() );
    			editor.putBoolean(LoginActivity.LOGGED_IN, true);
    			editor.putBoolean(LoginActivity.STAY_LOGGED, stayLogged);
    			editor.commit();
    			
    			// launch into main activity and finish this one
            	Intent intent = new Intent();
            	intent.setClass(getBaseContext(), MainActivity.class);
            	startActivity(intent);
            	finish();	
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Unknown Failure.\nPlease Try Again.", Toast.LENGTH_LONG).show();
    		}
    	}
    }
}
