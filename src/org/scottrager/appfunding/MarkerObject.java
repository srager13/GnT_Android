package org.scottrager.appfunding;


public class MarkerObject {

	private String couponName;
	private String expDate;
	private String fileURL;
    private int rowId;
    private double distance;
    private int numOffers;
    private double longitude;
    private double latitude;
    private String addrLine1;
    private String addrLine2;
	
	public MarkerObject( String name, String exp_date, String fileURLin, String detail, 
			int rowid, double dist, int num_offers, double latitudeIn, double longitudeIn, String addrLine1in, String addrLine2in ) {
		couponName = name.toString();
		expDate = exp_date.toString();
		fileURL = fileURLin.toString(); // file_url in web server database
		rowId = rowid;
		distance = dist;
		numOffers = num_offers;
		latitude = latitudeIn;
		longitude = longitudeIn;
		addrLine1 = addrLine1in.toString();
		addrLine2 = addrLine2in.toString();
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
	
	public String getfileURL() {
		return fileURL;
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
	
	public String getAddrLine1() {
		return addrLine1;
	}
	
	public String getAddrLine2() {
		return addrLine2;
	}
}