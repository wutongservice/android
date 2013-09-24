package twitter4j;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class IntegerJSONImpl extends Integers{

	public static Integers createIntegerResponse(HttpResponse response) throws TwitterException{
		JSONObject obj = response.asJSONObject();
		Integers integerresponse = new IntegerJSONImpl();
		try {			
			integerresponse.totalnumber = obj.getInt("result");
            return integerresponse;
            
        } catch (JSONException jsone) {
        	int status_code = 0;
        	String error_msg = "";
        	try {
				status_code = obj.getInt("error_code");
				error_msg = obj.getString("error_msg");
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
            throw new TwitterException(status_code,error_msg);
        }
	}

}
