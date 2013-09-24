package com.borqs.account.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import twitter4j.AsyncBorqsAccount;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.LookUpProvider;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.service.SyncContactStatusReceiver;
import com.borqs.qiupu.service.SyncContactStatusReceiver.AddressBookSyncContactListener;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;

/*
 * we need let phone do sync address book at period time
 */
public class PeopleLookupHelper implements AddressBookSyncContactListener {
    private static final String TAG = "PeopleLookupHelper";

    /**
     * constant defined from account sync project.
     */
    public static class SyncIntent{
        //broadcast for sync begin
        public static final String INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN = "com.borqs.intent.action.BORQS_CONTACT_SYNC_BEGIN";
        //broadcast for sync end
        public static final String INTENT_ACTION_BORQS_CONTACT_SYNC_END = "com.borqs.intent.action.BORQS_CONTACT_SYNC_END";
    }

    private final ContentResolver mContentResolver;

    public final static int LOOKUP_FLAG_SCANNED = 1;
    public final static int LOOKUP_FLAG_NONE = -1;

    private static final String queryContactsIdPrefix = LookUpProvider.CONTACT_ID + " = ?";

    private AddressBookSync mAddressBookSync;

    public static PeopleLookupHelper getInstance(Context context, BorqsAccountORM orm, AsyncBorqsAccount borqsAccount) {
        Log.d(TAG, "getInstance");
        return new PeopleLookupHelper(context, orm, borqsAccount);
    }

    private PeopleLookupHelper(Context context, BorqsAccountORM borm, AsyncBorqsAccount asyncBorqsAccount) {
        mAddressBookSync = AddressBookSync.getInstance(context, borm, asyncBorqsAccount);

        mContentResolver = context.getContentResolver();

        SyncContactStatusReceiver.registerSyncListener(getClass().getName(), this);
    }

    private boolean isPeopleCachedOutdated() {
        if (AddressBookSync.mIsLookupSessionTriggering) {
            return false;
        }

        if (mAddressBookSync.inProcessCount == 0 && mAddressBookSync.orm.getAddressBookSyncTime() == 0) {
            return true;
        }

        return false;
    }

    private void cleanCachedDb() {
//        mContext.getContentResolver().delete(LookUpProvider.CONTENT_LOOKUP_URI, null, null);
        File file = new File(entrys.filePath);
        Log.i(TAG, "cleanCachedDb() : entrys.filePath = " + entrys.filePath + " file.exists() = " + file.exists());
        if (file.exists()) {
            file.delete();
        }
    }

    private void lookupAllContacts() {
        cleanCachedDb();
        if (null == mAddressBookSync.mAccount) {
            mAddressBookSync.mAccount = mAddressBookSync.orm.getAccount();
        }
        mAddressBookSync.alarmAddressBookComing(true);
    }

    private void lookupNewContacts() {
        if (!mAddressBookSync.isReadyForSync()) {
            return;
        }

        final Cursor lookupCursor = getPendingLookupItems();
        final int totalCount = lookupCursor.getCount();
        if (totalCount > 0) {
        	
        	 synchronized (mAddressBookSync.mLock) {
                 AddressBookSync.mIsLookupSessionTriggering = true;
             }
        	 
            final int iteratorCount = (totalCount + AddressBookSync.ITERATION_COUNT_LIMIT - 1) / AddressBookSync.ITERATION_COUNT_LIMIT;
            synchronized (mAddressBookSync.mLock) {
                mAddressBookSync.inProcessCount += iteratorCount;
            }

            updateActivityUI(mAddressBookSync.handler, STATUS_DOING, null);
            for (int i = 0; i < iteratorCount; i++) {
                if (!DataConnectionUtils.testValidConnection(mAddressBookSync.mContext)) {
                    Log.i(TAG, "lookupNewContacts, ignore while no valid connection. i=" + i);
                    mAddressBookSync.onAddressBookGetFailed();
                    continue;
                }

                if(QiupuConfig.LowPerformance)
                    Log.d(TAG, "lookupNewContacts, i = " + i + ", contacts count: " + totalCount + ", upload count:" + iteratorCount);
                
                final String fileName = entrys.filePath + i + "new";

                String contactListjson = createCachedLookupNewEntries(fileName);
	            if (TextUtils.isEmpty(contactListjson)) {
	                List<ContactSimpleInfo> contacts = new ArrayList<ContactSimpleInfo>();
	                final int firstIndex = i * (AddressBookSync.ITERATION_COUNT_LIMIT);
	                lookupCursor.moveToPosition(firstIndex);
	                final int lastIndex = i == iteratorCount - 1 ?
                            totalCount - firstIndex : (AddressBookSync.ITERATION_COUNT_LIMIT);
	                for (int index = 0; index < lastIndex; ++index) {
	                    ContactSimpleInfo cinfo = new ContactSimpleInfo();
	                    cinfo.phone_number = lookupCursor.getString(lookupCursor.getColumnIndex(LookUpProvider.PHONE));
	                    if (TextUtils.isEmpty(cinfo.phone_number)) {
	                        cinfo.email = lookupCursor.getString(lookupCursor.getColumnIndex(LookUpProvider.EMAIL));
	                        if (TextUtils.isEmpty(cinfo.email)) {
	                        	if(QiupuConfig.LowPerformance)
	                                Log.i(TAG, "lookupNewContacts, invalid entity without email or phone, i = " + i + ", index = " + index);
	                        	
	                            continue;
	                        } else {
	                            cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
	                        }
	                    } else {
	                        cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
	                    }
	                    cinfo.mContactId = lookupCursor.getLong(lookupCursor.getColumnIndex(LookUpProvider.CONTACT_ID));
	                    cinfo.mBorqsId = lookupCursor.getLong(lookupCursor.getColumnIndex(LookUpProvider.UID));
	                    cinfo.mPhotoId = lookupCursor.getLong(lookupCursor.getColumnIndex(LookUpProvider.PHOTO_ID));
	                    cinfo.display_name_primary = lookupCursor.getString(lookupCursor.getColumnIndex(LookUpProvider.NAME));
	                    
	                    if(QiupuConfig.LowPerformance)
	                        Log.i(TAG, "cinfo ========= " + cinfo.toString());
	                    
	                    contacts.add(cinfo);
	
	                    lookupCursor.moveToNext();
	                }
	
	                contactListjson = JSONUtil.createLightJSONArray(contacts);
	                if(QiupuConfig.LowPerformance)
	                    Log.d(TAG, "======= contactListjson = " + contactListjson);
	                
	                createLookupEntries(contactListjson, fileName);
	            }

                uploadSocialContactIteration(contactListjson);
            }
        }
        closeCursor(lookupCursor);
    }

