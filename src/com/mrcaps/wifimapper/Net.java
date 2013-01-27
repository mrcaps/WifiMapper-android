package com.mrcaps.wifimapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class Net {
	private static final String TAG = "WM-Net";
	
	private static byte[] sBuffer = new byte[512];
	private static final int HTTP_STATUS_OK = 200;
	
	public Net() {
		
	}
	
	public HttpEntity getEntity(byte[] bytes) {
		return new ByteArrayEntity(bytes);
	}
	
	public HttpEntity getEntity(List<NameValuePair> nvps) {
		try {
			return new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		} catch (UnsupportedEncodingException ex) {
	    	throw new RuntimeException("Couldn't encode name-value pairs", ex);
	    }
	}
	
	public InputStream doPost(String url, HttpEntity post) throws NetException {
		Log.v(TAG, "Starting POST to " + url);
		
		// Create client and set our specific user-agent string
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		if (post != null) {
		    request.setEntity(post);
		}
		
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (IOException ex) {
			throw new NetException(ex);
		}
			
		// Check if server response is valid
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != HTTP_STATUS_OK) {
		    throw new NetException("Invalid response from server: " +
		            status.toString());
		}
		
		// Pull content stream from response
		HttpEntity entity = response.getEntity();
		InputStream inputStream = null;
		try {
			inputStream = entity.getContent();
		} catch (IOException ex) {
			throw new NetException(ex);
		}
        
		return inputStream;
	}
	
	public static byte[] isToByteArray(InputStream inputStream) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        
        try {
	        // Read response into a buffered stream
	        int readBytes = 0;
	        while ((readBytes = inputStream.read(sBuffer)) != -1) {
	            content.write(sBuffer, 0, readBytes);
	        }
        } catch (IOException ex) {
        	Log.e(TAG, "Couldn't read from input stream", ex);
        }
        
        // Return result from buffered stream
        return content.toByteArray();  		
	}
	
	public static String isToString(InputStream content) {
		return new String(isToByteArray(content));
	}
}
