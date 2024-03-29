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

public class EnterSellerIdDialog extends Activity {

	private int coupon_book_id;
	private String sellerId;
	private int coupon_book_cost;
	
	static final int BOOK_PURCHASED = 1;
	public final String TAG = "entersellerid";

	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter_seller_id_dialog);
		
		sellerId = "";
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		coupon_book_id = b.getInt("couponBookId");
		coupon_book_cost = b.getInt("couponBookCost");
		
		Log.d(TAG, "in EnterSellerId: ");
		Log.d(TAG, "coupon book Cost = "+coupon_book_cost);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_enter_seller_id_dialog, menu);
		return true;
	}
	
	public void onSubmit( View view ) {
		EditText et = (EditText)findViewById(R.id.seller_code_input_box);
		sellerId = new String(et.getText().toString());
		
		new validateSellerId().execute(sellerId);
	}
	
	public void onNoSellerId( View view ) {
		sellerId = new String("NoSeller");

		goToChoosePayment();
	}
	
	public void onCancel( View view ) {
		finish();
	}
	
	private void goToChoosePayment() {
		Intent intent = new Intent( this, ChoosePaymentOptionActivity.class );
		intent.putExtra("sellerId", sellerId);
		intent.putExtra("couponBookId", coupon_book_id);
		intent.putExtra("couponBookCost", coupon_book_cost);
		startActivityForResult(intent, BOOK_PURCHASED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("choosecouponbook", "in EnterSellerIdDialog: onActivityResult.  resultCode = "+resultCode);
        if (resultCode == RESULT_OK) {
        	// user bought a coupon book, so we can kill this activity

    		Log.d("choosecouponbook", "\tbook was purchased, so can finish");
    		this.setResult(RESULT_OK);
        	finish();
        }
	}
	
	private class validateSellerId extends AsyncTask<String, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {
    		
		if( params.length < 1 )
		{
			Toast.makeText(getApplicationContext(), "Seller not found.\nPlease check and try again.", Toast.LENGTH_LONG).show();
			Log.d(TAG, "No sellerId passed into validateSellerId AsyncTask...exiting");
			return false;
		}
    	
    	String sellerId = params[0];
		Log.d(TAG, "Checking for existence of sellerId = "+sellerId);

		try {
		// need to send json object "sellerId" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/validate_seller_id.php");
		   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	       nameValuePairs.add(new BasicNameValuePair("seller_id", sellerId));
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
	            	Log.d(TAG, "Seller Id not validated");    		
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
            dialog= new ProgressDialog(EnterSellerIdDialog.this);
            dialog.setIndeterminate(true);
//            dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_dialog_anim));
            dialog.setCancelable(false);
            dialog.setMessage("Searching for Seller...");
            dialog.show();
		}
	     
    	@Override
		protected void onPostExecute( Boolean result )
    	{
    		dialog.dismiss();
    		if( !result )
    		{
    			Toast.makeText(getApplicationContext(), "Seller not found.\nPlease check and try again.", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			goToChoosePayment();
    		}
    	}
	}
}
