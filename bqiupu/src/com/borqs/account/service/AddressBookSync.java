package com.borqs.account.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.AsyncBorqsAccount;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * we need let phone do sync address book at period time
 */
public class AddressBookSync implements AccountListener {
    private static final String TAG = "BorqsAccountService.Alarm.AddressBookSync";

    final Context mContext;
    final AsyncBorqsAccount mAsyncBorqsAccount;
    final Object mLock = new Object();
    BorqsAccount mAccount;
    final BorqsAccountORM orm;
    final AddressbookHandler handler;
    static final int ITERATION_COUNT_LIMIT = 400;

    static boolean mIsLookupSessionTriggering;

    public static AddressBookSync getInstance(Context context, BorqsAccountORM orm, AsyncBorqsAccount borqsAccount) {
        Log.d(TAG, "getInstance");
        return new AddressBookSync(context, orm, borqsAccount);
    }

    private AddressBookSync(Context context, BorqsAccountORM borm, AsyncBorqsAccount asyncBorqsAccount) {
        mContext = context;
        mAsyncBorqsAccount = asyncBorqsAccount;
        orm = borm;

        mAccount = orm.getAccount();
        handler = new AddressbookHandler();
    }

    public void start() {
        Log.d(TAG, "start");
        AccountObserver.registerAccountListener(getClass().getName(), this);

        if (null == mAccount) {
            mAccount = orm.getAccount();
        }

        if (mAccount != null) {
            rescheduleAddressbook(false);
        }
    }

    public void onCancelLogin() {
        Log.d(TAG, "onCancelLogin");
    }

    private int nErrorCount = 0;

    public void onLogin() {
        Log.d(TAG, "onLogin");

        mAccount = orm.getAccount();
        if (mAccount != null) {
            nErrorCount = 0;
            rescheduleAddressbook(true);
        }
    }

    private void rescheduleAddressbook(boolean force) {
        Log.d(TAG, "rescheduleAddressbook=" + force);

        final long current_time = System.currentTimeMillis();
        final long last_sync_time = orm.getAddressBookSyncTime();
        final long donespan = current_time - last_sync_time;
        final long left_time = orm.getAddressBookInterval() * QiupuConfig.A_DAY +
                last_sync_time - current_time;

        long nexttime;
        if (donespan < 0 || left_time <= 0) {
            long waitRatio = 1;
            for (int i = 0; i < nErrorCount && i < 10; i++) {
                waitRatio = waitRatio * 2;
            }
            nexttime = current_time + 20 * QiupuConfig.A_SECOND * waitRatio;
        } else {
            nexttime = current_time + left_time;
        }

        if (force == true) {
            nexttime = current_time + 20 * QiupuConfig.A_SECOND;
        }

        if (BorqsAccountService.TEST_LOOP) {
            nexttime = +2 * QiupuConfig.A_MINUTE;
        }


        final long finaltime = nexttime;
        Log.d(TAG, "will launch address sync after seconds=" + (finaltime - current_time) / 1000);

        handler.post(new Runnable() {
            public void run() {
                Log.d(TAG, "rescheduleAddressbook, finaltime: " + finaltime);
                resetAddressBookSyncAlarm(finaltime);
            }
        });
    }

    public void alarmAddressBookComing() {
        Log.d(TAG, "alarmAddressBookComing");
    	alarmAddressBookComing(false);
    }
    
    public void alarmAddressBookComing(boolean immediately) {
        Log.d(TAG, "alarmAddressBookComing immediately ="+immediately);

        long nexttimecall = 10 * QiupuConfig.A_SECOND;
        if(immediately)
        {
        	nexttimecall = 0;
        }
        Message msg = handler.obtainMessage(ADDRESSBOOKS_GET);
        handler.sendMessageDelayed(msg, nexttimecall);

        final long nexttime = System.currentTimeMillis() +
                orm.getAddressBookInterval() * QiupuConfig.A_DAY;
        handler.post(new Runnable() {
            public void run() {
                Log.d(TAG, "alarmAddressBookComing, Runable run");
                resetAddressBookSyncAlarm(nexttime);
            }
        });
    }

