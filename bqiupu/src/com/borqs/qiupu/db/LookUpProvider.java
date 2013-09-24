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

public class LookUpProvider extends ContentProvider {
    private static final String     TAG              = "LookUpProvider";

    public static final String      DATABASE_NAME    = "lookup.db";
    public static final int         DATABASE_VERSION = 7;
    public static final String      AUTHORITY        = "com.borqs.lookup";
    public static SQLiteOpenHelper mOpenHelper;

    private static final String     TABLE_LOOKUP    = "lookup";
//    private static final String     TABLE_PEOPLE    = "people";

    public static final String _ID = "_id";
    public static final String CONTACT_ID = "contact_id";
    public static final String PHOTO_ID = "photo_id";
    public static final String UID = "uid";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String TYPE = "type";
//    public static final String IMAGE_URL = "image_url";
//    public static final String DISPLAY_NAME = "display_name";
    public static final Uri CONTENT_LOOKUP_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_LOOKUP);

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_LOOKUP + " ("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " contact_id INTEGER,"
                    + " photo_id INTEGER,"
                    + " uid INTEGER DEFAULT -1,"
                    + " name TEXT,"
                    + " email TEXT,"
                    + " phone TEXT,"
                    + " type INTEGER DEFAULT -1"
                    + " );");

//            db.execSQL("CREATE TABLE " + TABLE_PEOPLE + " ("
//                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT,"
//                    + " contact_id INTEGER,"
//                    + " uid INTEGER DEFAULT -1,"
//                    + " photo_id INTEGER,"
//                    + " name TEXT,"
//                    + " display_name TEXT,"
//                    + " image_url TEXT,"
//                    + " type INTEGER DEFAULT -1"
//                    + " );");
        }

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOOKUP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
            Log.d(TAG, "upgradedb from version " + oldVersion + " to version " + newVersion);
            if (oldVersion < 7) {
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
             *    2.whether saving high version database's data or not.
             *    3.when do so, it will use the installing application's version to 
             *      decide how to create database and tables.
             */
        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args,
                args.groupby, null, sortOrder);
        return result;
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
        if (rowId <= 0)
            return null;
        else {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        uri = ContentUris.withAppendedId(uri, rowId);
        return uri;
    }

    static class SqlArguments {
        public final String   table;
        public final String   where;
        public final String[] args;
        public String         groupby = null;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException(
                        "WHERE clause not supported: " + url);
            } else {
                if (url.getPathSegments().size() == 2
                        && url.getPathSegments().get(1).equals("apkgroup")) {
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
