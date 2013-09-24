package com.borqs.sync.ds.datastore.contacts;

import java.util.ArrayList;
import java.util.List;

import android.provider.ContactsContract.CommonDataKinds;
import android.text.TextUtils;

import com.borqs.sync.client.vdata.card.AddressFieldsParser;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.client.vdata.card.ContactStruct.EmailData;
import com.borqs.sync.client.vdata.card.ContactStruct.OrganizationData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhoneData;
import com.borqs.sync.client.vdata.card.ContactStruct.PhotoData;
import com.borqs.sync.client.vdata.card.ContactStruct.PostalData;
import com.borqs.sync.ds.datastore.GeneralWordsUtil;
import com.borqs.sync.ds.datastore.IFieldsInterface;
import com.borqs.syncml.ds.imp.common.Util;

public class ContactFieldsProcess implements IFieldsInterface<ContactStruct> {
	// contact max words
	private static final int NAME = 450;
	private static final int BIRTHDAY = 8;
	private static final int MOBILE = 128;
	private static final int TEL = 128;
	private static final int FAX = 128;
	private static final int TEL_VEDIO = 128;
	private static final int MOBILE_VIDEO = 128;
	private static final int EMAIL = 450;
	private static final int PAGE = 500;
	private static final int COMPANY = 380;
	private static final int TITLE = 450;
	private static final int ROLE = 192;
	private static final int ADR = 910;
	private static final int NOTE = 2000;
	private static final int HOME_MOBILE = 128;
	private static final int HOME_TEL = 128;
	private static final int HOME_FAX = 128;
	private static final int HOME_TEL_VEDIO = 128;
	private static final int HOME_EMAIL = 450;
	private static final int HOME_PAGE = 500;
	private static final int HOME_ADR = 910;
	private static final int OFFICE_MOBILE = 128;
	private static final int OFFICE_TEL = 128;
	private static final int OFFICE_FAX = 128;
	private static final int OFFICE_TEL_VEDIO = 128;
	private static final int OFFICE_EMAIL = 450;
	private static final int COMPANY_PAGE = 500;
	private static final int COM_ADR = 910;
	private static final int PHOTO = 4000;
	private static final int X_PROPERTY = 300;
	
	/*
	 * deal with contact fields
	 */
	public ContactStruct structProcess(ContactStruct con){
	    if(true){
	        return con;
	    }
		if(con == null){
			return null;
		}
		ContactStruct returnContact = new ContactStruct();
		// name： family name, given name
		setNameList(con, returnContact);
		
		String tmpValue;
		// nickName
		if(con.getNickNameList() != null && con.getNickNameList().size() > 0){
			for (String nickName : con.getNickNameList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(nickName, NAME, true, false);
				returnContact.addNickNameList(tmpValue);
			}
		}
		// notes
		if(con.getNotes() != null && con.getNotes().size() > 0){
			tmpValue = GeneralWordsUtil.getValueByMaxLength(con.getNotes().get(0), NOTE, true, false);
			returnContact.addNote(tmpValue);
		}
		// ringtone
//		tmpValue = con.getRingtoneFile();
//		if(GeneralWordsUtil.getEncodingLength(tmpValue, null, true, false) > X_PROPERTY){
//			// when beyond the maxlength, send nothing
////			con.setRingtoneFile("");
//			tmpValue = "";
//		}
//		returnContact.setRingtoneFile(tmpValue);
		// phone
		setPhoneList(con, returnContact);
		// org
		//surpport ; / escapse
		setOrg(con, returnContact);
		// email
		setEmail(con, returnContact);
		// im
		setIM(con, returnContact);
		// adr
		//surpport ; / escapse
		setAdr(con, returnContact);
		// birthday,block
		returnContact.setBirthday(con.getBirthday());
		returnContact.setBlock(con.isBlock());
		// photo
		if(con.getPhotoList() !=null && con.getPhotoList().size() > 0){
			for(PhotoData photo : con.getPhotoList()){
				returnContact.addPhotoBytes(photo.formatName, photo.photoBytes);
			}
		}
		//group
		if(con.getGroupList() != null && con.getGroupList().size() > 0){
			for (String group : con.getGroupList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(group, X_PROPERTY, true, false);
				returnContact.addGroup(tmpValue);
			}
		}
		//url
		setUrl(con,returnContact);
		//xname
		setXName(con,returnContact);
		
		//account type
		returnContact.setAccountType(con.getAccountType());
		//borqsid
		returnContact.setBorqsUid(con.getBorqsUid());
		returnContact.setBorqsName(con.getBorqsName());
		return returnContact;
	}

