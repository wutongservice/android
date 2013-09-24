/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.ds.datastore.contacts;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.borqs.common.util.BLog;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.syncml.ds.protocol.IDataChangeListener;
import com.borqs.syncml.ds.protocol.IDataChangeListener.ContactsChangeData;

public class ContactChangeLog {
    //for contact sync log
    public static final String KEY_CONTACT_SYNC_LOG_BEGIN = "sync_log_sync_begin";
    public static final String KEY_CONTACT_SYNC_LOG_END = "sync_log_sync_end";
    public static final String KEY_CONTACT_SYNC_LOG_RESULT = "sync_log_sync_result";
    public static final String KEY_CONTACT_SYNC_LOG_ADD_TO_CLIENT = "sync_log_add_to_client"; 
    public static final String KEY_CONTACT_SYNC_LOG_UPDATE_TO_CLIENT = "sync_log_update_to_client"; 
    public static final String KEY_CONTACT_SYNC_LOG_DELETE_FROM_CLIENT = "sync_log_delete_from_client";
    public static final String KEY_CONTACT_SYNC_LOG_ADD_TO_SERVER = "sync_log_add_to_server";
    public static final String KEY_CONTACT_SYNC_LOG_UPDATE_TO_SERVER = "sync_log_update_to_server";
    public static final String KEY_CONTACT_SYNC_LOG_DELETE_FROM_SERVER = "sync_log_delete_from_server";
    
    public static final String CONTACT_SYNC_DIR = Environment.getExternalStorageDirectory() + File.separator + "com" + File.separator + "borqs" + File.separator + "sync";
    
    private long mStart;
    private long mEnd;
    private boolean mSuccess;
    
    private List<ContactsChangeData> mClientAdds = new ArrayList<IDataChangeListener.ContactsChangeData>();
    private List<ContactsChangeData> mClientUpdates = new ArrayList<IDataChangeListener.ContactsChangeData>();
    private List<ContactsChangeData> mClientDeletes = new ArrayList<IDataChangeListener.ContactsChangeData>();
    private List<ContactsChangeData> mServerAdds = new ArrayList<IDataChangeListener.ContactsChangeData>();
    private List<ContactsChangeData> mServerUpdates = new ArrayList<IDataChangeListener.ContactsChangeData>();
    private List<ContactsChangeData> mServerDeletes = new ArrayList<IDataChangeListener.ContactsChangeData>();

    public void begin() {
        mStart = System.currentTimeMillis();
    }

    public void end(boolean isSuccess) {
        mSuccess = isSuccess;
        mEnd = System.currentTimeMillis();
        try {
            String logJson = composeJsonLog();
            saveLog(logJson);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClientAdd(ContactsChangeData ccd) {
        BLog.d("add to client,name is :" + ccd.name);
        mClientAdds.add(ccd);
    }

    public void onClientUpate(ContactsChangeData ccd) {
        BLog.d("update to client,name is :" + ccd.name);
        mClientUpdates.add(ccd);
    }

    public void onClientDelete(ContactsChangeData ccd) {
        BLog.d("delete from client,name is :" + ccd.name);
        mClientDeletes.add(ccd);
    }

    public void onServerAdd(ContactsChangeData ccd) {
        BLog.d("add to server,name is :" + ccd.name);
        mServerAdds.add(ccd);
    }

    public void onServerUpdate(ContactsChangeData ccd) {
        BLog.d("update to server,name is :" + ccd.name);
        mServerUpdates.add(ccd);
    }

    public void onServerDelete(ContactsChangeData ccd) {
        BLog.d("delete from server,name is :" + ccd.name);
        mServerDeletes.add(ccd);
    }
    
    private String composeJsonLog() throws JSONException{
        JSONObject logJson = new JSONObject();
        logJson.put(KEY_CONTACT_SYNC_LOG_BEGIN, mStart);
        logJson.put(KEY_CONTACT_SYNC_LOG_END, mEnd);
        logJson.put(KEY_CONTACT_SYNC_LOG_RESULT, mSuccess);
        String clientAddStr = ContactChangeLogJsonAdapter.toJson(mClientAdds);
        logJson.put(KEY_CONTACT_SYNC_LOG_ADD_TO_CLIENT, clientAddStr);
        String clientUpdateStr = ContactChangeLogJsonAdapter.toJson(mClientUpdates);
        logJson.put(KEY_CONTACT_SYNC_LOG_UPDATE_TO_CLIENT, clientUpdateStr);
        String clientDeleteStr = ContactChangeLogJsonAdapter.toJson(mClientDeletes);
        logJson.put(KEY_CONTACT_SYNC_LOG_DELETE_FROM_CLIENT, clientDeleteStr);
        String serverAddStr = ContactChangeLogJsonAdapter.toJson(mServerAdds);
        logJson.put(KEY_CONTACT_SYNC_LOG_ADD_TO_SERVER, serverAddStr);
        String serverUpdateStr = ContactChangeLogJsonAdapter.toJson(mServerUpdates);
        logJson.put(KEY_CONTACT_SYNC_LOG_UPDATE_TO_SERVER, serverUpdateStr);
        String serverDeleteStr = ContactChangeLogJsonAdapter.toJson(mServerDeletes);
        logJson.put(KEY_CONTACT_SYNC_LOG_DELETE_FROM_SERVER, serverDeleteStr);
        return logJson.toString();
    }
    
    private void saveLog(String logJson) throws Exception{
        //save to sdcard
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            throw new Exception("no sdcard");
        }
        File dir = new File(CONTACT_SYNC_DIR);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File logFile = new File(CONTACT_SYNC_DIR + File.separator + "synclog");
        if(!logFile.exists()){
            logFile.createNewFile();
        }
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(logFile);
            fos.write(logJson.getBytes());
            fos.flush();
        }finally{
            if(fos != null){
                fos.close();
            }
        }
    }

}
