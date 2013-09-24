/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.common.contact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.util.BLog;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.sync.client.vdata.card.ContactProviderOperation;
import com.borqs.sync.ds.datastore.contacts.ContactChangeLog;
import com.borqs.sync.ds.datastore.contacts.ContactChangeLogJsonAdapter;
import com.borqs.sync.store.ContactSyncSettings;
import com.borqs.syncml.ds.protocol.IDataChangeListener;
import com.borqs.syncml.ds.protocol.IDataChangeListener.ContactsChangeData;

public class ContactService {

    private static final String COLUMN_BORQS_PLUS_LABEL = Data.DATA3;

    private static final String COLUMN_BORQS_PLUS_ACTION = Data.DATA4;

    public static final String MIME_TYPE_BORQS_PLUS_OPENFACE = "vnd.android.cursor.item/vnd.borqsplus.openface";

    public static final String MIME_TYPE_BORQS_PLUS_WUTONG = "vnd.android.cursor.item/vnd.borqsplus.profile";

    private static final String SUCCESSED_SYNC_KEY = "sync_success";

    private static final String[] PROJECTION_SOCIAL_CONTACT_MAPPING = new String[] {
            RawContacts._ID, RawContacts.SYNC3
    };

    private Context mContext;

    public ContactService(Context context) {
        mContext = context;
    }

