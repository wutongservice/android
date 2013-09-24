/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.profile;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;

import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.util.BLog;
import com.borqs.common.account.AccountException;
import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.jcontact.JContact;
import com.borqs.profile.model.ContactProfileStruct;
import com.borqs.sync.client.vdata.card.JContactConverter;

/**
 * service used to sync account profile data
 * this class depends on login.AccountService, must sure account have logged in
 * @author linxh
 *
 */
public class ProfileSyncService extends Service {
    private static final String ACTION_SYNC_PROFILE = "borqs.sync.action.SYNC_PROFILE";
    private static final String ACTION_SYNC_MODE_UP = "mode_up";
    
    private boolean mSyncUp = false; // client sync data to server
    private SyncProfileTask mSyncTask = null;
    private AccountService mAccountService = null;
    private AccountProfileInfo mProfile;    
    
    public static void actionSyncProfile(Context context){ 
            Intent i = new Intent();
            i.setClass(context, ProfileSyncService.class);
            i.setAction(ACTION_SYNC_PROFILE);
            i.putExtra(ACTION_SYNC_MODE_UP, false);
            context.startService(i);
    }
    
    public static void actionSyncUpProfile(Context context){
        Intent i = new Intent();
        i.setClass(context, ProfileSyncService.class);
        i.putExtra(ACTION_SYNC_MODE_UP, true);
        i.setAction(ACTION_SYNC_PROFILE);
        context.startService(i);
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        //ApplicationGlobals.setContext(this);
        mAccountService = new AccountService(this);        
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);        
        String task = intent.getAction();   
        if(taskNotStart(task)){
           mSyncUp = intent.getBooleanExtra(ACTION_SYNC_MODE_UP, false);
           startTask(startId);
        }        
        return START_NOT_STICKY;
    }
     
    private boolean taskNotStart(String task){
        return ACTION_SYNC_PROFILE.equals(task) && (mSyncTask==null);
    }
    
    private void startTask(int taskId){
        mProfile = AccountProfileInfo.create(this);
        mSyncTask = new SyncProfileTask(taskId);
        mSyncTask.execute();
    }
    
    private class SyncProfileTask extends AsyncTask<Void, Void, Void>{
        private int mServiceId;
        
        public SyncProfileTask(int serviceId){
            mServiceId = serviceId;
        }
           
        @Override
        protected void onPreExecute() {
            mAccountService.loadData(null);
        }
        
        @Override
        protected void onPostExecute(Void result) {
            mSyncTask = null;
            //ContactLock.unLockStatus(ContactLock.TOKEN_SYNC_PROFILE);
            stopSelfResult(mServiceId);
        }

        
        @Override
        protected Void doInBackground(Void... params) {  
            /*if (!ContactLock.lockStatus(ContactLock.TOKEN_SYNC_PROFILE)){
                BLog.d("sync profile lock error");
                return null;
            }*/
            
            String uid = mAccountService.getUserId();
            String ticket = mAccountService.getSessionId();            
            if(TextUtils.isEmpty(uid) || TextUtils.isEmpty(ticket)){
                BLog.d("sync profile error, not login");
                return null;
            }        
            
            if (mSyncUp){
                //because sync up was caused by db changed, record the change time
                mProfile.setModifyTime();
            }
            
            SyncProfileClient client = new SyncProfileClient(ProfileSyncService.this, SimpleHttpClient.get());            
			try {	
			    String resultInfo = null;
			    if (!mProfile.hasData()){
			        BLog.d("get data from server");
			        resultInfo = client.getProfile(uid, ticket);                                        
			    } else if (mProfile.isProfileChanged()){
			        BLog.d("sync data to server");
                    resultInfo = client.syncProfile(uid, ticket, getUpdateInfo());
                    BLog.d("sync data result:" + resultInfo);
			    } else {
			        BLog.d("get server latest data");         
			        resultInfo = client.getProfile(uid, ticket);			        
			    }
			    
			    if (!TextUtils.isEmpty(resultInfo)){
			        updateAccountProfile(resultInfo);
			    } else {
			        BLog.d("sync data error: server return null string");
			    }
			} catch (JSONException e) {
				e.printStackTrace();
				BLog.d("syncprofile failed 1:" + e.getMessage());
			} catch (IOException e) {
                e.printStackTrace();
                BLog.d("syncprofile faild 2:" + e.getMessage());
            } catch (AccountException e) {
				e.printStackTrace();
				BLog.d("syncprofile faild 3:" + e.getMessage());
			} catch (Exception e){
			    e.printStackTrace();
                BLog.d("syncprofile faild 4, unknown:" + e.getMessage());
			}
            
            return null;
        }        
    };
        
    private String getUpdateInfo() throws JSONException{
        String updInfo = null;             
        ContactProfileStruct me = mProfile.toProfileStruct();
        if (me != null){
            JContactConverter converter = new JContactConverter();
            updInfo = converter.convertToJson(me);
            
            //TODO: if no modify time(or time = 0), 
            // server will regard the client didn't change the data
            JSONObject json = new JSONObject(updInfo);            
            json.put("modify_time", mProfile.getLastModifyTime());            
            updInfo = json.toString();
            //BLog.d("syncprofile update params:" + updInfo);
        }
        
        return updInfo;
    }
    	
	private void updateAccountProfile(String info) throws JSONException{
	    JSONObject json = new JSONObject(info);
	    if (!isRightResult(json)){
	        BLog.d("syncprofile result error:" + info);
	        return;
	    }
	    
	    String strData = json.optString("data");
	    if (TextUtils.isEmpty(strData)) {	        
	        BLog.d("syncprofile result ok, don't need updata in client");
	        mProfile.cleanDirtyMark();
	        return;
	    }
	    
	    JSONObject jData = new JSONObject(strData);	    
	    JContactConverter converter = new JContactConverter();
	    JContact contact = JContact.fromJsonString(jData.toString());
	    
        ContactProfileStruct me = new ContactProfileStruct();
        converter.convertToStruct(contact, me);
        
        String uid = mAccountService.getLoginId();
        if (!TextUtils.isEmpty(uid)){ //avoid user delete account during sync process    
            mProfile.saveProfileInfo(me);
       } 
    }  	
	
	private boolean isRightResult(JSONObject resJson){
	    boolean res = false;	    
	    //result is OK && has "data" string
	    if (!TextUtils.isEmpty(resJson.optString("result"))){
	        if (resJson.optString("result").equalsIgnoreCase("ok")){	            
	           res = true;
	        }
	    }
	    return res;
	}
}
