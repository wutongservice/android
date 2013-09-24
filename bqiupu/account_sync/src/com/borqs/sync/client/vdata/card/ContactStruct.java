package com.borqs.sync.client.vdata.card;

import java.text.SimpleDateFormat;
import java.util.*;

import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.TextUtils;

/**
 * This class bridges between data structure of Contact app and VCard data.
 */
public class ContactStruct {
    private static final String LOG_TAG = "vcard.ContactStruct";

    
	static boolean stringEqual(String str1, String str2) {
		if (TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)) {
			return true;
		} else if (TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2)) {
			return false;
		} else if (!TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)) {
			return false;
		} else {
			return str1.equals(str2);
		}
	}
    
    /**
     * phone
     */
    static public class PhoneData {
        public final int type;
        public final String data;
        public final String label;
        // isPrimary is changable only when there's no appropriate one existing in
        // the original VCard.
        public boolean isPrimary;
        public PhoneData(int type, String data, String label, boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof PhoneData)) {
                return false;
            }
            
            PhoneData phoneData = (PhoneData)obj;
            return (type == phoneData.type && stringEqual(data,phoneData.data) &&
            		stringEqual(label, phoneData.label) && isPrimary == phoneData.isPrimary);
        }
        
        public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 46; // any arbitrary constant will do 

        }
        
        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s",
                    type, data, label, isPrimary);
        }
    }

    /**
     * Email
     */
    static public class EmailData {
        public final int type;
        public final String data;
        // Used only when TYPE is TYPE_CUSTOM.
        public final String label;
        // isPrimary is changable only when there's no appropriate one existing in
        // the original VCard.
        public boolean isPrimary;
        public EmailData(int type, String data, String label, boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof EmailData)) {
                return false;
            }
            EmailData emailData = (EmailData)obj;
            return (type == emailData.type && stringEqual(data,emailData.data) &&
            		stringEqual(label,emailData.label) && isPrimary == emailData.isPrimary);
        }
        
        public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 49; // any arbitrary constant will do 

        }
        
        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s",
                    type, data, label, isPrimary);
        }
    }

    /**
     * Postal
     */
    static public class PostalData {
        // Determined by vCard spec.
        // PO Box, Extended Addr, Street, Locality, Region, Postal Code, Country Name
        public static final int ADDR_MAX_DATA_SIZE = 7;
        private final String[] dataArray;
        public final String pobox;
        public final String extendedAddress;
        public final String street;
        public final String localty;
        public final String region;
        public final String postalCode;
        public final String country;

        public final int type;
        
        // Used only when type variable is TYPE_CUSTOM.
        public final String label;

        // isPrimary is changable only when there's no appropriate one existing in
        // the original VCard.
        public boolean isPrimary;
        
      //TODO bad code about setting address property,MUST refactor ContactStruct 
        public PostalData(int type, List<String> propValueList,
                String label, boolean isPrimary) {
            this.type = type;
            dataArray = new String[ADDR_MAX_DATA_SIZE];

            int size = propValueList.size();
            if (size > ADDR_MAX_DATA_SIZE) {
                size = ADDR_MAX_DATA_SIZE;
            }

            // adr-value    = 0*6(text-value ";") text-value
            //              ; PO Box, Extended Address, Street, Locality, Region, Postal
            //              ; Code, Country Name
            //
            // Use Iterator assuming List may be LinkedList, though actually it is
            // always ArrayList in the current implementation.
            int i = 0;
            for (String addressElement : propValueList) {
                dataArray[i] = addressElement;
                if (++i >= size) {
                    break;
                }
            }
            while (i < ADDR_MAX_DATA_SIZE) {
                dataArray[i++] = null;
            }

            this.pobox = dataArray[0];
            this.extendedAddress = dataArray[1];
            this.street = dataArray[2];
            this.localty = dataArray[3];
            this.region = dataArray[4];
            this.postalCode = dataArray[5];
            this.country = dataArray[6];
            
            this.label = label;
            this.isPrimary = isPrimary;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof PostalData)) {
                return false;
            }
            PostalData postalData = (PostalData)obj;
            return (Arrays.equals(dataArray, postalData.dataArray) && 
                    (type == postalData.type &&
                            (type == StructuredPostal.TYPE_CUSTOM ?
                            		stringEqual(label, postalData.label) : true)) &&
                    (isPrimary == postalData.isPrimary));
        }
        
        public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 45; // any arbitrary constant will do 

        }
        
        public String getFormattedAddress(int vcardType) {
            StringBuilder builder = new StringBuilder();
            boolean empty = true;
//            if (VCardConfig.isJapaneseDevice(vcardType)) {
            if (false) {
                // In Japan, the order is reversed.
                for (int i = ADDR_MAX_DATA_SIZE - 1; i >= 0; i--) {
                    String addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (!empty) {
                            builder.append(' ');
                        }
                        builder.append(addressPart);
                        empty = false;
                    }
                }
            } else {
                for (int i = 0; i < ADDR_MAX_DATA_SIZE; i++) {
                    String addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (!empty) {
                            builder.append(' ');
                        }
                        builder.append(addressPart);
                        empty = false;
                    }
                }
            }

            return builder.toString().trim();
        }
        
        public String toString() {
            return String.format("type: %d, label: %s, isPrimary: %s",
                    type, label, isPrimary);
        }
    }
    
    /**
     * Organization
     */
    static public class OrganizationData {
        public final int type;
        public String companyName;
        public String department;
        // can be changed in some VCard format. 
        public String positionName;
        // isPrimary is changable only when there's no appropriate one existing in
        // the original VCard.
        public String label;
        public boolean isPrimary;
        public OrganizationData(int type, String companyName, String positionName,
                String department, String label, boolean isPrimary) {
            this.type = type;
            this.companyName = companyName;
            this.positionName = positionName;
            this.department = department;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof OrganizationData)) {
                return false;
            }
            OrganizationData organization = (OrganizationData)obj;
            return (type == organization.type && stringEqual(companyName,organization.companyName) &&
            		stringEqual(positionName,organization.positionName) &&
            		stringEqual(department,organization.department) &&
                    stringEqual(label, organization.label) &&
                    isPrimary == organization.isPrimary);
        }
        
        public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 47; // any arbitrary constant will do 

        }
        
        public String toString() {
            return String.format("type: %d, company: %s, position: %s, department: %s,label:%s, isPrimary: %s",
                    type, companyName, positionName, department,label, isPrimary);
        }
        
        public void setCompany(String company) {
            this.companyName = company;
        }
        
        public void setPosition(String position) {
            this.positionName = position;
        }
        
        public void setDepartment(String department) {
            this.department = department;
        }
        
        public void setIsPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }    
   
    /**
     * Im
     */
    static public class ImData {
        public final int type;
        public final String data;
        public final String label;
        public final String customProtocol;
        public final boolean isPrimary;
        
        // TODO: ContactsConstant#PROTOCOL, ContactsConstant#CUSTOM_PROTOCOL should be used?
        //for IM,the label value is CUSTOM_PROTOCOL
        /**
         * label:
         * if TYPE_FETION:fetion
         * else:null value
         * 
         */
        public ImData(int type, String data, String label,String customProtocol,boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.customProtocol = customProtocol;
            this.isPrimary = isPrimary;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof ImData)) {
                return false;
            }
            ImData imData = (ImData)obj;
            return (type == imData.type && stringEqual(data,imData.data)
            		&& stringEqual(label,imData.label)
            		&& stringEqual(customProtocol,imData.customProtocol)
            		&& isPrimary == imData.isPrimary);
        }
        
        public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 48; // any arbitrary constant will do 

        }
        
        public String toString() {
            return String.format("type: %d, data: %s, label: %s,customProtocol:%s, isPrimary: %s",
                    type, data, label,customProtocol, isPrimary);
        }
    }
    
    /**
     * Photo
     */
    static public class PhotoData {
        public static final String FORMAT_FLASH = "SWF";
        public final int type;
        public final String formatName;  // used when type is not defined in ContactsContract.
        public final byte[] photoBytes;

        public PhotoData(int type, String formatName, byte[] photoBytes) {
            this.type = type;
            this.formatName = formatName;
            this.photoBytes = photoBytes;
        }
    }
    
    static public class WebsiteData{
        public final int type;
        public final String data;
        // Used only when TYPE is TYPE_CUSTOM.
        public final String label;
        // isPrimary is changable only when there's no appropriate one existing in
        // the original VCard.
        public boolean isPrimary;
        public WebsiteData(int type, String data, String label, boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        
		public boolean equals(Object obj) {
			if (!(obj instanceof WebsiteData)) {
				return false;
			}
			WebsiteData data = (WebsiteData) obj;
			return (type == data.type && stringEqual(this.data, data.data)
					&& stringEqual(this.label, data.label) && isPrimary == data.isPrimary);
		}
		
		public int hashCode() {
        	// TODO Auto-generated method stub
        	assert false : "hashCode not designed";
        	return 44; // any arbitrary constant will do 

        }
        
        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s",
                    type, data, label, isPrimary);
        }

    }
    /*
     * 
     */
    static public class Property {
        String mPropertyName;
        Map<String, Collection<String>> mParameterMap =
            new HashMap<String, Collection<String>>();
        List<String> mPropertyValueList = new ArrayList<String>();
        byte[] mPropertyBytes;
        
        public Property() {
            clear();
        }
        
        public void setPropertyName(final String propertyName) {
            mPropertyName = propertyName;
        }
        
        public void addParameter(final String paramName, final String paramValue) {
            Collection<String> values;
            if (!mParameterMap.containsKey(paramName)) {
                if (paramName.equals("TYPE")) {
                    values = new HashSet<String>();
                } else {
                    values = new ArrayList<String>();
                }
                mParameterMap.put(paramName, values);
            } else {
                values = mParameterMap.get(paramName);
            }
            values.add(paramValue);
        }
        
        public void addToPropertyValueList(final String propertyValue) {
            mPropertyValueList.add(propertyValue);
        }
        
        public void setPropertyBytes(final byte[] propertyBytes) {
            mPropertyBytes = propertyBytes;
        }

        public final Collection<String> getParameters(String type) {
            return mParameterMap.get(type);
        }
        
        public final List<String> getPropertyValueList() {
            return mPropertyValueList;
        }
        
        public void clear() {
            mPropertyName = null;
            mParameterMap.clear();
            mPropertyValueList.clear();
        }
    }
    
    private long mRawContactId;
    private long mContactId;
