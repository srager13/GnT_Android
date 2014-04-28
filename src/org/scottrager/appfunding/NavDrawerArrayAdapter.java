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

public class NavDrawerArrayAdapter extends ArrayAdapter<String> {  
	private final Context context;
	private final ArrayList<String> navOptions;

	public NavDrawerArrayAdapter(Context context, ArrayList<String> navOptions) {
		super(context, R.layout.options_row, navOptions);
		this.context = context;
		this.navOptions = navOptions;
	}

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.options_row, parent, false);
	    
	    TextView textView = (TextView) rowView.findViewById(R.id.optionTitle);
	    textView.setText(navOptions.get(position));
	    
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.optionPic);
	    imageView.setImageDrawable( context.getResources().getDrawable(R.drawable.categories_button));
	    
		//int logoId = context.getResources().getIdentifier("drawable/" + coupons.get(position).getCouponPic(), "drawable", context.getPackageName());
		//imageView.setImageDrawable( context.getResources().getDrawable(logoId) );
		
	    return rowView;
	  }
	  
	  private OnClickListener doNothing = new OnClickListener() {				
			@Override
			public void onClick(View v) {
				
			}
	  };
}