    private String createCachedLookupNewEntries(String fileName) {
        String content = "";
        FileInputStream fis;
        ObjectInputStream in;
        try {
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

        return content;
    }

    public void triggerPeopleLookupSession(boolean needPrompt) {
        Log.d(TAG, "triggerPeopleLookupSession, needPrompt: " + needPrompt);
        if (isPeopleCachedOutdated()) {
            boolean hasAllow = QiupuORM.isAllowLookup(mAddressBookSync.mContext);
            if (needPrompt && !hasAllow) {
                DialogUtils.showConfirmDialog(mAddressBookSync.mContext, R.string.add_contact_title,
                        R.string.add_contact_message, R.string.label_ok, R.string.label_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "triggerPeopleLookupSession, dialog on click.");
                                QiupuORM.setAllowLookup(mAddressBookSync.mContext, true);
                                lookupAllContacts();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                QiupuORM.setConfirmLookup(mAddressBookSync.mContext, true);
                            }
                        }
                );
            } else {
                lookupAllContacts();
            }
        } else if (!AddressBookSync.mIsLookupSessionTriggering) {
            Log.d(TAG, "triggerPeopleLookupSession, no changed since last look up.");
            lookupNewContacts();
            return;
        }
    }

    private int nErrorCount = 0;
    public void alarmAddessbookComming(boolean immediatly) {
        Log.d(TAG, "alarmAddressBookComing immediatly="+immediatly);
        mAddressBookSync.alarmAddressBookComing(immediatly);
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

//    private int inProcessCount = 0;

    private void uploadSocialContactIteration(String uploadList) {
    	if(QiupuConfig.LowPerformance)
        Log.d(TAG, "uploadSocialContactIteration, uploadList = " + uploadList);
    	
        mAddressBookSync.mAsyncBorqsAccount.getUserFromContact(mAddressBookSync.mAccount.sessionid, uploadList, new TwitterAdapter() {
            public void syncUserFromContact(Set<ContactSimpleInfo> cinfos) {
                if (QiupuConfig.DBLOGD) Log.d(TAG, "syncUserFromContact, inProcessCount = " + mAddressBookSync.inProcessCount +
                        ", cinfos.size() = " + cinfos.size());

                cacheSystemUser(mAddressBookSync.mContext, cinfos);
                if (1 == mAddressBookSync.inProcessCount) {
                    updateFinalUserTypeLookup(mAddressBookSync.mContext);
                }
                mAddressBookSync.onAddressBookGetResult(true);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "uploadSocialContactIteration, met exception: " + ex);
                TwitterExceptionUtils.printException(TAG, "syncUserFromContact, fail sync with server exception:", ex);
                mAddressBookSync.onAddressBookGetResult(false);
            }
        });
    }

    static void cacheSystemUser(final Context con, final Set<ContactSimpleInfo> cinfos) {
    	if (cinfos == null || cinfos.size() == 0) {
              Log.d(TAG, "cacheSystemUser, no contacts in system.");
              return;
        }
    	  
    	QiupuORM.sWorker.post(new Runnable()
    	{
    		public void run()
    		{
                final int entityCount = cinfos.size();
                Log.d(TAG, "cacheSystemUser, Runnable run size: " + entityCount);

    			Iterator<ContactSimpleInfo> iterator = cinfos.iterator();
		        ContentValues values = new ContentValues();
                final ContentResolver resolver = con.getContentResolver();
                String[] queryArg;
                ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
		        while (iterator.hasNext()) {
		            ContactSimpleInfo cinfo = iterator.next();
//		            String where = LookUpProvider.CONTACT_ID + " = \'" + cinfo.mContactId + "\'";

                    queryArg = new String[] {String.valueOf(cinfo.mContactId)};
		            values.clear();
		            values.put(LookUpProvider.UID, cinfo.mBorqsId);

                    final int updateCount = resolver.update(LookUpProvider.CONTENT_LOOKUP_URI, values,
                            queryContactsIdPrefix, queryArg);
                    if (updateCount <= 0) {
                        values.put(LookUpProvider.CONTACT_ID, cinfo.mContactId);
                        values.put(LookUpProvider.PHOTO_ID, cinfo.mPhotoId);
                        values.put(LookUpProvider.NAME, cinfo.display_name_primary);
                        bulkInsertList.add(values);
//                        resolver.insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
                    }

//		            Cursor oneUser = con.getContentResolver().query(LookUpProvider.CONTENT_LOOKUP_URI, null, where, null, null);
//		            if(oneUser != null && oneUser.getCount() > 0){
//		            	con.getContentResolver().update(LookUpProvider.CONTENT_LOOKUP_URI, values, where, null);
//		            }else{
//		            	values.put(LookUpProvider.CONTACT_ID, cinfo.mContactId);
//		            	values.put(LookUpProvider.PHOTO_ID, cinfo.mPhotoId);
//		            	values.put(LookUpProvider.NAME, cinfo.display_name_primary);
//		            	Uri uri = con.getContentResolver().insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
//		            	if(QiupuConfig.DBLOGD)Log.d(TAG, "cacheSystemUser --> uri = " + uri.toString());
//		            }
//		            closeCursor(oneUser);
		        }

                if (bulkInsertList.size() > 0) {                    
                    ContentValues[] valueArray = new ContentValues [bulkInsertList.size()];
                    valueArray = bulkInsertList.toArray(valueArray);
                    resolver.bulkInsert(LookUpProvider.CONTENT_LOOKUP_URI, valueArray);
                }
    		}
    	});
    }

    static void updateFinalUserTypeLookup(Context context) {
        // Only do this once the last sync result comes.
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(LookUpProvider.TYPE, LOOKUP_FLAG_SCANNED);
        String where = LookUpProvider.TYPE + " = " + LOOKUP_FLAG_NONE;
        cr.update(LookUpProvider.CONTENT_LOOKUP_URI, values, where, null);
    }

    private boolean isEmptyCursor(Cursor cursor) {
        if (cursor == null) {
            return true;
        } else if (cursor.getCount() <= 0) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private HashSet obtainIds(Cursor idCursor) {
        if (isEmptyCursor(idCursor)) {
            return null;
        }

        HashSet idSet = new HashSet();

        idCursor.moveToFirst();
        int count = idCursor.getCount();
        for (int i = 0; i < count; i++) {
            idSet.add(idCursor.getLong(idCursor.getColumnIndex(ContactsContract.Contacts._ID)));
            idCursor.moveToNext();
        }

        closeCursor(idCursor);
        return idSet;
    }

    private static boolean mIsDirtyLookup;
    private ContentObserver mContactMonitor = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange, contact monitor selfChange: " + selfChange);
            mIsDirtyLookup = true;
            updateActivityUI(mAddressBookSync.handler, CONTENTOBSERVER_CHANGED, null);
//            unRegisterObserver();
        }
    };

    private void shootLookupVerify() {
        if (mAddressBookSync.isReadyForSync()) {
            Cursor contactIdCursor = ContactUtils.getContactIds(mContentResolver);
            mIsDirtyLookup = false;
//            registerObserver();

            if (isEmptyCursor(contactIdCursor)) {
                deleteOutdatedCachedContact(null);
            } else {
                QiupuORM.sWorker.post(new SyncContactRunnable(contactIdCursor));
            }
        }
    }
    private class SyncContactRunnable implements Runnable {
        private Cursor mCursor;
        SyncContactRunnable(Cursor cursor) {
            super();
            mCursor = cursor;
        }

        @Override
        public void run() {
            Log.d(TAG, "SyncContactRunnable, Runnable run.");
            syncDataWithContact(mCursor);
            mCursor.close();
            mCursor = null;
            lookupNewContacts();
        }
    }

