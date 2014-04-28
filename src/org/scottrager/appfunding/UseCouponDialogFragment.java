package org.scottrager.appfunding;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class UseCouponDialogFragment extends DialogFragment {
	
	public UseCouponDialogFragment(){
		
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    // Use the Builder class for convenient dialog construction
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setMessage(R.string.use_coupon_title)
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   // User cancelled the dialog
	            	   
	               }
	           })
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   // User wants to use the coupon, so we'll remove it from the list
	            	   ((CouponDisplayActivity) getActivity()).useCouponConfirmed();
	               }
	           });
	    // Create the AlertDialog object and return it
	    return builder.create();
    }

}
