/**
 * Borqs 2008
 * 
 *
 */

package com.borqs.sync.service;

public class Define {
	// name for intent
	final static public String EXTRA_NAME_SYNC_ITEM = "oms.android.syncml.items";
	final static public String EXTRA_NAME_SYNC_MODE = "oms.android.syncml.mode";
	final static public String EXTRA_NAME_PROFILE = "oms.android.syncml.profile";

	// Do not change the number
	public static final int SYNC_ITEMS_INT_CONTACTS = 0;
	public static final int SYNC_ITEMS_INT_CALENDAR = 1;
	public static final int SYNC_ITEMS_INT_TASK = 2;
	public static final int SYNC_ITEMS_INT_TOTAL = 3;

	// Sync ML status
	public static final int SYNC_STATUS_IDLE = 0;
	public static final int SYNC_STATUS_DATA_CONNECTING = 1;
	public static final int SYNC_STATUS_SYNCING = 2;
	
	//Sync item status
	public static final int SYNC_ITEM_STATUS_IDLE = 0;
	public static final int SYNC_ITEM_STATUS_WAITING_SYNC = 1;
	public static final int SYNC_ITEM_STATUS_SYNCING = 2;
	public static final int SYNC_ITEM_STATUS_SYNC_END_WITH_ERROR = 3;
	public static final int SYNC_ITEM_STATUS_SYNC_END_SUCCESSFULLY = 4;
	public static final int SYNC_ITEM_STATUS_SYNC_INTERRUPTED = 5;
}
