package com.borqs.sync.ds.datastore.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.util.BLog;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.vdata.PimSyncmlInterface;
import com.borqs.sync.client.vdata.card.ContactOperator;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.client.vdata.card.ContactsVCardOperator;
import com.borqs.sync.ds.config.StoredataSetting;
import com.borqs.sync.ds.datastore.GeneralDatastore;
import com.borqs.sync.ds.datastore.GeneralPimInterface;
import com.borqs.sync.ds.datastore.GeneralPimOperator;
import com.borqs.sync.ds.datastore.GeneralPimWordsProcess;
import com.borqs.sync.provider.SyncMLDb.TContactChangeLog;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.common.Constant;
import com.borqs.syncml.ds.protocol.IDataChangeListener;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.protocol.ISyncLogInterface;

import java.util.Hashtable;

/**
 * 
 * IDataChangelistener :for contact change log saving,we will save the log into Account_Settings,it can 
 * be shared to any Application binding with Borqs Account.
 *
 */
public class ContactsInterface extends GeneralPimInterface implements IDataChangeListener {
    private ContactChangeLog mChangeLog;
	public ContactsInterface(ContentResolver resolver,
			PimSyncmlInterface<Object> pim, String dateType, String prefix,
			Uri uri) {
		super(resolver, pim, dateType, prefix, uri);
		mChangeLog = new ContactChangeLog();
	}
	
	static final private String DATA_TYPE = "text/x-vcard";
	
	Hashtable<Long, Long> mContactLogHt;

	@SuppressWarnings("unchecked")
	static public IDatastore datastore(IProfile profile,
			ContentResolver resolver, StoredataSetting setting,
			ISyncListener listener) {
		PimSyncmlInterface<?> contact = new ContactsVCardOperator(true);
		((ContactsVCardOperator) contact).isContainPhotoInfo(profile
				.getDeviceInfo().isSyncContactsPhoto());
//		// set vcard version to 2.1
//		((ContactsVCardOperator) contact).setVcardVersion(1);
		ContactsInterface pim = new ContactsInterface(resolver,
				(PimSyncmlInterface<Object>) contact, DATA_TYPE,
				Constant.PREFIX_CONTACTS, Contacts.People.CONTENT_URI);
		IDatastore contactStore = new GeneralDatastore(profile, resolver, setting, pim, listener,
                Constant.PREFIX_CONTACTS);
		contactStore.setDataChangeListener(pim);
		return contactStore;
	}
	
	public boolean isItemChanged(long id, long hash, Hashtable<Long, Long> ht) {
		if(ht != null && ht.containsKey(id)){
			return ht.get(id) != hash;
		}else{
			return true;
		}	
	}
	
