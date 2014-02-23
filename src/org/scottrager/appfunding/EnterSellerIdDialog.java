package org.scottrager.appfunding;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class EnterSellerIdDialog extends Activity {

	private int coupon_book_id;
	
	static final int BOOK_PURCHASED = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter_seller_id_dialog);
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		coupon_book_id = b.getInt("couponBookId");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_enter_seller_id_dialog, menu);
		return true;
	}
	
	public void onSubmit( View view ) {
		EditText et = (EditText)findViewById(R.id.seller_code_input_box);
		String sellerId = new String(et.getText().toString());
		Intent intent = new Intent( this, ChoosePaymentOptionActivity.class );
		intent.putExtra("sellerCode", sellerId);
		intent.putExtra("couponBookId", coupon_book_id);
		startActivityForResult(intent, BOOK_PURCHASED);
	}
	
	public void onNoSellerId( View view ) {
		String sellerId = new String("NoSeller");
		Intent intent = new Intent( this, ChoosePaymentOptionActivity.class );
		intent.putExtra("sellerCode", sellerId);
		intent.putExtra("couponBookId", coupon_book_id);
		startActivityForResult(intent, BOOK_PURCHASED);
	}
	
	public void onCancel( View view ) {
		finish();
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

}