//    private void addNewCacheContacts(HashSet idSet) {
//        if (idSet == null) {
//            return;
//        }
//
//        final String sIds = toString(idSet);
//        String selection = ContactsContract.Contacts._ID + " NOT IN " + sIds;
//        Cursor insertCursor = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, selection, null, null);
////        Log.d(TAG,"!isEmptyCursor(insertCursor) = " +!isEmptyCursor(insertCursor));
//        if (!isEmptyCursor(insertCursor)) {
//            insertData(insertCursor);
//        }
//    }

    private void deleteOutdatedCachedContact(HashSet idSet) {
        if (null == idSet || idSet.size() == 0) {
            mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, null, null);
        } else {
//            Cursor cachedContactIds = getCachedContactIds(mContentResolver);
//            if (!isEmptyCursor(cachedContactIds)) {
                final String sIds = toString(idSet);
                String selection = LookUpProvider.CONTACT_ID + " NOT IN " + sIds;
                final int deleteCount = mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, selection, null);
                Log.d(TAG, "deleteOutdatedCachedContact, deleted count: " + deleteCount);
//            }
//            closeCursor(cachedContactIds);
        }
    }

    private static final HashMap<String, WeakReference<AddressBookSyncServiceListener>> listeners = new HashMap<String, WeakReference<AddressBookSyncServiceListener>>();
    public final static int STATUS_DEFAULT = 0; //
    public final static int STATUS_DOING = STATUS_DEFAULT + 1;
    public final static int STATUS_DO_ALL_OK = STATUS_DOING + 1;
    public final static int STATUS_DO_FAIL = STATUS_DO_ALL_OK + 1;
    public final static int STATUS_DO_ONE_LOOP_OK = STATUS_DO_FAIL + 1;
    
    public final static int CONTENTOBSERVER_CHANGED = STATUS_DO_ONE_LOOP_OK + 1;

    public interface AddressBookSyncServiceListener {
        public void updateUI(int msgcode, Message msg);
    }

    public static void registerFriendsServiceListener(String key, AddressBookSyncServiceListener listener) {
        Log.d(TAG, "registerFriendsServiceListener");
        if (listeners.get(key) == null) {
            synchronized (listeners) {
                listeners.put(key, new WeakReference<AddressBookSyncServiceListener>(listener));
            }
        }
    }

    public static void unregisterFriendsServiceListener(String key) {
        Log.d(TAG, "unregisterFriendsServiceListener");
        synchronized (listeners) {
            listeners.remove(key);
        }
    }

    private static void updateActivityUI(Handler handle, final int msgcode, final Message msg) {
        if (QiupuConfig.LOGD) Log.d(TAG, "updateActivityUI msgcode:" + msgcode + " listener count:" + listeners.size());
        handle.post(new Runnable()
        {
        	public void run()
        	{
                Log.d(TAG, "updateActivityUI, Runnable run.");
		        synchronized (listeners) {
		            Set<String> set = listeners.keySet();
		            Iterator<String> it = set.iterator();
		            while (it.hasNext()) {
		                String key = it.next();
		                WeakReference<AddressBookSyncServiceListener> ref = listeners.get(key);
		                if( ref!= null && ref.get() != null)
		                {
			                AddressBookSyncServiceListener listener = ref.get();
			                if (listener != null) {
			                    listener.updateUI(msgcode, msg);
			                }
		                }
		            }
		        }
        	}
        });
    }

    private void syncDataWithContact(Cursor contactIdCursor) {
        Log.d(TAG, "syncDataWithContact, enter");
        Cursor allContactCursor = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (isEmptyCursor(allContactCursor)) {
            // delete cached tables
            mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, null, null);
        } else {
            HashSet idSet = obtainIds(contactIdCursor);
            deleteOutdatedCachedContact(idSet);
            Log.d(TAG, "syncDataWithContact, deleting outdated entities completed.");

            bulkUpdateContacts(idSet);
            Log.d(TAG, "syncDataWithContact, bulk update cached contacts entities completed, count: " + contactIdCursor.getCount());
        }

        closeCursor(allContactCursor);
        Log.d(TAG, "syncDataWithContact, exit");
    }

    private final static String[]contactProject = new String[]{ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_ID,
            ContactsContract.Contacts.Data.SYNC4
    };

    private void bulkUpdateContacts(HashSet idSet) {
        final String sIds = toString(idSet);
        String selection = LookUpProvider.CONTACT_ID + " IN " + sIds;
        Cursor contactCur = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, contactProject, selection, null, null);
        if (null != contactCur && contactCur.moveToFirst()) {
            final String contactEmailSelection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " IN " + sIds;
            final String contactPhoneNumberSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN " + sIds;

            final String lookupEmailSelection = LookUpProvider.CONTACT_ID + " IN " + sIds +
                    " and " + LookUpProvider.EMAIL + "  IS NOT NULL";
            final String lookupNumberSelection = LookUpProvider.CONTACT_ID + " IN " + sIds +
                    " and " + LookUpProvider.PHONE + "  IS NOT NULL";

            Set<String> contactEmailSet = new HashSet<String>();
            Set<String> phoneNumberSet = new HashSet<String>();

            HashMap<String, Integer> emailToIdMap = new HashMap<String, Integer>();
            HashMap<String, Integer> numberToIdMap = new HashMap<String, Integer>();

            String contactName;
            long contactPhotoId;
            int contactId;
            do {
                contactName = contactCur.getString(contactCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                contactPhotoId = contactCur.getLong(contactCur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                contactId = contactCur.getInt(contactCur.getColumnIndex(ContactsContract.Contacts._ID));

                // bulk update email lookup entities.
                Cursor contactCursor = mContentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        contactEmailSelection, null, null);
                if (null != contactCursor && contactCursor.moveToFirst()) {
                    String email;
                    do {
                        email = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        if (StringUtil.isValidString(email)) {
                            contactEmailSet.add(email);
                            emailToIdMap.put(email, contactId);
                        }
                    } while (contactCursor.moveToNext());
                }
                closeCursor(contactCursor);

                if (contactEmailSet.size() > 0) {
                    Cursor lookupEmailCursor = mContentResolver.query(LookUpProvider.CONTENT_LOOKUP_URI, null,
                            lookupEmailSelection, null, null);
                    if (null != lookupEmailCursor && lookupEmailCursor.moveToFirst()) {
                        Set<String> lookupEmailSet = new HashSet<String>();
                        String email;
                        do {
                            email = lookupEmailCursor.getString(lookupEmailCursor.getColumnIndex(LookUpProvider.EMAIL));
                            if (StringUtil.isValidString(email)) {
                                lookupEmailSet.add(email);
                            }
                        } while (lookupEmailCursor.moveToNext());

                        contactEmailSet.removeAll(lookupEmailSet);
                    }
                    closeCursor(lookupEmailCursor);

                    cacheNewEmailAddress(contactEmailSet, emailToIdMap, contactName, contactPhotoId);
                } else {
                    mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, lookupEmailSelection, null);
                }
                // email entities lookup update end.

                // bulk update telephone number lookup entities
                Cursor phoneNumberCursor = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        contactPhoneNumberSelection, null, null);

                if (null != phoneNumberCursor && phoneNumberCursor.moveToFirst()) {
                    String number;
                    do {
                        number = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                        if (StringUtil.isValidString(number)) {
                            phoneNumberSet.add(number);
                            numberToIdMap.put(number, contactId);
                        }
                    } while (phoneNumberCursor.moveToNext());
                }
                closeCursor(phoneNumberCursor);

                if (phoneNumberSet.size() > 0) {
                    Cursor lookupPhoneNumber = mContentResolver.query(LookUpProvider.CONTENT_LOOKUP_URI, null,
                            lookupNumberSelection, null, null);
                    if (null != lookupPhoneNumber && lookupPhoneNumber.moveToFirst()) {
                        Set<String> lookupNumberSet = new HashSet<String>();
                        String number;
                        do {
                            number = lookupPhoneNumber.getString(lookupPhoneNumber.getColumnIndex(LookUpProvider.PHONE));
                            if (StringUtil.isValidString(number)) {
                                lookupNumberSet.add(number);
                            }
                        } while (lookupPhoneNumber.moveToNext());
                        phoneNumberSet.removeAll(lookupNumberSet);
                    }
                    closeCursor(lookupPhoneNumber);

                    cacheNewPhoneNumber(phoneNumberSet, numberToIdMap, contactName, contactPhotoId);
                } else {
                    mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, lookupNumberSelection, null);
                }
                // telephone number entities lookup update end.

            } while (contactCur.moveToNext());
        }
        closeCursor(contactCur);
    }

    private String toString(Set<Long> set) {
        Iterator<Long> iterator = set.iterator();
        StringBuilder sbId = new StringBuilder();
        sbId.append("(");
        while(iterator.hasNext()) {
            long id = iterator.next();
            sbId.append(id).append(",");
        }
        sbId.setLength(sbId.length() - 1);
        sbId.append(")");
//        Log.d(TAG, "############  sbId = " + sbId.toString());
        return sbId.toString();
    }

    private void cacheNewEmailAddress(Set<String> emailSet, HashMap<String, Integer> emailToIdMap, String name, long photoId) {

        if (null != emailSet && !emailSet.isEmpty()) {
            ContentValues[] values = new ContentValues[emailSet.size()];

            Iterator<String> iterator = emailSet.iterator();
            int i = 0;
            String email;
            while (iterator.hasNext()) {
                email = iterator.next();
                values[i].put(LookUpProvider.EMAIL, email);

                values[i].put(LookUpProvider.CONTACT_ID, emailToIdMap.get(email));
                values[i].put(LookUpProvider.NAME, name);
                values[i].put(LookUpProvider.PHOTO_ID, photoId);
                values[i].put(LookUpProvider.TYPE, LOOKUP_FLAG_NONE);

                i++;
            }

            mContentResolver.bulkInsert(LookUpProvider.CONTENT_LOOKUP_URI, values);
        }
    }

    private void cacheNewPhoneNumber(Set<String> phoneNumberSet, HashMap<String, Integer> numberToIdMap, String name, long photoId) {
        if (null != phoneNumberSet && !phoneNumberSet.isEmpty()) {
        	 ContentValues[] values = new ContentValues[phoneNumberSet.size()];
             Iterator<String> iterator = phoneNumberSet.iterator();
             int i = 0;
            String phoneNumber;
             while (iterator.hasNext()) {
                 phoneNumber = iterator.next();
                 values[i].put(LookUpProvider.CONTACT_ID, numberToIdMap.get(phoneNumber));
                 values[i].put(LookUpProvider.NAME, name);
                 values[i].put(LookUpProvider.PHOTO_ID, photoId);
                 values[i].put(LookUpProvider.TYPE, LOOKUP_FLAG_NONE);
                 values[i].put(LookUpProvider.PHONE, phoneNumber);
                 i++;
             }             
             mContentResolver.bulkInsert(LookUpProvider.CONTENT_LOOKUP_URI, values);
        }
    }

    public int getSyncStatus() {
        Log.d(TAG, "getSyncStatus, enter.");
        if (isPeopleCachedOutdated()) {
            return STATUS_DEFAULT;
        } else if (AddressBookSync.mIsLookupSessionTriggering == true) {
            return STATUS_DOING;
        } else {
            if (mIsDirtyLookup) {
                shootLookupVerify();
                return STATUS_DOING;
            } else if (existPendingSyncItem()) {
                Log.d(TAG, "getSyncStatus() --> true");
                if (nErrorCount == 0) {
                    lookupNewContacts();
                    return STATUS_DOING;
                } else {
                    return STATUS_DO_ALL_OK;
                }
            } else {
                return STATUS_DO_ALL_OK;
            }
        }
    }

    private Cursor getPendingLookupItems() {
        String lookupSelection = LookUpProvider.TYPE + " = " + LOOKUP_FLAG_NONE;
        Cursor lookupCur = mContentResolver.query(LookUpProvider.CONTENT_LOOKUP_URI, null,
                lookupSelection, null, null);
        return lookupCur;
    }

    private boolean existPendingSyncItem() {
        boolean ret = false;
        final Cursor lookupCursor = getPendingLookupItems();

        if (null != lookupCursor) {
            final int count = lookupCursor.getCount();
            closeCursor(lookupCursor);
            Log.d(TAG, "existPendingSyncItem, count = " + count);
            if (count > 0) {
                ret = true;
            }
        }
        closeCursor(lookupCursor);

        return ret;
    }

