package com.borqs.profile.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
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
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.OrganizationData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;
import com.borqs.sync.client.vdata.card.ContactStruct.PostalData;

public class ContactsProfileOperatorImp {
	
	/*rawcontact,birthday and name is not list ,so their size is 3*/
	private static final int CONTACT_FIXED_OPERATION_SIZE = 3;
	 /*
	  * copy from @com.android.providers.contacts.SQLiteContentProvider 
	  * Google design
	  * Maximum number of operations allowed in a batch between yield points.
	  */
	private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;
	
	public static boolean update(long people, ContactProfileStruct contact,
			ContentResolver resolver) {
		return ContactProfileUpdate.update(people, contact, resolver);
	}
	
	public static boolean batchUpdate(long ids[], int start, int length, ContentResolver resolver,
			ContactProfileStruct[] contacts){
		boolean ret = true;
		ContactProfileProviderOperation cpo = new ContactProfileProviderOperation(resolver);
		for (int i = 0; i < length &&  start + i < ids.length; i++) {
			ContactProfileStruct contact = contacts[start + i];

			ret &= ContactProfileUpdate.updatePeopleNoCommit(ids[start + i],contact,resolver,cpo);
				
		}
		cpo.execute();
		return ret;
	}
	
	public static boolean delete(long people, ContentResolver resolver) {
		ContactProfileProviderOperation co = new ContactProfileProviderOperation(resolver);
		co.delete(people);
		co.execute();
		return true;
	}
	
	public static boolean batchDelete(long ids[], int start, int length, ContentResolver resolver){
		ContactProfileProviderOperation co = new ContactProfileProviderOperation(resolver);
		for (int i = 0; i < length &&  start + i < ids.length; i++) {
			co.delete(ids[start + i]);
		}
		co.execute();
		return true;
	}

	public static long add(ContactProfileStruct contact, ContentResolver resolver) {
		long[] ids = batchAdd(new ContactProfileStruct[]{contact},resolver);
		return ids[0];
	}
	
	public static long[] batchAdd(ContactProfileStruct[] contacts,  ContentResolver resolver){
		ContactProfileProviderOperation ops = new ContactProfileProviderOperation(resolver);
		List<Long> idsList = new ArrayList<Long>();
		
		for(ContactProfileStruct contact:contacts){
			if(contact == null){
			    continue;			
			}
			
			ContactTransferStruct cts = new ContactTransferStruct(contact);
			int operationSize = ops.size() + cts.getContactListPropertySize();
			/*
			 * if the previous loop size  + current loop size is more than CONTACT_FIXED_OPERATION_SIZE,we will commit previous.
			 * and reset the ContactProviderOperation for new operation list count
			 * else ,continue the loop 
			 * After the loop finished,we will commit the remaining ContentProviderOperations
			 */
			if(operationSize >= MAX_OPERATIONS_PER_YIELD_POINT){
				applyBatch(ops,idsList);
				//reset the operation
				ops = new ContactProfileProviderOperation(resolver);
			}
			
			ops.newContact(contact);
			// birthday
			ops.addBirthday(null, contact.getBirthday());
			// photo
			if(contact.getPhotoList() != null && contact.getPhotoList().size() > 0){
				for(ContactProfileStruct.PhotoData photo:contact.getPhotoList()){
					ops.addPhoto(null, photo.photoBytes);
				}
			}
			// name
			ops.addName(null, contact.getPrefix(),
					contact.getGivenName(),
					contact.getFamilyName(),
					contact.getMiddleName(),
					contact.getSuffix(), contact.getPhoneticGivenName(),
					contact.getPhoneticFamilyName(), contact.getPhoneticMiddleName(),null);
			if(!TextUtils.isEmpty(contact.getNickName())) {
				ops.addNickname(null, contact.getNickName());
			}
			
			// phone
			if(cts.phoneList != null){
				for(ContactProfileStruct.PhoneData phone:cts.phoneList){
					ops.addPhone(null, phone.type, phone.data, phone.label, phone.isPrimary);
				}
			}
			// org
			if(cts.orgList != null){
				for(ContactProfileStruct.OrganizationData org:cts.orgList){
					ops.addOrganization(null, org.type, org.companyName, org.positionName, org.department, null, null,
										org.label, org.isPrimary);
				}
			}
			
			// email
			if(cts.emailList != null){
				for(ContactProfileStruct.EmailData email:cts.emailList){
					ops.addEmail(null, email.type, email.data, email.label, email.isPrimary);
				}
			}
			// adr
			if(cts.addressList != null){
				for(ContactProfileStruct.PostalData adr:cts.addressList){
					ops.addPostal(null, adr.type, adr.pobox, adr.extendedAddress, adr.street, adr.localty,
							adr.region, adr.postalCode, adr.country, adr.label, adr.isPrimary);
				}
			}
				
		}
		
		applyBatch(ops,idsList);
		return toLongArray(idsList);
	}
	
