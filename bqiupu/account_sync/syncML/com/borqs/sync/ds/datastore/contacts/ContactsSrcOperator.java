package com.borqs.sync.ds.datastore.contacts;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.EntityIterator;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.util.BLog;
import com.borqs.sync.client.vdata.card.ContactOperator;
import com.borqs.sync.client.vdata.card.ContactProviderOperation;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.client.vdata.card.ContactsVCardOperator;
import com.borqs.sync.client.vdata.card.VcardOperator;
import com.borqs.sync.ds.datastore.GeneralPimWordsProcess;
import com.borqs.sync.ds.datastore.SyncItem;
import com.borqs.sync.provider.ContactsDBHelper;
import com.borqs.syncml.ds.imp.common.Constant;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagString;
import com.borqs.syncml.ds.protocol.IPimInterface2;
import com.borqs.syncml.ds.protocol.ISyncItem;

/**
 * a pim interface that implements contacts operation 
 * @author linxh
 *
 */
public class ContactsSrcOperator implements IPimInterface2 {
    
    public static final String DEFAULT_SOURCE_ID = "12535";
    
    private Account mAccount;
    private Context mContext;
    private ContactsDBHelper mDataHelper;
    private ContactProviderOperation mContactOperator;
    
    private String DATA_TYPE = "text/x-vcard";
    private String PREFIX = Constant.PREFIX_CONTACTS;
    
    public ContactsSrcOperator(Context ctx, Account account){
        mContext = ctx;
        mAccount = account;
        mDataHelper = new ContactsDBHelper(mContext.getContentResolver(), mAccount);
        mContactOperator = new ContactProviderOperation(mContext.getContentResolver());
    }
    
    @Override
    public int getChangedItemCount(){
        int count = 0;
        /*Cursor cr = mDataHelper.getDirtyContacts();
        if (cr != null){
            count = cr.getCount();
            cr.close();
        }*/
        count = getNewList().size() + getUpdateList().size() + getDelList().size();
        BLog.d("syncml", "getChangedItemCount:" + count);
        return count;
    }
    
    @Override
    public ArrayList<Long> getNewList(){
        ArrayList<Long> idList = new ArrayList<Long>();
        Cursor cr = mDataHelper.getDirtyContacts();        
        try {
            int srcID = cr.getColumnIndex(RawContacts.SOURCE_ID);            
            int id = cr.getColumnIndex(RawContacts._ID);
            int delID = cr.getColumnIndex(RawContacts.DELETED);
            while (cr.moveToNext()) {
                String serverID = cr.getString(srcID);
                if (TextUtils.isEmpty(serverID)) {
                    int deleted = cr.getInt(delID);
                    if (deleted != 1) { // not delete
                        idList.add(Long.valueOf(cr.getInt(id)));
                    }                    
                }
            }                 
        } finally {
            if (cr != null){
                cr.close();
            }
        }
        BLog.d("syncml", "get added items to server, ids:" + idList.toString());
        return idList;
    }
    
    @Override
    public ArrayList<Long> getUpdateList(){
        ArrayList<Long> idList = new ArrayList<Long>();
        Cursor cr = mDataHelper.getDirtyContacts();
        try {
            int srcID = cr.getColumnIndex(RawContacts.SOURCE_ID);
            int delID = cr.getColumnIndex(RawContacts.DELETED);
            int ID = cr.getColumnIndex(RawContacts._ID);
            while (cr.moveToNext()) {          
                String serverID = cr.getString(srcID);
                if (!TextUtils.isEmpty(serverID)) {
                    int deleted = cr.getInt(delID);
                    if (deleted != 1) { // not delete
                        idList.add(Long.valueOf(cr.getInt(ID)));
                    }
                }
            }                 
        } finally {
            if (cr != null){
                cr.close();
            }
        }
        BLog.d("syncml", "get updated items to server, ids:" + idList.toString());
        return idList;
    }
    
    @Override
    public ArrayList<Long> getDelList(){
        ArrayList<Long> idList = new ArrayList<Long>();
        Cursor cr = mDataHelper.getDirtyContacts(); 
        try {
            int srcID = cr.getColumnIndex(RawContacts.SOURCE_ID);
            int delID = cr.getColumnIndex(RawContacts.DELETED);
            int ID = cr.getColumnIndex(RawContacts._ID);
            while (cr.moveToNext()) {                
                String serverID = cr.getString(srcID);
                if (!TextUtils.isEmpty(serverID)) {
                    int deleted = cr.getInt(delID);
                    if (deleted == 1) {
                        //TODO: may have on ocaasions:
                        //user local add a contact, then delete it, this shouldn't sync
                        idList.add(Long.valueOf(cr.getInt(ID)));
                    }
                }
            }                 
        } catch (Exception exp){
            BLog.d("syncml", "getDelList exception:" + exp.getMessage());
        }
        finally {
            if (cr != null){
                cr.close();
            }
        }
        BLog.d("syncml", "get deleted items to server, ids:" + idList.toString());
        return idList;
    }
    
    @Override
    public ISyncItem genDelItem(long id){
        BLog.d("syncml", "generate delete item to server, id:" +id);
        SyncItem item = new SyncItem(PREFIX + Long.toString(id), null,
                DATA_TYPE, ISyncItem.CMD_DELETE, null, 0);
        return item;
    }

