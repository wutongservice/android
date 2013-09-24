package com.borqs.information.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.information.InformationBase;
import com.borqs.information.db.Notification.NotificationColumns;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationReadCache;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.StringUtil;

public class NotificationOperator implements IOperator {
	
	private ContentResolver mResolver;
	private BatchOperation batchOperation;
	private Context mContext;
	private static final String TAG = "NotificationOperator";
	
	public NotificationOperator(Context context) {
	    mContext = context;
		mResolver = context.getContentResolver();
		batchOperation = new BatchOperation(context);
	}

	@Override
	public void add(InformationBase infor) {
		ContentValues values = buildContentValues(infor, AccountServiceUtils.getBorqsAccountID());
		mResolver.insert(NotificationColumns.CONTENT_URI, values);
	}

	@Override
	public void delete(String where, String selectionArgs[]) {
		mResolver.delete(NotificationColumns.CONTENT_URI, where + " and " + NotificationColumns.U_ID + "=" + Long.toString(AccountServiceUtils.getBorqsAccountID()), selectionArgs);
	}
	
	@Override
	public boolean update(long id, ContentValues values) {
		return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI, id), values, null, null) > 0;
	}
	
	@Override
	public boolean update(long id, InformationBase infor) {
		ContentValues values = buildContentValues(infor, AccountServiceUtils.getBorqsAccountID());
		return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI, id), values, null, null) > 0;
	}

	@Override
	public long getLastModifyDate() {
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[] {NotificationColumns.LAST_MODIFY}, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}, NotificationColumns.LAST_MODIFY + " desc");
		long lastModifyDate = 0;
		if(cursor.moveToNext()) {
			lastModifyDate = cursor.getLong(0);
		}
		cursor.close();
		return lastModifyDate;
	}

	public long getEarliestModifyDate() {
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[] {NotificationColumns.LAST_MODIFY}, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}, NotificationColumns.LAST_MODIFY + " ASC");
		long lastModifyDate = 0;
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				lastModifyDate = cursor.getLong(0);
			}
			cursor.close();
			cursor = null;
		}
		return lastModifyDate;
	}

	@Override
	public int getUnReadCount() {
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, null, NotificationColumns.IS_READ + "=? and " + NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?", new String[] {"0", Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"}, null);
		int count = 0;
		if(cursor.moveToNext()) {
			count = cursor.getCount();
		}
		cursor.close();
		return count;
	}

	@Override
	public int getThisWeekUnReadCount() {
		long time = System.currentTimeMillis() - 7*24*60*60*1000L;
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, null, NotificationColumns.IS_READ + "=? and " + NotificationColumns.U_ID + "=? and " + NotificationColumns.LAST_MODIFY + ">? and " + NotificationColumns.PROCESSED + "=?", new String[] {"0", Long.toString(AccountServiceUtils.getBorqsAccountID()), Long.toString(time), "0"}, null);
		int count = 0;
		if(cursor.moveToNext()) {
			count = cursor.getCount();
		}
		cursor.close();
		return count;
	}

