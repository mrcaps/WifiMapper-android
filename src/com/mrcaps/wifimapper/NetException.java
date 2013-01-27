package com.mrcaps.wifimapper;

public class NetException extends Exception {
	public NetException(String message) {
		super(message);
	}
	
	public NetException(Exception other) {
		super(other.getMessage(), other);
	}
}
