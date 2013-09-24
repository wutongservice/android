package com.borqs.sync.provider;

import com.borqs.sync.client.vdata.card.ContactProviderOperation;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Entity;
import android.content.EntityIterator;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;

/**
 * a class that help operate data on contacts which associated an account
 * @author linxh
 *
 */
public class ContactsDBHelper {
    private ContentResolver mResolver;
    private Account mAccount;    
    private Uri mEntityUri;
    
    private final String[] ID_PROJECTION = new String[] {"_id"};
    private final String DIRTY_CONDITION = RawContacts.DIRTY + "=1";
        
    /**
     * 
     * @param resolver ContentResolvoer
     * @param account account
     * caller must assure resolver&account not null, all this class operation<br> 
     * won't check the two params valid value
     */
    public ContactsDBHelper(ContentResolver resolver, Account account){
        mResolver = resolver;
        mAccount = account;
        
        mEntityUri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, mAccount.name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccount.type)
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();
    }
    
    public Cursor getDirtyContacts(){
        //Log.i("syncml", "getDirtyContacts:" + mEntityUri.toString());
        Cursor cr = mResolver.query(mEntityUri, null, DIRTY_CONDITION, null, null);
        return cr;
    }
    
    /**
     * delete all contents of the mAccount
     */
    public void deleteAll(){
        Cursor cr = mResolver.query(mEntityUri, ID_PROJECTION, DIRTY_CONDITION, null, null);
        ContactProviderOperation co = new ContactProviderOperation(mResolver);
        try {
            while (cr.moveToNext()){
                co.delete(cr.getLong(0));
            }
        } finally{
            if (cr != null){
                cr.close();
            }
            co.execute();
        }
        
        //Log.i("syncml", "new contacts:" + count);
    }
    
    public void delete(long id){
        ContactProviderOperation co = new ContactProviderOperation(mResolver);
        co.delete(id);
        co.execute();
    }
}
