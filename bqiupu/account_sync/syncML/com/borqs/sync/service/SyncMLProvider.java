package com.borqs.sync.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.borqs.common.util.BLog;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.common.ContactLock;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.ds.config.StoredataSetting;
import com.borqs.sync.provider.SyncMLDb;


public class SyncMLProvider extends ContentProvider {
	private static final String TAG = "SyncMLProvider";

    public static SyncMLProvider instance(Context context){
        SyncMLProvider provider = new SyncMLProvider();
        provider.mOpenHelper = SyncMLDatabaseHelper.getInstance(context);
        return provider;
    }


    public static void release(SyncMLProvider provider){
        if(provider != null){
            provider.mOpenHelper.close();
        }
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		int match = sURLMatcher.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (match) {
		case SYNCML_MAPPING:
			count = db.delete(SyncMLDb.TABLE_MAPPING, selection, selectionArgs);
			break;
		case SYNCML_MAPPING_ID:
			count = db.delete(SyncMLDb.TABLE_MAPPING, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_PROFILE_ID:
			count = db.delete(SyncMLDb.TABLE_PROFILE, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_SYNC_SOURCE_ID:
			count = db.delete(SyncMLDb.TABLE_SYNC_SOURCE, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_SYNC_RECORD:
			count = db.delete(SyncMLDb.TABLE_SYNC_RECORD, selection,
					selectionArgs);
			break;
		case SYNCML_SYNC_LOG:
			count = db
					.delete(SyncMLDb.TABLE_SYNC_LOG, selection, selectionArgs);
			break;
		case SYNCML_SYNC_LOG_ID:
			count = db.delete(SyncMLDb.TABLE_SYNC_LOG, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_CLEAR_SYNC_INFO:
		    BLog.d("=====on account login ,clear the syncifo");
		    clearSyncInfo();
		    break;
		default:
			throw new UnsupportedOperationException();
		}
		return count;
	}
	
    private void clearSyncInfo() {
        Context context = getContext();
        if(context == null){
            context = ApplicationGlobals.getContext();
        }
        // reset the status of syncing ,importing and merging
        ContactLock.unLockSyncRelated();
        // clear the sync info
        clearSyncRecordInfo(context);
    }

    private void clearSyncRecordInfo(Context context) {
//        ContentResolver resolver = context.getContentResolver();
        // record clear
        delete(SyncMLDb.SyncRecord.CONTENT_URI, null, null);
        // map clear
        delete(SyncMLDb.Mapping.CONTENT_URI, null, null);
        // sync anchor clear
        StoredataSetting.clearSyncAnchor();
        // the sync time caused by network change
        SyncHelper.setSyncTimeCausedByNetworkChange(context, 0);
    }

	@Override
	public String getType(Uri uri) {
		int match = sURLMatcher.match(uri);
		switch (match) {
		case SYNCML_PROFILE:
			return "vnd.android.cursor.dir/com.borqs.sync.syncML/profile";
		case SYNCML_PROFILE_ID:
			return "vnd.android.cursor.item/com.borqs.sync.syncML/profile";
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowID;
		int match = sURLMatcher.match(uri);
		switch (match) {
		case SYNCML_MAPPING:
			rowID = db.insert(SyncMLDb.TABLE_MAPPING, SyncMLDb.Mapping.SRC_ID,
					values);
			if (rowID > 0) {
				return Uri.parse(SyncMLDb.Mapping.CONTENT_URI + "/" + rowID);
			} else {
				return null;
			}
		case SYNCML_PROFILE:
			rowID = db.insert(SyncMLDb.TABLE_PROFILE,
					SyncMLDb.ProfileTable.PROFILE_NAME, values);
			if (rowID > 0) {
				return Uri.parse(SyncMLDb.ProfileTable.CONTENT_URI + "/"
						+ rowID);
			} else {
				return null;
			}
		case SYNCML_SYNC_SOURCE:
			rowID = db.insert(SyncMLDb.TABLE_SYNC_SOURCE,
					SyncMLDb.SyncSourceTable.REMOTE_URI, values);
			if (rowID > 0) {
				return Uri.parse(SyncMLDb.SyncSourceTable.CONTENT_URI + "/"
						+ rowID);
			} else {
				return null;
			}
		case SYNCML_SYNC_RECORD:
			rowID = db.insert(SyncMLDb.TABLE_SYNC_RECORD,
					SyncMLDb.SyncRecord.HASH, values);
			if (rowID > 0) {
				return Uri.parse(SyncMLDb.SyncRecord.CONTENT_URI + "/" + rowID);
			} else {
				return null;
			}
		case SYNCML_SYNC_LOG:
			rowID = db.insert(SyncMLDb.TABLE_SYNC_LOG, SyncMLDb.SyncLog.SOURCE,
					values);
			if (rowID > 0) {
				return Uri.parse(SyncMLDb.SyncLog.CONTENT_URI + "/" + rowID);
			} else {
				return null;
			}
//		case SYNCML_SETTING:
		default:
			throw new UnsupportedOperationException();

		}
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = SyncMLDatabaseHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Generate the body of the query.
		int match = sURLMatcher.match(uri);
		switch (match) {
		case SYNCML_MAPPING:
			qb.setTables(SyncMLDb.TABLE_MAPPING);
			break;
		case SYNCML_MAPPING_ID:
			qb.setTables(SyncMLDb.TABLE_MAPPING);
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
			break;
		case SYNCML_PROFILE:
			qb.setTables(SyncMLDb.TABLE_PROFILE);
			break;
		case SYNCML_PROFILE_ID:
			qb.setTables(SyncMLDb.TABLE_PROFILE);
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
			break;
		case SYNCML_SYNC_SOURCE:
			qb.setTables(SyncMLDb.TABLE_SYNC_SOURCE);
			break;
		case SYNCML_SYNC_SOURCE_ID:
			qb.setTables(SyncMLDb.TABLE_SYNC_SOURCE);
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
			break;
		case SYNCML_SYNC_RECORD:
			qb.setTables(SyncMLDb.TABLE_SYNC_RECORD);
			break;
		case SYNCML_SYNC_LOG_ID:
			qb.setTables(SyncMLDb.TABLE_SYNC_LOG);
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
			break;
		case SYNCML_SYNC_LOG:
			qb.setTables(SyncMLDb.TABLE_SYNC_LOG);
			break;
		case SYNCML_SETTING:
			qb.setTables(SyncMLDb.TABLE_SETTINGS);
			break;
//		case SYNCML_SETTING_ID:
//			qb.setTables(SyncMLDb.TABLE_SETTINGS);
//			qb.appendWhere("_id = " + uri.getPathSegments().get(1) + "");
//			break;
		default:
			throw new UnsupportedOperationException();
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, projection, selection, selectionArgs, null, null,
				sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (sURLMatcher.match(uri)) {
		case SYNCML_SETTING:
			if (selectionArgs != null) {
				throw new IllegalArgumentException(
						"you aren't allowed to specify where args when updating settings");
			}
			if (selection != null) {
				throw new IllegalArgumentException(
						"you aren't allowed to specify a where string when updating settings");
			}
			return updateSettings(values);
		case SYNCML_MAPPING:
			count = db.update(SyncMLDb.TABLE_MAPPING, values, selection,
					selectionArgs);
			break;
		case SYNCML_MAPPING_ID:
			count = db.update(SyncMLDb.TABLE_MAPPING, values, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_PROFILE_ID:
			count = db.update(SyncMLDb.TABLE_PROFILE, values, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_SYNC_SOURCE_ID:
			count = db.update(SyncMLDb.TABLE_SYNC_SOURCE, values, "_id="
					+ uri.getPathSegments().get(1), null);
			break;
		case SYNCML_SYNC_RECORD:
			count = db.update(SyncMLDb.TABLE_SYNC_RECORD, values, selection,
					selectionArgs);
			break;
		// master reset
		case SYNCML_MASTER_RESET:
			Logger.logW(TAG, "Reset database version");	
			
			db.setVersion(SyncMLDatabaseHelper.DATABASE_VERSION_RESET);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return count;
	}

	private int updateSettings(ContentValues values) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final String key = values.getAsString(SyncMLDb.Settings.KEY);
		if (key == null) {
			throw new IllegalArgumentException(
					"you must specify the key when updating settings");
		}
		db.delete(SyncMLDb.TABLE_SETTINGS, "key=?", new String[] { key });

		long rowId = db.insert(SyncMLDb.TABLE_SETTINGS, SyncMLDb.Settings.KEY,
				values);
		if (rowId < 0) {
			throw new SQLException("error updating settings with " + values);
		}
		return 1;
	}
	private SQLiteOpenHelper mOpenHelper;

	private static final int SYNCML_MAPPING = 0;
	private static final int SYNCML_MAPPING_ID = 1;
	private static final int SYNCML_SETTING = 2;
	private static final int SYNCML_SETTING_ID = 3;
	private static final int SYNCML_PROFILE = 4;
	private static final int SYNCML_PROFILE_ID = 5;
	private static final int SYNCML_SYNC_SOURCE = 6;
	private static final int SYNCML_SYNC_SOURCE_ID = 7;
	private static final int SYNCML_SYNC_RECORD = 8;
	private static final int SYNCML_SYNC_RECORD_ID = 9;
	private static final int SYNCML_SYNC_LOG = 10;
	private static final int SYNCML_SYNC_LOG_ID = 11;
	// master reset const 
	private static final int SYNCML_MASTER_RESET = 12;
	//clear sync info
	private static final int SYNCML_CLEAR_SYNC_INFO = 13;

	private static final UriMatcher sURLMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.Mapping.CONTENT_DIRECTORY, SYNCML_MAPPING);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.Mapping.CONTENT_DIRECTORY + "/#", SYNCML_MAPPING_ID);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.Settings.CONTENT_DIRECTORY, SYNCML_SETTING);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.Settings.CONTENT_DIRECTORY + "/#", SYNCML_SETTING_ID);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.ProfileTable.CONTENT_DIRECTORY, SYNCML_PROFILE);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.ProfileTable.CONTENT_DIRECTORY + "/#",
				SYNCML_PROFILE_ID);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncSourceTable.CONTENT_DIRECTORY, SYNCML_SYNC_SOURCE);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncSourceTable.CONTENT_DIRECTORY + "/#",
				SYNCML_SYNC_SOURCE_ID);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncRecord.CONTENT_DIRECTORY, SYNCML_SYNC_RECORD);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncRecord.CONTENT_DIRECTORY + "/#",
				SYNCML_SYNC_RECORD_ID);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncLog.CONTENT_DIRECTORY, SYNCML_SYNC_LOG);
		sURLMatcher.addURI(SyncMLDb.AUTHORITY,
				SyncMLDb.SyncLog.CONTENT_DIRECTORY + "/#", SYNCML_SYNC_LOG_ID);
		// master reset
		sURLMatcher
				.addURI(SyncMLDb.AUTHORITY,
						SyncMLDb.Functions.MASTER_RESET_DIRECTORY,
						SYNCML_MASTER_RESET);
        //clear sync info
		sURLMatcher
        .addURI(SyncMLDb.AUTHORITY,
                SyncMLDb.Functions.CLEAR_SYNC_INFO,
                SYNCML_CLEAR_SYNC_INFO);
	}
}
