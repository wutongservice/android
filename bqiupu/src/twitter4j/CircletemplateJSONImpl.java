package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class CircletemplateJSONImpl extends Circletemplate {

	private static final long serialVersionUID = 5555929547568380673L;
	private final static String TAG = "CircletemplateJSONImpl";

	public CircletemplateJSONImpl(JSONObject obj) throws TwitterException {
		if(obj == null) return;
		majorVersion = obj.optString("majorVersion");
		minorVersion = obj.optString("minorVersion");
		description = obj.optString("description");
		parseTemplateJsonArray(obj, this);
		
		JSONArray templateformalJsonArray = obj.optJSONArray("template.formal");
		
		if(templateformalJsonArray != null && templateformalJsonArray.length() > 0) {
			templateFormal = new ArrayList<TemplateInfo>();
			for(int i=0;i<templateformalJsonArray.length();i++) {
				JSONObject memberobj;
				try {
					memberobj = templateformalJsonArray.getJSONObject(i);
					if(memberobj != null) {
						templateFormal.add(createTempLateJsonObj(memberobj));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		JSONArray templateJsonArray = obj.optJSONArray("template");
		if(templateJsonArray != null && templateJsonArray.length() > 0) {
			template = new ArrayList<TemplateInfo>();
			for(int i=0;i<templateJsonArray.length();i++) {
				JSONObject memberobj;
				try {
					memberobj = templateJsonArray.getJSONObject(i);
					if(memberobj != null) {
						template.add(createTempLateJsonObj(memberobj));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static TemplateInfo createTempLateJsonObj(JSONObject obj) {
		TemplateInfo info = new TemplateInfo();
		info.name = obj.optString("name");
		info.formal = obj.optInt("formal");
		JSONObject titleobj = obj.optJSONObject("title");
		if(titleobj != null) {
			info.title = titleobj.optString("zh_CN"); 
			info.title_en = titleobj.optString("en");
		}
		JSONObject descriptionobj = obj.optJSONObject("description");
		if(descriptionobj != null) {
			info.description = descriptionobj.optString("zh_CN"); 
			info.description_en = descriptionobj.optString("en");
		}
		info.icon_url = obj.optString("icon_url");
		return info;
	}
	
	public static void parseTemplateJsonArray(JSONObject obj, Circletemplate info) {
		JSONArray templateJsonArray = obj.optJSONArray("template");
		if(templateJsonArray != null && templateJsonArray.length() > 0) {
			info.template = new ArrayList<TemplateInfo>();
			for(int i=0;i<templateJsonArray.length();i++) {
				JSONObject infoObj;
				try {
					infoObj = templateJsonArray.getJSONObject(i);
					if(infoObj != null) {
						info.template.add(createTempLateJsonObj(infoObj));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static ArrayList<TemplateInfo> parseTemplateJsonArrayWithSubType(JSONObject obj, String subType) {
		JSONArray templateJsonArray = obj.optJSONArray(subType);
		ArrayList<TemplateInfo> infoArray= new ArrayList<TemplateInfo>();
		if(templateJsonArray != null && templateJsonArray.length() > 0) {
//			info.template = new ArrayList<TemplateInfo>();
			for(int i=0;i<templateJsonArray.length();i++) {
				JSONObject infoObj;
				try {
					infoObj = templateJsonArray.getJSONObject(i);
					if(infoObj != null) {
						infoArray.add(createTempLateJsonObj(infoObj));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return infoArray;
	}
}
