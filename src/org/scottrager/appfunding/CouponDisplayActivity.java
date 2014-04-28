package org.scottrager.appfunding;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CouponDisplayActivity extends FragmentActivity {

	int position = -1;
    public static final String TAG = "coupondisplay";
    static final int COUPON_REDEEMED_REQUEST = 1;
	private Boolean usable;
	private String imageName = "";
	private String companyName = "";
	private String couponDetail = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_coupon_display);
		   

	   Intent intent = getIntent();
	   Bundle b = intent.getExtras();
	   if( b != null )
	   {
		   imageName = b.getString("couponPic");
		   companyName = b.getString("companyName");
		   couponDetail = b.getString("couponDetail");
		   position = b.getInt("position");
		   usable = b.getBoolean("usable");
		   
	   }
	   Log.d(TAG, "position = "+position+", imageName = "+imageName);
	   Log.d(TAG, "company = "+companyName+", coupon detail = "+couponDetail);

	   int logoId = getResources().getIdentifier("drawable/" + imageName, "drawable", getPackageName());

	   ImageView imageView = (ImageView)findViewById(R.id.company_logo);
	   imageView.setImageDrawable( getResources().getDrawable(logoId) );
	    
	   TextView compName = (TextView)findViewById(R.id.company_name_title);
	   compName.setText( companyName );
	   
	   TextView compAddr = (TextView)findViewById(R.id.company_address_1);
	   compAddr.setText("123 Somewhere Lane");
	   TextView compAddr_2 = (TextView)findViewById(R.id.company_address_2);
	   compAddr_2.setText("Pittsburgh, PA 15237");

	   TextView coupDets = (TextView)findViewById(R.id.coupon_details_1);
	   coupDets.setText(couponDetail);
	   TextView coupDets2 = (TextView)findViewById(R.id.coupon_details_2);
	   coupDets2.setText("Prices and participation may vary.");
	   
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_coupon_display, menu);
		return true;
	}

	public void onCancelClick( View view ) {
		Intent returnData = new Intent();
		returnData.putExtra( "position",  -1 );
		setResult( RESULT_OK, returnData );
		finish();
	}
	  
	public void onRedeemCoupon( View view ) {

		   if( !usable )
		   {
			   Toast.makeText(this, "Please purchase the book to use!", Toast.LENGTH_LONG).show();
			   return;
		   }

		   //Toast.makeText(this, "Coupon Details = "+couponDetail, Toast.LENGTH_LONG).show();
		   
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle(R.string.use_coupon_title);
	        builder.setMessage(R.string.coupon_use_confirm_message)
	               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                     // Use Coupon
	                	   
	               		// return value back to coupon display activity so it knows to finish()
	               		//Intent returnData = new Intent();
	               		//setResult(RESULT_OK, returnData);
	               		
	               		//Then go to thank you page
	               		Intent intent = new Intent(CouponDisplayActivity.this, CouponUsedDisplay.class);
	               		intent.putExtra("couponDetails", couponDetail);
	               		startActivity(intent);
	               		CouponDisplayActivity.this.finish();
	                   }
	               })
	               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // Cancelled - return to coupon without using
	                   }
	               });
	        // Create the AlertDialog object and return it
	        builder.show();
		   
//			Intent intent = new Intent( this, EnterCoupConfirmCode.class );
//			Bundle b = new Bundle();
//	        b.putString("couponDetail", couponDetail);
//	        intent.putExtras(b);
//	        startActivityForResult(intent, COUPON_REDEEMED_REQUEST);
			
	        return;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data ) {
		   Log.d(TAG, "in onActivityResult:  position = "+position);
		if( requestCode == COUPON_REDEEMED_REQUEST )
		{
			if( resultCode == RESULT_OK )
			{
				// coupon was redeemed, so we can mark it as used in the database and exit here

				// return value back to browse coupons activity so it knows to mark coupon as used
				Intent returnData = new Intent();
				returnData.putExtra("position", position);
				Log.d(BrowseCouponsActivity.POSITION_TAG, "onActivityResult in CouponDisplayActivity: position = "+position);
				setResult(RESULT_OK, returnData);
				
				finish();
			}
		}
	}
	  
	public void useCouponConfirmed() {
		  Intent returnData = new Intent();
		  returnData.putExtra("position", position);
		  setResult(RESULT_OK, returnData);
		  finish();
	}
	  
	public void clickBackButton( View view ) {
		finish();
	}
	
	public void onClickMoreOffers( View view ) {
 
		Log.d(TAG, "Should launch Company Info/More offers activity.");

        Intent intent = new Intent(this, CompanyInfoMoreOffersActivity.class);
        Bundle b = new Bundle();
        b.putString("couponPic", imageName);
        b.putInt("position", position);
        b.putString("companyName", companyName);
        //b.putString("couponDetail", couponDetail);
       // b.putBoolean("usable", true);
        intent.putExtras(b);

        startActivity( intent );
        //startActivityForResult( coupDisplayIntent, use_coup_request_Code );
	}
}
