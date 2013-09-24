/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.test;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.test.AndroidTestCase;
import com.borqs.pim.jcontact.JContact;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.ImData;
import com.borqs.sync.client.vdata.card.ContactStruct.OrganizationData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;
import com.borqs.sync.client.vdata.card.ContactStruct.PostalData;
import com.borqs.sync.client.vdata.card.ContactStruct.WebsiteData;
import com.borqs.sync.client.vdata.card.JContactConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * test request is depended on the "VCard_All_Field_JSON.txt"
 * 
 * @author b211
 * 
 */
public class ConverterTest extends AndroidTestCase {

//	public void testConvertToContactStruct() {
//		InputStream fs = null;
//		BufferedReader br = null;
//		try {
//			fs = getContext().getResources().openRawResource(
//					R.raw.vard_all_field_json);
//			byte[] bytes = new byte[fs.available()];
//			fs.read(bytes);
//			String jCard = EncodingUtils.getString(bytes, "UTF-8");
//			System.out.println(jCard);
//			assertTrue(jCard.length() > 0);
//			JContact jContact = JContact.fromJsonString(jCard);
//			assertNotNull(jContact);
//			JContactConverter jc = new JContactConverter();
//			ContactStruct contact = jc.convertToContactStruct(jContact);
//			checkContact(contact);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				fs.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public void testConvertToJson() {
		ContactStruct contact = new ContactStruct();
		addContactStructNote(contact);
		addContactStructPhone(contact);
		addContactStructEmail(contact);
		addContactStructName(contact);
		addContactStructWebPage(contact);
		addContactStructAddress(contact);
		addContactStructXTags(contact);
		addContactStructOrg(contact);
		addContactStructBirthday(contact);
		addContactStructIM(contact);

