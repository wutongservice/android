/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.util;

import android.util.Log;

public class BLog {
	private static String TAG = "LoginService";
	private static boolean ACCOUNT_LOG_EABLED = true;
	
	public static void d(String msg){
	    if(ACCOUNT_LOG_EABLED){
	    	Log.d(TAG, msg);
	    }
	}
	
	public static void w(String msg){
	    if(ACCOUNT_LOG_EABLED){
	    	Log.w(TAG, msg);
	    }
	}
	
	public static void e(String msg){
	    if(ACCOUNT_LOG_EABLED){
	    	Log.e(TAG, msg);
	    }
	}
	
	public static void enable(){
	    ACCOUNT_LOG_EABLED = true;
	}
	
	public static void disable(){
	    ACCOUNT_LOG_EABLED = false;
	}
}
