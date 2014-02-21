package org.scottrager.appfunding;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class CouponBookArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> couponBookNames;
	private final ArrayList<String> couponBookCosts;
	private final ArrayList<String> couponBookValues;
	private final ArrayList<Integer> couponBookNumbers;

	public CouponBookArrayAdapter(Context context, ArrayList<String> couponBookNames, 
			ArrayList<String> couponBookCosts, ArrayList<String> couponBookValues, 
			ArrayList<Integer> couponBookNumbers) {
		super(context, R.layout.coupon_row, couponBookNames);
		this.context = context;
		this.couponBookNames = couponBookNames;
		this.couponBookCosts = couponBookCosts;
		this.couponBookValues = couponBookValues;
		this.couponBookNumbers = couponBookNumbers;
	}


	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.coupon_book_row, parent, false);
	    
	    TextView coupBookName = (TextView) rowView.findViewById(R.id.couponBookName);
	    coupBookName.setText(couponBookNames.get(position));
	    
	    TextView coupBookCost = (TextView) rowView.findViewById(R.id.couponBookCost);
	    coupBookCost.setText(couponBookCosts.get(position));
	    
	    TextView coupBookValue = (TextView) rowView.findViewById(R.id.couponBookValue);
	    coupBookValue.setText(couponBookValues.get(position));
		
	    return rowView;
	  }
}