//    private String mName;
    private String mLastName;  // family name
    private String mFirstName; // given name
    private String mMiddleName;
    private String mPrefix;
    private String mSuffix;
    private String mPhoneticLastName; 
    private String mPhoneticFirstName;
    private String mPhoneticMiddleName;

    // Used only when no family nor given name is found.
    private String mFullName;    
    private String mPhoneticFullName;
    private List<String> mNickNameList;

    private String mDisplayName; 

    private String mBirthday;
    private boolean mIsBlock;
    private String mRingtoneFile;
    private boolean mIsFavorite = false;
    
    
    private List<String> mNoteList;
    private List<PhoneData> mPhoneList;
    private List<EmailData> mEmailList;
    private List<PostalData> mPostalList;
    private List<OrganizationData> mOrganizationList;
    private OrganizationData mBaseOrganization;
    private List<ImData> mImList;
    private List<PhotoData> mPhotoList;
    private List<WebsiteData> mWebsiteList;
    private List<String> mGroupList;

    //account info
    private String mAccountType;
    private String mAccountName;
    private String mBorqsUid;
    private String mBorqsName;
    private String mSourceID;
    
    public long getRawContactId(){
    	return mRawContactId;
    }
    
    public void setRawContactId(long id){
    	mRawContactId = id;
    }
    
    public long getContactId(){
    	return mContactId;
    }
    
    public void setContactId(long id){
    	mContactId = id;
    }
    
    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String accountName) {
        this.mAccountName = accountName;
    }

    public void setAccountType(String accountType){
    	mAccountType = accountType;
    }
    
    public void setAccount(String accountName, String accountType) {
        setAccountName(accountName);
        setAccountType(accountType);      
    }
    
    public String getAccountType(){
    	return mAccountType;
    }
    
    public void setSourceID(String id){
        mSourceID = id;
    }
    
    public String getSourceID(){
        return mSourceID;
    }

    /**
     * set name(when only have one name field)
     * @param name
     */
    public void setName(String name) {
        mLastName= name;
    }
    
    /**
     * get Name(when only have one name field)
     */
    public String getName() {
        return mDisplayName;
    }
    
    /**
     * @hide
     */
    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName){
        mLastName = lastName;
    }
    
    /**
     * @hide
     */
    public String getFirstName() {
        return mFirstName;
    }
    
    public void setFirstName(String firstName){
        mFirstName = firstName;
    }

    /**
     * @hide
     */
    public String getMiddleName() {
        return mMiddleName;
    }
    
    public void setMiddleName(String middleName){
    	mMiddleName = middleName;
    }

    /**
     * @hide
     */
    public String getPrefix() {
        return mPrefix;
    }
    
    public void setPrefix(String prefix){
    	mPrefix = prefix;
    }

    /**
     * @hide
     */
    public String getSuffix() {
        return mSuffix;
    }
    
    public void setSuffix(String suffix){
    	mSuffix = suffix;
    }
    
    /**
     * @hide
     */
    public void setFullName(String fullName) {
        mFullName = fullName;
    }
    
    /**
     * @hide
     */
    public String getFullName() {
        return mFullName;
    }

    /**
     * @hide
     */
    public String getPhoneticLastName() {
        return mPhoneticLastName;
    }
    
    public void setPhoneticLastName(String phoneticLastName){
    	mPhoneticLastName = phoneticLastName;
    }

    /**
     * @hide
     */
    public String getPhoneticFirstName() {
        return mPhoneticFirstName;
    }
    
    public void setPhoneticFirstName(String phoneticFirstName){
    	mPhoneticFirstName = phoneticFirstName;
    }

    /**
     * @hide
     */
    public String getPhoneticMiddleName() {
        return mPhoneticMiddleName;
    }
    
    public void setPhoneticMiddleName(String phoneticMiddleName){
    	mPhoneticMiddleName = phoneticMiddleName;
    }

    /**
     * @hide
     */
    public String getPhoneticFullName() {
        return mPhoneticFullName;
    }
    
    public void addNickNameList(String nickName){
    	if(mNickNameList == null){
    		mNickNameList = new ArrayList<String>();
    	}
    	if(!TextUtils.isEmpty(nickName)){
    	    mNickNameList.add(nickName);
    	}
    }

    public final List<String> getNickNameList() {
        return mNickNameList==null? Collections.EMPTY_LIST: mNickNameList;
    }
    
    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getDisplayName() {
        if(TextUtils.isEmpty(mDisplayName)){
            return (TextUtils.isEmpty(mLastName) ? "" : mLastName)
                    + (TextUtils.isEmpty(mMiddleName) ? "" : mMiddleName)
                    + (TextUtils.isEmpty(mFirstName) ? "" : mFirstName);
        }
        return mDisplayName;
    }
    
    /**
     * get favorite value
     * @return boolean
     */
    public boolean isFavorite() {
		return mIsFavorite;
	}
    
    /**
     * set Favorite value
     * @param mIsFavorite
     */
	public void setFavorite(boolean mIsFavorite) {
		this.mIsFavorite = mIsFavorite;
	}

	/**
     * set Birthday Date
     * @param birthday
     */
    public void setBirthday(Date birthday) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mBirthday = sdf.format(birthday);
    }
    
    /**
     * set Birthday String
     * @param Birthday String
     */
	public void setBirthday(String birthday) {
		mBirthday = birthday;
	}
	
    /**
     * get Birthday
     */
    public String getBirthday() {
        return mBirthday;
    }
    
    /**
     * set Block
     * @param isBlock 
     */
    public void setBlock(boolean isBlock) {
    	mIsBlock = isBlock;
    }
    
    /**
     * get Block
     */
    public boolean isBlock() {
        return mIsBlock;
    }
    
    /**
     * set ringTone_file
     * @param ringtone_file
     */ 
	public void setRingtoneFile(String ringtone_file) {
		mRingtoneFile = ringtone_file;
	}
	
    /**
     * get RingtoneFile
     */
	public String getRingtoneFile() {
		return mRingtoneFile;
	}

    /**
     * get PhotoList
     */
    public final List<PhotoData> getPhotoList() {
        return mPhotoList==null? Collections.EMPTY_LIST: mPhotoList;
    }

    /**
     * get NotesList
     */
    public final List<String> getNotes() {
        return mNoteList==null? Collections.EMPTY_LIST: mNoteList;
    }
    
    /**
     * get PhoneList
     */
    public final List<PhoneData> getPhoneList() {
        return mPhoneList==null? Collections.EMPTY_LIST: mPhoneList;
    }
    
    /**
     * get EmailList
     */
    public final List<EmailData> getEmailList() {
        return mEmailList==null? Collections.EMPTY_LIST: mEmailList;
    }
    
    /**
     * get Postal List
     */
    public final List<PostalData> getPostalList() {
        return mPostalList==null? Collections.EMPTY_LIST: mPostalList;
    }
    
    /**
     * get ORGList
     */
    public final List<OrganizationData> getOrganizationList() {
        return mOrganizationList==null? Collections.EMPTY_LIST: mOrganizationList;
    }
    
    /**
     * get ImList
     */
    public final List<ImData> getImList() {
        return mImList==null? Collections.EMPTY_LIST: mImList;
    }

    /**
     * get WebSiteList
     */
    public final List<WebsiteData> getWebsiteList() {
        return mWebsiteList==null? Collections.EMPTY_LIST : mWebsiteList;
    }
    
    /**
     * get GroupList
     */
    public final List<String> getGroupList() {
        return mGroupList==null? Collections.EMPTY_LIST : mGroupList;
    }

    /**
     *  add company to org
     * @param company
     * @param unit
     */
	public void setBaseOrganization(String company, String department){
		if(mBaseOrganization == null){
			mBaseOrganization = new OrganizationData(Organization.TYPE_WORK, company, null, department, null, false);// mark
    	}
		mBaseOrganization.setCompany(company);
		mBaseOrganization.setDepartment(department);
	}
	
	/**
	 * parse ORG property,we create one OrganizationData.
	 * @param company
	 * @param department
	 */
	public void setOrg_Organization(String company, String department){
		mBaseOrganization = new OrganizationData(Organization.TYPE_WORK, company, null, department, null, false);// mark
		 if (mOrganizationList == null) {
	            mOrganizationList = new ArrayList<OrganizationData>();
	     }
		 mOrganizationList.add(mBaseOrganization);
	}
	
	public OrganizationData getBaseOrganization(){
		return mBaseOrganization;
	}
	
    /**
     * Add position info to OrgList
     * @param positionValue
     */
    public void setTitle(String titleValue) {
    	if(mBaseOrganization == null){
			mBaseOrganization = new OrganizationData(Organization.TYPE_WORK, null, titleValue, null,  null, false);
    	}
		mBaseOrganization.setPosition(titleValue);
    }
    
    /**
     * Add a phone info to phoneList.
     * @param data phone number
     * @param type type col of content://contacts/phones
     * @param label lable col of content://contacts/phones
     */
    public void addPhone(int type, String data, String label, boolean isPrimary){
        if (mPhoneList == null) {
            mPhoneList = new ArrayList<PhoneData>();
        }
        PhoneData phoneData = new PhoneData(type,
        		data,
                label, isPrimary);

        mPhoneList.add(phoneData);
    }
    
    public void addPhone(PhoneData phoneData ){
        if (mPhoneList == null) {
            mPhoneList = new ArrayList<PhoneData>();
        }

        mPhoneList.add(phoneData);
    }
    
    
    /**
     * Add a Email info to EmailList
     * @param type
     * @param data 
     * @param label
     * @param isPrimary
     */
    public void addEmail(int type, String data, String label, boolean isPrimary){
        if (mEmailList == null) {
            mEmailList = new ArrayList<EmailData>();
        }
        mEmailList.add(new EmailData(type, data, label, isPrimary));
    }
    
    /**
     * Add adr info to PostalList
     * @param type
     * @param propValueList
     * @param label
     * @param isPrimary
     */
    public void addPostal(int type, List<String> propValueList, String label, boolean isPrimary){
        if (mPostalList == null) {
            mPostalList = new ArrayList<PostalData>();
        }
        mPostalList.add(new PostalData(type, propValueList, label, isPrimary));
    }
    
    public void addPostal(PostalData data){
        if (mPostalList == null) {
            mPostalList = new ArrayList<PostalData>();
        }
        mPostalList.add(data);
    }
    
    /**
     * Add ORG info to OrgList with label
     * @param type
     * @param companyName
     * @param positionName
     * @param isPrimary
     */
    public void addOrganization(int type, final String companyName,
            final String positionName, String department, String label, boolean isPrimary) {
        if (mOrganizationList == null) {
            mOrganizationList = new ArrayList<OrganizationData>();
        }
        mOrganizationList.add(new OrganizationData(type, companyName, positionName, department,  label,isPrimary));
    }
    
    /**
     * Add ORG info to OrgList wihtout label
     * @param type
     * @param companyName
     * @param positionName
     * @param isPrimary
     */
    public void addOrganization(int type, final String companyName,
            final String positionName, String department,boolean isPrimary) {
        if (mOrganizationList == null) {
            mOrganizationList = new ArrayList<OrganizationData>();
        }
        mOrganizationList.add(new OrganizationData(type, companyName, positionName, department,null,isPrimary));
    }
    
	/**
	 * add org to orglist
	 * @param org
	 */
    public void addOrganization(OrganizationData org) {
        if (mOrganizationList == null) {
            mOrganizationList = new ArrayList<OrganizationData>();
        }
        mOrganizationList.add(org);
    }
    
    /**
     * Add IM info to IMList
     * @param type
     * @param data
     * @param label
     * @param isPrimary
     */
    public void addIm(int type, String data, String label, String customProtocol,boolean isPrimary) {
        if (mImList == null) {
            mImList = new ArrayList<ImData>();
        }
        mImList.add(new ImData(type, data, label,customProtocol, isPrimary));
    }
    
    public void addIm(int type, String data, String label,boolean isPrimary) {
        if (mImList == null) {
            mImList = new ArrayList<ImData>();
        }
        mImList.add(new ImData(type, data, label,null,isPrimary));
    }
    
    /**
     * Add Note info to NoteList
     * @param note
     */
    public void addNote(final String note) {
        if (mNoteList == null) {
            mNoteList = new ArrayList<String>(1);
        }
        mNoteList.add(note);
    }
    
    /**
     * Add Photo info to PhotoList
     * @param formatName
     * @param photoBytes
     */
    public void addPhotoBytes(String formatName, byte[] photoBytes) {
        if (mPhotoList == null) {
            mPhotoList = new ArrayList<PhotoData>(1);
        }
        final PhotoData photoData = new PhotoData(0, null, photoBytes);
        mPhotoList.add(photoData);
    }
    
    /**
     * Add a Email info to EmailList
     * @param type
     * @param data 
     * @param label
     * @param isPrimary
     */
    public void addWebsite(int type, String data, String label, boolean isPrimary){
        if (mWebsiteList == null) {
        	mWebsiteList = new ArrayList<WebsiteData>();
        }
        mWebsiteList.add(new WebsiteData(type, data, label, isPrimary));
    }
    
    /**
     * Add website info to WebsiteList
     * @param website
     */
    public void addWebsite(final WebsiteData website) {
        if (mWebsiteList == null) {
        	mWebsiteList = new ArrayList<WebsiteData>(1);
        }
        mWebsiteList.add(website);
    }
    
    /**
     * Add group info to GroupList
     * @param group
     */
    public void addGroup(final String group) {
        if (mGroupList == null) {
        	mGroupList = new ArrayList<String>();
        }
        if(!TextUtils.isEmpty(group)){
        	mGroupList.add(group);
        }
    }
    
    public void setBorqsUid(String uid){
    	mBorqsUid = uid;
    }
    
    public String getBorqsUid(){
    	return mBorqsUid;
    }
    
    public void setBorqsName(String name){
    	mBorqsName = name;
    }
    
    public String getBorqsName(){
    	return mBorqsName;
    }
    
}
