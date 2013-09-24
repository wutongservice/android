package twitter4j;

import android.text.TextUtils;

import com.borqs.qiupu.db.QiupuORM;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.internal.org.json.JSONException;

import java.util.ArrayList;

public class EmployeeJsonImpl extends Employee {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4224730091181979084L;
	
	public EmployeeJsonImpl(JSONObject obj)  {
		if(obj == null) return;
		
		name = obj.optString("display_name");
		if(TextUtils.isEmpty(name)) {
			name = obj.optString("name");
		}
		namePinYin = QiupuORM.getPinyin(name);
		employee_id = obj.optString("employee_id");
		if(obj.has("user")) {
			JSONObject userOjb;
			try {
				userOjb = obj.getJSONObject("user");
				user_id = userOjb.optString("user_id");
				image_url_s = userOjb.optString("small_image_url");
				image_url_m = userOjb.optString("image_url");
				image_url_l = userOjb.optString("large_image_url");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			user_id = obj.optString("user_id");
			image_url_s = obj.optString("small_image_url");
			image_url_m = obj.optString("image_url");
			image_url_l = obj.optString("large_image_url");
		}
		
		email = obj.optString("email");
		tel = obj.optString("tel");
		mobile_tel = obj.optString("mobile_tel");
		department = obj.optString("department");
		job_title = obj.optString("job_title");
		role_in_group = obj.optInt("role_in_group");
		status = obj.optInt("status");
		
	}
	
	public static ArrayList<Employee> createEmployeeList(HttpResponse response) {
		ArrayList<Employee> list = new ArrayList<Employee>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				list.add(new EmployeeJsonImpl(obj));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	public static Employee createEmployeeInfo(HttpResponse response)  {
		Employee c = new  Employee();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
			if(array.length() >= 1 ) {
				
				JSONObject obj;
				obj = array.getJSONObject(0);
				c = new EmployeeJsonImpl(obj);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return c;
	}

}