	/*
	 * judge and set name
	 */
	private void setNameList(ContactStruct con, ContactStruct returnContact){
		String tmpValue;
		String valueByMaxLength;
		// family name
		int nameLength = NAME;
		tmpValue = con.getLastName();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setLastName(valueByMaxLength);
			return;
		}
		returnContact.setLastName(tmpValue);
		//given name
		nameLength = nameLength - GeneralWordsUtil.getEncodingLength(tmpValue + ";", null, true, false);
		tmpValue = con.getFirstName();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setFirstName(valueByMaxLength);
			return;
		} 
		returnContact.setFirstName(tmpValue);
//		//middle name
		nameLength = nameLength - GeneralWordsUtil.getEncodingLength(tmpValue + ";", null, true, false);
		tmpValue = con.getMiddleName();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setMiddleName(valueByMaxLength);
			return;
		}
		returnContact.setMiddleName(tmpValue);
	}

	/*
	 * judge and set phone number
	 */
	private void setPhoneList(ContactStruct con, ContactStruct returnContact){
		if(con.getPhoneList() != null && con.getPhoneList().size() > 0){
			String tmpValue;
			for (ContactStruct.PhoneData phone : con.getPhoneList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(phone.data, TEL, true, false);
				// only support digital,(,),*, #, +, P or p, W or w
				tmpValue = Util.regexPhone(tmpValue);
				returnContact.addPhone(phone.type, tmpValue, phone.label, phone.isPrimary);
			}
		}
	}
	
	/*
	 * judge and set org
	 */
	private void setOrg(ContactStruct con, ContactStruct returnContact){
		if (con.getOrganizationList() != null
				&& con.getOrganizationList().size() > 0) {
			String tmpValue;
			String valueByMaxLength;
			boolean firstWorkOrg = true;
			String company;
			String title;
			String department;
			for (ContactStruct.OrganizationData org : con.getOrganizationList()) {
				if(firstWorkOrg && org.type == CommonDataKinds.Organization.TYPE_WORK){
					// company 
					company = GeneralWordsUtil.getValueByMaxLength(org.companyName, COMPANY, true, false);
					// title
					title = GeneralWordsUtil.getValueByMaxLength(org.positionName, TITLE, true, false);
					// department
					department = GeneralWordsUtil.getValueByMaxLength(org.department, ROLE, true, false);
					returnContact.addOrganization(org.type, company, title, department, org.isPrimary);
					firstWorkOrg = false;
				}else{
					// X-BORQS-ORG-TITLE-GROUPS
					// company
					tmpValue = org.companyName;
					int preBytes = GeneralWordsUtil.getEncodingLength(getTypeString(org.type) + ";", null, true, true);
					int maxlength = X_PROPERTY - preBytes;
					valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, maxlength, true, true);
					if(tmpValue != null &&
							!tmpValue.equals(valueByMaxLength)){
						returnContact.addOrganization(org.type, valueByMaxLength, null, null,org.label, org.isPrimary);
						continue;
					}
					// department
					tmpValue = org.department;
					preBytes = GeneralWordsUtil.getEncodingLength(org.companyName + ";", null, true, true);
					maxlength = maxlength - preBytes;
					valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, maxlength, true, true);
					if(tmpValue != null && !tmpValue.equals(valueByMaxLength)){
						returnContact.addOrganization(org.type, org.companyName, null, valueByMaxLength,org.label, org.isPrimary);
						continue;
					}
					// title
					tmpValue = org.positionName;
					preBytes = GeneralWordsUtil.getEncodingLength(org.department + ";", null, true, true);
					maxlength = maxlength - preBytes;
					valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, maxlength, true, true);
					if(tmpValue != null && !tmpValue.equals(valueByMaxLength)){
						returnContact.addOrganization(org.type, org.companyName, valueByMaxLength, org.department,org.label, org.isPrimary);
						continue;
					}
					returnContact.addOrganization(org.type, org.companyName, org.positionName, org.department,org.label, org.isPrimary);
				}
				
			}
		}
	}
	private String getTypeString(int type) {
		switch (type) {
		case CommonDataKinds.Organization.TYPE_WORK:
			return "WORK";
		case CommonDataKinds.Organization.TYPE_OTHER:
		case CommonDataKinds.Organization.TYPE_CUSTOM:
		default:
			return "OTHER";
		}
	}
	
	/*
	 * judge and set email
	 */
	private void setEmail(ContactStruct con, ContactStruct returnContact){
		if(con.getEmailList() != null && con.getEmailList().size() > 0){
			String tmpValue;
			for (ContactStruct.EmailData email : con.getEmailList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(email.data, EMAIL, true, false);
				returnContact.addEmail(email.type, tmpValue, email.label, email.isPrimary);
			}
		}
	}
	
	/*
	 * judge and set im
	 */
	private void setIM(ContactStruct con, ContactStruct returnContact){
		if(con.getImList() != null && con.getImList().size() > 0){
			String tmpValue;
			for (ContactStruct.ImData im : con.getImList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(im.data, X_PROPERTY, true, false);
				returnContact.addIm(im.type, tmpValue, im.label,im.customProtocol, im.isPrimary);
			}
		}
	}
	
	/*
	 * judge and set adr
	 */
	private void setAdr(ContactStruct con, ContactStruct returnContact){
		if(con.getPostalList() != null && con.getPostalList().size() > 0){
			String tmpValue;
			for (ContactStruct.PostalData adr : con.getPostalList()) {
				AddressFieldsParser adrParser = new AddressFieldsParser(adr.pobox, adr.extendedAddress, adr.street, adr.localty, adr.region, adr.postalCode, adr.country);
				tmpValue = adrParser.getFullData();
				tmpValue = GeneralWordsUtil.getValueByMaxLength(tmpValue, ADR, true, false);
				AddressFieldsParser adrCombineParser = new AddressFieldsParser(tmpValue);
				List<String> adrValue=new ArrayList<String>();
				adrValue.add(adrCombineParser.getMailbox());
				adrValue.add(adrCombineParser.getDetail());
				adrValue.add(adrCombineParser.getStreet());
				adrValue.add(adrCombineParser.getLocality());
				adrValue.add(adrCombineParser.getRegion());
				adrValue.add(adrCombineParser.getPostalCode());
				adrValue.add(adrCombineParser.getCountry());
				returnContact.addPostal(adr.type, adrValue, adr.label, adr.isPrimary);
			}
		}
	}
	
	/*
	 * judge and set url
	 */
	private void setUrl(ContactStruct con, ContactStruct returnContact){
		if(con.getWebsiteList() != null && con.getWebsiteList().size() > 0){
			String tmpValue;
			for (ContactStruct.WebsiteData url : con.getWebsiteList()) {
				tmpValue = GeneralWordsUtil.getValueByMaxLength(url.data, X_PROPERTY, true, false);
				returnContact.addWebsite(url.type, tmpValue, url.label, url.isPrimary);
			}
		}
	}
	
	/*
	 * judge and set xname
	 */
	private void setXName(ContactStruct con, ContactStruct returnContact){
		String tmpValue;
		String valueByMaxLength;
		//X-N
		// middle name
		int nameLength = X_PROPERTY;
		tmpValue = con.getMiddleName();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setMiddleName(valueByMaxLength);
			return;
		}
		returnContact.setMiddleName(tmpValue);
		//pre
		nameLength = nameLength - GeneralWordsUtil.getEncodingLength(tmpValue + ";", null, true, false);
		tmpValue = con.getPrefix();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setPrefix(valueByMaxLength);
			return;
		}
		returnContact.setPrefix(tmpValue);
		//suffix
		nameLength = nameLength - GeneralWordsUtil.getEncodingLength(tmpValue + ";", null, true, false);
		tmpValue = con.getSuffix();
		valueByMaxLength = GeneralWordsUtil.getValueByMaxLength(tmpValue, nameLength, true, false);
		if(tmpValue != null &&
				!tmpValue.equals(valueByMaxLength)){
			returnContact.setSuffix(valueByMaxLength);
			return;
		}
		returnContact.setSuffix(tmpValue);
		
		//phoneticFamilyName
		if(!TextUtils.isEmpty(con.getPhoneticLastName())){
			tmpValue = GeneralWordsUtil.getValueByMaxLength(con.getPhoneticLastName(), X_PROPERTY, true, false);
			returnContact.setPhoneticLastName(tmpValue);
		}
		//phoneticGivenName
		if(!TextUtils.isEmpty(con.getPhoneticFirstName())){
			tmpValue = GeneralWordsUtil.getValueByMaxLength(con.getPhoneticFirstName(), X_PROPERTY, true, false);
			returnContact.setPhoneticFirstName(tmpValue);
		}
		//phoneticMiddleName
		if(!TextUtils.isEmpty(con.getPhoneticMiddleName())){
			tmpValue = GeneralWordsUtil.getValueByMaxLength(con.getPhoneticMiddleName(), X_PROPERTY, true, false);
			returnContact.setPhoneticMiddleName(tmpValue);
		}
	}

	private boolean mPrefIsSet_Address = false;
    private boolean mPrefIsSet_Phone = false;
    private boolean mPrefIsSet_Email = false;
    private boolean mPrefIsSet_Organization = false;
    
	public ContactStruct structProcessBeforeSave(ContactStruct con) {
		
		//deal with order:phone
		List<PhoneData> phoneList = con.getPhoneList();
		setPhoneOrder(con,phoneList);
		//reload the phoneList
		phoneList = con.getPhoneList();
		//deal with the default value:phone,org,email,address
		//1.set phone default value
		if(phoneList != null && phoneList.size() > 0){
			for (ContactStruct.PhoneData phone : phoneList) {
				if(phone.isPrimary){
					mPrefIsSet_Phone = true;
					break;
				}
			}
			setPhonePrimary(con,phoneList);
		}
		
		//2.set org default value
		List<OrganizationData> orgList = con.getOrganizationList();
		if (orgList != null && orgList.size() > 0) {
			for (ContactStruct.OrganizationData org : orgList) {
				if(org.isPrimary){
					mPrefIsSet_Organization = true;
					break;
				}
			}
			setOrganizationPrimary(orgList);
		}
		
		//3.set email default value
		List<EmailData> emailList = con.getEmailList();
		if(emailList != null && emailList.size() > 0){
			for (ContactStruct.EmailData email : emailList) {
				if(email.isPrimary){
					mPrefIsSet_Email = true;
					break;
				}
			}
			setEmailPrimary(emailList);
		}
		
		//4.set adr default value
		List<PostalData> postalList = con.getPostalList();
		if(postalList != null && postalList.size() > 0){
			for (ContactStruct.PostalData adr : postalList) {
				if(adr.isPrimary){
					mPrefIsSet_Address = true;
					break;
				}
			}
			setAddressPrimary(postalList);
		}
		
		return con;
	}
	
	//set the primary phone
	private void setPhonePrimary(ContactStruct con,List<PhoneData> pdList) {

		PhoneData pdMobile = null;
		PhoneData pdWork = null;
		PhoneData pdHome = null;
		PhoneData pdOther = null;
		if(!mPrefIsSet_Phone){
			boolean existPrimaryPhone = false;
			//CMCC requirement:Default number:Mobile>Work>Home>Other
			//find out which phone exist in the number list.
			for (PhoneData phoneData : pdList) {
					if(!TextUtils.isEmpty(phoneData.data)){
						if(phoneData.type == CommonDataKinds.Phone.TYPE_MOBILE){
							pdMobile = phoneData;
							break;
						}else if(phoneData.type == CommonDataKinds.Phone.TYPE_WORK){
							pdWork = phoneData;
							break;
						}else if(phoneData.type == CommonDataKinds.Phone.TYPE_HOME){
							pdHome = phoneData;
							break;
						}else if(phoneData.type == CommonDataKinds.Phone.TYPE_OTHER){
							pdOther = phoneData;
							break;
						}
					}
			}
			
			//update the corresponding phone's isParmary
			if(!existPrimaryPhone){
				if(pdMobile != null){
					pdMobile.isPrimary = true;
				}else if(pdWork != null){
					pdWork.isPrimary = true;
				}else if(pdHome != null){
					pdHome.isPrimary = true;
				}else if(pdOther != null){
					pdOther.isPrimary = true;
				}else{
					//set the first as default number
				    PhoneData pd = con.getPhoneList().get(0);
				    if(pd != null){
				        pd.isPrimary = true;
				    }
				}
			}
		}
	}
	
	//set the primary address
	private void setAddressPrimary(List<PostalData> mPostalList){
		if (!mPrefIsSet_Address && mPostalList.get(0) != null) {
	         mPostalList.get(0).isPrimary = true;
	    }
	}
	
	//set the primary Email
	private void setEmailPrimary(List<EmailData> mEmailList){
		if (!mPrefIsSet_Email && mEmailList.get(0) != null) {
			mEmailList.get(0).isPrimary = true;
		}
	}
	
	//set the primary organization
	private void setOrganizationPrimary(List<OrganizationData> mOrganizationList) {
		if (!mPrefIsSet_Organization && mOrganizationList.get(0) != null) {
			mOrganizationList.get(0).isPrimary = true;
		}
	}
	
	//set phone order
	private void setPhoneOrder(ContactStruct con, List<PhoneData> phoneList) {
		if (phoneList != null && phoneList.size() > 0) {
			List<PhoneData> newPhoneList = new ArrayList<ContactStruct.PhoneData>();
			for (PhoneData phoneData : phoneList) {
				newPhoneList.add(phoneData);
			}
			int[] types = new int[] { CommonDataKinds.Phone.TYPE_MOBILE,
					CommonDataKinds.Phone.TYPE_WORK,
					CommonDataKinds.Phone.TYPE_HOME,
					CommonDataKinds.Phone.TYPE_OTHER };
			con.getPhoneList().clear();
			// mobile->work->home->other
			for (int type : types) {
				for (PhoneData phoneData : newPhoneList) {
					if (phoneData.type == type) {
						con.addPhone(phoneData);
					}
				}
			}
			for (PhoneData phoneData : newPhoneList) {
				switch (phoneData.type) {
				case CommonDataKinds.Phone.TYPE_MOBILE:
					break;
				case CommonDataKinds.Phone.TYPE_WORK:
					break;
				case CommonDataKinds.Phone.TYPE_HOME:
					break;
				case CommonDataKinds.Phone.TYPE_OTHER:
					break;
				default:
					con.addPhone(phoneData);
					break;
				}
			}
		}
	}
}
