/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;
import com.borqs.account.login.R;
import com.borqs.account.login.service.ConstData;
import com.borqs.app.Env;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author b251
 * Configuration class for the email runtime
 */

public final class Configuration {
    //private static final String TAG = "Configuration";
    
    private static final String XML_NAME = "name";
    private static final String XML_VALUE = "value";
    
	/**
	 * helper function:
	 * get the Borqs cloud server URL
	 * @return Borqs server URL,endWith("/")
	 */
    public static String getAccountServerHost(Context context) {
        String host = Env.getHost(context, Env.HOST_API_BASE);
        //String host = "http://192.168.5.35:8881/";
        if(!TextUtils.isEmpty(host)){
            return host;
        }

        XmlResourceParser xml = context.getResources().getXml(
                getDefaultConfigResId(context));
        return getAttribute(context, xml, "account_server_host");
    }

    /**
     * get Sync app server host
     * 
     * @param context
     * @return
     */
    public static String getWebAgentServerHost(Context context) {
        
        return getAccountServerHost(context)+"sync/webagent/";
    }
	
    public static String getAccountServerVersion(Context context)
    {
        XmlResourceParser xml = context.getResources().getXml(
                getDefaultConfigResId(context));
        return getAttribute(context, xml, "account_server_version");
    }
        
	/**
	 * core function to retrieve the feature configuration 
	 * @param context android context
	 * @param config resource 
	 * @param name the feature name
	 * @return value of the feature configuration
	 */
    private static String getAttribute(Context context, XmlResourceParser config, String name){
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
            return R.xml.account_configuration;
    }
}