	/**
	 * class for transfer the ContactStruct.
	 * we can get the List property of ContactStruct.
	 * @author b211
	 *
	 */
	static class ContactTransferStruct{
		List<PhotoData> photoList = null;
		String nickname = null;
		List<PhoneData> phoneList = null;
		List<EmailData> emailList = null;
		List<OrganizationData> orgList = null;
		List<PostalData> addressList = null;
		public ContactTransferStruct(ContactProfileStruct contact){
			photoList = contact.getPhotoList();
			nickname = contact.getNickName();
			phoneList = contact.getPhoneList();
			emailList = contact.getEmailList();
			addressList = contact.getPostalList();
			orgList = contact.getOrganizationList();
		}
		
		public int getContactListPropertySize(){
			int photoSize = getListSize(photoList);
			int nickNameSize = TextUtils.isEmpty(nickname) ? 0 : 1;
			int phoneSize = getListSize(phoneList);
			int emailSize = getListSize(emailList);
			int orgSize = getListSize(orgList);
			int addressSize = getListSize(addressList);
			// compute the content provider operation size that we will commit.
			return CONTACT_FIXED_OPERATION_SIZE + photoSize + nickNameSize
					+ phoneSize + emailSize + orgSize + addressSize;
		}
	}
	
	/**
	 * insert the contact data into db
	 * @param ops ContentProviderOperation list
	 * @param idList the added id list
	 */
	private static void applyBatch(ContactProfileProviderOperation ops,List<Long> idList){
		ops.execute();
		if (ops.mResults != null) {
            for (int i = 0; i < ops.mContactIndexCount; i++) {
                int index = ops.mContactIndexArray[i];
                Uri u = ops.mResults[index].uri;
                if (u != null) {
                    String idString = u.getLastPathSegment();
                    idList.add(Long.parseLong(idString));
                }
            }
        }
	}
	
	public static long add(ContactProfileStruct contact, long id, ContentResolver resolver) {
		return add(contact,resolver);
	}

	public static class ContactData {
		private String mMimetype;
		private boolean mIsPrimary;
		private String[] mData = new String[15];

		public ContactData(String mimetype,
						  boolean isPrimary,
						  String[] data){
			this.mMimetype = mimetype;
			this.mIsPrimary = isPrimary;
			this.mData = data;
		}

		public String getMimeType() {
			return mMimetype;
		}

		public boolean isPrimary() {
			return mIsPrimary;
		}
		
		public String[] getData() {
			return mData;
		}
	}

	public static ContactProfileStruct load(long people, ContentResolver resolver) {
		return load(ContentUris.withAppendedId(Profile.CONTENT_RAW_CONTACTS_URI,
				people), resolver);
	}
	