		JContactConverter jcc = new JContactConverter();
		String json = jcc.convertToJson(contact);
		assertTrue(json.length() > 1);
		JContact jContact = JContact.fromJsonString(json);
		contact = jcc.convertToContactStruct(jContact);
		checkContact(contact);
	}

	private void addContactStructIM(ContactStruct contact) {
		contact.addIm(Im.PROTOCOL_CUSTOM, "fetion", "fetion", "FETION", false);
		contact.addIm(Im.PROTOCOL_CUSTOM, "custom im", "custom", null, false);
		contact.addIm(Im.PROTOCOL_AIM, "aim", null, false);
		contact.addIm(Im.PROTOCOL_GOOGLE_TALK, "google talk", null, null,
				false);
		contact.addIm(Im.PROTOCOL_MSN, "msn", null, null, false);
		contact.addIm(Im.PROTOCOL_ICQ, "icq", null, null, false);
		contact.addIm(Im.PROTOCOL_JABBER, "jabber", null, null, false);
		contact.addIm(Im.PROTOCOL_NETMEETING, "netmeeting", null, null,
				false);
		contact.addIm(Im.PROTOCOL_QQ, "qq", null, null, false);
		contact.addIm(Im.PROTOCOL_SKYPE, "skype", null, null, false);
		contact.addIm(Im.PROTOCOL_YAHOO, "yahoo", null, null, false);
	}

	private void addContactStructBirthday(ContactStruct contact) {
		contact.setBirthday("2011-08-23");
	}

	private void addContactStructOrg(ContactStruct contact) {
		contact.addOrganization(Organization.TYPE_OTHER, "other company",
				"other title", null, null, false);
		contact.addOrganization(Organization.TYPE_WORK, "work company",
				"work title", null, null, false);
		contact.addOrganization(Organization.TYPE_CUSTOM, "custom company",
				"custom title", null, "custom", false);
	}

	private void addContactStructXTags(ContactStruct contact) {
		// block
		contact.setBlock(false);
		// ringtone
		// contact.setRingtoneFile(getXValue(jContact, contact,
		// JXTag.X_RINGTONG));
		// photo
		// group
		contact.addGroup("Friends");
		contact.addGroup("Family");
		// STARRED
		contact.setFavorite(false);
		// account type
		contact.setAccountType("vnd.ophoneos.contact.phone");
	}

	private void addContactStructAddress(ContactStruct contact) {
		List<String> propValueList = new ArrayList<String>();
		propValueList.add(null);// pobox
		propValueList.add(null);// extendedAddress
		propValueList.add("home street");// street
		propValueList.add("home city");// localty
		propValueList.add("home provience");// region
		propValueList.add("1111");// postalCode
		propValueList.add(null);// country
		contact.addPostal(StructuredPostal.TYPE_HOME, propValueList, null,
				false);

		propValueList = new ArrayList<String>();
		propValueList.add(null);// pobox
		propValueList.add(null);// extendedAddress
		propValueList.add("other street");// street
		propValueList.add("other city");// localty
		propValueList.add("other provience");// region
		propValueList.add("1111111");// postalCode
		propValueList.add(null);// country
		contact.addPostal(StructuredPostal.TYPE_OTHER, propValueList, null,
				false);

		propValueList = new ArrayList<String>();
		propValueList.add(null);// pobox
		propValueList.add(null);// extendedAddress
		propValueList.add("work street");// street
		propValueList.add("work city");// localty
		propValueList.add("work provience");// region
		propValueList.add("11111");// postalCode
		propValueList.add(null);// country
		contact.addPostal(StructuredPostal.TYPE_WORK, propValueList, null,
				false);

		propValueList = new ArrayList<String>();
		propValueList.add(null);// pobox
		propValueList.add(null);// extendedAddress
		propValueList.add("custom street");// street
		propValueList.add("custom city");// localty
		propValueList.add("custom provience");// region
		propValueList.add("11111");// postalCode
		propValueList.add(null);// country
		contact.addPostal(StructuredPostal.TYPE_CUSTOM, propValueList,
				"custom", false);
	}

	private void addContactStructWebPage(ContactStruct contact) {
		contact.addWebsite(Website.TYPE_BLOG, "blog.com", null, false);
		contact.addWebsite(Website.TYPE_FTP, "ftp.com", null, false);
		contact.addWebsite(Website.TYPE_HOME, "home.com", null, false);
		contact.addWebsite(Website.TYPE_HOMEPAGE, "homepage.com", null, false);
		contact.addWebsite(Website.TYPE_OTHER, "other.com", null, false);
		contact.addWebsite(Website.TYPE_PROFILE, "profile.com", null, false);
		contact.addWebsite(Website.TYPE_WORK, "work.com", null, false);
		contact.addWebsite(Website.TYPE_CUSTOM, "custom.com", "custom", false);
	}

	private void addContactStructName(ContactStruct contact) {
		List<String> nameList = new ArrayList<String>();
		contact.setFirstName("firstname");
        contact.setMiddleName("middlename");
        contact.setLastName("lastname");
        contact.setPrefix("prefix");
        contact.setSuffix("suffix");
        contact.setPhoneticFirstName("firstnamepinyin");
        contact.setPhoneticLastName("lastnamepinyin");
        contact.setPhoneticMiddleName("middlenamepinyin");


		contact.addNickNameList("nickname");
	}

	private void addContactStructEmail(ContactStruct contact) {
		contact.addEmail(Email.TYPE_HOME, "home@home.com", null, false);
		contact.addEmail(Email.TYPE_MOBILE, "mobile@mobile.com", null, false);
		contact.addEmail(Email.TYPE_OTHER, "other@other.com", null, false);
		contact.addEmail(Email.TYPE_WORK, "work@work.com", null, false);
		contact.addEmail(Email.TYPE_CUSTOM, "custom@custom.com", "custom",
				false);
	}

	private void addContactStructPhone(ContactStruct contact) {
		PhoneData pd = new PhoneData(Phone.TYPE_HOME, "1", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_HOME, "1", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_MOBILE, "2", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_WORK, "3", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_FAX_WORK, "4", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_FAX_HOME, "5", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_PAGER, "6", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_CUSTOM, "7", "custom", false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_CALLBACK, "8", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_COMPANY_MAIN, "10", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_ISDN, "11", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_MAIN, "12", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_OTHER_FAX, "13", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_RADIO, "14", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_TELEX, "15", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_TTY_TDD, "16", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_WORK_MOBILE, "17", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_WORK_PAGER, "18", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_ASSISTANT, "19", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_MMS, "20", null, false);
		contact.addPhone(pd);
		pd = new PhoneData(Phone.TYPE_OTHER, "21", null, false);
		contact.addPhone(pd);
	}

	private void addContactStructNote(ContactStruct contact) {
		contact.addNote("note");
	}

	private void checkContact(ContactStruct contact) {
		checkName(contact);
		checkBirthday(contact);
		checkName(contact);
		checkPhoto(contact);
		checkPhone(contact);
		checkEmail(contact);
		checkAddress(contact);
		checkOrg(contact);
		checkIM(contact);
		checkWebPage(contact);
	}

	private void checkWebPage(ContactStruct contact) {
		List<WebsiteData> webList = contact.getWebsiteList();
		assertNotNull(webList);
		assertEquals(8, webList.size());
		for (WebsiteData websiteData : webList) {
			assertNotNull(websiteData);
			switch (websiteData.type) {
			case Website.TYPE_BLOG:
				assertEquals("blog.com", websiteData.data);
				break;
			case Website.TYPE_PROFILE:
				assertEquals("profile.com", websiteData.data);
				break;
			case Website.TYPE_HOME:
				assertEquals("home.com", websiteData.data);
				break;
			case Website.TYPE_HOMEPAGE:
				assertEquals("homepage.com", websiteData.data);
				break;
			case Website.TYPE_WORK:
				assertEquals("work.com", websiteData.data);
				break;
			case Website.TYPE_FTP:
				assertEquals("ftp.com", websiteData.data);
				break;
			case Website.TYPE_OTHER:
				assertEquals("other.com", websiteData.data);
				break;
			case Website.TYPE_CUSTOM:
				assertEquals("custom.com", websiteData.data);
				break;
			}
		}
	}

	private void checkIM(ContactStruct contact) {
		List<ImData> imList = contact.getImList();
		assertNotNull(imList);
		assertEquals(11, imList.size());
		for (ImData imData : imList) {
			assertNotNull(imData);
			switch (imData.type) {
			case Im.PROTOCOL_AIM:
				assertEquals("aim", imData.data);
				break;
			case Im.PROTOCOL_CUSTOM:
				if ("fetion".equals(imData.label)) {
					assertEquals("fetion", imData.data);
					assertEquals("FETION", imData.customProtocol);
				} else {
					assertEquals("custom im", imData.data);
				}
				break;
			case Im.PROTOCOL_GOOGLE_TALK:
				assertEquals("google talk", imData.data);
				break;
			case Im.PROTOCOL_ICQ:
				assertEquals("icq", imData.data);
				break;
			case Im.PROTOCOL_JABBER:
				assertEquals("jabber", imData.data);
				break;
			case Im.PROTOCOL_MSN:
				assertEquals("msn", imData.data);
				break;
			case Im.PROTOCOL_NETMEETING:
				assertEquals("netmeeting", imData.data);
				break;
			case Im.PROTOCOL_QQ:
				assertEquals("qq", imData.data);
				break;
			case Im.PROTOCOL_SKYPE:
				assertEquals("skype", imData.data);
				break;
			case Im.PROTOCOL_YAHOO:
				assertEquals("yahoo", imData.data);
				break;
			}
		}
	}

	private void checkOrg(ContactStruct contact) {
		List<OrganizationData> orgList = contact.getOrganizationList();
		assertNotNull(orgList);
		assertEquals(3, orgList.size());
		for (OrganizationData organizationData : orgList) {
			assertNotNull(organizationData);
			switch (organizationData.type) {
			case Organization.TYPE_OTHER:
				assertEquals("other company", organizationData.companyName);
				assertEquals("other title", organizationData.positionName);
				break;
			case Organization.TYPE_WORK:
				assertEquals("work company", organizationData.companyName);
				assertEquals("work title", organizationData.positionName);
				break;
			case Organization.TYPE_CUSTOM:
				assertEquals("custom", organizationData.label);
				assertEquals("custom company", organizationData.companyName);
				assertEquals("custom title", organizationData.positionName);
				break;
			}
		}
	}

	private void checkAddress(ContactStruct contact) {
		List<PostalData> addressList = contact.getPostalList();
		assertNotNull(addressList);
		assertEquals(4, addressList.size());
		for (PostalData address : addressList) {
			assertNotNull(address);
			switch (address.type) {
			case StructuredPostal.TYPE_HOME:
				assertEquals("1111", address.postalCode);
				assertEquals("home provience", address.region);
				assertEquals("home city", address.localty);
				assertEquals("home street", address.street);
				break;
			case StructuredPostal.TYPE_WORK:
				assertEquals("11111", address.postalCode);
				assertEquals("work provience", address.region);
				assertEquals("work city", address.localty);
				assertEquals("work street", address.street);
				break;
			case StructuredPostal.TYPE_OTHER:
				assertEquals("1111111", address.postalCode);
				assertEquals("other provience", address.region);
				assertEquals("other city", address.localty);
				assertEquals("other street", address.street);
				break;
			case StructuredPostal.TYPE_CUSTOM:
				assertEquals("11111", address.postalCode);
				assertEquals("custom provience", address.region);
				assertEquals("custom city", address.localty);
				assertEquals("custom street", address.street);
				assertEquals("custom", address.label);
				break;
			}
		}
	}

	private void checkEmail(ContactStruct contact) {
		List<EmailData> emailList = contact.getEmailList();
		assertNotNull(emailList);
		assertEquals(5, emailList.size());
		for (EmailData emailData : emailList) {
			assertNotNull(emailData);
			switch (emailData.type) {
			case Email.TYPE_HOME:
				assertEquals("home@home.com", emailData.data);
				break;
			case Email.TYPE_WORK:
				assertEquals("work@work.com", emailData.data);
				break;
			case Email.TYPE_MOBILE:
				assertEquals("mobile@mobile.com", emailData.data);
				break;
			case Email.TYPE_CUSTOM:
				assertEquals("custom@custom.com", emailData.data);
				assertEquals("custom", emailData.label);
				break;
			case Email.TYPE_OTHER:
				assertEquals("other@other.com", emailData.data);
				break;
			}
		}
	}

	private void checkPhone(ContactStruct contact) {
		List<PhoneData> phoneList = contact.getPhoneList();
		assertNotNull(phoneList);
		assertEquals(21, phoneList.size());
		for (PhoneData phoneData : phoneList) {
			assertNotNull(phoneData);
			switch (phoneData.type) {
			case Phone.TYPE_HOME:
				assertEquals("1", phoneData.data);
				break;
			case Phone.TYPE_MOBILE:
				assertEquals("2", phoneData.data);
				break;
			case Phone.TYPE_WORK:
				assertEquals("3", phoneData.data);
				break;
			case Phone.TYPE_FAX_WORK:
				assertEquals("4", phoneData.data);
				break;
			case Phone.TYPE_FAX_HOME:
				assertEquals("5", phoneData.data);
				break;
			case Phone.TYPE_PAGER:
				assertEquals("6", phoneData.data);
				break;
			case Phone.TYPE_CUSTOM:
				assertEquals("7", phoneData.data);
				assertEquals("custom", phoneData.label);
				break;
			case Phone.TYPE_CALLBACK:
				assertEquals("8", phoneData.data);
				break;
			case Phone.TYPE_CAR:
				assertEquals("9", phoneData.data);
				break;
			case Phone.TYPE_COMPANY_MAIN:
				assertEquals("10", phoneData.data);
				break;
			case Phone.TYPE_ISDN:
				assertEquals("11", phoneData.data);
				break;
			case Phone.TYPE_MAIN:
				assertEquals("12", phoneData.data);
				break;
			case Phone.TYPE_OTHER_FAX:
				assertEquals("13", phoneData.data);
				break;
			case Phone.TYPE_RADIO:
				assertEquals("14", phoneData.data);
				break;
			case Phone.TYPE_TELEX:
				assertEquals("15", phoneData.data);
				break;
			case Phone.TYPE_TTY_TDD:
				assertEquals("16", phoneData.data);
				break;
			case Phone.TYPE_WORK_MOBILE:
				assertEquals("17", phoneData.data);
				break;
			case Phone.TYPE_WORK_PAGER:
				assertEquals("18", phoneData.data);
				break;
			case Phone.TYPE_ASSISTANT:
				assertEquals("19", phoneData.data);
				break;
			case Phone.TYPE_MMS:
				assertEquals("20", phoneData.data);
				break;
			case Phone.TYPE_OTHER:
				assertEquals("21", phoneData.data);
				break;
			}
		}
	}

	private void checkPhoto(ContactStruct contact) {
		assertTrue(contact.getPhotoList().size() == 1);
		PhotoData pd = contact.getPhotoList().get(0);
		assertNull(pd.photoBytes);
	}

	private void checkBirthday(ContactStruct contact) {
		assertEquals("2011-08-23", contact.getBirthday());
	}

	private void checkName(ContactStruct contact) {
		assertEquals("firstname", contact.getFirstName());
		assertEquals("middlename", contact.getMiddleName());
		assertEquals("lastname", contact.getLastName());
		assertEquals("nickname", contact.getNickNameList().get(0));
		assertEquals("suffix", contact.getSuffix());
	}

}
