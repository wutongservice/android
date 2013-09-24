package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.util.StringUtil;

public class QiupuAccountInfo extends QiupuSimpleUser implements java.io.Serializable {
	private static final long serialVersionUID = 5407931216597307323L;

    public String company;
   	public String department;
   	public String office_address;
	public String description;
	public Address.AddressInfo addressInfo;
	public String jobtitle;
    public ArrayList<PhoneEmailInfo>  phoneList;
   	public ArrayList<PhoneEmailInfo>  emailList;
    public ArrayList<ShareSourceItem> sharedResource;

	public QiupuAccountInfo() {
		super();
		sharedResource =  new ArrayList<ShareSourceItem>();

		phoneList = new ArrayList<PhoneEmailInfo>();
		emailList = new ArrayList<PhoneEmailInfo>();
	}

	public String toString() {
		return "company" + company + 
				" department           = "+department +
				"office_address     = "+ office_address +
	           " description          = " +description+
	           " location         = "+location;
	}
	public void despose() {
		super.despose();

		if(addressInfo != null)
		{
			addressInfo.despose();
		    addressInfo = null;
		}

		company = null;
		department = null;
		office_address = null;
		description = null;

		jobtitle = null;

		sharedResource.clear();
		sharedResource = null;

		if(phoneList != null) {
		    phoneList.clear();
		    phoneList = null;
		}
		if(emailList != null) {
			emailList.clear();
			emailList = null;
		}
	}

	// TODO why compare with nick_name not with uid.
	public int compareTo(QiupuAccountInfo another) {
		if (QiupuAccountInfo.class.isInstance(another)) {
			String tnickname = another.nick_name;
			return nick_name.compareToIgnoreCase(tnickname);
		}
		return 0;
	}

	public QiupuAccountInfo clone() {
	    return clone(null);
	}
	
	public QiupuAccountInfo clone(QiupuAccountInfo user) {
	    if(user == null) {
	        user = new QiupuAccountInfo();
	    }
        user.id = id;
        user.uid = uid;
        user.name = name;
        user.nick_name = nick_name;
        user.location = location;
        user.profile_image_url = profile_image_url;
        user.profile_limage_url = profile_limage_url;
        user.profile_simage_url = profile_simage_url;

        user.company = company;
        user.department = department;
        user.office_address = office_address;

        user.description = description;

        if(addressInfo != null)
            user.addressInfo = addressInfo.clone();

        user.name_pinyin = name_pinyin;
        user.jobtitle = jobtitle;


        for(int i=0;i<phoneList.size();i++) {
            user.phoneList.add(phoneList.get(i).clone());
        }
        for(int i=0;i<emailList.size();i++) {
            user.emailList.add(emailList.get(i).clone());
        }

        return user;
    }

    public static ContentValues toContentValues(QiupuAccountInfo userinfo) {
        ContentValues cv = new ContentValues();
        cv.put(UsersColumns.USERID, userinfo.uid);
        cv.put(UsersColumns.USERNAME, userinfo.name);

        cv.put(UsersColumns.NICKNAME, userinfo.nick_name);

        cv.put(UsersColumns.NAME_PINGYIN, QiupuORM.getPinyin(userinfo.nick_name));

        cv.put(UsersColumns.COMPANY, userinfo.company);
        cv.put(UsersColumns.OFFICE_ADDRESS, userinfo.office_address);
        cv.put(UsersColumns.DEPARTMENT, userinfo.department);
//        cv.put(UsersColumns.DOMAIN, userinfo.domain);
        cv.put(UsersColumns.PROFILE_IMAGE_URL, userinfo.profile_image_url);
        cv.put(UsersColumns.PROFILE_SIMAGE_URL, userinfo.profile_simage_url);
        cv.put(UsersColumns.PROFILE_LIMAGE_URL, userinfo.profile_limage_url);
        cv.put(UsersColumns.LOCATION, userinfo.location);
        cv.put(UsersColumns.DESCRIPTION, userinfo.description);
        //no need the following two, it is local save
        //cv.put(UsersColumns.SHORTCUT, isShortCut ? 1 : 0);
        cv.put(UsersColumns.JOB_TITLE, userinfo.jobtitle);
        //		cv.put(UsersColumns.WORK_HISTORY, work_history);
        return cv;
    }

