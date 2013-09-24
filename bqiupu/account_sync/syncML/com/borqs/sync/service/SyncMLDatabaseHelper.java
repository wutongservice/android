package com.borqs.sync.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.provider.SyncMLDb;
import com.borqs.sync.provider.SyncMLDb.Mapping;
import com.borqs.sync.provider.SyncMLDb.ProfileTable;
import com.borqs.sync.provider.SyncMLDb.Settings;
import com.borqs.sync.provider.SyncMLDb.SyncLog;
import com.borqs.sync.provider.SyncMLDb.SyncRecord;
import com.borqs.sync.provider.SyncMLDb.SyncSourceTable;
import com.borqs.syncml.ds.imp.common.CryptUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;


public class SyncMLDatabaseHelper extends SQLiteOpenHelper {
	private static SyncMLDatabaseHelper mInstance = null;

	static final String DATABASE_NAME = "syncml.db";
	static final int DATABASE_VERSION = 23;
	// master reset version 
	static final int DATABASE_VERSION_RESET = 1;
	
	public static final int STORED_LOG_NUMBER = 1;
	
	private Context mContext;

	private SyncMLDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	static SyncMLDatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SyncMLDatabaseHelper(context);
		}
		return mInstance;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTabels(db);
	}
	
	private void createTabels(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_SETTINGS + " (" +
        		Settings._ID + " INTEGER PRIMARY KEY," +
        		Settings.KEY + " TEXT," + 
        		Settings.VALUE + " TEXT" + ")");
        
        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_PROFILE + " (" +
        		ProfileTable._ID + " INTEGER PRIMARY KEY," +
        		ProfileTable.PROFILE_NAME + " TEXT COLLATE LOCALIZED," +
        		ProfileTable.ACCOUNT_USER + " TEXT," +
        		ProfileTable.ACCOUNT_PASSWD + " TEXT," +
        		ProfileTable.SERVER_URL + " TEXT," +
        		ProfileTable.APN + " TEXT," +
        		ProfileTable.NAME_READ_ONLY + " INTEGER," +
        		ProfileTable.RESERVE + " INTEGER," +
        		ProfileTable.CONTACT + " INTEGER," +
        		ProfileTable.EVENT + " INTEGER," +
        		ProfileTable.TASK + " INTEGER," +
        		ProfileTable.HIDE + " INTEGER" + ")");
        
        db.execSQL("CREATE INDEX profileNameIndex ON " + SyncMLDb.TABLE_PROFILE
				+ " (" + ProfileTable.PROFILE_NAME + ");");
        
		db.execSQL("CREATE TRIGGER profileDelete AFTER DELETE ON " + SyncMLDb.TABLE_PROFILE +
				" BEGIN " +
				"DELETE FROM " + SyncMLDb.TABLE_SYNC_SOURCE + " WHERE (" +
						"_id = old." + ProfileTable.CONTACT + " OR " +
						"_id = old." + ProfileTable.EVENT + " OR " +
						"_id = old." + ProfileTable.TASK +
						");" +
				" UPDATE " + SyncMLDb.TABLE_SETTINGS + " SET " + Settings.VALUE + 
				"=(SELECT profile_table._id FROM profile_table ORDER BY profile_table._id LIMIT 1) " +
				" WHERE(" + Settings.KEY + "='" + Settings.KEY_DEFAULT_PROFILE + "' AND " +
				Settings.VALUE + "=" + "old._id)" +";" +
				"END");

        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_SYNC_SOURCE + " (" +
        		SyncSourceTable._ID + " INTEGER PRIMARY KEY," +
        		SyncSourceTable.IS_ACTIVE + " INTEGER," +
        		SyncSourceTable.ANCHOR_NEXT + " INTEGER," + 
        		SyncSourceTable.ANCHOR_LAST + " INTEGER," + 
        		SyncSourceTable.NAME + " TEXT," +
        		SyncSourceTable.TYPE + " TEXT," +
        		SyncSourceTable.ENCODING + " TEXT," +
        		SyncSourceTable.LOCAL_URI + " TEXT," +
