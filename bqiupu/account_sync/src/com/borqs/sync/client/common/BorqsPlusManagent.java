/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.common.util.BLog;
import com.borqs.common.util.HttpLog;
import com.borqs.sync.client.common.BorqsPlusStruct.PlusEntry;
import com.borqs.sync.client.transport.ContactServiceClient;
import com.borqs.sync.client.vdata.card.ContactProviderOperation;

/**
 * borqs plus managent,like set,parse
 * 
 * @author b211
 */
public class BorqsPlusManagent {

    private static final String COLUMN_BORQS_PLUS_LABEL = Data.DATA3;

    private static final String COLUMN_BORQS_PLUS_ACTION = Data.DATA4;

    // the account mimetype should be configure on client
    // (contact_contacts.xml,bind with Account)
    // and we can configure it into borqs_plus.xml,but should same with client
    public static final String WUTONG_ACCOUNT_PLUS_MIME_TYPE = "vnd.android.cursor.item/vnd.borqsplus.profile";

    public static final String OPENFACE_ACCOUNT_PLUS_MIME_TYPE = "vnd.android.cursor.item/vnd.borqsplus.openface";

    public static final String BORQS_PLUS_FILE_NAME = "borqs_plus.xml";

    private Context mContext;

    public BorqsPlusManagent(Context context) {
        mContext = context;

    }

