package com.borqs.sync.changelog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.borqs.sync.provider.SyncMLDb.SyncRecord;
import com.borqs.sync.service.LocalSyncMLProvider;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.protocol.IPimInterface;
import com.borqs.syncml.ds.protocol.ISyncListener;

import android.content.ContentResolver;
import android.database.Cursor;


public class ChangeLogCollection {
	private ContentResolver mResolver;

	static final String[] RECORD_PROJECTION = new String[] { SyncRecord.RECORD, // 0
			SyncRecord.HASH, // 1
	};
	static final int COLUMN_INDEX_RECORD = 0;
	static final int COLUMN_INDEX_HASH = 1;

	public ChangeLogCollection(ContentResolver resolver) {
		mResolver = resolver;
	}

	class SyncListener implements ISyncListener {

		@Override
		public void endSync(int status) {
		}

		@Override
		public void setServerNoc(int n) {
		}

		@Override
		public void handleServer(int opt) {
		}

		@Override
		public void setLocalNoc(int n) {
		}

		@Override
		public void handleLocal() {
		}

		@Override
		public void pleaseWaiting() {
		}

		@Override
		public void checkingLocalData() {
		}

		@Override
		public void setPhase(int phase) {
		}

		@Override
		public void setDeleteAllCount(int count) {
		}

		@Override
		public void deletedOneItemOfAll() {
		}

		@Override
		public void deletedItemsOfAll(int items) {
		}

		@Override
		public void setCurrentDeviceSize(int itemsListSize) {
		}

		@Override
		public void analyzeDeviceItem() {
		}

	}

	/**
	 * 
	 * @param syncSourceId
	 *            the SyncRecord.SOURCE :Contacts,Calendar,Task?
	 * @return the changelog collection
	 * @throws DsException
	 */
	public OrderedObjectSelection executeChangeCollection(IPimInterface mPim,
			long syncSourceId) {
		//get current sync item list
		//current id and hash
		Hashtable<Long, Long> ht = mPim.getItemChangedInfo();
		List currentList = new ArrayList();
		
		Iterator iter = ht.entrySet().iterator();
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 
		    Object currentKey = entry.getKey(); 
		    Object currentVal = entry.getValue();
		    SyncLogItem currentSyncItem = new SyncLogItem(
					String.valueOf(currentKey),
					String.valueOf(currentVal));
		    currentList.add(currentSyncItem);
		} 


		//get previous sync item list.
		String selectString;
		String[] selectArgs;
		selectString = SyncRecord.SOURCE + "=?";
		selectArgs = new String[] { Long.toString(syncSourceId) };
		Cursor previousItems = LocalSyncMLProvider.query(SyncRecord.CONTENT_URI,
				RECORD_PROJECTION, selectString, selectArgs,
				SyncRecord.SORTING_ORDER);
		List previousList = new ArrayList();
		if (previousItems != null) {
			try {
				while (previousItems.moveToNext()) {
					long previousId = previousItems
							.getLong(COLUMN_INDEX_RECORD);
					long previousHash = previousItems
							.getLong(COLUMN_INDEX_HASH);
					SyncLogItem previousSyncItem = new SyncLogItem(
							String.valueOf(previousId),
							String.valueOf(previousHash));
					previousList.add(previousSyncItem);
				}
			} finally {
				previousItems.close();
                LocalSyncMLProvider.close();
			}
		}

		//compare the current and previous sync item ,then get the change log
		OrderedObjectSelection selection = new OrderedObjectSelection(mComparor);
		selection.selection(currentList, previousList);
		
		return selection;
	}

	private SelectionComparor mComparor = new SelectionComparor() {

		@Override
		public boolean compareHash(Object cur, Object pre) {
			return ((SyncLogItem) cur).getHash()
					.compareTo(((SyncLogItem) pre).getHash()) == 0;
		}

		@Override
		public int compareKey(Object cur, Object pre) {
			return ((SyncLogItem) cur).getKey()
					.compareTo(((SyncLogItem) pre).getKey());
		}

	};

}
