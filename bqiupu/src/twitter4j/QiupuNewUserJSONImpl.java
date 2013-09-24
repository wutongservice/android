package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.QiupuUser.PerhapsName;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;

public class QiupuNewUserJSONImpl extends QiupuUser {
	private static final long serialVersionUID = 6662628759814282916L;

	final static String TAG = "QiupuNewUserJSONImpl";
	private String login_phone1 = "";
	private String login_phone2 = "";
	private String login_phone3 = "";
	private String login_email1 = "";
	private String login_email2 = "";
	private String login_email3 = "";

	public QiupuNewUserJSONImpl(JSONObject obj) throws TwitterException {
		try {
			if(obj.has("user_id")){
				uid = obj.getLong("user_id");
			}
			if(obj.has("display_name")){
				nick_name = obj.getString("display_name");
			}
			//login name 
			if(obj.has("login_phone1")){
				login_phone1 = obj.getString("login_phone1");
			}
			if(obj.has("login_phone2")){
				login_phone2 = obj.getString("login_phone2");
			}
			if(obj.has("login_phone3")){
				login_phone3 = obj.getString("login_phone3");
			}
			if(obj.has("login_email1")){
				login_email1 = obj.getString("login_email1");
			}
			if(obj.has("login_email2")){
				login_email2 = obj.getString("login_email2");
			}
			if(obj.has("login_email3")){
				login_email3 = obj.getString("login_email3");
			}
			
			//parse contactInfo
			if(obj.has("contact_info")) {
			    parseContactInfo(obj.getJSONObject("contact_info"));
			}
//			//contact info
//			if(obj.has("contact_info")) {
//				JSONObject contactInfo = obj.getJSONObject("contact_info");
//				if(contactInfo.has("email_address"))
//				{
//					contact_email1 = contactInfo.getString("email_address");
//				}
//				if(contactInfo.has("email_2_address"))
//				{
//					contact_email2 = contactInfo.getString("email_2_address");
//				}
//				if(contactInfo.has("email_3_address"))
//				{
//					contact_email3 = contactInfo.getString("email_3_address");
//				}
//				if(contactInfo.has("mobile_telephone_number"))
//				{
//					contact_phone1 = contactInfo.getString("mobile_telephone_number");
//				}
//				if(contactInfo.has("mobile_2_telephone_number"))
//				{
//					contact_phone2 = contactInfo.getString("mobile_2_telephone_number");
//				}
//				if(contactInfo.has("mobile_3_telephone_number"))
//				{
//					contact_phone3 = contactInfo.getString("mobile_3_telephone_number");
//				}
//			}
			if(obj.has("his_friend")){
				his_friend = obj.getBoolean("his_friend");
			}
			if(obj.has("company")) {
				company = obj.getString("company");
			}
			if(obj.has("department")) {
				department = obj.getString("department");
			}
			if(obj.has("office_address")) {
				office_address = obj.getString("office_address");
			}
//			this.province = obj.getInt("province");
//			this.city = obj.getInt("city");
			if(obj.has("created_time")) {
				created_at = obj.getLong("created_time");
			}
			if(obj.has("last_visited_time")) {
				last_visit_time = obj.getLong("last_visited_time");
			}
//			if(obj.has("domain_name")) {
//				domain = obj.getString("domain_name");
//			}
			if(obj.has("image_url")) {
				profile_image_url = obj.getString("image_url");
			}
			if(obj.has("small_image_url")) {
				profile_simage_url = obj.getString("small_image_url");
			}
			if(obj.has("large_image_url")) {
				profile_limage_url = obj.getString("large_image_url");
			}
			if(obj.has("bidi")) {
				bidi = obj.getBoolean("bidi");
			}
			if(obj.has("status")) {
				status = obj.getString("status");
			}
			if(obj.has("birthday")) {
				date_of_birth = obj.getString("birthday");
			}
			
//			this.verify_code = obj.getInt("verify_code");
//			this.verified = obj.getInt("verified");
//			this.location = obj.getString("location");
			// this.description=obj.getString("description");
//			this.url = obj.getString("url");
			
			//profile_privacy
			if(obj.has("profile_privacy")) {
				profile_privacy = obj.getBoolean("profile_privacy");
			}
			
			//pedding_requests
			if(obj.has("pedding_requests")) {
				JSONArray peddingObject = obj.getJSONArray("pedding_requests");
				if(peddingObject.length() <= 0)
				{
					pedding_requests = "";
				}
				else
				{
					StringBuilder idbuilder = new StringBuilder();
					for (int i = 0; i < peddingObject.length(); i++) {
						int  typeid;
						try {
							typeid = peddingObject.getInt(i);
							if(idbuilder.length() > 0)
							{
								idbuilder.append(",");
							}
							idbuilder.append(typeid);
						} catch (JSONException e) {
							throw new TwitterException(e);
						}
						pedding_requests = idbuilder.toString();
					}
				}
			}
			
			if(obj.has("job_title")) {
				jobtitle = obj.getString("job_title");
			}
			if(obj.has("status_updated_time")) {
				status_time = obj.getLong("status_updated_time");
			}
			if(obj.has("gender")) {
				gender = obj.getString("gender");
			}
//			if(obj.has("email")) {
//				email = obj.getString("email");
//			}
			if(obj.has("friends_count")) {
				friends_count = obj.getLong("friends_count");
			}
			if(obj.has("followers_count")) {
				followers_count = obj.getLong("followers_count");
			}
			if(obj.has("favorites_count")) {
				favorites_count = obj.getLong("favorites_count");
			}
			if(obj.has("apps_count")) {
				app_count = obj.getLong("apps_count");
			}
			
			if(obj.has("remark")){
			    remark = obj.optString("remark");
			}
			//circle
			if(obj.has("in_circles")) {
				JSONArray circlesArr = obj.getJSONArray("in_circles");
				createCircleInfo(circlesArr);
			}
			
			//address
			if(obj.has("address")) {
				addressInfo = new QiupuUser.Address.AddressInfo();
//				addressInfo = new QiupuUser.Address();
				JSONArray addressArr = obj.getJSONArray("address");
				JSONObject addressobj;
				if(addressArr.length() > 0) {
					addressobj = addressArr.getJSONObject(0);
					addressInfo = createAddressItemResponse(addressobj);
					location = addressInfo.toString();
				}
			}
			
			//workInfo
			if(obj.has("work_history")) {
				JSONArray workArr = obj.getJSONArray("work_history");
				for(int i=0;i<workArr.length();i++) {
					JSONObject workObj = workArr.getJSONObject(i);
					work_history_list.add(createWorkExprienceItemResponse(workObj,uid));
				}
			}
			
			//educationInfo
			if(obj.has("education_history")) {
				JSONArray eduArr = obj.getJSONArray("education_history");
				for(int i=0;i<eduArr.length();i++) {
					JSONObject eduObj = eduArr.getJSONObject(i);
					education_list.add(createEducationItemResponse(eduObj,uid));
				}
			}
			// share resource
			if(obj.has("shared_count")) {
				JSONObject shareres = obj.getJSONObject("shared_count");
				sharedResource = createShareResourceItemResponse(shareres);
			}
			
			//profile_friends
			if(obj.has("profile_friends")) {
				JSONArray friendsArr = obj.getJSONArray("profile_friends");
				if(friendsArr.length()>0) {
					friendsImageList = new ArrayList<UserImage>();
				}
				for(int i=0;i<friendsArr.length();i++) {
					JSONObject friendsObj = friendsArr.getJSONObject(i);
					friendsImageList.add(createUserImageItemResponse(friendsObj,uid));
				}
			}
			//profile_fans
			if(obj.has("profile_followers")) {
				JSONArray fansArr = obj.getJSONArray("profile_followers");
				if(fansArr.length()>0) {
					fansImageList = new ArrayList<UserImage>();
				}
				for(int i=0;i<fansArr.length();i++) {
					JSONObject fansObj = fansArr.getJSONObject(i);
					fansImageList.add(createUserImageItemResponse(fansObj,uid));
				}
			}
			//profile_shared_photos
			if(obj.has("profile_shared_photos")) {
				JSONArray shareArr = obj.getJSONArray("profile_shared_photos");
				if(shareArr.length()>0) {
					shareImageList = new ArrayList<SharedPhotos>();
				}
				for(int i=0;i<shareArr.length();i++) {
					JSONObject shareObj = shareArr.getJSONObject(i);
					shareImageList.add(createSharedPhotosResponse(shareObj,uid));
				}
			}
			
			parsePerHapsName(obj, perhapsNames);
			//perhaps_name
//            if(obj.has("perhaps_name")) {
//                JSONArray nameArr = obj.getJSONArray("perhaps_name");
//                if(nameArr.length() > 0) {
//                    perhapsNames = new ArrayList<PerhapsName>();
//                }
//                for(int i=0;i<nameArr.length();i++) {
//                    JSONObject nameObj = nameArr.getJSONObject(i);
//                    perhapsNames.add(createperhapsNameResponse(nameObj));
//                }
//            }

            if (obj.has("distance")) {
                distance = obj.getString("distance");
            }

		} catch (JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	public QiupuNewUserJSONImpl() {
	}
	
	public static UserImage createUserImageItemResponse(JSONObject obj,long uid) throws TwitterException {
		UserImage info = new UserImage();
//		try {	
		info.user_id = obj.optLong("user_id");
		info.image_url = obj.optString("image_url");
		info.userName = obj.optString("display_name");
		
//		} catch (JSONException jsone) {}
		
		return info;
	}
	
	public static UserImage createUserImageItemFromCircle(JSONObject obj,long uid) throws TwitterException {
		UserImage info = new UserImage();
		info.user_id = obj.optLong("circle_id");
		info.image_url = obj.optString("image_url");
		info.userName = obj.optString("circle_name");
		
		return info;
	}
	private static SharedPhotos createSharedPhotosResponse(JSONObject obj,long uid) throws TwitterException {
		SharedPhotos info = new SharedPhotos();
		try {	
			info.post_id = obj.getLong("post_id");
			info.photo_img_middle = obj.getString("photo_img_middle");
			
		} catch (JSONException jsone) {}
		
		return info;
	}
	private static WorkExperience createWorkExprienceItemResponse(JSONObject obj,long uid) throws TwitterException {
		WorkExperience weinfo = new WorkExperience();
		try {	
			weinfo.uid = uid;
			weinfo.from = obj.getString("from");
			weinfo.to = obj.getString("to");
			weinfo.company = obj.getString("company");
			weinfo.office_address = obj.getString("address");
			weinfo.job_title = obj.getString("title");
			weinfo.department = obj.getString("profession");
			weinfo.job_description = obj.getString("job_description");
			
        } catch (JSONException jsone) {}
        
        return weinfo;
	}
	
	private static Education createEducationItemResponse(JSONObject obj,long uid) throws TwitterException {
		Education edu = new Education();
		try {	
			edu.uid = uid;
			edu.from = obj.getString("from");
			edu.to = obj.getString("to");
			edu.school = obj.getString("school");
			if(obj.has("school_location")) {
				edu.school_location = obj.getString("school_location");
			}
			edu.type = obj.getString("type");
			edu.school_class = obj.getString("class");
			edu.degree = obj.getString("degree");
			edu.major = obj.getString("major");
			
        } catch (JSONException jsone) {}
        
        return edu;
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
	
	private static Circle.CircleInfo createCircleItemResponse(JSONObject obj) throws TwitterException {
		Circle.CircleInfo circleinfo = new Circle.CircleInfo();
		try {	
			circleinfo.circle_id  = obj.getString("circle_id");
			circleinfo.circle_name = obj.getString("circle_name");
			
        } catch (JSONException jsone) {}
        
        return circleinfo;
	}
	
	public static ArrayList<QiupuUser> createQiupuUserList(HttpResponse response)
			throws TwitterException {
		ArrayList<QiupuUser> list = new ArrayList<QiupuUser>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createQiupuUserList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new QiupuNewUserJSONImpl(obj));
		}

		return list;
	}


	public static ArrayList<QiupuUser> getUserYouMayKnow(HttpResponse response)
			throws TwitterException {
		ArrayList<QiupuUser> list = new ArrayList<QiupuUser>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}

		Log.d(TAG, "createQiupuUserList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(createRecommendUserResponse(obj));
		}

		return list;
	}

