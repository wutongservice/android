package com.borqs.qiupu.util;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.QiupuAccountInfo;
import twitter4j.QiupuUser;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;

import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.account.login.service.ConstData;
import com.borqs.common.view.RequestContactInfoSimpleView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageCacheManager;

public class ContactUtils {

    private static final String TAG = "ContactUtils";
    private static final String ACCOUNT_TYPE = "com.borqs";
    private static final String SELECTION_ALL = RawContacts.DELETED + "=0";
    private static final String SELECTION_ALL_BORQS = RawContacts.ACCOUNT_TYPE
            + "=\'" + AccountServiceAdapter.BORQS_ACCOUNT_TYPE + "\' and "
            + SELECTION_ALL;

    static String[] androidprojection = new String[] { ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_ID,
            ContactsContract.Contacts.SORT_KEY_PRIMARY };
    
    static String[] oms20Projection = new String[] { ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_ID,
        ContactsContract.Contacts.DISPLAY_NAME };
    
    static String[] projection = androidprojection;
    public static String sortCollom = ContactsContract.Contacts.SORT_KEY_PRIMARY;

    private static final Uri DATA_CONTENT_URI = Uri.withAppendedPath(Uri.parse("content://" + ContactsContract.AUTHORITY), "profile/data");
    private static final Uri RAW_CONTACT_URI = Uri.withAppendedPath(Uri.parse("content://" + ContactsContract.AUTHORITY), "profile/raw_contacts");

