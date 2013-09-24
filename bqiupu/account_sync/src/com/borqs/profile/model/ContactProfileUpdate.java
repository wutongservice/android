package com.borqs.profile.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;

class ContactProfileUpdate {
	static final private String[] PEOPLE_PROJECTS = { 
		Profile.CUSTOM_RINGTONE
			};
	private static final int COLUMN_INDEX_RINGTONE = 0;
	
	// data flag
	private static final int DATA_FLAG_NAME = 0;
//	private static final int DATA_FLAG_URL = 1;
	private static final int DATA_FLAG_NOTE = 2;
	private static final int DATA_FLAG_PHOTO = 3;
	private static final int DATA_FLAG_NICKNAME = 4;
	private static final int DATA_FLAG_BIRTHDAY = 5;

	static final int compareString(String left, String right) {
		if (TextUtils.isEmpty(left) && TextUtils.isEmpty(right)) {
			return 0;
		}
		if (TextUtils.isEmpty(left) && !TextUtils.isEmpty(right)) {
			return -1;
		}
		if (!TextUtils.isEmpty(left) && TextUtils.isEmpty(right)) {
			return 1;
		}
		return left.compareTo(right);
	}

	static void putUpdateValue(ContentValues cv, String key, String value) {
		cv.put(key, value);
	}

	static private boolean updatePeople(long people, ContactProfileStruct contact,
			ContentResolver resolver, ContactProfileProviderOperation cpo) {
		boolean retValue = false;

		Uri peopleUri = ContentUris.withAppendedId(Profile.CONTENT_RAW_CONTACTS_URI,
				people);

		Cursor contactC = resolver.query(peopleUri, PEOPLE_PROJECTS, null,
				null, null);

		int x = contactC.getCount();
		
		if (contactC != null) {
		    try{
		        if (contactC.moveToFirst()) {
	                ContentValues cv = new ContentValues();
	                
	                //disable the contact join in
	                cv.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
	                if (cv.size() > 0) {
	                    cpo.newUpdate(people, cv);
	                    
	                }
	                retValue = true;
	            }
		    }finally{
		        contactC.close();
		    }
			
		}
		return retValue;
	}

	static final private String[] DATA_PROJECTS = {Data._ID, 
													Data.MIMETYPE,
													Data.IS_SUPER_PRIMARY,
													Data.DATA1,
													Data.DATA2,
													Data.DATA3,
													Data.DATA4,
													Data.DATA5,
													Data.DATA6,
													Data.DATA7,
													Data.DATA8,
													Data.DATA9,
													Data.DATA10,
													Data.DATA14,
													Data.DATA15};