	private static QiupuUser createRecommendUserResponse(JSONObject obj) throws TwitterException {
        Recommendation qiupuUser = new Recommendation();
        qiupuUser.uid = obj.optLong("user_id");
        qiupuUser.nick_name = obj.optString("display_name");
        qiupuUser.profile_image_url = obj.optString("image_url");
        qiupuUser.suggest_type = obj.optInt("suggest_type");
        qiupuUser.profile_privacy = obj.optBoolean("profile_privacy");

        //perhaps_name
        if (obj.has("perhaps_name")) {
            JSONArray nameArr = obj.optJSONArray("perhaps_name");
            final int nameSize = null == nameArr ? 0 : nameArr.length();
            if (nameSize > 0) {
                qiupuUser.perhapsNames = new ArrayList<PerhapsName>();
                for (int i = 0; i < nameSize; i++) {
                    JSONObject nameObj = nameArr.optJSONObject(i);
                    qiupuUser.perhapsNames.add(createperhapsNameResponse(nameObj));
                }
            }
        }

        //pedding_requests
        try {
            JSONArray peddingObject = obj.getJSONArray("pedding_requests");
            if (peddingObject.length() <= 0) {
                qiupuUser.pedding_requests = "";
            } else {
                StringBuilder idbuilder = new StringBuilder();
                for (int i = 0; i < peddingObject.length(); i++) {
                    int typeid;
                    try {
                        typeid = peddingObject.getInt(i);
                        if (idbuilder.length() > 0) {
                            idbuilder.append(",");
                        }
                        idbuilder.append(typeid);
                    } catch (JSONException e) {
                        throw new TwitterException(e);
                    }
                    qiupuUser.pedding_requests = idbuilder.toString();
                }
            }
        } catch (Exception ne) {
        }
//			int suggest_type = obj.getInt("suggest_type");
        if (qiupuUser.suggest_type == USER_SUGGEST_TYPE_RECOMMENDER
                || qiupuUser.suggest_type == USER_SUGGEST_TYPE_BOTH_KNOW
                || qiupuUser.suggest_type == USER_SUGGEST_TYPE_FROM_BOTH_ADDRESSBOOK
                || qiupuUser.suggest_type == USER_SUGGEST_TYPE_FROM_MY_ADDRESSBOOK
                || qiupuUser.suggest_type == USER_SUGGEST_FROM_WORK_INFO
                || qiupuUser.suggest_type == USER_SUGGEST_FROM_EDUCATION_INFO) {
            try {
                qiupuUser.recommendUser = new RecommendUser();
                JSONArray recommend = obj.getJSONArray("suggest_reason");
                for (int i = 0; i < recommend.length(); i++) {
                    JSONObject js;
                    try {
                        js = recommend.getJSONObject(i);
                    } catch (JSONException e) {
                        throw new TwitterException(e);
                    }
                    qiupuUser.recommendUser.friends.add(createSimpleUserResponse(js));
                }
            } catch (Exception e) {
            }
        } else {
            Log.d(TAG, "unsupported type: have my contact , system recommend or request");
        }

        return qiupuUser;
    }


//	private static QiupuUser createRecommendSimpleUserResponse(JSONObject obj)
//			throws TwitterException {
//		QiupuUser qiupuUser = new QiupuUser();
//		try {
//			qiupuUser.uid = obj.getLong("user_id");
//			qiupuUser.nick_name = obj.getString("display_name");
//			qiupuUser.profile_image_url = obj.getString("image_url");
//
//			try
//			{
//				qiupuUser.app_count = obj.getInt("app_count");
//			} catch (Exception e)
//			{}
//			try {
//				qiupuUser.recommendUser = new RecommendUser();
//				JSONArray recommend = obj.getJSONArray("recommender");
//				for (int i = 0; i < recommend.length(); i++) {
//					JSONObject js;
//					try {
//						js = recommend.getJSONObject(i);
//					} catch (JSONException e) {
//						throw new TwitterException(e);
//					}
//					qiupuUser.recommendUser.friends.add(createRecommendSimpleUserResponse(js));
//				}
//			} catch (Exception e) {
//			}
//		} catch (JSONException jsone) {
//			throw new TwitterException(jsone.getMessage());
//		}
//
//		return qiupuUser;
//	}

