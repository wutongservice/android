package com.borqs.sync.ds.config;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import com.borqs.common.account.AccountAdapter;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.config.ProfileConfig;
import com.borqs.sync.ds.datastore.contacts.ContactsInterface;
import com.borqs.sync.ds.datastore.contacts.ContactsSrcOperator;
import com.borqs.sync.provider.SyncMLDb.ProfileTable;
import com.borqs.sync.provider.SyncMLDb.Settings;
import com.borqs.sync.service.Define;
import com.borqs.sync.service.LocalSyncMLProvider;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.common.ConnectivityUtil;
import com.borqs.syncml.ds.imp.common.CryptUtil;
import com.borqs.syncml.ds.imp.transport.HttpTransport;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.protocol.IPimInterface2;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.protocol.ITransportAgent;

import java.net.MalformedURLException;


public class SyncProfile implements IProfile {
	private long id;
	private String user;
	private String passwd;
	private String url;
	private String apn;
	private String proxyName;
	private int proxyPort;

	private StoredataSetting[] mSourcesSetting;

	private ITransportAgent mTransportAgent;
	private Context mContext;
	private IDeviceInfo mDeviceInfo;
	private boolean mCancelSync;
	private boolean mInBackground;
	
	private IPimInterface2 mPimSrcOperator;
	
	private static final String TAG = "SyncProfile";
	
	private static final String[] PROFILE_PROJECTION = new String[] {
			ProfileTable._ID, // 0
			ProfileTable.ACCOUNT_USER, // 1
			ProfileTable.ACCOUNT_PASSWD, // 2
			ProfileTable.SERVER_URL, // 3
			ProfileTable.APN, // 4
			ProfileTable.CONTACT, // 5
			ProfileTable.EVENT, // 6
			ProfileTable.TASK, // 7
			ProfileTable.PROFILE_NAME,//8
	};

	// private static final int COLUMN_INDEX_PROFILE_ID = 0;
	private static final int COLUMN_INDEX_PROFILE_ACCOUNT_USER = 1;
	private static final int COLUMN_INDEX_PROFILE_ACCOUNT_PASSWD = 2;
	private static final int COLUMN_INDEX_PROFILE_SERVER_URL = 3;
	private static final int COLUMN_INDEX_PROFILE_APN = 4;
	private static final int COLUMN_INDEX_PROFILE_CONTACT = 5;
	private static final int COLUMN_INDEX_PROFILE_EVENT = 6;
	private static final int COLUMN_INDEX_PROFILE_TASK = 7;
	private static final int COLUMN_INDEX_PROFILE_NAME = 8;

	public SyncProfile(long syncID, Context context) {
		this.id = syncID;
		mContext = context;
		long contactId;
		long eventId;
		long taskId;
		CryptUtil crypt = new CryptUtil();

        try{
            Cursor cursor = LocalSyncMLProvider.query(ContentUris.withAppendedId(
                    ProfileTable.CONTENT_URI, id), PROFILE_PROJECTION, null, null,
                    null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = ProfileConfig.getUserName(mContext);
                    passwd = crypt.decrypt(ProfileConfig.getPassword(mContext));
                    url = ProfileConfig.getSyncMLServerUrl(mContext);
                    apn = cursor.getString(COLUMN_INDEX_PROFILE_APN);
                    contactId = cursor.getLong(COLUMN_INDEX_PROFILE_CONTACT);
                    eventId = cursor.getLong(COLUMN_INDEX_PROFILE_EVENT);
                    taskId = cursor.getLong(COLUMN_INDEX_PROFILE_TASK);

                    mSourcesSetting = new StoredataSetting[Define.SYNC_ITEMS_INT_TOTAL];

                    Logger.logD(TAG, "new contacts StoredataSetting");
                    mSourcesSetting[Define.SYNC_ITEMS_INT_CONTACTS] = new StoredataSetting(
                            contactId);
                    Logger.logD(TAG, "new calendar StoredataSetting");
                    mSourcesSetting[Define.SYNC_ITEMS_INT_CALENDAR] = new StoredataSetting(
                            eventId);
                    Logger.logD(TAG, "new task StoredataSetting");
                    mSourcesSetting[Define.SYNC_ITEMS_INT_TASK] = new StoredataSetting(
                            taskId);
                }
                cursor.close();
            }
        }finally {
            LocalSyncMLProvider.close();
        }
	}

	private void initProxy() {
		proxyName = ConnectivityUtil.instance().getProxyHost();
		proxyPort = ConnectivityUtil.instance().getProxyPort();
	}

	// public SyncSourcePreferences source(int item) {
	// return sources[item];
	// }
	
	public String getAccountUserdata(String key){
		//TODO
		return null;
	}

	public String getUserName() {
		return user;
	}

	public String getPassword() {
		return passwd;
	}

	public String getServerUrl() {
		return url;
	}

	public String getApn()
	{
		return apn;
	}

	static public long defaultProfile(Context context) {
		String value = Settings.getSetting(context.getContentResolver(), null,
				Settings.KEY_DEFAULT_PROFILE);
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			return -1;
		}
	}
	
	static public void setDefaultProfile(long id, Context context) {
		Settings.setSetting(context.getContentResolver(), null,
				Settings.KEY_DEFAULT_PROFILE, Long.toString(id));
	}

	public long getId() {
		return id;
	}

	public IDatastore getDatastore(int type, ISyncListener listener) {
		switch (type) {
		case Define.SYNC_ITEMS_INT_CONTACTS:
			return ContactsInterface.datastore(this, mContext
					.getContentResolver(),
					mSourcesSetting[Define.SYNC_ITEMS_INT_CONTACTS], listener);
		case Define.SYNC_ITEMS_INT_CALENDAR:
		default:
			return null;
		}
	}

	public ITransportAgent getTransport() throws DsException {
		checkCancelSync();
		
		if (mTransportAgent == null) {
			initProxy();

			try {
				mTransportAgent = new HttpTransport(this);
			} catch (MalformedURLException e) {
				throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
						DsException.VALUE_MALFORMED_URL);
			}
		}
		return mTransportAgent;
	}

	public void stopSync() {
		if (mTransportAgent != null) {
			mTransportAgent.shutDown();
		}
	}

	public void checkCancelSync() throws DsException {
		if (mCancelSync) {
			throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
					DsException.VALUE_INTERRUPT);
		}
		if (mInBackground) {
		}
	}

	public IDeviceInfo getDeviceInfo() {
		if (mDeviceInfo == null) {
			mDeviceInfo = new DeviceInfo(mContext);
		}
		return mDeviceInfo;
	}

	public void cancelSync() {
		mCancelSync = true;
		if (mTransportAgent != null) {
			mTransportAgent.shutDown();
		}
	}

	public boolean isCanceled() {
		return mCancelSync;
	}

	public void setInBackground(boolean status) {
		mInBackground = status;
	}

	public String proxyName() {
		return proxyName;
	}

	public int proxyPort() {
		return proxyPort;
	}

	public long getSyncSourceId(int source) {
		return mSourcesSetting[source].getId();
	}
	
	public IPimInterface2 getPimSrcOperator(){
	    if (mPimSrcOperator == null){
	        mPimSrcOperator = new ContactsSrcOperator(mContext, AccountAdapter.getBorqsAccount(mContext));
	    }
	    return mPimSrcOperator;
	}
}
