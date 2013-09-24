/*
 * Copyright (C) 2008 Borqs Inc.
 *
 */
package com.borqs.sync.client.vdata.card;

import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.borqs.sync.client.vdata.PimSyncmlInterface;

/**
 * Copy from MMS VCardManager This class helps export and import a VCard file.
 */

final public class ContactsVCardOperator implements
		PimSyncmlInterface<ContactStruct> {

	static private final String TAG = "ContactsVCardOperator";
	static private final String LAST_NAME_KEY = "ln";
//	static private final String ORGANIZATION_UNIT = "unit";

	private boolean mIsContainPhoto = true;
	private boolean mHasPref = true;
	//only contain family and given name for PIM
	private boolean mOnlyContainFGName;

	public void isContainPhotoInfo(boolean isContainPhoto) {
		mIsContainPhoto = isContainPhoto;
	}
	
	/**
	 * 
	 * @param onlyContainFGName true vCard "N" property only contains FamilyName and GivenName
	 */
	public void setOnlyContainFGName(boolean onlyContainFGName){
		mOnlyContainFGName = onlyContainFGName;
	}

	public ContactsVCardOperator() {
	}
	
	public ContactsVCardOperator(boolean notUseDefaultCharset) {

	}

	
	public void hasPrefInVcard(boolean hasPref){
		mHasPref = hasPref;
	}

	public boolean update(long people, ContactStruct contact,
			ContentResolver resolver) {
		return ContactOperator.update(people, contact, resolver);
	}

	public boolean delete(long people, ContentResolver resolver) {
		return ContactOperator.delete(people, resolver);
	}

	public long add(ContactStruct contact, ContentResolver mResolver) {
		return ContactOperator.add(contact, mResolver);
	}

	public long add(ContactStruct contact, long id, ContentResolver mResolver) {
		return ContactOperator.add(contact, id, mResolver);
	}

	public List<ContactStruct> parseList(String data) {
		return null;
	}

	public List<ContactStruct> parseList(byte[] data) {
		return VcardOperator.parseList(data, 0);
	}

	public ContactStruct parse(String data) {
		return VcardOperator.parse(data, 0);
	}
	
	public ContactStruct parse(byte[] data) {
		return VcardOperator.parse(data, 0);
	}

	public ContactStruct load(long people, ContentResolver mResolver) {
		return ContactOperator.load(people, mResolver);
	}
	
	public ContactStruct load(Uri uri, ContentResolver mResolver) {
		return ContactOperator.load(uri, mResolver);
	}

	public ContactStruct load(Cursor contactC, ContentResolver mResolver) {
		return ContactOperator.load(contactC, mResolver);
	}

	public String create(ContactStruct contact, int version) {
		return VcardOperator.create(contact);
	}
}
