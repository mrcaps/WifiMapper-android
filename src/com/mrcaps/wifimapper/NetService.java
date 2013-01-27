package com.mrcaps.wifimapper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

import android.util.Log;

public class NetService {
	private static final String TAG = "WM-NetService";
	
	private static final String URL_BASE = "http://www.younav.com/";
	private static final String URL_SERVICE = URL_BASE + "s/";
	
	private static final String URL_NEWPRINT = "newprint.php";
	
	private static final String serviceURL(String url) {
		return URL_SERVICE + url;
	}
	
	private Net net;
	
	public NetService() {
		net = new Net();
	}
	
	public void uploadPrint(Fingerprint print) throws NetException {
		String url = serviceURL(URL_NEWPRINT);
		
		HttpEntity printBytes = net.getEntity(print.toByteArray());
		InputStream is = net.doPost(url, printBytes);
		
		Log.v(TAG, "Upload Print Response: " + Net.isToString(is));
		
		try {
			is.close();
		} catch (IOException ex) {}
	}
}
