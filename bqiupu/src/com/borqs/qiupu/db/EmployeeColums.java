package com.borqs.qiupu.db;

public class EmployeeColums {
	public static final int STATUS_DEFAULT = 0;
   	public static final int STATUS_UPDATED = 1;
	
	public static final String ID                        =  "_id";
    public static final String OWNER_ID 					 = "owner_id";
	public static final String EMPLOYEE_ID				 = "employee_id";
	public static final String USER_ID 					 = "user_id";
	public static final String NAME 		 		     = "name";
	public static final String NAME_PINYIN 		 		     = "namePinYin";
	public static final String IMAGE_URL_S 				 = "image_url_s";
	public static final String IMAGE_URL_M 				 = "image_url_m";
	public static final String IMAGE_URL_L 				 = "image_url_l";
	public static final String EMAIL 				     = "email";
	public static final String TEL 						 = "tel";
	public static final String MOBILE_TEL 				 = "mobile_tel";
	public static final String DEPARTMENT				 = "department";
	public static final String JOB_TITLE				 = "job_title";
	public final static String DB_STATUS      			 = "db_status"; 
	public final static String IS_FAVORITE      		 = "is_favorite";
	
	//TODO
	public final static String ROLE_IN_GROUP      		 = "role_in_group";
	public final static String REFERRED_COUNT            = "referred_count";
	
	public static final String[] PROJECTION = {
		ID,
            OWNER_ID,
		EMPLOYEE_ID,
		USER_ID,
		NAME,
		NAME_PINYIN,
		IMAGE_URL_S,
		IMAGE_URL_M,
		IMAGE_URL_L,
		EMAIL,
		TEL,
		MOBILE_TEL,
		DEPARTMENT,
		JOB_TITLE,
		IS_FAVORITE,
		DB_STATUS,
		ROLE_IN_GROUP
    };
}
