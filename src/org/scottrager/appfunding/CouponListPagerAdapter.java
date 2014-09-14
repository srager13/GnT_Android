package org.scottrager.appfunding;

import java.util.Collections;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;


public class CouponListPagerAdapter extends FragmentPagerAdapter {

	public enum SortState {
		Nearest, Expiring, Alphabetical
	}
	
	public CouponListPagerAdapter(FragmentManager fm) {
	   super(fm);
	}
	
	@Override
	public Fragment getItem(int i) {
	   Fragment fragment = new CouponListFragment();
	   Bundle args = new Bundle();
//		switch( i )
//		{
//			case SortByValueEnum.SORT_BY_NAME:
//				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Name");
//				args.putInt(CouponListFragment.ARG_OBJECT, i);
//				break;
//			case SORT_BY_NEAREST:
//				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Nearest");
//				args.putInt(CouponListFragment.ARG_OBJECT, i);
//				break;
//			case SORT_BY_EXP_DATE:
//				Log.d(BrowseCouponsActivity.TAG,  "Sorting by Exp Date");
//				args.putInt(CouponListFragment.ARG_OBJECT, i);
//				break;
//			default:
//				break;
//		}
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

