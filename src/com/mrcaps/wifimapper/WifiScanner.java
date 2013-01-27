package com.mrcaps.wifimapper;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.HandlerThread;
import android.util.Log;

public class WifiScanner {
	protected final String TAG = "WS-Scanner";
	protected Context ctx;
	protected WifiManager manager;
	protected WifiReceiver receiver;
	protected ConnectivityManager connmanager;
	
	protected boolean associated;
	protected boolean scanning;
	protected long lastScan;
	protected WifiScanCallback callback;
	
	private WifiManager.WifiLock mylock;
	private WifiManager.WifiLock fulllock;
	
	public WifiScanner(Context ctx) {
		this.ctx = ctx;
		
		manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		connmanager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		mylock = manager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "com.mrcaps.AccelWifi.WifiScannerLock");
		fulllock = manager.createWifiLock(WifiManager.WIFI_MODE_FULL, "com.mrcaps.AccelWifi.WifiFullLock");
		mylock.setReferenceCounted(false);
		fulllock.setReferenceCounted(false);
		receiver = new WifiReceiver();
		lastScan = System.currentTimeMillis();
		scanning = false;
	}
	
	public void setCallback(WifiScanCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * Call me whenever the activity associated with this WifiScanner is resumed.
	 */
	public void associate() {
		IntentFilter itf = new IntentFilter();
		itf.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		ctx.registerReceiver(receiver, itf);  
	}
	
	public void close() {
		setCallback(null);
		
		try {
			ctx.unregisterReceiver(receiver);
		} catch (Exception ex) {
			Log.e(TAG, "Couldn't unregister receiver", ex);
		}
	}
	
	public void acquireLock(boolean full) {
		if (full)
			fulllock.acquire();
		else
			mylock.acquire();
	}
	
	public void releaseLock(boolean full) {
		if (full)
			fulllock.release();
		else
			mylock.release();
	}
	
	public WifiManager getManager() {
		return manager;
	}
	
	public boolean isConnected() {
		//http://www.androidsnippets.org/snippets/131/
		NetworkInfo[] infos = connmanager.getAllNetworkInfo();
		boolean conn = false;
		for (NetworkInfo info : infos) {
			conn |= (info!=null) && info.isConnected();
		}
		return conn;
	}
	
	/**
	 * @return true on success
	 */
	public boolean scan() {
		boolean success = false;
		
		if (scanning) {
			Log.v(TAG, "Already in a scan; not scanning again.");
		} else {
			scanning = true;
			success = manager.startScan();
			Log.v(TAG, "Starting scan " + ((success) ? "succeeded" : "failed"));
			lastScan = System.currentTimeMillis();
		}
		
		return success;
	}
	
	public interface WifiScanCallback {
		public void scanComplete(List<ScanResult> res);
	}
	
	protected class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long scanTime = System.currentTimeMillis() - lastScan;
			Log.v(TAG, String.format("Got Scan Results in %d ms", scanTime));
			List<ScanResult> res = manager.getScanResults();
			for (ScanResult s : res) {
				Log.v(TAG, s.toString());
			}
			if (callback != null) {
				callback.scanComplete(res);
			}
			
			scanning = false;
		}
	}
}