    public static QiupuAccountInfo createUserInformation(QiupuAccountInfo result, Cursor cursor) {
//        QiupuAccountInfo result = new QiupuAccountInfo();
        result.uid = cursor.getLong(cursor.getColumnIndex(UsersColumns.USERID));
        result.name = cursor.getString(cursor.getColumnIndex(UsersColumns.USERNAME));

        result.nick_name = cursor.getString(cursor.getColumnIndex(UsersColumns.NICKNAME));
        result.name_pinyin = cursor.getString(cursor.getColumnIndex(UsersColumns.NAME_PINGYIN));
        result.company = cursor.getString(cursor.getColumnIndex(UsersColumns.COMPANY));
        result.office_address = cursor.getString(cursor.getColumnIndex(UsersColumns.OFFICE_ADDRESS));
        result.department = cursor.getString(cursor.getColumnIndex(UsersColumns.DEPARTMENT));
//        result.domain = cursor.getString(cursor.getColumnIndex(UsersColumns.DOMAIN));
        result.profile_image_url = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
        result.profile_simage_url = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_SIMAGE_URL));
        result.profile_limage_url = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_LIMAGE_URL));
        result.location = cursor.getString(cursor.getColumnIndex(UsersColumns.LOCATION));
        result.description = cursor.getString(cursor.getColumnIndex(UsersColumns.DESCRIPTION));

        result.name_pinyin = cursor.getString(cursor.getColumnIndex(UsersColumns.NAME_PINGYIN));
        result.jobtitle = cursor.getString(cursor.getColumnIndex(UsersColumns.JOB_TITLE));
        return result;
    }

    public final void parseQiupuAccountInfo(JSONObject obj) {
    	profile_image_url = obj.optString("image_url");
    	profile_limage_url = obj.optString("large_image_url");
    	profile_simage_url = obj.optString("small_image_url");
    	company = obj.optString("company");
    	department = obj.optString("department");
    	office_address = obj.optString("office_address");
    	jobtitle = obj.optString("jobtitle");
    	description = obj.optString("description");
        try {
        	if(obj.has("address")) {
        		addressInfo = new QiupuUser.Address.AddressInfo();
//			addressInfo = new QiupuUser.Address();
        		JSONArray addressArr = obj.optJSONArray("address");
        		if(addressArr != null && addressArr.length() > 0) {
        			JSONObject addressobj;
        			try {
        				addressobj = addressArr.getJSONObject(0);
        				addressInfo = createAddressItemResponse(addressobj);
        				location = addressInfo.toString();
        			} catch (JSONException e) { }
        		}
        	}
        	
        	if(obj.has("contact_info")) {
        		parseContactInfo(obj.optJSONObject("contact_info"));
        	}
        	
        	if(obj.has("shared_count")) {
        		JSONObject shareres = obj.optJSONObject("shared_count");
        		sharedResource = createShareResourceItemResponse(shareres);
        	}
		} catch (TwitterException e) {
			// TODO: handle exception
		}
        
    }
    
    private static Address.AddressInfo createAddressItemResponse(JSONObject obj) throws TwitterException {
		Address.AddressInfo addressinfo = new Address.AddressInfo();
		try {	
			addressinfo.type  = obj.getString("type");
			addressinfo.country = obj.getString("country");
			addressinfo.state = obj.getString("state");
			addressinfo.city = obj.getString("city");
			addressinfo.street = obj.getString("street");
			addressinfo.postal_code = obj.getString("postal_code");
			addressinfo.po_box = obj.getString("po_box");
			addressinfo.extended_address = obj.getString("extended_address");
			
        } catch (JSONException jsone) {}
        
        return addressinfo;
	}
    
    private void parseContactInfo(JSONObject contactInfo) {
    	try {
    		if(contactInfo.has("email_address")) {
    			String contact_email1 = contactInfo.getString("email_address");
    			if(StringUtil.isValidString(contact_email1)) {
    				emailList.add(createPhoneEmailList(contact_email1, QiupuConfig.TYPE_EMAIL1));
    			}
    		}
    		if(contactInfo.has("email_2_address")) {
    			String contact_email2 = contactInfo.getString("email_2_address");
    			if(StringUtil.isValidString(contact_email2)) {
    				emailList.add(createPhoneEmailList(contact_email2, QiupuConfig.TYPE_EMAIL2));
    			}
    		}
    		if(contactInfo.has("email_3_address")) {
    			String contact_email3 = contactInfo.getString("email_3_address");
    			if(StringUtil.isValidString(contact_email3)) {
    				emailList.add(createPhoneEmailList(contact_email3, QiupuConfig.TYPE_EMAIL3));
    			}
    		}
    		if(contactInfo.has("mobile_telephone_number")) {
    			String contact_phone1 = contactInfo.getString("mobile_telephone_number");
    			if(StringUtil.isValidString(contact_phone1)) {
    				phoneList.add(createPhoneEmailList(contact_phone1, QiupuConfig.TYPE_PHONE1));
    			}
    		}
    		if(contactInfo.has("mobile_2_telephone_number")) {
    			String contact_phone2 = contactInfo.getString("mobile_2_telephone_number");
    			if(StringUtil.isValidString(contact_phone2)) {
    				phoneList.add(createPhoneEmailList(contact_phone2, QiupuConfig.TYPE_PHONE2));
    			}
    		}
    		if(contactInfo.has("mobile_3_telephone_number")) {
    			String contact_phone3 = contactInfo.getString("mobile_3_telephone_number");
    			if(StringUtil.isValidString(contact_phone3)) {
    				phoneList.add(createPhoneEmailList(contact_phone3, QiupuConfig.TYPE_PHONE3));
    			}
    		}
    	} catch (JSONException jsone) {}
    }
    
    private PhoneEmailInfo createPhoneEmailList(String content, String type) {
        PhoneEmailInfo phoneEmail = new PhoneEmailInfo();
        phoneEmail.uid = uid;
        phoneEmail.type = type;
        phoneEmail.info = content;
        if(uid == AccountServiceUtils.getBorqsAccountID()) {
//            phoneEmail.isbind = isbind(content);    //TODO
        }
        return phoneEmail;
    }
    
    protected static ArrayList<ShareSourceItem> createShareResourceItemResponse(JSONObject obj) throws TwitterException {
		ArrayList<ShareSourceItem> allres = new ArrayList<ShareSourceItem>();
		ShareSourceItem shared; 
		try {	
			if(obj.has("shared_text")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.TEXT_POST;
				shared.mCount = obj.getInt("shared_text");
				allres.add(shared);
			}
			
			if(obj.has("shared_photo")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.IMAGE_POST;
				shared.mCount = obj.getInt("shared_photo");
				allres.add(shared);
			}
			
			if(obj.has("shared_book")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.BOOK_POST;
				shared.mCount = obj.getInt("shared_book");
				allres.add(shared);
			}
			
			if(obj.has("shared_apk")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.APK_POST;
				shared.mCount = obj.getInt("shared_apk");
				allres.add(shared);
			}
			
			if(obj.has("shared_link")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.LINK_POST;
				shared.mCount = obj.getInt("shared_link");
				allres.add(shared);
			}
			
			if(obj.has("shared_audio")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.AUDIO_POST;
				shared.mCount = obj.getInt("shared_audio");
				allres.add(shared);
			}
			
			if(obj.has("shared_video")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.VIDEO_POST;
				shared.mCount = obj.getInt("shared_video");
				allres.add(shared);
			}
			
			if(obj.has("shared_static_file")){
				shared = new ShareSourceItem("");
				shared.mType = BpcApiUtils.STATIC_FILE_POST;
				shared.mCount = obj.getInt("shared_static_file");
				allres.add(shared);
			}
			
        } catch (JSONException jsone) {}
        
        return allres;
	}
    
	public static class Address implements java.io.Serializable
	{
    	private static final long serialVersionUID = 1L;
    	public List<AddressInfo>  addressList;
    	
    	public Address()
    	{
    		addressList = new ArrayList<AddressInfo>();
    	}
    	
    	public void despose()
    	{
    		while(addressList.size() > 0)
    		{
    			AddressInfo item = addressList.get(0);    			
    			item.despose();
    			
    			addressList.remove(0);    			
    		}
    		
    		addressList = null;
    	}
    	
    	public static class AddressInfo implements Comparable, java.io.Serializable
    	{
    		private static final long serialVersionUID = 1L;
	    	public String type = "";
	    	public String country = "";
	    	
	    	public String state = "";
	    	public String city = "";
	    	
	    	public String street = "";
	    	public String postal_code = "";
	    	public String po_box = "";
	    	public String extended_address = "";
	    	
	    	public AddressInfo() {
			}
	    	
	    	
	    	public AddressInfo clone()
	    	{
	    		AddressInfo item  = new AddressInfo();
	    		item.type      = type;
	    		item.country   = country;
	    		item.state     = state;
	    		item.city      = city;
	    		item.street    = street;
	    		item.postal_code = postal_code;
	    		item.po_box      = po_box;
	    		item.extended_address = extended_address;
	    		
	    		return item;
	    	}
	    	
	    	public void despose()
	    	{
	    		type = null;
	    		country = null;
	    		state  = null;
	    		city = null;
	    		street = null;
	    		postal_code = null;
	    		po_box = null;
	    		extended_address = null;
	    	}
	    	public String toString()
	    	{
	    		return country +
	    		       state +
	    		       city +
	    		       street +
	    		       postal_code +
	    		       extended_address +
	    		       po_box +
	    		       extended_address;
	    	}
	    	
            public int compareTo(Object another) 
            {
            	return 0;
            }
            
            @Override public boolean equals(Object obj)
        	{
        		return false;		
        	}
    	}    	
	}
	

	public static class PhoneEmailInfo implements Comparable, java.io.Serializable
	{
	    private static final long serialVersionUID = 1L;
	    public String type;
	    public long uid;
	    public String info;
	    public boolean isbind;
	    
	    public PhoneEmailInfo clone() {
	        PhoneEmailInfo item  = new PhoneEmailInfo();
	        item.type       = type;
	        item.uid        = uid;
	        item.info       = info;
	        item.isbind     = isbind;
	        return item;
	    }
	    
	    public void despose() {
	        type = null;
	        info = null;
	    }
	    public String toString() {
	        return " type = "+type + " info = "+info + " isbind: " + isbind;
	    }
	    
	    public int compareTo(Object another)  {
	        return 0;
	    }
	    
	    @Override public boolean equals(Object obj) {
	        return false;       
	    }
	}       

}
