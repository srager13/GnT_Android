package org.scottrager.appfunding;


public class MarkerObject {

	private String couponName;
	private String expDate;
	private String companyName;
    private int rowId;
    private double distance;
    private int numOffers;
    private double longitude;
    private double latitude;
	
	public MarkerObject( String name, String exp_date, String compName, String detail, 
			int rowid, double dist, int num_offers, double latitudeIn, double longitudeIn ) {
		couponName = name.toString();
		expDate = exp_date.toString();
		companyName = compName.toString(); // file_url in web server database
		rowId = rowid;
		distance = dist;
		numOffers = num_offers;
		latitude = latitudeIn;
		longitude = longitudeIn;
	}
	
	public int getRowId() {
		return rowId;
	}
	
	public String getCouponName() {
		return couponName;
	}
	
	public String getExpDate() {
		return expDate;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	
	public double getCouponDistance() {
		return distance;
	}
	
	public int getNumOffers() {
		return numOffers;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
}