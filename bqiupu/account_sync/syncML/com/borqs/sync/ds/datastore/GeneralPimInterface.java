package com.borqs.sync.ds.datastore;

import java.util.Hashtable;

import android.content.ContentResolver;
import android.net.Uri;

import com.borqs.sync.client.vdata.PimSyncmlInterface;
import com.borqs.syncml.ds.protocol.IPimInterface;
import com.borqs.syncml.ds.protocol.ISyncItem;

public abstract class GeneralPimInterface implements IPimInterface {
	// static final private String DATA_TYPE = "text/x-vcard";
	private PimSyncmlInterface<Object> pim;
	protected ContentResolver mResolver;
	private String mDataType;
	private String mPrefix;
	private Uri mUri;

	public GeneralPimInterface(ContentResolver resolver,
			PimSyncmlInterface<Object> pim, String dateType, String prefix,
			Uri uri) {
		mResolver = resolver;
		this.pim = pim;
		this.mDataType = dateType;
		mPrefix = prefix;
		mUri = uri;

		// TODO:
		// pim
		// .setNotLoadPhoto(!DeviceInformation.instance()
		// .isSyncContactsPhoto());
	}

	public long add(byte[] data) {
		long id = 0;
		Object contact = pim.parse(new String(data));
		if (contact != null) {
//			if(contact instanceof EventStruct){
//				convertEventTime((EventStruct)contact);
//			}
			//deal with value before save.eg,default,order
			contact = GeneralPimWordsProcess.dealFieldsProcessBeforeSave(mPrefix, contact);
			id = pim.add(contact, mResolver);
		}
		return id;
	}
	
	public boolean update(long key, byte[] data) {
		Object contact = pim.parse(new String(data));
		if (contact != null) {
//			if(contact instanceof EventStruct){
//				convertEventTime((EventStruct)contact);
//			}
			//deal with value before save.eg,default,order
			contact = GeneralPimWordsProcess.dealFieldsProcessBeforeSave(mPrefix, contact);
			return pim.update(key, contact, mResolver);
		} else {
			return false;
		}
	}

	public boolean delete(long key) {
		return pim.delete(key, mResolver);
	}

//	public long getItemHash(long currentId) {
//		long hash = 0;
//		Object contact = pim.load(currentId, mResolver);
//		if (contact != null) {
//			String vcard = pim.create(contact, 0);
//			if (vcard != null) {
//				CRC32 crc = new CRC32();
//				crc.update(vcard.getBytes());
//				hash = crc.getValue();
//			}
//		}
//		return hash;
//	}

	public ISyncItem genAddItem(long id) {
		return addReplaceItem(id, true);
	}

	public ISyncItem genDeleteItem(long id) {
		SyncItem item = new SyncItem(mPrefix + Long.toString(id), null,
				mDataType, ISyncItem.CMD_DELETE, null, 0);
		return item;
	}

	public ISyncItem genUpdateItem(long id) {
		return addReplaceItem(id, false);
	}

	private ISyncItem addReplaceItem(long id, boolean add) {
		long hash = 0;
		byte[] vcard = null;
		Object contact = pim.load(id, mResolver);
		if (contact != null) {
			contact = GeneralPimWordsProcess.dealFieldsProcess(mPrefix, contact);
			String data = pim.create(contact, 0);
			if (data != null) {
				vcard = data.getBytes();
				hash = this.getItemHash(id);
			}
		}
		return new SyncItem(mPrefix + Long.toString(id), null, mDataType,
				add ? ISyncItem.CMD_ADD : ISyncItem.CMD_REPLACE, vcard, hash);
	}

	public Uri getUri() {
		return mUri;
	}
	public void syncBegin() {
		
	}

	public void syncEnd() {
		
	}
	public long[] batchAdd(byte[][] data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean supportBatchAdd() {
		return false;
	}
	
	public boolean supportBatchDelete() {
		return false;
	}
	
	public boolean batchDelete(long[] key){
		return false;
	}
	
	public Hashtable<Long, Long> getItemsHash(long[] ids) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//vCalendar from PIM server.
//	BEGIN:VCALENDAR
//	VERSION:1.0
//	BEGIN:VEVENT
//	PRODID;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Borqs
//	SUMMARY;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Test. Tz
//	CATEGORIES:MISCELLANEOUS
//	STATUS:TENTATIVE
//	DTSTART:20110124T110300
//	DTEND:20110124T120300
//	AALARM:20110124T105300
//	X-BORQS-TZ;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Asia/Tokyo
//	TRANSP:0
//	END:VEVENT
//	END:VCALENDAR
	/**	
	 * convert the eventTime to CST time for PIM Server
	 * @param event
	 */
//	private void convertEventTime(EventStruct event){
//		if(event.getDtStart() != null){
//			event.setDtStart(Util.convertDefaultTzTimeToCST(event.getDtStart()));
//		}
//		if(event.getDtEnd() != null){
//			event.setDtEnd(Util.convertDefaultTzTimeToCST(event.getDtEnd()));
//		}
//		if(event.getDtLast() != null){
//			event.setDtLast(Util.convertDefaultTzTimeToCST(event.getDtLast()));
//		}
//		if(event.getExceptionStartTime() != null){
//			event.setDtStart(Util.convertDefaultTzTimeToCST(event.getExceptionStartTime()));
//		}
//	}
	
}
