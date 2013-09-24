/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.util.android;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import com.borqs.util.android.Base64;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.TextView;


public class Utility {
	//constants function for Email ISP
	public static String[] getEmailProviders(){
		return new String[]{
			"qq.com", "126.com", "163.com", "gmail.com",
			"sina.com", "sohu.com", "yahoo.com", "yahoo.cn",
			"borqs.com", 
		};
	}
	
	//Base 64 decider	 
    public static String base64Decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        return new String(decoded);
    }

    //base 64 encoder
    public static String base64Encode(String s) {
        if (s == null) {
            return s;
        }
        return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
    }

    //base 64 encoder
    public static String MD5Encode(String s) {
        if (s == null) {
            return s;
        }
        return MD5.toMd5(s.getBytes()).toUpperCase();
    }
    
    //check a textView has valid data or not
    public static boolean requiredFieldValid(TextView view) {
        return view.getText() != null && view.getText().length() > 0;
    }

    //check a Editable object has valid data or not
    public static boolean requiredFieldValid(Editable s) {
        return s != null && s.length() > 0;
    }
	
    /** 
     * Checks whether a string email address is valid.
     * E.g. name@domain.com is valid.
     */
    public static boolean isValidAddress(String address) {
        // Note: Some email provider may violate the standard, so here we only check that
        // address consists of two part that are separated by '@', and domain part contains
        // at least one '.'.
        int len = address.length();
        int firstAt = address.indexOf('@');
        int lastAt = address.lastIndexOf('@');
        int firstDot = address.indexOf('.', lastAt + 1);
        int lastDot = address.lastIndexOf('.');
        return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
            && firstDot <= lastDot && lastDot < len - 1;
    }
    
    //Check if the string can parse to a phone number 
    public static boolean isValidNumber(String phoneNumber){
    	if(TextUtils.isEmpty(phoneNumber)){
    		return false;
    	}
    	
    	return TextUtils.isDigitsOnly(phoneNumber);
    }
    
    /**
     * get the device number if possible
     */
    public static String getPhoneNumber(Context context){
		TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyMgr.getLine1Number();
    }
    
    /**
     * get the device IMSI code
     */
    public static String getImsi(Context context){
		TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telephonyMgr.getSubscriberId();
		return imsi==null? "" : imsi;
    }
    
    /**
     * get the device IMEI code
     */
    public static String getImei(Context context){
		TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyMgr.getDeviceId();
		return imei==null? "" : imei;
    }
    
    private static String treeSetToString(TreeSet<String> set) {
        Iterator<String> it = set.iterator();
        String str = "";
        while (it.hasNext()) {
            str += it.next();
        }
        return str;
    }

    public static String md5Sign(String appSecret, Collection<String> paramNames) {
        TreeSet<String> set = new TreeSet<String>(paramNames);
        String sign = appSecret + treeSetToString(set) + appSecret;
        return MD5.md5Base64(sign.getBytes());
    } 
    
    public static String generateGUID(Context context){
        return getImei(context)+String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * get the Sim Card Serial Number
     */
    public static String getSimSerial(Context context,boolean encode)
    {
    	TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String sn = telephonyMgr.getSimSerialNumber();
		if(encode)
		{
			sn = MD5.md5Base64(sn.getBytes());
		}
		return sn==null? "" : sn;
    }
}
