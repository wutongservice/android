package twitter4j;

import android.util.Log;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import java.util.List;

public class ApkinfoJSONImpl {
      private static final String TAG = "Qiupu.ApkinfoJSONImpl";

	public static String createJSONArrayString(List<Apkinfo> infoList) throws JSONException{
    	  JSONArray array = new JSONArray(); 
    	  for(int i=0;i<infoList.size();i++){
        	  JSONObject obj = new JSONObject();
    		  Apkinfo item = infoList.get(i);
    		  obj.put("apkName",item.apkName);
    		  obj.put("apkComponentName",item.apkComponentName);
    		  obj.put("apkVersion", item.apkVersion);
    		  
    		  if(item.apkVersionName == null)item.apkVersionName="";
    		  obj.put("apkVersionName",item.apkVersionName);
    		  obj.put("apkTargetSDKVersion", item.apkTargetSDKVersion);
    		  obj.put("apkFilesize", item.apkFilesize);
    		  //obj.put
    		  array.put(obj);
    	  }
    	  return array.toString();
      }
      
      public static String createJSONObjectString(Apkinfo item) throws JSONException{
    	  JSONObject obj = new JSONObject();
		  obj.put("apkName",item.apkName);
		  obj.put("apkComponentName",item.apkComponentName);
		  obj.put("apkVersion", item.apkVersion);
		  
		  if(item.apkVersionName == null)item.apkVersionName="";
		  obj.put("apkVersionName",item.apkVersionName);
		  obj.put("apkTargetSDKVersion", item.apkTargetSDKVersion);
		  obj.put("apkFilesize", item.apkFilesize);
		  
		  Log.d(TAG, "createJSONObjectString:"+obj.toString());
    	  return obj.toString();
      }
}
