package org.scottrager.appfunding;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class CouponUsedDisplay extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coupon_used_display);

		
		   String couponDetail = "";
		   Intent intent = getIntent();
		   Bundle b = intent.getExtras();
		   if( b != null )
		   {
			   couponDetail = "Enjoy your "+b.getString("couponDetails")+".  Make sure your server sees this page to redeem offer.";
		   }
		   Log.d(CouponDisplayActivity.TAG, "coupon detail = "+couponDetail);
		   
		   TextView message = (TextView)findViewById( R.id.coupon_used_details );
		   message.setText(couponDetail);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		setResult(RESULT_OK);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_coupon_used_display, menu);
		return true;
	}
	
	public void onClickDone(View view) {
		setResult(RESULT_OK);
		finish();
	}

}