//        		SyncSourceTable.SYNC_MODE + " INTEGER," +
        		SyncSourceTable.REMOTE_URI + " TEXT" + ")");

		db.execSQL("CREATE TRIGGER syncSourceDelete AFTER DELETE ON " + SyncMLDb.TABLE_SYNC_SOURCE +
				" BEGIN " +
				"DELETE FROM " + SyncMLDb.TABLE_MAPPING + " WHERE " +
					Mapping.SOURCE +  "= old._id;" +
				"DELETE FROM " + SyncMLDb.TABLE_SYNC_RECORD + " WHERE " +
					Mapping.SOURCE +  "= old._id;" +
				"DELETE FROM " + SyncMLDb.TABLE_SYNC_LOG + " WHERE " +
					SyncLog.SOURCE +  "= old._id;" +
				"END");

        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_MAPPING + " (" +
        		Mapping._ID + " INTEGER PRIMARY KEY," +
        		Mapping.SOURCE + " INTEGER," +
        		Mapping.TAG_ID + " TEXT," + 
        		Mapping.SRC_ID + " TEXT" + ")");

        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_SYNC_RECORD + " (" +
        		SyncRecord.SOURCE + " INTEGER," +
        		SyncRecord.RECORD + " INTEGER," +
        		SyncRecord.HASH + " INTEGER" + ")");
        
        db.execSQL("CREATE INDEX syncrecordSourceIndex ON " + SyncMLDb.TABLE_SYNC_RECORD
				+ " (" + SyncRecord.SOURCE + ");");
        db.execSQL("CREATE INDEX syncrecordRecordIndex ON " + SyncMLDb.TABLE_SYNC_RECORD
				+ " (" + SyncRecord.RECORD + ");");

        db.execSQL("CREATE TABLE " + SyncMLDb.TABLE_SYNC_LOG + " (" +
        		SyncLog._ID + " INTEGER PRIMARY KEY," +
        		SyncLog.SOURCE + " INTEGER," +
        		SyncLog.SYNC_MODE + " INTEGER," +
        		SyncLog.RESULT + " INTEGER," +
        		SyncLog.START + " INTEGER," +
        		SyncLog.END + " INTEGER," +
        		SyncLog.CLIENT_ADD + " INTEGER," +
        		SyncLog.CLIENT_ADD_FAIL + " INTEGER," +
        		SyncLog.CLIENT_MODIFY + " INTEGER," +
        		SyncLog.CLIENT_MODIFY_FAIL + " INTEGER," +
        		SyncLog.CLIENT_DEL + " INTEGER," +
        		SyncLog.CLIENT_DEL_FAIL + " INTEGER," +
        		SyncLog.SERVER_ADD + " INTEGER," +
        		SyncLog.SERVER_ADD_FAIL + " INTEGER," +
        		SyncLog.SERVER_MODIFY + " INTEGER," +
        		SyncLog.SERVER_MODIFY_FAIL + " INTEGER," +
        		SyncLog.SERVER_DEL + " INTEGER," +
        		SyncLog.SERVER_DEL_FAIL + " INTEGER" + ")");
        
