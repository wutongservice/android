package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class CompanyJSONImpl extends Company {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5555929547568380673L;
	private final static String TAG = "CompanyJSONImpl";

	public CompanyJSONImpl(JSONObject obj) throws TwitterException {
		if(obj == null) return;
		id = obj.optLong("company_id");
		department_id = obj.optLong("department_id");
		created_time = obj.optLong("created_time");
		updated_time = obj.optLong("updated_time");
		role = obj.optInt("role");
		person_count = obj.optInt("employee_count");
		department_count = obj.optInt("sub_department_count");
		email_domain1 = obj.optString("email_domain1");
		email_domain2 = obj.optString("email_domain2");
		email_domain3 = obj.optString("email_domain3");
		email_domain4 = obj.optString("email_domain4");
		name = obj.optString("name");
		address = obj.optString("address");
		email = obj.optString("email");
		website = obj.optString("website");
		tel = obj.optString("tel");
		fax = obj.optString("fax");
		zip_code = obj.optString("zip_code");
		small_logo_url = obj.optString("small_logo_url");
		logo_url = obj.optString("logo_url");
		large_logo_url = obj.optString("large_logo_url");
		small_cover_url = obj.optString("small_cover_url");
		cover_url = obj.optString("cover_url");
		large_cover_url = obj.optString("large_cover_url");
		description = obj.optString("description");
		
		JSONArray memberJsonArray = obj.optJSONArray("some_members");
		if(memberJsonArray != null && memberJsonArray.length() > 0) {
			memberList = new ArrayList<UserImage>();
			for(int i=0;i<memberJsonArray.length();i++) {
				JSONObject memberobj;
				try {
					memberobj = memberJsonArray.getJSONObject(i);
					if(memberobj != null) {
						memberList.add(QiupuNewUserJSONImpl.createUserImageItemResponse(memberobj, department_id));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		JSONArray depJsonArray = obj.optJSONArray("sub_department");
	    if(depJsonArray != null && depJsonArray.length() > 0) {
	    	depList = new ArrayList<UserImage>();
	        for(int i=0;i<depJsonArray.length();i++) {
                JSONObject depobj;
				try {
					depobj = depJsonArray.getJSONObject(i);
					if(depobj != null) {
						depList.add(QiupuNewUserJSONImpl.createUserImageItemFromCircle(depobj, department_id));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
	    }
	}
	
	public static ArrayList<Company> createCompanyList(HttpResponse response)
			throws TwitterException {
		ArrayList<Company> list = new ArrayList<Company>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}
		
		Log.d(TAG, "createCompanyList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new CompanyJSONImpl(obj));
		}
		
		return list;
	}
	
	public static Company createCompanyInfo(HttpResponse response)
			throws TwitterException {
		Company c = new  Company();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createCompanyList size:" + array.length());
		if(array.length() >= 1 ) {
			
			JSONObject obj;
			try {
				obj = array.getJSONObject(0);
				c = new CompanyJSONImpl(obj);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
		}

		return c;
	}
}
