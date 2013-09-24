package com.borqs.qiupu.test;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
import android.test.AndroidTestCase;

import com.borqs.contacts.manage.ContactsManageUtil;
import com.borqs.contacts.model.ContactStruct;
import com.borqs.contacts.model.ContactsOperator;

public class ContactsManageUtilTest extends AndroidTestCase {
	private Context context;
	private ContactsOperator co;
	private String borqsaccountname;
	private String borqsaccounttype;
	private String notborqsaccountname;
	private String notborqsaccounttype;

	@Override
	protected void setUp() throws Exception {
		context = this.getContext().getApplicationContext();
		co = new ContactsOperator(context);
		borqsaccountname = "274";
		borqsaccounttype = "com.borqs";
		notborqsaccountname = "kt.hector@gmail.com";
		notborqsaccounttype = "com.google";
		super.setUp();
	}

	private void init() {
		Cursor cursor = context.getContentResolver().query(
				RawContacts.CONTENT_URI, new String[] { RawContacts._ID },
				RawContacts.DELETED + "=0", null, null);
		if (cursor != null) {
			long[] ids = new long[cursor.getCount()];
			int i = 0;
			try {
				while (cursor.moveToNext()) {
					ids[i++] = cursor.getLong(0);
				}
				if (ids.length > 0) {
					co.batchDelete(ids, 0, ids.length);
				}
			} finally {
				cursor.close();
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void setAsBorqsFriend(long id) {
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.SYNC4, 100);
		Uri peopleUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, id);
		context.getContentResolver().update(peopleUri, cv, "_id=" + id, null);
	}
	
	/*
	 * Both A & b are  under Borqs account.
	 * They have same phone's number. 12811047700
	*/
	public void test1() {
		init();

		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.keaaa@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.keBBB@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		
		ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil.getMergeContacts(context, borqsaccountname, borqsaccounttype, list);
		
		Assert.assertEquals(0, noNeedUserDeal.size());
		Assert.assertEquals(1, list.size());
		HashSet<Long> set = list.get(0);
		Assert.assertTrue(set.contains(ida));
		Assert.assertTrue(set.contains(idb));
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * Both A & b are  under Borqs account.
	 * They have same email. abc@borqs.com
	*/
	public void test2() {
		init();

		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);

		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "23456", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long idb = co.add(csB);

		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname,
						borqsaccounttype);
		ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil
				.getMergeContacts(context, borqsaccountname, borqsaccounttype,
						list);

		Assert.assertEquals(0, noNeedUserDeal.size());
		Assert.assertEquals(1, list.size());
		HashSet<Long> set = list.get(0);
		Assert.assertTrue(set.contains(ida));
		Assert.assertTrue(set.contains(idb));

		HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
				borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * Both A & b are  under Borqs account.
	 * A : phone : 12345
	 * B : phone : 23456
	 * A : email : tian.ke@borqs.com
	 * B : email : tian.kebbb@borqs.com
	 * They are different people.
	*/
	public void test3() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "23456", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.kebbb@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long idb = co.add(csB);

		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname,
						borqsaccounttype);
		ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil
				.getMergeContacts(context, borqsaccountname, borqsaccounttype,
						list);

		Assert.assertEquals(0, noNeedUserDeal.size());
		Assert.assertEquals(0, list.size());

		HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
				borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * A under borqs account
	 * B not under borqs account
	 * They have some phone/email.
	 * A : phone : 12345
	 * B : phone : 12345
	 * A : email : tian.ke@borqs.com
	 * B : email : tian.ke@borqs.com
	 * 
	*/
	public void test4() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		
		Assert.assertEquals(0, list.size());
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * A under borqs account
	 * B not under borqs account
	 * A : phone : 12345, 23456
	 * B : phone : 12345
	 * A : email : tian.ke@borqs.com
	 * B : email : tian.ke@borqs.com
	 * 
	*/
	public void test5() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addPhone(Phone.TYPE_MOBILE, "23456", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		
		Assert.assertEquals(0, list.size());
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * A under borqs account
	 * B not under borqs account
	 * They have some phone/email.
	 * A : phone : 12345
	 * B : phone : 12345, 23456
	 * A : email : tian.ke@borqs.com
	 * B : email : tian.ke@borqs.com
	 * 
	*/
	public void test6() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csB.addPhone(Phone.TYPE_MOBILE, "23456", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(1, list.size());
		
		HashSet<Long> set = list.get(0);
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains(ida));
		Assert.assertTrue(set.contains(idb));
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * A under borqs account
	 * B not under borqs account
	 * They have some phone/email.
	 * A : phone : 12345
	 * B : phone : 23456
	 * A : email : tian.ke@borqs.com
	 * B : email : ke.tian@borqs.com
	 * 
	*/
	public void test7() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_MOBILE, "23456", "label", false);
		csB.addEmail(Email.TYPE_HOME, "ke.tian@borqs.com", "label", false);
		co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, list.size());
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(1, newImport.size());
		Assert.assertTrue(newImport.contains(idb));
	}
	
	/*
	 * A under borqs account
	 * B not under borqs account
	 * They have some phone/email.
	 * A : phone : 12345
	 * B : phone : none
	 * A : email : tian.ke@borqs.com
	 * B : email : none
	 * 
	*/
	public void test8() {
		init();
		
		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12345", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
		long idb = co.add(csB);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, list.size());
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
	
	/*
	 * Both A & b are  under Borqs account, A & B are both my borqs friend.
	 * They have same phone's number. 12811047700
	*/
	public void test9() {
		init();

		ContactStruct csA = new ContactStruct();
		csA.setGivenName("Ag");
		csA.setFamilyName("Af");
		csA.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
		csA.addEmail(Email.TYPE_HOME, "tian.keaaa@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long ida = co.add(csA);
		setAsBorqsFriend(ida);
		ContactStruct csB = new ContactStruct();
		csB.setGivenName("Bg");
		csB.setFamilyName("Bf");
		csB.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
		csB.addEmail(Email.TYPE_HOME, "tian.keBBB@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long idb = co.add(csB);
		setAsBorqsFriend(idb);
		ContactStruct csC = new ContactStruct();
		csC.setGivenName("Cg");
		csC.setFamilyName("Cf");
		csC.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
		csC.addEmail(Email.TYPE_HOME, "tian.keCC@borqs.com", "label", false);
		co.setTargetAccount(borqsaccountname, borqsaccounttype);
		long idc = co.add(csC);
		
		ArrayList<HashSet<Long>> list = ContactsManageUtil
				.getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(1, list.size());
		ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil.getMergeContacts(context, borqsaccountname, borqsaccounttype, list);
		
		Assert.assertEquals(0, noNeedUserDeal.size());
		Assert.assertEquals(1, list.size());
		
		HashSet<Long> newImport  = ContactsManageUtil.getNewContacts(context, borqsaccountname, borqsaccounttype);
		Assert.assertEquals(0, newImport.size());
	}
}