    public void onLogout() {
        Log.d(TAG, "Logout");

        mAccount = null;

        cancelAddressBookSyncAlarm();
    }

    private final static int ADDRESSBOOKS_GET = 1;
    private final static int ADDRESSBOOKS_GET_END = 2;
    // one of the contact upload partition complete,
    // which means we need to refresh UI if any.
    private final static int ADDRESSBOOKS_GET_REFRESH = 3;

    class AddressbookHandler extends Handler {
        public AddressbookHandler() {
            super();
            Log.d(TAG, "new AddressbookHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            switch (msg.what) {
                case ADDRESSBOOKS_GET: {
                	new backgroudTask().execute((Void[]) null);
                    break;
                }

                case ADDRESSBOOKS_GET_END: {
                    boolean suc = msg.getData().getBoolean("RESULT");
                    Log.d(TAG, "ADDRESSBOOKS_GET_END ----> suc = " + suc);
                    if (suc) {
                        nErrorCount = 0;
                        orm.setAddressBookSyncTime();
                    } else {
                        nErrorCount++;
                        Log.d(TAG, "Fail to get friend suggestion reschedule");
                        rescheduleAddressbook(false);
                    }

                    PeopleLookupHelper.onAddressbookSyncEnd(AddressBookSync.this, msg);
                    break;
                }
                case ADDRESSBOOKS_GET_REFRESH: {
                    PeopleLookupHelper.onAddressbookGetRefresh(handler, msg);
                    break;
                }
                default:
                    Log.w(TAG, "handleMessage, unexpected message type: " + msg.what);
                    break;
            }
        }
    }

    private class backgroudTask extends android.os.AsyncTask<Void, Void, Void> {
        public backgroudTask() {
            super();
            Log.d(TAG, "create backgroudTask=" + this);
        }

        @Override
        protected Void doInBackground(Void... params) {
//            initDisplayingPeopleDbTable();
            syncAddressBook();
            return null;
        }
    }

    private static class CachePeople {
        public String filePath;
        public long lasttime;
    }


    private static final CachePeople entrys;

    static {
        entrys = new CachePeople();

        //set file path to tmp
        entrys.filePath = QiupuConfig.getTmpCachePath() + "lookupentry.source";
        entrys.lasttime = System.currentTimeMillis();
    }


