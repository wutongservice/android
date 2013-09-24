package com.borqs.sync.ds.config;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.provider.SyncMLDb.SyncSourceTable;
import com.borqs.sync.service.LocalSyncMLProvider;


public class StoredataSetting {
	private long id;
	private String remoteUri;
	private String name;
	private String type;
	private static final String TAG = "StoredataSetting";

	static final String[] SYNC_SOURCE_PROJECTION = new String[] {
			SyncSourceTable._ID, // 0
			SyncSourceTable.REMOTE_URI, // 1
			SyncSourceTable.IS_ACTIVE, // 2
			SyncSourceTable.ANCHOR_NEXT, // 3
			SyncSourceTable.ANCHOR_LAST, // 4
			SyncSourceTable.NAME, // 5
			SyncSourceTable.TYPE, // 6
			SyncSourceTable.ENCODING, // 7
			// SyncSourceTable.SYNC_MODE, //8
			SyncSourceTable.LOCAL_URI, // 8
	};

	// private static final int COLUMN_INDEX_ID = 0;
	private static final int COLUMN_INDEX_REMOTE_URI = 1;
	private static final int COLUMN_INDEX_IS_ACTIVE = 2;
	private static final int COLUMN_INDEX_ANCHOR_NEXT = 3;
	private static final int COLUMN_INDEX_ANCHOR_LAST = 4;
	private static final int COLUMN_INDEX_NAME = 5;
	private static final int COLUMN_INDEX_TYPE = 6;
	private static final int COLUMN_INDEX_ENCODING = 7;
	// private static final int COLUMN_INDEX_SYNC_MODE = 8;
	private static final int COLUMN_INDEX_LOCAL_URI = 8;

	Uri mUri;

	public StoredataSetting(long id) {
		this.id = id;
		mUri = ContentUris.withAppendedId(SyncSourceTable.CONTENT_URI, id);
		update();
	}

	public void update() {
        try{
            Cursor cursor = LocalSyncMLProvider.query(mUri, SYNC_SOURCE_PROJECTION, null,
                    null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    remoteUri = cursor.getString(COLUMN_INDEX_REMOTE_URI);
                    name = cursor.getString(COLUMN_INDEX_NAME);
                    type = cursor.getString(COLUMN_INDEX_TYPE);
                }
                cursor.close();
            }
        }finally {
            LocalSyncMLProvider.close();
        }
	}

	public String getRemoteUri() {
		return remoteUri;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public long getId() {
		return id;
	}

	
	public static void clearSyncAnchor() {
        SyncDeviceContext  device = new SyncDeviceContext(ApplicationGlobals.getContext());
        device.setLastSuccessSyncAnchor(0);
	}
}
