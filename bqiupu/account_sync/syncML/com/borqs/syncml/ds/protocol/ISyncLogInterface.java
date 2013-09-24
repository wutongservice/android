package com.borqs.syncml.ds.protocol;

public interface ISyncLogInterface {
    
	void syncBegin(int mode);

	void syncEnd(int status);

	void clientAdd(boolean result);

	void clientModify(boolean result);

	void clientDel(boolean result);
	
	void clientDelItems(boolean result, int items);

	void serverAdd(boolean result);

	void serverModify(boolean result);

	void serverDel(boolean result);
}
