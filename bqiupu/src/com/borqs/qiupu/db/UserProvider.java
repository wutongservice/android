package com.borqs.qiupu.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import android.util.Log;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM.CompanyColumns;


public class UserProvider extends ContentProvider{
	private static final String TAG = "UserProvider";

    private static final boolean DEBUG = false;

    public static final String DATABASE_NAME = "user.db";
    public static final int DATABASE_VERSION = 67;
    public static SQLiteOpenHelper mOpenHelper;

    public static final String AUTHORITY = "com.borqs.users";
    
    private static final String TABLE_USERS    = "users";
    private static final String TABLE_CIRCLE    = "circles";
    private static final String TABLE_CIRCLE_USERS = "circle_users";
    private static final String TABLE_USER_CIRCLES = "user_circles";
    private static final String TABLE_WORK_EXPERIENCE = "work_experience";
    private static final String TABLE_EDUCATION = "education";
    private static final String TABLE_SHARERESOURCE = "share_resource";
    private static final String TABLE_PHONE_EMAIL = "phone_email";
    private static final String TABLE_USER_IMAGE = "user_image";
    private static final String TABLE_SHARED_PHOTOS = "shared_photos";
    private static final String TABLE_PERHAPS_NAME = "perhaps_name";
    private static final String TABLE_QIUPU_ALBUM = "qiupu_album";
    private static final String TABLE_QIUPU_PHOTO = "qiupu_photo";
    private static final String TABLE_GROUP = "groups";
    private static final String TABLE_CHAT_RECORD = "chat_record";
    private static final String TABLE_THEME = "themes";
    private static final String TABLE_POLL = "poll";
    private static final String TABLE_CIRCLE_POLL = "circle_poll";
    private static final String TABLE_EVNETS_CALENDAR_MAPPING = "events_calendar";
    private static final String TABLE_COMPANY = "company";
    private static final String TABLE_PAGE = "pages";
    private static final String CIRCLE_EVENT = "circle_event";
    protected static final String CIRCLE_CIRCLES = "circle_circles";

    protected static final String TABLE_EMPLOYEE    = "employee";
    protected static final String TABLE_CATEGORY    = "category";
    
    public static final int TYPE_PHONE = 1;
    public static final int TYPE_EMAIL = 2;
    
    public static final int TYPE_FRIENDS = 1;
    public static final int TYPE_FANS = 2;
    
    public static final int IMAGE_TYPE_IN_MEMBER = 3;
    public static final int IMAGE_TYPE_APPLY_MEMBER = 4;
    public static final int IMAGE_TYPE_INVITE_MEMBER = 5;
    
    public static final int IMAGE_TYPE_COMPANY_MEMBER = 6;
    public static final int IMAGE_TYPE_DEPARTMENT_MEMBER = 7;
    
    public static final int IMAGE_TYPE_FORMAL_CIRCLE = 8;
    public static final int IMAGE_TYPE_PAEG_FANS = 9;

