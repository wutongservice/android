package com.borqs.sync.ds.datastore;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import com.borqs.sync.provider.SyncMLDb.Mapping;
import com.borqs.sync.provider.SyncMLDb.SyncRecord;
import com.borqs.sync.service.LocalSyncMLProvider;

import java.util.ArrayList;


public class BatchRecordOperation extends ArrayList<ContentProviderOperation> {
	
	static private final String TAG = "SyncRecordOperations";
    private static final long serialVersionUID = 1L;
    public ContentProviderResult[] mResults = null;

    private ContentResolver mResolver;
    
    public BatchRecordOperation(ContentResolver resolver){
    	mResolver = resolver;
    }

    public void newSyncRecord(long source, long record, long hash) {
//		Builder builder = ContentProviderOperation
//				.newInsert(SyncRecord.CONTENT_URI);
		ContentValues values = new ContentValues();
		values.put(SyncRecord.SOURCE, source);
		values.put(SyncRecord.RECORD, record);
		values.put(SyncRecord.HASH, hash);
//		builder.withValues(values);
//		add(builder.build());
        try{
            LocalSyncMLProvider.insert(SyncRecord.CONTENT_URI, values);
        } finally {
            LocalSyncMLProvider.close();
        }
	}
    
	public void delSyncRecord(long source, long record) {
//		Builder builder = ContentProviderOperation
//				.newDelete(SyncRecord.CONTENT_URI);
		String selectString;
		String[] selectArgs;
		selectString = SyncRecord.SOURCE + "=?" + " AND " + SyncRecord.RECORD
				+ "=?";
		selectArgs = new String[] { Long.toString(source),
				Long.toString(record) };
        try{
            LocalSyncMLProvider.delete(SyncRecord.CONTENT_URI, selectString, selectArgs);
        }finally {
            LocalSyncMLProvider.close();
        }

//		builder.withSelection(selectString, selectArgs);
//		add(builder.build());
	}
	
    public void newMapping(long source, String tgtId, String srcId) {
//		Builder builder = ContentProviderOperation
//				.newInsert(Mapping.CONTENT_URI);
		ContentValues values = new ContentValues();
		values.put(Mapping.TAG_ID, tgtId);
		values.put(Mapping.SRC_ID, srcId);
		values.put(Mapping.SOURCE, source);
//		builder.withValues(values);
//		add(builder.build());

        try{
            LocalSyncMLProvider.insert(Mapping.CONTENT_URI, values);
        }finally {
            LocalSyncMLProvider.close();
        }
	}  
    
  
    public void execute() {
//        try {
//            if (!isEmpty()) {
//                mResults = mResolver.applyBatch(SyncMLDb.AUTHORITY, this);
//            }
//        } catch (RemoteException e) {
//            // There is nothing sensible to be done here
//            Log.e(TAG, "problem inserting contact ", e);
//        } catch (OperationApplicationException e) {
//            // There is nothing sensible to be done here
//            Log.e(TAG, "problem inserting contact", e);
//        }
    }
}
