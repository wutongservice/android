package com.borqs.account.service.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.ui.BorqsAccountConfig;
import twitter4j.QiupuUser;
import twitter4j.util.StringUtil;

public class BorqsAccountORM {
	private final static String TAG = "BorqsAccountORM";
	private static BorqsAccountORM _instance;
	private Context mContext;
	private static String AUTHORITY = BorqsAccountProvider.AUTHORITY;
	
	public static final String LAST_SYNC_USERINFO_TIME         = "last_sync_userinfo_time";
	public static final String LAST_SYNC_ADDRESSBOOK_TIME      = "LAST_SYNC_ADDRESSBOOK_TIME";
	public static final String LAST_SYNC_ADDRESSBOOK_INTERVAL  = "last_sync_ADDRESS_interval";
	public static final String LAST_LOGIN_USER = "last_login_user";
	public static final Uri ACCOUNT_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/account");
	public static final Uri USERS_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/users");
	public static final Uri SETTINGS_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/settings");
		
	private BorqsAccountORM(Context context) {
		mContext = context;
	}
	
	public static BorqsAccountORM getInstance(Context context) {
		if(_instance == null)
		{
			_instance = new BorqsAccountORM(context);
		}
		
		return _instance;
	}

	static final String[] ACCOUNT_PROJECTION = {
		AccountColumns._ID,
		AccountColumns.SESSIONID,
		AccountColumns.UID,
		AccountColumns.USERNAME,
		AccountColumns.CREATETIME,
		AccountColumns.MODIFYTIME,
		AccountColumns.NICKNAME,
		AccountColumns.SCREENNAME,
		AccountColumns.VERIFY
	};
	
	public static final class AccountColumns
	{
		public static final String _ID = "_id";
		public static final String SESSIONID = "sessionid";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String CREATETIME = "createtime";
		public static final String MODIFYTIME = "modifytime";
		public static final String NICKNAME = "nickname";
		public static final String SCREENNAME = "screenname";
		public static final String VERIFY = "verify";
	}
	
	final static String[] USERS_INFO_PROJECTION={
		UsersColumns.ID,
		UsersColumns.USERID,
		UsersColumns.USERNAME,
		UsersColumns.RELATIONSHIP,
		UsersColumns.NICKNAME,
		UsersColumns.PHONE_NUMBER,
		UsersColumns.DATA_OF_BIRTH,
		UsersColumns.COMPANY,
		UsersColumns.PROVINCE,
		UsersColumns.CITY,
		UsersColumns.CREATED_AT,
		UsersColumns.LAST_VISIT_TIME,
		UsersColumns.VERIFY_CODE,
		UsersColumns.VERIFIED,
		UsersColumns.DOMAIN,
		UsersColumns.PROFILE_IMAGE_URL,
		UsersColumns.PROFILE_SIMAGE_URL,
		UsersColumns.PROFILE_LIMAGE_URL,
		UsersColumns.LOCATION,
		UsersColumns.DESCRIPTION,
		UsersColumns.URL,
		UsersColumns.GENDER,
		UsersColumns.FRIENDS_COUNT,
		UsersColumns.FOLLOWERS_COUNT,
		UsersColumns.FAVOURITES_COUNT,
		UsersColumns.APP_COUNT,
		UsersColumns.STATUS
	};
	
	public final static class UsersColumns{
		public final static String ID                     = "_id";
		public final static String USERID 		          = "uid";
		public final static String USERNAME        		  = "name";
		public final static String RELATIONSHIP    	      = "relationship";
		public final static String NICKNAME               = "nickname";
		public final static String PHONE_NUMBER           = "phone_number";
		public final static String DATA_OF_BIRTH          = "date_of_birth";
		public final static String COMPANY                ="company";
		public final static String PROVINCE               = "province";
		public final static String CITY 		          = "city";
		public final static String CREATED_AT        	  = "created_at";
		public final static String LAST_VISIT_TIME    	  = "last_visit_time";
		public final static String VERIFY_CODE            = "verify_code";
		public final static String VERIFIED               = "verified";
		public final static String DOMAIN                 = "domain";
		public final static String PROFILE_IMAGE_URL      ="profile_image_url";
		public final static String PROFILE_SIMAGE_URL     ="profile_simage_url";
		public final static String PROFILE_LIMAGE_URL     = "profile_limage_url";
		public final static String LOCATION 		      = "location";
		public final static String DESCRIPTION        	  = "description";
		public final static String URL    	              = "url";
		public final static String GENDER                 = "gender";
		public final static String FRIENDS_COUNT          = "friends_count";
		public final static String FOLLOWERS_COUNT        = "followers_count";
		public final static String FAVOURITES_COUNT       ="favourites_count";
		public final static String APP_COUNT              = "app_count";
		public final static String STATUS                 ="status";
	}
	
