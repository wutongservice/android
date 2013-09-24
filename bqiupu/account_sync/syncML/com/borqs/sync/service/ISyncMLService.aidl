
package com.borqs.sync.service;

import com.borqs.sync.service.ISyncMLCallBack;

interface ISyncMLService
{
	int[] registerCallBack(ISyncMLCallBack callback);
	void unregisterCallBack(ISyncMLCallBack callback);
	int getSyncItemStatus(int item);
	void stop();
	long getSyncingProfile();
	void setInBackground(boolean status);
	int getSyncStatus();
	String getLastMessage();
	int getCurrentPhase();
}