    /**
     * @return the raw_contact id and BorqsID mapping
     */
    public Map<Long, String> getSocialContacts() {
        String accountName = AccountAdapter.getLoginID(mContext);
        Map<Long, String> borqsIDMapping = new HashMap<Long, String>();
        if(TextUtils.isEmpty(accountName)){
            return borqsIDMapping;
        }
        String where = RawContacts.ACCOUNT_NAME + "=? and " + RawContacts.ACCOUNT_TYPE + "=? and "
                + RawContacts.DELETED + "=? and " + RawContacts.SYNC3 + " is not null and "
                + RawContacts.SYNC3 + " !=''";
        Cursor cursor = mContext.getContentResolver().query(RawContacts.CONTENT_URI,
                PROJECTION_SOCIAL_CONTACT_MAPPING, where, new String[] {
                        accountName, AccountAdapter.BORQS_ACCOUNT_TYPE, "0"
                }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String borqsID = cursor.getString(1);
                    borqsIDMapping.put(id, borqsID);
                }
            } finally {
                cursor.close();
            }
        }
        return borqsIDMapping;
    }

    /**
     * check if the sync is success after account login(init sync)
     * @param context
     * @return true if the first sync success
     */
    public static boolean isInitSyncSuccess(Context context) {
        boolean syncSuccess = Boolean.valueOf(AccountAdapter.getUserData(context,
                SUCCESSED_SYNC_KEY));
        BLog.d("is successed first sync : " + syncSuccess);
        return syncSuccess;
    }
    
    
    /**
     * Get the Borqs-ID for a contacts
     * 
     * @param context
     * @param contactId
     *            the raw_contact_id who is the primary kye :_id of Raw_Contacts
     * @return Borqs-ID or null if the contact is not Borqs contact
     */
    public static String getBorqsIdByContact(Context context, long contactId) {
        return queryBorqsIDByContact(context, "" + contactId);
    }
    
    /**
     * 
     * @param context
     * @return SyncLog who contains clientAdd,clientUpdate,clientDelete,serverAdd,serverUpdate,serverDelete,begin,end,result
     */
    public static SyncLog getSyncLog(Context context){
        SyncLog syncLog = new SyncLog();
        
        String logStr;
        try {
            logStr = readLog();
            if(TextUtils.isEmpty(logStr)){
                BLog.d("log string from sdcard is null,read log fail");
                return syncLog;
            }
            JSONObject logJson  = new JSONObject(logStr);
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_ADD_TO_CLIENT)){
                String clientAddStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_ADD_TO_CLIENT);
                if(!TextUtils.isEmpty(clientAddStr)){
                  syncLog.mClientAdds = ContactChangeLogJsonAdapter.toContactsChangeData(clientAddStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_UPDATE_TO_CLIENT)){
                String clientUpdateStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_UPDATE_TO_CLIENT);
                if(!TextUtils.isEmpty(clientUpdateStr)){
                  syncLog.mClientUpdates = ContactChangeLogJsonAdapter.toContactsChangeData(clientUpdateStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_DELETE_FROM_CLIENT)){
                String clientDeleteStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_DELETE_FROM_CLIENT);
                if(!TextUtils.isEmpty(clientDeleteStr)){
                  syncLog.mClientDeletes = ContactChangeLogJsonAdapter.toContactsChangeData(clientDeleteStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_ADD_TO_SERVER)){
                String serverAddStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_ADD_TO_SERVER);
                if(!TextUtils.isEmpty(serverAddStr)){
                  syncLog.mServerAdds = ContactChangeLogJsonAdapter.toContactsChangeData(serverAddStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_DELETE_FROM_SERVER)){
                String serverDeleteStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_DELETE_FROM_SERVER);
                if(!TextUtils.isEmpty(serverDeleteStr)){
                  syncLog.mServerDeletes = ContactChangeLogJsonAdapter.toContactsChangeData(serverDeleteStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_UPDATE_TO_SERVER)){
                String serverUpdateStr = logJson.getString(ContactChangeLog.KEY_CONTACT_SYNC_LOG_UPDATE_TO_SERVER);
                if(!TextUtils.isEmpty(serverUpdateStr)){
                  syncLog.mServerUpdates = ContactChangeLogJsonAdapter.toContactsChangeData(serverUpdateStr);
                }
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_BEGIN)){
                syncLog.mStart = logJson.getLong(ContactChangeLog.KEY_CONTACT_SYNC_LOG_BEGIN);
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_END)){
                syncLog.mEnd = logJson.getLong(ContactChangeLog.KEY_CONTACT_SYNC_LOG_END);
            }
            if(logJson.has(ContactChangeLog.KEY_CONTACT_SYNC_LOG_RESULT)){
                syncLog.mSuccess = logJson.getBoolean(ContactChangeLog.KEY_CONTACT_SYNC_LOG_RESULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return syncLog;
    }
    
    private static String readLog() throws IOException{
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            BLog.d("no sdcard,read synclog fail");
            return null;
        }
        File logFile = new File(ContactChangeLog.CONTACT_SYNC_DIR + File.separator + "synclog");
        if(!logFile.exists()){
            BLog.d("no synclog file,read synclog fail");
            return null;
        }
        FileReader fr = new FileReader(logFile);
        BufferedReader bf = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = bf.readLine())!=null){
            sb.append(line);
        }
        return sb.toString();
    }
    
    public static class SyncLog{
        private List<ContactsChangeData> mClientAdds = new ArrayList<ContactsChangeData>();
        private List<ContactsChangeData> mClientUpdates = new ArrayList<ContactsChangeData>();
        private List<ContactsChangeData> mClientDeletes = new ArrayList<ContactsChangeData>();
        private List<ContactsChangeData> mServerAdds = new ArrayList<ContactsChangeData>();
        private List<ContactsChangeData> mServerUpdates = new ArrayList<ContactsChangeData>();
        private List<ContactsChangeData> mServerDeletes = new ArrayList<ContactsChangeData>();
        
        private long mStart;
        private long mEnd;
        private boolean mSuccess;
        
        public long getSyncStart(){
            return mStart;
        }
        
        public long getSyncEnd(){
            return mEnd;
        }
        
        public boolean isSyncSuccess(){
            return mSuccess;
        }
        
        public List<ContactsChangeData> getClientAdds() {
            return mClientAdds;
        }
        public List<ContactsChangeData> getClientUpdates() {
            return mClientUpdates;
        }
        public List<ContactsChangeData> getClientDeletes() {
            return mClientDeletes;
        }
        public List<ContactsChangeData> getServerAdds() {
            return mServerAdds;
        }
        public List<ContactsChangeData> getServerUpdates() {
            return mServerUpdates;
        }
        public List<ContactsChangeData> getServerDeletes() {
            return mServerDeletes;
        }
    }

    // For Contacts provider
    private static final String ACCOUNT_COLUMN_ID = RawContacts._ID;
    private static final String ACCOUNT_COLUMN_CONTACT_ID = RawContacts._ID;
    private static final String ACCOUNT_COLUMN_BORQS_ID = RawContacts.SYNC3;
    private static final String[] ACCOUNT_BORQS_ID_PROJECTION = new String[] {
            ACCOUNT_COLUMN_ID, ACCOUNT_COLUMN_CONTACT_ID,
            ACCOUNT_COLUMN_BORQS_ID };

    private static String queryBorqsIDByContact(Context context,
            String contactId) {
        if (TextUtils.isEmpty(contactId)) {
            return null;
        }

        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                        Long.valueOf(contactId)), ACCOUNT_BORQS_ID_PROJECTION,
                null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return cursor.getString(cursor
                            .getColumnIndexOrThrow(ACCOUNT_COLUMN_BORQS_ID));
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    @Deprecated
    public class BorqsPlus {

        public BorqsPlus(String label, String action) {
            this.label = label;
            this.action = action;
        }

        /**
         * quick action label :the text display on the quick item
         */
        private String label;

        /**
         * action name:you can know what you click
         */
        private String action;

        @Override
        public String toString() {
            return "label:" + label + ",action:" + action;
        }
    }

    /**
     * set borqs contact plus data,like openface,wutong
     * 
     * @param context
     * @param mimeType @com.borqs.common.contact.ContactService
     * @param borqsPlus structure list for quick action's label and name
     */
    @Deprecated
    public void setBorqsContactPlus(Context context, String mimeType, List<BorqsPlus> borqsPlus) {

        List<Long> currentPlusContactIds;
        try {
            currentPlusContactIds = getCurrentPlusContactIds(context, mimeType);
        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
        BLog.d("setBorqsContactPlus->,currentPlusContactIds:" + currentPlusContactIds.toString());
        Set<Long> existedPlusContact = getExistedPlusContactIds(context.getContentResolver(),
                mimeType);
        BLog.d("setBorqsContactPlus->,existedPlusContact:" + existedPlusContact.toString());

        // check if the borqsplus is changed
        boolean plusChanged = checkBorqsPlusChanged(context, mimeType, borqsPlus,
                currentPlusContactIds, existedPlusContact);
        if (plusChanged) {
            BLog.d("setBorqsContactPlus->,borqsplus is changed,so we should refresh the borqsplus");
            // 1.delete all
            deleteBorqsPlus(null, mimeType, context);
            // 2.insert by id
            insertBorqsPlus(currentPlusContactIds, mimeType, borqsPlus, context);
        } else {
            BLog.d("setBorqsContactPlus->,borqsplus not change,so we delete not borqs plus and insert new");
            // 1.find out these data whose raw_contact is not borqs contacts
            deleteNotBorqsContactPlus(context, mimeType, currentPlusContactIds, existedPlusContact);

            // 2.insert new plus
            insertNewBorqsContactPlus(context, mimeType, borqsPlus, currentPlusContactIds,
                    existedPlusContact);
        }
    }

    private boolean checkBorqsPlusChanged(Context context, String mimeType,
            List<BorqsPlus> borqsPlus, List<Long> currentPlusContactIds,
            Set<Long> existedPlusContact) {
        long sameId = 0;
        for (Long currentId : currentPlusContactIds) {
            if (existedPlusContact.contains(currentId)) {
                sameId = currentId;
            }
        }
        BLog.d("checkBorqsPlusChanged->,sameId:" + sameId);

        // check label and action are same
        String originalPlus = getBorqsContactPlusByMimetype(context, mimeType, sameId);

        return !borqsPlus.toString().equals(originalPlus);
    }

    private String getBorqsContactPlusByMimetype(Context context, String mimeType, long id) {
        List<BorqsPlus> pluses = new ArrayList<ContactService.BorqsPlus>();
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                COLUMN_BORQS_PLUS_LABEL, COLUMN_BORQS_PLUS_ACTION
        }, Data.MIMETYPE + "=? AND " + RawContacts._ID + "=?", new String[] {
                mimeType, String.valueOf(id)
        }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String label = cursor.getString(0);
                    String action = cursor.getString(0);
                    pluses.add(new BorqsPlus(label, action));
                }
            } finally {
                cursor.close();
            }
        }
        return pluses.toString();
    }

    private void insertNewBorqsContactPlus(Context context, String mimeType,
            List<BorqsPlus> borqsPlus, List<Long> currentPlusContactIds,
            Set<Long> existedPlusContact) {
        List<Long> newIds = new ArrayList<Long>();
        for (Long currentId : currentPlusContactIds) {
            if (!existedPlusContact.contains(currentId)) {
                newIds.add(currentId);
            }
        }
        BLog.d("insertNewBorqsContactPlus->,newIds:" + newIds.toString());

        insertBorqsPlus(newIds, mimeType, borqsPlus, context);
    }

    private void insertBorqsPlus(List<Long> newIds, String mimeType, List<BorqsPlus> borqsPlus,
            Context context) {
        ArrayList<ContentProviderOperation> cpoList = new ArrayList<ContentProviderOperation>();
        try {
            for (Long newId : newIds) {
                writeQuickData(context, mimeType, newId, borqsPlus, cpoList);
                // the max batch operation is 500
                if (cpoList.size() + borqsPlus.size() >= ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT) {
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoList);
                    cpoList.clear();
                }
            }
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void deleteNotBorqsContactPlus(Context context, String mimeType,
            List<Long> currentPlusContactIds, Set<Long> existedPlusContact) {
        // 1.find out these data whose raw_contact is not borqs contacts
        List<Long> willDelete = new ArrayList<Long>();
        for (Long existedId : existedPlusContact) {
            if (!currentPlusContactIds.contains(existedId)) {
                willDelete.add(existedId);
            }
        }

        deleteBorqsPlus(willDelete, mimeType, context);
    }

    private void deleteBorqsPlus(List<Long> willDelete, String mimeType, Context context) {
        String where = null;
        if (willDelete == null || willDelete.isEmpty()) {
            // delete all
            where = Data.MIMETYPE + "=?";
        } else {
            // delete specified plus
            String idWhere = willDelete.toString().replace("[", "(").replace("]", ")");
            BLog.d("deleteBorqsPlus_>id will be delete:" + idWhere);
            where = Data.MIMETYPE + "=? AND " + Data.RAW_CONTACT_ID + " in " + idWhere;
        }
        ArrayList<ContentProviderOperation> cpos = new ArrayList<ContentProviderOperation>();
        cpos.add(ContentProviderOperation
                .newDelete(
                        Data.CONTENT_URI
                                .buildUpon()
                                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                                        "true").build()).withSelection(where, new String[] {
                    mimeType
                }).build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpos);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private Set<Long> getExistedPlusContactIds(ContentResolver resolver, String mimeType) {
        Set<Long> contactIds = new HashSet<Long>();
        Cursor cursor = resolver.query(Data.CONTENT_URI, new String[] {
            Data.RAW_CONTACT_ID
        }, Data.MIMETYPE + "=?", new String[] {
            mimeType
        }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    contactIds.add(cursor.getLong(0));
                }
            } finally {
                cursor.close();
            }
        }
        return contactIds;
    }

    private void writeQuickData(Context context, String mimeType, long contactId,
            List<BorqsPlus> borqsPuls, ArrayList<ContentProviderOperation> cpoList) {

        Builder builder = null;

        for (BorqsPlus plus : borqsPuls) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build());
            builder.withValue(Data.MIMETYPE, mimeType);
            builder.withValue(COLUMN_BORQS_PLUS_LABEL, plus.label);
            builder.withValue(COLUMN_BORQS_PLUS_ACTION, plus.action);
            builder.withValue(Data.RAW_CONTACT_ID, contactId);
            cpoList.add(builder.build());
        }
    }

    // look up the plus contactIds whose sync3(for openface) or sync4(for
    // wutong) is not null
    private List<Long> getCurrentPlusContactIds(Context context, String mimeType) throws Exception{
        String columnMarkPlus = null;
        if (MIME_TYPE_BORQS_PLUS_OPENFACE.equals(mimeType)) {
            columnMarkPlus = RawContacts.SYNC3;
        } else if (MIME_TYPE_BORQS_PLUS_WUTONG.equals(mimeType)) {
            columnMarkPlus = RawContacts.SYNC4;
        } else {
            throw new IllegalArgumentException("unknown mimetype");
        }
        List<Long> friendContactIds = new ArrayList<Long>();
        String accountName = AccountAdapter.getLoginID(context);
        String accountType = AccountAdapter.BORQS_ACCOUNT_TYPE;
        if(TextUtils.isEmpty(accountName)){
            throw new IllegalArgumentException("invalid account,the name is null");
        }
        Cursor cursor = context.getContentResolver().query(
                RawContacts.CONTENT_URI,
                new String[] {
                    RawContacts._ID
                },
                RawContacts.ACCOUNT_NAME + "=?" + " AND " + RawContacts.ACCOUNT_TYPE + " =? AND "
                        + RawContacts.DELETED + "=? AND (" + columnMarkPlus + " is not null AND "
                        + columnMarkPlus + "!='')", new String[] {
                        accountName, accountType, "0"
                }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    friendContactIds.add(cursor.getLong(0));
                }
            } finally {
                cursor.close();
            }
        }
        return friendContactIds;
    }

}