//    private static boolean mIsRunningVerifyContactType;
private void verifyContactsType(Cursor contactsCursor) {
//        if (!mIsRunningVerifyContactType) {
//            if (!mIsLookupSessionTriggering && isEmptyLookupResult()) {
//                mIsRunningVerifyContactType = true;
                QiupuORM.sWorker.post(new VerifyContactRunnable(contactsCursor));
//            }
//        }
    }

    class VerifyContactRunnable implements Runnable {
        private Cursor mCursor;
        VerifyContactRunnable(Cursor cursor) {
            super();
            mCursor = cursor;
        }

        @Override
        public void run() {
            Log.d(TAG, "VerifyContactRunnable, Runnable run.");
            if (null != mCursor && !mCursor.isClosed()) {
                verifyContactsWithLocalUserDb();
                mCursor.close();
                mCursor = null;
            }
//            mIsRunningVerifyContactType = false;
        }

        private boolean needVerify(long contactId) {
            boolean ret = true;
            final String where = LookUpProvider.CONTACT_ID + " = " + contactId + " AND " +
                    LookUpProvider.TYPE + " != " + LOOKUP_FLAG_NONE;

            Cursor lookupResult = mContentResolver.query(LookUpProvider.CONTENT_LOOKUP_URI, null, where, null, null);
            if (null != lookupResult) {
                if (lookupResult.getCount() > 0) {
                    ret = false;
                }
            }
            closeCursor(lookupResult);

            return ret;
        }

        private void deleteExistingLookupResult(long contactId) {
            final String where = LookUpProvider.CONTACT_ID + " = " + contactId;
            int deleteCount = mContentResolver.delete(LookUpProvider.CONTENT_LOOKUP_URI, where, null);
            //Log.d(TAG, "deleteExistingLookupResult, deleted count: " + deleteCount);
        }


        private void lookupContactsInfo(long contactId) {
            Cursor phones = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            if (null != phones)
            {
            	if(phones.moveToFirst()){
            		ContentValues values = new ContentValues();
            		do {
            			String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            			if (StringUtil.isValidString(phone)) {
            			    final String where = PhoneEmailColumns.INFO + " = \'" + phone + "\'"; 
//            				final String where = QiupuORM.UsersColumns.CONTACT_PHONE1 + " = \'" + phone + "\' OR " +
//            						QiupuORM.UsersColumns.CONTACT_PHONE2 + " = \'" + phone + "\' OR " +
//            						QiupuORM.UsersColumns.CONTACT_PHONE3 + " = \'" + phone + "\'";
//            				Cursor userCursor = mContentResolver.query(QiupuORM.USERS_CONTENT_URI, USERS_LOOKUP_PROJECTION, where, null, null);
            				Cursor userCursor = mContentResolver.query(QiupuORM.PHONEEMAIL_CONTENT_URI, new String[]{PhoneEmailColumns.USERID}, where, null, null);
            				if (null != userCursor) {
            					if(userCursor.moveToFirst()){
            						values.clear();
            						values.put(LookUpProvider.CONTACT_ID, contactId);
            						values.put(LookUpProvider.PHONE, phone);
//                            values.put(LookUpProvider.PHOTO_ID, cinfo.mPhotoId);
//                            values.put(LookUpProvider.EMAIL, cinfo.email);
            						values.put(LookUpProvider.TYPE, LOOKUP_FLAG_SCANNED);
            						do {
//            							values.put(LookUpProvider.NAME, userCursor.getString(userCursor.getColumnIndex(USERS_LOOKUP_PROJECTION[1])));
//            							values.put(LookUpProvider.UID, userCursor.getString(userCursor.getColumnIndex(USERS_LOOKUP_PROJECTION[0])));

            						    values.put(LookUpProvider.UID, userCursor.getString(userCursor.getColumnIndex(PhoneEmailColumns.USERID)));
            						    
            							mContentResolver.insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
            						} while (userCursor.moveToNext());	                            
            					}
            					
            					userCursor.close();		                        
            				}
            			}
            			
            		} while (phones.moveToNext());   
            	}
                
                phones.close();               
            }
            
           

            Cursor emails = mContentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
            if (null != emails)
            {
            	if(emails.moveToFirst()){
            		ContentValues values = new ContentValues();
            		do {
            			String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));// "data1"
            			if (StringUtil.isValidString(email)) {
            			    final String where = PhoneEmailColumns.INFO + " = \'" + email + "\'"; 
//            				final String where = QiupuORM.UsersColumns.CONTACT_EMAIL1 + " = \'" + email + "\' OR " +
//            						QiupuORM.UsersColumns.CONTACT_EMAIL2 + " = \'" + email + "\' OR " +
//            						QiupuORM.UsersColumns.CONTACT_EMAIL3 + " = \'" + email + "\'";
//            				Cursor userCursor = mContentResolver.query(QiupuORM.USERS_CONTENT_URI, USERS_LOOKUP_PROJECTION, where, null, null);
            				
            			    Cursor userCursor = mContentResolver.query(QiupuORM.PHONEEMAIL_CONTENT_URI, new String[]{PhoneEmailColumns.USERID}, where, null, null);
            				if (null != userCursor )
            				{
            					if(userCursor.moveToFirst()){
            						values.clear();
            						values.put(LookUpProvider.CONTACT_ID, contactId);
            						values.put(LookUpProvider.EMAIL, email);
//                            values.put(LookUpProvider.PHOTO_ID, cinfo.mPhotoId);
//                            values.put(LookUpProvider.EMAIL, cinfo.email);
            						values.put(LookUpProvider.TYPE, LOOKUP_FLAG_SCANNED);
            						do {
//            							values.put(LookUpProvider.NAME, userCursor.getString(userCursor.getColumnIndex(USERS_LOOKUP_PROJECTION[1])));
//            							values.put(LookUpProvider.UID, userCursor.getString(userCursor.getColumnIndex(USERS_LOOKUP_PROJECTION[0])));
            						    
            						    values.put(LookUpProvider.UID, userCursor.getString(userCursor.getColumnIndex(PhoneEmailColumns.USERID)));
            						    
            							mContentResolver.insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
            						} while (userCursor.moveToNext());
            						
            					}
            					userCursor.close();
            				}
            			}
            			
            		} while (emails.moveToNext());
            	}
                
                emails.close();
            }
        }

        private boolean isUserDatabaseInfoReady() {
            boolean ret = false;

            Cursor cursor = mContentResolver.query(QiupuORM.USERS_CONTENT_URI, new String[] {QiupuORM.UsersColumns.ID}, null, null, null);
            if (null != cursor) {
                ret = cursor.getCount() > 0;
                cursor.close();

            }
            return ret;
        }

        private void verifyContactsWithLocalUserDb() {
            if (null != mCursor) {
                if (isUserDatabaseInfoReady() && mCursor.moveToFirst()) {
                    long contactId;                    
                    do {
                        contactId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        if (needVerify(contactId)) {
                            deleteExistingLookupResult(contactId);
                            lookupContactsInfo(contactId);
                        }
                    } while (mCursor.moveToNext());

                    onAddressbookVerifyEnd(mAddressBookSync.handler);
                }
            }
        }
    }

    public boolean isEmptyLookupResult() {        
    	boolean ret = false;
    	
       	SQLiteDatabase db = LookUpProvider.mOpenHelper.getReadableDatabase();
       	String sql = "select count(*) as usercount from lookup" ;
       	Cursor cr = db.rawQuery(sql, null);
          
       	ret = cr.getCount() <=0;
        closeCursor(cr);           
        return ret ;
    }
    
    private static boolean mIsRunningVerifyContactWithSync4 = false;
    public void verifyContactsTypeWithSync4(Cursor contactsCursor, boolean isOnlySync4) {
        Log.d(TAG, "verifyContactsTypeWithSync4, enter.");
        if (!mIsRunningVerifyContactWithSync4) {
            if (!AddressBookSync.mIsLookupSessionTriggering /*&& isEmptyLookupResult()*/) {
            	mIsRunningVerifyContactWithSync4 = true;
                QiupuORM.sWorker.post(new VerifyContactWithSync4(contactsCursor, isOnlySync4));
            }
        }
    }
    
    private class VerifyContactWithSync4 implements Runnable {
        private Cursor mCursor;
        private final boolean mIsOnlySync4;
        public VerifyContactWithSync4(Cursor cursor, boolean isOnlySync4) {
            super();
            Log.d(TAG, "VerifyContactWithSync4, constructor.");
            mCursor = cursor;
            mIsOnlySync4 = isOnlySync4;
        }

        @Override
        public void run() {
            Log.d(TAG, "VerifyContactWithSync4, Runnable run.");
            if (null != mCursor && !mCursor.isClosed()) {
            	verifyContactsTypeWithSync4(mCursor);
            	if(mIsOnlySync4 == false) {
            		verifyContactsType(mCursor);
            	}else {
            		mCursor.close();
            		mCursor = null;
            	}
            }
            mIsRunningVerifyContactWithSync4 = false;
        }
        
        private void verifyContactsTypeWithSync4(Cursor contactsCursor) {
        	if (null != contactsCursor) {
                if (contactsCursor.moveToFirst()) {
                    long contactId;
                    ContentValues values = new ContentValues();
                    values.put(LookUpProvider.TYPE, LOOKUP_FLAG_SCANNED);
                    String[] queryArg;
                    do {
                        contactId = contactsCursor.getLong(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        long borqsId = ContactUtils.getBorqsIdFromContact(mAddressBookSync.mContext, contactId);
                        if(borqsId > 0) {
    						values.put(LookUpProvider.UID, borqsId);
    						
//    						String where = LookUpProvider.CONTACT_ID + " = \'" + contactId + "\'";
                            queryArg = new String[] {String.valueOf(contactId)};
                            final int updateCount = mContentResolver.update(LookUpProvider.CONTENT_LOOKUP_URI, values, queryContactsIdPrefix, queryArg);
                            if (updateCount <= 0) {
                                values.put(LookUpProvider.CONTACT_ID, contactId);
                                mContentResolver.insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
                            }
//                        	Cursor oneUser = mContentResolver.query(LookUpProvider.CONTENT_LOOKUP_URI, null, where, null, null);
//                            if(oneUser != null && oneUser.getCount() > 0){
//                            	mContentResolver.update(LookUpProvider.CONTENT_LOOKUP_URI, values, where, null);
//                            	oneUser.close();
//                            }else{
//                            	values.put(LookUpProvider.CONTACT_ID, contactId);
//                            	Uri uri = mContentResolver.insert(LookUpProvider.CONTENT_LOOKUP_URI, values);
//                            }
                        }
                    } while (contactsCursor.moveToNext());
                }
            }
        }
    }
    
    public void tryToUploadALlContacts(Context context) {
        Log.d(TAG, "tryToUploadALlContacts, enter.");
    	if(!AddressBookSync.mIsLookupSessionTriggering &&
    			SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END.equals(QiupuORM.getSyncContactStatus(mAddressBookSync.mContext))) {
    		Log.d(TAG, "tryToUploadALlContacts**************");
    		QiupuORM.setSyncContactStatus(mAddressBookSync.mContext, "");
    		lookupAllContacts();
    		verifyContactsTypeWithSync4(ContactUtils.getContacts(context), true);
    	}
    }

	@Override
	public void intentToAddressBook(Message msg) {
        Log.d(TAG, "intentToAddressBook, enter.");
		String action = msg.getData().getString("sync_action");
		if(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN.equals(action)) {
//			if(mSyncTask != null) {
//				mSyncTask.cancel(true);
//			}
		}else if(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END.equals(action)) {
			QiupuORM.setSyncContactStatus(mAddressBookSync.mContext, "");
			lookupAllContacts();
			verifyContactsTypeWithSync4(ContactUtils.getContacts(mAddressBookSync.mContext), true);
		}
	}

    static void onAddressbookSyncEnd(AddressBookSync addressBookSync, Message msg) {
        updateActivityUI(addressBookSync.handler, STATUS_DO_ALL_OK, msg);

        boolean suc = null == msg ? false : msg.getData().getBoolean("RESULT");
        if (suc) {
            updateFinalUserTypeLookup(addressBookSync.mContext);
            if (PeopleLookupHelper.SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END.equals(QiupuORM.getSyncContactStatus(addressBookSync.mContext))) {
                // if syncML completed between addressbook sync, will addressbook sync again
                QiupuORM.setSyncContactStatus(addressBookSync.mContext, "");
                addressBookSync.alarmAddressBookComing(true);
            }
        }
    }

    static void onAddressbookGetRefresh(AddressBookSync.AddressbookHandler handler, Message msg) {
        updateActivityUI(handler,STATUS_DO_ONE_LOOP_OK, msg);
    }

    static void onAddressbookVerifyEnd(AddressBookSync.AddressbookHandler handler) {
        updateActivityUI(handler, CONTENTOBSERVER_CHANGED, null);
    }

    static void onAddressbookSyncStart(AddressBookSync.AddressbookHandler handler) {
        updateActivityUI(handler,STATUS_DOING, null);
    }

    static void setupContactsLookup(Context context, List<ContactSimpleInfo> contacts) {
        ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
        for (ContactSimpleInfo cinfo : contacts) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "Contactinfo : " + cinfo.mContactId + " " + cinfo.display_name_primary);
            ContentValues values = new ContentValues();
            values.put(LookUpProvider.CONTACT_ID, cinfo.mContactId);
            values.put(LookUpProvider.NAME, cinfo.display_name_primary);
            values.put(LookUpProvider.PHONE, cinfo.phone_number);
            values.put(LookUpProvider.PHOTO_ID, cinfo.mPhotoId);
            values.put(LookUpProvider.EMAIL, cinfo.email);
            values.put(LookUpProvider.TYPE, LOOKUP_FLAG_NONE);
            bulkInsertList.add(values);
        }

        if (bulkInsertList.size() > 0) {
            ContentValues[] valueArray = new ContentValues[bulkInsertList.size()];
            valueArray = bulkInsertList.toArray(valueArray);

            ContentResolver cr = context.getContentResolver();
            cr.bulkInsert(LookUpProvider.CONTENT_LOOKUP_URI, valueArray);
        }
    }
}
