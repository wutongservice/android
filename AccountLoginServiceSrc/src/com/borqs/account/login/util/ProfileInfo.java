package com.borqs.account.login.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public final class ProfileInfo {
	private JSONObject mJSONData;

	public static ProfileInfo from(JSONObject jsonData) {
		if (null == jsonData) {
			return null;
		}

		ProfileInfo info = new ProfileInfo();
		info.mJSONData = jsonData;
		return info;

	}
	
	public boolean has(String key){
        return mJSONData.has(key);
    }
	
	public String getStringField(String key) {
		try {
			if (mJSONData.has(key)) {
				return mJSONData.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	public List<String> getStringListField(String key) {
		try {
			if (mJSONData.has(key)) {
				JSONArray array = mJSONData.getJSONArray(key);
				List<String> result = new ArrayList<String>();
				for(int i=0;i<array.length();i++)
				{
					result.add(array.getString(i));
				}
				
				return result;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public Long getLongField(String key) {
		try {
			if (mJSONData.has(key)) {
				return mJSONData.getLong(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public ProfileContactInfo getAccountContactInfo()
	{
		try {
			return ProfileContactInfo.from(mJSONData.getJSONObject("contact_info"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	/*
last_visited_time 	用户最后访问时间
basic_updated_time 	基础信息更新时间
status 	用户当前状态
status_updated_time 	用户状态更新时间





timezone 	时区
interests 	兴趣
languages 	语言能力,json数组
marriage 	婚否
religion 	宗教

profile_updated_time 	用户资料更新时间
department 	所在工作部门
job_title 	工作头衔
office_address 	办公地址
profession 	职业或专业
job_description 	工作描述
business_updated_time 	商业信息更新时间
contact_info 	用户联系方式，json对象
contact_info_updated_time 	联系方式更新时间
family 	用户家庭成员，json数组
coworker 	用户同事，json数组
address 	用户生活或工作地址，json数组
address_updated_time 	用户所在地址更新时间
work_history 	用户工作历史，json数组
work_history_updated_time 	用户工作历史更新时间
education_history 	用户教育历史，json数组
education_history_updated_time 	用户教育历史更新时间
miscellaneous 	其他杂项，json对象
in_circles 	那个用户你的哪些圈子中，json数组，每一项为{circle_id:xx, circle_name:"xx"}
in_his_circles 	你在那个用户的哪些圈子中，json数组，每一项为{circle_id:xx, circle_name:"xx"}
bidi 	那个人是否为双向好友(双向关注)
favorites_count 	收藏数
friends_count 	在该用户所有圈子的好友数
followers_count 	粉丝数*/
	
	//user_id 	用户ID
	public String get_user_id()
	{
		return getStringField("user_id");
	}
	//login_email1 	登陆邮箱1
	public String get_login_email1()
	{
		return getStringField("login_email1");
	}
	//login_email2 	登陆邮箱2
	public String get_login_email2()
	{
		return getStringField("login_email2");
	}
	//login_email3 	登陆邮箱3
	public String get_login_email3()
	{
		return getStringField("login_email3");
	}
	//login_phone1 	登陆电话号码1
	public String get_login_phone1()
	{
		return getStringField("login_phone1");
	}
	//login_phone2 	登陆电话号码2
	public String get_login_phone2()
	{
		return getStringField("login_phone2");
	}
	//login_phone3 	登陆电话号码3
	public String get_login_phone3()
	{
		return getStringField("login_phone3");
	}
	//domain_name 	用户域名
	public String get_domain_name()
	{
		return getStringField("domain_name");
	}
	//display_name 	用户显示名称
	public String get_display_name()
	{
		return getStringField("display_name");
	}
	//image_url 	用户头像图片URL
	public String get_image_url()
	{
		return getStringField("image_url");
	}
	//large_image_url 	用户大头像图片URL
	public String get_large_image_url()
	{
		return getStringField("large_image_url");
	}
	//small_image_url 	用户小头像图片URL
	public String get_small_image_url()
	{
		return getStringField("small_image_url");
	}
	//first_name 	First name
	public String get_first_name()
	{
		return getStringField("first_name");
	}
	//middle_name 	Middle name
	public String get_middle_name()
	{
		return getStringField("middle_name");
	}
	//last_name 	Last name
	public String get_last_name()
	{
		return getStringField("last_name");
	}
	//gender 	性别:m,f
	public String get_gender()
	{
		return getStringField("gender");
	}
	//birthday 	生日
	public String get_birthday()
	{
		return getStringField("birthday");
	}
	//about_me 	自我描述
	public String get_about_me()
	{
		return getStringField("about_me");
	}
	//company 	用户所在公司
	public String get_company()
	{
		return getStringField("company");
	}
	//remark 	备注信息
	public String get_remark()
	{
		return getStringField("remark");
	}
	//address 	用户生活或工作地址，json数组
	public List<List<String>> get_address()
	{
		if (mJSONData.has("address")) {
			JSONObject addressObj;
			try {
				JSONArray addressList = mJSONData.getJSONArray("address");
				List<List<String>> result  = new ArrayList<List<String>>();
				for(int i=0;i<addressList.length();i++)
				{
					addressObj = addressList.getJSONObject(i);
					List<String> one = new ArrayList<String>();
					one.add(addressObj.getString("po_box"));
					one.add(addressObj.getString("extended_address"));
					one.add(addressObj.getString("street"));
					one.add(addressObj.getString("city"));
					one.add(addressObj.getString("state"));
					one.add(addressObj.getString("postal_code"));
					one.add(addressObj.getString("country"));
					result.add(one);
				}
				return result;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