    private static String createLookupEntriesFromCache(Context con, int offset) {
        String content = "";
        if ((System.currentTimeMillis() - entrys.lasttime < 10 * QiupuConfig.A_MINUTE)
                && (System.currentTimeMillis() - entrys.lasttime > 0)) {
            FileInputStream fis;
            ObjectInputStream in;
            try {
                final String fileName = entrys.filePath + offset;
                Log.d(TAG, "fileName = " + fileName);
                if (FileUtils.testReadFile(fileName)) {
                    fis = new FileInputStream(fileName);
                    in = new ObjectInputStream(fis);
                    content = (String) in.readObject();
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            if (content.length() == 0) {
                Log.d(TAG, "no cache, create a new one");
                content = createLookupEntries(con, ITERATION_COUNT_LIMIT, offset);
            } else {
                Log.d(TAG, "get from cache");
            }
        } else {
            content = createLookupEntries(con, ITERATION_COUNT_LIMIT, offset);
        }
        return content;
    }


    private static String createLookupEntries(Context con, int limit, int offset) {
        String entries = getLookupEntries(con, limit, offset);
        createLookupEntries(entries, entrys.filePath + offset);
        return entries;
    }

    private static void createLookupEntries(String entries, String filePath) {
        if (TextUtils.isEmpty(entries)) {
            Log.i(TAG, "createLookupEntries, ignore writing empty string to file: " + filePath);
            return;
        }

        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = new FileOutputStream(filePath);
            out = new ObjectOutputStream(fos);
            out.writeObject(entries);
            out.close();
            entrys.lasttime = System.currentTimeMillis();
        } catch (IOException ex) {
            Log.d(TAG, "fail to save the entry=" + ex.getMessage());
        }
    }

    private static String getLookupEntries(Context con, int limit, int offset) {
        try {
            List<ContactSimpleInfo> contacts = new ArrayList<ContactSimpleInfo>();
            final ContentResolver cr = con.getContentResolver();

//            if (notIncludeBorqsContact) {
//                final String where = ContactsContract.RawContacts.ACCOUNT_TYPE + "<>'com.borqs' or " + ContactsContract.RawContacts.ACCOUNT_TYPE + " is null";
//                Cursor rawCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI,
//                        new String[]{ContactsContract.RawContacts.CONTACT_ID}, where, null, null);
//
//                StringBuilder idswhere = new StringBuilder();
//                idswhere.append(" _id in (");
//                boolean firsttime = true;
//                while (rawCursor.moveToNext()) {
//                    if (firsttime == false)
//                        idswhere.append(",");
//
//                    if (firsttime == true) {
//                        firsttime = false;
//                    }
//
//                    idswhere.append(rawCursor.getInt(0));
//                }
//                idswhere.append(")");
//                rawCursor.close();
//            }
            Cursor contactCursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                    /*idswhere.toString()*/null, null, null);


            int flag = 0;
            int startindex = limit * offset;
            int endindex = limit * offset + limit;
            while (contactCursor.moveToNext()) {
                if (flag >= startindex && flag < endindex) {
                    int contactId = contactCursor.getInt(0);
                    String name = contactCursor.getString(1);

                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_ID},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    if (null != phones && phones.moveToFirst()) {                        
                        do {
                            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));// "data1"
                            if (!StringUtil.isValidString(phone)) {
                                continue;
                            }
                            long borqsId = 0;
                            long photoId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                            contacts.add(buildPhoneContactItem(contactId, name, phone, borqsId, photoId));
                        } while (phones.moveToNext());                        
                    }
                    
                    if (AccountServiceConfig.LowPerformance)
                        Log.d(TAG, "name :" + name + " phones size:" + phones.getCount() );
                    
                    if(phones != null)
                        phones.close();

                    Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Phone.PHOTO_ID},
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
                    if (null != emails && emails.moveToFirst()) {                        
                    	
                        if (AccountServiceConfig.LowPerformance)
                            Log.d(TAG, "name :" + name + "  emails size:" + emails.getCount());
                        
                        do {
                            String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));// "data1"
                            if (!StringUtil.isValidString(email)) {
                                continue;
                            }
                            long borqsId = 0;
                            long photoId = emails.getLong(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_ID));
                            contacts.add(buildEmailContactItem(contactId, name, email, borqsId, photoId));
                        } while (emails.moveToNext());                        
                    }
                    
                    if(emails != null)
                        emails.close();
                    
                } else if (flag >= endindex) {
                    break;
                }
                flag++;
            }

            contactCursor.close();


            //for sim card
            if (offset == 0) {
                try {
                    Uri uri = Uri.parse("content://icc/adn");
                    Cursor cursor = con.getContentResolver().query(uri, null, null, null, null);
                    Log.d(TAG, "Sim Query count:" + cursor.getCount());

                    while (cursor.moveToNext()) {
//				        String id = cursor.getString(cursor.getColumnIndex(People._ID));
                        String name = cursor.getString(cursor.getColumnIndex(People.NAME));
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(People.NUMBER));
                        long photoId = 0;
                        long borqsId = 0;

                        contacts.add(buildPhoneContactItem(-Integer.valueOf(phoneNumber), name, phoneNumber, borqsId, photoId));

                        if (AccountServiceConfig.LowPerformance)
                            Log.d(TAG, "SimQuery" + "_id, " + cursor.getString(cursor.getColumnIndex(People._ID))
                                    + "name, " + name + "phone number, " + phoneNumber);
                    }
                    cursor.close();
                } catch (Exception ne) {
                    Log.d(TAG, "getLookupEntries, exception:" + ne.getMessage());
                }

                if (AccountServiceConfig.LowPerformance) {
                    for (ContactSimpleInfo cinfo : contacts) {
                        Log.i(TAG, "------- getLookupEntries cinfo:'" + cinfo + "'");
                    }
                }
            }

            PeopleLookupHelper.setupContactsLookup(con, contacts);

            return JSONUtil.createLightJSONArray(contacts);
        } catch (Exception ne) {
            Log.d(TAG, "getLookupEntries, exception:" + ne.getMessage());
        }

        return "";
    }

    private static boolean notIncludeBorqsContact = false;

    private static int getContactnumber(Context con) {
        int totalcount = 0;
        if (notIncludeBorqsContact == true) {
            final String where = ContactsContract.RawContacts.ACCOUNT_TYPE + "<>'com.borqs' or " + ContactsContract.RawContacts.ACCOUNT_TYPE + " is null";
            Cursor rawCursor = con.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts.CONTACT_ID}, where, null, null);


            totalcount = rawCursor.getCount();
            rawCursor.close();
        } else {
            Cursor contactCursor = con.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (contactCursor != null) {
                totalcount = contactCursor.getCount();
                contactCursor.close();
            }
        }
        Log.d(TAG, "total contact number is " + totalcount);
        return totalcount;
    }

    protected int inProcessCount = 0;

    boolean isReadyForSync() {
        if (mAccount == null) {
            if (AccountServiceConfig.LOGD) Log.d(TAG, "isReadyForSync, mAccount is null");            
            return false;
        }

        if (!DataConnectionUtils.testValidConnection(mContext)) {
            Log.i(TAG, "isReadyForSync, ignore while no valid connection.");            
            return false;
        }

        synchronized (mLock) {
            if (mIsLookupSessionTriggering == true) {
                Log.d(TAG, "isReadyForSync, in doing get friends data");
                return false;
            }
        }

        return true;
    }

    private void syncAddressBook() {
        Log.d(TAG, "syncAddressBook enter.");

        if (!isReadyForSync()) {
            return;
        }       

        PeopleLookupHelper.onAddressbookSyncStart(handler);

        final int totalcontacts = getContactnumber(mContext);
        if (totalcontacts > 0) {
        	
        	 synchronized (mLock) {
             	mIsLookupSessionTriggering = true;
             }
        	 
            final int iteratorCount = (totalcontacts + ITERATION_COUNT_LIMIT - 1) / ITERATION_COUNT_LIMIT;
            synchronized (mLock) {
                inProcessCount = iteratorCount;
                Log.d(TAG, "syncAddressBook  inProcessCount="+inProcessCount);
            }
            
            for (int i = 0; i < iteratorCount; i++) {
                if (!DataConnectionUtils.testValidConnection(mContext)) {
                    Log.i(TAG, "syncAddressBook, ignore while no valid connection. i=" + i);
                    onAddressBookGetFailed();
                    continue;
                }

                if(QiupuConfig.DBLOGD)Log.d(TAG, "syncAddressBook, i = " + i + ", contacts count: " + totalcontacts + ", iteration count:" + iteratorCount);
                final String contactListjson = createLookupEntriesFromCache(mContext, i);
                uploadSocialContactIteration(contactListjson);
            }
        }   
        else
        {
            PeopleLookupHelper.onAddressbookSyncEnd(this, null);
        }
    }

    private void uploadSocialContactIteration(String uploadList) {
    	if(QiupuConfig.LowPerformance)
        Log.d(TAG, "uploadSocialContactIteration, uploadList = " + uploadList);
    	
        mAsyncBorqsAccount.getUserFromContact(mAccount.sessionid, uploadList, new TwitterAdapter() {
            public void syncUserFromContact(Set<ContactSimpleInfo> cinfos) {
                if (QiupuConfig.DBLOGD) Log.d(TAG, "syncUserFromContact, inProcessCount = " + inProcessCount +
                        ", cinfos.size() = " + cinfos.size());

                PeopleLookupHelper.cacheSystemUser(mContext, cinfos);

//                if (1 == inProcessCount) {
//                    PeopleLookupHelper.updateFinalUserTypeLookup(mContext);
//                }
                onAddressBookGetResult(true);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "uploadSocialContactIteration, met exception: " + ex);
                TwitterExceptionUtils.printException(TAG, "syncUserFromContact, fail sync with server exception:", ex);
                onAddressBookGetResult(false);
            }
        });
    }

    void onAddressBookGetFailed() {
        synchronized (mLock) {
            inProcessCount--;            
            
            if(inProcessCount == 0)
            {
                mIsLookupSessionTriggering = false;
            }
        }

        //if did one time successfully, we can think the operator is success
        Message msg = handler.obtainMessage(ADDRESSBOOKS_GET_END);
        msg.getData().putBoolean("RESULT", false);
        msg.sendToTarget();
    }

    void onAddressBookGetResult(boolean result) {
        synchronized (mLock) {
            inProcessCount--;
            
            if(inProcessCount == 0)
            {
                mIsLookupSessionTriggering = false;
                
                Log.d(TAG, "syncAddressBook  inProcessCount="+inProcessCount + "  mIsLookupSessionTriggering="+mIsLookupSessionTriggering);
            }            
        }

        final int message = inProcessCount == 0 ? ADDRESSBOOKS_GET_END : ADDRESSBOOKS_GET_REFRESH;
        Message msg = handler.obtainMessage(message);
        msg.getData().putBoolean("RESULT", result);
        msg.sendToTarget();
    }

    public void stop() {
        Log.d(TAG, "stop");
        AccountObserver.unregisterAccountListener(getClass().getName());
    }

    private PendingIntent getAddressBookSyncPendingIntent() {
        Intent i = new Intent(mContext, mContext.getClass());
        i.setAction("QiupuService.INTENT_QP_SYNC_ADDRESSBOOK_INFORMATION");
        PendingIntent pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),
                0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }

    private void resetAddressBookSyncAlarm(final long newTime) {
        PendingIntent pendingIntent = getAddressBookSyncPendingIntent();
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC, newTime, pendingIntent);
    }

    private void cancelAddressBookSyncAlarm() {
        PendingIntent pendingIntent = getAddressBookSyncPendingIntent();
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
    }

    static ContactSimpleInfo buildPhoneContactItem(long contactId, String name, String phone, long borqsId, long photoId) {
        ContactSimpleInfo cinfo = new ContactSimpleInfo();
        cinfo.mContactId = contactId;
        cinfo.mBorqsId = borqsId;
        cinfo.mPhotoId = photoId;
        cinfo.display_name_primary = changeDashFromLongToShort(name);
        cinfo.phone_number = phone;
        cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
        return cinfo;
    }

    static ContactSimpleInfo buildEmailContactItem(long contactId, String name, String email, long borqsId, long photoId) {

        ContactSimpleInfo cinfo = new ContactSimpleInfo();
        cinfo.mContactId = contactId;
        cinfo.mBorqsId = borqsId;
        cinfo.mPhotoId = photoId;
        cinfo.display_name_primary = changeDashFromLongToShort(name);
        cinfo.email = email;
        cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
        return cinfo;
    }

    // TODO: It is a work-around fix.
    private static String changeDashFromLongToShort(String name) {
        if (name.contains("—")) {
            name = name.replace("—", "-");
            if(QiupuConfig.DBLOGD)Log.i(TAG, "changeDashFromLongToShort ----> name = " + name);
        }
        return name;
    }

}
