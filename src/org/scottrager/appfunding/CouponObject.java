package org.scottrager.appfunding;


public class CouponObject {

	private String companyName; // actually company name
	private String expDate;
	private String couponPic;
	private String couponDetail;
	private boolean favorite;
    private int rowId;
    private double distance;
    private int dateUsed;
	
	public CouponObject( String name, String exp_date, String pic, String detail, boolean fav, int rowid, double dist, int date_used ) {
		companyName = name.toString();
		expDate = exp_date.toString();
		couponPic = pic.toString();
		couponDetail = detail.toString();
		favorite = fav;
		rowId = rowid;
		distance = dist;
		dateUsed = date_used;
	}
	
	public int getRowId() {
		return rowId;
	}
	
	public String getCouponName() {
		return companyName;
	}
	
	public String getExpDate() {
		return expDate;
	}
	
	public String getCouponPic() {
		return couponPic;
	}
	
	public String getCouponDetail() {
		return couponDetail;
	}
	
	public boolean getFavorite() {
		return favorite;
	}
	
	public double getCouponDistance() {
		return distance;
	}
	
	public int getDateUsed() {
		return dateUsed;
	}
}