	static private void updateData(long people, ContactProfileStruct contact,
			ContentResolver resolver, ContactProfileProviderOperation cpo){
		Uri uri = Uri.withAppendedPath(ContentUris.withAppendedId(
				Profile.CONTENT_RAW_CONTACTS_URI, people),
				RawContacts.Data.CONTENT_DIRECTORY);
		// Get device data
		Cursor cursor = resolver.query(uri, DATA_PROJECTS, null, null, 
						Data.DATA2 + " ASC," + Data.DATA1
						+ " ASC");
		List<DeviceNameData> deviceNameItems = null;
		List<DevicePhotoData> devicePhotoItems = null;
		List<DeviceCommonData> deviceNicNameItems = null;
		List<DevicePhoneData> devicePhoneItems = null;
		List<DeviceCommonListData> deviceEmailItems = null;
		if (cursor != null) {
			String mimetype = null;
			while (cursor.moveToNext()) {
				mimetype = 
					cursor.getString(cursor.getColumnIndexOrThrow(Data.MIMETYPE));
				if(StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceNameItems == null){
						deviceNameItems = new LinkedList<DeviceNameData>();
					}
					DeviceNameData data = new DeviceNameData(
							cursor.getLong(cursor.getColumnIndexOrThrow(StructuredName._ID)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.FAMILY_NAME)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.GIVEN_NAME)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.MIDDLE_NAME)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.PREFIX)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.SUFFIX)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.PHONETIC_FAMILY_NAME)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.PHONETIC_GIVEN_NAME)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.PHONETIC_MIDDLE_NAME))
							);
					deviceNameItems.add(data);
				} else if(Phone.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(devicePhoneItems == null){
						devicePhoneItems = new LinkedList<DevicePhoneData>();
					}
					DevicePhoneData data = new DevicePhoneData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Phone._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(Phone.TYPE)), 
							cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER)),
							cursor.getString(cursor.getColumnIndexOrThrow(Phone.LABEL)),
							"1".equals(cursor.getString(cursor.getColumnIndexOrThrow(Phone.IS_SUPER_PRIMARY))));
					devicePhoneItems.add(data);
				} else if(Email.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceEmailItems == null){
						deviceEmailItems = new LinkedList<DeviceCommonListData>();
					}
					DeviceCommonListData data = new DeviceCommonListData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Email._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(Email.TYPE)),
							cursor.getString(cursor.getColumnIndexOrThrow(Email.DATA)),
							cursor.getString(cursor.getColumnIndexOrThrow(Email.LABEL))
							);
					deviceEmailItems.add(data);
				} else if(Photo.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(devicePhotoItems == null){
						devicePhotoItems = new LinkedList<DevicePhotoData>();
					}
					DevicePhotoData data = new DevicePhotoData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Photo._ID)),
							cursor.getBlob(cursor.getColumnIndexOrThrow(Photo.PHOTO))
							);
					devicePhotoItems.add(data);
				} else if(Nickname.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceNicNameItems == null){
						deviceNicNameItems = new LinkedList<DeviceCommonData>();
					}
					DeviceCommonData data = new DeviceCommonData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Nickname._ID)),
							cursor.getString(cursor.getColumnIndexOrThrow(Nickname.NAME))
							);
					deviceNicNameItems.add(data);
				}
				
			}
			cursor.close();
			
		}
		updatePhones(people, contact, cpo, devicePhoneItems);
		updateName(people, contact, cpo, deviceNameItems);
		updatePhoto(people, contact, cpo, devicePhotoItems);
		updateCommonData(people, contact, cpo, deviceNicNameItems, DATA_FLAG_NICKNAME);
		updateEmailData(people, contact, cpo, deviceEmailItems);
	}
	
	static private void deleteData(long id, ContactProfileProviderOperation cpo) {
		cpo.deleteData(id);
	}
	
	// phone
	private static class DevicePhoneData {
		DevicePhoneData(long id, int type, String number, String label, boolean isPrimary) {
			this.id = id;
			this.type = type;
			this.number = number;
			this.label = label;
			this.isPrimary = isPrimary;
		}
		
		long id;
		String number;
		int type;
		String label;
		boolean isPrimary;
	}
	static private void updatePhones(long people, ContactProfileStruct contact,
								ContactProfileProviderOperation cpo, List<DevicePhoneData> deviceItems) {

		// Get server data
		List<PhoneData> serverItems = new LinkedList<PhoneData>();
		List<PhoneData> tmpList = contact.getPhoneList();
		List<PhoneData> needAddPhones = new LinkedList<PhoneData>();

		if (tmpList != null) {
			for (PhoneData item : tmpList) {
				serverItems.add(item);
			}
		}
		//sort the deviceItems by type
		Collections.sort(serverItems, new Comparator<PhoneData>() {
			public int compare(PhoneData left, PhoneData right) {
				if (left.type == right.type) {
					return compareString(left.data, right.data);
				} else {
					return left.type - right.type;
				}
			}
		});
		if(deviceItems == null){
			deviceItems = new LinkedList<DevicePhoneData>();
		}
		//sort the deviceItems by type
		int phoneType[] = new int[] { Phone.TYPE_HOME, Phone.TYPE_MOBILE,
				Phone.TYPE_WORK, Phone.TYPE_FAX_WORK, Phone.TYPE_FAX_HOME,
				Phone.TYPE_PAGER, Phone.TYPE_OTHER, Phone.TYPE_CALLBACK,
				Phone.TYPE_CAR, Phone.TYPE_COMPANY_MAIN, Phone.TYPE_ISDN,
				Phone.TYPE_MAIN, Phone.TYPE_OTHER_FAX, Phone.TYPE_RADIO,
				Phone.TYPE_TELEX, Phone.TYPE_TTY_TDD, Phone.TYPE_WORK_MOBILE,
				Phone.TYPE_WORK_PAGER, Phone.TYPE_ASSISTANT, Phone.TYPE_MMS,Phone.TYPE_CUSTOM};
		List<DevicePhoneData> deviceItemsTmp = new LinkedList<DevicePhoneData>();
		for (int type : phoneType) {
			for (DevicePhoneData phoneData : deviceItems) {
				if(type == phoneData.type){
					deviceItemsTmp.add(phoneData);
				}
			}
		}
		Iterator<PhoneData> currentItems = serverItems.iterator();

		PhoneData currentId = null;
		DevicePhoneData previousId = null;
		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;

		Iterator<DevicePhoneData> previousItems = deviceItemsTmp.iterator();

		do {
			if (curMoveFlag) {
				if (currentItems != null && currentItems.hasNext()) {
					currentId = currentItems.next();
				} else {
					currentId = null; // no item remains
				}
				curMoveFlag = false;
			}

			if (prevMoveFlag) {
				if (previousItems != null && previousItems.hasNext()) {
					previousId = previousItems.next();
				} else {
					previousId = null;
				}
			}

			// terminal condition
			if (currentId == null && previousId == null) {
				break;
			}
			int comValue = comparePhone(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddPhones.add(currentId);
				// do not move previousItems
				curMoveFlag = true;
				prevMoveFlag = false;
			} else {
				// Delete item
				deleteData(previousId.id, cpo);
				prevMoveFlag = true;
				curMoveFlag = false;
			}
		} while (true);

		if (needAddPhones.size() > 0) {
			addPhones(needAddPhones, people, cpo);
		}
	}

	static int comparePhone(PhoneData currentId, DevicePhoneData previousId) {
		if (currentId == null && previousId == null) {
			return 0;
		}

		if (currentId == null && previousId != null) {
			return 1;
		}

		if (currentId != null && previousId == null) {
			return -1;
		}

		if (currentId.type != previousId.type) {
			return currentId.type - previousId.type;
		}
		int value = compareString(currentId.data, previousId.number);
		if (value != 0) {
			return value;
		}
		return 0;
	}

	static private void addPhones(List<PhoneData> phones, long people,
			ContactProfileProviderOperation cpo) {
		for (ContactProfileStruct.PhoneData data : phones) {
			ContentValues values = new ContentValues();
			values.put(Phone.RAW_CONTACT_ID, people);
			values.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			values.put(Phone.TYPE, data.type);
			values.put(Phone.NUMBER, data.data);
			values.put(Phone.LABEL, data.label);
			values.put(Phone.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			cpo.newData(values);
		}
	}

	// phone
	private static class DeviceNameData {
		DeviceNameData(long id,
				String familyName,
				String givenName,
				String middleName,
				String prefix,
				String suffix,
				String phoneticFamilyName,
				String phoneticGivenName,
				String phoneticMiddleName) {
			this.id = id;
			this.familyName = familyName;
			this.givenName = givenName;
			this.middleName = middleName;
			this.prefix = prefix;
			this.suffix = suffix;
			this.phoneticFamilyName = phoneticFamilyName;
			this.phoneticGivenName = phoneticGivenName;
			this.phoneticMiddleName = phoneticMiddleName;
		}
		
		long id;
		String familyName;
		String givenName;
		String middleName;
		String prefix;
		String suffix;
		String phoneticFamilyName;
		String phoneticGivenName;
		String phoneticMiddleName;
	}
	
	static private void updateName(long people, ContactProfileStruct contact,
								ContactProfileProviderOperation cpo, List<DeviceNameData> deviceItems) { 
		DeviceNameData deviceItem = null;
		if(deviceItems != null && deviceItems.size() > 0){
			deviceItem = deviceItems.get(0);
		}
		long id = 0;
		if(deviceItem != null){
			id = deviceItem.id;
		}
		DeviceNameData serverItem = new DeviceNameData(id, contact.getFamilyName(),
													contact.getGivenName(),
													contact.getMiddleName(),
													contact.getPrefix(),
													contact.getSuffix(),
													contact.getPhoneticFamilyName(),
													contact.getPhoneticGivenName(),
													contact.getPhoneticMiddleName());
		//compare 
		int comValue = compareName(serverItem, deviceItem);
		if (comValue == 0) {
			return;
		} else if (comValue < 0) {
			// delete
			deleteData(deviceItem.id, cpo);
		} else {
			// Delete item
			if(deviceItem != null){
				deleteData(deviceItem.id, cpo);
			}
			addNameInfo(contact, cpo, people);
		}
	}
	
	private static class DevicePhotoData {
		DevicePhotoData(long id, byte[] photoBytes) {
			this.id = id;
			this.photoBytes = photoBytes;
		}
		
		long id;
		byte[] photoBytes;
	}
	
	static private void updatePhoto(long people, ContactProfileStruct contact,
			ContactProfileProviderOperation cpo, List<DevicePhotoData> deviceItems) { 
		DevicePhotoData deviceItem = null;
		if(deviceItems != null && deviceItems.size() > 0){
			deviceItem = deviceItems.get(0);
		}
		long id = 0;
		if(deviceItem != null){
			id = deviceItem.id;
		}
		//DevicePhotoData serverItem = new DevicePhotoData(id, contact.getPhotoList().get(0).photoBytes);
		
		if(deviceItem != null){
			deleteData(deviceItem.id, cpo);
		}
		addPhotoInfo(contact, cpo, people);
	}
	
	static final int compareName(String left, String right) {
		if (TextUtils.isEmpty(left) && TextUtils.isEmpty(right)) {
			return 0;
		}
		if (TextUtils.isEmpty(left) && !TextUtils.isEmpty(right)) {
			return 1;
		}
		if (!TextUtils.isEmpty(left) && TextUtils.isEmpty(right)) {
			return 1;
		}
		if(left.equals(right)){
			return 0;
		}else{
			return 1;
		}
		
	}
	
	static int compareName(DeviceNameData serverItem, DeviceNameData deviceItem) {
		if (serverItem == null && deviceItem == null) {
			//do nothing
			return 0;
		}
		
		if (serverItem == null && deviceItem != null) {
			//delete device item.
			return -1;
		}
		
		if (serverItem != null && deviceItem == null) {
			//add into device
			return 1;
		}
		int value = compareName(serverItem.familyName, deviceItem.familyName);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.givenName, deviceItem.givenName);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.middleName, deviceItem.middleName);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.prefix, deviceItem.prefix);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.suffix, deviceItem.suffix);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.phoneticFamilyName, deviceItem.phoneticFamilyName);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.phoneticGivenName, deviceItem.phoneticGivenName);
		if(value != 0){
			return value;
		}
		value = compareName(serverItem.phoneticMiddleName, deviceItem.phoneticMiddleName);
		if(value != 0){
			return value;
		}
		return 0;
	}
	
	static void addNameInfo(ContactProfileStruct contact, ContactProfileProviderOperation cpo, long people){
		ContentValues values = new ContentValues();
		
		values.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.RAW_CONTACT_ID, people);
		values.put(StructuredName.GIVEN_NAME, contact.getGivenName());
		values.put(StructuredName.FAMILY_NAME, contact.getFamilyName());
		values.put(StructuredName.MIDDLE_NAME, contact.getMiddleName());
		values.put(StructuredName.SUFFIX, contact.getSuffix());
		values.put(StructuredName.PHONETIC_GIVEN_NAME, contact.getPhoneticGivenName());
		values.put(StructuredName.PHONETIC_FAMILY_NAME, contact.getPhoneticFamilyName());
		values.put(StructuredName.PHONETIC_MIDDLE_NAME, contact.getPhoneticMiddleName());
		values.put(StructuredName.PREFIX, contact.getPrefix());
		cpo.newData(values);
	}
	
	static void addPhotoInfo(ContactProfileStruct contact, ContactProfileProviderOperation cpo, long people){
		List<PhotoData> photoList = contact.getPhotoList();
		if(photoList == null || photoList.size() <= 0){
			return ;
		}
		ContentValues values = new ContentValues();
		values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
		values.put(Photo.RAW_CONTACT_ID, people);
		values.put(Photo.PHOTO, photoList.get(0).photoBytes);
		cpo.newData(values);
	}
	
	// name, note
	private static class DeviceCommonData {
		DeviceCommonData(long id, String data) {
			this.id = id;
			this.data = data;
		}
		
		long id;
		String data;
	}
	
	static private void updateCommonData(long people, ContactProfileStruct contact,
								ContactProfileProviderOperation cpo, List<DeviceCommonData> deviceItems,
								int dataFlag) {
		// Get server data
		String serverItems = null;
		switch(dataFlag){
			case DATA_FLAG_NICKNAME:
				serverItems = contact.getNickName();
				break;
		}
		long deviceId = 0; 
		String deviceValue = null;
		if(deviceItems != null){
			Iterator<DeviceCommonData> device = deviceItems.iterator();
			if(device.hasNext()){
				DeviceCommonData data = device.next();
				deviceId = data.id;
				deviceValue = data.data;
			}
		}
		
		if(deviceValue == null && serverItems == null){
			return;
		} else if(deviceValue == null && serverItems != null){
			addCommonData(people, serverItems, dataFlag, cpo);
		} else if(deviceValue != null && serverItems == null){
			deleteData(deviceId, cpo);
		} else if(deviceValue != null && serverItems != null){
			deleteData(deviceId, cpo);
			addCommonData(people, serverItems, dataFlag, cpo);
		}
		
	}
	
	static private void addCommonData(long people, String value, int dataFlag,
			ContactProfileProviderOperation cpo) {
		ContentValues values = new ContentValues();
		switch(dataFlag){
			// photo
			case DATA_FLAG_PHOTO:
				values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
				values.put(Photo.RAW_CONTACT_ID, people);
				values.put(Photo.PHOTO, value.getBytes());
				break;
			case DATA_FLAG_NICKNAME:
				values.put(Nickname.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
				values.put(Nickname.RAW_CONTACT_ID, people);
				values.put(Nickname.TYPE, Nickname.TYPE_DEFAULT);
				values.put(Nickname.NAME, value);
				break;
		}
		cpo.newData(values);
	}
	
	// email, url
	private static class DeviceCommonListData {
		DeviceCommonListData(long id, int type, String data, String label) {
			this.id = id;
			this.type = type;
			this.data = data;
			this.label = label;
		}

		long id;
		int type;
		String data;
		String label;
	}
	
	static private void updateEmailData(long people, ContactProfileStruct contact,
			ContactProfileProviderOperation cpo, List<DeviceCommonListData> deviceItems) {

		// Get server data
		List<EmailData> serverItems = new LinkedList<EmailData>();
		List<EmailData> tmpList = contact.getEmailList();
		List<EmailData> needAddEmail = new LinkedList<EmailData>();

		if (tmpList != null) {
			for (EmailData item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<EmailData>() {
			public int compare(EmailData left, EmailData right) {
				return compareEmail(left, right);
			}
		});

		Iterator<EmailData> currentItems = serverItems.iterator();

		EmailData currentId = null;
		DeviceCommonListData previousId = null;
		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;
		if(deviceItems == null){
			deviceItems = new LinkedList<DeviceCommonListData>();
		}
		Iterator<DeviceCommonListData> previousItems = deviceItems.iterator();

		do {
			if (curMoveFlag) {
				if (currentItems != null && currentItems.hasNext()) {
					currentId = currentItems.next();
				} else {
					currentId = null; // no item remains
				}
				curMoveFlag = false;
			}

			if (prevMoveFlag) {
				if (previousItems != null && previousItems.hasNext()) {
					previousId = previousItems.next();
				} else {
					previousId = null;
				}
			}

			// terminal condition
			if (currentId == null && previousId == null) {
				break;
			}

			int comValue = compareEmail(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddEmail.add(currentId);
				// do not move previousItems
				curMoveFlag = true;
				prevMoveFlag = false;
			} else {
				// Delete item
				deleteData(previousId.id, cpo);
				prevMoveFlag = true;
				curMoveFlag = false;
			}
		} while (true);

		if (needAddEmail.size() > 0) {
			addEmail(needAddEmail, people, cpo);
		}
	}

	static private void addEmail(List<EmailData> email, long people,
			ContactProfileProviderOperation cpo) {
		for (ContactProfileStruct.EmailData data : email) {	
			String email_data;
	        String displayName;
			Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(data.data);
	        // Can't happen, but belt & suspenders
	        if (tokens.length == 0) {
	        	email_data = "";
	            displayName = "";
	        } else {
	            Rfc822Token token = tokens[0];
	            email_data = token.getAddress();
	            displayName = token.getName();
	        }
			ContentValues values = new ContentValues();
			values.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
			values.put(Email.RAW_CONTACT_ID, people);
			values.put(Email.TYPE, data.type);
			values.put(Email.DATA, email_data);
			values.put(Email.DISPLAY_NAME, displayName);
			values.put(Email.LABEL, data.label);
			values.put(Email.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			cpo.newData(values);
		}
		
	}

	static int compareEmail(EmailData left, EmailData right) {
		if (left.type != right.type) {
			return left.type - right.type;
		}
		int value = compareString(left.data, right.data);
		if (value != 0) {
			return value;
		}

		return 0;
	}

	static int compareEmail(EmailData currentId, DeviceCommonListData previousId) {
		if (currentId == null && previousId == null) {
			return 0;
		}

		if (currentId == null && previousId != null) {
			return 1;
		}

		if (currentId != null && previousId == null) {
			return -1;
		}

		if (currentId.type != previousId.type) {
			return currentId.type - previousId.type;
		}
		int value = compareString(currentId.data, previousId.data);
		if (value != 0) {
			return value;
		}

		return 0;
	}
	
	static public boolean update(long people, ContactProfileStruct contact,
			ContentResolver resolver) {
//		contact.consolidateFields();
		ContactProfileProviderOperation cpo = new ContactProfileProviderOperation(resolver);
		boolean retValue = updatePeople(people, contact, resolver, cpo);
		if(retValue){
			updateData(people, contact, resolver, cpo);
		}		

		cpo.execute();
		return retValue;
	}

	public static boolean updatePeopleNoCommit(long people, ContactProfileStruct contact,
			ContentResolver resolver, ContactProfileProviderOperation cpo) {
		boolean retValue = updatePeople(people, contact, resolver, cpo);
		if(retValue){
			updateData(people, contact, resolver, cpo);
		}	
		return retValue;
	}
	
}
