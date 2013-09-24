/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import android.telephony.PhoneNumberUtils;
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
	
    public static boolean isValidUserName(String userName){
        //return isValidPhoneNumber(userName);        
        //return !TextUtils.isEmpty(userName);
        //return (isValidPhoneNumber(userName)||isValidEmailAddress(userName));
        
        // email or number
        boolean res = false;
        if (isValidEmailAddress(userName)){
            res = true;
        } else if (isValidPhoneNumber(userName)){
            res = true;
        } else if (isValidNumber(userName)) {
            res = true;
        }
        return res;
    }
    
    public static boolean isValidUserDispName(String userName){
        return !TextUtils.isEmpty(userName);
    }
    
    public static boolean isValidPassword(String password){
        boolean res = false;
        if (password != null){
            if ((password.length() >= 6) && (password.length() <= 16)){
                res = true;
            }
        }
        return res;
    }
    
    /** 
     * Checks whether a string email address is valid.
     * E.g. name@domain.com is valid.
     */
    public static boolean isValidEmailAddress(String address) {
        // Note: Some email provider may violate the standard, so here we only check that
        // address consists of two part that are separated by '@', and domain part contains
        // at least one '.'.        
        int len = address.length();
        int firstAt = address.indexOf('@');
        int lastAt = address.lastIndexOf('@');
        int firstDot = address.indexOf('.', lastAt + 1);
        int lastDot = address.lastIndexOf('.');
        boolean validEmail = firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
                && firstDot <= lastDot && lastDot < len - 1;
        if (validEmail){
            // check it contains space char or not
            if (address.contains(" ")){
                validEmail = false;
            }
        }
        return validEmail;
    }
    
    public static boolean isValidNumber(String phoneNumber){
        boolean res = true;
        
        if (TextUtils.isEmpty(phoneNumber)){
            res = false;
        } else if (!TextUtils.isDigitsOnly(phoneNumber)){
            res = false;
        }        
        
        return res;
    }
    
    //Check if the string can parse to a phone number 
    public static boolean isValidPhoneNumber(String phoneNumber){
        if(phoneNumber == null || phoneNumber.length()<5) return false;
        for(int i=0;i<phoneNumber.length(); i++){
            char c = phoneNumber.charAt(i);
            if(!PhoneNumberUtils.isDialable(c)) return false;
        }
    	return true;
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
    
    public static String generateGUID(String baseStr){
        return baseStr+String.valueOf(System.currentTimeMillis());
    }
}