    static {
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
            String platform = getplatform("apps.setting.platformversion");
            if (platform.contains("OPhone")) {
                Log.d(TAG, "shit I am OPhone 2.0");
            } else {
                Log.d(TAG, "sdk version older than FROYO: " + android.os.Build.VERSION.SDK_INT);
            }
    		
    		isOPhone20 = true;
    		projection = oms20Projection;
        	sortCollom = ContactsContract.Contacts.DISPLAY_NAME;	
    	}
    }	
    
    public static boolean isOPhone20()
    {
    	return isOPhone20;
    }
    static boolean isOPhone20 = false;
	static Method getInMethod;
	public static String getplatform(String key)
	{
		try {
			getInMethod = Class.forName("android.os.SystemProperties").getMethod("get", new Class[] {String.class});
			return (String) getInMethod.invoke(null, new Object[] {key});
		} catch (Exception e) {
		}
		return "";
	}
    
    public static Cursor getContacts(Context context) {

        ContentResolver cr = context.getContentResolver();        

//        String where = ContactsContract.Contacts.DISPLAY_NAME
//                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
//                + "!= '' and " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
        
        String where = ContactsContract.Contacts.DISPLAY_NAME
                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
                + "!= ''";
        
        
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                projection, where, null, sortCollom);
        return cur;
    }
    
    public static Cursor getContactsNotInMyCircle(Context context, String inmyCircleContactId) {

        ContentResolver cr = context.getContentResolver();

        String where = ContactsContract.Contacts.DISPLAY_NAME
                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
                + "!= '' and " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1 and "
                + ContactsContract.Contacts._ID + " not in (" + inmyCircleContactId + ")";
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                projection, where, null, sortCollom );
        return cur;
    }

    public static Cursor getContactIds(ContentResolver contentResolver) {
        String[] projection = new String[] { ContactsContract.Contacts._ID };

        String where = ContactsContract.Contacts.DISPLAY_NAME
                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
                + "!= '' and " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                projection, where, null, sortCollom);
        return cursor;
    }

    public static Cursor searchContactByKey(Context context, String key) {
    	key = key.replace("'", "");
        ContentResolver cr = context.getContentResolver();
        
        String where = ContactsContract.Contacts.DISPLAY_NAME
                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
                + "!= '' and " + ContactsContract.Contacts.IN_VISIBLE_GROUP +" = 1 and " + ContactsContract.Contacts.DISPLAY_NAME
                + " like '%" + key + "%'";
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                projection, where, null, sortCollom );
        return cur;
    }
    
    public static Cursor searchContactsNotInMyCircleByKey(Context context, String inmyCircleContactId, String key) {
    	key = key.replace("'", "");
        ContentResolver cr = context.getContentResolver();
        
        String where = ContactsContract.Contacts.DISPLAY_NAME
                + " IS NOT NULL and " + ContactsContract.Contacts.DISPLAY_NAME
                + "!= '' and " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1 and "
                + ContactsContract.Contacts._ID + " not in (" + inmyCircleContactId + ") and " + ContactsContract.Contacts.DISPLAY_NAME
                + " like '%" + key + "%'";
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                projection, where, null, sortCollom );
        return cur;
    }

    public static void generatePhoneEmailSetById(Context context,
            long contactId, HashSet<String> phoneSet, HashSet<String> emailSet,
            boolean isSelect) {
        // get phone number
        Cursor phones = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
        if (phones != null && phones.moveToFirst()) {
            do {
                String phone = phones.getString(0);
                if (StringUtil.isValidString(phone)) {
                    if (isSelect)
                        phoneSet.add(phone);
                    else
                        phoneSet.remove(phone);
                }
            } while (phones.moveToNext());
        }
        closeCursor(phones);

        // get email
        Cursor emails = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
        if (emails != null && emails.moveToFirst()) {
            do {
                String email = emails.getString(0);// "data1"
                if (StringUtil.isValidString(email)) {
                    if (isSelect)
                        emailSet.add(email);
                    else
                        emailSet.remove(email);
                }
            } while (emails.moveToNext());
        }
        closeCursor(emails);
    }
    
    public static long generateSelectResultById(Context context,
            long contactId, boolean isSelect, HashSet<String> selectResult, HashMap<String, String> mSelectMap) {
        // get phone number
        Cursor phones = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
        
     // get email
        Cursor emails = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
        
        int count = 0;
        long tmpContactId = 0;
        boolean hasPhone = false;
        boolean hasEmail = false;
        if(phones != null && phones.getCount() > 0) {
            count = phones.getCount();
            hasPhone = true;
        }
        
        if(emails != null && emails.getCount() > 0) {
            count = count + emails.getCount();
            hasEmail = true;
        }
        
        if(count < 1) {
            if(QiupuConfig.LOGD) Log.d(TAG, "phone and email count < 1");
            tmpContactId = -1;
        }else if(count == 1) {
            tmpContactId = -1;
            String name = getContactName(context, contactId);
            if(hasPhone) {
                phones.moveToFirst();
                String phone = phones.getString(0);
                if (StringUtil.isValidString(phone)) {
                    if (isSelect) {
                        selectResult.add(phone);
                        mSelectMap.put(phone, name);
                    }
                    else{
                        selectResult.remove(phone);
                        mSelectMap.remove(phone);
                    }
                }
            }else if(hasEmail) {
                emails.moveToFirst();
                String email = emails.getString(0);// "data1"
                if (StringUtil.isValidString(email)) {
                    if (isSelect){
                        selectResult.add(email);
                        mSelectMap.put(email, name);
                    }
                    else {
                        selectResult.remove(email);
                        mSelectMap.remove(email);
                    }
                }
            }
        }else {
            if(QiupuConfig.LOGD) Log.d(TAG, "phone and email count > 1, return contactid");
            tmpContactId = contactId; 
        }
        closeCursor(phones);
        closeCursor(emails);
        
        return tmpContactId;
    }

    public static ArrayList<QiupuUser> generateMulitPhoneEmailUserList(Context context, String contactIds ) {
        if(StringUtil.isValidString(contactIds) == false) {
            Log.d(TAG, "select mulit phone email user is null");
            return null;
        }
        
        String[] ids = contactIds.split(",");
        ArrayList<QiupuUser> userlist = new ArrayList<QiupuUser>();
        for(int i=0; i<ids.length; i++) {
            QiupuUser user = new QiupuUser();
            user.nick_name = getContactName(context, Long.parseLong(ids[i]));
            String tmpId = ids[i];
            Cursor phones = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{tmpId}, null);
            
            if (phones != null && phones.moveToFirst()) {
                user.phoneList = new ArrayList<QiupuAccountInfo.PhoneEmailInfo>();
                do {
                    String phone = phones.getString(0);
                    if (StringUtil.isValidString(phone)) {
                        QiupuAccountInfo.PhoneEmailInfo phoneinfo = new QiupuAccountInfo.PhoneEmailInfo();
                        phoneinfo.info = phone;
                        user.phoneList.add(phoneinfo);
                    }
                } while (phones.moveToNext());
            }
            closeCursor(phones);
            
         // get email
            Cursor emails = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{tmpId}, null);
            if (emails != null && emails.moveToFirst()) {
                user.emailList = new ArrayList<QiupuAccountInfo.PhoneEmailInfo>();
                do {
                    String email = emails.getString(0);// "data1"
                    if (StringUtil.isValidString(email)) {
                        QiupuAccountInfo.PhoneEmailInfo phoneinfo = new QiupuAccountInfo.PhoneEmailInfo();
                        phoneinfo.info = email;
                        user.emailList.add(phoneinfo);
                    }
                } while (emails.moveToNext());
            }
            closeCursor(emails);

            userlist.add(user);
        }
        
        return userlist;
    }
    
    private static boolean isCursorEmpty(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static HashMap<String, Integer> getPhoneAndEmails(Context context, long contactId) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" , new String[]{String.valueOf(contactId)}, null);

        Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);

        int phone_type = 1;
        int email_type = 2;

        HashMap<String, Integer> contact = new HashMap<String, Integer>();

        if (!isCursorEmpty(phones)) {
            phones.moveToFirst();
            for (int i = 0; i < phones.getCount(); i++) {
                String phone = phones.getString(0);
                contact.put(phone, phone_type);
                phones.moveToNext();
            }
        }

        if (!isCursorEmpty(emails)) {
            emails.moveToFirst();
            for (int i = 0; i < emails.getCount(); i++) {
                String email = emails.getString(0);
                contact.put(email, email_type);
                emails.moveToNext();
            }
        }
        closeCursor(phones);
        closeCursor(emails);
        return contact;
    }

    public static String[] getPhones(Context context, long contactId) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" , new String[]{String.valueOf(contactId)}, null);
        if (isCursorEmpty(phones)) {
            return null;
        }
        String[] phone = new String[phones.getCount()];

        phones.moveToFirst();
        for (int i = 0; i < phones.getCount(); i++) {
            phone[i] = phones.getString(0);
            phones.moveToNext();
        }
        closeCursor(phones);

        return phone;
    }

    public static String[] getEmails(Context context, long contactId) {
        Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?" , new String[]{String.valueOf(contactId)}, null);
        if (isCursorEmpty(emails)) {
            return null;
        }
        String[] email = new String[emails.getCount()];

        emails.moveToFirst();
        for (int i = 0; i < emails.getCount(); i++) {
            email[i] = emails.getString(0);
            emails.moveToNext();
        }
        closeCursor(emails);

        return email;
    }

    public static ArrayList<String> getPhonesList(Context context, long contactId) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" , new String[]{String.valueOf(contactId)}, null);
        if (isCursorEmpty(phones)) {
            return null;
        }
        ArrayList<String> phone = new ArrayList<String>();

        phones.moveToFirst();
        for (int i = 0; i < phones.getCount(); i++) {
            phone.add(phones.getString(0));
            phones.moveToNext();
        }
        closeCursor(phones);

        return phone;
    }

    public static ArrayList<String> getEmailsList(Context context, long contactId) {
        Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
        if (isCursorEmpty(emails)) {
            return null;
        }
        ArrayList<String> email = new ArrayList<String>();

        emails.moveToFirst();
        for (int i = 0; i < emails.getCount(); i++) {
            email.add(emails.getString(0));
            emails.moveToNext();
        }
        closeCursor(emails);

        return email;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }

    public static void setContactItemView(Context context, String name, ArrayList<String> phones, ArrayList<String> emails, LinearLayout vcard) {
        String contactWay = null;

        if (phones != null) {
            for (int i = 0; i < phones.size(); i++) {
                contactWay = context.getResources().getString(R.string.phone_way) + phones.get(i);
                RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(context, contactWay);
                vcard.addView(info);
            }
        }

        if (emails != null) {
            for (int i = 0; i < emails.size(); i++) {
                contactWay = context.getResources().getString(R.string.email_way) + emails.get(i);
                RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(context, contactWay);
                vcard.addView(info);
            }
        }

        if (TextUtils.isEmpty(name)) {
            if (phones != null) {
                name = phones.get(0);
            } else {
                if (emails != null) {
                    name = emails.get(0);
                } else {
                    Log.e("ContactUtils", "getContactInfo() ERROR, No such contact in system");
                }
            }
        }
    }

    public static long getProfileId(Context context) {
        String[] projection = {RawContacts.CONTACT_ID};
        ContentResolver cr = context.getContentResolver();
        Cursor profile = null;
        Cursor cursor = null;
        try {
            profile = cr.query(DATA_CONTENT_URI, projection, null, null, null);
            if (isCursorEmpty(profile)) {
                return -1;
            } else {
                profile.moveToFirst();
                long contactId = profile.getLong(0);
                String selection = RawContacts.CONTACT_ID + " = " + contactId + " AND " 
                        + RawContacts.ACCOUNT_TYPE + " = \'com.borqs\'";
                cursor = cr.query(RAW_CONTACT_URI, new String[]{RawContacts._ID}, selection, null, null);
                if (isCursorEmpty(cursor)) {
                    Log.i(TAG, "cursor is null");
                    return -1;
                } else {
                    cursor.moveToFirst();
                    long _id = cursor.getLong(0);
                    return _id;
                }
            }
        } finally {
            closeCursor(profile);
            closeCursor(cursor);
        }
    }

    public static ArrayList<String> getProfilePhones(Context context, long contactId) {
        Cursor phones = null;
        try {
            phones = context.getContentResolver().query(DATA_CONTENT_URI,
                    new String[] {"data1"},
                    Data.RAW_CONTACT_ID + " = " + contactId + " AND "
                    + " mimetype_id " + " = 5 ", null, null);
            if (isCursorEmpty(phones)) {
                return null;
            }
            ArrayList<String> phoneList = new ArrayList<String>();
            phones.moveToFirst();
            for (int i = 0; i < phones.getCount(); i++) {
                phoneList.add(phones.getString(0));
                phones.moveToNext();
            }
            return phoneList;
        } finally {
            closeCursor(phones);
        }
    }

    public static ArrayList<String> getProfileEmails(Context context, long contactId) {
        Cursor emails = null;
        try {
            emails = context.getContentResolver().query(DATA_CONTENT_URI,
                    new String[] {"data1"},
                    Data.RAW_CONTACT_ID + " = " + contactId + " AND "
                            + " mimetype_id " + " = 1 ", null, null);
            if (isCursorEmpty(emails)) {
                return null;
            }
            ArrayList<String> emailList = new ArrayList<String>();
            emails.moveToFirst();
            for (int i = 0; i < emails.getCount(); i++) {
                emailList.add(emails.getString(0));
                emails.moveToNext();
            }
            return emailList;
        } finally {
            closeCursor(emails);
        }
    }

    public static String getProfileName(Context context, long contactId) {
        Cursor nameCursor = null;
        try {
            nameCursor = context.getContentResolver().query(DATA_CONTENT_URI,
                    new String[] {"data1"},
                    Data.RAW_CONTACT_ID + " = " + contactId + " AND "
                            + " mimetype_id " + " = 7 ", null, null);
            if (isCursorEmpty(nameCursor)) {
                return null;
            }
            nameCursor.moveToFirst();
            return nameCursor.getString(0);
        } finally {
            closeCursor(nameCursor);
        }
    }
    
    static HashMap<Long, Bitmap>ContactPhoto = new HashMap<Long, Bitmap>();
    
    static ImageCacheManager cm = ImageCacheManager.buildNewInstance(10000, false);
    static Bitmap getCacheBitMap(String key)
    {
    	ImageCacheManager.ImageCache ic = cm.getCache(key);
    	if(ic != null)
    	{
    		return ic.bmp;
    	}
    	return null;
    }
    
    public static void desposeCachedBitMap()
    {
    	cm.despose();
    }
    
    //do we really need this,    
    static HashMap<Long, Boolean>noImageRecord = new HashMap<Long, Boolean>();
    
    public static Bitmap getContactPhoto(Context context, long contactId) {
    	Boolean bl = noImageRecord.get(new Long(contactId));
    	if(bl != null && bl.booleanValue() == true)
    		return null;
    	
        Bitmap contactPhoto = getCacheBitMap(String.valueOf(contactId));
        if(contactPhoto == null )
        {
	        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
	        contactPhoto = BitmapFactory.decodeStream(input);
	        
	        if(contactPhoto == null)
	        {
	        	noImageRecord.put(new Long(contactId), new Boolean(true));	            
	        }
	        else
	        {
	        	cm.addCache(String.valueOf(contactId), contactPhoto);
	        }
        }
        
        return contactPhoto;
    }
    
    public static String getDataIdWithActionType(Context context, String contactId, String actionType) {

    	Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[] { ContactsContract.Data._ID, ContactsContract.Data.DATA4 },
                ContactsContract.Data.CONTACT_ID + " = ?" , new String[]{String.valueOf(contactId)}, null);

    	String dataId = "" ; 
        if(cursor != null) {
        	if (cursor.getCount() > 0 )
        	{
        		if(cursor.moveToFirst())
        		{
	        		do {
	        			String action = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4));
	        			if(StringUtil.isEmpty(action) == false && action.equals(actionType)){
	        				dataId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
	        				break;
	        			}
	        			
	        		} while(cursor.moveToNext());
        		}
        	}
        	cursor.close();
        }
        
        return dataId;
    }
    
    public static long getBorqsIdFromContact(Context context, long contactId) {

    	Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[] { ContactsContract.RawContacts.SYNC4, ContactsContract.RawContacts.ACCOUNT_TYPE },
                ContactsContract.RawContacts.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);

        long borqsId = -1 ;
        
        if(cursor != null) {
        	if (cursor.getCount() > 0)
        	{
        		if(cursor.moveToFirst()){
	        		long tempBorqsId = -1;
	        		String tempAccountType;
	        		do {
	        			tempAccountType = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
	        			if(ACCOUNT_TYPE.equals(tempAccountType)) {
	        				tempBorqsId = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts.SYNC4));
	        			}
	        			if(QiupuConfig.DBLOGD)Log.d(TAG, "tempBorqsId: " + tempBorqsId);
	        			
	        			if (borqsId < tempBorqsId) {
	        				borqsId = tempBorqsId;
	        			}
	        		} while(cursor.moveToNext());
	        	}
        	}
        	cursor.close();
        }
        return borqsId;
    }
    
    public static String getContactName(Context context, long contactId) {
    	Cursor contact = null;
    	try {
    		contact = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    new String[] { ContactsContract.Contacts.DISPLAY_NAME },
                    ContactsContract.Contacts._ID + " = ?", new String[]{String.valueOf(contactId)}, null);

            String name = "";
             
            if (!isCursorEmpty(contact)) {
            	contact.moveToFirst();
            	name = contact.getString(0);
            }
            return name;
    	} finally {
    		closeCursor(contact);
    	}
    }
    
    public static int getBorqsIdfromCalendar(Context context) {  
    	int tmpId = -1;
    	try {
    		String where = null;
    		final ContentResolver cr = context.getContentResolver();
    		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
    			where = CalendarContract.Calendars.ACCOUNT_TYPE + " = '"+ConstData.BORQS_ACCOUNT_TYPE+ "'";
    		}else {
    			where = "_sync_account_type = '" +ConstData.BORQS_ACCOUNT_TYPE+ "'"; 
    		}
    		
    		Cursor myCursor = cr.query(CalendarMappingUtils.CALENDAR_URL, new String[]{CalendarContract.Calendars._ID}, where, null, null);
    		if(myCursor != null) {
    			if(myCursor.getCount() > 0) {
    				myCursor.moveToFirst();
    				tmpId = myCursor.getInt(0);
    			}
    			myCursor.close();
    			myCursor = null;
    		}
		} catch (Exception e) {
			Log.d(TAG, "getBorqsIdfromCalendar : query calendar failed " + e.getMessage());
		}
		return tmpId;
    }
}
