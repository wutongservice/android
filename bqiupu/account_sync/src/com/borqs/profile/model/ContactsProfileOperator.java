package com.borqs.profile.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

public class ContactsProfileOperator implements IOperator {
	
	private static final String SELECTION = RawContacts.ACCOUNT_NAME
			+ "=? AND " + RawContacts.ACCOUNT_TYPE + "=? AND "
			+ RawContacts.DELETED + "=?";
	
	private ContentResolver mResolver;

	private String accountName;
	private String accountType;
	
	public ContactsProfileOperator(Context context) {
		mResolver = context.getContentResolver();
	}
	
	@Override
	public long add(ContactProfileStruct d) {
		if(null != this.accountName && null != this.accountType) {
			d.setAccount(accountName, accountType);
		}
		return ContactsProfileOperatorImp.add(d, mResolver);
	}

	@Override
	public boolean update(long id, ContactProfileStruct d) {
		return ContactsProfileOperatorImp.update(id, (ContactProfileStruct)d, mResolver);
	}

	@Override
	public boolean delete(long id) {
		return ContactsProfileOperatorImp.delete(id, mResolver);
	}

	@Override
	public Object load(long id) {
		return ContactsProfileOperatorImp.load(id, mResolver);
	}

	@Override
	public long[] getCurrentItems(String accountName, String accountType) {
		long ids[] = null;
        if(TextUtils.isEmpty(accountName)){
            return ids;
        }
		Cursor cursor = mResolver.query(Profile.CONTENT_RAW_CONTACTS_URI,
				new String[] { Profile._ID }, SELECTION, new String[] {
						accountName, accountType, "0" }, null);
		if (cursor != null) {
			try {
				int size = cursor.getCount();
				if (size > 0) {
					ids = new long[size];
					int i = 0;
					if (cursor.moveToFirst()) {
						do {
							ids[i++] = cursor.getLong(0);
						} while (cursor.moveToNext());
					}
				}
			} finally {
				cursor.close();
			}
		}
		return ids;
	}

	@Override
	public void setTargetAccount(String accountName, String accountType) {
		this.accountName = accountName;
		this.accountType = accountType;
	}

	@Override
	public long[] batchAdd(ContactProfileStruct[] contacts) {
		return ContactsProfileOperatorImp.batchAdd(contacts, mResolver);
	}

	@Override
	public boolean batchDelete(long[] ids, int start, int length) {
		return ContactsProfileOperatorImp.batchDelete(ids, start, length, mResolver);
	}

	@Override
	public boolean batchUpdate(long[] ids, int start, int length,
			ContactProfileStruct[] contacts) {
		return ContactsProfileOperatorImp.batchUpdate(ids, start, length, mResolver, contacts);
	}
}
