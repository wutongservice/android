package com.borqs.syncml.ds.protocol;

import java.util.List;

import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.tag.TagAdd;
import com.borqs.syncml.ds.imp.tag.TagDelete;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMapItem;
import com.borqs.syncml.ds.imp.tag.TagStatus;
import com.borqs.syncml.ds.imp.tag.devinfo.TagDevInf;




public interface IDatastore {
    
	public static final int END_SYNC_SUCCESSFULLY = 0;
	public static final int END_SYNC_USER_INTERRUT = 1;
	public static final int END_SYNC_FAILED = 2;

	int getSyncMode();

	String getServerUri();

	long getLastAnchor();

	String getName();

	long getNextAnchor();

	void updateLastSuccessAnchor(long anchor);

	void setSyncMode(int parseInt);

	ISyncListener getListener();

	void prepareSync(TagDevInf serverInf) throws DsException;

	int getNoc();

	void delMap();

	void syncEnd(int result);

	ISyncItem getNextSyncItem();

	int addItem(TagItem item);

	int deleteItem(TagItem item);

	int replaceItem(TagItem item);

	List<TagMapItem> mappings();

	void handleSyncingItemStatus(TagStatus status);

	int[] addItems(List<TagAdd> adds);

	boolean supportBatchAdd();
	
	boolean supportBatchDelete();

	int[] deleteItems(List<TagDelete> deletions);

    boolean onHandleSyncBodyEnd();
    
    void setDataChangeListener(IDataChangeListener dataChangeListener);
}
