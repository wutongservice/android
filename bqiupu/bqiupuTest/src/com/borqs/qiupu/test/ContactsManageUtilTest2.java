package com.borqs.qiupu.test;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
import android.test.AndroidTestCase;

import com.borqs.contacts.model.ContactStruct;

public class ContactsManageUtilTest2 extends AndroidTestCase {
	private Context context;

	@Override
	protected void setUp() throws Exception {
		context = this.getContext().getApplicationContext();
		super.setUp();
	}

	private void init() {
		context.getContentResolver().delete(
				Uri.parse(RawContacts.CONTENT_URI.toString() + "?"
						+ "caller_is_syncadapter = false"),
				RawContacts._ID + " >= 0", null);
	}

	// 1条需要增加的数据，一条无关数据，合并后应为3条
	public void testService0() {
		DataMaker dm = new DataMaker() {

			@Override
			protected void genImportData() {
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("A0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "13855552222", "label", false);
				cs.addEmail(Email.TYPE_HOME, "A01@borqs.com", "label", false);
				op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
				long id = op.add(cs);
				mImport.add(id);
			}

			@Override
			protected void genOtherData() {
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("D0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "123456789", "label", false);
				cs.addEmail(Email.TYPE_HOME, "D01@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				op.add(cs);
			}

		};
		UTService service = new UTService(dm, context);
		service.Test();
		Assert.assertEquals(3, dm.mData.size());
	}

	// 2播思账户，电话相同，邮件不同，姓名不同，合并结果为1条
	// 并且缺省情况下名字为第一个
	public void testService1() {
		DataMaker dm = new DataMaker() {

			@Override
			protected void genMergeData() {
				HashSet<Long> set = new HashSet<Long>();
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "13811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				long id = op.add(cs);
				set.add(id);

				cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("2");
				cs.addPhone(Phone.TYPE_HOME, "+8613811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C02@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				id = op.add(cs);
				set.add(id);
				mMerge.add(set);
			}

		};
		UTService service = new UTService(dm, context);
		service.Test();
		Assert.assertEquals(1, dm.mData.size());
		ContactStruct cs = dm.mData.values().iterator().next();
		Assert.assertEquals("1", cs.getFamilyName());
	}

	// 2播思账户，电话相同，邮件不同，姓名相同，直接合并为1条
	public void testService2() {
		DataMaker dm = new DataMaker() {

			@Override
			protected void genSimpleData() {
				HashSet<Long> set = new HashSet<Long>();
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("B0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "12811047736", "label", false);
				cs.addEmail(Email.TYPE_HOME, "B01@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				long id = op.add(cs);
				set.add(id);

				cs = new ContactStruct();
				cs.setGivenName("B0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "+8612811047736", "label", false);
				cs.addEmail(Email.TYPE_HOME, "B02@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				id = op.add(cs);
				set.add(id);

				mSimple.add(set);
			}
		};
		UTService service = new UTService(dm, context);
		service.Test();
		Assert.assertEquals(1, dm.mData.size());
	}

	long bid = 0;

	// 1播思账户，1非播思账户，电话相同，邮件不同，姓名不同，合并结果为2条

	public void testService3() {
		DataMaker dm = new DataMaker() {

			@Override
			protected void genMergeData() {
				HashSet<Long> set = new HashSet<Long>();
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "13811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
				op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
				long id = op.add(cs);
				set.add(id);

				cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("2");
				cs.addPhone(Phone.TYPE_HOME, "+8613811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C02@borqs.com", "label", false);
				op.setTargetAccount(borqsaccountname, borqsaccounttype);
				id = op.add(cs);
				bid = id;
				set.add(id);
				mMerge.add(set);
			}

		};
		UTService service = new UTService(dm, context);
		service.Test();
		Assert.assertTrue(dm.mData.containsKey(bid));
		Assert.assertEquals(2, dm.mData.size());
		ContactStruct cs = dm.mData.values().iterator().next();
		Assert.assertEquals("1", cs.getFamilyName());
	}

	// 2非播思账户，电话相同，邮件不同，姓名不同，合并结果为3条

	public void testService4() {
		DataMaker dm = new DataMaker() {

			@Override
			protected void genMergeData() {
				HashSet<Long> set = new HashSet<Long>();
				ContactStruct cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("1");
				cs.addPhone(Phone.TYPE_HOME, "13811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
				op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
				long id = op.add(cs);
				set.add(id);

				cs = new ContactStruct();
				cs.setGivenName("C0");
				cs.setFamilyName("2");
				cs.addPhone(Phone.TYPE_HOME, "+8613811047735", "label", false);
				cs.addEmail(Email.TYPE_HOME, "C02@borqs.com", "label", false);
				op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
				id = op.add(cs);
				bid = id;
				set.add(id);
				mMerge.add(set);
			}

		};
		UTService service = new UTService(dm, context);
		service.Test();
		Assert.assertTrue(dm.mData.containsKey(bid));
		Assert.assertEquals(3, dm.mData.size());
		ContactStruct cs = dm.mData.values().iterator().next();
		Assert.assertEquals("1", cs.getFamilyName());
	}
	// 2播思账户，电话不同，邮件相同，姓名不同，合并结果为1条
		public void testService5() {
			DataMaker dm = new DataMaker() {

				@Override
				protected void genMergeData() {
					HashSet<Long> set = new HashSet<Long>();
					ContactStruct cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("1");
					cs.addPhone(Phone.TYPE_HOME, "13811047736", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(borqsaccountname, borqsaccounttype);
					long id = op.add(cs);
					set.add(id);

					cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("2");
					cs.addPhone(Phone.TYPE_HOME, "+8613811047735", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(borqsaccountname, borqsaccounttype);
					id = op.add(cs);
					set.add(id);
					mMerge.add(set);
				}

			};
			UTService service = new UTService(dm, context);
			service.Test();
			Assert.assertEquals(1, dm.mData.size());
			ContactStruct cs = dm.mData.values().iterator().next();
			Assert.assertEquals("1", cs.getFamilyName());
		}

		// 2播思账户，电话不同，邮件相同，姓名相同，直接合并为1条
		public void testService6() {
			DataMaker dm = new DataMaker() {

				@Override
				protected void genSimpleData() {
					HashSet<Long> set = new HashSet<Long>();
					ContactStruct cs = new ContactStruct();
					cs.setGivenName("B0");
					cs.setFamilyName("1");
					cs.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
					cs.addEmail(Email.TYPE_HOME, "B01@borqs.com", "label", false);
					op.setTargetAccount(borqsaccountname, borqsaccounttype);
					long id = op.add(cs);
					set.add(id);

					cs = new ContactStruct();
					cs.setGivenName("B0");
					cs.setFamilyName("1");
					cs.addPhone(Phone.TYPE_HOME, "+8612811047736", "label", false);
					cs.addEmail(Email.TYPE_HOME, "B01@borqs.com", "label", false);
					op.setTargetAccount(borqsaccountname, borqsaccounttype);
					id = op.add(cs);
					set.add(id);

					mSimple.add(set);
				}
			};
			UTService service = new UTService(dm, context);
			service.Test();
			Assert.assertEquals(1, dm.mData.size());
		}

		// 1播思账户，1非播思账户，电话不同，邮件相同，姓名不同，合并结果为2条

		public void testService7() {
			DataMaker dm = new DataMaker() {

				@Override
				protected void genMergeData() {
					HashSet<Long> set = new HashSet<Long>();
					ContactStruct cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("1");
					cs.addPhone(Phone.TYPE_HOME, "13811047735", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
					long id = op.add(cs);
					set.add(id);

					cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("2");
					cs.addPhone(Phone.TYPE_HOME, "+8613811048735", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C02@borqs.com", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(borqsaccountname, borqsaccounttype);
					id = op.add(cs);
					bid = id;
					set.add(id);
					mMerge.add(set);
				}

			};
			UTService service = new UTService(dm, context);
			service.Test();
			Assert.assertTrue(dm.mData.containsKey(bid));
			Assert.assertEquals(2, dm.mData.size());
			ContactStruct cs = dm.mData.values().iterator().next();
			Assert.assertEquals("1", cs.getFamilyName());
		}

		// 2非播思账户，电话不同，邮件相同，姓名不同，合并结果为3条

		public void testService8() {
			DataMaker dm = new DataMaker() {

				@Override
				protected void genMergeData() {
					HashSet<Long> set = new HashSet<Long>();
					ContactStruct cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("1");
					cs.addPhone(Phone.TYPE_HOME, "13811047735", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
					long id = op.add(cs);
					set.add(id);

					cs = new ContactStruct();
					cs.setGivenName("C0");
					cs.setFamilyName("2");
					cs.addPhone(Phone.TYPE_HOME, "+8613811047731", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C02@borqs.com", "label", false);
					cs.addEmail(Email.TYPE_HOME, "C01@borqs.com", "label", false);
					op.setTargetAccount(notborqsaccountname, notborqsaccounttype);
					id = op.add(cs);
					bid = id;
					set.add(id);
					mMerge.add(set);
				}

			};
			UTService service = new UTService(dm, context);
			service.Test();
			Assert.assertTrue(dm.mData.containsKey(bid));
			Assert.assertEquals(3, dm.mData.size());
			ContactStruct cs = dm.mData.values().iterator().next();
			Assert.assertEquals("1", cs.getFamilyName());
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
	 * public void test1() { init(); ContactStruct csA,csB;
	 * 
	 * //全Borqs账户，名字不同, 电话相同，邮件不同 csA = new ContactStruct();
	 * csA.setGivenName("A0"); csA.setFamilyName("1");
	 * csA.addPhone(Phone.TYPE_HOME, "1795112811047735", "label", false);
	 * csA.addEmail(Email.TYPE_HOME, "A0@borqs.com", "label", false);
	 * co.setTargetAccount(borqsaccountname, borqsaccounttype); co.add(csA);
	 * 
	 * csB = new ContactStruct(); csB.setGivenName("A0");
	 * csB.setFamilyName("2"); csB.addPhone(Phone.TYPE_HOME, "+8612811047735",
	 * "label", false); csB.addEmail(Email.TYPE_HOME, "A0@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * co.add(csB);
	 * 
	 * //全Borqs账户，名字不同, 电话不同，邮件相同
	 * 
	 * csA = new ContactStruct(); csA.setGivenName("A1");
	 * csA.setFamilyName("1"); csA.addPhone(Phone.TYPE_HOME, "1795112811047736",
	 * "label", false); csA.addEmail(Email.TYPE_HOME, "A1@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * co.add(csA);
	 * 
	 * csB = new ContactStruct(); csB.setGivenName("A1");
	 * csB.setFamilyName("2"); csB.addPhone(Phone.TYPE_HOME, "+8612811047739",
	 * "label", false); csB.addEmail(Email.TYPE_HOME, "A1@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * co.add(csB);
	 * 
	 * //一个非borqs账户 csA = new ContactStruct(); csA.setGivenName("B0");
	 * csA.setFamilyName("1"); csA.addPhone(Phone.TYPE_HOME, "13855552222",
	 * "label", false); csA.addEmail(Email.TYPE_HOME, "b01@borqs.com", "label",
	 * false); co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
	 * co.add(csA); }
	 */
	/*
	 * Both A & b are under Borqs account. They have same phone's number.
	 * 12811047700
	 * 
	 * public void test1() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12811047735",
	 * "label", false); csA.addEmail(Email.TYPE_HOME, "tian.keaaa@borqs.com",
	 * "label", false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * long ida = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "12811047735",
	 * "label", false); csB.addEmail(Email.TYPE_HOME, "tian.keBBB@borqs.com",
	 * "label", false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * long idb = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * 
	 * ArrayList<HashSet<Long>> noNeedUserDeal =
	 * ContactsManageUtil.getMergeContacts(context, borqsaccountname,
	 * borqsaccounttype, list);
	 * 
	 * Assert.assertEquals(0, noNeedUserDeal.size()); Assert.assertEquals(1,
	 * list.size()); HashSet<Long> set = list.get(0);
	 * Assert.assertTrue(set.contains(ida));
	 * Assert.assertTrue(set.contains(idb));
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * Both A & b are under Borqs account. They have same email. abc@borqs.com
	 * 
	 * public void test2() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "23456", "label",
	 * false); csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long idb
	 * = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil
	 * .getMergeContacts(context, borqsaccountname, borqsaccounttype, list);
	 * 
	 * Assert.assertEquals(0, noNeedUserDeal.size()); Assert.assertEquals(1,
	 * list.size()); HashSet<Long> set = list.get(0);
	 * Assert.assertTrue(set.contains(ida));
	 * Assert.assertTrue(set.contains(idb));
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * Both A & b are under Borqs account. A : phone : 12345 B : phone : 23456 A
	 * : email : tian.ke@borqs.com B : email : tian.kebbb@borqs.com They are
	 * different people.
	 * 
	 * public void test3() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "23456", "label",
	 * false); csB.addEmail(Email.TYPE_HOME, "tian.kebbb@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long idb
	 * = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * ArrayList<HashSet<Long>> noNeedUserDeal = ContactsManageUtil
	 * .getMergeContacts(context, borqsaccountname, borqsaccounttype, list);
	 * 
	 * Assert.assertEquals(0, noNeedUserDeal.size()); Assert.assertEquals(0,
	 * list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * A under borqs account B not under borqs account They have some
	 * phone/email. A : phone : 12345 B : phone : 12345 A : email :
	 * tian.ke@borqs.com B : email : tian.ke@borqs.com
	 * 
	 * 
	 * public void test4() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
	 * long idb = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * 
	 * Assert.assertEquals(0, list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * A under borqs account B not under borqs account A : phone : 12345, 23456
	 * B : phone : 12345 A : email : tian.ke@borqs.com B : email :
	 * tian.ke@borqs.com
	 * 
	 * 
	 * public void test5() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addPhone(Phone.TYPE_MOBILE, "23456", "label", false);
	 * csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
	 * co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida =
	 * co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(notborqsaccountname, notborqsaccounttype);
	 * long idb = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * 
	 * Assert.assertEquals(0, list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * A under borqs account B not under borqs account They have some
	 * phone/email. A : phone : 12345 B : phone : 12345, 23456 A : email :
	 * tian.ke@borqs.com B : email : tian.ke@borqs.com
	 * 
	 * 
	 * public void test6() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csB.addPhone(Phone.TYPE_MOBILE, "23456", "label", false);
	 * csB.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label", false);
	 * co.setTargetAccount(notborqsaccountname, notborqsaccounttype); long idb =
	 * co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * Assert.assertEquals(1, list.size());
	 * 
	 * HashSet<Long> set = list.get(0); Assert.assertEquals(2, set.size());
	 * Assert.assertTrue(set.contains(ida));
	 * Assert.assertTrue(set.contains(idb));
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * A under borqs account B not under borqs account They have some
	 * phone/email. A : phone : 12345 B : phone : 23456 A : email :
	 * tian.ke@borqs.com B : email : ke.tian@borqs.com
	 * 
	 * 
	 * public void test7() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); csB.addPhone(Phone.TYPE_MOBILE, "23456",
	 * "label", false); csB.addEmail(Email.TYPE_HOME, "ke.tian@borqs.com",
	 * "label", false); co.setTargetAccount(notborqsaccountname,
	 * notborqsaccounttype); long idb = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * Assert.assertEquals(0, list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(1,
	 * newImport.size()); Assert.assertTrue(newImport.contains(idb)); }
	 * 
	 * 
	 * A under borqs account B not under borqs account They have some
	 * phone/email. A : phone : 12345 B : phone : none A : email :
	 * tian.ke@borqs.com B : email : none
	 * 
	 * 
	 * public void test8() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12345", "label",
	 * false); csA.addEmail(Email.TYPE_HOME, "tian.ke@borqs.com", "label",
	 * false); co.setTargetAccount(borqsaccountname, borqsaccounttype); long ida
	 * = co.add(csA);
	 * 
	 * ContactStruct csB = new ContactStruct(); csB.setGivenName("Bg");
	 * csB.setFamilyName("Bf"); co.setTargetAccount(notborqsaccountname,
	 * notborqsaccounttype); long idb = co.add(csB);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * Assert.assertEquals(0, list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 * 
	 * 
	 * Both A & b are under Borqs account, A & B are both my borqs friend. They
	 * have same phone's number. 12811047700
	 * 
	 * public void test9() { init();
	 * 
	 * ContactStruct csA = new ContactStruct(); csA.setGivenName("Ag");
	 * csA.setFamilyName("Af"); csA.addPhone(Phone.TYPE_HOME, "12811047735",
	 * "label", false); csA.addEmail(Email.TYPE_HOME, "tian.keaaa@borqs.com",
	 * "label", false); co.setTargetAccount(borqsaccountname, borqsaccounttype);
	 * long ida = co.add(csA); setAsBorqsFriend(ida); ContactStruct csB = new
	 * ContactStruct(); csB.setGivenName("Bg"); csB.setFamilyName("Bf");
	 * csB.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
	 * csB.addEmail(Email.TYPE_HOME, "tian.keBBB@borqs.com", "label", false);
	 * co.setTargetAccount(borqsaccountname, borqsaccounttype); long idb =
	 * co.add(csB); setAsBorqsFriend(idb); ContactStruct csC = new
	 * ContactStruct(); csC.setGivenName("Cg"); csC.setFamilyName("Cf");
	 * csC.addPhone(Phone.TYPE_HOME, "12811047735", "label", false);
	 * csC.addEmail(Email.TYPE_HOME, "tian.keCC@borqs.com", "label", false);
	 * co.setTargetAccount(borqsaccountname, borqsaccounttype); long idc =
	 * co.add(csC);
	 * 
	 * ArrayList<HashSet<Long>> list = ContactsManageUtil
	 * .getDuplicatedContacts(context, borqsaccountname, borqsaccounttype);
	 * Assert.assertEquals(1, list.size()); ArrayList<HashSet<Long>>
	 * noNeedUserDeal = ContactsManageUtil.getMergeContacts(context,
	 * borqsaccountname, borqsaccounttype, list);
	 * 
	 * Assert.assertEquals(0, noNeedUserDeal.size()); Assert.assertEquals(1,
	 * list.size());
	 * 
	 * HashSet<Long> newImport = ContactsManageUtil.getNewContacts(context,
	 * borqsaccountname, borqsaccounttype); Assert.assertEquals(0,
	 * newImport.size()); }
	 */
}
