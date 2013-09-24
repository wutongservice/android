package twitter4j;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class DBHelper {
	private static DBHelper _instance;
	private Context mContext;
	private static String AUTHORITY = "com.android.borqsaccount";
	
	private DBHelper(Context context)
	{
		mContext = context;
	}
	
	public static DBHelper getInstance(Context context)
	{
		if(_instance == null)
		{
			_instance = new DBHelper(context);
		}
		
		return _instance;
	}
	
	final  Uri ACCOUNT_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/account");
	static final String[] ACCOUNT_PROJECTION = {
		"uid",
		"email",
		"password",
		"nickname",
		"session_id"
	};
	
	static final class AccountColumns
	{
		static final String ID = "_id";
		static final String UID = "uid";
		static final String EMAIL = "email";
		static final String PWD = "password";
		static final String NICKNAME = "nickname";
		static final String SESSION_ID = "session_id";
	}
	
	public Account getAccount()
	{
		Account result = null;
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
		String[] tpProjection = {AccountColumns.ID};
		Cursor mCursor = mContext.getContentResolver().query(ACCOUNT_CONTENT_URI, tpProjection, null, null, null);
		if(mCursor != null )
		{
			if(mCursor.moveToFirst())
			{
				isExistID = mCursor.getLong(mCursor.getColumnIndex(AccountColumns.ID));
			}
			mCursor.close();
			mCursor = null;
		}
		
		return isExistID;
	}
	
	public boolean updateAccount(Account mAccount)
	{
	    ContentResolver cr = mContext.getContentResolver();
	    ContentValues values = formatAccountContentValues(mAccount);
	    long existID = isExistAccount();
	    if(existID >= 0)
	    {
	    	String where = AccountColumns.ID + "="+existID;
	    	cr.update(ACCOUNT_CONTENT_URI, values, where, null);
	    }
	    else
	    {
	    	cr.insert(ACCOUNT_CONTENT_URI, values);
	    }
		
	    return true;
	}
	
	private ContentValues formatAccountContentValues(Account mAccount) {
		ContentValues cv = new ContentValues();
		cv.put(AccountColumns.EMAIL, mAccount.email);
		cv.put(AccountColumns.PWD, mAccount.pwd);
		cv.put(AccountColumns.NICKNAME, mAccount.nickname);
		cv.put(AccountColumns.UID, mAccount.uid);
		cv.put(AccountColumns.SESSION_ID, mAccount.session_id);
		return cv;
	}

	private Account formatAccount(Cursor mCursor) {
		Account result = new Account();
		result.uid = mCursor.getString(mCursor.getColumnIndex(AccountColumns.UID));
		result.email = mCursor.getString(mCursor.getColumnIndex(AccountColumns.EMAIL));
		result.pwd = mCursor.getString(mCursor.getColumnIndex(AccountColumns.PWD));
		result.nickname = mCursor.getString(mCursor.getColumnIndex(AccountColumns.NICKNAME));
		result.session_id = mCursor.getString(mCursor.getColumnIndex(AccountColumns.SESSION_ID));
		return result;
	}

	public static class Account
	{
		public String uid;
		public String email;
		public String nickname;
		public String pwd;
		public String session_id;
	}

    public void logout() {
       deleteAccount();
    }

    private void deleteAccount() {        
        mContext.getContentResolver().delete(ACCOUNT_CONTENT_URI, null,null);       
    }
	
	
}
