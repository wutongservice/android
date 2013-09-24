package com.borqs.sync.client.vdata.card;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import com.borqs.common.account.AccountAdapter;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.config.ProfileConfig;
import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.ImData;
import com.borqs.sync.client.vdata.card.ContactStruct.OrganizationData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;
import com.borqs.sync.client.vdata.card.ContactStruct.PostalData;
import com.borqs.sync.client.vdata.card.ContactStruct.WebsiteData;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class ContactUpdate {
	static final private String[] PEOPLE_PROJECTS = { 
//			ContactsExt.PeopleExt.BIRTHDAY,
//			ContactsExt.PeopleExt.BLOCK,
//			RawContacts.CUSTOM_RINGTONE,
//			"hasfetion"
		RawContacts.CUSTOM_RINGTONE
			};
//	private static final int COLUMN_INDEX_BIRTHDAY = 0;
//	private static final int COLUMN_INDEX_BLOCK = 1;
//	private static final int COLUMN_INDEX_RINGTONE = 2;
//	private static final int COLUMN_INDEX_HASFETION = 3;
	private static final int COLUMN_INDEX_RINGTONE = 0;
	
	// data flag
	private static final int DATA_FLAG_NAME = 0;
//	private static final int DATA_FLAG_URL = 1;
	private static final int DATA_FLAG_NOTE = 2;
	private static final int DATA_FLAG_PHOTO = 3;
	private static final int DATA_FLAG_NICKNAME = 4;
	private static final int DATA_FLAG_BIRTHDAY = 5;

	private static final String COLUMN_BORQS_NAME_SUMMARY = "data2";
	private static final String COLUMN_BORQS_NAME = "data3";
	
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

	static private boolean updatePeople(long people, ContactStruct contact,
			ContentResolver resolver, ContactProviderOperation cpo) {
		boolean retValue = false;

		Uri peopleUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
				people);

		Cursor contactC = resolver.query(peopleUri, PEOPLE_PROJECTS, null,
				null, null);

		if (contactC != null) {
		    try{
		        if (contactC.moveToFirst()) {
	                ContentValues cv = new ContentValues();
	                
	                String value;
	                //disable the contact join in
	                cv.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
	                cv.put(RawContacts.SYNC4, TextUtils.isEmpty(contact.getBorqsUid())?"":contact.getBorqsUid());
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


	static private void updateData(long people, ContactStruct contact,
			ContentResolver resolver, ContactProviderOperation cpo){
		Uri uri = Uri.withAppendedPath(ContentUris.withAppendedId(
				RawContacts.CONTENT_URI, people),
				RawContacts.Data.CONTENT_DIRECTORY);
		// Get device data
		Cursor cursor = resolver.query(uri, DATA_PROJECTS, null, null, 
						Data.DATA2 + " ASC," + Data.DATA1
						+ " ASC");
		List<DeviceNameData> deviceNameItems = null;
		List<DeviceCommonListData> deviceWebsiteItems = null;
		List<DeviceCommonData> deviceNoteItems = null;
		List<DevicePhotoData> devicePhotoItems = null;
		List<DeviceCommonData> deviceBirthdayItems = null;
		List<DeviceCommonData> deviceNicNameItems = null;
		List<DevicePhoneData> devicePhoneItems = null;
		List<DeviceOrgData> deviceOrgItems = null;
		List<DeviceCommonListData> deviceEmailItems = null;
		List<DeviceCommonListData> deviceIMItems = null;
		List<DevicePostalData> devicePostalItems = null;
		List<DeviceGroupData> deviceGroupItems = null;
		
		DeviceBorqsName deviceBorqsName = null;
		List<DeviceBorqsPlusData> deviceBorqsPlusItems = null;
		
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
				} else if(Website.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceWebsiteItems == null){
						deviceWebsiteItems = new LinkedList<DeviceCommonListData>();
					}
					DeviceCommonListData data = new DeviceCommonListData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Website._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(Website.TYPE)),
							cursor.getString(cursor.getColumnIndexOrThrow(Website.URL)),
							cursor.getString(cursor.getColumnIndexOrThrow(Website.LABEL))
							);
					deviceWebsiteItems.add(data);
				} else if(Note.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceNoteItems == null){
						deviceNoteItems = new LinkedList<DeviceCommonData>();
					}
					DeviceCommonData data = new DeviceCommonData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Note._ID)),
							cursor.getString(cursor.getColumnIndexOrThrow(Note.NOTE))
							);
					deviceNoteItems.add(data);
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
				} else if(Organization.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceOrgItems == null){
						deviceOrgItems = new LinkedList<DeviceOrgData>();
					}
					DeviceOrgData data = new DeviceOrgData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Organization._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(Organization.TYPE)),
							cursor.getString(cursor.getColumnIndexOrThrow(Organization.COMPANY)),
							cursor.getString(cursor.getColumnIndexOrThrow(Organization.TITLE)),
							cursor.getString(cursor.getColumnIndexOrThrow(Organization.DEPARTMENT)),
							cursor.getString(cursor.getColumnIndexOrThrow(Organization.LABEL))
							);
					deviceOrgItems.add(data);
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
				} else if(Im.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceIMItems == null){
						deviceIMItems = new LinkedList<DeviceCommonListData>();
					}
					DeviceCommonListData data = new DeviceCommonListData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Im._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(Im.TYPE)),
							cursor.getString(cursor.getColumnIndexOrThrow(Im.DATA)),
							cursor.getString(cursor.getColumnIndexOrThrow(Im.LABEL))
							);
					deviceIMItems.add(data);
				} else if(StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(devicePostalItems ==  null){
						devicePostalItems = new LinkedList<DevicePostalData>();
					}
					DevicePostalData data = new DevicePostalData(
							cursor.getLong(cursor.getColumnIndexOrThrow(StructuredPostal._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(StructuredPostal.TYPE)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.POBOX)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.NEIGHBORHOOD)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.STREET)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.CITY)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.REGION)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.POSTCODE)),
							cursor.getString(cursor.getColumnIndexOrThrow(StructuredPostal.POSTCODE))
							);
					devicePostalItems.add(data);
				} else if(Photo.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(devicePhotoItems == null){
						devicePhotoItems = new LinkedList<DevicePhotoData>();
					}
					DevicePhotoData data = new DevicePhotoData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Photo._ID)),
							cursor.getBlob(cursor.getColumnIndexOrThrow(Photo.PHOTO))
							);
					devicePhotoItems.add(data);
				} else if (ContactProviderOperation.MIME_TYPE_BORQS_NAME.equals(mimetype)) {
					deviceBorqsName = new DeviceBorqsName(
							cursor.getLong(cursor.getColumnIndexOrThrow(Photo._ID)),
							cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORQS_NAME)));
					// private static final String COLUMN_BORQS_NAME_SUMMARY = "data2";
					// private static final String COLUMN_BORQS_NAME = "data3";
					// private static final String MIME_TYPE_BORQS_NAME = "vnd.android.cursor.item/borqsname";
					
				} else if(Nickname.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceNicNameItems == null){
						deviceNicNameItems = new LinkedList<DeviceCommonData>();
					}
					DeviceCommonData data = new DeviceCommonData(
							cursor.getLong(cursor.getColumnIndexOrThrow(Nickname._ID)),
							cursor.getString(cursor.getColumnIndexOrThrow(Nickname.NAME))
							);
					deviceNicNameItems.add(data);
				} else if(Event.CONTENT_ITEM_TYPE.equals(mimetype)){
				    String type = cursor.getString(cursor.getColumnIndexOrThrow(CommonDataKinds.Event.TYPE));
                    if(!TextUtils.isEmpty(type) && CommonDataKinds.Event.TYPE_BIRTHDAY == Integer.parseInt(type)){
                        if(deviceBirthdayItems == null){
                            deviceBirthdayItems = new LinkedList<DeviceCommonData>();
                        }
                        DeviceCommonData data = new DeviceCommonData(
                                cursor.getLong(cursor.getColumnIndexOrThrow(Event._ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(Event.START_DATE))
                                );
                        deviceBirthdayItems.add(data);
                    }
				}else if(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE.equals(mimetype)){
					if(deviceGroupItems == null){
						deviceGroupItems = new LinkedList<DeviceGroupData>();
					}
					DeviceGroupData data = new DeviceGroupData(
							cursor.getLong(cursor.getColumnIndexOrThrow(CommonDataKinds.GroupMembership._ID)),
							cursor.getInt(cursor.getColumnIndexOrThrow(CommonDataKinds.GroupMembership.GROUP_ROW_ID))
							);
					deviceGroupItems.add(data);
				}else if(ContactProviderOperation.MIME_TYPE_BORQS_PLUS.equals(mimetype)){
				    if(deviceBorqsPlusItems == null){
				        deviceBorqsPlusItems = new LinkedList<DeviceBorqsPlusData>();
				    }
				    String borqsName =  cursor.getString(cursor.getColumnIndexOrThrow(ContactProviderOperation.COLUMN_BORQS_NAME));
				    borqsName = borqsName.replace(ContactProviderOperation.TEXT_BORQS_NAME_SUMMARY,"");
				    DeviceBorqsPlusData data = new DeviceBorqsPlusData(
				            cursor.getLong(cursor.getColumnIndexOrThrow(Data._ID)),
				            cursor.getString(cursor.getColumnIndexOrThrow(ContactProviderOperation.COLUMN_BORQS_NAME_SUMMARY)),
				            borqsName,
				            cursor.getString(cursor.getColumnIndexOrThrow(ContactProviderOperation.COLUMN_ACTION))
				            );
				    deviceBorqsPlusItems.add(data);
				}
				
			}
			cursor.close();
			
		}
		updatePhones(people, contact, cpo, devicePhoneItems);
		updateName(people, contact, cpo, deviceNameItems);
