package com.borqs.sync.ds.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import android.text.TextUtils;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.util.BLog;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.contacts.app.ContactsApp;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.client.vdata.card.ContactOperator;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.ds.config.StoredataSetting;
import com.borqs.sync.ds.datastore.DatastoreSyncLog.ISyncFailListener;
import com.borqs.sync.provider.SyncMLDb;
import com.borqs.sync.provider.SyncMLDb.SyncRecord;
import com.borqs.sync.service.LocalSyncMLProvider;
import com.borqs.sync.service.SyncMLService;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.engine.LargeObjectUpload;
import com.borqs.syncml.ds.imp.tag.AlertCode;
import com.borqs.syncml.ds.imp.tag.StatusValue;
import com.borqs.syncml.ds.imp.tag.TagAdd;
import com.borqs.syncml.ds.imp.tag.TagDelete;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMapItem;
import com.borqs.syncml.ds.imp.tag.TagStatus;
import com.borqs.syncml.ds.imp.tag.TagString;
import com.borqs.syncml.ds.imp.tag.devinfo.TagDevInf;
import com.borqs.syncml.ds.protocol.IDataChangeListener;
import com.borqs.syncml.ds.protocol.IDataChangeListener.ContactsChangeData;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.protocol.IPimInterface;
import com.borqs.syncml.ds.protocol.IPimInterface2;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.ISyncItem;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.protocol.ISyncLogInterface;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class GeneralDatastore implements IDatastore,ISyncFailListener {
	private static final String TAG = "syncml";
	private int mSyncMode;
	private ISyncListener mListener;
	private StoredataSetting mSetting;
	private RecordManager recordManager;
	private HashMap<String, ISyncItem> mPutItemsHash;
	private ISyncLogInterface mSyncLog;
	private ContentResolver resolver;
	private IPimInterface pim;
	private IProfile mProfile;
	private String mPrefix;
	private IDeviceInfo mDeviceInfo;
	private TagDevInf mServerInf;
    private SyncDeviceContext mDevice;

	private long mNextAnchor;
	
	private IDataChangeListener mDataChangeListener;
	
	public GeneralDatastore(IProfile profile, ContentResolver resolver,
			StoredataSetting setting, IPimInterface pim,
			ISyncListener listener, String prefix) {
        mDevice = new SyncDeviceContext(ApplicationGlobals.getContext());
		mSetting = setting;
		this.pim = pim;
		this.resolver = resolver;
		mProfile = profile;
		mDeviceInfo = profile.getDeviceInfo();
		mListener = listener;
		mPrefix = prefix;

		recordManager = new RecordManager();
		mPutItemsHash = new HashMap<String, ISyncItem>();

		mNextAnchor = System.currentTimeMillis();
	}

	public int getSyncMode() {
		return mSyncMode;
	}

	public long getLastAnchor() {
		return mDevice.getLastSuccessSyncAnchor();
	}

	public String getName() {
		return mSetting.getName();
	}

	public long getNextAnchor() {
		return mNextAnchor;
	}

	public String getServerUri() {
		return mSetting.getRemoteUri();
	}

	public void updateLastSuccessAnchor(long anchor) {
        mDevice.setLastSuccessSyncAnchor(anchor);
	}
	
	public void updateLastSyncAnchor(long anchor) {
        mDevice.setLastSyncAnchor(anchor);
    }

	public void setSyncMode(int mode) {
	    Logger.logD(TAG, "setSyncMode,mode = " + mode + ",lastAnchor = " + getLastAnchor());
		if (AlertCode.ALERT_CODE_FAST == mode && getLastAnchor() == 0) {
			mSyncMode = AlertCode.ALERT_CODE_SLOW;
		} else {
			mSyncMode = mode;
		}
		Logger.logD(TAG, "after judge,the sync mode is :" + mSyncMode);
	}

	public ISyncListener getListener() {
		return mListener;
	}

	//TODO
	public int getNoc() {
		return recordManager.getNoc();
	}

	public void prepareSync(TagDevInf serverInf) throws DsException {
		mServerInf = serverInf;
		mSyncLog = new DatastoreSyncLog(mSetting.getId(), resolver,this);
		mSyncLog.syncBegin(mSyncMode);
		onSyncBegin();
		if (mSyncMode == AlertCode.ALERT_CODE_REFRESH_FROM_SERVER) {
			deleteAllContent();
		}
		recordManager.prepareSync(mSyncMode);
	}

	public void deleteAllContent() throws DsException {
		//pim.deleteAllContent(mListener, mProfile, mSyncLog);
	    mProfile.getPimSrcOperator().deleteAllContent();
	}

	public void delMap() {
		SyncMLDb.Mapping.delMap(mSetting.getId(), resolver);
	}

	public void handleSyncingItemStatus(TagStatus status) {
		if (status.getSrcRef() == null || status.getSrcRef().size() == 0) {
			if (status.getItems() == null || status.getItems().size() == 0) {
				// All sync commands
			    Logger.logD(TAG, "handleSyncingItemStatus 1");
				boolean tryAgain;
				do {
					tryAgain = false;
					for (ISyncItem item : mPutItemsHash.values()) {
						if (ISyncItem.CMD_STRING[item.getCmd()].equals(status
								.getCmd())) {
							if (setSyncingItemStatus(status.getCmd(), item
									.getSrcLocUri(), status.getStatus())) {
								tryAgain = true;
								break;
							}
						}
					}
				} while (tryAgain);
			} else {
			    Logger.logD(TAG, "handleSyncingItemStatus 2");
				for (TagItem item : status.getItems()) {
					setSyncingItemStatus(status.getCmd(), item.getSrcLocUri(),
							status.getStatus());
				}
			}
		} else if (status.getSrcRef() != null) {
		    Logger.logD(TAG, "handleSyncingItemStatus 3");
			for (String src : status.getSrcRef()) {
				setSyncingItemStatus(status.getCmd(), src, status.getStatus());
			}
		}
	}

	private boolean setSyncingItemStatus(String cmd, String key0, int status) {

		ISyncItem item = mPutItemsHash.get(key0);
		String key = key0.substring(mPrefix.length());
		if (item != null) {
			if (SyncML.Add.equals(cmd)) {
				if (status == StatusValue.ITEM_ADDED) {
					mSyncLog.serverAdd(true);
					onServerAdd(Long.parseLong(key));
				} else if (!StatusValue.isSuccess(status)) {
					mSyncLog.serverAdd(false);
				}
				insertRecord(key, item.getHash());
				mProfile.getPimSrcOperator().serverAdd(Long.parseLong(key), item.getHash());
			} else if (SyncML.Replace.equals(cmd)) {
			    if(StatusValue.isSuccess(status)){
			        onServerUpdate(Long.parseLong(key)); 
			    }
				mSyncLog.serverModify(StatusValue.isSuccess(status));
				updateRecord(key, item.getHash());
				
				mProfile.getPimSrcOperator().serverUpdate(Long.parseLong(key));
			} else if (SyncML.Delete.equals(cmd)) {
			    if(StatusValue.isSuccess(status)){
                    onServerDelete(Long.parseLong(key));
                }
				mSyncLog.serverDel(StatusValue.isSuccess(status));
				deleteRecord(key);
				mProfile.getPimSrcOperator().serverDel(Long.parseLong(key));
			}
			mPutItemsHash.remove(key0);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean onHandleSyncBodyEnd(){
	    return mProfile.getPimSrcOperator().serverOperationExecute();
	}

	public void syncEnd(int result) {
		if (result == END_SYNC_SUCCESSFULLY) {
			Logger.logD(TAG, "syncEnd,successful,nextAnchor will be save as lastAnchor," +
					"the nextAnchor is :" + mNextAnchor);
			updateLastSuccessAnchor(mNextAnchor);
			if(!SyncHelper.isInitSyncSuccess(ApplicationGlobals.getContext())){
                SyncHelper.setInitSyncSuccess(ApplicationGlobals.getContext(), true);
            }
		}

		if (mSyncLog != null) {
			mSyncLog.syncEnd(result);
		}
		onSyncEnd(result);
		mListener.endSync(result);

		mServerInf = null;
		
		updateLastSyncAnchor(mNextAnchor);
	}

	private void insertRecord(String key, long hash) {
		long id = Long.parseLong(key);
		recordManager.insert(id, hash);
	}

	private void updateRecord(String key, long hash) {
		long id = Long.parseLong(key);
		recordManager.update(id, hash);
	}

	private void deleteRecord(String key) {
		long id = Long.parseLong(key);
		recordManager.delete(id);
	}

	public ISyncItem getNextSyncItem() {
		ISyncItem item = null;

		do {
			item = recordManager.getNextSync();

			if (item == null) {
				break;
			}

			if (!item.isValid()) {
				continue;
			}

			if (
			// Large object
			item.getContent() != null
					&& LargeObjectUpload.isLargeObject(item.getContent(),
							mDeviceInfo.getMaxMsgSize())
					// Server does not support large object transport
					&& mServerInf != null && !mServerInf.SupportLargeObjs

			) {
				// clear sync dirty
				long key = Long.parseLong(item.getSrcLocUri().substring(
						mPrefix.length()));
				continue;
			}

			mPutItemsHash.put(item.getSrcLocUri(), item);
			break;
		} while (true);

		return item;
	}

	//TODO
	public int addItem(TagItem item) {
		mListener.handleServer(ISyncListener.SERVER_OPERATION_ADD);
		int status = StatusValue.COMMAND_FAILED;
		long id = 0;
		try {
			if(ContactsApp.isLowStorage()){
				throw new SQLiteException();
			} else if (item.getData() != null && item.getData() instanceof TagString) {
				//id = pim.add(((TagString) (item.getData())).data());
			    id = mProfile.getPimSrcOperator().add(item);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			status = StatusValue.DEVICE_FULL;
		} catch (Exception e) {
			e.printStackTrace();
			status = StatusValue.COMMAND_FAILED;
		}
		if (id <= 0) {
			mSyncLog.clientAdd(false);
		} else {
			status = StatusValue.ITEM_ADDED;
			mSyncLog.clientAdd(true);
			onClientAdd(id);
			recordManager.insert(id, pim.getItemHash(id));

			SyncMLDb.Mapping.put(mSetting.getId(), mPrefix + Long.toString(id),
					item.getSrcLocUri(), resolver);
		}
		return status;
	}

	//TODO
	public int[] addItems(List<TagAdd> adds) {
		int exceptionStauts = StatusValue.COMMAND_FAILED;
		long[] ids = null;
		List<byte[]> itemsList = new ArrayList<byte[]>();
		List<TagItem> items = new ArrayList<TagItem>();

		for (TagAdd add : adds) {
			for (TagItem item : add.items) {
				mListener.handleServer(ISyncListener.SERVER_OPERATION_ADD);
				itemsList.add(item.getByteData());
				items.add(item);
			}
		}
		try {
			if(ContactsApp.isLowStorage()){
				throw new SQLiteException();
			} else {
				//ids = pim.batchAdd(itemsList.toArray(new byte[itemsList.size()][]));
			    ids = mProfile.getPimSrcOperator().batchAdd(items);
			}		
		} catch (SQLiteException e) {
			e.printStackTrace();
			exceptionStauts = StatusValue.DEVICE_FULL;
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] statusList = new int[itemsList.size()];
		int status = StatusValue.COMMAND_FAILED;
		if(ids != null && ids.length > 0){
			BatchRecordOperation syncOp = new BatchRecordOperation(this.resolver);
			Hashtable<Long, Long> hashHt = pim.getItemsHash(ids);
			for (int i = 0; i < ids.length; i++) {
				if(ids[i] <= 0){
					status = exceptionStauts;
					mSyncLog.clientAdd(false);
				} else{
					status = StatusValue.ITEM_ADDED;
					mSyncLog.clientAdd(true);
					onClientAdd(ids[i]);
					if(hashHt != null && hashHt.contains(ids[i])){
						syncOp.newSyncRecord(mSetting.getId(), ids[i], hashHt.get(ids[i]));
					} else{
						syncOp.newSyncRecord(mSetting.getId(), ids[i], pim.getItemHash(ids[i]));
					}					
					syncOp.newMapping(mSetting.getId(), mPrefix
							+ Long.toString(ids[i]), items.get(i).getSrcLocUri());
				}
				statusList[i] = status;
			}
			if(syncOp.size() > 0){
				syncOp.execute();
			}
		} else{
			status = exceptionStauts;
			for(int j = 0; j < statusList.length; j++){
				statusList[j] = status;
			}
		}
		
		return statusList;
	}
	
	//TODO
	public int[] deleteItems(List<TagDelete> deletions) {
		long[] ids = null;
		List<Long> itemsList = new ArrayList<Long>();
		int[] statusList = null;

		for (TagDelete del : deletions) {
			for (TagItem item : del.items) {
				mListener.handleServer(ISyncListener.SERVER_OPERATION_DELETE);
				itemsList.add(Long.parseLong(item.getTarLocUri().substring(
						mPrefix.length())));
			}
		}

		int exceptionStauts = StatusValue.COMMAND_FAILED;
		ContactsChangeData[] cds = null;
		try {
			ids = new long[itemsList.size()];
			cds = new ContactsChangeData[itemsList.size()];
			int i = 0;
			for (Long id : itemsList) {
			    cds[i] = onClientDeleteBegin(id);
				ids[i] = id;
				i++;
			}
			//boolean s = pim.batchDelete(ids);
			boolean s = mProfile.getPimSrcOperator().batchDelete(ids);
			if (s) {
				exceptionStauts = StatusValue.SUCCESS;
				BatchRecordOperation syncOp = new BatchRecordOperation(
						this.resolver);
				for (int j = 0; j < ids.length; j++) {
				    syncOp.delSyncRecord(mSetting.getId(), ids[j]);
				    mSyncLog.clientDel(true);
				    onClientDeleteEnd(cds[i]);
                }
				if (syncOp.size() > 0) {
					syncOp.execute();
				}
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			exceptionStauts = StatusValue.DEVICE_FULL;
			mSyncLog.clientDelItems(false, ids.length);
		} catch (Exception e) {
			e.printStackTrace();
			mSyncLog.clientDelItems(false, ids.length);
		}

		statusList = new int[itemsList.size()];
		for (int j = 0; j < statusList.length; j++) {
			statusList[j] = exceptionStauts;
		}
		return statusList;
	}

	public List<TagMapItem> mappings() {
		return SyncMLDb.Mapping.get(mSetting.getId(), resolver);
	}

	//TODO
	public int deleteItem(TagItem item) {
		mListener.handleServer(ISyncListener.SERVER_OPERATION_DELETE);
		int status = StatusValue.COMMAND_FAILED;
		boolean result = false;
		try {
			long key = Long.parseLong(item.getTarLocUri().substring(
					mPrefix.length()));
			//result = pim.delete(key);
			ContactsChangeData cd = onClientDeleteBegin(key);
			result = mProfile.getPimSrcOperator().delete(key);
			if (result) {
			    onClientDeleteEnd(cd);
				recordManager.delete(key);
				status = StatusValue.SUCCESS;
			} else {
				status = StatusValue.NOT_FOUND;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			status = StatusValue.DEVICE_FULL;
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSyncLog.clientDel(result);
		return status;
	}

	//TODO
	public int replaceItem(TagItem item) {
		mListener.handleServer(ISyncListener.SERVER_OPERATION_UPDATE);
		int status = StatusValue.COMMAND_FAILED;
		boolean replaceResult = false;
		try {
			if(ContactsApp.isLowStorage()){
				throw new SQLiteException();
			} else if (item.getData() != null && item.getData() instanceof TagString) {
				long key = Long.parseLong(item.getTarLocUri().substring(
						mPrefix.length()));
				//replaceResult = pim.update(key, ((TagString) (item.getData())).data());
				replaceResult = mProfile.getPimSrcOperator().update(key, item);
				if (replaceResult) {
				    onClientUpdate(key);
					recordManager.update(key, pim.getItemHash(key));
					status = StatusValue.SUCCESS;
				}
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			status = StatusValue.DEVICE_FULL;
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSyncLog.clientModify(replaceResult);
		return status;
	}

	private interface IFillModified {
		ISyncItem getNext();

		int getNumberOfChange();
	}

	static final String[] RECORD_PROJECTION = new String[] { SyncRecord.RECORD, // 0
			SyncRecord.HASH, // 1
	};

	static final int COLUMN_INDEX_RECORD = 0;
	static final int COLUMN_INDEX_HASH = 1;

	public class RecordManager {
		private IFillModified fillModified;

		public RecordManager() {
		}

		public void reset() {
			String selectString;
			String[] selectArgs;
			selectString = SyncRecord.SOURCE + "=?";
			selectArgs = new String[] { Long.toString(mSetting.getId()) };

            try{
                LocalSyncMLProvider.delete(SyncRecord.CONTENT_URI, selectString, selectArgs);
            }finally {
                LocalSyncMLProvider.close();
            }

			// Delete the map data
			SyncMLDb.Mapping.delMap(mSetting.getId(), resolver);
		}
		
		public void resetSyncInfo(){
            String accountName = AccountAdapter.getLoginID(ApplicationGlobals.getContext());
            if(TextUtils.isEmpty(accountName)){
                BLog.e("accountname is null,resetSyncInfo failed!");
                  return;
            }
		    //reset dirty and serverid
		    ContentValues values = new ContentValues();
		    values.put(RawContacts.DIRTY, 1);
		    values.put(RawContacts.SOURCE_ID, "");
		    resolver.update(RawContacts.CONTENT_URI, values, RawContacts.ACCOUNT_TYPE + "=? AND " +
		    		RawContacts.ACCOUNT_NAME + "=? AND " + RawContacts.DELETED + "=?" , 
		    		new String[]{AccountAdapter.BORQS_ACCOUNT_TYPE,
                            accountName
		            ,"0"});
		    
		    //reset last_anchor
		    StoredataSetting.clearSyncAnchor();
		}

		public void insert(long id, long hash) {

			ContentValues values = new ContentValues(3);
			values.put(SyncRecord.SOURCE, mSetting.getId());
			values.put(SyncRecord.RECORD, id);
			values.put(SyncRecord.HASH, hash);
            try{
                LocalSyncMLProvider.insert(SyncRecord.CONTENT_URI, values);
            }finally {
                LocalSyncMLProvider.close();
            }
		}

		public void delete(long id) {
			String selectString;
			String[] selectArgs;
			selectString = SyncRecord.SOURCE + "=?" + " AND "
					+ SyncRecord.RECORD + "=?";
			selectArgs = new String[] { Long.toString(mSetting.getId()),
					Long.toString(id) };
            try{
                LocalSyncMLProvider.delete(SyncRecord.CONTENT_URI, selectString, selectArgs);
            }finally {
                LocalSyncMLProvider.close();
            }
		}

		public void update(long id, long newHash) {
			String selectString;
			String[] selectArgs;
			selectString = SyncRecord.SOURCE + "=?" + " AND "
					+ SyncRecord.RECORD + "=?";
			selectArgs = new String[] { Long.toString(mSetting.getId()),
					Long.toString(id) };

			ContentValues values = new ContentValues(1);
			values.put(SyncRecord.HASH, newHash);
            try{
                LocalSyncMLProvider.update(SyncRecord.CONTENT_URI, values, selectString,
					selectArgs);
            }finally {
                LocalSyncMLProvider.close();
            }
		}
		
		public ISyncItem getNextSync() {
			return fillModified.getNext();
		}

		public int getNoc() {
			return fillModified.getNumberOfChange();
		}

		public void prepareSync(int syncMode) throws DsException {
		    Logger.logD(TAG, "recordManager prepareSync, mode:" + syncMode); 
			if (syncMode == AlertCode.ALERT_CODE_SLOW
					|| syncMode == AlertCode.ALERT_CODE_REFRESH_FROM_CLIENT
					|| syncMode == AlertCode.ALERT_CODE_REFRESH_FROM_SERVER) {
			    reset();
				resetSyncInfo();
			}

			// when syncMode is one way from server ,
			// fill nothing to syncItem
			if (syncMode == AlertCode.ALERT_CODE_ONE_WAY_FROM_SERVER
					|| syncMode == AlertCode.ALERT_CODE_REFRESH_FROM_SERVER) {
				fillModified = new SuperNoFillModified();
			} else {
				fillModified = new SuperFillModified();
			}

		}

		private class SuperFillModified implements IFillModified {
			private List<Long> mDeletedItems;
			private List<Long> mAddedItems;
			private List<Long> mUpdatedItems;
			private int mDeletedPos;
			private int mAddedPos;
			private int mUpdatedPos;
			private IPimInterface2 mPimSrc;
			
			SuperFillModified() throws DsException {
			    Logger.logD(TAG, "SuperFillModified construct"); 
				mListener.checkingLocalData();
			    mPimSrc = mProfile.getPimSrcOperator();
			    
			    mProfile.checkCancelSync();
			    mDeletedItems = mPimSrc.getDelList();
			    Logger.logD(TAG, "delete contact count:" + mDeletedItems.size());
			    
			    mProfile.checkCancelSync();
				mAddedItems = mPimSrc.getNewList();
				Logger.logD(TAG, "add contact count:" + mAddedItems.size());
				
				mProfile.checkCancelSync();
				mUpdatedItems = mPimSrc.getUpdateList();
			    Logger.logD(TAG, "update contact count:" + mUpdatedItems.size());
		    }

			public ISyncItem getNext() {
				ISyncItem item = null;
				if (mDeletedPos < mDeletedItems.size()) {
					item = mPimSrc.genDelItem(mDeletedItems.get(mDeletedPos));
					mDeletedPos++;
					Logger.logD(TAG, "get delete item:(" +item.getSrcLocUri()+","+ item.getHash());
					
				} else if (mUpdatedPos < mUpdatedItems.size()) {
					item = mPimSrc.genUpdateItem(mUpdatedItems.get(mUpdatedPos));
					mUpdatedPos++;
				} else if (mAddedPos < mAddedItems.size()) {
					item = mPimSrc.genAddItem(mAddedItems.get(mAddedPos));
					mAddedPos++;
				}
				return item;
			}

			public int getNumberOfChange() {
				return mAddedItems.size() + mDeletedItems.size() + mUpdatedItems.size();
			}
		}

		/**
		 * do not fill modifications to SyncItem
		 * */
		private class SuperNoFillModified implements IFillModified {

			SuperNoFillModified() {

			}

			public ISyncItem getNext() {
				return null;
			}

			public int getNumberOfChange() {
				return 0;
			}
		}
	}

	public boolean supportBatchAdd() {
		return pim.supportBatchAdd();
	}
	
	public boolean supportBatchDelete() {
		return pim.supportBatchDelete();
	}

	//===============================for detail sync log
    @Override
    public void setDataChangeListener(IDataChangeListener dataChangeListener) {
        mDataChangeListener = dataChangeListener;
    }
    
    private void onSyncBegin(){
        if(mDataChangeListener != null){
            mDataChangeListener.onBegin();
        }
    }
    
    private void onSyncEnd(int result){
        if(mDataChangeListener != null){
            mDataChangeListener.onEnd(result == IDatastore.END_SYNC_SUCCESSFULLY);
        }
    }
    
    private void onClientAdd(long clientId){
        if(mDataChangeListener != null){
            ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
            ContactsChangeData cd = new ContactsChangeData();
            cd.name = cs.getDisplayName();
            cd.rawContactId = cs.getRawContactId();
            cd.contactId = cs.getContactId();
            mDataChangeListener.onClientAdd(cd);
        }
    }
    
    private void onClientUpdate(long clientId){
        ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
        ContactsChangeData cd = new ContactsChangeData();
        cd.name = cs.getDisplayName();
        cd.rawContactId = cs.getRawContactId();
        cd.contactId = cs.getContactId();
        if(mDataChangeListener != null){
            mDataChangeListener.onClientUpdate(cd);
        }
    }
    
    private ContactsChangeData onClientDeleteBegin(long clientId){
        ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
        ContactsChangeData cd = new ContactsChangeData();
        cd.name = cs.getDisplayName();
        cd.rawContactId = cs.getRawContactId();
        cd.contactId = cs.getContactId();
        return cd;
    }
    
    private void onClientDeleteEnd(ContactsChangeData cd){
        if(mDataChangeListener != null){
            mDataChangeListener.onClientDelete(cd);
        }
    }
    
    private void onServerAdd(long clientId){
        if(mDataChangeListener != null){
            ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
            ContactsChangeData cd = new ContactsChangeData();
            cd.name = cs.getDisplayName();
            cd.rawContactId = cs.getRawContactId();
            cd.contactId = cs.getContactId();
            mDataChangeListener.onServerAdd(cd);
        }
    }
    
    private void onServerUpdate(long clientId){
        if(mDataChangeListener != null){
            ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
            ContactsChangeData cd = new ContactsChangeData();
            cd.name = cs.getDisplayName();
            cd.rawContactId = cs.getRawContactId();
            cd.contactId = cs.getContactId();
            mDataChangeListener.onServerUpdate(cd);
        }
    }
    
    private void onServerDelete(long clientId){
        if(mDataChangeListener != null){
            ContactStruct cs = ContactOperator.loadForSyncLog(clientId, resolver);
            ContactsChangeData cd = new ContactsChangeData();
            cd.name = cs.getDisplayName();
            cd.rawContactId = cs.getRawContactId();
            cd.contactId = cs.getContactId();
            mDataChangeListener.onServerDelete(cd);
        }
    }

    @Override
    public void onSyncFailManyTimes() {
        SyncHelper.showManyRestryFailNotifcation(ApplicationGlobals.getContext());
    }
	
}
