package org.scottrager.appfunding;



import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DBAdapter {
	
	public static final String KEY_EVENTID = "event_id";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_PW_HASH = "pw_hash";
	public static final String KEY_COUPONID = "coupon_id";
	public static final String KEY_COMPANYID = "company_id";
	public static final String KEY_COMPANY_NAME = "company_name";
	public static final String KEY_COUPON_DETAILS = "coupon_details";
	public static final String KEY_EXP_DATE = "exp_date";
	public static final String KEY_FILE_URL = "file_url";
	public static final String KEY_DATE_USED = "date_used";
	public static final String KEY_FAVORITE = "favorite";
	public static final String KEY_RECV_SYNC = "recv_sync";
	public static final String KEY_LOCATIONID = "location_id";
	public static final String KEY_ADDR_LINE1 = "address_line_1";
	public static final String KEY_ADDR_LINE2 = "address_line_2";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String TAG = "DBAdapter";

	public static final String DATABASE_NAME = "MyDB";
	public static final String DATABASE_COUPONS_TABLE = "coupons";
	public static final String DATABASE_COMPANY_TABLE = "company";
	public static final String DATABASE_LOCATIONS_TABLE = "locations";
	public static final String DATABASE_EVENTS_TABLE = "events";
	public static final int DATABASE_VERSION = 34;
	

	private static final String DATABASE_CREATE_1 = "create table "+DATABASE_COMPANY_TABLE
			+" (company_id integer primary key, "
			+ "company_name text not null, file_url text not null);";
	
	private static final String DATABASE_CREATE_2 = "create table "+DATABASE_COUPONS_TABLE
			+" (coupon_id integer primary key, "
			+ " company_id integer, coupon_details text not null, "
			+ " exp_date text not null, "
			+ " favorite integer not null, "
			+ " FOREIGN KEY (company_id) REFERENCES company(company_id));";
		
	private static final String DATABASE_CREATE_3 = "create table "+DATABASE_LOCATIONS_TABLE
			+" (company_id integer, "
			+ "location_id integer, address_line_1 text, address_line_2 text, latitude float, longitude float, "
			+ "FOREIGN KEY (company_id) REFERENCES company(company_id) );";
	
	private static final String DATABASE_CREATE_4 = "create table "+DATABASE_EVENTS_TABLE
			+" ( event_id integer not null primary key, "
			+ " coupon_id integer not null, "
			+ " date_used text, recv_sync text not null, "
			+ " FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) );";
	
	private final Context context;
	
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter (Context ctx) 
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			try {
				Log.d(TAG, "Executing the following sql statements to make tables:");
				Log.d(TAG, DATABASE_CREATE_1);
				Log.d(TAG, DATABASE_CREATE_2);
				Log.d(TAG, DATABASE_CREATE_3);
				Log.d(TAG, DATABASE_CREATE_4);
				
				db.execSQL(DATABASE_CREATE_1);
				db.execSQL(DATABASE_CREATE_2);
				db.execSQL(DATABASE_CREATE_3);
				db.execSQL(DATABASE_CREATE_4);
			}
			catch (SQLException e)
			{
				Log.e(TAG, "Exception thrown while creating tables");
				e.printStackTrace();
			}
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

			Log.d(TAG, "Upgrading database version from "+oldVersion+" to " + newVersion + " which will destroy data.");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_LOCATIONS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_COUPONS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_COMPANY_TABLE);
			onCreate(db);
		}
	}
	
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		DBHelper.close();
	}
	
	// These coupons need to go into the events table
	//   they will point to the correct company_id values and coupon_id values
	// Need to check if we have 
	public int insertCouponEvent( int event_id, int coupon_id )
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EVENTID, event_id);
		initialValues.put(KEY_COUPONID, coupon_id);
		//initialValues.put(KEY_DATE_USED, );
		initialValues.put(KEY_RECV_SYNC, "0");
		return (int)db.insert(DATABASE_EVENTS_TABLE, null, initialValues);
	}
	
	public int insertCoupon( int coupon_id, int company_id, String coupon_details, String exp_date, int fav )
	{
		Log.d(TAG, "Inserting coupon with coupon_id = "+coupon_id+", company_id = "+company_id+", exp_date = "+exp_date+", fav = "+fav);
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_COUPONID, coupon_id);
		initialValues.put(KEY_COMPANYID, company_id);
		initialValues.put(KEY_COUPON_DETAILS, coupon_details);
		initialValues.put(KEY_EXP_DATE, exp_date);
		initialValues.put(KEY_FAVORITE, fav);
		return (int)db.insert(DATABASE_COUPONS_TABLE, null, initialValues);
	}
	
	public int insertCompany( int company_id, String company_name, String file_url)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_COMPANYID, company_id);
		initialValues.put(KEY_COMPANY_NAME, company_name);
		initialValues.put(KEY_FILE_URL, file_url);
		return (int)db.insert(DATABASE_COMPANY_TABLE, null, initialValues);
	}
	
	public int insertLocation( int company_id, int location_id, String addr_line_1, String addr_line_2, double latitude, double longitude )
	{
		Log.d(TAG, "Trying to insertLocation with company_id = "+company_id+", and location_id="+location_id+", addr_1 = " + addr_line_1 + ", addr_2 = "+addr_line_2+", lat="+latitude+", long="+longitude);
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_COMPANYID, company_id);
		initialValues.put(KEY_LOCATIONID, location_id);
		initialValues.put(KEY_ADDR_LINE1, addr_line_1);
		initialValues.put(KEY_ADDR_LINE2, addr_line_2);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUDE, longitude);
		int numInserted = -1;
		try {
			numInserted = (int)db.insert(DATABASE_LOCATIONS_TABLE, null, initialValues);
		}
		catch( SQLException e )
		{
			Log.d(TAG, "SQLException:");
			e.printStackTrace();
		}
		return numInserted; 
	}
	
	public boolean deleteCoupon( int rowId )
	{
		long rows = db.delete(DATABASE_EVENTS_TABLE, KEY_EVENTID + "=?", new String[]{""+rowId+""});
		Log.d(TAG, "number of rows deleted = "+rows);
		return (rows > (long)0);
	}
	
	public boolean markCouponSynced( int rowId )
	{
		ContentValues newValue = new ContentValues();
		newValue.put(KEY_RECV_SYNC, "1");
		int numRowsAffected = db.update(DATABASE_EVENTS_TABLE, newValue, KEY_EVENTID + "=?", new String[]{""+rowId+""});
		return numRowsAffected > 0;
	}
	
	public boolean markCouponUsed( int rowId )
	{
		ContentValues newDateUsed = new ContentValues();
		Date date = new Date();
		newDateUsed.put(KEY_DATE_USED, (int)date.getTime()/1000);
		Log.w(TAG, "Marking Coupon with rowId = " +rowId+ " as used...date used = " + date.getTime()/1000);
		Log.w(BrowseCouponsActivity.POSITION_TAG, "markCouponUsed in DBAdapter: rowId = " +rowId+ ", date used = " + date.getTime()/1000);
		int numRowsAffected =  db.update( DATABASE_EVENTS_TABLE, newDateUsed, KEY_EVENTID + "=?", new String[]{""+rowId+""});
		Log.w(TAG, "Number rows affected = " +numRowsAffected);

		/*//For debug purposes:
		Cursor c = db.query(DATABASE_TABLE, new String[] {KEY_EVENTID, KEY_DATE_USED}, null, null, null, null, null);
		if( c.moveToFirst() )
		{
			do
			{
				Log.w(TAG, "Row ID = "+c.getInt( c.getColumnIndex(KEY_EVENTID) ) );
				Log.w(TAG, "Date Used = "+c.getInt( c.getColumnIndex(KEY_DATE_USED) ) );
			}while( c.moveToNext() );
		}
		*/
		
		return numRowsAffected > 0;
	}
	
	public boolean markCouponAsFavorite( int rowId )
	{
		// need to find coupon_event in events table and get coupon_id of that coupon_event
		//   then we can update that coupon_id's favorite value
		int coupon_id;
		Cursor cursor = db.query( DATABASE_EVENTS_TABLE, new String[] { KEY_COUPONID },
							KEY_EVENTID + "=?", new String[]{String.valueOf(rowId)}, null, null, null);
		if( cursor.moveToFirst() )
		{
			coupon_id = cursor.getInt(cursor.getColumnIndex(KEY_COUPONID));
		}
		else
		{
			return false;
		}
		ContentValues newValue = new ContentValues();
		newValue.put(KEY_FAVORITE, 1);
		Log.w(TAG, "Marking Coupon with rowId = " +rowId+ " and " +coupon_id+" as favorite");
		int numRowsAffected =  db.update( DATABASE_COUPONS_TABLE, newValue, KEY_COUPONID + "=?", new String[]{String.valueOf(coupon_id)} );
		Log.w(TAG, "Number rows affected = " +numRowsAffected);
		
		return numRowsAffected > 0;
	}
	
	public boolean markCouponAsNotFavorite( int rowId )
	{		
		// need to find coupon_event in events table and get coupon_id of that coupon_event
		//   then we can update that coupon_id's favorite value
		int coupon_id;
		Cursor cursor = db.query( DATABASE_EVENTS_TABLE, new String[] { KEY_COUPONID },
							KEY_EVENTID + "=?", new String[]{String.valueOf(rowId)}, null, null, null);
		if( cursor.moveToFirst() )
		{
			coupon_id = cursor.getInt(cursor.getColumnIndex(KEY_COUPONID));
		}
		else
		{
			return false;
		}
		ContentValues newValue = new ContentValues();
		newValue.put(KEY_FAVORITE, 0);
		Log.w(TAG, "Marking Coupon with rowId = " +rowId+ " and " +coupon_id+" as not favorite");
		int numRowsAffected =  db.update( DATABASE_COUPONS_TABLE, newValue, KEY_COUPONID + "=?", new String[]{String.valueOf(coupon_id)} );
		Log.w(TAG, "Number rows affected = " +numRowsAffected);
		
		return numRowsAffected > 0;
	}
	
	public boolean deleteAllCoupons()
	{
		return db.delete( DATABASE_EVENTS_TABLE, null, null ) > 0;
	}
	
	// need to make a query that joins all tables
	public Cursor getAllUnusedCoupons()
	{
		//return db.query( DATABASE_EVENTS_TABLE, new String[] {KEY_EVENTID, KEY_COUPON_NAME, 
		//		KEY_COUPON_DETAILS, KEY_EXP_DATE, KEY_FILE_URL, KEY_DATE_USED, KEY_FAVORITE, KEY_LATITUDE, KEY_LONGITUDE},
		//					KEY_DATE_USED + "=?", new String[]{"0"}, null, null, null);

					//	SELECT * FROM events, coupons, company
		 			// WHERE events.date_used IS NULL AND
					// events.coupon_id=coupons.coupon_id AND
					// coupons.company_id=company.company_id AND

		Cursor c1 = db.rawQuery("SELECT * FROM company", null);
		Log.d(TAG, "Number of companies in db = "+c1.getCount());
		Cursor c2 = db.rawQuery("SELECT * FROM coupons", null);
		Log.d(TAG, "Number of coupons in db = "+c2.getCount());
		Cursor c3 = db.rawQuery("SELECT * FROM events", null);
		Log.d(TAG, "Number of events in db = "+c3.getCount());
		Cursor c4 = db.rawQuery("SELECT * FROM locations",  null);
		Log.d(TAG,  "Number of locations in db = "+c4.getCount());
		if( c3.moveToFirst() )
		{
			do
			{
				Log.d(TAG, "rowId = "+c3.getInt(c3.getColumnIndex(KEY_EVENTID)));	
			}while( c3.moveToNext() );
		}
		//String query = "SELECT * FROM events INNER JOIN coupons ON events.coupon_id=coupons.coupon_id INNER JOIN company ON coupons.company_id=company.company_id";
//		String query = "SELECT * FROM events, coupons, company";
/*		String query = "SELECT * FROM " + DATABASE_EVENTS_TABLE + " INNER JOIN " + DATABASE_COUPONS_TABLE + " INNER JOIN " + DATABASE_COMPANY_TABLE
				+ "ON "+ DATABASE_EVENTS_TABLE + "." + KEY_DATE_USED + " IS NULL AND "  
				+ DATABASE_EVENTS_TABLE+"."+KEY_COUPONID+"="+DATABASE_COUPONS_TABLE+"."+KEY_COUPONID+" AND " 
				+ DATABASE_COUPONS_TABLE + "." + KEY_COMPANYID + "=" + DATABASE_COMPANY_TABLE + "." + KEY_COMPANYID +";";
				*/
		
/*		String query = "SELECT * FROM events, coupons, company, locations WHERE events.date_used IS NULL "
				+ "AND events.coupon_id=coupons.coupon_id AND coupons.company_id=company.company_id"
				+" AND locations.location_id=1";*/

		//String query = "SELECT "+KEY_EVENTID+", "+KEY_COMPANY_NAME+", "+KEY_COUPON_DETAILS+", "+KEY_EXP_DATE+", "+KEY_FILE_URL+", "+KEY_DATE_USED
		String query = "SELECT * "
				+ "FROM events, coupons, locations, company "
				+ "WHERE events.coupon_id=coupons.coupon_id "
				+ "AND events.date_used IS NULL "
				+ "AND coupons.company_id=company.company_id "
				+ "AND company.company_id=locations.company_id "
				+" AND locations.location_id=1";
		Cursor c = db.rawQuery(query, null);
		
		Log.d(TAG, "Number of unused coupons = "+c.getCount());
		/*if( c.moveToFirst() )
		{
			do
			{
				Log.d(TAG, "rowId = "+c.getInt(c.getColumnIndex(KEY_EVENTID)));	
			}while( c.moveToNext() );
		}*/
		return c;
	}

	// need to make a query that joins all tables
	public Cursor getAllUnusedCouponsOfCompany( String desiredCompany )
	{
		//return db.query( DATABASE_EVENTS_TABLE, new String[] {KEY_EVENTID, KEY_COUPON_NAME, 
		//		KEY_COUPON_DETAILS, KEY_EXP_DATE, KEY_FILE_URL, KEY_DATE_USED, KEY_FAVORITE, KEY_LATITUDE, KEY_LONGITUDE},
		//					KEY_DATE_USED + "=? AND KEY_COUPON_NAME =?", new String[]{"0", company}, null, null, null);
		String query = "SELECT * "
				+ "FROM events, coupons, locations, company "
				+ "WHERE events.coupon_id=coupons.coupon_id "
				+ "AND events.date_used IS NULL "
				+ "AND coupons.company_id=company.company_id "
				+ "AND company.company_id=locations.company_id "
				+ "AND locations.location_id=1 "
				+ "AND company.company_name=\""+desiredCompany+"\"";
		Cursor c = db.rawQuery(query, null);
		//	SELECT * FROM events, coupons, company
//		String query = "SELECT * FROM " + DATABASE_EVENTS_TABLE + ", " + DATABASE_COUPONS_TABLE + ", " + DATABASE_COMPANY_TABLE
//				+ " WHERE " + DATABASE_EVENTS_TABLE + "." + KEY_DATE_USED + " IS NULL AND "  // WHERE events.date_used IS NULL AND 
//				+ DATABASE_COMPANY_TABLE + "." + KEY_COMPANY_NAME + "='" + desiredCompany + "' AND "// company.name="desiredCompany" AND
//				+ DATABASE_EVENTS_TABLE+"."+KEY_COUPONID+"="+DATABASE_COUPONS_TABLE+"."+KEY_COUPONID+" AND " // events.coupon_id=coupons.coupon_id AND
//				+ DATABASE_COUPONS_TABLE + "." + KEY_COMPANYID + "=" + DATABASE_COMPANY_TABLE + "." + KEY_COMPANYID +";";  // coupons.company_id=company.company_id AND
		Log.d(TAG, "Number of unused coupons from "+desiredCompany+" = "+c.getCount());
		if( c.moveToFirst() )
		{
			do
			{
				Log.d(TAG, "rowId = "+c.getInt(c.getColumnIndex(KEY_EVENTID)));	
			}while( c.moveToNext() );
		}
		return c;
	}

	// need to make a query that joins all tables ??
	public Cursor getUsedCoupons()
	{
		return db.query( DATABASE_EVENTS_TABLE+", "+DATABASE_COUPONS_TABLE, 
				new String[] {KEY_EVENTID, KEY_FAVORITE, KEY_DATE_USED}, 
				"events.coupon_id=coupons.coupon_id AND " + KEY_DATE_USED + "!=?", new String[]{"0"}, null, null, null, null);
	}
	

	// need to make a query that joins all tables
	public Cursor getUnsyncedCoupons()
	{
		return db.query( DATABASE_EVENTS_TABLE, new String[] {KEY_EVENTID}, 
				KEY_RECV_SYNC + "=?", new String[]{"0"}, null, null, null, null);
	}
	
	// this will need to do more...probably need to accept company_id as input, query that table
	//   and then query locations database and return results...will have to decide if we should 
	//   calculate distances and limit results here (probably) or from place where call is made
	public Cursor getLocations()
	{
		// should we only return locations that we have coupons for?
		//Cursor c = db.query( DATABASE_LOCATIONS_TABLE, new String[]{"company_id", "latitude", "longitude"}, "company_id=?", new String[]{"1"}, null, null, null, null);
		Cursor c = db.rawQuery("SELECT * FROM locations",  null);
		
		if( c == null )
			Log.d(TAG, "locations query returned null");
		
		return c;
	}

	// need to make a query that joins all tables
	public Cursor getCompanyNameFromId( int id )
	{
		// choose the companies that we have unused coupons for and get all locations
		String query = "SELECT * "
				+ "FROM company "
				+ "WHERE company.company_id="+id+" ";
		Log.d(TAG, "Executing query: "+query);
		Cursor c = db.rawQuery(query, null);
		Log.d(TAG, "Num responses = "+c.getCount());
		return c;
	}
	
	public int getNumCouponsFromId( int id )
	{
		// SELECT * FROM events,company,coupons 
		//            WHERE events.coupon_id=coupons.coupon_id
		//            AND   events.date_used IS NULL
		//            AND   company.company_id=1
		//            AND   coupons.comany_id=company.company_id
		String query = "SELECT * "
				+ "FROM "+DATABASE_EVENTS_TABLE+","+DATABASE_COMPANY_TABLE+","+DATABASE_COUPONS_TABLE+" "
				+ "WHERE "+DATABASE_EVENTS_TABLE+"."+KEY_COUPONID+"="+DATABASE_COUPONS_TABLE+"."+KEY_COUPONID+" "
				+ "AND "+DATABASE_EVENTS_TABLE+"."+KEY_DATE_USED+" IS NULL "
				+ "AND "+DATABASE_COMPANY_TABLE+"."+KEY_COMPANYID+"=1 "
				+ "AND "+DATABASE_COUPONS_TABLE+"."+KEY_COMPANYID+"="+DATABASE_COMPANY_TABLE+"."+KEY_COMPANYID;
		
		Cursor c = db.rawQuery(query, null);
		Log.d(TAG, "In getNumCouponsFromId: companyId "+ id + " returns "+ c.getCount() +" coupons.");
		return c.getCount();
	}
	
	public boolean couponInDatabase( int coupon_id )
	{
		Cursor c = db.query( DATABASE_COUPONS_TABLE, new String[] {KEY_COUPONID}, 
				KEY_COUPONID + "=?", new String[]{String.valueOf(coupon_id)}, null, null, null, null);
		if( c.moveToFirst() )
			return true;
		else
			return false;
	}
	
	public boolean locationInDatabase( int company_id, int location_id )
	{
		Cursor c = db.query( DATABASE_LOCATIONS_TABLE, new String[] {KEY_COMPANYID, KEY_LOCATIONID}, 
				KEY_COMPANYID + "=? AND " + KEY_LOCATIONID + "=?", new String[]{String.valueOf(company_id),String.valueOf(location_id)}, 
				null, null, null, null);
		if( c.moveToFirst() )
			return true;
		else
			return false;
	}
	
	public boolean companyInDatabase( int company_id )
	{

		Cursor c = db.query( DATABASE_COMPANY_TABLE, new String[] {KEY_COMPANYID}, 
				KEY_COMPANYID + "=?", new String[]{String.valueOf(company_id)}, null, null, null, null);
		if( c.moveToFirst() )
			return true;
		else
			return false;
	}
	
	public void deleteDatabase()
	{
		db.execSQL( "DROP TABLE IF EXISTS coupons" );
	}
	
	
}