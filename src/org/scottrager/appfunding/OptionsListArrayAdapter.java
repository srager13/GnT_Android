package org.scottrager.appfunding;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OptionsListArrayAdapter extends ArrayAdapter<String> {  
	private final Context context;
	private final ArrayList<String> titles;
	private final ArrayList<String> pictures;

	public OptionsListArrayAdapter(Context context, ArrayList<String> titles, ArrayList<String> pictures) {
		super(context, R.layout.options_row, titles);
		this.context = context;
		this.titles = titles;
		this.pictures = pictures;
	}

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.options_row, parent, false);
	    
	    TextView title = (TextView) rowView.findViewById(R.id.optionTitle);
	    title.setText(titles.get(position));

	    ImageView imageView = (ImageView) rowView.findViewById(R.id.optionPic);

		int logoId = context.getResources().getIdentifier("drawable/" + pictures.get(position).toString(), "drawable", context.getPackageName());
		imageView.setImageDrawable( context.getResources().getDrawable(logoId) );
	    
	    return rowView;
	  }

}
