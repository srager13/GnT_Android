package org.scottrager.appfunding;

import java.util.ArrayList;

import android.content.Context;
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

		int logoId = context.getResources().getIdentifier("drawable/" + coupons.get(position).getCouponPic(), "drawable", context.getPackageName());
		imageView.setImageDrawable( context.getResources().getDrawable(logoId) );

		if( !allowFavorite )
		{
			favStar.setImageResource(R.drawable.fav_star);
			favStar.setOnClickListener( doNothing );
		}
		else
		{
			if( coupons.get(position).getFavorite() )
			{
				favStar.setImageResource(R.drawable.fav_star);
				favStar.setOnClickListener( makeNotFavorite );
			}
			else
			{
				favStar.setImageResource(R.drawable.not_fav_star);
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
}
