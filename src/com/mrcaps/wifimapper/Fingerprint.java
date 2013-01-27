package com.mrcaps.wifimapper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.net.wifi.ScanResult;
import android.util.Log;

public class Fingerprint {
	private static final String TAG = "WM-Fingerprint";
	
	private long id;
	private long timestamp;
	private double lat;
	private double lon;
	private float floor;
	
	private static final int APS_LEN = 10;
	private List<AP> aps; 
	
	public static final int PRESIZE =
		8 + //id
		8 + //timestamp
		8 + //lat
		8 + //lon
		4;  //floor
	
	public static final int SIZE =
		PRESIZE + //fixed data
		APS_LEN * AP.SIZE; //AP scans
	
	public Fingerprint() {
		aps = new ArrayList<AP>();
	}
	
	public Fingerprint(
			long timestamp,
			double lat,
			double lon,
			float floor,
			List<ScanResult> results) {
		
		this();
		this.timestamp = timestamp;
		this.lat = lat;
		this.lon = lon;
		this.floor = floor;
		
		Collections.sort(results, comparator_scanStrength);
		for (ScanResult res : results) {
			aps.add(new AP(res));
		}
	}
	
	private static Comparator<ScanResult> comparator_scanStrength =
		new Comparator<ScanResult>() {
			@Override
			public int compare(ScanResult sr1, ScanResult sr2) {
				return sr2.level - sr1.level;
			}
	};
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Fingerprint [\n\taps=").append(aps).append("\n\t, floor=")
				.append(floor).append(", id=").append(id).append(", lat=")
				.append(lat).append(", lon=").append(lon)
				.append(", timestamp=").append(timestamp).append("]");
		return builder.toString();
	}

	/**
	 * Read in a fingerprint from the given ByteBuffer
	 * @param input
	 */
	public Fingerprint(ByteBuffer input) {
		this();
		
		id = input.getLong();
		timestamp = input.getLong();
		lat = input.getDouble();
		lon = input.getDouble();
		floor = input.getFloat();
		
		for (int x = 0; x < APS_LEN; ++x) {
			AP ap = new AP(input);
			if (!ap.isEmpty()) {
				aps.add(ap);
			}
		}
	}
	
	public void toBytes(ByteBuffer output) {
		output.putLong(id);
		output.putLong(timestamp);
		output.putDouble(lat);
		output.putDouble(lon);
		output.putFloat(floor);
		
		for (int x = 0; x < APS_LEN; ++x) {
			if (x < aps.size()) {
				aps.get(x).toBytes(output);
			} else {
				AP.emptyBytes(output);
			}
		}
	}
	
	public byte[] toByteArray() {
		ByteBuffer self = ByteBuffer.allocate(SIZE);
		self.position(0);
		toBytes(self);
		return self.array();
	}
	
	public static class AP {	
		private static final int BSSID_LEN = 8;
		private static final int HEX_RADIX = 16;
		private static final byte[] emptyBytes = new byte[BSSID_LEN];
		private byte[] bssid;
		private int rssi;
		
		public static final int SIZE = BSSID_LEN + 8;
		
		public AP() {
			bssid = new byte[BSSID_LEN];
		}
		
		/**
		 * Make a new AP out of 16 bytes of encoded input
		 * @param input
		 */
		public AP(ByteBuffer input) {
			this();
			
			input.get(bssid, 0, BSSID_LEN);
			rssi = input.getInt();
			//ignore 4 bytes
			input.getInt();
		}
		
		public AP(ScanResult s) {
			this();
			
			//decode the BSSID
			String bs = s.BSSID;
			if (bs.length() != 6*2+5) {
				Log.w(TAG, "Unexpected BSSID length:" + bs.length());
			}
			
			bssid[0] = (byte) Integer.parseInt(bs.substring(0, 0+2), HEX_RADIX);
			bssid[1] = (byte) Integer.parseInt(bs.substring(3, 3+2), HEX_RADIX);
			bssid[2] = (byte) Integer.parseInt(bs.substring(6, 6+2), HEX_RADIX);
			bssid[3] = (byte) Integer.parseInt(bs.substring(9, 9+2), HEX_RADIX);
			bssid[4] = (byte) Integer.parseInt(bs.substring(12, 12+2), HEX_RADIX);
			bssid[5] = (byte) Integer.parseInt(bs.substring(15, 15+2), HEX_RADIX);
			
			this.rssi = s.level;
		}
		
		public boolean isEmpty() {
			for (int x = 0; x < bssid.length; ++x) {
				if (bssid[x] != 0) {
					return false;
				}
			}
			return true;
		}
		
		public void toBytes(ByteBuffer output) {
			output.put(bssid, 0, BSSID_LEN);
			output.putInt(rssi);
			output.putInt(0);
		}
		
		public static void emptyBytes(ByteBuffer output) {
			output.put(emptyBytes, 0, BSSID_LEN);
			output.putInt(0);
			output.putInt(0);
		}
		
		public String toString() {
			return "AP[" + Arrays.toString(bssid) + " rssi=" + rssi;
		}
	}
}
