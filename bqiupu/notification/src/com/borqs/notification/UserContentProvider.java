package com.borqs.notification;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UserContentProvider extends ContentProvider{
	
	public static final String DBNAME = "userdb";
    public static final String TNAME = "user";
    public static final int VERSION = 3;
     
    public static String TID = "tid";
    public static final String BORQSID = "borqs_id";
    public static final String STATUS = "status";
     
    public static final String AUTOHORITY = "com.borqs.notification";
    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;
     
    public static final String CONTENT_TYPE = AUTOHORITY + "/user";
    public static final String CONTENT_ITEM_TYPE = AUTOHORITY + ".item/user";
     
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/user");
    
    public SQLiteDatabase db;
    
    
    
    private static final UriMatcher sMatcher;
    static{
            sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            sMatcher.addURI(AUTOHORITY,TNAME, ITEM);
            sMatcher.addURI(AUTOHORITY, TNAME+"/#", ITEM_ID);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
            // TODO Auto-generated method stub
        int count = 0;
        switch (sMatcher.match(uri)) {
            case ITEM:
                count = db.delete(TNAME,selection, selectionArgs);
                break;
            case ITEM_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(TID, TID+"="+id+(!TextUtils.isEmpty(TID="?")?"AND("+selection+')':""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
    }

    @Override
    public String getType(Uri uri) {
            // TODO Auto-generated method stub
        switch (sMatcher.match(uri)) {
            case ITEM:
                return CONTENT_TYPE;
            case ITEM_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
            // TODO Auto-generated method stub
        long rowId;
        if(sMatcher.match(uri)!=ITEM){
            throw new IllegalArgumentException("Unknown URI"+uri);
        }
        rowId = db.insert(TNAME,TID,values);
        if(rowId>0){
            Uri noteUri=ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new IllegalArgumentException("Unknown URI"+uri);
    }

    @Override
    public boolean onCreate() {
            // TODO Auto-generated method stub
    	db = SQLiteDatabase.create(null);
       db.execSQL("create table "+TNAME+"(" +
              TID+" integer primary key autoincrement not null,"+
              BORQSID+" text not null," +
              STATUS+" text not null);");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                    String[] selectionArgs, String sortOrder) {
            // TODO Auto-generated method stub              
        Cursor c;
        Log.d("-------", String.valueOf(sMatcher.match(uri)));
        switch (sMatcher.match(uri)) {
        case ITEM:
            c = db.query(TNAME, projection, selection, selectionArgs, null, null, null);
            break;
        case ITEM_ID:
            String id = uri.getPathSegments().get(1);
            c = db.query(TNAME, projection, TID+"="+id+(!TextUtils.isEmpty(selection)?"AND("+selection+')':""),selectionArgs, null, null, sortOrder);
            break;
        default:
            Log.d("!!!!!!", "Unknown URI"+uri);
            throw new IllegalArgumentException("Unknown URI"+uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
            // TODO Auto-generated method stub
        return 0;
    }

}