	private static final String[] PROJECTIONS_LOG_ID = { TContactChangeLog._ID, TContactChangeLog.ANCHOR };
	private static final String SQLWHERE_ACCOUNT = RawContacts.ACCOUNT_NAME + " =? AND " + RawContacts.ACCOUNT_TYPE + " =? AND " + RawContacts.DELETED + " =? ";
//	private static final String[] SQLARGS_ACCOUNT = new String[] { Constant.BORQS_ACCOUNT_TYPE,Integer.toString(0)};
	public Hashtable<Long, Long> getItemChangedInfo() {
	    String accountName = AccountAdapter.getLoginID(ApplicationGlobals.getContext());
        Hashtable<Long, Long> contactHt = new Hashtable<Long,Long>();
        if(TextUtils.isEmpty(accountName)){
            return contactHt;
        }
	    String[] argsAccount = new String[]{accountName, AccountAdapter.BORQS_ACCOUNT_TYPE,"0"};

        Cursor cursor = mResolver
                .query(TContactChangeLog.CONTENT_URI, PROJECTIONS_LOG_ID,
                        SQLWHERE_ACCOUNT,
                        argsAccount,
                        TContactChangeLog._ID);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    contactHt.put(cursor.getLong(0), cursor.getLong(1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

		mContactLogHt = contactHt;
		return contactHt;
	}
	
	public long getChangedItemCount(){
	    long count = 0;
	    return count;
	}

	private static final String[] PROJECTIONS_LOG = { TContactChangeLog.ANCHOR };

	public long getItemHash(long currentId) {
		long retValue = 0;
        Cursor cursor = mResolver
                .query(ContentUris.withAppendedId(TContactChangeLog.CONTENT_URI,
                        currentId), PROJECTIONS_LOG, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                retValue = cursor.getLong(0);
            }
            cursor.close();
        }
		return retValue;
	}

	public long[] batchAdd(byte[][] data) {
		ContactsVCardOperator operator = new ContactsVCardOperator();
		
		ContactStruct[] contacts = new ContactStruct[data.length];
		
		for(int i = 0 ; i < data.length; i ++){
			//deal with value before save.eg,default,order
			ContactStruct cs = operator.parse(data[i]);
			if(cs != null){
				contacts[i]= (ContactStruct) GeneralPimWordsProcess.dealFieldsProcessBeforeSave(Constant.PREFIX_CONTACTS, cs);
			}
		}

		long[] ids = ContactOperator.batchAdd(contacts, mResolver);
		
		return ids;
	}

	public boolean supportBatchAdd() {
		return true;
	}

	public Hashtable<Long, Long> getItemsHash(long[] ids) {
		Hashtable<Long, Long> contactHt = null;
		StringBuilder selectString = new StringBuilder().append(TContactChangeLog._ID + " in (");
		for(int i = 0; i < ids.length; i++){
			if(i == 0){
				selectString.append(String.valueOf(ids[i]));
			}else{
				selectString.append(",").append(String.valueOf(ids[i]));
			}
		}
        selectString.append(")");

        Cursor cursor = mResolver
                .query(TContactChangeLog.CONTENT_URI, PROJECTIONS_LOG_ID, selectString.toString(), null, null);
        if (cursor != null) {
            contactHt = new Hashtable();
            if (cursor.moveToFirst()) {
                do {
                    contactHt.put(cursor.getLong(0), cursor.getLong(1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
		return contactHt;
	}
	
	public boolean supportBatchDelete() {
		return true;
	}

	static private final int BATCH_DELETE_SIZE = 100;
	
	public boolean batchDelete(long[] key){
		for (int start = 0; start < key.length; start += BATCH_DELETE_SIZE) {
			ContactOperator.batchDelete(key, start, BATCH_DELETE_SIZE, mResolver);
		}
		return true;
	}

	public void deleteAllContent(ISyncListener syncListener, IProfile profile,
			ISyncLogInterface syncLog) throws DsException {
	    String accountName = AccountAdapter.getLoginID(ApplicationGlobals.getContext());
        if(TextUtils.isEmpty(accountName)){
            BLog.e("accountName is null,delteAllContent failed");
             return;
        }
        String[] argsAccount = new String[]{accountName, AccountAdapter.BORQS_ACCOUNT_TYPE,"0"};
        
		Uri uri = RawContacts.CONTENT_URI;
		Cursor cursor = mResolver.query(uri, new String[] { BaseColumns._ID },
				SQLWHERE_ACCOUNT, argsAccount, null);
		if (cursor != null) {
			syncListener.setDeleteAllCount(cursor.getCount());
			
			long[] ids = new long[cursor.getCount()];
			try {
				int i = 0;
				while (cursor.moveToNext()) {
					ids[i++] = cursor.getLong(0);
				}
			} finally {
				cursor.close();
			}
			
			for (int start = 0; start < ids.length; start += BATCH_DELETE_SIZE) {
				profile.checkCancelSync();
				int syncLength = BATCH_DELETE_SIZE;
				if((start + BATCH_DELETE_SIZE) > ids.length){
					syncLength = ids.length - start;
				}
				syncListener.deletedItemsOfAll(syncLength);
				syncLog.clientDelItems(
						ContactOperator.batchDelete(ids, start, syncLength, mResolver)
						, syncLength);
			}
		}		
	}

	public long[] getCurrentItems(ISyncListener syncListener) {
	    String accountName = AccountAdapter.getLoginID(ApplicationGlobals.getContext());
        if(TextUtils.isEmpty(accountName)){
            return new long[0];
        }
        String[] argsAccount = new String[]{accountName, AccountAdapter.BORQS_ACCOUNT_TYPE,"0"};
        
		return GeneralPimOperator.getCurrentItems(RawContacts.CONTENT_URI, mResolver, SQLWHERE_ACCOUNT, argsAccount, syncListener);
	}
	
    @Override
    public void onBegin() {
        mChangeLog.begin();
    }

    @Override
    public void onClientAdd(ContactsChangeData data) {
        mChangeLog.onClientAdd(data);
    }

    @Override
    public void onClientDelete(ContactsChangeData data) {
        mChangeLog.onClientDelete(data);
    }

    @Override
    public void onClientUpdate(ContactsChangeData data) {
        mChangeLog.onClientUpate(data);
    }

    @Override
    public void onServerAdd(ContactsChangeData data) {
        mChangeLog.onServerAdd(data);
    }

    @Override
    public void onServerUpdate(ContactsChangeData data) {
        mChangeLog.onServerUpdate(data);
    }

    @Override
    public void onServerDelete(ContactsChangeData data) {
        mChangeLog.onServerDelete(data);
    }

    @Override
    public void onEnd(boolean success) {
        mChangeLog.end(success);
    }

}
