package com.borqs.syncml.ds.protocol;

import com.borqs.syncml.ds.exception.DsException;

public interface IProfile {
	String getUserName();

	String getPassword();

	//The value is the apn type
	String getApn();

	String getServerUrl();

	ITransportAgent getTransport() throws DsException;

	IDeviceInfo getDeviceInfo();

	void checkCancelSync() throws DsException;

//	boolean isSyncSourceActive(int i);
//
//	long getLastSyncTime(int type);

	//long getId();

	void stopSync();

	IDatastore getDatastore(int item, ISyncListener syncListener);

	void cancelSync();

	void setInBackground(boolean status);

	boolean isCanceled();

	String proxyName();

	int proxyPort();
	
	long getSyncSourceId(int type);
	
	IPimInterface2 getPimSrcOperator();
}
