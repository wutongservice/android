/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.vdata.card;

import android.provider.ContactsContract.CommonDataKinds.*;
import android.text.TextUtils;
import com.borqs.pim.jcontact.*;
import com.borqs.pim.jcontact.JContact.TypedEntity;
import com.borqs.sync.client.common.Util;
import com.borqs.sync.client.vdata.card.ContactStruct.*;
import com.borqs.syncml.ds.imp.common.TypeMatcher;

import java.util.ArrayList;
import java.util.List;

public class JContactConverter {

	private static final TypeMatcher mTypeMatcher = new TypeMatcher();
	private static final String VALUE_TRUE = "true";
	private static final String VALUE_FALSE = "false";
	
	private static final String X_TAG_BORQS_UID = "X-BORQS-UID";
	private static final String X_TAG_BORQS_NAME = "X-BORQS-NAME";

	static {
		// phone matcher
		mTypeMatcher.addType(Phone.TYPE_ASSISTANT, JPhone.ASSISTANT,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_CALLBACK, JPhone.CALLBACK,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_CAR, JPhone.CAR,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_COMPANY_MAIN, JPhone.COMPANY_MAIN,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_FAX_HOME, JPhone.HOME_FAX,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_FAX_WORK, JPhone.WORK_FAX,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_HOME, JPhone.HOME,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_ISDN, JPhone.ISDN,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_MAIN, JPhone.MAIN,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_MMS, JPhone.MMS,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_MOBILE, JPhone.MOBILE,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_OTHER, JPhone.OTHER,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_OTHER_FAX, JPhone.OTHER_FAX,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_PAGER, JPhone.PAGE,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_RADIO, JPhone.RADIO,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_TELEX, JPhone.TELEGRAPH,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_TTY_TDD, JPhone.TTY_TDD,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_WORK, JPhone.WORK,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_WORK_MOBILE, JPhone.WORK_MOBILE,
				TypeMatcher.TYPE_PHONE_MATCHER);
		mTypeMatcher.addType(Phone.TYPE_WORK_PAGER, JPhone.WORK_PAGE,
				TypeMatcher.TYPE_PHONE_MATCHER);
		// org matcher
		mTypeMatcher.addType(Organization.TYPE_WORK, JORG.WORK,
				TypeMatcher.TYPE_ORG_MATCHER);
		mTypeMatcher.addType(Organization.TYPE_OTHER, JORG.OTHER,
				TypeMatcher.TYPE_ORG_MATCHER);
		// email matcher
		mTypeMatcher.addType(Email.TYPE_HOME, JEMail.HOME,
				TypeMatcher.TYPE_EMAIL_MATCHER);
		mTypeMatcher.addType(Email.TYPE_MOBILE, JEMail.MOBILE,
				TypeMatcher.TYPE_EMAIL_MATCHER);
		mTypeMatcher.addType(Email.TYPE_OTHER, JEMail.OTHER,
				TypeMatcher.TYPE_EMAIL_MATCHER);
		mTypeMatcher.addType(Email.TYPE_WORK, JEMail.WORK,
				TypeMatcher.TYPE_EMAIL_MATCHER);
		// address matcher
		mTypeMatcher.addType(StructuredPostal.TYPE_HOME, JAddress.HOME,
				TypeMatcher.TYPE_ADDRESS_MATCHER);
		mTypeMatcher.addType(StructuredPostal.TYPE_WORK, JAddress.WORK,
				TypeMatcher.TYPE_ADDRESS_MATCHER);
		mTypeMatcher.addType(StructuredPostal.TYPE_OTHER, JAddress.OTHER,
				TypeMatcher.TYPE_ADDRESS_MATCHER);
		// im matcher
		mTypeMatcher.addType(Im.PROTOCOL_AIM, JIM.AIM,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_GOOGLE_TALK, JIM.GOOGLE_TALK,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_ICQ, JIM.ICQ,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_JABBER, JIM.JABBER,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_MSN, JIM.MSN,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_NETMEETING, JIM.NETMEETING,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_QQ, JIM.QQ,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_SKYPE, JIM.SKYPE,
				TypeMatcher.TYPE_IM_MATCHER);
		mTypeMatcher.addType(Im.PROTOCOL_YAHOO, JIM.YAHOO,
				TypeMatcher.TYPE_IM_MATCHER);
		// website matcher
		mTypeMatcher.addType(Website.TYPE_BLOG, JWebpage.BLOG,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_FTP, JWebpage.FTP,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_HOME, JWebpage.HOME,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_HOMEPAGE, JWebpage.HOMEPAGE,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_OTHER, JWebpage.OTHER,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_PROFILE, JWebpage.PROFILE,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
		mTypeMatcher.addType(Website.TYPE_WORK, JWebpage.WORK,
				TypeMatcher.TYPE_WEBSITE_MATCHER);
	}

	public String convertToJson(ContactStruct contact) {
		if (contact == null || isEmpty(contact)) {
			return null;
		}
		JContactBuilder jContactBuilder = new JContactBuilder();
		// set builder name
		jContactBuilder
				.setFirstName(Util.trimString(contact.getFirstName()),
				        Util.trimString(contact.getPhoneticFirstName()))
				.setMiddleName(Util.trimString(contact.getMiddleName()),
				        Util.trimString(contact.getPhoneticMiddleName()))
				.setLastName(Util.trimString(contact.getLastName()),
				        Util.trimString(contact.getPhoneticLastName()));
		
				if(contact.isFavorite()){
				    jContactBuilder.addXTag(JXTag.X_STARRED,VALUE_TRUE);
				}
				
				if(contact.isBlock()){
				    jContactBuilder.addXTag(JXTag.X_BLOCK, VALUE_TRUE);
				}
				
        if (!TextUtils.isEmpty(contact.getPrefix())) {
            jContactBuilder.setNamePrefix(contact.getPrefix());
        }
        if (!TextUtils.isEmpty(contact.getSuffix())) {
            jContactBuilder.setNamePostfix(contact.getSuffix());
        }
        if (!TextUtils.isEmpty(contact.getBirthday())) {
            jContactBuilder.setBirthday(contact.getBirthday());
        }
		
        if (!TextUtils.isEmpty(contact.getAccountType())) {
            jContactBuilder.addXTag(JXTag.X_ACCOUNT_TYPE, contact.getAccountType());
        }
        if (!TextUtils.isEmpty(contact.getRingtoneFile())) {
            jContactBuilder.addXTag(JXTag.X_RINGTONG, contact.getRingtoneFile());
        }
        if (!TextUtils.isEmpty(contact.getBorqsUid())){
        	jContactBuilder.addXTag(X_TAG_BORQS_UID, contact.getBorqsUid());
        }
//        if (!TextUtils.isEmpty(contact.getBorqsName())){
//        	jContactBuilder.addXTag(X_TAG_BORQS_NAME, contact.getBorqsName());
//        }
		addJBuilderNote(jContactBuilder, contact);
		addJBuilderNickName(jContactBuilder, contact);
		addJBuilderPhoto(jContactBuilder, contact);
		addJBuilderPhone(jContactBuilder, contact);
		addJBuilderEmail(jContactBuilder, contact);
		addJBuilderAddress(jContactBuilder, contact);
		addJBuilderOrg(jContactBuilder, contact);
		addJBuilderIM(jContactBuilder, contact);
		addJBuilderWebSite(jContactBuilder, contact);
//		addJBuilderGroup(jContactBuilder, contact);
		return jContactBuilder.createJson();
	}

	private void addJBuilderNote(JContactBuilder jBuilder, ContactStruct contact) {
		List<String> mNoteList = contact.getNotes();
		if (mNoteList != null && mNoteList.size() > 0) {
		    if(!TextUtils.isEmpty(mNoteList.get(0))){
		        jBuilder.setNote(mNoteList.get(0));
		    }
		}
	}

	private void addJBuilderNickName(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<String> mNickNameList = contact.getNickNameList();
		if (mNickNameList != null && mNickNameList.size() > 0) {
		    if(!TextUtils.isEmpty(mNickNameList.get(0))){
		        jBuilder.setNickName(mNickNameList.get(0));
		    }
		}
	}

	private void addJBuilderPhoto(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<PhotoData> mPhotoList = contact.getPhotoList();
		if (mPhotoList != null && mPhotoList.size() > 0) {
			PhotoData pd = mPhotoList.get(0);
			if (pd != null && pd.photoBytes != null) {
				jBuilder.setPhoto(pd.photoBytes);
			}
		}
	}

	private void addJBuilderPhone(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<PhoneData> mPhoneList = contact.getPhoneList();
		if (mPhoneList != null) {
			for (PhoneData phoneData : mPhoneList) {
				if (phoneData == null || TextUtils.isEmpty(phoneData.data)) {
					continue;
				}
				jBuilder.addPhone(
						convertToJType(phoneData.type, JPhone.OTHER/*other as default phone type*/,
								TypeMatcher.TYPE_PHONE_MATCHER),
						phoneData.data, phoneData.isPrimary);
			}
		}
	}

	private void addJBuilderEmail(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<EmailData> mEmailList = contact.getEmailList();
		if (mEmailList != null) {
			for (EmailData emailData : mEmailList) {
				if (emailData == null || TextUtils.isEmpty(emailData.data)) {
					continue;
				}
				jBuilder.addEmail(
						convertToJType(emailData.type, JEMail.OTHER/*other as default email type*/,
								TypeMatcher.TYPE_EMAIL_MATCHER),
						emailData.data, emailData.isPrimary);
			}
		}
	}

	private void addJBuilderAddress(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<PostalData> mPostalList = contact.getPostalList();
		if (mPostalList != null) {
			for (PostalData addressData : mPostalList) {
				if (addressData == null) {
					continue;
				}
				String street = TextUtils.isEmpty(addressData.street)?"":addressData.street;
				String localty = TextUtils.isEmpty(addressData.localty)?"":addressData.localty;
				String region = TextUtils.isEmpty(addressData.region)?"":addressData.region;
				String postalCode = TextUtils.isEmpty(addressData.postalCode)?"":addressData.postalCode;
				
				if(TextUtils.isEmpty(street + localty + region + postalCode)){
				    continue;
				}
				
				jBuilder.addAddress(
						convertToJType(addressData.type, JAddress.OTHER/*other as default address type*/,
								TypeMatcher.TYPE_ADDRESS_MATCHER),
								street, localty,
								region, postalCode);
			}
		}
	}

	private void addJBuilderOrg(JContactBuilder jBuilder, ContactStruct contact) {
		List<OrganizationData> mOrganizationList = contact
				.getOrganizationList();
		if (mOrganizationList != null) {
			for (OrganizationData orgData : mOrganizationList) {
				if (orgData == null || (TextUtils.isEmpty(orgData.companyName) 
				        && TextUtils.isEmpty(orgData.positionName))) {
					continue;
				}
				jBuilder.addOrg(
						convertToJType(orgData.type, JORG.OTHER/*other as default org type*/,
								TypeMatcher.TYPE_ORG_MATCHER),
						orgData.companyName, orgData.positionName);
			}
		}
	}

	private void addJBuilderIM(JContactBuilder jBuilder, ContactStruct contact) {
		List<ImData> mImList = contact.getImList();
		if (mImList != null) {
			for (ImData imData : mImList) {
				if (imData == null || TextUtils.isEmpty(imData.data)) {
					continue;
				}
				jBuilder.addIM(
						convertToJType(imData.type, JIM.QQ/*qq as default im type*/,
								TypeMatcher.TYPE_IM_MATCHER), imData.data);
			}
		}
	}

	private void addJBuilderWebSite(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<WebsiteData> mWebsiteList = contact.getWebsiteList();
		if (mWebsiteList != null) {
			for (WebsiteData websiteData : mWebsiteList) {
				if (websiteData == null || TextUtils.isEmpty(websiteData.data)) {
					continue;
				}
				jBuilder.addWebpage(
						convertToJType(websiteData.type, JWebpage.OTHER/*other as default web type*/,
								TypeMatcher.TYPE_WEBSITE_MATCHER),
						websiteData.data);
			}
		}
	}

	private void addJBuilderGroup(JContactBuilder jBuilder,
			ContactStruct contact) {
		List<String> mGroupList = contact.getGroupList();
		if (mGroupList != null) {
			for (String group : mGroupList) {
				jBuilder.addXTag(JXTag.X_GROUPS, group);
			}
		}
	}
	
	public void convertToStruct(JContact jContact, ContactStruct contact){
        contact.setFirstName(Util.trimString(jContact.getFirstName()));
        contact.setMiddleName(Util.trimString(jContact.getMiddleName()));
        contact.setLastName(Util.trimString(jContact.getLastName()));
        contact.setPrefix(Util.trimString(jContact.getNamePrefix()));
        contact.setSuffix(Util.trimString(jContact.getNamePostfix()));
        contact.setPhoneticFirstName(Util.trimString(jContact.getFirstNamePinyin()));
        contact.setPhoneticLastName(Util.trimString(jContact.getLastNamePinyin()));
        contact.setPhoneticMiddleName(Util.trimString(jContact.getMiddleNamePinyin()));
        // nickname
        if(!TextUtils.isEmpty(jContact.getNickName())){
            contact.addNickNameList(jContact.getNickName());
        }
        // address
        addContactAddress(jContact, contact);
        // email
        addContactEmail(jContact, contact);
        // org
        addContactOrg(jContact, contact);
        // phone
        addContactPhone(jContact, contact);
        // IM
        addContactIM(jContact, contact);
        // website
        addContactWebsite(jContact, contact);
        // birthday
        contact.setBirthday(jContact.getBirthday());
        // block
        
        String xValue = getXValue(jContact, contact, JXTag.X_BLOCK);
        if(!TextUtils.isEmpty(xValue)){
            contact.setBlock(VALUE_TRUE.equals(xValue));
        }
        xValue = getXValue(jContact, contact, JXTag.X_RINGTONG);
        // ringtone
        if(!TextUtils.isEmpty(xValue)){
            contact.setRingtoneFile(xValue);
        }
        // photo
        if(jContact.getPhoto() != null){
            contact.addPhotoBytes(null, jContact.getPhoto());
        }
        // group
        xValue = getXValue(jContact, contact, JXTag.X_GROUPS);
        if(!TextUtils.isEmpty(xValue)){
            addGroups(contact, xValue);
//          contact.addGroup(xValue);
        }
        
        // STARRED
        xValue = getXValue(jContact, contact, JXTag.X_STARRED);
        if(!TextUtils.isEmpty(xValue)){
            contact.setFavorite(VALUE_TRUE.equals(xValue));
        }
        
        // account type
        xValue = getXValue(jContact, contact, JXTag.X_ACCOUNT_TYPE);
        if(!TextUtils.isEmpty(xValue)){
            contact.setAccountType(xValue);
        }
        // note
        if(!TextUtils.isEmpty(jContact.getNote())){
            contact.addNote(jContact.getNote());
        }
        //borqs_uid
        xValue = getXValue(jContact, contact, X_TAG_BORQS_UID);
        if(!TextUtils.isEmpty(xValue)){
            contact.setBorqsUid(xValue);
        }
        // borqs_name
        xValue = getXValue(jContact, contact, X_TAG_BORQS_NAME);
        if (!TextUtils.isEmpty(xValue)) {
            contact.setBorqsName(xValue);
        }
    }

	public ContactStruct convertToContactStruct(JContact jContact) {
		ContactStruct contact = new ContactStruct();
		convertToStruct(jContact, contact);
		return contact;
	}

    //The groups like "朋友,同事"
    private void addGroups(ContactStruct c, String combinedGroups){
        if(TextUtils.isEmpty(combinedGroups)) {
            return;
        }

        String[] groupsName = combinedGroups.split(",");
        for(String group: groupsName){
            if(!TextUtils.isEmpty(group)){
                c.addGroup(group);
            }
        }
    }

	private void addContactWebsite(JContact jContact, ContactStruct contact) {
		List<TypedEntity> websiteList = jContact.getWebpageList();
		if (websiteList != null) {
			for (TypedEntity typedEntity : websiteList) {
				if (typedEntity == null || TextUtils.isEmpty((String)typedEntity.getValue())) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_WEBSITE_MATCHER);
				String label = null;
				if (contactType == -1) {
					// custom
					contactType = Website.TYPE_CUSTOM;
					label = jType;
				}
				contact.addWebsite(contactType,
						(String) typedEntity.getValue(), label, false);
			}
		}
	}

