package twitter4j;

import java.util.ArrayList;


import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;

public class NewPlatformUserJSONImpl extends QiupuUser {
	private static final long serialVersionUID = 6662628759814282916L;

	final static String TAG = "NewPlatformUserJSONImpl";

	public NewPlatformUserJSONImpl(JSONObject obj) throws TwitterException {
		try {
			uid = obj.optLong("user_id");
			nick_name = obj.optString("display_name");
			his_friend = obj.optBoolean("his_friend");
			last_visit_time = obj.optLong("last_visited_time");
//			domain = obj.optString("domain_name");
			created_at = obj.optLong("created_time");

			//parse photo
			if(obj.has("photo")) {
			    parseUserPhoto(obj.getJSONObject("photo"));
			}
			
			//parse profile
			if(obj.has("profile")) {
			    parseUserProfile(obj.getJSONObject("profile"));
			}
			
			//parse phone
			if(obj.has("tel")) {
			    parseUserPhone(obj.getJSONArray("tel"));
			}
			//parse email
			if(obj.has("email")) {
			    parseUserEmail(obj.getJSONArray("email"));
			}
			
			//parse organization
			if(obj.has("organization")) {
			    parseOrganization(obj.getJSONArray("organization"));
			}
			//parse data
			if(obj.has("date")) {
			    parseData(obj.getJSONArray("date"));
			}
			
			bidi = obj.optBoolean("bidi");
			status = obj.optString("status");
			
			//profile_privacy
			profile_privacy = obj.optBoolean("profile_privacy");
			
			//pedding_requests
			if(obj.has("pending_req_types")) {
				JSONArray peddingObject = obj.getJSONArray("pending_req_types");
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
			status_time = obj.optLong("status_updated_time");
			friends_count = obj.optLong("friends_count");
			followers_count = obj.optLong("followers_count");
			favorites_count = obj.optLong("favorites_count");
			app_count = obj.optLong("apps_count");

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
		} catch (JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	public NewPlatformUserJSONImpl() {
	}
	
	private static WorkExperience createWorkExprienceItemResponse(JSONObject obj,long uid) throws TwitterException {
		WorkExperience weinfo = new WorkExperience();
		weinfo.uid = uid;
		weinfo.from = obj.optString("from");
		weinfo.to = obj.optString("to");
		weinfo.company = obj.optString("company");
		weinfo.office_address = obj.optString("office_location");
		weinfo.job_title = obj.optString("title");
		weinfo.department = obj.optString("department");
		weinfo.job_description = obj.optString("job_description");
        return weinfo;
	}
	
	private static Education createEducationItemResponse(JSONObject obj,long uid) throws TwitterException {
		Education edu = new Education();
		edu.uid = uid;
		edu.from = obj.optString("from");
		edu.to = obj.optString("to");
		edu.school = obj.optString("school");
		edu.school_location = obj.optString("school_location");
		edu.type = obj.optString("type");
		edu.school_class = obj.optString("class");
		edu.degree = obj.optString("degree");
		edu.major = obj.optString("major");
		
        return edu;
	}
	
	private static Address.AddressInfo createAddressItemResponse(JSONObject obj) throws TwitterException {
		Address.AddressInfo addressinfo = new Address.AddressInfo();
		addressinfo.type  = obj.optString("type");
		addressinfo.country = obj.optString("country");
		addressinfo.state = obj.optString("state");
		addressinfo.city = obj.optString("city");
		addressinfo.street = obj.optString("street");
		addressinfo.postal_code = obj.optString("postal_code");
		addressinfo.po_box = obj.optString("po_box");
		addressinfo.extended_address = obj.optString("extended_address");
        
        return addressinfo;
	}
	
	private static Circle.CircleInfo createCircleItemResponse(JSONObject obj) throws TwitterException {
		Circle.CircleInfo circleinfo = new Circle.CircleInfo();
		circleinfo.circle_id  = obj.optString("circle_id");
		circleinfo.circle_name = obj.optString("circle_name");
        
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
			list.add(new NewPlatformUserJSONImpl(obj));
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
		
		JSONObject photo = obj.optJSONObject("photo");
		if(photo != null) {
		    qiupuUser.profile_image_url = obj.optString("image_url");
		}
		
		qiupuUser.suggest_type = obj.optInt("suggested_reason");
		
		if(qiupuUser.suggest_type == USER_SUGGEST_TYPE_RECOMMENDER) {
		    try {
		        qiupuUser.recommendUser = new RecommendUser();
		        JSONArray recommend = obj.getJSONArray("recommend_by");
		        for (int i = 0; i < recommend.length(); i++) {
		            JSONObject js;
		            try {
		                js = recommend.getJSONObject(i);
		            } catch (JSONException e) {
		                throw new TwitterException(e);
		            }
		            qiupuUser.recommendUser.friends.add(createQiupuSimpleUser(js));
		        }
		    } catch (Exception e) {}
		}
		
		return qiupuUser;
	}

	private static QiupuSimpleUser createQiupuSimpleUser(JSONObject obj) {
	    QiupuSimpleUser simpleUser = new QiupuSimpleUser();
	    simpleUser.uid = obj.optLong("user_id");
	    simpleUser.nick_name = obj.optString("display_name");
	    JSONObject photo = obj.optJSONObject("photo");
        if(photo != null) {
            simpleUser.profile_limage_url = obj.optString("large_url");
            simpleUser.profile_image_url = obj.optString("image_url");
            simpleUser.profile_simage_url = obj.optString("small_url");
        }
	    return simpleUser;
	}
	private static QiupuUser createRecommendSimpleUserResponse(JSONObject obj)
			throws TwitterException {
		QiupuUser qiupuUser = new QiupuUser();
		try {
			qiupuUser.uid = obj.optLong("user_id");
			qiupuUser.nick_name = obj.optString("display_name");
			qiupuUser.profile_image_url = obj.getString("image_url");
			
			try
			{
				qiupuUser.app_count = obj.getInt("app_count");
			} catch (Exception e)
			{}
		} catch (JSONException jsone) {
			throw new TwitterException(jsone.getMessage());
		}

		return qiupuUser;
	}

	public static QiupuUser creatFriend(HttpResponse response)
			throws TwitterException {
		JSONObject obj = null;
		try {
			obj = response.asJSONObject();
		} catch (TwitterException e) {
			throw new TwitterException(e);
		}

		return new NewPlatformUserJSONImpl(obj);
	}
	
	public static QiupuUser creatUsersInfo(HttpResponse response) throws TwitterException {
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
			user = new NewPlatformUserJSONImpl(obj);
		}
		return user;
	}
	
	public static ArrayList<QiupuUser> createInstallUserList(HttpResponse response) throws TwitterException {
		ArrayList<QiupuUser> list = new ArrayList<QiupuUser>();
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
			list.add(createRecommendSimpleUserResponse(obj));
		}
		
		return list;
	}
	

//	private static ArrayList<ShareSourceItem> createShareResourceItemResponse(JSONObject obj) throws TwitterException {
//		ArrayList<ShareSourceItem> allres = new ArrayList<ShareSourceItem>();
//		ShareSourceItem shared; 
//		shared = new ShareSourceItem("");
//		shared.mType = BpcApiUtils.TEXT_POST;
//		shared.mCount = obj.optInt("shared_text");
//		allres.add(shared);
//		
//		shared = new ShareSourceItem("");
//		shared.mType = BpcApiUtils.IMAGE_POST;
//		shared.mCount = obj.optInt("shared_photo");
//		allres.add(shared);
//		
//		shared = new ShareSourceItem("");
//		shared.mType = BpcApiUtils.LINK_POST;
//		shared.mCount = obj.optInt("shared_link");
//		allres.add(shared);
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

		QiupuUser user = new QiupuUser();
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

        } catch (JSONException jsone) {}