	public static QiupuUser creatFriend(HttpResponse response)
			throws TwitterException {
		JSONObject obj = null;
		try {
			obj = response.asJSONObject();
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}

		return new QiupuNewUserJSONImpl(obj);
	}
	
	public static QiupuUser creatUsersInfo(HttpResponse response)
	throws TwitterException {
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}

		QiupuUser user = new QiupuUser();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			user = new QiupuNewUserJSONImpl(obj);
		}
		return user;
	}
	
	public static ArrayList<QiupuSimpleUser> createInstallUserList(HttpResponse response) throws TwitterException {
		ArrayList<QiupuSimpleUser> list = new ArrayList<QiupuSimpleUser>();
		JSONArray array = null;
		try{
			array = response.asJSONArray();
		}catch(TwitterException e){
			throw e;
		}
		
		Log.d(TAG, "createQiupuUserList size:"+array.length());
		for(int i=0;i<array.length();i++){
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
            list.add(createSimpleUserResponse(obj));
//			list.add(createRecommendSimpleUserResponse(obj));
		}
		
		return list;
	}
	

//	private static ArrayList<ShareSourceItem> createShareResourceItemResponse(JSONObject obj) throws TwitterException {
//		ArrayList<ShareSourceItem> allres = new ArrayList<ShareSourceItem>();
//		ShareSourceItem shared; 
//		try {	
//			if(obj.has("shared_text")){
//				shared = new ShareSourceItem("");
//				shared.mType = BpcApiUtils.TEXT_POST;
//				shared.mCount = obj.getInt("shared_text");
//				allres.add(shared);
//			}
//			
//			if(obj.has("shared_photo")){
//				shared = new ShareSourceItem("");
//				shared.mType = BpcApiUtils.IMAGE_POST;
//				shared.mCount = obj.getInt("shared_photo");
//				allres.add(shared);
//			}
			
