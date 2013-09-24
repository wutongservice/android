package com.borqs.syncml.ds.protocol;

import java.io.IOException;

public interface IDeviceInfo {

	int getMaxMsgSize();

	String getUserAgent();

	String deviceId();

	int getMaxObjSize();

	byte[] deviceData(boolean devCap, int syncItem) throws IOException;

	boolean isSyncContactsPhoto();
	
//	boolean isDisplayUserInfo();
	
	boolean isDisplaySettings();
	
	boolean isPrintLog();
}