	 //settings
    public static class SettingsCol{
        public static final String ID      = "_id";
        public static final String Name    = "name";
        public static final String Value   = "value";
    }
    
    public static String[]settingsProject =  new String[]{
        "_id",
        "name",
        "value",
    };
	
    
	public BorqsAccount getAccount()
	{
		BorqsAccount result = null;
		Cursor mCursor = mContext.getContentResolver().query(ACCOUNT_CONTENT_URI, ACCOUNT_PROJECTION, null, null, null);
		if(mCursor != null)
		{
			if(mCursor.moveToFirst())
			{
				result = formatAccount(mCursor);
			}
			mCursor.close();
			mCursor = null;
		}
		
		return result;
	}
	
	public long isExistAccount()
	{
		long isExistID = -1;
		String[] tpProjection = {AccountColumns._ID};
		Cursor mCursor = mContext.getContentResolver().query(ACCOUNT_CONTENT_URI, tpProjection, null, null, null);
		if(mCursor != null )
		{
			if(mCursor.moveToFirst())
			{
				isExistID = mCursor.getLong(mCursor.getColumnIndex(AccountColumns._ID));
			}
			mCursor.close();
			mCursor = null;
		}
		
		return isExistID;
	}
	
	public boolean updateAccount(BorqsAccount newAccount)
	{
		BorqsAccount preAccount = getAccount();
		if(BorqsAccountConfig.LOGD)Log.d(TAG, "updateAccount with: "+ newAccount);
	    final ContentResolver cr = mContext.getContentResolver();
	    ContentValues values = formatAccountContentValues(newAccount);
	    long existID = isExistAccount();
	    if(existID >= 0)
	    {
	    	String where = AccountColumns._ID + "="+existID;
	    	cr.update(ACCOUNT_CONTENT_URI, values, where, null);
	    }
	    else
	    {
	    	cr.insert(ACCOUNT_CONTENT_URI, values);
	    }

        //no usage, BUG, which cause too many call for address book upload
	    //AccountObserver.login();
	    
	    if(preAccount != null &&  preAccount.sessionid != null
                && preAccount.sessionid.equals(null == newAccount.sessionid ? "" : newAccount.sessionid))
	    {
	    	Log.d(TAG, "I am the same user=" + newAccount);
	    }
	    else
	    {
	    	Log.d(TAG, "I am not the same, need re-sync the data between phone and server user=" + newAccount);
	    	removeSetting(LAST_SYNC_USERINFO_TIME);
	        removeSetting(LAST_SYNC_ADDRESSBOOK_TIME);
	    }
	    return true;
	}
	
	private ContentValues formatAccountContentValues(BorqsAccount mAccount) {
		ContentValues cv = new ContentValues();
		if (mAccount == null) {
            Log.w(TAG, "formatAccountContentValues, use a dumb account instead incoming null.");
            mAccount = new BorqsAccount();
        }
		cv.put(AccountColumns.SESSIONID, mAccount.sessionid);
		cv.put(AccountColumns.UID, mAccount.uid);
		cv.put(AccountColumns.USERNAME, mAccount.username);
		cv.put(AccountColumns.CREATETIME, mAccount.createtime);
		cv.put(AccountColumns.MODIFYTIME, mAccount.modifytime);
		cv.put(AccountColumns.NICKNAME, mAccount.nickname);
		cv.put(AccountColumns.SCREENNAME, mAccount.screenname);
		cv.put(AccountColumns.VERIFY, mAccount.verify);

		return cv; 
	}  

