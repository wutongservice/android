package twitter4j;

import java.util.ArrayList;
import java.util.HashMap;

import com.borqs.qiupu.QiupuConfig;

import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class RequestJSONImpl extends Requests{
	
	private static final long serialVersionUID = -1644363744020341763L;

	public RequestJSONImpl(JSONObject obj) throws TwitterException {
		try {
			rid        = obj.getString("request_id");
			type       = obj.getInt("type");		
			message    = obj.getString("message");
			createTime = obj.getLong("created_time");
			data       = obj.getString("data");
			user = parserSource(obj.getJSONObject("source"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}		
	}
	
	QiupuUser parserSource(JSONObject userj) throws JSONException
	{
		QiupuUser user = new QiupuUser();
		try{
		user.uid  = userj.getLong("uid");
		user.nick_name = userj.getString("display_name");
		user.profile_image_url = userj.getString("image_url");
		user.profile_simage_url = userj.getString("small_image_url");
		user.profile_limage_url = userj.getString("large_image_url");
		// parse contact Info
		JSONObject contactInfo = userj.getJSONObject("contact_info");
		parseContactInfo(contactInfo, user);
//		if(contactInfo.has("email_address"))
//		{
//			user.contact_email1 = contactInfo.getString("email_address");
//		}
//		if(contactInfo.has("email_2_address"))
//		{
//			user.contact_email2 = contactInfo.getString("email_2_address");
//		}
//		if(contactInfo.has("email_3_address"))
//		{
//			user.contact_email3 = contactInfo.getString("email_3_address");
//		}
//		if(contactInfo.has("mobile_telephone_number"))
//		{
//			user.contact_phone1 = contactInfo.getString("mobile_telephone_number");
//		}
//		if(contactInfo.has("mobile_2_telephone_number"))
//		{
//			user.contact_phone2 = contactInfo.getString("mobile_2_telephone_number");
//		}
//		if(contactInfo.has("mobile_3_telephone_number"))
//		{
//			user.contact_phone3 = contactInfo.getString("mobile_3_telephone_number");
//		}
		}catch(Exception ne){ne.printStackTrace();}
		
		return user;
	}
	
	private PhoneEmailInfo createPhoneEmailList(QiupuUser user, String content, String type) {
	    PhoneEmailInfo phoneEmail = new PhoneEmailInfo();
	    phoneEmail.uid = user.uid;
	    phoneEmail.type = type;
	    phoneEmail.info = content;
	    return phoneEmail;
	}
	
	private void parseContactInfo(JSONObject contactInfo, QiupuUser user) {
	    try {
	        if(contactInfo.has("email_address")) {
	            String contact_email1 = contactInfo.getString("email_address");
	            user.emailList.add(createPhoneEmailList(user, contact_email1, QiupuConfig.TYPE_EMAIL1));
	        }
	        if(contactInfo.has("email_2_address")) {
	            String contact_email2 = contactInfo.getString("email_2_address");
	            user.emailList.add(createPhoneEmailList(user, contact_email2, QiupuConfig.TYPE_EMAIL2));
	        }
	        if(contactInfo.has("email_3_address")) {
	            String contact_email3 = contactInfo.getString("email_3_address");
	            user.emailList.add(createPhoneEmailList(user, contact_email3, QiupuConfig.TYPE_EMAIL3));
	        }
	        if(contactInfo.has("mobile_telephone_number")) {
	            String contact_phone1 = contactInfo.getString("mobile_telephone_number");
	            user.phoneList.add(createPhoneEmailList(user, contact_phone1, QiupuConfig.TYPE_PHONE1));
	        }
	        if(contactInfo.has("mobile_2_telephone_number")) {
	            String contact_phone2 = contactInfo.getString("mobile_2_telephone_number");
	            user.phoneList.add(createPhoneEmailList(user, contact_phone2, QiupuConfig.TYPE_PHONE2));
	        }
	        if(contactInfo.has("mobile_3_telephone_number")) {
	            String contact_phone3 = contactInfo.getString("mobile_3_telephone_number");
	            user.phoneList.add(createPhoneEmailList(user, contact_phone3, QiupuConfig.TYPE_PHONE3));
	        }
	    } catch (JSONException jsone) {}
	}
	 
	public static ArrayList<Requests> createReqeustList(HttpResponse response) throws TwitterException {
		ArrayList<Requests> rl = new ArrayList<Requests>();
		
		try {
			JSONArray array = null;
			array = response.asJSONArray();

			for (int i = 0; i < array.length(); i++) {
				JSONObject obj;
				try {
					obj = array.getJSONObject(i);
				} catch (JSONException e) {
					throw new TwitterException(e);
				}
				rl.add(new RequestJSONImpl(obj));
			}
			
			array = null;
		} catch (TwitterException e) {
			throw e;	
		} catch (Exception e) {
			throw new TwitterException(e.getMessage());
		}		
		return rl;
	}

    public static HashMap<String, Integer> createRequestMap(HttpResponse response) throws TwitterException {
        HashMap<String, Integer> requestMap = new HashMap<String, Integer>();
        try {
            JSONObject obj = response.asJSONObject();
            requestMap.put("num_friend", Integer.valueOf(obj.optString("num_friend")));
            requestMap.put("num_event", Integer.valueOf(obj.optString("num_event")));
            requestMap.put("num_circle", Integer.valueOf(obj.optString("num_circle")));
            requestMap.put("num_change_profile", Integer.valueOf(obj.optString("num_change_profile")));
        } catch (TwitterException e) {
            throw e;
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }

        return requestMap;
    }

}
