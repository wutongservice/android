package com.borqs.sync.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract.RawContacts;
import com.borqs.sync.service.LocalSyncMLProvider;
import com.borqs.syncml.ds.imp.tag.TagMapItem;
import com.borqs.syncml.ds.imp.tag.TagSource;
import com.borqs.syncml.ds.imp.tag.TagTarget;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SyncMLDb {
	final static public String TABLE_MAPPING = "mapping";
	final static public String TABLE_SETTINGS = "settings";
	final static public String TABLE_PROFILE = "profile_table";
	final static public String TABLE_SYNC_SOURCE = "sync_source_table";
	final static public String TABLE_SYNC_RECORD = "sync_record";
	final static public String TABLE_SYNC_LOG = "sync_log";

	public static final String AUTHORITY = "com.borqs.sync.syncML";
	
	static public class TContactChangeLog implements BaseColumns{
		private TContactChangeLog(){}
		
		//public static final String CONTENT_DIRECTORY = RawContacts.CONTENT_URI;
		public static final Uri CONTENT_URI =RawContacts.CONTENT_URI;
		public static final String ANCHOR = "version";
	}
	
	/**
	 * Columns from the Settings table that other columns join into themselves.
	 */
	public interface SettingsColumns {
		/**
		 * The key of this setting.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String KEY = "key";

		/**
		 * The value of this setting.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String VALUE = "value";
	}

	/**
	 * The settings over all of the people
	 */
	public static final class Settings implements BaseColumns, SettingsColumns {
		/**
		 * no public constructor since this is a utility class
		 */
		private Settings() {
		}

		/**
		 * The directory twig for this sub-table
		 */
		public static final String CONTENT_DIRECTORY = "settings";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "key ASC";

		public static String getSetting(ContentResolver cr, String account,
				String key) {
			// For now we only support a single account and the UI doesn't know
			// what
			// the account name is, so we're using a global setting for
			// SYNC_EVERYTHING.
			// Some day when we add multiple accounts to the UI this should
			// honor the account
			// that was asked for.
			String selectString;
			String[] selectArgs;
			selectString = "key=?";
			selectArgs = new String[] { key };
			Cursor cursor = LocalSyncMLProvider.query(Settings.CONTENT_URI,
                    new String[]{VALUE}, selectString, selectArgs, null);
			try {
				if (!cursor.moveToNext())
					return null;
				return cursor.getString(0);
			} finally {
				cursor.close();
                LocalSyncMLProvider.close();
			}
		}

		public static void setSetting(ContentResolver cr, String account,
				String key, String value) {
			ContentValues values = new ContentValues();
			// For now we only support a single account and the UI doesn't know
			// what
			// the account name is, so we're using a global setting for
			// SYNC_EVERYTHING.
			// Some day when we add multiple accounts to the UI this should
			// honor the account
			// that was asked for.
			// values.put(_SYNC_ACCOUNT, account);
			values.put(KEY, key);
			values.put(VALUE, value);
            try{
                LocalSyncMLProvider.update(Settings.CONTENT_URI, values, null, null);
            } finally {
                LocalSyncMLProvider.close();
            }
		}

		public static final String KEY_DEFAULT_PROFILE = "default_profile";
	}

	public interface MappingColumns {
		public static final String TAG_ID = "tagId";

		public static final String SRC_ID = "srcId";

		public static final String SOURCE = "source";
	}

	public static final class Mapping implements BaseColumns, MappingColumns {
		public static final String CONTENT_DIRECTORY = "mapping";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);

		public static int delMap(long source, ContentResolver resolver) {
            try{
			    return LocalSyncMLProvider.delete(CONTENT_URI, SOURCE + "=" + source, null);
            }finally {
                LocalSyncMLProvider.close();
            }
		}

		public static int insert(long source,
				Hashtable<String, String> mappings, ContentResolver resolver) {
			ContentValues values[] = new ContentValues[mappings.size()];
			int i;
			for (i = 0; i < values.length; i++) {
				values[i] = new ContentValues();
			}

			i = 0;
			Set<Map.Entry<String, String>> set = mappings.entrySet();
            try {
                for (Map.Entry<String, String> item : set) {
                    values[i].put(TAG_ID, item.getKey());
                    values[i].put(SRC_ID, item.getValue());
                    values[i].put(SOURCE, source);
                    i++;
                    LocalSyncMLProvider.insert(CONTENT_URI, values[i]);
                }
            }finally {
                LocalSyncMLProvider.close();
            }
			return i;//resolver.bulkInsert(CONTENT_URI, values);
		}

		public static Uri put(long source, String tgtId, String srcId,
				ContentResolver resolver) {
			ContentValues values = new ContentValues(3);
			values.put(TAG_ID, tgtId);
			values.put(SRC_ID, srcId);
			values.put(SOURCE, source);
            try{
			    return LocalSyncMLProvider.insert(CONTENT_URI, values);
            }finally {
                LocalSyncMLProvider.close();
            }
		}

		public static List<TagMapItem> get(long source, ContentResolver resolver) {
			List<TagMapItem> mappings = new LinkedList<TagMapItem>();

			String selection = SOURCE + "=" + source;
            try{
                Cursor cursor = LocalSyncMLProvider.query(CONTENT_URI, new String[] { TAG_ID,
                        SRC_ID }, selection, null, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        mappings.add(new TagMapItem(new TagTarget(cursor
                                .getString(1), null), new TagSource(cursor
                                .getString(0), null)));
                    }
                    cursor.close();
                }
            }finally {
                LocalSyncMLProvider.close();
            }

			return mappings;
		}
	}

	public interface ProfileTableColumns {
		public static final String PROFILE_NAME = "profile_name";
		public static final String ACCOUNT_USER = "account_user";
		public static final String ACCOUNT_PASSWD = "account_passwd";
		public static final String SERVER_URL = "server_url";
		public static final String APN = "apn";
		public static final String NAME_READ_ONLY = "name_read_only";
		public static final String CONTACT = "contact";
		public static final String EVENT = "event";
		public static final String TASK = "task";
		public static final String HIDE = "hide";
		public static final String RESERVE = "reserve";
	}

	static public class ProfileTable implements ProfileTableColumns,
			BaseColumns {
		public static final String CONTENT_DIRECTORY = "profileTable";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);
		public static final String DEFAULT_ORDER = PROFILE_NAME;
	}

	public interface SyncSourceTableColumns {
		public static final String ANCHOR_NEXT = "achor_next";

		public static final String ANCHOR_LAST = "achor_last";

		public static final String REMOTE_URI = "remote_uri";

		public static final String IS_ACTIVE = "is_active";

		public static final String NAME = "name";
		public static final String TYPE = "type";
		public static final String ENCODING = "encoding";
		// public static final String SYNC_MODE = "sync_mode";
		public static final String LOCAL_URI = "local_uri";
	}

	static public class SyncSourceTable implements SyncSourceTableColumns,
			BaseColumns {
		public static final String CONTENT_DIRECTORY = "sourceTable";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);
	}

	public interface SyncRecordColumns {
		/**
		 * The value record hash.
		 * <P>
		 * Type: LONG
		 * </P>
		 */
		public static final String HASH = "hash";
		/**
		 * The value source id.
		 * <P>
		 * Type: LONG
		 * </P>
		 */
		public static final String SOURCE = "source";
		/**
		 * The value record id in a source.
		 * <P>
		 * Type: LONG
		 * </P>
		 */
		public static final String RECORD = "record";
	}

	static public class SyncRecord implements SyncRecordColumns {
		public static final String CONTENT_DIRECTORY = "record";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);

		public static final String SORTING_ORDER = "record ASC";
	}

	public interface SyncLogColumns {
		public static final String SOURCE = "source";
		public static final String SYNC_MODE = "syncMode";
		public static final String RESULT = "result";
		public static final String START = "start";
		public static final String END = "end";
		public static final String CLIENT_ADD = "clientAdd";
		public static final String CLIENT_ADD_FAIL = "clientAddFail";
		public static final String CLIENT_MODIFY = "clientModify";
		public static final String CLIENT_MODIFY_FAIL = "clientModifyFail";
		public static final String CLIENT_DEL = "clientDel";
		public static final String CLIENT_DEL_FAIL = "clientDelFail";
		public static final String SERVER_ADD = "serverAdd";
		public static final String SERVER_ADD_FAIL = "serverAddFail";
		public static final String SERVER_MODIFY = "serverModify";
		public static final String SERVER_MODIFY_FAIL = "serverModifyFail";
		public static final String SERVER_DEL = "serverDel";
		public static final String SERVER_DEL_FAIL = "serverDelFail";
	}

	static public class SyncLog implements SyncLogColumns, BaseColumns {
		public static final String CONTENT_DIRECTORY = "syncLog";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + CONTENT_DIRECTORY);
	}
	static public class Functions {
	    public static final String MASTER_RESET_DIRECTORY = "function/master_reset";
        public static final String CLEAR_SYNC_INFO = "function/clear_sync_info";

        public static final Uri MASTER_RESET_URI = Uri.parse("content://"
                + AUTHORITY + "/" + MASTER_RESET_DIRECTORY);
        public static final Uri CLEAR_SYNC_INFO_URI = Uri.parse("content://"
                + AUTHORITY + "/" + CLEAR_SYNC_INFO);
	}
}
