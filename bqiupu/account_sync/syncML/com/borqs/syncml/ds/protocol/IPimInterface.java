package com.borqs.syncml.ds.protocol;

import java.util.Hashtable;

import com.borqs.syncml.ds.exception.DsException;

import android.net.Uri;


public interface IPimInterface {
	long add(byte[] data);
	
	boolean supportBatchAdd();
	
	long[] batchAdd(byte[][] data);
	
	boolean supportBatchDelete();
	
	boolean batchDelete(long[] key);
	
	boolean update(long key, byte[] data);

	boolean delete(long key);

	long getItemHash(long currentId);
	
	Hashtable<Long, Long> getItemsHash(long[] ids);
	
	void deleteAllContent(ISyncListener syncListener, 
			IProfile profile, ISyncLogInterface syncLog) throws DsException;
	
	ISyncItem genDeleteItem(long id);

	ISyncItem genAddItem(long id);

	ISyncItem genUpdateItem(long id);
	
	long[] getCurrentItems(ISyncListener syncListener);

	//Uri getUri();
	
	boolean isItemChanged(long id, long hash, Hashtable<Long, Long> ht);
	
	Hashtable<Long, Long> getItemChangedInfo();
	
	long getChangedItemCount();
	
	void syncBegin();
	
	void syncEnd();
}