//		updateCommonData(people, contact, cpo, deviceWebsiteItems, DATA_FLAG_URL);
		updateCommonData(people, contact, cpo, deviceNoteItems, DATA_FLAG_NOTE);
		//updateCommonData(people, contact, cpo, devicePhotoItems, DATA_FLAG_PHOTO);
		updatePhoto(people, contact, cpo, devicePhotoItems);
		updateBorqsName(people, contact, cpo, deviceBorqsName);
		updateCommonData(people, contact, cpo, deviceNicNameItems, DATA_FLAG_NICKNAME);
		updateCommonData(people, contact, cpo, deviceBirthdayItems, DATA_FLAG_BIRTHDAY);
		updateOrgs(people, contact, cpo, deviceOrgItems);
		updateEmailData(people, contact, cpo, deviceEmailItems);
		updateImData(people, contact, cpo, deviceIMItems);
		updatePostalData(people, contact, cpo, devicePostalItems);
		updateUrlData(people, contact, cpo, deviceWebsiteItems);
		updateGroupData(people, contact, cpo, deviceGroupItems,resolver);
//		updateBorqsPlus(people,contact,cpo,deviceBorqsPlusItems,resolver);
	}
	
	private static class DeviceBorqsPlusData {
	    DeviceBorqsPlusData(long id,String plusName,String plusNameSummary,String plusAction) {
            this.id = id;
            this.plusName = plusName;
            this.plusNameSummary = plusNameSummary;
            this.plusAction = plusAction;
        }
        
        long id;
        String plusName;
        String plusNameSummary;
        String plusAction;
    }
	
	static private void updateBorqsPlus(long people,ContactStruct contact,ContactProviderOperation cpo,
	        List<DeviceBorqsPlusData> plusItems,ContentResolver resolver){
        if(!TextUtils.isEmpty(contact.getBorqsUid()) && (plusItems == null || plusItems.size() == 0)){
            addBorqsPlusData(people,cpo);
        }else if(TextUtils.isEmpty(contact.getBorqsUid()) && (plusItems !=null && plusItems.size() > 0)){
            for (DeviceBorqsPlusData deviceBorqsPlusData : plusItems) {
                cpo.deleteData(deviceBorqsPlusData.id);
            }
        }
	}
	
	static private void addBorqsPlusData(long people,ContactProviderOperation cpo){
	    Context context = ApplicationGlobals.getContext();
        //conversion
	    ContentValues valuesCon = new ContentValues();
	    valuesCon.put(Data.MIMETYPE, ContactProviderOperation.MIME_TYPE_BORQS_PLUS);
	    valuesCon.put(Data.RAW_CONTACT_ID, people);
	    valuesCon.put(ContactProviderOperation.COLUMN_BORQS_NAME_SUMMARY, "");
	    valuesCon.put(ContactProviderOperation.COLUMN_BORQS_NAME, context.getString(R.string.request_conversion));
	    valuesCon.put(ContactProviderOperation.COLUMN_ACTION, ContactProviderOperation.ACTION_REQUEST_CONVERSATION);
        cpo.newData(valuesCon);
        
        //add to circles
        ContentValues valuesCir = new ContentValues();
        valuesCir.put(Data.MIMETYPE, ContactProviderOperation.MIME_TYPE_BORQS_PLUS);
        valuesCir.put(Data.RAW_CONTACT_ID, people);
        valuesCir.put(ContactProviderOperation.COLUMN_BORQS_NAME_SUMMARY, "");
        valuesCir.put(ContactProviderOperation.COLUMN_BORQS_NAME, context.getString(R.string.add_to_circles));
        valuesCir.put(ContactProviderOperation.COLUMN_ACTION, ContactProviderOperation.ACTION_ADD_TO_CIRCLES);
        cpo.newData(valuesCir);
        
        //view BPC profile
        ContentValues valuesView = new ContentValues();
        valuesView.put(Data.MIMETYPE, ContactProviderOperation.MIME_TYPE_BORQS_PLUS);
        valuesView.put(Data.RAW_CONTACT_ID, people);
        valuesView.put(ContactProviderOperation.COLUMN_BORQS_NAME_SUMMARY, "");
        valuesView.put(ContactProviderOperation.COLUMN_BORQS_NAME, context.getString(R.string.view_bpc_profile));
        valuesView.put(ContactProviderOperation.COLUMN_ACTION, ContactProviderOperation.ACTION_VIEW_PROFILE);
        cpo.newData(valuesView);
        
	}

	static private int compareUrl(WebsiteData left, WebsiteData right) {
		if (left.type != right.type) {
			return left.type - right.type;
		}
		int value = compareString(left.data, right.data);
		if (value != 0) {
			return value;
		}

		value = compareString(left.label, right.label);
		if (value != 0) {
			return value;
		}
		return 0;
	}
	static int compareUrl(WebsiteData currentId, DeviceCommonListData previousId) {
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

		value = compareString(currentId.label, previousId.label);
		if (value != 0) {
			return value;
		}
		return 0;
	}

	private static void updateUrlData(long people, ContactStruct contact,
			ContactProviderOperation cpo,
			List<DeviceCommonListData> deviceItems) {

		// Get server data
		List<WebsiteData> serverItems = new LinkedList<WebsiteData>();
		List<WebsiteData> tmpList = contact.getWebsiteList();
		List<WebsiteData> needAddUrl = new LinkedList<WebsiteData>();

		if (tmpList != null) {
			for (WebsiteData item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<WebsiteData>() {
			public int compare(WebsiteData left, WebsiteData right) {
				return compareUrl(left, right);
			}
		});

		Iterator<WebsiteData> currentItems = serverItems.iterator();

		WebsiteData currentId = null;
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

			int comValue = compareUrl(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddUrl.add(currentId);
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

		if (needAddUrl.size() > 0) {
			addUrl(needAddUrl, people, cpo);
		}
	}

	private static void addUrl(List<WebsiteData> urls, long people,
			ContactProviderOperation cpo) {
		for (ContactStruct.WebsiteData data : urls) {
			ContentValues values = new ContentValues();
			values.put(Website.MIMETYPE, Website.CONTENT_ITEM_TYPE);
			values.put(Website.RAW_CONTACT_ID, people);
			values.put(Website.TYPE, data.type);
			values.put(Website.URL, data.data);
			values.put(Website.LABEL, data.label);
			values.put(Website.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			cpo.newData(values);
		}
	}

	static private void deleteData(long id, ContactProviderOperation cpo) {
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
	static private void updatePhones(long people, ContactStruct contact,
								ContactProviderOperation cpo, List<DevicePhoneData> deviceItems) {

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
			ContactProviderOperation cpo) {
		for (ContactStruct.PhoneData data : phones) {
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
	
	static private void updateName(long people, ContactStruct contact,
								ContactProviderOperation cpo, List<DeviceNameData> deviceItems) { 
		DeviceNameData deviceItem = null;
		if(deviceItems != null && deviceItems.size() > 0){
			deviceItem = deviceItems.get(0);
		}
		long id = 0;
		if(deviceItem != null){
			id = deviceItem.id;
		}
		DeviceNameData serverItem = new DeviceNameData(id, contact.getLastName(),
													contact.getFirstName(),
													contact.getMiddleName(),
													contact.getPrefix(),
													contact.getSuffix(),
													contact.getPhoneticLastName(),
													contact.getPhoneticFirstName(),
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
	
	static private void updatePhoto(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DevicePhotoData> deviceItems) { 
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
	
	private static class DeviceBorqsName {
		public DeviceBorqsName(long id, String borqsName) {
			this.id = id;
			this.borqsName = borqsName;
		}
		
		long id;
		String borqsName;
	}
	
	static void updateBorqsName(long people, ContactStruct contact,
			ContactProviderOperation cpo, DeviceBorqsName deviceBorqsName) {
		long id = 0;
		if (deviceBorqsName != null) {
			String serverBorqsName = contact.getBorqsName();
			
			if (!TextUtils.isEmpty(deviceBorqsName.borqsName)) {
				// no change, no operation
				if (deviceBorqsName.borqsName.equals(serverBorqsName)) {
					return;
				}
			}
			
			id = deviceBorqsName.id;
			deleteData(id, cpo);
			
			// borqs name from the server is empty, then remove the item
			if (TextUtils.isEmpty(serverBorqsName)) {
				return;
			}
		}
		
		addBorqsName(contact, cpo, people);
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
	
	static void addNameInfo(ContactStruct contact, ContactProviderOperation cpo, long people){
		ContentValues values = new ContentValues();
		
		values.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.RAW_CONTACT_ID, people);
		values.put(StructuredName.GIVEN_NAME, contact.getFirstName());
		values.put(StructuredName.FAMILY_NAME, contact.getLastName());
		values.put(StructuredName.MIDDLE_NAME, contact.getMiddleName());
		values.put(StructuredName.SUFFIX, contact.getSuffix());
		values.put(StructuredName.PHONETIC_GIVEN_NAME, contact.getPhoneticFirstName());
		values.put(StructuredName.PHONETIC_FAMILY_NAME, contact.getPhoneticLastName());
		values.put(StructuredName.PHONETIC_MIDDLE_NAME, contact.getPhoneticMiddleName());
		values.put(StructuredName.PREFIX, contact.getPrefix());
		values.put(StructuredName.DISPLAY_NAME, contact.getDisplayName());
		cpo.newData(values);
	}
	
	static void addPhotoInfo(ContactStruct contact, ContactProviderOperation cpo, long people){
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
	
	static void addBorqsName(ContactStruct contact,
			ContactProviderOperation cpo, long people) {
		String borqsName = contact.getBorqsName();
		if (TextUtils.isEmpty(borqsName)) {
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put("mimetype", ContactProviderOperation.MIME_TYPE_BORQS_NAME);
		values.put("raw_contact_id", people);
		values.put(COLUMN_BORQS_NAME_SUMMARY, "");
		values.put(COLUMN_BORQS_NAME, ContactProviderOperation.TEXT_BORQS_NAME_SUMMARY + borqsName);
		
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
	
	static private void updateCommonData(long people, ContactStruct contact,
								ContactProviderOperation cpo, List<DeviceCommonData> deviceItems,
								int dataFlag) {
		// Get server data
		String serverItems = null;
		switch(dataFlag){
//			case DATA_FLAG_NAME:
//				serverItems = contact.getName();
//				break;
//			case DATA_FLAG_URL:
//				if(contact.getWebsiteList() != null && contact.getWebsiteList().size() > 0){
//					serverItems = contact.getWebsiteList().get(0);
//				}
//				break;
			case DATA_FLAG_NOTE:
				if(contact.getNotes() != null && contact.getNotes().size() > 0){
					serverItems = contact.getNotes().get(0);
				}
				break;
			case DATA_FLAG_NICKNAME:
				if(contact.getNickNameList() != null && contact.getNickNameList().size() > 0){
					serverItems = contact.getNickNameList().get(0);
				}
				break;
			case DATA_FLAG_BIRTHDAY:
				serverItems = contact.getBirthday();
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
			ContactProviderOperation cpo) {
	    if(TextUtils.isEmpty(value)){
	        return;
	    }
		ContentValues values = new ContentValues();
		switch(dataFlag){
//			case DATA_FLAG_NAME:
//				values.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
//				values.put(StructuredName.RAW_CONTACT_ID, people);
//				values.put(StructuredName.GIVEN_NAME, value);
//				break;
//			case DATA_FLAG_URL:
//				values.put(Website.MIMETYPE, Website.CONTENT_ITEM_TYPE);
//				values.put(Website.RAW_CONTACT_ID, people);
//				values.put(Website.TYPE, Website.TYPE_WORK);
//				values.put(Website.URL, value);
//				break;
			case DATA_FLAG_NOTE:
				values.put(Note.MIMETYPE, Note.CONTENT_ITEM_TYPE);
				values.put(Note.RAW_CONTACT_ID, people);
				values.put(Note.NOTE, value);
				break;
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
			case DATA_FLAG_BIRTHDAY:
				values.put(Event.MIMETYPE, Event.CONTENT_ITEM_TYPE);
				values.put(Event.RAW_CONTACT_ID, people);
				values.put(Event.TYPE, Event.TYPE_BIRTHDAY);
				values.put(Event.START_DATE, value);
				break;
		}
		cpo.newData(values);
	}
	
	// org
	private static class DeviceOrgData {
		DeviceOrgData(long id, int type, String company, String title, String department, String label) {
			this.id = id;
			this.type = type;
			this.company = company;
			this.title = title;
			this.department = department;
			this.label = label;
		}

		long id;
		int type;
		String company;
		String title;
		String department;
		String label;
		
	}
	
	static private void updateOrgs(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DeviceOrgData> deviceItems) {

		// Get server data
		List<OrganizationData> serverItems = new LinkedList<OrganizationData>();
		List<OrganizationData> tmpList = contact.getOrganizationList();
		List<OrganizationData> needAddOrgs = new LinkedList<OrganizationData>();

		if (tmpList != null) {
			for (OrganizationData item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<OrganizationData>() {
			public int compare(OrganizationData left, OrganizationData right) {
				return compareOrg(left, right);
			}
		});

		Iterator<OrganizationData> currentItems = serverItems.iterator();

		OrganizationData currentId = null;
		DeviceOrgData previousId = null;
		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;
		if(deviceItems == null){
			deviceItems = new LinkedList<DeviceOrgData>();
		}
		Iterator<DeviceOrgData> previousItems = deviceItems.iterator();

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

			int comValue = compareOrg(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddOrgs.add(currentId);
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

		if (needAddOrgs.size() > 0) {
			addOrgs(needAddOrgs, people, cpo);
		}
	}

	static private void addOrgs(List<OrganizationData> orgs, long people,
			ContactProviderOperation cpo) {
		for (ContactStruct.OrganizationData data : orgs) {
			ContentValues values = new ContentValues();
			values.put(Organization.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
			values.put(Organization.RAW_CONTACT_ID, people);
			values.put(Organization.TYPE, data.type);
			values.put(Organization.COMPANY, data.companyName);
			values.put(Organization.TITLE, data.positionName);
			values.put(Organization.DEPARTMENT, data.department);
			values.put(Organization.LABEL, data.label);
			values.put(Organization.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			cpo.newData(values);
		}
		
	}

	static int compareOrg(OrganizationData left, OrganizationData right) {
		if (left.type != right.type) {
			return left.type - right.type;
		}
		int value = compareString(left.companyName, right.companyName);
		if (value != 0) {
			return value;
		}
		value = compareString(left.positionName, right.positionName);
		if (value != 0) {
			return value;
		}		
		value = compareString(left.department, right.department);
		if (value != 0) {
			return value;
		}
		
		value = compareString(left.label, right.label);
		if (value != 0) {
			return value;
		}

		return 0;
	}

	static int compareOrg(OrganizationData currentId, DeviceOrgData previousId) {
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
		int value = compareString(currentId.companyName, previousId.company);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.positionName, previousId.title);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.department, previousId.department);
		if (value != 0) {
			return value;
		}
		
		value = compareString(currentId.label, previousId.label);
		if (value != 0) {
			return value;
		}

		return 0;
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
	
	static private void updateEmailData(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DeviceCommonListData> deviceItems) {

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
			ContactProviderOperation cpo) {
		for (ContactStruct.EmailData data : email) {	
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
	
	// Im	
	static private void updateImData(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DeviceCommonListData> deviceItems) {

		// Get server data
		List<ImData> serverItems = new LinkedList<ImData>();
		List<ImData> tmpList = contact.getImList();
		List<ImData> needAddIm = new LinkedList<ImData>();

		if (tmpList != null) {
			for (ImData item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<ImData>() {
			public int compare(ImData left, ImData right) {
				return compareIm(left, right);
			}
		});

		Iterator<ImData> currentItems = serverItems.iterator();

		ImData currentId = null;
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

			int comValue = compareIm(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddIm.add(currentId);
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

		if (needAddIm.size() > 0) {
			addIm(needAddIm, people, cpo);
		}
	}

	static private void addIm(List<ImData> im, long people,
			ContactProviderOperation cpo) {
		for (ContactStruct.ImData data : im) {
			ContentValues values = new ContentValues();
			values.put(Im.MIMETYPE, Im.CONTENT_ITEM_TYPE);
			values.put(Im.RAW_CONTACT_ID, people);
			values.put(Im.TYPE, Im.TYPE_OTHER);
			values.put(Im.DATA, data.data);
			values.put(Im.LABEL, data.label);
			values.put(Im.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			values.put(Im.PROTOCOL, data.type);
//			values.put(Im.CUSTOM_PROTOCOL, data.customProtocol);
//			values.put(Data.DATA14, ImVCard.encodePredefinedImProtocol(data.type));
			cpo.newData(values);
		}
		
	}

	static int compareIm(ImData left, ImData right) {
		if (left.type != right.type) {
			return left.type - right.type;
		}
		int value = compareString(left.data, right.data);
		if (value != 0) {
			return value;
		}

		return 0;
	}

	static int compareIm(ImData currentId, DeviceCommonListData previousId) {
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
	
	// postal
	private static class DevicePostalData {
		DevicePostalData(long id, 
				int type,
				String pobox,
				String extendedAddress,
				String street,
				String localty,//localty == city
				String region,
				String postalCode,
				String country) {
			this.id = id;
			this.type = type;
			this.pobox = pobox;
			this.extendedAddress = extendedAddress;
			this.extendedAddress = extendedAddress;
			this.street = street;
			this.localty = localty;
			this.region = region;
			this.postalCode = postalCode;
			this.country = country;
		}

		long id;
		int type;
		String pobox;
		String extendedAddress;
		String street;
		String localty;//localty == city
		String region;
		String postalCode;
		String country;
	}
	
	static private void updatePostalData(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DevicePostalData> deviceItems) {

		// Get server data
		List<PostalData> serverItems = new LinkedList<PostalData>();
		List<PostalData> tmpList = contact.getPostalList();
		List<PostalData> needAddPostal = new LinkedList<PostalData>();

		if (tmpList != null) {
			for (PostalData item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<PostalData>() {
			public int compare(PostalData left, PostalData right) {
				return comparePostal(left, right);
			}
		});

		Iterator<PostalData> currentItems = serverItems.iterator();

		PostalData currentId = null;
		DevicePostalData previousId = null;
		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;

		if(deviceItems == null){
			deviceItems = new LinkedList<DevicePostalData>();
		}
		Iterator<DevicePostalData> previousItems = deviceItems.iterator();

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

			int comValue = comparePostal(currentId, previousId);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddPostal.add(currentId);
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

		if (needAddPostal.size() > 0) {
			addPostal(needAddPostal, people, cpo);
		}
	}

	static private void addPostal(List<PostalData> postal, long people,
			ContactProviderOperation cpo) {
		for (ContactStruct.PostalData data : postal) {
			ContentValues values = new ContentValues();
			values.put(StructuredPostal.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
			values.put(StructuredPostal.RAW_CONTACT_ID, people);
			values.put(StructuredPostal.TYPE, data.type);
			values.put(StructuredPostal.POBOX, data.pobox);
			values.put(StructuredPostal.NEIGHBORHOOD, data.extendedAddress);
			values.put(StructuredPostal.STREET, data.street);
			values.put(StructuredPostal.CITY, data.localty);
			values.put(StructuredPostal.REGION, data.region);
			values.put(StructuredPostal.POSTCODE, data.postalCode);
			values.put(StructuredPostal.COUNTRY, data.country);
			values.put(StructuredPostal.LABEL, data.label);
			values.put(StructuredPostal.IS_SUPER_PRIMARY, data.isPrimary ? 1 : 0);
			cpo.newData(values);
		}
		
	}

	static int comparePostal(PostalData left, PostalData right) {
		if (left.type != right.type) {
			return left.type - right.type;
		}
		int value = compareString(left.pobox, right.pobox);
		if (value != 0) {
			return value;
		}
		value = compareString(left.extendedAddress, right.extendedAddress);
		if (value != 0) {
			return value;
		}
		value = compareString(left.street, right.street);
		if (value != 0) {
			return value;
		}
		value = compareString(left.localty, right.localty);
		if (value != 0) {
			return value;
		}
		value = compareString(left.region, right.region);
		if (value != 0) {
			return value;
		}
		value = compareString(left.postalCode, right.postalCode);
		if (value != 0) {
			return value;
		}
		value = compareString(left.country, right.country);
		if (value != 0) {
			return value;
		}

		return 0;
	}

	static int comparePostal(PostalData currentId, DevicePostalData previousId) {
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
		int value = compareString(currentId.pobox, previousId.pobox);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.extendedAddress, previousId.extendedAddress);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.street, previousId.street);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.localty, previousId.localty);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.region, previousId.region);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.postalCode, previousId.postalCode);
		if (value != 0) {
			return value;
		}
		value = compareString(currentId.country, previousId.country);
		if (value != 0) {
			return value;
		}

		return 0;
	}
	
	
	// postal
	private static class DeviceGroupData {
		DeviceGroupData(long id,int groupRowId) {
			this.id = id;
			this.groupRowId = groupRowId;
		}

		long id;
		int groupRowId;
	}
	
	static private void updateGroupData(long people, ContactStruct contact,
			ContactProviderOperation cpo, List<DeviceGroupData> deviceItems,ContentResolver resolver) {

		// Get server data
		List<String> serverItems = new LinkedList<String>();
		List<String> tmpList = contact.getGroupList();
		List<String> needAddGroup = new LinkedList<String>();

		if (tmpList != null) {
			for (String item : tmpList) {
				serverItems.add(item);
			}
		}
		Collections.sort(serverItems, new Comparator<String>() {
			public int compare(String left, String right) {
				return compareGroup(left, right);
			}
		});

		Iterator<String> currentItems = serverItems.iterator();

		String currentId = null;
		DeviceGroupData previousId = null;
		boolean prevMoveFlag = true;
		boolean curMoveFlag = true;

		if(deviceItems == null){
			deviceItems = new LinkedList<DeviceGroupData>();
		}
		Iterator<DeviceGroupData> previousItems = deviceItems.iterator();

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

			int comValue = compareGroup(currentId, previousId,resolver);
			if (comValue == 0) {
				curMoveFlag = true;
				prevMoveFlag = true;
			} else if (comValue < 0) {
				// new item
				needAddGroup.add(currentId);
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

		if (needAddGroup.size() > 0) {
			addGroup(needAddGroup, people, cpo,resolver,contact);
		}
	}

	static private void addGroup(List<String> group, long people,
			ContactProviderOperation cpo,ContentResolver resolver,ContactStruct contact) {
		for (String data : group) {
			ContentValues values = new ContentValues();
			values.put(CommonDataKinds.GroupMembership.MIMETYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
			values.put(CommonDataKinds.GroupMembership.RAW_CONTACT_ID, people);
			values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, getGroupIdByName(data, resolver,contact));
			cpo.newData(values);
		}
		
	}
	
	static private long getGroupIdByName( String group,ContentResolver resolver,ContactStruct contact ){
		long groupId = -1;
		Cursor dataC = resolver.query(Groups.CONTENT_URI, null, Groups.TITLE +" = \""+ group +"\" ", null,
				null);
		if ( dataC != null){
			if (dataC.moveToFirst()){
				groupId = dataC.getInt(dataC.getColumnIndexOrThrow(Groups._ID));
			} else {
				groupId = insertGroup( group, resolver,contact);
			}
			dataC.close();
		} else {
			groupId = insertGroup( group, resolver,contact);
		}
		return groupId;
	}
	
	protected static String getGroupNameById( int groupId, ContentResolver resolver){
		String group ="" ;
		
		Uri groupUri = Uri.withAppendedPath(Groups.CONTENT_URI, Long
				.toString(groupId));
		Cursor dataC = resolver.query(groupUri, null, null, null,
				null);
		if ( dataC != null){
			if (dataC.moveToFirst()){
				group = dataC.getString(dataC.getColumnIndexOrThrow(Groups.TITLE));
			}
			dataC.close();
		}
		return group;
	}
	
	static private long insertGroup( String group,ContentResolver resolver,ContactStruct contact){
	    //TODO temp solution ,we should sync account type and account name,get account name and type from 
	    //ContactStruct from server,not local Account's info
	    String accountName = ProfileConfig.getProfileName(ApplicationGlobals.getContext());
	    String accountType = TextUtils.isEmpty(contact.getAccountType())
	            ? AccountAdapter.BORQS_ACCOUNT_TYPE:contact.getAccountType();
	    
		long groupId = -1;
	  	ContentValues values = new ContentValues();
	  	values.put(Groups.ACCOUNT_NAME, accountName);
	  	values.put(Groups.ACCOUNT_TYPE, accountType);
	  	values.put(Groups.TITLE, group);
	  	values.put(Groups.GROUP_VISIBLE, 1);
	  	Uri uri = resolver.insert(Groups.CONTENT_URI, values);
	  	groupId = ContentUris.parseId(uri);
		return groupId;
	}

	static int compareGroup(String left, String right) {
		int value = compareString(left, right);
		if (value != 0) {
			return value;
		}
		return 0;
	}

	static int compareGroup(String currentId, DeviceGroupData previousId,ContentResolver resolver) {
		if (currentId == null && previousId == null) {
			return 0;
		}

		if (currentId == null && previousId != null) {
			return 1;
		}

		if (currentId != null && previousId == null) {
			return -1;
		}

		int value = compareString(currentId, getGroupNameById(previousId.groupRowId,resolver));
		if (value != 0) {
			return value;
		}
		return 0;
	}

	static private boolean update(long people, ContactStruct contact,
			ContentResolver resolver,boolean callerIsSyncAdapter) {
		ContactProviderOperation cpo = new ContactProviderOperation(resolver,callerIsSyncAdapter);
		boolean retValue = updatePeople(people, contact, resolver, cpo);
		if(retValue){
			updateData(people, contact, resolver, cpo);
		}		

		cpo.execute();
		return retValue;
	}
	
	static public boolean update(long people, ContactStruct contact,
            ContentResolver resolver) {
	    return update(people,contact,resolver,true);
    }
	
	static public boolean updateWithNotSyncCaller(long people, ContactStruct contact,
            ContentResolver resolver) {
	    return update(people,contact,resolver,false);
	}
}