    @Override
    public ISyncItem genAddItem(long id){
        return addReplaceItem(id, true);
    }

    @Override
    public ISyncItem genUpdateItem(long id){
        return addReplaceItem(id, false);
    }
    
    private ISyncItem addReplaceItem(long id, boolean add) {
        long hash = 0;
        byte[] vcard = null;
        
        ContactStruct cs = ContactOperator.load(id, mContext.getContentResolver());
        
        if (cs != null) {
            if (!add){
                BLog.d("syncml", "generate update item to server, id:" +id +",serverId:"+ cs.getSourceID());
                hash = Long.parseLong(cs.getSourceID());
            }else{
                BLog.d("syncml", "generate add item to server, id:" +id);
            }
            cs = (ContactStruct)GeneralPimWordsProcess.dealFieldsProcess(PREFIX, cs);
            String data = VcardOperator.create(cs);
            if (data != null) {
                vcard = data.getBytes();
            }
        }
        return new SyncItem(PREFIX + Long.toString(id), null, DATA_TYPE,
                add ? ISyncItem.CMD_ADD : ISyncItem.CMD_REPLACE, vcard, hash);
    }
    
    @Override
    public boolean serverAdd(long id, long srcId){
        BLog.d("syncml", "serverAdd:" + id + ", " + srcId);
        //TODO: at current server not response this srcId 
        // we insert one fixed id(it's only used to distinct local or sync record)
        srcId = 12535;
        ContentValues values  = new ContentValues();
        values.put(RawContacts.SOURCE_ID, DEFAULT_SOURCE_ID);
        values.put(RawContacts.DIRTY, "0");
        mContactOperator.newUpdate(id, values);
//        mContactOperator.execute();
//        mContactOperator.clear();
        return true;
    }
    
    public boolean serverDel(long id){
        Log.i("syncml", "serverDel:" + id);
        mContactOperator.delete(id);
//        mContactOperator.execute();
//        mContactOperator.clear();
        return true;
    }
    
    public boolean serverUpdate(long id){
        Log.i("syncml", "serverUpdate:" + id);
        ContentValues values = new ContentValues();
        values.put(RawContacts.DIRTY, "0");
        mContactOperator.newUpdate(id, values);
//        mContactOperator.execute();
//        mContactOperator.clear();
        return true;
    }
    
    //execute the serverAdd/serverUpdate/serverDelete after batch operation adding
    public boolean serverOperationExecute(){
        mContactOperator.execute();
        mContactOperator.clear();
        return true;
    }
    
    @Override
    public void deleteAllContent(){
        Log.i("syncml", "deleteAllContent");
        mDataHelper.deleteAll();
    }
    
    @Override
    public long add(TagItem item){
        long id = 0;
        ContactStruct contact = VcardOperator.parse(((TagString) (item.getData())).data(), 0);
        contact = (ContactStruct)GeneralPimWordsProcess.dealFieldsProcessBeforeSave(PREFIX, contact);
        contact.setSourceID(item.getSrcLocUri());
        id = ContactOperator.add(contact, mContext.getContentResolver());
        BLog.d("syncml", "client add item,id:" + id+ ",serverId:" + item.getSrcLocUri());
        return id;
    }
    
    @Override
    public long[] batchAdd(List<TagItem> itemList){
        ContactsVCardOperator operator = new ContactsVCardOperator();        
        ContactStruct[] contacts = new ContactStruct[itemList.size()];        
        for(int i = 0 ; i < itemList.size(); i ++){
            TagItem item = itemList.get(i);
            ContactStruct cs = operator.parse(item.getByteData());
            if(cs != null){
                contacts[i]= (ContactStruct) GeneralPimWordsProcess.dealFieldsProcessBeforeSave(PREFIX, cs);
                contacts[i].setSourceID(item.getSrcLocUri());
                BLog.d("syncml", "client add item,serverId:" + item.getSrcLocUri());
            }
        }

        long[] ids = ContactOperator.batchAdd(contacts, mContext.getContentResolver());
        if(ids != null){
            for (long id : ids) {
                BLog.d("syncml", "client add item,id:" + id);
            }
        }
        return ids;
    }
    
    @Override
    public boolean batchDelete(long[] keys){
        ContactProviderOperation co = new ContactProviderOperation(mContext.getContentResolver());
        for (long id:keys){
            BLog.d("syncml", "batchDelete:" + id);
            co.delete(id);
        } 
        co.execute();
        return true;
    }
    
    @Override
    public boolean update(long key, TagItem item){
        boolean res = false;
        ContactStruct contact = VcardOperator.parse(((TagString) (item.getData())).data(), 0);
        if (contact != null) {
            contact = (ContactStruct)GeneralPimWordsProcess.dealFieldsProcessBeforeSave(PREFIX, contact);
            contact.setSourceID(item.getSrcLocUri());
            if(ContactOperator.existContact(mContext.getContentResolver(), key)){
                res = ContactOperator.update(key, contact, mContext.getContentResolver());
            }else{
                //TODO only add? we need think about the SyncML protocol
            }
        }
        BLog.d("syncml", "server update item,id:" + key + ",serverId:" + item.getSrcLocUri() + ",result:" + res);        
        return res;
    }

    @Override
    public boolean delete(long key){
        BLog.d("syncml", "delete:" + key);
        mContactOperator.delete(key);
        mContactOperator.execute();
        mContactOperator.clear();
        return false;
    }
}