	public static ContactProfileStruct load(Uri uri, ContentResolver resolver) {
		Cursor contactC = resolver.query(uri, null, null, null, null);
		ContactProfileStruct contactStruct = null;
		if (contactC != null) {
			if (contactC.moveToFirst()) {
				contactStruct = load(contactC, resolver);
			}
			contactC.close();
		}
		return contactStruct;
	}

	public static ContactProfileStruct load(Cursor contactC, ContentResolver resolver) {

		ContactProfileStruct contactStruct = new ContactProfileStruct();
		long id = contactC.getLong(contactC
				.getColumnIndexOrThrow(Profile._ID));

		Uri rawContactUri = Uri.withAppendedPath(Profile.CONTENT_RAW_CONTACTS_URI, Long
				.toString(id));
		
		String t = contactC.getString(contactC.getColumnIndexOrThrow(RawContacts.ACCOUNT_TYPE));
		String n = contactC.getString(contactC.getColumnIndexOrThrow(RawContacts.ACCOUNT_NAME));
		contactStruct.setAccount(n,t);
		
		// display name
		contactStruct.setDisplayName(contactC.getString(contactC.getColumnIndexOrThrow("display_name")));
		
		Cursor dataC = resolver.query(Uri.withAppendedPath(rawContactUri,
				RawContacts.Data.CONTENT_DIRECTORY), null, null, null,
				null);
		String  imieType = "";
		if(dataC != null){		    
			while(dataC.moveToNext()){
				imieType = dataC.getString(dataC.getColumnIndexOrThrow(Data.MIMETYPE));
				// name
				if(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(imieType)){					
				    contactStruct.setFamilyName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.FAMILY_NAME)));
				    contactStruct.setGivenName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.GIVEN_NAME)));
				    contactStruct.setMiddleName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.MIDDLE_NAME)));
				    contactStruct.setPrefix(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.PREFIX)));
				    contactStruct.setSuffix(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.SUFFIX)));
				    contactStruct.setPhoneticFamilyName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.PHONETIC_FAMILY_NAME)));
				    contactStruct.setPhoneticGivenName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.PHONETIC_GIVEN_NAME)));
				    contactStruct.setPhoneticMiddleName(dataC.getString(dataC.getColumnIndexOrThrow(StructuredName.PHONETIC_MIDDLE_NAME)));
					
				// nickname
				}else if(Nickname.CONTENT_ITEM_TYPE.equals(imieType)){
					contactStruct.addNickNameList(dataC.getString(dataC.getColumnIndexOrThrow(Nickname.NAME)));
				// phone
				}else if(Phone.CONTENT_ITEM_TYPE.equals(imieType)){
					String strType = dataC.getString(dataC.getColumnIndexOrThrow(Phone.TYPE));
					int type = strType != null ? Integer.parseInt(strType) : Phone.TYPE_OTHER;
					contactStruct.addPhone(
							type,
							dataC.getString(dataC.getColumnIndexOrThrow(Phone.NUMBER)), 
							dataC.getString(dataC.getColumnIndexOrThrow(Phone.LABEL)),
							!"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(Phone.IS_SUPER_PRIMARY))) );					
				// email
				}else if(Email.CONTENT_ITEM_TYPE.equals(imieType)){
					String strType = dataC.getString(dataC.getColumnIndexOrThrow(Email.TYPE));
					int type = strType != null ? Integer.parseInt(strType) : Email.TYPE_OTHER;
					contactStruct.addEmail(
							type,
							dataC.getString(dataC.getColumnIndexOrThrow(Email.DATA)), 
							dataC.getString(dataC.getColumnIndexOrThrow(Email.LABEL)),
							!"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(Email.IS_SUPER_PRIMARY))) );					
				// adr
				}else if(StructuredPostal.CONTENT_ITEM_TYPE.equals(imieType)){
					if(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.TYPE)) == null){
						continue;
					}
					
					List<String> adrValue=new ArrayList<String>();
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.POBOX)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.NEIGHBORHOOD)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.STREET)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.CITY)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.REGION)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.POSTCODE)));
					adrValue.add(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.COUNTRY)));
					
					contactStruct.addPostal(
							Integer.parseInt(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.TYPE))),
							adrValue,
							dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.LABEL)),
							!"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(StructuredPostal.IS_SUPER_PRIMARY)))
							);
					// IM
                }else if(Im.CONTENT_ITEM_TYPE.equals(imieType)){
                    if(dataC.getString(dataC.getColumnIndexOrThrow(Im.PROTOCOL)) == null){
                        continue;
                    }
                    contactStruct.addIm(
                            Integer.parseInt(dataC.getString(dataC.getColumnIndexOrThrow(Im.PROTOCOL))),
                            dataC.getString(dataC.getColumnIndexOrThrow(Im.DATA)), 
                            dataC.getString(dataC.getColumnIndexOrThrow(Im.LABEL)),
                            dataC.getString(dataC.getColumnIndexOrThrow(Im.CUSTOM_PROTOCOL)),
                            !"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(Im.IS_SUPER_PRIMARY))) );
                // org
                }else if(Organization.CONTENT_ITEM_TYPE.equals(imieType)){
					String strType = dataC.getString(dataC.getColumnIndexOrThrow(Organization.TYPE));
					int type = strType != null ? Integer.parseInt(strType) : Organization.TYPE_OTHER;
					contactStruct.addOrganization(
							type,
							dataC.getString(dataC.getColumnIndexOrThrow(Organization.COMPANY)),
							dataC.getString(dataC.getColumnIndexOrThrow(Organization.TITLE)),
							dataC.getString(dataC.getColumnIndexOrThrow(Organization.DEPARTMENT)),
							dataC.getString(dataC.getColumnIndexOrThrow(Organization.LABEL)),
							!"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(Organization.IS_SUPER_PRIMARY))) );					
					// notes
                }else if(Note.CONTENT_ITEM_TYPE.equals(imieType)){
                    contactStruct.addNote(dataC.getString(dataC.getColumnIndexOrThrow(Note.NOTE)));
                }else if(Website.CONTENT_ITEM_TYPE.equals(imieType)){
                    //Website
                    String strType = dataC.getString(dataC.getColumnIndexOrThrow(Website.TYPE));
                    int type = strType != null ? Integer.parseInt(strType) : Website.TYPE_OTHER;
                    contactStruct.addWebsite(
                            type,
                            dataC.getString(dataC.getColumnIndexOrThrow(Website.URL)), 
                            dataC.getString(dataC.getColumnIndexOrThrow(Website.LABEL)),
                            !"0".equals(dataC.getString(dataC.getColumnIndexOrThrow(Website.IS_SUPER_PRIMARY))) );                    
                // photo
				}else if(Photo.CONTENT_ITEM_TYPE.equals(imieType)){
					contactStruct.addPhotoBytes(null, dataC.getBlob(dataC.getColumnIndexOrThrow(Photo.PHOTO)));
				//birthday
				}else if(CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(imieType)){
				    String type = dataC.getString(dataC.getColumnIndexOrThrow(CommonDataKinds.Event.TYPE));
                    if(!TextUtils.isEmpty(type) && CommonDataKinds.Event.TYPE_BIRTHDAY == Integer.parseInt(type)){
                        contactStruct.setBirthday(dataC.getString(dataC.getColumnIndexOrThrow(CommonDataKinds.Event.START_DATE)));
                    }
				}
			}
			dataC.close();
		}
		return contactStruct;
	}

    private static long[] toLongArray(List<Long> list) {
   		if (list != null) {
   			final int len = list.size();
   			long[] longArray = new long[len];
   			for (int i = 0; i < len; i++) {
   				longArray[i] = list.get(i);
   			}
   			return longArray;
   		}
   		return new long[0];
   	}

    private static int getListSize(List<?> list) {
   		return list != null ? list.size() : 0;
   	}
}