        return qiupuUser;
    }
    
    private void parseUserPhoto(JSONObject photo) {
        profile_image_url = photo.optString("middle_url");
        profile_simage_url = photo.optString("small_url");
        profile_limage_url = photo.optString("large_url");
    }
    
    private void parseUserProfile(JSONObject profile) {
        try {
            if(profile.has("gender")) {
                gender = profile.getString("gender");
            }
            //TODO The data structure have not these column
            if(profile.has("timezone")) {
            }
            if(profile.has("interests")) {
            }
            if(profile.has("interests")) {
            }
            if(profile.has("languages")) {
            }
            if(profile.has("marriage")) {
            }
            if(profile.has("religion")) {
            }
            if(profile.has("description")) {
            }
        } catch (JSONException e) {}
    }
    
    private void parseUserPhone(JSONArray phones) {
        if(phones.length() <= 0) {
            Log.d(TAG, "The user has no phone ");
        }
        else {
            JSONObject phone ;
            try {
                for (int i = 0; i < phones.length(); i++) {
                    phone = phones.getJSONObject(i);
                    phoneList.add(createPhoneEmailInfo(phone));
                }
            } catch (JSONException e) {}
        }
    }
    
  //TODO Compatible with the older api
    private void parseUserEmail(JSONArray emails) {
        if(emails.length() <= 0) {
            Log.d(TAG, "The user has no email ");
        }
        else
        {
            JSONObject emailobj ;
            try {
                for (int i = 0; i < emails.length(); i++) {
                    emailobj = emails.getJSONObject(i);
                    phoneList.add(createPhoneEmailInfo(emailobj));
                }
            } catch (JSONException e) {}
        }
    }
    
    private void parseOrganization(JSONArray organization) {
        if(organization.length() <= 0) {
            Log.d(TAG, "The user has no organization ");
        }
        else
        {
            JSONObject organizationObj ;
            try {// get first one to compatible user data structure
                organizationObj = organization.getJSONObject(0);
                if(organizationObj.has("type")) {
                }
                if(organizationObj.has("company")) {
                    company = organizationObj.getString("company");
                }
                if(organizationObj.has("title")) {
                    jobtitle = organizationObj.getString("title");
                }
                if(organizationObj.has("department")) {
                    department = organizationObj.getString("department");
                }
                if(organizationObj.has("office_location")) {
                    office_address = organizationObj.getString("office_location");
                }
                if(organizationObj.has("job_description")) {
                }
                if(organizationObj.has("symbol")) {
                }
                if(organizationObj.has("phonetic_name")) {
                }
                if(organizationObj.has("label")) {
                }
            } catch (JSONException e) {}
        }
    }
    
    private void parseData(JSONArray data) {
        if(data.length() <= 0) {
            Log.d(TAG, "The user has no organization ");
        }
        else
        {
            JSONObject dataObj ;
            try {
                for(int i=0; i<data.length(); i++) {
                    dataObj = data.getJSONObject(i);
                    String type = dataObj.getString("type");
                    if(type.equals("birthday")) {// parse data of birthday
                        date_of_birth = dataObj.getString("info");
                    }
                }
            } catch (JSONException e) {}
        }
    }
    
    private PhoneEmailInfo createPhoneEmailInfo(JSONObject infoObj) {
        PhoneEmailInfo phoneEmail = new PhoneEmailInfo();
        phoneEmail.uid = uid;
        phoneEmail.info = infoObj.optString("info");
        phoneEmail.type = infoObj.optString("type");
        phoneEmail.isbind = infoObj.optBoolean("binding");
        return phoneEmail;
    }
}
