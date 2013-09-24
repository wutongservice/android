package com.borqs.profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;

import com.borqs.account.login.service.AccountService;
import com.borqs.common.transport.AccountInfo;
import com.borqs.profile.model.ContactProfileStruct;
import com.borqs.sync.client.vdata.card.ContactStruct;

public class AccountProfileInfoL14 extends AccountProfileInfo {
    public static final String ACCOUNT_PROFILE_PHONES = "borqs_conact_phones";
    public static final String ACCOUNT_PROFILE_EMAILS = "borqs_conact_emails";
    
    private AccountService mAccountService = null;
    
    public AccountProfileInfoL14(Context ctx){
        mAccountService = new AccountService(ctx);
    }
    
    @Override
    public List<Pair<String, Integer>> getPhones() {
        return getPairAttribute(ACCOUNT_PROFILE_PHONES);
    }

    @Override
    public List<Pair<String, Integer>> getEMails() {
        return getPairAttribute(ACCOUNT_PROFILE_EMAILS);
    }
    
    @Override
    public ContactProfileStruct toProfileStruct(){
        return null;
    }
    
    @Override
    public void saveProfileInfo(ContactProfileStruct info){        
        Map<String, Integer> CONTACT_MAP = new HashMap<String, Integer>();
        
        //build phones
        CONTACT_MAP.put(AccountInfo.MOBILE_TEL_NUM, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        CONTACT_MAP.put(AccountInfo.HOME_TEL_NUM, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        CONTACT_MAP.put(AccountInfo.BUSINESS_TEL_NUM, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        CONTACT_MAP.put(AccountInfo.OTHER_TEL_NUM, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        
        StringBuilder phoneList = new StringBuilder();
        Set<String> keys = CONTACT_MAP.keySet();
        for(String key : keys){
            String phone = getTypePhone(info, CONTACT_MAP.get(key));
            if (phone != null){
                if(phoneList.length()>0){
                    phoneList.append(",");
                }
                phoneList.append(phone)
                    .append(":")
                    .append(String.valueOf(CONTACT_MAP.get(key)));
            }            
        }        
        if(phoneList.length() > 0){
            mAccountService.setUserData(ACCOUNT_PROFILE_PHONES, phoneList.toString());
        }
        
        //build emails
        CONTACT_MAP.clear();
        CONTACT_MAP.put(AccountInfo.EMAIL_ADDRESS, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        CONTACT_MAP.put(AccountInfo.EMAIL_2_ADDRESS, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        CONTACT_MAP.put(AccountInfo.EMAIL_3_ADDRESS, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        
        StringBuilder emailList = new StringBuilder();
        Set<String> emailKeys = CONTACT_MAP.keySet();
        for(String key : emailKeys){
            String email = getTypeEmail(info, CONTACT_MAP.get(key));
            if(email != null){
                if(emailList.length()>0){
                    emailList.append(",");
                }
                emailList.append(email)
                    .append(":")
                    .append(String.valueOf(CONTACT_MAP.get(key)));
            }            
        }        
        if(emailList.length() > 0){
            mAccountService.setUserData(ACCOUNT_PROFILE_EMAILS, emailList.toString());
        }
    }
    
    private String getTypePhone(ContactProfileStruct profile, int type){
        List<ContactProfileStruct.PhoneData> phones = profile.getPhoneList();
        if (phones != null){
            for (ContactProfileStruct.PhoneData phone:phones){
                if (phone.type == type){
                    return phone.data;
                }
            }
        }
        return null;
    }
    
    @Override 
    public boolean isProfileChanged(){  
        return false;
    }
    
    @Override 
    public void cleanDirtyMark(){  
        //do nothing
    }
    
    @Override
    public void setModifyTime(){
        //do nothing
    }
    
    @Override 
    public long getLastModifyTime(){
        return 0;
    }
    
    @Override
    public boolean hasData(){
        return ((getPhones().size() > 0) || (getEMails().size() > 0));
    }
    
    private String getTypeEmail(ContactProfileStruct profile, int type){
        List<ContactStruct.EmailData> emails = profile.getEmailList();
        if (emails != null){
            for (ContactStruct.EmailData email:emails){
                if (email.type == type){
                    return email.data;
                }
            }
        }
        return null;
    }
    
    private List<Pair<String, Integer>> getPairAttribute(String key) {
        String phones = mAccountService.getUserData(key);
        if (TextUtils.isEmpty(phones)) {
            return Collections.emptyList();
        }

        LinkedList<Pair<String, Integer>> list = new LinkedList<Pair<String, Integer>>();
        String[] phoneList = phones.split(",");
        for (String pair : phoneList) {
            String[] phone_type = pair.split(":");
            list.add(Pair.create(phone_type[0],
                    Integer.valueOf(phone_type[1])));
        }
        return list;
    }
}
