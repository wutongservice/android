package twitter4j;

import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import com.borqs.account.service.ContactSimpleInfo;

public class ContactSimpleInfoJSONImpl extends ContactSimpleInfo {

    public ContactSimpleInfoJSONImpl(JSONObject obj) {

        try {
            this.display_name_primary = obj.getString("username");
            this.mContactId = obj.getLong("contact_id");
            this.mBorqsId = obj.getLong("user_id");
            this.type = obj.getInt("type");
//            this.isfriend = obj.optBoolean("isfriend");
            this.isfriend = obj.getBoolean("isfriend");
            this.image_url = obj.getString("image_url");
            this.system_display_name = obj.getString("display_name");
            if (this.type == 0) {
                this.phone_number = obj.getString("content");
            } else {
                this.email = obj.getString("content");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }

      public static String createJSONObjectString(ContactSimpleInfo contact) throws JSONException{
    	  
    	  JSONObject obj = new JSONObject();
		  obj.put("display_name_primary",contact.display_name_primary);
		  obj.put("phone_number",contact.phone_number);
		  obj.put("email", contact.email);
		  
    	  return obj.toString();
      }

}
