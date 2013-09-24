package com.borqs.sync.ds.datastore;

import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.protocol.IPimInterface;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.protocol.ISyncLogInterface;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;


public class GeneralPimOperator {
//	public static void oneByOneDeleteAll(
//			ISyncListener syncListener,
//			IProfile profile,
//			ISyncLogInterface syncLog,
//			IPimInterface pim, ContentResolver resolver, Uri uri,
//			String selection, String[] selectionArgs) throws DsException {
//		
//		Cursor cursor = resolver.query(uri, new String[] { BaseColumns._ID },
//				selection, selectionArgs, null);
//		if (cursor != null) {
//			syncListener.setDeleteAllCount(cursor.getCount());
//			while (cursor.moveToNext()) {
//				profile.checkCancelSync();
//				syncListener.deletedOneItemOfAll();
//				syncLog.clientDel(pim.delete(cursor.getLong(0)));
//			}
//			cursor.close();
//		}
//	}

	public static long[] getCurrentItems(Uri uri, ContentResolver resolver,
			String selection, String[] selectionArgs, ISyncListener syncListener) {
		long currentItems[] = new long[0];
		Cursor cursor = resolver.query(uri, new String[] { BaseColumns._ID },
				selection, selectionArgs, BaseColumns._ID);
		if (cursor != null) {
			try {
				int itemsListSize = cursor.getCount();
				syncListener.setCurrentDeviceSize(itemsListSize);
				if (itemsListSize > 0) {
					currentItems = new long[itemsListSize];
					int i = 0;
					if (cursor.moveToFirst()) {
						do {
							currentItems[i++] = cursor.getLong(0);
						} while (cursor.moveToNext());
					}
				}
			} finally {
				cursor.close();
			}
		}
		return currentItems;
	}
}