//		db.execSQL("CREATE TRIGGER logAdded AFTER INSERT ON " + SyncMLDb.TABLE_SYNC_LOG +
//				" BEGIN " +
//				"DELETE FROM " + SyncMLDb.TABLE_SYNC_LOG + " WHERE (" +
//					SyncLog.SOURCE + " = new." + SyncLog.SOURCE + " AND " +
//						"_id NOT IN (SELECT _id FROM " + SyncMLDb.TABLE_SYNC_LOG + " WHERE " + 
//						SyncLog.SOURCE + " = new." + SyncLog.SOURCE + " ORDER BY _id DESC " +
//						"LIMIT " + SyncMLDatabaseHelper.STORED_LOG_NUMBER + ")); "+
//				"END");
        
        handlePreloadProfile(db, false);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//If master reset,read profile file,update profile content
		//else defult upgrade
		if(oldVersion == DATABASE_VERSION_RESET){
			handlePreloadProfile(db, true);
		}else{
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_SETTINGS);
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_MAPPING);
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_PROFILE);
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_SYNC_SOURCE);
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_SYNC_RECORD);
			db.execSQL("DROP TABLE IF EXISTS " + SyncMLDb.TABLE_SYNC_LOG);
			createTabels(db);
		}


	}

	private void handlePreloadProfile(SQLiteDatabase db, boolean bMasterReset) {
		Logger.logD("SyncML","Preload profile");
		
		InputStream inStream = mContext.getResources().openRawResource(
		        getConfigResId(mContext));
//		FileInputStream inStream;
//		try {
//			inStream = new FileInputStream(Constant.PRELOAD_PROFILE_FILE_NAME);
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//			return;
//		}
//		InputStream inStream = mContext.getResources().openRawResource(
//				R.raw.preload_profiles);
		
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(inStream, null);
			boolean leave = false;
			
			do {
				int eventType = parser.next();
				switch (eventType) {
				case XmlPullParser.START_TAG: {
					String name = parser.getName();
					if ("profiles".equals(name)) {
					} else if ("profile".equals(name)) {
						boolean def = "true".equals(parser.getAttributeValue(null, "default"));
						String user = parser.getAttributeValue(null, "user");
						String passwd = parser.getAttributeValue(null, "password");
						boolean hide = "true".equals(parser.getAttributeValue(null, "hide"));
						boolean reserve = "true".equals(parser.getAttributeValue(null, "reserve"));
						parser.nextTag();
						long id = Long.parseLong(parser.nextText());
						parser.nextTag();
						boolean nameReadonly = "true".equals(parser.getAttributeValue(null, "read_only"));
						String profileName = parser.nextText().trim();
						parser.nextTag();
						String apnId = parser.nextText();
						parser.nextTag();
						String serverUrl = parser.nextText().trim();
						
						parser.nextTag();
						long contact = saveSource(parser, db, bMasterReset);
						parser.nextTag();
						parser.nextTag();
						long event = saveSource(parser, db, bMasterReset);
						parser.nextTag();
						parser.nextTag();
						long task = saveSource(parser, db, bMasterReset);
						
						ContentValues values = new ContentValues();
						values.put(ProfileTable.PROFILE_NAME, profileName);
						values.put(ProfileTable.ACCOUNT_USER, user);
						values.put(ProfileTable.ACCOUNT_PASSWD, new CryptUtil().encrypt(passwd));
						values.put(ProfileTable.SERVER_URL, "");
						values.put(ProfileTable.APN, apnId);
						values.put(ProfileTable.HIDE, hide ? 1 : 0);
						values.put(ProfileTable.RESERVE, reserve ? 1 : 0);
						values.put(ProfileTable._ID, id);
						values.put(ProfileTable.NAME_READ_ONLY, nameReadonly ? 1 : 0);
						if(!bMasterReset){
							values.put(ProfileTable.CONTACT, contact);
							values.put(ProfileTable.EVENT, event);
							values.put(ProfileTable.TASK, task);
						}
						if(bMasterReset){
							db.update(SyncMLDb.TABLE_PROFILE, values, "_id=" + id, null);
						}else{
							db.insert(SyncMLDb.TABLE_PROFILE, ProfileTable.PROFILE_NAME, values);
						}

						if(def){
							ContentValues set = new ContentValues();
							set.put(Settings.KEY, Settings.KEY_DEFAULT_PROFILE);
							set.put(Settings.VALUE, id);
							if(bMasterReset){
								db.update(SyncMLDb.TABLE_SETTINGS, set, "_id=" + id, null);
							}else{
								db.insert(SyncMLDb.TABLE_SETTINGS, Settings.VALUE, set);
							}							
						}
						parser.nextTag();
						parser.nextTag();
					}
					break;
				}
				case XmlPullParser.END_DOCUMENT:
				case XmlPullParser.END_TAG:
					leave = true;
					break;
				}
			} while (!leave);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private long saveSource(XmlPullParser parser,
							SQLiteDatabase db,
							boolean bMasterReset) throws XmlPullParserException, IOException {
		boolean active = "true".equals(parser.getAttributeValue(null, "active"));
		parser.nextTag();
		String name = parser.nextText().trim();
		parser.nextTag();
		String remoteUri = parser.nextText().trim();
		parser.nextTag();
		String localUri = parser.nextText().trim();
		parser.nextTag();
		String type = parser.nextText().trim();
		
		ContentValues values = new ContentValues();		
		values.put(SyncSourceTable.REMOTE_URI, remoteUri);
		values.put(SyncSourceTable.IS_ACTIVE, active ? 1 : 0);

		values.put(SyncSourceTable.NAME, name);
//		values.put(SyncSourceTable.SYNC_MODE, AlertCode.ALERT_CODE_FAST);
//		values.put(SyncSourceTable.ENCODING, ISyncSource.ENCODING_NONE);
		values.put(SyncSourceTable.LOCAL_URI, localUri);
		values.put(SyncSourceTable.TYPE, type);
		long result;
		if(bMasterReset){
			result = db.update(SyncMLDb.TABLE_SYNC_SOURCE, values, "name='" + name + "'", null);
		} else{
			result = db.insert(SyncMLDb.TABLE_SYNC_SOURCE, SyncSourceTable.NAME, values);	
		}
		return result;
	}
	
    private static int getConfigResId(Context context){
        return R.raw.syncml_ds_preload_profiles;
    }
}
