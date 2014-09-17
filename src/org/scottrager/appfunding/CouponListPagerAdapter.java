package org.scottrager.appfunding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;


public class CouponListPagerAdapter extends FragmentPagerAdapter {

//	public enum SortState {
//		Nearest, Expiring, Alphabetical
//	}
	
	public CouponListPagerAdapter(FragmentManager fm) {
	   super(fm);
	}
	
	@Override
	public Fragment getItem(int i) {
	
		Log.d(CouponListFragment.TAG, "in CouponListPagerAdapter:  int passed to getItem() = "+i);
	   Fragment fragment = new CouponListFragment();
	   Bundle args = new Bundle();
		switch( i )
		{
			case 0:
				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Nearest");
				args.putInt(CouponListFragment.ARG_OBJECT, i);
				break;
			case 1:
				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Exp Date");
				args.putInt(CouponListFragment.ARG_OBJECT, i);
				break;
			case 2:
				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Name (A-Z)");
				args.putInt(CouponListFragment.ARG_OBJECT, i);
				break;
			default:
				break;
		}
	   args.putInt(CouponListFragment.ARG_OBJECT, i);
	   ((Fragment) fragment).setArguments(args);
	   return fragment;
	}
	
	@Override
	public int getCount() {
	   return 3;
	}
//	
//	@Override
//	public CharSequence getPageTitle(int position) {
//	   return "OBJECT " + (position + 1);
//	}
}