	private BorqsAccount formatAccount(Cursor mCursor) {
		BorqsAccount result = new BorqsAccount();
		
		result._id = mCursor.getLong(mCursor.getColumnIndex(AccountColumns._ID));
		result.sessionid = mCursor.getString(mCursor.getColumnIndex(AccountColumns.SESSIONID));
		result.uid = mCursor.getLong(mCursor.getColumnIndex(AccountColumns.UID));
		result.username = mCursor.getString(mCursor.getColumnIndex(AccountColumns.USERNAME));
		result.createtime = mCursor.getLong(mCursor.getColumnIndex(AccountColumns.CREATETIME));
		result.modifytime = mCursor.getLong(mCursor.getColumnIndex(AccountColumns.MODIFYTIME));
		result.nickname = mCursor.getString(mCursor.getColumnIndex(AccountColumns.NICKNAME));
		result.screenname = mCursor.getString(mCursor.getColumnIndex(AccountColumns.SCREENNAME));
		result.verify = mCursor.getInt(mCursor.getColumnIndex(AccountColumns.VERIFY));
		
		return result;
	}

    public void logout() {
       deleteAccount();
    }

    private void deleteAccount() {        
        mContext.getContentResolver().delete(ACCOUNT_CONTENT_URI, null,null);
        mContext.getContentResolver().delete(USERS_CONTENT_URI, null,null);
        mContext.getContentResolver().delete(SETTINGS_CONTENT_URI, null,null);        
    }
	
    public int getUserFriendsCount(int relationship){
    	int count = 0;
    	String where =  UsersColumns.RELATIONSHIP +"="+relationship ;
    	Cursor mCursor = mContext.getContentResolver().query(USERS_CONTENT_URI, null, where, null, null);
    	if(mCursor != null) {
    		count = mCursor.getCount();
    		
    		mCursor.close();
    		mCursor = null;
    	}
    	
    	return count;
    }
    
    private ContentValues createUserInformationValues(QiupuUser userinfo){
    	ContentValues cv = new ContentValues();
		cv.put(UsersColumns.USERID, userinfo.uid);
		cv.put(UsersColumns.USERNAME, userinfo.name);
//		cv.put(UsersColumns.RELATIONSHIP, userinfo.relationship);
		cv.put(UsersColumns.NICKNAME, userinfo.nick_name);
		cv.put(UsersColumns.PHONE_NUMBER, userinfo.phone_number);
		cv.put(UsersColumns.DATA_OF_BIRTH, userinfo.date_of_birth);
		cv.put(UsersColumns.COMPANY, userinfo.company);
		cv.put(UsersColumns.PROVINCE, userinfo.province);
		cv.put(UsersColumns.CITY, userinfo.city);
		cv.put(UsersColumns.CREATED_AT, userinfo.created_at);
		cv.put(UsersColumns.LAST_VISIT_TIME, userinfo.last_visit_time);
		cv.put(UsersColumns.VERIFY_CODE, userinfo.verify_code);
		cv.put(UsersColumns.VERIFIED, userinfo.verified);
//		cv.put(UsersColumns.DOMAIN, userinfo.domain);
		cv.put(UsersColumns.PROFILE_IMAGE_URL, userinfo.profile_image_url);
		cv.put(UsersColumns.PROFILE_SIMAGE_URL, userinfo.profile_simage_url);
		cv.put(UsersColumns.PROFILE_LIMAGE_URL, userinfo.profile_limage_url);
		cv.put(UsersColumns.LOCATION, userinfo.location);
		cv.put(UsersColumns.DESCRIPTION, userinfo.description);
		cv.put(UsersColumns.URL, userinfo.url);
		cv.put(UsersColumns.GENDER, userinfo.gender);
		cv.put(UsersColumns.FRIENDS_COUNT, userinfo.friends_count);
		cv.put(UsersColumns.FOLLOWERS_COUNT, userinfo.followers_count);
		cv.put(UsersColumns.FAVOURITES_COUNT, userinfo.favorites_count);
		cv.put(UsersColumns.APP_COUNT, userinfo.app_count);
		cv.put(UsersColumns.STATUS, userinfo.status);
		return cv;
    }
    
    public QiupuUser createUserInformation(Cursor mCursor) {
    	QiupuUser result = new QiupuUser();
		result.uid = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.USERID));
		result.name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.USERNAME));
