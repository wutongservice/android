package com.borqs.profile;

import java.util.List;

import android.content.Context;
import android.util.Pair;

import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.profile.model.ContactProfileStruct;
public abstract class AccountProfileInfo {
    /**
     * get the phones of the profile
     * 
     * @return list of pair of <Number, Type>, type refer to
     *         ContactsContract.CommonDataKinds.Phone
     */
    public abstract List<Pair<String, Integer>> getPhones();

    /**
     * get the EMails of the profile
     * 
     * @return list of pair of <EMail, Type>, type refer to
     *         ContactsContract.CommonDataKinds.Email
     */
    public abstract List<Pair<String, Integer>> getEMails();
    
    public abstract void saveProfileInfo(ContactProfileStruct info);
    public abstract ContactProfileStruct toProfileStruct();
    public abstract void cleanDirtyMark();
    public abstract boolean isProfileChanged();
    public abstract long getLastModifyTime();
    public abstract void setModifyTime();
    public abstract boolean hasData();

    public static AccountProfileInfo create(Context context) {
       if (ContactSyncHelper.isSDK4_0Available()){
           return new AccountProfileInfoG14(context);
       } else {
           return new AccountProfileInfoL14(context);
       }
    }    
}
