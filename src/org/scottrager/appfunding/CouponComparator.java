package org.scottrager.appfunding;

import java.util.Comparator;

enum CouponComparator implements Comparator<CouponObject> {
    EXP_DATE_SORT {
        public int compare(CouponObject o1, CouponObject o2) {
            return String.valueOf(o1.getExpDate()).compareTo(o2.getExpDate());
        }},
    NEAREST_SORT {
        public int compare(CouponObject o1, CouponObject o2) {
            return Double.valueOf(o2.getCouponDistance()).compareTo(o1.getCouponDistance());
        }},
    NAME_SORT {
        public int compare(CouponObject o1, CouponObject o2) {
        	//Log.d(BrowseCouponsActivity.TAG, "Comparing coupons..." + o1.getCouponName() + " and " + o2.getCouponName() );
            return String.valueOf(o2.getCouponName()).compareTo(o1.getCouponName());
        }};

    public static Comparator<CouponObject> descending(final Comparator<CouponObject> other) {
        return new Comparator<CouponObject>() {
            public int compare(CouponObject o1, CouponObject o2) {
                return -1 * other.compare(o1, o2);
            }
        };
    }

    public static Comparator<CouponObject> getComparator(final CouponComparator... multipleOptions) {
        return new Comparator<CouponObject>() {
            public int compare(CouponObject o1, CouponObject o2) {
                for (CouponComparator option : multipleOptions) {
                    int result = option.compare(o1, o2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }
}