//		result.relationship = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.RELATIONSHIP));
		result.nick_name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.NICKNAME));
		result.phone_number = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PHONE_NUMBER));
		result.date_of_birth = mCursor.getString(mCursor.getColumnIndex(UsersColumns.DATA_OF_BIRTH));
		result.company=mCursor.getString(mCursor.getColumnIndex(UsersColumns.COMPANY));
		result.province = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.PROVINCE));
		result.city = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.CITY));
		result.created_at = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.CREATED_AT));
		result.last_visit_time = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.LAST_VISIT_TIME));
		result.verify_code = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.VERIFY_CODE));
		result.verified = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.VERIFIED));
//		result.domain=mCursor.getString(mCursor.getColumnIndex(UsersColumns.DOMAIN));
		result.profile_image_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
		result.profile_simage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_SIMAGE_URL));
		result.profile_limage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_LIMAGE_URL));
		result.location = mCursor.getString(mCursor.getColumnIndex(UsersColumns.LOCATION));
		result.description = mCursor.getString(mCursor.getColumnIndex(UsersColumns.DESCRIPTION));
		result.url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.URL));
		result.gender=mCursor.getString(mCursor.getColumnIndex(UsersColumns.GENDER));
		result.friends_count = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.FRIENDS_COUNT));
		result.followers_count = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.FOLLOWERS_COUNT));
		result.favorites_count = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.FAVOURITES_COUNT));
		result.app_count = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.APP_COUNT));
		result.status = mCursor.getString(mCursor.getColumnIndex(UsersColumns.STATUS));
		return result;
	}
    
    /**
     * settings
     * @param name
     * @return
     */
    
	public String getSettingValue(String name) {
        String va = null;
        String where = SettingsCol.Name +"='"+name+"'";
        Cursor cursor = mContext.getContentResolver().query(SETTINGS_CONTENT_URI,settingsProject,where, null, null);
        if(cursor != null)
        {
        	if(cursor.getCount()>0 && cursor.moveToFirst()){
        		va = cursor.getString(cursor.getColumnIndex(SettingsCol.Value));
        	}
            cursor.close();
            cursor = null;
        }
        return va;
    }
    
    public boolean removeSetting(String name) {
        int ret = -1;
        try{
            ret = mContext.getContentResolver().delete(SETTINGS_CONTENT_URI, " name='"+name+"'", null);
        }catch(SQLiteException ne){}
        return ret > 0;
    } 
    
    public Uri addSetting(String name, String value) {
        Uri ret = null;
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put(SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        //if exist, update
        if(!StringUtil.isEmpty(getSettingValue(name)))
        {
            updateSetting(name, value);
        }
        else
        {        
            ret = mContext.getContentResolver().insert(SETTINGS_CONTENT_URI, ct); 
        }
        
        return ret;
    } 
    
    public boolean updateSetting(String name, String value) {
        boolean ret = false;
        String where = String.format(" name = \"%1$s\" ", name);
        android.content.ContentValues ct = new android.content.ContentValues();         
        ct.put(SettingsCol.Value, value);
        
        if(mContext.getContentResolver().update(SETTINGS_CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
    }
    
    public void setLastLoginUser(String email){
    	addSetting(LAST_LOGIN_USER, email);
    }
    
    public String getLastLoginUser(){    	
    	return getSettingValue(LAST_LOGIN_USER);    	
    }
    
    public void setUserInfoLastSyncTime(){
    	addSetting(LAST_SYNC_USERINFO_TIME,String.valueOf(System.currentTimeMillis()));
    }
    
    public long getUserInfoLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_USERINFO_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	return Long.valueOf(value);
    }
    
    //for address book sync settings
    public void setAddressBookSyncTime(){
    	addSetting(LAST_SYNC_ADDRESSBOOK_TIME,String.valueOf(System.currentTimeMillis()));
    }
    
    public long getAddressBookSyncTime(){
    	String value = getSettingValue(LAST_SYNC_ADDRESSBOOK_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}    	
    	return Long.valueOf(value);
    }
    
    public int getAddressBookInterval(){
    	int interval = 7;//days
    	String value = getSettingValue(LAST_SYNC_ADDRESSBOOK_INTERVAL);    	
    	try{
    		interval = Integer.valueOf(value);
    	}catch(Exception ne){}
    	
    	return interval;
    }    

}
