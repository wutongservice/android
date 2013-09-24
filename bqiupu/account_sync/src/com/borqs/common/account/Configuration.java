/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.borqs.app.Env;
import com.borqs.contacts_plus.R;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * @author b251
 * Configuration class for the email runtime
 */

public final class Configuration {	
    
    private static final String TAG = "Configuration";
    
	static final String XML_NAME = "name";
	static final String XML_VALUE = "value";
	
	static final String FEATURE_ACCOUNT_SERVER_HOST = "account_server_host";
	static final String FEATURE_SYNC_SERVER_HOST = "sync_server_host";
	static final String FEATURE_SMS_GATEWAY_NUMBER = "sms_gateway_number";
	static final String FEATURE_SHARE_NAME_PREFIX = "share_key_prefix";
	
//	public static final String ACCOUNT_SHOW = "user/show";
//    public static final String ACCOUNT_UPDATE = "account/update";
    
    private static final String SYNC_SYNCML_HOST = "sync_syncml_host";
    private static final String SYNC_WEBAGENT_HOST = "sync_webagent_host";
    private static final String API_HOST = "api_host";
    
    private static final String SHOW_HTTP_LOG="SHOW_HTTP_LOG";
    private static final String QiupuProvider_AUTHORITY = "com.borqs.qiupu";
    private static final Uri SETTINGS_CONTENT_URI = Uri.parse("content://"+QiupuProvider_AUTHORITY+"/settings");

    private static class SettingsCol{
        public static final String ID      = "_id";
        public static final String NAME    = "name";
        public static final String VALUE   = "value";
    }
    private static String[]settingsProject =  new String[]{
        SettingsCol.ID,
        SettingsCol.NAME,
        SettingsCol.VALUE
    };
    
	/**
	 * helper function:
	 * get the Borqs cloud server URL
	 * @return Borqs server URL,endWith("/")
	 */
	public static String getAccountServerHost(Context context){
		
	    String apiHost = getApiHost(context);
	    if(apiHost != null && !apiHost.endsWith("/")){
	        return apiHost + "/";
	    }else if(TextUtils.isEmpty(apiHost)){
	        XmlResourceParser xml = context.getResources().getXml(getDefaultConfigResId(context));
	        return getAttribute(context, xml, FEATURE_ACCOUNT_SERVER_HOST);
	    }else{
	        return apiHost ;
	    }
	}
	
	/**
	 * get Sync app server host
	 * @param context
	 * @return
	 */
	public static String getWebAgentServerHost(Context context){
       
	    String webAgentUrl = getWebagentHost(context);
	    if(webAgentUrl != null && !webAgentUrl.endsWith("/")){
            return webAgentUrl + "/";
        }else if(TextUtils.isEmpty(webAgentUrl)){
            XmlResourceParser xml = context.getResources().getXml(getDefaultConfigResId(context));
            return getAttribute(context, xml, FEATURE_SYNC_SERVER_HOST);
        }else {
            return webAgentUrl ;
        }
    }
	
	/**
	 * get the sms gateway number
	 * @param context
	 * @return
	 */
	public static String getSmsGatewayNumber(Context context){
	    XmlResourceParser xml = context.getResources().getXml(getDefaultConfigResId(context));
        return getAttribute(context, xml, FEATURE_SMS_GATEWAY_NUMBER);
	}
	
	/**
     * get the share name of login/register
     * @param context
     * @return
     */
    public static String getSharedPreferencePrefix(Context context){
        XmlResourceParser xml = context.getResources().getXml(getDefaultConfigResId(context));
        return getAttribute(context, xml, FEATURE_SHARE_NAME_PREFIX);
    }
	
	/**
	 * core function to retrieve the feature configuration 
	 * @param context android context
	 * @param config resource 
	 * @param name the feature name
	 * @return value of the feature configuration
	 */
	static String getAttribute(Context context, XmlResourceParser config, String name){
		int xmlEventType;
		try {
			while ((xmlEventType = config.next()) != XmlResourceParser.END_DOCUMENT) {
				if (xmlEventType == XmlResourceParser.START_TAG
						&& "feature".equals(config.getName())
						&& name.equalsIgnoreCase(config.getAttributeValue(null, XML_NAME))) {
					return getXmlAttribute(context, config, XML_VALUE);
				}
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
    private static String getXmlAttribute(Context context, XmlResourceParser xml, String name) {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        }
        else {
            return context.getString(resId);
        }
    }
    
    private static int getDefaultConfigResId(Context context){
            return R.xml.contact_configuration;
    }

    //contacts sync MUST use same server with account login service
    public static String getSyncMLHost(Context context){
        return Env.getHost(context, Env.HOST_SYNC_SYNCML);
    }

    public static String getWebagentHost(Context context){
        return Env.getHost(context, Env.HOST_SYNC_WEB_API);
    }

    public static String getApiHost(Context context){
        return Env.getHost(context, Env.HOST_API_BASE);
    }
    
    
    public static boolean isDebugMode(Context con)
    {
        try{
            String value = getSettingValue(con, SHOW_HTTP_LOG);
            if(value == null || value.equals("")) {
                return false;
            }
            return Integer.valueOf(value) == 1?true:false;
        }catch (Exception e){
            return true;
        }
    }

    private static String getSettingValue(Context con, String name) {
        String va = null;
        String where = SettingsCol.NAME + "=?";
        Cursor cursor = con.getContentResolver().query(SETTINGS_CONTENT_URI, settingsProject,
                where, new String[]{name}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    va = cursor.getString(cursor.getColumnIndex(SettingsCol.VALUE));
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        }
        return va;
    }
}