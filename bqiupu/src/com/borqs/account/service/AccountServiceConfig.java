package com.borqs.account.service;

import twitter4j.conf.ConfigurationBase;

public class AccountServiceConfig {
    public static final boolean LOGD = true;
    public static final boolean DBLOGD = false;
    public static final boolean LowPerformance = false;

    /** used for imagerun set default image */
    public final static int DEFAULT_IMAGE_INDEX_APK = 0;
    
    public final static int DEFAULT_IMAGE_INDEX_USER = 1;
    
//	public static final String URL_SHARE_PREFIX = ConfigurationBase.getAPIURL()+ "search?q=";
	
	public static final String[] mailSuf = new String[]{"gmail.com","borqs.com","sina.com","sohu.com",
			"139mail.com","yahoo.com.cn","yahoo.com","msn.com","mac.com","qq.com","163.com","126.com","hotmail.com"};
}
