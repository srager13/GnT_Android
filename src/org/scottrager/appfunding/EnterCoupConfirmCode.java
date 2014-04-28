package org.scottrager.appfunding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class EnterCoupConfirmCode extends Activity {
	
	String couponDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter_coup_confirm_code);

	   couponDetails = "";
	   Intent intent = getIntent();
	   Bundle b = intent.getExtras();
	   if( b != null )
	   {
		   couponDetails = b.getString("couponDetail");
	   }
	   Log.d(CouponDisplayActivity.TAG, "coupon detail = "+couponDetails);
	   
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_enter_coup_confirm_code, menu);
		return true;
	}
	
	public void onSubmit( View view ) {
		// return value back to coupon display activity so it knows to finish()
		Intent returnData = new Intent();
		setResult(RESULT_OK, returnData);
		
		//Then go to thank you page
		Intent intent = new Intent( this, CouponUsedDisplay.class );
		intent.putExtra("couponDetails", couponDetails);
		startActivity(intent);
		finish();
	}
	
	public void onCancel( View view ) {

		// return value back to coupon display activity so it knows to finish()
		Intent returnData = new Intent();
		setResult(RESULT_CANCELED, returnData);
		finish();
	}

}
