

package com.borqs.sync.service;

interface ISyncMLCallBack
{
	void handleCallBack(String msg);
	void handleRegisterCallBack(String msg,boolean showRegisterDialog);
	void handleAlertMsg(String msg);
	void syncBegin();
	void syncEnd(int syncResultType,int exceptionCode,int excepitonCategory);
	void updateSyncItemStatus(int item, int status);
	void handleSyncPhase(int phase);
}