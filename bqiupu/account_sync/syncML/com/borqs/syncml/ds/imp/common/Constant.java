/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.common;

public interface Constant {
//	public final static String PRELOAD_PROFILE_FILE_NAME = "/opl/etc/syncml_ds_preload_profiles.xml";
	public final static String CONFIG_FILE = "/opl/etc/syncml_ds_config.xml";
//	public final static String DEV_INFO_FILE = "/opl/etc/syncml_ds_dev_info.xml";
	// public final static String DEV_INFO_DATA_STORE_CARD =
	// "/opl/etc/syncml_ds_dev_info_data_store_card.xml";
	// public final static String DEV_INFO_DATA_STORE_CALENDAR =
	// "/opl/etc/syncml_ds_dev_info_data_store_calendar.xml";
	// public final static String DEV_INFO_CAP_X_CARD =
	// "/opl/etc/syncml_ds_dev_info_cap_x_vcard.xml";
	// public final static String DEV_INFO_CAP_X_CALENDAR =
	// "/opl/etc/syncml_ds_dev_info_cap_x_vcalendar.xml";
	public static final String PREFIX_CONTACTS = "0:";
	public static final String PREFIX_EVENT = "1:";
	public static final String PREFIX_TASKS = "2:";
	public static final String CONTACT_LOCAL_ACCOUNT_NAME = "vnd.ophoneos.contact.phone"; 
	public static final String CONTACT_LOCAL_ACCOUNT_TYPE = "vnd.ophoneos.contact.phone";
	
	
	public static final String BORQS_ACCOUNT_TYPE = "com.borqs";
	
	
//	//changelog constant
	public static final int CHANGE_LOG_ADDED = 0;
	public static final int CHANGE_LOG_UPDATED = 1;
	public static final int CHANGE_LOG_DELETED = 2;

}
