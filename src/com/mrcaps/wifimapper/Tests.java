package com.mrcaps.wifimapper;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.mrcaps.wifimapper.WifiScanner.WifiScanCallback;

public class Tests {
	private static final String TAG = "WM-Tests";
	private static WifiScanner scanner;
	
	private static void disassociate() {
		if (scanner != null) {
			scanner.close();
		}
	}
	private static WifiScanner getScanner(Context ctx) {
		if (scanner == null) {
			scanner = new WifiScanner(ctx);
		}
		
		return scanner;
	}
	
	public static void testScan(Context ctx) {
		WifiScanner scanner = getScanner(ctx);
		scanner.associate();
		scanner.setCallback(testScanCallback);
	}
	
	public static void testUpload(Context ctx) {
		WifiScanner scanner = getScanner(ctx);
		scanner.associate();
		scanner.setCallback(testUploadCallback);
	}
	
	private static WifiScanCallback testScanCallback = new WifiScanner.WifiScanCallback() {	
		@Override
		public void scanComplete(List<ScanResult> res) {
			Fingerprint f = new Fingerprint(
				System.currentTimeMillis(),
				10d,
				-35d,
				1f,
				res);
			
			Log.v(TAG, "Obtained Fingerprint:");
			Log.v(TAG, f.toString());
			
			ByteBuffer outbuf = ByteBuffer.allocate(Fingerprint.SIZE);
			f.toBytes(outbuf);
			
			Log.v(TAG, "Serialized Fingerprint:");
			byte[] bfinger = outbuf.array();
			Log.v(TAG, Arrays.toString(bfinger));
			
			ByteBuffer readin = ByteBuffer.wrap(bfinger);
			Fingerprint f2 = new Fingerprint(readin);
			Log.v(TAG, "Round-Trip Fingerprint:");
			Log.v(TAG, f2.toString());
			
			disassociate();
		}
	};
	
	private static WifiScanCallback testUploadCallback = new WifiScanner.WifiScanCallback() {
		@Override
		public void scanComplete(List<ScanResult> res) {
			Fingerprint f = new Fingerprint(
					System.currentTimeMillis(),
					10d,
					-35d,
					1f,
					res);
			
			Log.v(TAG, "Obtained Fingerprint:");
			Log.v(TAG, f.toString());
			
			NetService ns = new NetService();
			
			try {
				ns.uploadPrint(f);
			} catch (NetException ex) {
				Log.e(TAG, "Couldn't upload print", ex);
			}
			
			disassociate();
		}
	};
}
