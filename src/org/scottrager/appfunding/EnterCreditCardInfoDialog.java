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
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.stripe.android.*;
import com.stripe.android.model.Card;
import com.stripe.exception.AuthenticationException;

public class EnterCreditCardInfoDialog extends Activity {

	private String credit_card_number;
	private int exp_date_month;
	private int exp_date_year;
	private String CVC_code;
	
	private int coupon_book_cost;
	
	public final String TAG = "entercreditcardinfo";

	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter_credit_card_info_dialog);
		
		credit_card_number = "";
		CVC_code = "";
		coupon_book_cost = 0;
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		coupon_book_cost = b.getInt("couponBookCost")*100; // cost given to this activity in dollars, but is given to charge_customer.php in cents
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_enter_seller_id_dialog, menu);
//		return true;
//	}
	
	public void onSubmit( View view ) {
		Log.d(TAG, "Pushed submit button.");
		EditText cc_num_box = (EditText)findViewById(R.id.credit_card_number_input_box);
		credit_card_number = new String(cc_num_box.getText().toString());
		
		EditText cvc_box = (EditText)findViewById(R.id.credit_card_CVC_input_box);
		CVC_code = new String(cvc_box.getText().toString());

		EditText exp_date_month_box = (EditText)findViewById(R.id.credit_card_exp_date_month_input_box);
		exp_date_month = Integer.parseInt(exp_date_month_box.getText().toString());
		
		EditText exp_date_year_box = (EditText)findViewById(R.id.credit_card_exp_date_year_input_box);
		exp_date_year = Integer.parseInt(exp_date_year_box.getText().toString());
		
		Card card = new Card( credit_card_number, exp_date_month, exp_date_year, CVC_code );
		
		if( !card.validateCard() )
		{
			Toast.makeText(getApplicationContext(), "Error in Credit Card Information.\nPlease check and try again.", Toast.LENGTH_LONG).show();
			return;
		}
		
		Stripe stripe;
		try {
			stripe = new Stripe("pk_test_X2jMsVVLLB4HY0dSEDTxJRrw");
			stripe.createToken(
				    card,
				    new TokenCallback() {
						@Override
						public void onSuccess(
								com.stripe.android.model.Token token) {
							// Send token to server
							
							new chargeCustomer().execute(token.toString(), String.valueOf(coupon_book_cost));
						}
				        public void onError(Exception error) {
				            // Show localized error message
				            Toast.makeText(getBaseContext(), "Error charging Credit Card.\nPlease try again.", Toast.LENGTH_LONG).show();
				        }
				    }
				);
				
		} catch (AuthenticationException e) {
			Log.d(TAG, "Failure to create token");
			e.printStackTrace();
		}
		
		
		
	}
	
	public void onCancel( View view ) {
		finish();
	}


	private class chargeCustomer extends AsyncTask<String, Integer, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {
    		
		if( params.length < 2 )
		{
			Toast.makeText(getApplicationContext(), "Credit Card Error.\nPlease check and try again.", Toast.LENGTH_LONG).show();
			Log.d(TAG, "No token or no book cost passed into sendToken AsyncTask...exiting");
			return false;
		}
    	
    	String token = params[0];
    	String coupon_book_cost = params[1];

    	Log.d(TAG, "Trying to charge...");
    	
		try {
		// need to send json object "sellerId" to server  
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost("http://166.78.251.32/gnt/charge_customer.php");
		   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	       nameValuePairs.add(new BasicNameValuePair("stripeToken", token));
	       nameValuePairs.add(new BasicNameValuePair("couponBookCost", coupon_book_cost));
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

	            if( !json.getString("error").equals("none") )
	            {
	            	Log.d(TAG, "Credit card declined.");    		
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
            dialog= new ProgressDialog(EnterCreditCardInfoDialog.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage("Charging credit card...");
            dialog.show();
		}
	     
    	@Override
		protected void onPostExecute( Boolean result )
    	{
    		dialog.dismiss();
    		if( !result )
    		{
    			Toast.makeText(getApplicationContext(), "Credit card declined.\nPlease check and try again.", Toast.LENGTH_LONG).show();
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