    public static final int IMAGE_TYPE_FREE_CIRCLE = 10;
    public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_USERS);

    public static void ensureDatabase(Context context) {
        if (null == mOpenHelper) {
            mOpenHelper = new DatabaseHelper(context);
        }
    }

	@Override
	public boolean onCreate() {
        ensureDatabase(getContext());
		return true;
	}
 
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        if (DEBUG) Log.d(TAG, "query, uri = " + uri + ", projection = " +
                (null == projection ? "" : TextUtils.join(",", projection)) +
                ", selection = " + selection + ", selection args = " +
                (null == selectionArgs ? "" : TextUtils.join(",", selectionArgs)) +
                ", sort order = " + sortOrder);
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor =  qb.query(db, projection, args.where, args.args, args.groupby, null, sortOrder);
        if (DEBUG) {
            if (cursor == null ) {
                Log.d(TAG, "query result is null");
            } else {
                Log.d(TAG, "query result size: " + cursor.getCount());
            }
        }
        return cursor;
    }

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(uri, null);
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
        	getContext().getContentResolver().notifyChange(uri, null);
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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE);
		    db.execSQL("CREATE TABLE " + TABLE_CIRCLE +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "circleid    INTEGER," +
                    "uid         INTEGER," +                    
                    "name        TEXT," +
                    "memberCount INTEGER," +
                    "showOnStream INTEGER," +
                    "type               INTEGER DEFAULT 0," +
                    "referred_count     INTEGER DEFAULT 0" +

                    ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		    db.execSQL("CREATE TABLE " + TABLE_USERS +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
                    "name               TEXT,"+
                    "nickname           TEXT," +
                    "name_pinyin        TEXT," +//for sort
                    "date_of_birth      TEXT," +
                    "about_me           TEXT,"+
                    "company            TEXT,"+
                    "office_address     TEXT,"+
                    "department         TEXT,"+
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
                    "gender             TEXT,"+
                    "friends_count      INTEGER,"+
                    "followers_count    INTEGER,"+
                    "favourites_count   INTEGER,"+
                    "app_count          INTEGER,"+
                    "status             TEXT,"+
                    "status_time        INTEGER,"+  
                    "circleid           TEXT,"+
                    "circlename         TEXT,"+
                    "bidi               INTEGER DEFAULT 0," +
                    "shortcut           INTEGER DEFAULT 0," +
                    "requested_id       TEXT," +
                    "his_friend         INTEGER DEFAULT 0," +
                    "profile_privacy    INTEGER DEFAULT 0," +
                    "work_history       TEXT," +
                    "job_title          TEXT,"+
                    "remark             TEXT,"+
                    "referred_count     INTEGER DEFAULT 0," +
                    "db_status          INTEGER DEFAULT 0" +
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE_USERS);
		    db.execSQL("CREATE TABLE " + TABLE_CIRCLE_USERS +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +		    		
                    "circleid    INTEGER," +
                    "uid         INTEGER" +
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CIRCLES);
		    db.execSQL("CREATE TABLE " + TABLE_USER_CIRCLES +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +		    		
                    "circleid    INTEGER," +
                    "uid         INTEGER" +
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORK_EXPERIENCE);
		    db.execSQL("CREATE TABLE " + TABLE_WORK_EXPERIENCE +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
					"work_from               TEXT," +
					"work_to                 TEXT," +
                    "company            TEXT,"+
                    "office_address     TEXT,"+
                    "department         TEXT,"+
                    "job_title          TEXT,"+
                    "job_description    TEXT"+
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDUCATION);
		    db.execSQL("CREATE TABLE " + TABLE_EDUCATION +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
					"edu_from               TEXT," +
					"edu_to                 TEXT," +
                    "school            TEXT,"+
                    "type     TEXT,"+
                    "class         TEXT,"+
                    "school_location          TEXT,"+
                    "degree          TEXT,"+
                    "major    TEXT"+
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARERESOURCE);
		    db.execSQL("CREATE TABLE " + TABLE_SHARERESOURCE +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
					"shared_text_count  INTEGER," +
					"shared_app_count   INTEGER," +
                    "shared_photo_count INTEGER,"+
                    "shared_link_count  INTEGER,"+
                    "shared_book_count  INTEGER,"+
                    "shared_static_file  INTEGER,"+
                    "shared_audio  INTEGER,"+
                    "shared_video  INTEGER"+
  		             ");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONE_EMAIL);
		    db.execSQL("CREATE TABLE " + TABLE_PHONE_EMAIL +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
		    		"uid                INTEGER," +
		    		"type                TEXT," +
		    		"info                TEXT," +
		    		"isbind              INTEGER," +
		    		"phone_or_email      INTEGER"+
		    		");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_IMAGE);
		    db.execSQL("CREATE TABLE " + TABLE_USER_IMAGE +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
		    		"uid                INTEGER," +
		    		"belong_uid                INTEGER," +
		    		"type                INTEGER," +
		    		"image_url            TEXT," +
		    		"display_name         TEXT" +
		    		");");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_PHOTOS);
		    db.execSQL("CREATE TABLE " + TABLE_SHARED_PHOTOS +" (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
		    		"uid                INTEGER," +
		    		"post_id                INTEGER," +
		    		"photo_img_middle            TEXT" +
		    		");");
		    createTableQiupuAlbum(db);
            createPossibleNameTable(db);
            createGroupTable(db);
            createChatRecordTable(db);

            createTableQiupuPhoto(db);
            insertNativeCircles(db);
            createEventThemeTable(db);
            createTablePoll(db);
            createTableEventsCalendar(db);
            createTableCompany(db);
            createPageTable(db);
            createCircleEventTable(db);
            createDirectoryMemberTable(db);
            createTableCirclePoll(db);
            createCategoryTable(db);
            
            createCircleCirclesTable(db);
        }
		
		private void createTableQiupuAlbum(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QIUPU_ALBUM);
			db.execSQL("CREATE TABLE " + TABLE_QIUPU_ALBUM +" (" +
					"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"album_id               	INTEGER," +
					"uid                	 	INTEGER," +
					"album_type              	INTEGER," +
					"title            		 	TEXT,"    +
					"summary            	 	TEXT,"    +
					"privacy                 	INTEGER," +
					"created_time            	INTEGER," +
					"updated_time            	INTEGER," +
					"hava_expired            	INTEGER," +
					"photo_count               	INTEGER," +
					"album_cover_photo_middle   TEXT"     +
					");");
		}
		private void createTableQiupuPhoto(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QIUPU_PHOTO);
			db.execSQL("CREATE TABLE " + TABLE_QIUPU_PHOTO +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "photo_id                	INTEGER," +
                    "album_id               	INTEGER," +
                    "uid                	 	INTEGER," +
                    "photo_url_original         TEXT,"    +
                    "photo_url_small         	TEXT,"    +
                    "photo_url_middle         	TEXT,"    +
                    "photo_url_big         	    TEXT,"    +
                    "photo_url_thumbnail      	TEXT,"    +
                    "caption            	 	TEXT,"    +
                    "created_time            	INTEGER," +
                    "location                   TEXT,"    +
                    "iliked            	        INTEGER," +
                    "likes_count            	INTEGER," +
                    "from_user_id            	INTEGER," +
                    "from_nick_name             TEXT,"    +
                    "from_image_url             TEXT,"    +
                    "comments_count            	INTEGER"  +
                     ");");
		}

        private void insertNativeCircles(SQLiteDatabase db) {
		    db.execSQL("INSERT INTO " + TABLE_CIRCLE + "(circleid, showOnStream, type)" + 
		      "VALUES (" + QiupuConfig.CIRCLE_ID_ALL + ", 1, -100)");
		    db.execSQL("INSERT INTO " + TABLE_CIRCLE + "(circleid, showOnStream, type)" + 
				      "VALUES (" + QiupuConfig.CIRCLE_ID_PUBLIC + ", 1, -100)");
		    db.execSQL("INSERT INTO " + TABLE_CIRCLE + "(circleid, showOnStream, type)" + 
				      "VALUES (" + QiupuConfig.CIRCLE_ID_HOT + ", 1, -100)");
		    db.execSQL("INSERT INTO " + TABLE_CIRCLE + "(circleid, showOnStream, type)" + 
				      "VALUES (" + QiupuConfig.CIRCLE_ID_NEAR_BY + ", 1, -100)");
		    
		}

        // start with v58
        private void createDirectoryMemberTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
            db.execSQL("CREATE TABLE " + TABLE_EMPLOYEE +" (" +
                    EmployeeColums.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EmployeeColums.OWNER_ID + "    TEXT," +
                    EmployeeColums.EMPLOYEE_ID + "    TEXT," +
                    EmployeeColums.USER_ID + "    TEXT," +
                    EmployeeColums.NAME + "    TEXT," +
                    EmployeeColums.NAME_PINYIN + "    TEXT," +
                    EmployeeColums.IMAGE_URL_S + "    TEXT," +
                    EmployeeColums.IMAGE_URL_M + "    TEXT," +
                    EmployeeColums.IMAGE_URL_L + "    TEXT," +
                    EmployeeColums.EMAIL + "    TEXT," +
                    EmployeeColums.TEL + "    TEXT," +
                    EmployeeColums.MOBILE_TEL + "    TEXT," +
                    EmployeeColums.DEPARTMENT + "    TEXT," +
                    EmployeeColums.JOB_TITLE + "    TEXT," +
                    EmployeeColums.IS_FAVORITE + "     INTEGER DEFAULT 0," +
                    EmployeeColums.DB_STATUS + "     INTEGER DEFAULT 0," +
                    EmployeeColums.ROLE_IN_GROUP + " INTEGER DEFAULT 0," +
                    EmployeeColums.REFERRED_COUNT + " INTEGER DEFAULT 0" +
                    ");");
        }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
            if (oldVersion < 25) {
                // construct db for too old version.
                dropTables(db);
                onCreate(db);
                return;
            }

            if (judgeVersion(oldVersion, 26)) {
                upgradeToVersion26(db);
                oldVersion = 26;
            }
            
            if (judgeVersion(oldVersion, 27)) {
                upgradeToVersion27(db);
                oldVersion = 27;
            }
            
            if(judgeVersion(oldVersion, 28)) {
            	upgradeToVersion28(db);
            	oldVersion = 28;
            }
            if(judgeVersion(oldVersion, 29)) {
                upgradeToVersion29(db);
                oldVersion = 29;
            }
            
            if(judgeVersion(oldVersion, 30)) {
            	upgradeToVersion30(db);
            	oldVersion = 30;
            }

            if(judgeVersion(oldVersion, 31)) {
                upgradeToVersion31(db);
                oldVersion = 31;
            }

            if(judgeVersion(oldVersion, 32)) {
            	upgradeToVersion32(db);
            	oldVersion = 32;
            }
            
            if(judgeVersion(oldVersion, 33)) {
                upgradeToVersion33(db);
                oldVersion = 33;
            }
            if(judgeVersion(oldVersion, 34)) {
                upgradeToVersion34(db);
                oldVersion = 34;
            }
            
            if(judgeVersion(oldVersion, 35)) {
                upgradeToVersion35(db);
                oldVersion = 35;
            }
            
            if(judgeVersion(oldVersion, 36)) {
            	upgradeToVersion36(db);
            	oldVersion = 36;
            }
            
            if(judgeVersion(oldVersion, 37)) {
                upgradeToVersion37(db);
                oldVersion = 37;
            }
            if (judgeVersion(oldVersion, 38)) {
            	upgradeToVersion38(db);
            	oldVersion = 38;
            }
            
            if (judgeVersion(oldVersion, 39)) {
                upgradeToVersion39(db);
                oldVersion = 39;
            }

            if (judgeVersion(oldVersion, 40)) {
                upgradeToVersion40(db);
                oldVersion = 40;
            }
            
            if (judgeVersion(oldVersion, 41)) {
                upgradeToVersion41(db);
                oldVersion = 41;
            }
            
            if (judgeVersion(oldVersion, 42)) {
                upgradeToVersion42(db);
                oldVersion = 42;
            }
            if (judgeVersion(oldVersion, 43)) {
                upgradeToVersion43(db);
                oldVersion = 43;
            }
            if (judgeVersion(oldVersion, 44)) {
                upgradeToVersion44(db);
                oldVersion = 44;
            }
            if (judgeVersion(oldVersion, 45)) {
                upgradeToVersion45(db);
                oldVersion = 45;
            }

            if (judgeVersion(oldVersion, 46)) {
                upgradeToVersion46(db);
                oldVersion = 46;
            }
            
            if (judgeVersion(oldVersion, 47)) {
                upgradeToVersion47(db);
                oldVersion = 47;
            }
            if (judgeVersion(oldVersion, 48)) {
                upgradeToVersion48(db);
                oldVersion = 48;
            }

            if (judgeVersion(oldVersion, 49)) {
                upgradeToVersion49(db);
                oldVersion = 49;
            }
            if (judgeVersion(oldVersion, 50)) {
            	upgradeToVersion50(db);
            	oldVersion = 50;
            }
            if (judgeVersion(oldVersion, 51)) {
                upgradeToVersion51(db);
                oldVersion = 51;
            }
            if (judgeVersion(oldVersion, 52)) {
                upgradeToVersion52(db);
                oldVersion = 52;
            }
            if (judgeVersion(oldVersion, 53)) {
            	upgradeToVersion53(db);
            	oldVersion = 53;
            }
            if (judgeVersion(oldVersion, 54)) {
                upgradeToVersion54(db);
                oldVersion = 54;
            }
            if (judgeVersion(oldVersion, 55)) {
                upgradeToVersion55(db);
                oldVersion = 55;
            }
            if (judgeVersion(oldVersion, 56)) {
                upgradeToVersion56(db);
                oldVersion = 56;
            }
            if (judgeVersion(oldVersion, 57)) {
                upgradeToVersion57(db);
                oldVersion = 57;
            }
            if (judgeVersion(oldVersion, 58)) {
            	upgradeToVersion58(db);
            	oldVersion = 58;
            }
            
            if (judgeVersion(oldVersion, 59)) {
            	upgradeToVersion59(db);
            	oldVersion = 59;
            }
            
            if (judgeVersion(oldVersion, 60)) {
                upgradeToVersion60(db);
                oldVersion = 60;
            }
            
            if (judgeVersion(oldVersion, 61)) {
                upgradeToVersion61(db);
                oldVersion = 61;
            }
            
            if(judgeVersion(oldVersion, 62)) {
            	upgradeToVersion62(db);
                oldVersion = 62;
            }
            
            if(judgeVersion(oldVersion, 63)) {
            	upgradeToVersion63(db);
                oldVersion = 63;
            }
            
            if(judgeVersion(oldVersion, 64)) {
            	upgradeToVersion64(db);
                oldVersion = 64;
            }
            
            if(judgeVersion(oldVersion, 65)) {
            	createCircleCirclesTable(db);
                oldVersion = 65;
            }
            
            if(judgeVersion(oldVersion, 66)) {
            	upgradeToVersion66(db);
                oldVersion = 66;
            }
            if(judgeVersion(oldVersion, 67)) {
            	upgradeToVersion67(db);
                oldVersion = 67;
            }
            
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }
		}
		
		private void upgradeToVersion67(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + CIRCLE_EVENT + " ADD COLUMN role_in_group INTEGER;");
			db.execSQL("ALTER TABLE " + CIRCLE_EVENT + " ADD COLUMN referred_count INTEGER;");
		}
		
		private void upgradeToVersion66(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_EMPLOYEE + " ADD COLUMN "+ EmployeeColums.REFERRED_COUNT +" INTEGER;");
			db.execSQL("ALTER TABLE " + CIRCLE_CIRCLES + " ADD COLUMN referred_count INTEGER;");
		}
		
		private void upgradeToVersion64(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_EMPLOYEE + " ADD COLUMN role_in_group INTEGER;");
		}
		
		private void upgradeToVersion63(SQLiteDatabase db) {
			createCategoryTable(db);
		}

		// add column "reffred_count" to circle table
		private void upgradeToVersion62(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN referred_count INTEGER;");
		}
		
		//fix for circle event create time issue
		private void upgradeToVersion61(SQLiteDatabase db) { 
			//rude update
			createCircleEventTable(db);
        }
		
		private void upgradeToVersion60(SQLiteDatabase db) { 
		    createTableCirclePoll(db);
        }

		private void upgradeToVersion59(SQLiteDatabase db) { 
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN formal_circle_count INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN free_circle_count INTEGER;");
            // TODO : why add create_time here as it was created in Version57
			try{
			    db.execSQL("ALTER TABLE " + CIRCLE_EVENT + " ADD COLUMN create_time INTEGER;");
			}catch(Exception ne){
				Log.e(TAG, "Error, please fix it msg="+ne.getMessage(), ne);
			}
		}
		
		private void upgradeToVersion57(SQLiteDatabase db) { 
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN event_count INTEGER;");
			createCircleEventTable(db);
		}
		
		private void upgradeToVersion56(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN parent_id INTEGER;");
		}
		
		private void upgradeToVersion55(SQLiteDatabase db) {
			createPageTable(db);
			
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN page_id INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN formal  INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN subtype TEXT;");
        }
		
        private void upgradeToVersion54(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN top_post_name  TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN top_post_count INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN top_poll_count INTEGER;");
        }

		private void upgradeToVersion53(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN db_status INTEGER;");
		}
		
		private void upgradeToVersion51(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN repeat_type INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN reminder_time INTEGER;");
		}
		
		private void upgradeToVersion50(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN creator_id INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN creator_name TEXT;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN creator_imageurl TEXT;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN invited_count INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN applied_count INTEGER;");
		}

        private void upgradeToVersion49(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_POLL + " ADD COLUMN user_name TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_POLL + " ADD COLUMN image_url TEXT;");
        }

		private void upgradeToVersion48(SQLiteDatabase db) {
			createTableEventsCalendar(db);
		}
		
		private void createTableEventsCalendar(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVNETS_CALENDAR_MAPPING);
			db.execSQL("CREATE TABLE " + TABLE_EVNETS_CALENDAR_MAPPING + " ("
					+ " _id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " event_id             INTEGER,"
					+ " calendar_event_id    INTEGER,"
					+ " update_time          INTEGER"
					+ ");");
		}
		
		private void createTableCompany(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANY);
			db.execSQL("CREATE TABLE " + TABLE_COMPANY+ " ("
					+ CompanyColumns.ID 				+ "  INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ CompanyColumns.COMPANY_ID 		+ "     INTEGER,"
					+ CompanyColumns.DEPARTMENT_ID		+ "     INTEGER,"
					+ CompanyColumns.CREATED_TIME 		+ "     INTEGER,"
					+ CompanyColumns.UPDATED_TIME 		+ "     INTEGER,"
					+ CompanyColumns.ROLE 				+ "     INTEGER,"
					+ CompanyColumns.PERSON_COUNT 		+ "     INTEGER,"
					+ CompanyColumns.DEPARTMENT_COUNT 	+ "     INTEGER,"
					+ CompanyColumns.DOMAIN1			+ "     TEXT,"
					+ CompanyColumns.DOMAIN2 			+ "     TEXT,"
					+ CompanyColumns.DOMAIN3 			+ "     TEXT,"
					+ CompanyColumns.DOMAIN4 			+ "     TEXT,"
					+ CompanyColumns.NAME 				+ "     TEXT,"
					+ CompanyColumns.ADDRESS 			+ "     TEXT,"
					+ CompanyColumns.EMAIL 				+ "     TEXT,"
					+ CompanyColumns.WEB_SITE 			+ "     TEXT,"
					+ CompanyColumns.TEL 				+ "     TEXT,"
					+ CompanyColumns.FAX 				+ "     TEXT,"
					+ CompanyColumns.ZIP_CODE 			+ "     TEXT,"
					+ CompanyColumns.SMALL_LOGO_URL 	+ "     TEXT,"
					+ CompanyColumns.LOGO_URL 			+ "     TEXT,"
					+ CompanyColumns.LARGE_LOGO_URL		+ "     TEXT,"
					+ CompanyColumns.SMALL_COVER_URL 	+ "     TEXT,"
					+ CompanyColumns.COVER_URL 			+ "     TEXT,"
					+ CompanyColumns.LARGE_COVER_URL	+ "     TEXT,"
					+ CompanyColumns.DESCRIPTION 		+ "     TEXT"
					+ ");");
		}
		private void upgradeToVersion47(SQLiteDatabase db) {
			 db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN need_invite_confirm INTEGER;");
		}
		
        private void upgradeToVersion46(SQLiteDatabase db) {
            createTablePoll(db);
        }

        private void createTablePoll(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLL);
            db.execSQL("CREATE TABLE " + TABLE_POLL + " ("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " poll_id         TEXT,"
                    + " uid             INTEGER,"
                    + " title           TEXT,"
                    + " description     TEXT,"
                    + " type            INTEGER,"
                    + " restrict        INTEGER,"
                    + " target          TEXT,"
                    + " multi_count     INTEGER,"
                    + " limits          INTEGER,"
                    + " privacy         INTEGER,"
                    + " created_time    INTEGER,"
                    + " end_time        INTEGER,"
                    + " updated_time    INTEGER,"
                    + " destroyed_time  INTEGER,"
                    + " attend_status   INTEGER,"
                    + " attend_count    INTEGER,"
                    + " left_time       INTEGER,"
                    + " viewer_can_vote INTEGER,"
                    + " mode            INTEGER,"
                    + " has_voted       INTEGER,"
                    + " viewer_left     INTEGER,"
                    + " user_name       TEXT,"
                    + " image_url       TEXT"
                    + ");");
        }

        private void createTableCirclePoll(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE_POLL);
            db.execSQL("CREATE TABLE " + TABLE_CIRCLE_POLL + " ("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " poll_id         TEXT,"
                    + " uid             INTEGER,"
                    + " title           TEXT,"
                    + " target          TEXT,"
                    + " created_time    INTEGER,"
                    + " end_time        INTEGER,"
                    + " attend_status   INTEGER,"
                    + " attend_count    INTEGER,"
                    + " user_name       TEXT,"
                    + " image_url       TEXT"
                    + ");");
        }

		private void upgradeToVersion45(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN from_user_id INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN from_nick_name TEXT;");
		    db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN from_image_url TEXT;");
		}
		private void upgradeToVersion44(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN cover TEXT;");
			createEventThemeTable(db);
		}

		private void upgradeToVersion43(SQLiteDatabase db) {
		    db.execSQL("ALTER TABLE " + TABLE_QIUPU_ALBUM + " ADD COLUMN updated_time INTEGER;");
		    db.execSQL("ALTER TABLE " + TABLE_QIUPU_ALBUM + " ADD COLUMN hava_expired INTEGER;");
		}
		private void upgradeToVersion42(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_SHARERESOURCE + " ADD COLUMN shared_static_file INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_SHARERESOURCE + " ADD COLUMN shared_audio INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_SHARERESOURCE + " ADD COLUMN shared_video INTEGER;");
		}
		private void upgradeToVersion41(SQLiteDatabase db) {
        	db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN start_time INTEGER;");
        	db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN end_time INTEGER;");
		} 

        private void upgradeToVersion40(SQLiteDatabase db) {
        	db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN can_member_quit INTEGER;");
		}

		private boolean judgeVersion(int oldVersion, int newVersion) {
            if (DATABASE_VERSION > oldVersion && oldVersion < newVersion) {
                return true;
            } else {
                return false;
            }
        }

		private void upgradeToVersion30(SQLiteDatabase db) {
		    db.execSQL("ALTER TABLE " + TABLE_USER_IMAGE + " ADD COLUMN display_name TEXT;");
		}

		private void upgradeToVersion29(SQLiteDatabase db) {
			createTableQiupuAlbum(db);
			
		}
		private void upgradeToVersion31(SQLiteDatabase db) {
			createTableQiupuPhoto(db);
			
		}
		private void upgradeToVersion52(SQLiteDatabase db) {
			createTableCompany(db);
			
		}
		private void upgradeToVersion28(SQLiteDatabase db) {
		    changeCircleTab28(db);
		    createGroupTable(db);
		}
        private void upgradeToVersion58(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
            createDirectoryMemberTable(db);

        }
		
		private void upgradeToVersion27(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN member_limit INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN is_stream_public INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN can_search INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN can_view_members INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN can_join INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN created_time INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN updated_time INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN destroyed_time INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN role_in_group INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN viewer_can_update INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN viewer_can_destroy INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN viewer_can_remove INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN viewer_can_grant INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_CIRCLE + " ADD COLUMN type INTEGER;");
		}
		
		private void upgradeToVersion33(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN photo_url_middle TEXT;");
			db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN photo_url_big TEXT;");
		}
		
		
        private void upgradeToVersion26(SQLiteDatabase db) {
            createPossibleNameTable(db);
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN remark TEXT;");
        }

        private void createPossibleNameTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERHAPS_NAME);
            db.execSQL("CREATE TABLE " + TABLE_PERHAPS_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uid                INTEGER," +
                    "fullname           TEXT," +
                    "count              INTEGER" +
                    ");");
        }
        
        private void changeCircleTab28(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE);
            db.execSQL("CREATE TABLE " + TABLE_CIRCLE +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "circleid    INTEGER," +
                    "uid         INTEGER," +                    
                    "name        TEXT," +
                    "memberCount INTEGER," +
                    "showOnStream INTEGER," +
                    "type         INTEGER DEFAULT 0" +

                    ");");
            insertNativeCircles(db);
        }
        private void createGroupTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
            db.execSQL("CREATE TABLE " + TABLE_GROUP +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "circleid    INTEGER," +
                    "profile_image_url  TEXT,"+
                    "profile_simage_url TEXT,"+
                    "profile_limage_url TEXT,"+
                    "company            TEXT,"+
                    "office_address     TEXT,"+
                    "department         TEXT,"+
                    "job_title          TEXT,"+
                    "location           TEXT,"+
                    "description        TEXT,"+
                    
                    "member_limit       INTEGER," +
                    "is_stream_public   INTEGER," +                    
                    "can_search         INTEGER," +
                    "can_view_members   INTEGER," +
                    "can_join           INTEGER," +
                    "can_member_invite  INTEGER," +
                    "can_member_approve INTEGER," +
                    "can_member_post    INTEGER," +
                    "can_member_quit    INTEGER," +
                    "need_invite_confirm INTEGER," +
                    
                    "created_time       INTEGER," +
                    "updated_time       INTEGER," +
                    "destroyed_time     INTEGER," +
                    "role_in_group      INTEGER," +
                    "viewer_can_update  INTEGER," +
                    "viewer_can_destroy INTEGER," +
                    "viewer_can_remove  INTEGER," +
                    "viewer_can_grant   INTEGER," +
                    "viewer_can_quit    INTEGER," +
                    "bulletin           TEXT,"    +
                    "bulletin_updated_time  INTEGER," +
                    "invited_ids        TEXT," +
                    "start_time         INTEGER," + 
                    "end_time           INTEGER," +
                    "cover              TEXT," +
                    "creator_id         INTEGER," +
                    "creator_name       TEXT," +
                    "creator_imageurl   TEXT," +
                    "invited_count      INTEGER," + 
                    "applied_count      INTEGER," +
                    "repeat_type        INTEGER," +
                    "reminder_time      INTEGER," +
                    "top_post_name      TEXT," +
                    "top_post_count     INTEGER," +
                    "top_poll_count     INTEGER," +
                    "event_count        INTEGER," +
                    "page_id            INTEGER," +
                    "formal             INTEGER," +
                    "subtype            TEXT," +
                    "parent_id          INTEGER,"+
                    "formal_circle_count    INTEGER,"+
                    "free_circle_count      INTEGER"+
                    
                    ");");
        }
        
        private void createEventThemeTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
            db.execSQL("CREATE TABLE " + TABLE_THEME +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id            INTEGER," +
                    "creator       INTEGER,"+
                    "updated_time INTEGER,"+
                    "name          TEXT,"+
                    "image_url     TEXT"+
                    ");");
        }
        
        private void upgradeToVersion34(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN can_member_invite INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN can_member_approve INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN can_member_post INTEGER;");
        }
        
        private void upgradeToVersion35(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN viewer_can_quit INTEGER;");
        }
        
        private void upgradeToVersion36(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN bulletin TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN bulletin_updated_time INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN invited_ids TEXT;");
        }
        
        private void upgradeToVersion37(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN iliked INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN likes_count INTEGER;");
			db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN comments_count INTEGER;");
		}
        private void upgradeToVersion38(SQLiteDatabase db) {
        	db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN referred_count INTEGER;");
        }
        private void upgradeToVersion39(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_QIUPU_PHOTO + " ADD COLUMN photo_url_original TEXT;");
        }
        private void upgradeToVersion32(SQLiteDatabase db) {
            createChatRecordTable(db);
        }

        private void createChatRecordTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_RECORD);
            db.execSQL("CREATE TABLE " + TABLE_CHAT_RECORD + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "borqs_id INTEGER," +
                    "message TEXT," +
                    "profile_url TEXT," +
                    "type INTEGER," +
                    "created_time INTEGER," +
                    "unread INTEGER," +
                    "display_name TEXT" +
                    ");");
        }

        private void createPageTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGE);
            db.execSQL("CREATE TABLE " + TABLE_PAGE +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pageid         INTEGER," +
                    "name            TEXT,"+
                    "name_en         TEXT,"+
                    "address         TEXT,"+
                    "address_en      TEXT,"+
                    "description     TEXT,"+
                    "description_en  TEXT,"+
                    "email           TEXT,"+
                    "website         TEXT,"+
                    "tel             TEXT,"+
                    "fax             TEXT," +
                    "zip_code        TEXT," +                    
                    "small_logo_url  TEXT," +
                    "logo_url        TEXT," +
                    "large_logo_url  TEXT," +
                    "small_cover_url TEXT," +
                    "cover_url       TEXT," +
                    "large_cover_url TEXT," +
                    "associated_id   INTEGER," +
                    "free_circle_ids TEXT," +
                    "created_time    INTEGER," +
                    "updated_time    INTEGER," +
                    "followers_count INTEGER," +
                    "followed        INTEGER," +
                    "viewer_can_update     INTEGER," +
                    "creatorId     INTEGER" +
                    ");");
        }

        private void createCircleEventTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + CIRCLE_EVENT);
            db.execSQL("CREATE TABLE " + CIRCLE_EVENT +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "circleid    INTEGER," +
                    "event_id    INTEGER," +
                    "event_name  TEXT," +
                    "location           TEXT,"+
                    "start_time         INTEGER," + 
                    "end_time           INTEGER," +
                    "cover              TEXT," +
                    "creator_id         INTEGER," +
                    "creator_name       TEXT," +
                    "creator_imageurl   TEXT," +
                    "create_time        INTEGER," +
                    "role_in_group      INTEGER," +
                    "referred_count     INTEGER" +
                    ");");
        }
        
        private void createCategoryTable(SQLiteDatabase db) {
        	db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        	db.execSQL("CREATE TABLE " + TABLE_CATEGORY +" (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category_id    INTEGER," +
                    "category_name    TEXT," +
                    "creator_id  INTEGER," +
                    "scope_id           INTEGER,"+
                    "scope_name         TEXT" + 
                    ");");
        }
        
        private void createCircleCirclesTable(SQLiteDatabase db) {
        	db.execSQL("DROP TABLE IF EXISTS " + CIRCLE_CIRCLES);
        	db.execSQL("CREATE TABLE " + CIRCLE_CIRCLES +" (" +
        			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "circleid    INTEGER," +
                    "circleid_name    TEXT," +
                    "profile_image_url  TEXT,"+
                    "profile_simage_url TEXT,"+
                    "profile_limage_url TEXT,"+
                    "description        TEXT,"+
                    "role_in_group      INTEGER," +
                    "cover              TEXT," +
                    "formal             INTEGER," +
                    "subtype            TEXT," +
                    "parent_id          INTEGER,"+
                    "referred_count     INTEGER" + 
                    ");");
        }
        
        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CIRCLES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORK_EXPERIENCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDUCATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARERESOURCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONE_EMAIL);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_IMAGE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_PHOTOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QIUPU_ALBUM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QIUPU_PHOTO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERHAPS_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_RECORD);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLL);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVNETS_CALENDAR_MAPPING);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE); // created in v58
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIRCLE_POLL); // created in v60
            db.execSQL("DROP TABLE IF EXISTS " + CIRCLE_EVENT); // created in v60
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY); // created in v60
            db.execSQL("DROP TABLE IF EXISTS " + CIRCLE_CIRCLES); 
            
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
             *    2.whether saving high version database's data or not.
             *    3.when do so, it will use the installing application's version to 
             *      decide how to create database and tables.
             */
        }

	}

	static String getGroupby(String tableName) {
        if (TABLE_USERS.equals(tableName)) {
            return "uid";
        }

        return null;
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
                this.groupby = getGroupby(this.table);
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
            	if(url.getPathSegments().size() == 2 && url.getPathSegments().get(1).equals("apkgroup")){
            		this.table = url.getPathSegments().get(0);
            		this.where = where;
            		this.args = args;
            		this.groupby = QiupuORM.ApkinfoGroupColumns.APKSTATUS;
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
