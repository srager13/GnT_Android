package org.scottrager.appfunding;

import org.scottrager.appfunding.util.SystemUiHider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenCoupon extends FragmentActivity
{
	int position = -1;
    public static final String TAG = "fullscreencoupon";
	
  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
   setContentView(R.layout.activity_fullscreen_coupon);
   

	Log.d(FullscreenCoupon.TAG, "In onCreate()");
   
   String imageName = "";
   Intent intent = getIntent();
   Bundle b = intent.getExtras();
   if( b != null )
   {
	   imageName = b.getString("couponPic");
	   position = b.getInt("position");
   }

   Log.d(FullscreenCoupon.TAG, "position = "+position+", imageName = "+imageName);

	  int logoId = getResources().getIdentifier("drawable/" + imageName, "drawable", getPackageName());

	  ImageView imageView = (ImageView)findViewById(R.id.fullscreen_coupon);
	  imageView.setImageDrawable( getResources().getDrawable(logoId) );
      imageView.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onCouponClicked();
			}
		});
   }	

  @Override
	public void onResume() {
		super.onResume();
  }	
  
  public void onCouponClicked() {
		FragmentManager fm = getSupportFragmentManager();
		UseCouponDialogFragment useCouponDialog = new UseCouponDialogFragment();
		useCouponDialog.show(fm, "dialog");
		return;
  }
  
  public void useCouponConfirmed() {
	  Intent returnData = new Intent();
	  returnData.putExtra("position", position);
	  setResult(RESULT_OK, returnData);
	  finish();
  }
 }
