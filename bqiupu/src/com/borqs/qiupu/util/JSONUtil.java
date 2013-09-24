package com.borqs.qiupu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import twitter4j.Education;
import twitter4j.QiupuUser;
import twitter4j.WorkExperience;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.qiupu.QiupuConfig;

public class JSONUtil {

	private static final String TAG = "JSONUtil";

	public static String createLightJSONArray(List<ContactSimpleInfo> userList) {
        if (null == userList || userList.size() <= 0) {
            Log.i(TAG, "createLightJSONArray, ignore empty user list.");
            return "";
        }

		JSONArray array = new JSONArray();
		int size = 0;
		if (userList != null) {
			size = userList.size();
		}

		for (int i = 0; i < size; i++) {
			JSONObject obj = new JSONObject();
			ContactSimpleInfo item = userList.get(i);
			//Log.d(TAG, "----> createLightJSONArray  item:"+item);
			try {
                obj.put("contact_id", item.mContactId);
				obj.put("username", item.display_name_primary);
				obj.put("type", item.type);

				if (item.type == ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL) {
					obj.put("content", item.email);
				} else {
					obj.put("content", item.phone_number);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);
			//Log.d(TAG, "====> createLightJSONArray obj:"+obj);
		}
		
		return array.toString();
	}
	
	public static String createAddressJSONArray(ArrayList<QiupuUser.Address.AddressInfo> addressList) {
		JSONArray array = new JSONArray();
		int size = 0;
		if (addressList != null) {
			size = addressList.size();
		}

		for (int i = 0; i < size; i++) {
			JSONObject obj = new JSONObject();
			QiupuUser.Address.AddressInfo item = addressList.get(i);
			try {
				obj.put("type", item.type);
				obj.put("country", item.country);
				obj.put("state", item.state);
				obj.put("city", item.city);
				obj.put("street", item.street);
				obj.put("postal_code", item.postal_code);
				obj.put("po_box", item.po_box);
				obj.put("extended_address", item.extended_address);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);
			
			//Log.d(TAG, "====> createLightJSONArray obj:"+obj);
		}
		
		if(QiupuConfig.DBLOGD)Log.d(TAG, "createAddressJSONArray array : " + array + " to string : " + array.toString());
		return array.toString();
	}
	
	public static String createWorkExperienceJSONArray(ArrayList<WorkExperience> workList) {
		JSONArray array = new JSONArray();
		int size = 0;
		if (workList != null) {
			size = workList.size();
		}

		for (int i = 0; i < size; i++) {
			JSONObject obj = new JSONObject();
			WorkExperience item = workList.get(i);
			try {
				obj.put("from",item.from);
				obj.put("to",item.to);
				obj.put("company", item.company);
				obj.put("address", item.office_address);
				obj.put("title", item.job_title);
				obj.put("profession", item.department);
//				obj.put("job_description", item.job_description);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);
			
			//Log.d(TAG, "====> createLightJSONArray obj:"+obj);
		}
		
		if(QiupuConfig.DBLOGD)Log.d(TAG, "createWorkExprienceJSONArray array : " + array + " to string : " + array.toString());
		return array.toString();
	}
	
	public static String createEducationJSONArray(ArrayList<Education> eduList) {
		JSONArray array = new JSONArray();
		int size = 0;
		if (eduList != null) {
			size = eduList.size();
		}

		for (int i = 0; i < size; i++) {
			JSONObject obj = new JSONObject();
			Education item = eduList.get(i);
			try {
				obj.put("from",item.from);
				obj.put("to",item.to);
				obj.put("type", item.type);
				obj.put("school", item.school);
//				obj.put("school_location", item.school_location);
				obj.put("class", item.school_class);
				obj.put("degree", item.degree);
				obj.put("major", item.major);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);
			
		}
		
		if(QiupuConfig.DBLOGD)Log.d(TAG, "createEducationJSONArray array : " + array + " to string : " + array.toString());
		return array.toString();
	}
	
	public static String createContactInfoJSONObject(HashMap<String, String> contactInfo) {
		JSONObject obj = new JSONObject();
		try {
			Iterator iter = contactInfo.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String val = (String) entry.getValue();
				obj.put(key, val);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj.toString();
	}
	
	
    public static String createPhoneAndEmailJSONArray(long borqsId, String name, ArrayList<String> phoneList, ArrayList<String> emailList) {
        JSONArray array = new JSONArray();

        JSONObject objInfo = new JSONObject();

        if (phoneList != null) {
            for (int i = 0, size = phoneList.size(); i < size; i++) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("phone", phoneList.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(obj);
            }
        }

        if (emailList != null) {
            for (int i = 0, size = emailList.size(); i < size; i++) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("email", emailList.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(obj);
            }
        }

        try {
            objInfo.put("mime_type", "vcard");
            objInfo.put("version", "1.0");
            objInfo.put("name", name);
            objInfo.put("borqs_id", borqsId);
            objInfo.put("phone_email", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "createPhoneAndEmailJSONArray array : " + array + " to string : " + array.toString());
        return objInfo.toString();
    }
}