	private void addContactIM(JContact jContact, ContactStruct contact) {
		List<TypedEntity> imList = jContact.getIMList();
		if (imList != null) {
			for (TypedEntity typedEntity : imList) {
				if (typedEntity == null || TextUtils.isEmpty((String)typedEntity.getValue())) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_IM_MATCHER);
				String label = null;
				String customProtocol = null;
				// custom
				if (contactType == -1) {
					contactType = Im.PROTOCOL_CUSTOM;
					// fetion
					if ("fetion".equals(jType)) {
						label = "fetion";
						customProtocol = "FETION";
					}else{
						customProtocol = jType;
					}
				}
				contact.addIm(contactType, (String) typedEntity.getValue(),
						label, customProtocol, false);
			}
		}
	}

	private void addContactPhone(JContact jContact, ContactStruct contact) {
		List<TypedEntity> phoneList = jContact.getPhoneList();
		if (phoneList != null) {
			for (TypedEntity typedEntity : phoneList) {
				if (typedEntity == null || TextUtils.isEmpty((String)typedEntity.getValue())) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_PHONE_MATCHER);
				String label = null;
				if (contactType == -1) {
					// custom
					contactType = Phone.TYPE_CUSTOM;
					label = jType;
				}
				contact.addPhone(contactType, (String) typedEntity.getValue(),
						label, JPhone.isPrimary(typedEntity));
			}
		}
	}

	private void addContactOrg(JContact jContact, ContactStruct contact) {
		List<TypedEntity> orgList = jContact.getOrgList();
		if (orgList != null) {
			for (TypedEntity typedEntity : orgList) {
				if (typedEntity == null || (TextUtils.isEmpty(JORG.company(typedEntity.getValue())) 
				        && TextUtils.isEmpty(JORG.title(typedEntity.getValue())))) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_ORG_MATCHER);
				String label = null;
				if (contactType == -1) {
					// custom
					contactType = Organization.TYPE_CUSTOM;
					label = jType;
				}
				contact.addOrganization(contactType, JORG.company(typedEntity.getValue()),
						JORG.title(typedEntity.getValue()), null, label, false);
			}
		}
	}

	private void addContactEmail(JContact jContact, ContactStruct contact) {
		List<TypedEntity> emailList = jContact.getEmailList();
		if (emailList != null) {
			for (TypedEntity typedEntity : emailList) {
				if (typedEntity == null || TextUtils.isEmpty((String)typedEntity.getValue())) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_EMAIL_MATCHER);
				String label = null;
				if (contactType == -1) {
					// custom
					contactType = Email.TYPE_CUSTOM;
					label = jType;
				}
				contact.addEmail(contactType, (String) typedEntity.getValue(),
						label, JEMail.isPrimary(typedEntity));
			}
		}
	}

	private void addContactAddress(JContact jContact, ContactStruct cs) {
		List<TypedEntity> addressList = jContact.getAddressList();
		if (addressList != null) {
			for (TypedEntity typedEntity : addressList) {
				if (typedEntity == null || (TextUtils.isEmpty(JAddress.street(typedEntity.getValue())) 
				        && TextUtils.isEmpty(JAddress.city(typedEntity.getValue()))
				        && TextUtils.isEmpty(JAddress.province(typedEntity.getValue()))
				        && TextUtils.isEmpty(JAddress.zipcode(typedEntity.getValue())))) {
					continue;
				}
				String jType = typedEntity.getType();
				int contactType = mTypeMatcher.matchContactType(jType,
						TypeMatcher.TYPE_ADDRESS_MATCHER);
				String label = null;
				if (contactType == -1) {
					// custom
					contactType = StructuredPostal.TYPE_CUSTOM;
					label = jType;
				}
				List<String> addressValue = new ArrayList<String>();
				addressValue.add(null);// pobox
				addressValue.add(null);// extendedAddress
				addressValue.add(JAddress.street(typedEntity.getValue()));// street
				addressValue.add(JAddress.city(typedEntity.getValue()));// localty
				addressValue.add(JAddress.province(typedEntity.getValue()));// region
				addressValue.add(JAddress.zipcode(typedEntity.getValue()));// postalCode
				addressValue.add(null);// country
				cs.addPostal(contactType, addressValue, label, false);
			}
		}
	}

	private String getXValue(JContact jContact, ContactStruct cs, String type) {
		List<TypedEntity> entityList = jContact.getXTags();
		if (entityList != null) {
			for (TypedEntity typedEntity : entityList) {
				if (typedEntity == null || TextUtils.isEmpty((String)typedEntity.getValue())) {
					continue;
				}
				if (type.equals(typedEntity.getType())) {
					return (String) typedEntity.getValue();
				}
			}
		}
		return null;
	}

	// if the type is not defined,we use the default type as jType directly
	private String convertToJType(int type, String defaultType, int matcherType) {
		String jType = mTypeMatcher.matchJContactType(type, matcherType);
		return jType == null ? defaultType : jType;
	}
	
    private boolean isEmpty(ContactStruct c) {
        //name
        boolean hasName = !TextUtils.isEmpty(c.getLastName())
                || !TextUtils.isEmpty(c.getMiddleName())
                || !TextUtils.isEmpty(c.getFirstName())
                || !TextUtils.isEmpty(c.getPhoneticLastName())
                || !TextUtils.isEmpty(c.getPhoneticFirstName())
                || !TextUtils.isEmpty(c.getPhoneticMiddleName())
                || !TextUtils.isEmpty(c.getPrefix())
                || !TextUtils.isEmpty(c.getSuffix())
                || (c.getNickNameList() != null || !TextUtils.isEmpty(c.getNickNameList().get(0)));

        //birthday
        boolean hasBirthday = !TextUtils.isEmpty(c.getBirthday());

        //email
        List<EmailData> emailList = c.getEmailList();
        boolean hasEmail = false;
        if (emailList != null) {
            for (EmailData emailData : emailList) {
                if (emailData != null && !TextUtils.isEmpty(emailData.data)) {
                    hasEmail = true;
                    break;
                }
            }
        }

        //im
        List<ImData> imList = c.getImList();
        boolean hasIM = false;
        if (imList != null) {
            for (ImData imData : imList) {
                if (imData != null && !TextUtils.isEmpty(imData.data)) {
                    hasIM = true;
                    break;
                }
            }
        }

        //org
        List<OrganizationData> orgList = c.getOrganizationList();
        boolean hasOrg = false;
        if (orgList != null) {
            for (OrganizationData org : orgList) {
                if (org == null) {
                    continue;
                }
                String company = org.companyName;
                String dep = org.department;
                String title = org.positionName;
                if (!TextUtils.isEmpty(company) || !TextUtils.isEmpty(dep)
                        || !TextUtils.isEmpty(title)) {
                    hasOrg = true;
                    break;
                }
            }

        }

        //phone
        List<PhoneData> phoneList = c.getPhoneList();
        boolean hasPhone = false;
        if (phoneList != null) {
            for (PhoneData phoneData : phoneList) {
                if (phoneData != null && !TextUtils.isEmpty(phoneData.data)) {
                    hasPhone = true;
                    break;
                }
            }
        }

        //photo
        List<PhotoData> photoList = c.getPhotoList();
        boolean hasPhoto = false;
        if (photoList != null) {
            for (PhotoData photoData : photoList) {
                if (photoData != null && photoData.photoBytes != null
                        && photoData.photoBytes.length > 0) {
                    hasPhoto = true;
                    break;
                }
            }
        }

        //postal
        List<PostalData> postalList = c.getPostalList();
        boolean hasPostal = false;
        if (postalList != null) {
            for (PostalData postalData : postalList) {
                if (postalData == null) {
                    continue;
                }
                String country = postalData.country;
                String extendedAddress = postalData.extendedAddress;
                String localty = postalData.localty;
                String pobox = postalData.pobox;
                String postalCode = postalData.postalCode;
                String region = postalData.region;
                String street = postalData.street;
                if (!TextUtils.isEmpty(country) || !TextUtils.isEmpty(extendedAddress)
                        || !TextUtils.isEmpty(localty) || !TextUtils.isEmpty(pobox)
                        || !TextUtils.isEmpty(postalCode) || !TextUtils.isEmpty(region)
                        || !TextUtils.isEmpty(street)) {
                    hasPostal = true;
                    break;
                }
            }
        }

        //web
        List<WebsiteData> webList = c.getWebsiteList();
        boolean hasWeb = false;
        if (webList != null) {
            for (WebsiteData websiteData : webList) {
                if (websiteData != null && !TextUtils.isEmpty(websiteData.data)) {
                    hasWeb = true;
                    break;
                }
            }
        }

        return !hasName && !hasEmail && !hasBirthday && !hasIM && !hasOrg && !hasPhone && !hasPhoto
                && !hasPostal && !hasWeb;
    }
	
//	private boolean isNotEmpty(List contactItems){
//	    if(contactItems != null){
//	        
//	    }
//        return false;
//	}
//	
}
