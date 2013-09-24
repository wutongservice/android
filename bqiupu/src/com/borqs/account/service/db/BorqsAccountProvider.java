package com.borqs.account.service.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;

public class BorqsAccountProvider extends ContentProvider{
	private static final String TAG = "BorqsAccountProvider";
	
	public static final String AUTHORITY = "com.borqs.account";
    private static final String DATABASE_NAME = "borqsaccount.db";
    private static final int DATABASE_VERSION = 2;
    public static SQLiteOpenHelper mOpenHelper;

    private static final String TABLE_ACCOUNT  = "account";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_USERS    = "users";

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, args.groupby, null, sortOrder);
       // result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) {
        	//getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
	}

	@Override
	public String getType(Uri uri) {
		 SqlArguments args = new SqlArguments(uri, null, null);
		 if (TextUtils.isEmpty(args.where)) {
			 return "vnd.android.cursor.dir/" + args.table;
		 } else {
			 return "vnd.android.cursor.item/" + args.table;
		 }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, values);
        if (rowId <= 0) return null;
        else{
        	//getContext().getContentResolver().notifyChange(uri, null);
        }
        uri = ContentUris.withAppendedId(uri, rowId);
        return uri;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper{
        
		public DatabaseHelper(Context context){
			super(context,DATABASE_NAME,null,DATABASE_VERSION);
		}
		
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
		    db.execSQL("CREATE TABLE " + TABLE_ACCOUNT + " (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
		    		"sessionid TEXT," +
                    "uid    INTEGER," +
                    "username  TEXT," +
                    "createtime INTEGER," +
                    "modifytime INTEGER," +
                    "nickname TEXT,"+
                    "screenname TEXT," +
                    "verify INTEGER" +
                    ");");
		    
		    
		    db.execSQL("CREATE TABLE " + TABLE_SETTINGS +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name  TEXT," +
                    "value TEXT" +
                    ");");
		    
		    db.execSQL("CREATE TABLE " + TABLE_USERS +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
                    "name               TEXT,"+
                    "relationship       INTEGER," +
                    "nickname           TEXT," +
                    "phone_number       TEXT," +
                    "date_of_birth      TEXT," +
                    "company            TEXT,"+
                    "province           INTEGER," +
                    "city               INTEGER," +
                    "created_at         INTEGER," +
                    "last_visit_time    INTEGER,"+
                    "verify_code        INTEGER," +
                    "verified           INTEGER,"+
                    "domain             TEXT," +
                    "profile_image_url  TEXT,"+
                    "profile_simage_url TEXT,"+
                    "profile_limage_url TEXT,"+
                    "location           TEXT,"+
                    "description        TEXT,"+
                    "url                TEXT,"+
                    "gender             INTEGER,"+
                    "friends_count      INTEGER,"+
                    "followers_count    INTEGER,"+
                    "favourites_count   INTEGER,"+
                    "app_count          INTEGER,"+
                    "status             TEXT"+
                    ");");
		    
		}

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        }
		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
            if (oldVersion < 2) {
                dropTables(db);
                onCreate(db);
                return;
            }
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                super.onDowngrade(db, oldVersion, newVersion);
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }
            /**
             * When users install qiupu from high version to low version, we need to support.
             *  comments:
             *    1.drop all tables and re-create database.
             *    2.save high version database's data.
             *    3.when do so, it will use the installing application's version to 
             *      decide how to create database and tables.
             */
        }

	}
	
	static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;
        public String groupby = null;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
            	if(url.getPathSegments().size() == 2 && url.getPathSegments().get(1).equals("apkgroup")){
            		this.table = url.getPathSegments().get(0);
            		this.where = where;
            		this.args = args;
            		return;
            	}
        		 this.table = url.getPathSegments().get(0);
                 this.where = "_id=" + ContentUris.parseId(url);                
                 this.args = null;
               
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }

}
