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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class EnterCashCodeDialog extends Activity {

	private int coupon_book_id;
	private String CashCode;
	private int coupon_book_cost;
	private String seller_id;
	
	public final String TAG = "entercashcode";

	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter_cash_code_dialog);
		
		CashCode = "";
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		coupon_book_cost = b.getInt("couponBookCost");
		seller_id = b.getString("sellerId");

		Log.d(TAG, "in EnterCashCodeDialog:");
		Log.d(TAG, "coupon book cost = "+coupon_book_cost);
		Log.d(TAG, "seller id = "+seller_id);
	}
	
	public void onSubmit( View view ) {
		EditText et = (EditText)findViewById(R.id.cash_code_input_box);
		CashCode = new String(et.getText().toString());
		
		new validateCashCode().execute(CashCode, seller_id, Integer.toString(coupon_book_cost) );
	}
	
	public void onCancel( View view ) {
		finish();
	}
	
	
	private class validateCashCode extends AsyncTask<String, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {
    		
		if( params.length < 3 )
		{
			Toast.makeText(getApplicationContext(), "Code not found.\nPlease check and try again.", Toast.LENGTH_LONG).show();
			Log.d(TAG, "paramater missing from passing into validateCashCode AsyncTask...exiting");
			return false;
		}
    	
    	String CashCode = params[0];
    	String sellerId = params[1];
    	String coupon_book_cost = params[2];
		Log.d(TAG, "Checking for existence of CashCode = "+CashCode+", sellerId = "+sellerId);

		try {
		// need to send json object "CashCode" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/validate_cash_code.php");
		   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	       nameValuePairs.add(new BasicNameValuePair("seller_id", sellerId));
	       nameValuePairs.add(new BasicNameValuePair("cash_code", CashCode));
	       nameValuePairs.add(new BasicNameValuePair("book_cost", coupon_book_cost));
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

	            if( !json.getString("success").equals("true") )
	            {
	            	Log.d(TAG, "Cash Code not validated");    		
	            	return false;
	            }
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
            dialog= new ProgressDialog(EnterCashCodeDialog.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage("Checking code...");
            dialog.show();
		}
	     
    	@Override
		protected void onPostExecute( Boolean result )
    	{
    		dialog.dismiss();
    		if( !result )
    		{
    			Toast.makeText(getApplicationContext(), "Code not valid.\nPlease check and try again.", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			Intent resultIntent = new Intent();
    			//resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, tabIndexValue);
    			setResult(Activity.RESULT_OK, resultIntent);
    			finish();
    		}
    	}
	}
}
