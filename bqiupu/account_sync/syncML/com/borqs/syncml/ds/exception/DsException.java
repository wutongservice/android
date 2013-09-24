package com.borqs.syncml.ds.exception;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

public class DsException extends Exception {
	public static final int CATEGORY_CLIENT_SETTING = 0;
	public static final int VALUE_MALFORMED_URL = 0;
	public static final int VALUE_INTERRUPT = 1;
	public static final int VALUE_ACCESS_SERVER = 2;
	public static final int VALUE_DATABASE_FULL = 3;
	public static final int VALUE_WBXML_ERROR = 4;

	public static final int CATEGORY_SYNC_STATUS = 2;

	public static final int CATEGORY_HTTP_STATUS = 3;
	
	public static final int CATEGORY_OTHER = 4;

	private int mCategory;
	private int mValue;
	private boolean mTryAgain = false;
	private String mCmd = "";

	public static void checkHttpStatus(HttpResponse response)
			throws DsException {
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new DsException(CATEGORY_HTTP_STATUS, response
					.getStatusLine().getStatusCode());
		}
	}

	public DsException(int category, int value) {
		mCategory = category;
		mValue = value;
	}
	
	public DsException(int category, int value, boolean tryAgain) {
		mCategory = category;
		mValue = value;
		mTryAgain = tryAgain;
	}
	
	public DsException(int category, int value, String cmd) {
		mCategory = category;
		mValue = value;
		mCmd = cmd;
	}
	
	public boolean getTryAgain() {
		return mTryAgain;
	}	

	public int getCategory() {
		return mCategory;
	}

	public int getValue() {
		return mValue;
	}
	
	public String getCmd(){
		return mCmd;
	}
}