//			if(obj.has("shared_book")){
//				shared = new ShareSourceItem("");
//				shared.mType = BpcApiUtils.BOOK_POST;
//				shared.mCount = obj.getInt("shared_book");
//				allres.add(shared);
//			}
			
//			if(obj.has("shared_apk")){
//				shared = new ShareSourceItem("");
//				shared.mType = BpcApiUtils.APK_POST;
//				shared.mCount = obj.getInt("shared_apk");
//				allres.add(shared);
//			}
//			
//			if(obj.has("shared_link")){
//				shared = new ShareSourceItem("");
//				shared.mType = BpcApiUtils.LINK_POST;
//				shared.mCount = obj.getInt("shared_link");
//				allres.add(shared);
//			}
//			
//        } catch (JSONException jsone) {}
//        
//        return allres;
//	}
	
	private void createCircleInfo(JSONArray circlesArr) throws TwitterException { 
		QiupuUser.Circle circles = new QiupuUser.Circle();
		StringBuilder idbuilder = new StringBuilder();
		StringBuilder namebuilder = new StringBuilder();
		for (int i = 0; i < circlesArr.length(); i++) {
			JSONObject circleobj;
			try {
				circleobj = circlesArr.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			Circle.CircleInfo info = createCircleItemResponse(circleobj);
			idbuilder.append(info.circle_id);
			if(!QiupuHelper.inLocalCircle(info.circle_id))
			{
				if(namebuilder.length() > 0)
				{
					namebuilder.append(",");
				}
				namebuilder.append(info.circle_name);
			}
			if(i < circlesArr.length()-1)
			{
				idbuilder.append(",");
			}
			circles.circleList.add(info);
		}
		this.circleId = idbuilder.toString();
		this.circleName = namebuilder.toString();
	}
	
	public static ArrayList<QiupuSimpleUser> creatQiupuSimpleInfoList(HttpResponse response) throws TwitterException {
		JSONArray array = null;
		ArrayList<QiupuSimpleUser> allusers = new ArrayList<QiupuSimpleUser>();
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}

		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			allusers.add(createSimpleUserResponse(obj)); 
		}
		return allusers;
	}
	
    public static QiupuSimpleUser createSimpleUserResponse(JSONObject obj) throws TwitterException {
        QiupuSimpleUser qiupuUser = new QiupuSimpleUser();
        try {
            qiupuUser.uid = obj.getLong("user_id");
            qiupuUser.nick_name = obj.getString("display_name");
            qiupuUser.profile_image_url = obj.getString("image_url");
            // TODO: for recommend show(the value is null at present)
            qiupuUser.contact_method = obj.optString("contact_method");
            qiupuUser.work_background = obj.optString("work_background");
            qiupuUser.education_background = obj.optString("education_background");

        } catch (JSONException jsone) {}

        return qiupuUser;
    }
    
    private PhoneEmailInfo createPhoneEmailList(String content, String type) {
        PhoneEmailInfo phoneEmail = new PhoneEmailInfo();
        phoneEmail.uid = uid;
        phoneEmail.type = type;
        phoneEmail.info = content;
        if(uid == AccountServiceUtils.getBorqsAccountID()) {
            phoneEmail.isbind = isbind(content);
        }
        return phoneEmail;
    }
    
    private void parseContactInfo(JSONObject contactInfo) {
        try {
            if(contactInfo.has("email_address")) {
                String contact_email1 = contactInfo.getString("email_address");
                emailList.add(createPhoneEmailList(contact_email1, QiupuConfig.TYPE_EMAIL1));
            }
            if(contactInfo.has("email_2_address")) {
                String contact_email2 = contactInfo.getString("email_2_address");
                emailList.add(createPhoneEmailList(contact_email2, QiupuConfig.TYPE_EMAIL2));
            }
            if(contactInfo.has("email_3_address")) {
                String contact_email3 = contactInfo.getString("email_3_address");
                emailList.add(createPhoneEmailList(contact_email3, QiupuConfig.TYPE_EMAIL3));
            }
            if(contactInfo.has("mobile_telephone_number")) {
                String contact_phone1 = contactInfo.getString("mobile_telephone_number");
                phoneList.add(createPhoneEmailList(contact_phone1, QiupuConfig.TYPE_PHONE1));
            }
            if(contactInfo.has("mobile_2_telephone_number")) {
                String contact_phone2 = contactInfo.getString("mobile_2_telephone_number");
                phoneList.add(createPhoneEmailList(contact_phone2, QiupuConfig.TYPE_PHONE2));
            }
            if(contactInfo.has("mobile_3_telephone_number")) {
                String contact_phone3 = contactInfo.getString("mobile_3_telephone_number");
                phoneList.add(createPhoneEmailList(contact_phone3, QiupuConfig.TYPE_PHONE3));
            }
        } catch (JSONException jsone) {}
    }
    
    private boolean isbind(String phoneEmail) {
        return phoneEmail.equals(login_phone1) || 
                phoneEmail.equals(login_phone2) || 
                phoneEmail.equals(login_phone3) || 
                phoneEmail.equals(login_email1) || 
                phoneEmail.equals(login_email2) || 
                phoneEmail.equals(login_email3) ; 
    }
    
    public static PerhapsName createperhapsNameResponse(JSONObject obj) throws TwitterException {
        PerhapsName info = new PerhapsName();
        info.name = obj.optString("fullname");
        info.count = obj.optInt("count");
        
        return info;
    }
    
    public static void parsePerHapsName(JSONObject obj, ArrayList<PerhapsName> perhapsNames) {
        if(obj.has("perhaps_name")) {
            JSONArray nameArr;
            try {
                nameArr = obj.getJSONArray("perhaps_name");
                if(nameArr.length() > 0) {
                    perhapsNames = new ArrayList<PerhapsName>();
                }
                for(int i=0;i<nameArr.length();i++) {
                    JSONObject nameObj = nameArr.getJSONObject(i);
                    perhapsNames.add(QiupuNewUserJSONImpl.createperhapsNameResponse(nameObj));
                }
            } catch (Exception e) { }
            
        }
    }
    
    public static String createProfileImageUrlRespose(HttpResponse response) throws TwitterException {
    	String profile_image = null;
    	try {
			JSONObject obj = response.asJSONObject();
			profile_image = obj.optString("image_url");
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}
    	return profile_image;
    }
}
