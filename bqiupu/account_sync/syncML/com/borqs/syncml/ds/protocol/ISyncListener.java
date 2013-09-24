package com.borqs.syncml.ds.protocol;

/**
 * Handle the sync process
 * 
 * @author b059
 * 
 */
public interface ISyncListener {
	public static final int PHASE_NONE = 0;
	public static final int PHASE_DATA_CONNECTION = 1;
	public static final int PHASE_INITIALIZATION = 2;
	public static final int PHASE_DEVICE_PREPARE = 3;
	public static final int PHASE_SYNC_DEVICE_TO_SERVER = 4;
	public static final int PHASE_SYNC_SERVER_TO_CLIENT = 5;
	public static final int PHASE_SEND_MAP_TO_SERVER = 6;

	public static final int SERVER_OPERATION_ADD = 0;
	public static final int SERVER_OPERATION_UPDATE = 1;
	public static final int SERVER_OPERATION_DELETE = 2;

	void endSync(int status);

	void setServerNoc(int n);

	void handleServer(int opt);

	void setLocalNoc(int n);

	void handleLocal();

	void pleaseWaiting();

	void checkingLocalData();

	void setPhase(int phase);

	void setDeleteAllCount(int count);

	/**
	 * Deleted one item of Clear data store items
	 */
	void deletedOneItemOfAll();
	
	void deletedItemsOfAll(int items);
	
	/**
	 * Set the size of current data store size
	 * @param itemsListSize Current size
	 */
	void setCurrentDeviceSize(int itemsListSize);

	/**
	 * Analyze one device item
	 */
	void analyzeDeviceItem();
}