    private void updateBorqsPlusFromServer() {
        ContactServiceClient csc = new ContactServiceClient(mContext, SimpleHttpClient.get());
        try {
//            String plusConfig = csc.queryConfFile(BORQS_PLUS_FILE_NAME);
            InputStream is = csc.queryStaticConfFile(BORQS_PLUS_FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }
            is.close();
            HttpLog.d("Response: " + buf.toString());
            
            String plusConfig = buf.toString();
            if (!TextUtils.isEmpty(plusConfig)) {
                // save the configure file into internal
                writePlusConfigToLocal(plusConfig);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePlusConfigToLocal(String plusConfigStr) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(BORQS_PLUS_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(plusConfigStr.toString().getBytes("UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
    }

    private InputStream loadBorqsPlusByLocal() {
        try {
            InputStream is = mContext.openFileInput(BORQS_PLUS_FILE_NAME);
            return is;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get borqs plus
     * 
     * @return
     * @throws Exception
     */
    public List<BorqsPlusStruct> getBorqsPlus() throws Exception {
        InputStream is = loadBorqsPlusByLocal();

        if (is == null) {
            throw new IllegalAccessException("load borqsplus from local error!");
        }
        return parseBorqsPlus(is);
    }

    /**
     * set borqs plus by plus configuration
     * 
     * @param context
     */
    public void setBorqsPlus() throws Exception {
        // 1.update borqs_plus configuration from server to local
        updateBorqsPlusFromServer();
        // 2.read the configuration from local.
        InputStream is = loadBorqsPlusByLocal();

        if (is == null) {
            throw new IllegalAccessException("load borqsplus from local error!");
        }
        List<BorqsPlusStruct> bpses = parseBorqsPlus(is);
        if (bpses == null || bpses.isEmpty()) {
            throw new IllegalArgumentException(
                    "set plus error,the BorqsPlusStruct is null or empty");
        }
        String plusEntryType = BorqsPlusStruct.ENTRY_TYPE_EN;
        String systemLanguage = Locale.getDefault().getLanguage();

        if (Locale.CHINESE.toString().equals(systemLanguage)) {
            // zh
            plusEntryType = BorqsPlusStruct.ENTRY_TYPE_ZH;
        } else {
            // treat it as en
            plusEntryType = BorqsPlusStruct.ENTRY_TYPE_EN;
        }

        for (BorqsPlusStruct bps : bpses) {
            Map<String, List<PlusEntry>> entryMap = bps.getEntryList();
            List<PlusEntry> entryList = entryMap.get(plusEntryType);
            if (entryList != null) {
                List<PlusEntry> pluses = new ArrayList<PlusEntry>();
                for (PlusEntry plusEntry : entryList) {
                    pluses.add(bps.new PlusEntry(plusEntry.mLabel, plusEntry.mAction));
                }
                setBorqsContactPlus(mContext, bps.getMimeType(), pluses);
            }
        }
    }

    /**
     * parse borqsPlus by borqs plus file
     * 
     * @param is the file inputstream who is the borqs plus configuration file.
     * @return BorqsPlusStruct
     * @throws Exception
     */
    private static List<BorqsPlusStruct> parseBorqsPlus(InputStream is) throws Exception {
        return BorqsPlusParser.parseBorqsPlus(is);
    }

    /**
     * set borqs contact plus data,like openface,wutong
     * 
     * @param context
     * @param mimeType @com.borqs.sync.common.contact.ContactService
     * @param borqsPlus structure list for quick action's label and name
     */
    private void setBorqsContactPlus(Context context, String mimeType, List<PlusEntry> borqsPlus) {
        try {
            List<Long> currentPlusContactIds = getCurrentPlusContactIds(context, mimeType);
            BLog.d("setBorqsContactPlus->,currentPlusContactIds:"
                    + currentPlusContactIds.toString());
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
                deleteNotBorqsContactPlus(context, mimeType, currentPlusContactIds,
                        existedPlusContact);

                // 2.insert new plus
                insertNewBorqsContactPlus(context, mimeType, borqsPlus, currentPlusContactIds,
                        existedPlusContact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkBorqsPlusChanged(Context context, String mimeType,
            List<PlusEntry> borqsPlus, List<Long> currentPlusContactIds,
            Set<Long> existedPlusContact) {
        long sameId = 0;
        for (Long currentId : currentPlusContactIds) {
            if (existedPlusContact.contains(currentId)) {
                sameId = currentId;
            }
        }
        BLog.d("checkBorqsPlusChanged->,sameId:" + sameId);
        
        if(sameId <= 0){
            return false;
        }

        // check label and action are same
        String originalPlus = getBorqsContactPlusByMimetype(context, mimeType, sameId);

        return !borqsPlus.toString().equals(originalPlus);
    }

    private String getBorqsContactPlusByMimetype(Context context, String mimeType, long id) {
        List<PlusEntry> pluses = new ArrayList<PlusEntry>();
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                COLUMN_BORQS_PLUS_LABEL, COLUMN_BORQS_PLUS_ACTION
        }, Data.MIMETYPE + "=? AND " + RawContacts._ID + "=?", new String[] {
                mimeType, String.valueOf(id)
        }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    BorqsPlusStruct bps = new BorqsPlusStruct();
                    String label = cursor.getString(0);
                    String action = cursor.getString(0);
                    pluses.add(bps.new PlusEntry(label, action));
                }
            } finally {
                cursor.close();
            }
        }
        return pluses.toString();
    }

    private void insertNewBorqsContactPlus(Context context, String mimeType,
            List<PlusEntry> borqsPlus, List<Long> currentPlusContactIds,
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

    private void insertBorqsPlus(List<Long> newIds, String mimeType, List<PlusEntry> borqsPlus,
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
            List<PlusEntry> borqsPuls, ArrayList<ContentProviderOperation> cpoList) {

        Builder builder = null;

        for (PlusEntry plus : borqsPuls) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build());
            builder.withValue(Data.MIMETYPE, mimeType);
            builder.withValue(COLUMN_BORQS_PLUS_LABEL, plus.mLabel);
            builder.withValue(COLUMN_BORQS_PLUS_ACTION, plus.mAction);
            builder.withValue(Data.RAW_CONTACT_ID, contactId);
            cpoList.add(builder.build());
        }
    }

    // look up the plus contactIds whose sync3(for openface) or sync4(for
    // wutong) is not null
    private List<Long> getCurrentPlusContactIds(Context context, String mimeType)
            throws IllegalArgumentException {
        String columnMarkPlus = null;
        if (OPENFACE_ACCOUNT_PLUS_MIME_TYPE.equals(mimeType)) {
            columnMarkPlus = RawContacts.SYNC3;
        } else if (WUTONG_ACCOUNT_PLUS_MIME_TYPE.equals(mimeType)) {
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