//    public String getThisWeekUnReadInforIds() {
//        long time = System.currentTimeMillis() - 7*24*60*60*1000L;
//        String[] projectString = new String[]{NotificationColumns.M_ID};
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, projectString, NotificationColumns.IS_READ + "=? and " + NotificationColumns.U_ID + "=? and " + NotificationColumns.LAST_MODIFY + ">? and " + NotificationColumns.PROCESSED + "=?", new String[] {"0", Long.toString(AccountServiceUtils.getBorqsAccountID()), Long.toString(time), "0"}, null);
//        StringBuilder ids = new StringBuilder();
//        if(cursor != null) {
//            if(cursor.moveToFirst()) {
//                do {
//                    if(ids.length() > 0) {
//                        ids.append(",");
//                    }
//                    ids.append(cursor.getString(0));
//                } while (cursor.moveToNext());
//
//            }
//            cursor.close();
//            cursor = null;
//        }
//        return ids.toString();
//    }

    public String getAllUnReadInfoIds() {
        String[] projectString = new String[]{NotificationColumns.M_ID};
        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, projectString,
                NotificationColumns.IS_READ + "=? and " +
                        NotificationColumns.U_ID + "=? and " +
//                        NotificationColumns.LAST_MODIFY + ">? and " +
                        NotificationColumns.PROCESSED + "=?",
                new String[] {"0",
                        Long.toString(AccountServiceUtils.getBorqsAccountID()),
//                        Long.toString(time),
                        "0"},
                null);
        StringBuilder ids = new StringBuilder();
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    if(ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append(cursor.getString(0));
                } while (cursor.moveToNext());
                
            }
            cursor.close();
            cursor = null;
        }
        return ids.toString();
    }
    
	private ContentValues buildContentValues(InformationBase infor, long uid) {
		ContentValues values = new ContentValues();
		values.put(NotificationColumns.M_ID, infor.id);
		values.put(NotificationColumns.TYPE, infor.type);
		values.put(NotificationColumns.APP_ID, infor.appId);
		values.put(NotificationColumns.DATA, infor.data);
		values.put(NotificationColumns.LONGPRESSURI, infor.apppickurl == null?"":infor.apppickurl.toString());
		values.put(NotificationColumns.DATE, infor.date);
		values.put(NotificationColumns.IMAGE_URL, infor.image_url);
		values.put(NotificationColumns.IS_READ, infor.read);
		values.put(NotificationColumns.PROCESSED, false);
		values.put(NotificationColumns.LAST_MODIFY, infor.lastModified);
		values.put(NotificationColumns.RECEIVER_ID, infor.receiverId);
		values.put(NotificationColumns.SENDER_ID, infor.senderId);
		values.put(NotificationColumns.TITLE, infor.title);
		values.put(NotificationColumns.URI, infor.uri);
		values.put(NotificationColumns.BODY, infor.body);
		values.put(NotificationColumns.BODY_HTML, infor.body_html);
		values.put(NotificationColumns.TITLE_HTML, infor.title_html);
		values.put(NotificationColumns.U_ID, uid);
		values.put(NotificationColumns.SCENE, infor.scene);
		return values;
	}

	@Override
	public int add(List<InformationBase> items) {
	    HashMap<Long, NotificationObj> result = getExistedItems();
		long uid = AccountServiceUtils.getBorqsAccountID();
		if(items.size() > 0 && isneedRemove(items.get(items.size() - 1).lastModified)) {
			clearAllDbNtf();
		}
		int handledCount = 0;
		for (InformationBase item : items) {
			if (result.containsKey(item.id)) {
			    if (hasRead(item, result) == true) {
			        //do nothing, no data change.
			        handledCount++;
			    } else {
                    checkCachedReadItem(item);
			        batchOperation.add(ContentProviderOperation
						.newUpdate(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, item.id))
						.withValues(buildContentValues(item, uid)).build());
			    }
			} else {
                checkCachedReadItem(item);
				batchOperation.add(ContentProviderOperation
						.newInsert(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY)
						.withValues(buildContentValues(item, uid)).build());
			}
			if (batchOperation.size() >= 100) {
				batchOperation.execute(true);
			}
		}
		batchOperation.execute(true);
		return items.size() - handledCount;
	}
	
	public int add(List<InformationBase> items, boolean isTome) {
	    HashMap<Long, NotificationObj> result = getExistedItems();
		long uid = AccountServiceUtils.getBorqsAccountID();
//		if(items.size() > 0 && isneedRemove(items.get(items.size() - 1).lastModified)) {
//			if(isTome) {
//				removeAllTomeNtf();
//			}else {
//				removeAllWithOutTomeNtf();
//			}
//		}
		int handledCount = 0;
		
		for (InformationBase item : items) {
			checkCachedReadItem(item);
			if (hasRead(item, result)) {
				handledCount++;
			}
			
			if (result.containsKey(item.id)) {
			    updateNtf(item);
			    
			} else {
//                checkCachedReadItem(item);
				batchOperation.add(ContentProviderOperation
						.newInsert(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY)
						.withValues(buildContentValues(item, uid)).build());
			}
			if (batchOperation.size() >= 100) {
				batchOperation.execute(true);
			}
		}
		
		batchOperation.execute(true);
		return items.size() - handledCount;
	}
	
	private void removeAllTomeNtf(String sceneId) {
		String deletewhere = NotificationColumns.SCENE + " = " + sceneId + " and " 
	            + NotificationColumns.DATA + " is not null and " 
	            + NotificationColumns.DATA + " != '' and " 
				+ NotificationColumns.DATA + " like \'%," 
	            + AccountServiceUtils.getBorqsAccountID() 
	            + ",%\' and " + NotificationColumns.PROCESSED + " = "+ 0;
		int count = mResolver.delete(NotificationColumns.CONTENT_URI, deletewhere, null);
		Log.i(TAG, "delete count : " + count);
	}
	private void removeAllWithOutTomeNtf(String sceneId) {
		String deletewhere = "(" + NotificationColumns.DATA + " is null or " + NotificationColumns.DATA + "='' or " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\') and "
	            + NotificationColumns.PROCESSED + " = "+ 0 + " and " + NotificationColumns.SCENE + " = " + sceneId;
		int count = mResolver.delete(NotificationColumns.CONTENT_URI, deletewhere, null);
		Log.i(TAG, "removeAllWithOutTomeNtf delete count : " + count);
	}

    private boolean hasRead(InformationBase item, HashMap<Long, NotificationObj> result) {
    	boolean is_read = false;
    	final NotificationObj obj = result.get(item.id); 
    	if(obj != null) {
    		is_read = obj.is_read;
    		item.read = obj.is_read;
    		if (obj.last_modify == item.lastModified) {
    			if(!item.read) {
    				InformationReadCache.ReadStreamCache.cacheUnReadNtfIdsWithoutDb(item.id);
    			}
                is_read = true;
            } else {
                is_read = false;
            }
    	}else {
    		is_read = item.read;
    	}
        return is_read;
    }

	@Override
	public boolean updateReadStatus(long id, boolean status) {
		ContentValues values = new ContentValues();
		values.put(NotificationColumns.IS_READ, status);
		return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, id), values, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}) > 0;
	}
	
	@Override
	public boolean updateAllReadStatus(boolean status) {
		ContentValues values = new ContentValues();
		values.put(NotificationColumns.IS_READ, status);
		return mResolver.update(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, values, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}) > 0;
	}
	
	public void updateReadStatusWithType(boolean isToMe, boolean  status) {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
		String where = null;
		if(isToMe) {
			where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.U_ID + "=" + AccountServiceUtils.getBorqsAccountID()
					+ " and " + NotificationColumns.PROCESSED + "= 0 and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'";
			
		}else {
			where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.U_ID + "=" + AccountServiceUtils.getBorqsAccountID()
					+ " and " + NotificationColumns.PROCESSED + "= 0 and " + NotificationColumns.DATA + " is null or " + NotificationColumns.DATA + "='' or " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'";
		}
		ContentValues values = new ContentValues();
		values.put(NotificationColumns.IS_READ, status);
		mResolver.update(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, values, where, null);
	}
	
	public void updateNtf(InformationBase infor) {
		String where = NotificationColumns.U_ID + "=" + AccountServiceUtils.getBorqsAccountID()
					+ " and " + NotificationColumns.PROCESSED + "= 0 and " + NotificationColumns.M_ID + "  = " + infor.id;
			
		ContentValues values = buildContentValues(infor, AccountServiceUtils.getBorqsAccountID());
		mResolver.update(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, values, where, null);
	}
	
	@Override
	public Cursor loadAll(String queryType) {
        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
		return mResolver.query(NotificationColumns.CONTENT_URI, null,
                NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + actualType,
                new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
                NotificationColumns.LAST_MODIFY + " desc");
	}
	
	public Cursor loadNtfToMe(String queryType) {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
		return mResolver.query(NotificationColumns.CONTENT_URI, null,
                NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + actualType + " and " +  NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\' and " + NotificationColumns.SCENE + " = " + sceneId,
                new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
                NotificationColumns.LAST_MODIFY + " desc");
	}
	
	public String loadNtfToMeString() {
		StringBuilder tmpString = new StringBuilder();
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.M_ID},
				NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + " and " + NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " +  NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'",
				new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
				NotificationColumns.LAST_MODIFY + " desc"); 
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				do {
					if(tmpString.length() > 0) {
						tmpString.append(",");
					}
					tmpString.append(cursor.getString(cursor.getColumnIndex(NotificationColumns.M_ID)));
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return tmpString.toString();
	}
	
	public String loadUnReadNtfToMeString() {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
		StringBuilder tmpString = new StringBuilder();
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.M_ID},
				NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + " and " +  NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'" + " and " + NotificationColumns.IS_READ + "=" + 0  + " and " +NotificationColumns.SCENE + " = " + sceneId ,
				new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
				NotificationColumns.LAST_MODIFY + " desc"); 
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				do {
					if(tmpString.length() > 0) {
						tmpString.append(",");
					}
					tmpString.append(cursor.getString(cursor.getColumnIndex(NotificationColumns.M_ID)));
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return tmpString.toString();
	}
	
	public String loadunReadNtfOtherString() {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
		StringBuilder tmpString = new StringBuilder();
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.M_ID},
				NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + " and " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'" + " and " + NotificationColumns.IS_READ + "=" + 0 + " and " +NotificationColumns.SCENE + " = " + sceneId,
				new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
				NotificationColumns.LAST_MODIFY + " desc"); 
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				do {
					if(tmpString.length() > 0) {
						tmpString.append(",");
					}
					tmpString.append(cursor.getString(cursor.getColumnIndex(NotificationColumns.M_ID)));
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return tmpString.toString();
	}
	
	public int loadUnReadToMeNtfCount() {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
		String where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.U_ID + "=" + AccountServiceUtils.getBorqsAccountID()
				+ " and " + NotificationColumns.PROCESSED + "= 0 and " + NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\' and " + NotificationColumns.IS_READ + "=" + 0;
		Cursor cursor = mContext.getContentResolver().query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.M_ID},
                where, null, null);
		if(cursor != null) {
			return cursor.getCount();
		}
		return 0;
	}
	
	public Cursor loadNtfWithOutToMe(String queryType) {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
		return mResolver.query(NotificationColumns.CONTENT_URI, null,
                NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?" + actualType+ " and (" + NotificationColumns.DATA + " is null or " + NotificationColumns.DATA + "='' or " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\') and " + NotificationColumns.SCENE + " = " + sceneId,
                new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "0"},
                NotificationColumns.LAST_MODIFY + " desc");
	}
	
	public int loadUnReadOtherNtfCount() {
		//add filter with scene
		final String sceneId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
		String where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.U_ID + "=" + AccountServiceUtils.getBorqsAccountID()
				+ " and " + NotificationColumns.PROCESSED + "= 0 and " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\' and " + NotificationColumns.IS_READ + "=" + 0;
		Cursor cursor = mContext.getContentResolver().query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.M_ID},
                where, null, null);
		if(cursor != null) {
			return cursor.getCount();
		}
		return 0;
	}
	
	
	@Override
	public Cursor loadThisWeek(String queryType) {
		long time = System.currentTimeMillis() - 7*24*60*60*1000L;
        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
		return mResolver.query(NotificationColumns.CONTENT_URI, null,
                NotificationColumns.U_ID + "=? and " +
                        NotificationColumns.LAST_MODIFY + ">? and " +
                        NotificationColumns.PROCESSED + "=?" +
                        actualType,
                new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()),
                        Long.toString(time), "0"},
                NotificationColumns.LAST_MODIFY + " desc");
	}

	private HashMap<Long, NotificationObj> getExistedItems() {
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
				new String[] {NotificationColumns.M_ID, NotificationColumns.LAST_MODIFY, NotificationColumns.IS_READ}, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}, null);
		HashMap<Long, NotificationObj> result = new HashMap<Long, NotificationObj>();
		if(cursor != null) {
			try {
				while(cursor.moveToNext()) {
				    NotificationObj nObj = new NotificationObj();
				    long m_id = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.M_ID));
	                nObj.last_modify = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));
	                nObj.is_read = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IS_READ)) == 1;
	                result.put(m_id, nObj);
				}
			} finally {
				cursor.close();
			}
		}
		return result;
	}

    private class NotificationObj {
        boolean is_read;
        long last_modify;
    }

	public HashSet<Long> getProcessedItems() {
		Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
				new String[] {NotificationColumns.M_ID}, NotificationColumns.U_ID + "=? and " + NotificationColumns.PROCESSED + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID()), "1"}, null);
		HashSet<Long> result = new HashSet<Long>();
		if(cursor != null) {
			try {
				while(cursor.moveToNext()) {
					long m_id = cursor.getLong(0);
					result.add(m_id);
				}
			} finally {
				cursor.close();
			}
		}
		return result;
	}
	
	public boolean updateReadStatusByUrl(String url, boolean status) {
        synchronized (this) {
            ContentValues values = new ContentValues();
            values.put(NotificationColumns.IS_READ, status);
            return mResolver.update(NotificationColumns.CONTENT_URI, values, NotificationColumns.URI + " like '" + url + "'", null) > 0;
        }
    }
	
	public String getUnReadInformationWithUrl(String url) {
	    String where = NotificationColumns.U_ID + "="+ AccountServiceUtils.getBorqsAccountID() + " and " + NotificationColumns.URI + " like '" + url + "' and " + NotificationColumns.IS_READ + "=" + 0;
	    Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
                new String[] {NotificationColumns.M_ID}, where, null, null);
	    
	    StringBuilder inforIds = new StringBuilder();
	    if(cursor != null) {
	        if(cursor.moveToFirst()) {
	            do {
	                if(inforIds.length() > 0) {
	                    inforIds.append(",");
	                }
	                inforIds.append(cursor.getString(0));
	                
                } while (cursor.moveToNext());
	        }
            cursor.close();
	    }
	    return inforIds.toString();
	}

    public boolean setProcessedToHideNotification() {
        ContentValues values = new ContentValues();
        values.put(NotificationColumns.PROCESSED, 1);
        return mResolver.update(NotificationColumns.CONTENT_URI, values, NotificationColumns.PROCESSED + "=?", new String[]{"0"}) > 0;
    }

    public int getUnProcessedNotification() {
        String where = NotificationColumns.PROCESSED + " = " + 0;
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = mResolver.query(NotificationColumns.CONTENT_URI,
                    new String[] {NotificationColumns.M_ID}, where, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                count = cursor.getCount();
            }
            return count;
        } finally {
            if (cursor != null && cursor.isClosed() == false) {
                cursor.close();
                cursor = null;
            }
        }
    }
    
    private String[] NTF_PROJECTION = new String[]{
    		NotificationColumns._ID,
    		NotificationColumns.M_ID,
    		NotificationColumns.TYPE,
    		NotificationColumns.APP_ID,
    		NotificationColumns.IMAGE_URL,
    		NotificationColumns.RECEIVER_ID,
    		NotificationColumns.SENDER_ID,
    		NotificationColumns.DATE,
    		NotificationColumns.TITLE,
    		NotificationColumns.URI,
    		NotificationColumns.LAST_MODIFY,
    		NotificationColumns.DATA,
    		NotificationColumns.LONGPRESSURI,
    		NotificationColumns.IS_READ,
    		NotificationColumns.PROCESSED,
    		NotificationColumns.BODY,
    		NotificationColumns.BODY_HTML,
    		NotificationColumns.TITLE_HTML,
    		NotificationColumns.U_ID
    		
    };
    
    public static InformationBase createInformation(Cursor cursor) {
    	InformationBase data = new InformationBase();
		data._id = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns._ID));
		data.id = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.M_ID));
		data.type = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TYPE));
		data.appId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.APP_ID));
		data.image_url = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IMAGE_URL));
		data.receiverId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.RECEIVER_ID));
		data.senderId = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.SENDER_ID));
		data.date = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.DATE));
		data.title = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE));
		data.uri = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.URI));
		data.lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LAST_MODIFY));
		data.data = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.DATA));
		try{
		    data.apppickurl = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.LONGPRESSURI)));
		}catch(Exception ne){}
		data.read = cursor.getInt(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.IS_READ))==1;
		data.body = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY));
		data.body_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.BODY_HTML));
		data.title_html = cursor.getString(cursor.getColumnIndexOrThrow(Notification.NotificationColumns.TITLE_HTML));
		return data;
	}
    
    public ArrayList<InformationBase> getNotiflcations() {
    	String where = NotificationColumns.PROCESSED + " = " + 0 ;
    	String sortOrder = NotificationColumns.LAST_MODIFY + " DESC";
    	ArrayList<InformationBase> list = new ArrayList<InformationBase>();
    	Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
    			NTF_PROJECTION, where, null, sortOrder);
    	if (cursor != null) {
    		if (cursor.moveToFirst()) {
    			do {
    				InformationBase info = createInformation(cursor);
    				list.add(info);
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return list;
    }
    
    public void removeExcessntf() {
    	String where = NotificationColumns.PROCESSED + " = " + 0 ;
    	String sortOrder = NotificationColumns.LAST_MODIFY + " DESC";
    	Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
    			new String[]{NotificationColumns.LAST_MODIFY}, where, null, sortOrder);
    	long lastTime = 0;
    	if(cursor != null) {
    		if(cursor.getCount() > InformationConstant.DEFAULT_NOTIFICATION_COUNT) {
    			cursor.moveToPosition(InformationConstant.DEFAULT_NOTIFICATION_COUNT -1);
    			lastTime = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationColumns.LAST_MODIFY));
    		}
    		cursor.close();
    		cursor = null;
    	}
    	if(QiupuConfig.LOGD ) Log.d(TAG, "get lastTime: " + lastTime);
    	if(lastTime != 0) {
    		String deletewhere = NotificationColumns.LAST_MODIFY + " < " + lastTime + " and " + NotificationColumns.PROCESSED + " = "+ 0;
    		int count = mResolver.delete(NotificationColumns.CONTENT_URI, deletewhere, null);
    		Log.i(TAG, "removeExcessntf delete count : " + count);
    	}else {
    		Log.d(TAG, "no need remove ");
    	}
    }
    
    public void removeExcessntfWithType(boolean isToMe, String sceneId) {
    	String where = null;
    	if(isToMe) {
    		where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.PROCESSED + " = " + 0  + " and " + NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'";
    	}else {
    		where = NotificationColumns.SCENE + " = " + sceneId + " and " + NotificationColumns.PROCESSED + " = " + 0  + " and (" + NotificationColumns.DATA + " is null or " + NotificationColumns.DATA + "='' or " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\')";
    	}
    	String sortOrder = NotificationColumns.LAST_MODIFY + " DESC";
    	Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
    			new String[]{NotificationColumns.LAST_MODIFY}, where, null, sortOrder);
    	long lastTime = 0;
    	if(cursor != null) {
    		if(cursor.getCount() > InformationConstant.NOTIFICATION_DB_MAX_SIZE) {
    			cursor.moveToPosition(InformationConstant.NOTIFICATION_DB_MAX_SIZE -1);
    			lastTime = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationColumns.LAST_MODIFY));
    		}
    		cursor.close();
    		cursor = null;
    	}
    	if(QiupuConfig.LOGD ) Log.d(TAG, "get lastTime: " + lastTime);
    	if(lastTime != 0) {
    		String deletewhere = NotificationColumns.LAST_MODIFY + " < " + lastTime + " and " + NotificationColumns.PROCESSED + " = "+ 0;
    		if(StringUtil.isValidString(where)) {
    			deletewhere = deletewhere + " and " + where;
    		}
    		int count = mResolver.delete(NotificationColumns.CONTENT_URI, deletewhere, null);
    		Log.i(TAG, "removeExcessntfWithType delete count : " + count);
    	}else {
    		Log.d(TAG, "no need remove ");
    	}
    }
    
    public boolean isneedRemove(long arrayListTime) {
    	boolean needremove = false;
    	String where = NotificationColumns.PROCESSED + " = " + 0 ;
    	String sortOrder = NotificationColumns.LAST_MODIFY + " DESC";
    	Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
    			new String[]{NotificationColumns.LAST_MODIFY}, where, null, sortOrder);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			long maxTime = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationColumns.LAST_MODIFY));
    			if(maxTime < arrayListTime) {
                    Log.d(TAG, "isneedRemove, return true while saved latest = " + maxTime +
                            " < get least time = " + arrayListTime);
    				needremove = true;
    			}
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return needremove;
    }
    
    public void clearAllDbNtf() {
    	int count = mResolver.delete(NotificationColumns.CONTENT_URI, null, null);
    	Log.i(TAG, "clear all ntf: " + count);
    }

    // todo : need to set changed read status back to infomation server.
    private void checkCachedReadItem(InformationBase item) {
        if (item.read) {
            if(QiupuConfig.DBLOGD)Log.d(TAG, "checkCachedReadItem, skip with read item = " + item);
            return;
        }

//        if ("ntf.my_stream_comment".equalsIgnoreCase(item.type)) {
        final Uri uri = Uri.parse(item.uri);
        if (null != uri) {
            String param = uri.getQueryParameter("id");
            final String scheme = uri.getScheme();
            if ("borqs".equalsIgnoreCase(scheme) &&
                    (!TextUtils.isEmpty(param)) && TextUtils.isDigitsOnly(param)) {
                final String path = uri.getPath();
                if ("comment".equalsIgnoreCase(path)) {
                    final long id = Long.parseLong(param);
                    if (id > 0) {
                        param = uri.getQueryParameter("comment_id");
                        final long commentId;
                        if (TextUtils.isEmpty(param) || !TextUtils.isDigitsOnly(param)) {
                            commentId = -1;
                        } else {
                            commentId = Long.parseLong(param);
                        }
                        if (InformationReadCache.ReadStreamCache.hitTest(id, commentId)) {
                        	// cache unReadNtf
                        	InformationReadCache.ReadStreamCache.cacheUnReadNtfIdsWithoutDb(item.id);
                            item.read = true;
                        }
                    }
                }
            }
        }

        Log.d(TAG, "checkCachedReadItem, exit item.read = " + item.read);
    }

	public int addTopNWithType(boolean isToMe, ArrayList<InformationBase> items, String sceneId) {
		 HashMap<Long, NotificationObj> result = getExistedItems();
		 Log.d(TAG, "addTopNWithType: " + isToMe + " " + items.size() + " " + result.size());
			long uid = AccountServiceUtils.getBorqsAccountID();
			if(items.size() > 0 && isneedRemove(items.get(items.size() - 1).lastModified)) {
				Log.d(TAG, "addTopNWithType: clear all dbNtf");
				if(isToMe) {
					removeAllTomeNtf(sceneId);
				}else {
					removeAllWithOutTomeNtf(sceneId);
				}
			}
			int handledCount = 0;
			for (InformationBase item : items) {
				checkCachedReadItem(item);
				if (hasRead(item, result)) {
					handledCount++;
				}
				
				if (result.containsKey(item.id)) {
				    updateNtf(item);
				    
				} else {
//	                checkCachedReadItem(item);
					batchOperation.add(ContentProviderOperation
							.newInsert(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY)
							.withValues(buildContentValues(item, uid)).build());
				}
				if (batchOperation.size() >= 100) {
					batchOperation.execute(true);
				}
			}
			batchOperation.execute(true);
			
			removeExcessntfWithType(isToMe, sceneId);  //only save <= 50 ntf
			return items.size() - handledCount;	
	}
    
	@Override
	public int addUnReadWithType(boolean isToMe, ArrayList<InformationBase> list) {
		if(list == null || list.size() <= 0) {
			return 0;
		}
//		if(list != null && list.size() > 0) {
//			if(isToMe) {
//				// remove all tome ntf first;
//				removeAllTomeNtf();
//				
//			}else {
//				removeAllWithOutTomeNtf();
//			}
//		}
		
		HashMap<Long, NotificationObj> result = getExistedItems();
		long uid = AccountServiceUtils.getBorqsAccountID();
//		if(items.size() > 0 && isneedRemove(items.get(items.size() - 1).lastModified)) {
//			clearAllDbNtf();
//		}
		int handledCount = 0;
		StringBuilder selectString = new StringBuilder();
		for (InformationBase item : list) {
			if(selectString.length() > 0) {
				selectString.append(",");
			}
			selectString.append(item.id);
			
			if (result.containsKey(item.id)) {
			    if (hasRead(item, result) == true) {
			        //do nothing, no data change.
			        handledCount++;
			    } else {
                    checkCachedReadItem(item);
			        batchOperation.add(ContentProviderOperation
						.newUpdate(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, item.id))
						.withValues(buildContentValues(item, uid)).build());
			    }
			} else {
                checkCachedReadItem(item);
				batchOperation.add(ContentProviderOperation
						.newInsert(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY)
						.withValues(buildContentValues(item, uid)).build());
			}
			
			if (batchOperation.size() >= 100) {
				batchOperation.execute(true);
			}
		}
		
		removeRedundancyData(isToMe, selectString.toString());
		batchOperation.execute(true);
		return list.size() - handledCount;		
	}
	
	private void removeRedundancyData(boolean isToMe, String filterIds) {
		String deletewhere = NotificationColumns.M_ID + " not in(" + filterIds + ")";
		if(isToMe) {
			deletewhere = deletewhere + " and " +  NotificationColumns.DATA + " is not null and " + NotificationColumns.DATA + " != '' and " + NotificationColumns.DATA + " like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'";
		}else {
			deletewhere = deletewhere + " and " + NotificationColumns.DATA + " not like \'%," + AccountServiceUtils.getBorqsAccountID() + ",%\'";
		}
		int count = mResolver.delete(NotificationColumns.CONTENT_URI, deletewhere, null);
		Log.i(TAG, "removeRedundancyData delete count : " + count);
	}
}
