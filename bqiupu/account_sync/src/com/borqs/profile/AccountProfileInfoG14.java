package com.borqs.profile;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Pair;

import com.borqs.account.login.service.AccountService;
import com.borqs.common.util.BLog;
import com.borqs.profile.model.ContactProfileStruct;
import com.borqs.profile.model.ContactsProfileOperator;
import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;

public class AccountProfileInfoG14 extends AccountProfileInfo {
    private Context mContext;
    private AccountService mAccountService = null;
    
    public AccountProfileInfoG14(Context ctx){
        mContext = ctx;
        mAccountService = new AccountService(mContext);
    }
    
    @Override
    public List<Pair<String, Integer>> getPhones() {
        ContactProfileStruct profile = toProfileStruct();
        if (profile == null){
            return Collections.emptyList();
        } 
        
        LinkedList<Pair<String, Integer>> list = new LinkedList<Pair<String, Integer>>();
        List<PhoneData> phoneList = profile.getPhoneList();
        if (phoneList != null){
            for (PhoneData phone:phoneList){
                list.add(Pair.create(phone.data, phone.type));
            }
        }
        
        return list;
    }

    @Override
    public List<Pair<String, Integer>> getEMails() {
        ContactProfileStruct profile = toProfileStruct();
        if (profile == null){
            return Collections.emptyList();
        } 
        
        LinkedList<Pair<String, Integer>> list = new LinkedList<Pair<String, Integer>>();
        List<EmailData> emailList = profile.getEmailList();
        if (emailList != null){
            for (EmailData email:emailList){
                list.add(Pair.create(email.data, email.type));
            }
        }
        
        return list;
    }
    
    @Override
    public ContactProfileStruct toProfileStruct(){
        ContactProfileStruct me = null;
        String uid = mAccountService.getLoginId();
        if (!TextUtils.isEmpty(uid)){
            ContactsProfileOperator profile_operator = new ContactsProfileOperator(mContext);
            long[] ids = profile_operator.getCurrentItems(uid, mAccountService.getAccountType());
            
            if ((ids != null)&&(ids.length > 0)){ 
                me = (ContactProfileStruct)profile_operator.load(ids[0]);
            }
        }
        return me;
    }
    
    @Override
    public void saveProfileInfo(ContactProfileStruct info){ 
        ContactsProfileOperator profile_operator = new ContactsProfileOperator(mContext);
        String uid = mAccountService.getLoginId();
        
        info.setAccount(uid, mAccountService.getAccountType());
        
        long[] ids = profile_operator.getCurrentItems(uid, mAccountService.getAccountType());
        //TODO: should del old, and add new one
        /*if ((ids != null)&&(ids.length > 0)){ 
            profile_operator.update(ids[0], info);
        } else {
            profile_operator.add(info);
        }   */
        if ((ids != null)&&(ids.length > 0)){ 
            profile_operator.delete(ids[0]);
        } 
        
        profile_operator.add(info);
    }
    
    @Override 
    public boolean isProfileChanged(){
        boolean res = false;
        if (!TextUtils.isEmpty(mAccountService.getLoginId())){
            Uri uri = ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
                    .appendQueryParameter(RawContacts.ACCOUNT_NAME, mAccountService.getLoginId())
                    .appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccountService.getAccountType())
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cr = resolver.query(uri, new String[] {RawContacts.DIRTY}, 
                                        RawContacts.DIRTY + "=1", null, null);
            if (cr != null){
                while (cr.moveToNext()){
                    res = true;
                    break;
                }
                cr.close();
            }
        }
        return res;
    }
    
    @Override 
    public void cleanDirtyMark(){
        String accountName = mAccountService.getLoginId();
        if(TextUtils.isEmpty(accountName)){
            BLog.e("accountName is null,clean dirty mark failed!");
            return;
        }
        Uri uri = ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, accountName)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccountService.getAccountType())
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(RawContacts.DIRTY, "0");
        resolver.update(uri, values, null, null);
    }
    
    @Override
    public boolean hasData(){
        ContactsProfileOperator profile_operator = new ContactsProfileOperator(mContext);
        String uid = mAccountService.getLoginId();
        long[] ids = profile_operator.getCurrentItems(uid, mAccountService.getAccountType());
        return ((ids != null)&&(ids.length > 0));
    }
    
    @Override
    public void setModifyTime(){
        String accountName = mAccountService.getLoginId();
        if(TextUtils.isEmpty(accountName)){
            BLog.e("accountName is null,setModifyTime failed!");
            return;
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();
        Uri uri = ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
        .appendQueryParameter(RawContacts.ACCOUNT_NAME, accountName)
        .appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccountService.getAccountType())
        .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
        .build();
        
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(RawContacts.SYNC4, currentTime);
        resolver.update(uri, values, null, null);
    }
    
    @Override 
    public long getLastModifyTime(){
        String accountName = mAccountService.getLoginId();
        if(TextUtils.isEmpty(accountName)){
            BLog.e("accountName is null,getLastModifyTime failed!");
            return 0;
        }

        long time = 0;
        Uri uri = ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
        .appendQueryParameter(RawContacts.ACCOUNT_NAME, accountName)
        .appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccountService.getAccountType())
        .build();
        
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cr = resolver.query(uri, new String[]{RawContacts.SYNC4}, null, null, null);
        if (cr != null){
            try {
                cr.moveToFirst();
                time = cr.getLong(0);
            } finally{
                cr.close();
            }
        }
        return time;
    }
}