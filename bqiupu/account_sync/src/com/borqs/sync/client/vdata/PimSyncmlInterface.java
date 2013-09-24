package com.borqs.sync.client.vdata;


import android.content.ContentResolver;
import android.database.Cursor;

/**
 * Interface between SyncML and other PIM content provider with VCard,VCalendar
 * 
 * @author b059
 * 
 * @param <T> The data structure of a Sync Item
 */
public interface PimSyncmlInterface<T> {
	/**
	 * Parse a string
	 * 
	 * @param s
	 *            the VCard/VCalendar stream
	 * @return the data structure.
	 */
	public T parse(String s);

	/**
	 * Create a VCard/VCalendar string
	 * 
	 * @param d data structure
	 * @param version
	 * @return VCard/VCalendar string
	 */
	public String create(T d, int version);

	/**
	 * Insert a data record
	 * @param d data structure
	 * @param resover Content resolver
	 * @return the record id. if insert error <=0.  
	 */
	public long add(T d, ContentResolver resover);

	/**
	 * Insert a data record with the id
	 * @param d data structure
	 * @param id record id
	 * @param resolver 
	 * @return record id. id <=0 if error.
	 */
	public long add(T d, long id, ContentResolver resolver);

	/**
	 * Update a record with the data structure
	 * @param id record id
	 * @param d data structure
	 * @param resolver
	 * @return true success, false failed.
	 */
	public boolean update(long id, T d, ContentResolver resolver);

	/**
	 * Delete a record
	 * @param id record id
	 * @param resolver
	 * @return true success, false failed.
	 */
	public boolean delete(long id, ContentResolver resolver);

	/**
	 * Load a record data
	 * @param id record id
	 * @param resolver
	 * @return
	 */
	public T load(long id, ContentResolver resolver);

	/**
	 * load a record data
	 * @param cursor cursor that query all columns. In query, the projection is null
	 * @param resolver
	 * @return
	 */
	public T load(Cursor cursor, ContentResolver resolver);

}
