package org.scottrager.appfunding;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CouponListArrayAdapter extends ArrayAdapter<String> {  
	private final Context context;
	private final ArrayList<CouponObject> coupons;
	private final Boolean allowFavorite;
	
	private DBAdapter db;

	public CouponListArrayAdapter(Context context, ArrayList<CouponObject> coupons, ArrayList<String> names, Boolean allowFavorite) {
		super(context, R.layout.coupon_row, names);
		this.context = context;
		this.coupons = coupons;
		this.allowFavorite = allowFavorite;
		db = new DBAdapter( getContext() );  // just use context?
	}

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.coupon_row, parent, false);
	    
	    TextView textView = (TextView) rowView.findViewById(R.id.couponName);
	    textView.setText(coupons.get(position).getCouponName());
	    
	    TextView details = (TextView) rowView.findViewById(R.id.couponDetails);
	    details.setText(coupons.get(position).getCouponDetail());
	    
	    TextView expDate = (TextView) rowView.findViewById(R.id.couponExpDate);
	    expDate.setText("EXP: May 30th");

	    TextView distance = (TextView) rowView.findViewById(R.id.couponDistance);
	    if( coupons.get(position).getCouponDistance() == -1.0 )
	    {
	    	distance.setText( String.format("", coupons.get(position).getCouponDistance()/1609.34) );
	    }
	    else
	    {
	    	distance.setText( String.format("%.2f miles", coupons.get(position).getCouponDistance()/1609.34) );
	    }
	    
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.couponSmallPic);
	    
	    ImageView favStar = (ImageView) rowView.findViewById(R.id.favStar);
	    PositionTag posTag = new PositionTag( coupons.get(position).getRowId() );
	    favStar.setTag(posTag);

	    // TODO:: Take image loading off of UI thread.  Move it to async task: http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
		int logoId = context.getResources().getIdentifier("drawable/" + coupons.get(position).getCouponPic(), "drawable", context.getPackageName());
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource( context.getResources(), logoId, options);
		
		imageView.setImageBitmap( decodeSampledBitmapFromResource(context.getResources(), logoId, 44, 44)); // 44 x 44 because that is defined in coupon_row.xml

		if( !allowFavorite )
		{
//			favStar.setImageResource(R.drawable.fav_star);
			favStar.setImageBitmap(decodeSampledBitmapFromResource(context.getResources(), R.drawable.fav_star, 44, 44));
			favStar.setOnClickListener( doNothing );
		}
		else
		{
			if( coupons.get(position).getFavorite() )
			{
				//favStar.setImageResource(R.drawable.fav_star);
				favStar.setImageBitmap(decodeSampledBitmapFromResource(context.getResources(), R.drawable.fav_star, 44, 44));
				favStar.setOnClickListener( makeNotFavorite );
			}
			else
			{
				//favStar.setImageResource(R.drawable.not_fav_star);
				favStar.setImageBitmap(decodeSampledBitmapFromResource(context.getResources(), R.drawable.not_fav_star, 44, 44));
				favStar.setOnClickListener( makeFavorite );
			}
		}
		
	    return rowView;
	  }
	  
	  private OnClickListener makeFavorite = new OnClickListener() {				
			@Override
			public void onClick(View v) {
				int rowId = ((PositionTag) v.getTag()).getPosition();
				db.open();
				db.markCouponAsFavorite(rowId);
				db.close();
				ImageView favStar = (ImageView) v.findViewById(R.id.favStar);
				favStar.setImageResource(R.drawable.fav_star);
				favStar.setOnClickListener(makeNotFavorite);
			}
	  };
	  
	  private OnClickListener makeNotFavorite = new OnClickListener() {				
			@Override
			public void onClick(View v) {
				int rowId = ((PositionTag) v.getTag()).getPosition();
				db.open();
				db.markCouponAsNotFavorite(rowId);
				db.close();
				ImageView favStar = (ImageView) v.findViewById(R.id.favStar);
				favStar.setImageResource(R.drawable.not_fav_star);
				favStar.setOnClickListener(makeFavorite);
			}
	  };
	  
	  private OnClickListener doNothing = new OnClickListener() {				
			@Override
			public void onClick(View v) {
				
			}
	  };
	  
	  public static int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;

	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

	    return inSampleSize;
	}
	  
	  public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
		        int reqWidth, int reqHeight) {

		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeResource(res, resId, options);

		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeResource(res, resId, options);
		}
}
