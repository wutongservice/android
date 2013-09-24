
package com.borqs.sync.ds.datastore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.borqs.account.login.util.BLog;
import com.borqs.common.account.AccountAdapter;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.provider.SyncMLDb.SyncLog;
import com.borqs.sync.service.LocalSyncMLProvider;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.ISyncLogInterface;

/**
 * only for sync fail log FROM NOW(2013-1-21)
 * 
 * @author b211
 */
public class DatastoreSyncLog implements ISyncLogInterface {
    
    private static final String TAG = "DatastoreSyncLog";
    
    private static final int DEFAULT_FAIL_TIMES = 5;

    private long sourceId;
    private int syncMode;
    private int result;
    private long start;
    private long end;

    private int clientAdd;
    private int clientAddFail;
    private int clientModify;
    private int clientModifyFail;
    private int clientDel;
    private int clientDelFail;

    private int serverAdd;
    private int serverAddFail;
    private int serverModify;
    private int serverModifyFail;
    private int serverDel;
    private int serverDelFail;

    private ContentResolver mResolver;

    private ISyncFailListener mSyncFailListener;

    public interface ISyncFailListener {
        public void onSyncFailManyTimes();
    }

    public DatastoreSyncLog(long sourceId, ContentResolver resolver, ISyncFailListener listener) {
        this.sourceId = sourceId;
        mResolver = resolver;
        mSyncFailListener = listener;
    }

    public void clientAdd(boolean result) {
        if (result) {
            clientAdd++;
        } else {
            clientAddFail++;
        }
    }

    public void clientDel(boolean result) {
        if (result) {
            clientDel++;
        } else {
            clientDelFail++;
        }
    }

    public void clientDelItems(boolean result, int items) {
        if (result) {
            clientDel = clientDel + items;
        } else {
            clientDelFail = clientDelFail + items;
        }
    }

    public void clientModify(boolean result) {
        if (result) {
            clientModify++;
        } else {
            clientModifyFail++;
        }
    }

    public void serverAdd(boolean result) {
        if (result) {
            serverAdd++;
        } else {
            serverAddFail++;
        }
    }

    public void serverDel(boolean result) {
        if (result) {
            serverDel++;
        } else {
            serverDelFail++;
        }
    }

    public void serverModify(boolean result) {
        if (result) {
            serverModify++;
        } else {
            serverModifyFail++;
        }
    }

    public void syncBegin(int mode) {
        syncMode = mode;
        Calendar cal = Calendar.getInstance();
        start = cal.getTimeInMillis();
    }

    public void syncEnd(int status) {
        Logger.logD(TAG,"sync end1");
        //check status
        if (status != IDatastore.END_SYNC_FAILED) {
            Logger.logD(TAG,"sync not failed,maybe success or canceled,we do not save the log");
            return;
        }
        Logger.logD(TAG,"sync end2");
        //check sync settings
        Account acc = AccountAdapter.getBorqsAccount(ApplicationGlobals.getContext());
        if(acc != null && !SyncHelper.isContactAutoSync(acc)){
            Logger.logD(TAG,"contact is not auto sync,we do not save the fail log");
            return ;
        }
        Logger.logD(TAG,"sync end3");
        
        result = status;
        Calendar cal = Calendar.getInstance();
        end = cal.getTimeInMillis();

        if (needNotifyUser(end)) {
            Logger.logD(TAG,"sync end4");
            // notify user
            mSyncFailListener.onSyncFailManyTimes();
            // clear the fail log,then we will re-compute the fail times
            try{
                LocalSyncMLProvider.delete(SyncLog.CONTENT_URI, SyncLog.SOURCE + "=?", new String[] {
                        String.valueOf(sourceId)
                });
            }finally{
                LocalSyncMLProvider.close();
            }
            return;
        }
        Logger.logD(TAG,"sync end5");
        ContentValues values = new ContentValues(17);
        values.put(SyncLog.SOURCE, sourceId);
        values.put(SyncLog.SYNC_MODE, syncMode);
        values.put(SyncLog.RESULT, result);
        values.put(SyncLog.START, start);
        values.put(SyncLog.END, end);
        values.put(SyncLog.CLIENT_ADD, clientAdd);
        values.put(SyncLog.CLIENT_ADD_FAIL, clientAddFail);
        values.put(SyncLog.CLIENT_MODIFY, clientModify);
        values.put(SyncLog.CLIENT_MODIFY_FAIL, clientModifyFail);
        values.put(SyncLog.CLIENT_DEL, clientDel);
        values.put(SyncLog.CLIENT_DEL_FAIL, clientDelFail);
        values.put(SyncLog.SERVER_ADD, serverAdd);
        values.put(SyncLog.SERVER_ADD_FAIL, serverAddFail);
        values.put(SyncLog.SERVER_MODIFY, serverModify);
        values.put(SyncLog.SERVER_MODIFY_FAIL, serverModifyFail);
        values.put(SyncLog.SERVER_DEL, serverDel);
        values.put(SyncLog.SERVER_DEL_FAIL, serverDelFail);

        try {
            Logger.logD(TAG,"sync end6");
            LocalSyncMLProvider.insert(SyncLog.CONTENT_URI, values);
        } finally {
            Logger.logD(TAG,"sync end7");
            LocalSyncMLProvider.close();
        }
    }

    private boolean needNotifyUser(long end) {
        List<Long> failTimes = existedFailTime();
        if (failTimes.size() > 0 && failTimes.size() == (DEFAULT_FAIL_TIMES - 1)) {
            long min = failTimes.get(0);
            for (Long logTime : failTimes) {
                if (logTime < min) {
                    min = logTime;
                }
            }
            if (end - min <= 12*60 * 60 * 1000/*12 hours*/) {
                return true;
            }
        }
        return false;
    }

    private List<Long> existedFailTime() {
        List<Long> logs = new ArrayList<Long>();
        Cursor cursor = LocalSyncMLProvider.query(SyncLog.CONTENT_URI, null, SyncLog.SOURCE
                + "=? AND " + SyncLog.RESULT + "=?", new String[] {
                String.valueOf(sourceId), String.valueOf(IDatastore.END_SYNC_FAILED)
        }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    logs.add(cursor.getLong(cursor.getColumnIndexOrThrow(SyncLog.END)));
                }
            } finally {
                cursor.close();
            }
        }
        return logs;
    }
}
