package org.scottrager.appfunding;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
 
public class JSONParser {
	
	static private final String TAG = "jsonparser";
 
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
 
    // constructor
    public JSONParser() {
 
    }
 
    public JSONObject getJSONFromUrl(String urlString) {
    	Log.d(TAG, "In getJSONFromUrl()...using urlString="+urlString);
    	URL url;
		try {
			url = new URL(urlString);
			
	    	HttpURLConnection urlConnection;
			urlConnection = (HttpURLConnection) url.openConnection();
			if( urlConnection == null )
			{
				Log.d(TAG, "Unable to connect to "+urlString);
				return null;
			}
			else
			{
				Log.d(TAG, "opened connection to URL");
			}
			InputStream is = urlConnection.getInputStream();
			BufferedInputStream in;
			if( is != null )
			{
				Log.d(TAG, "got input stream from urlConnection");
				in = new BufferedInputStream(is);
			}
			else
			{
				Log.d(TAG, "urlConnection.getInputStream() failed");
				return null;
			}
	        Log.d(TAG, "got input stream");
         
            ByteArrayBuffer baf = new ByteArrayBuffer(500);
            int current = 0;
            while( (current = in.read()) != -1 ) {
            	baf.append((byte) current);
            	//Log.d(TAG, "byte = "+current);
            }
            in.close();
            Log.d(TAG, "BAF: "+baf.toString());
            json = new String(baf.toByteArray(), "utf-8");
            
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        // try parse the string to a JSON object
        try {
        	Log.d(TAG, json);
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }
 
        // return JSON String
        return jObj;
 
    }
}