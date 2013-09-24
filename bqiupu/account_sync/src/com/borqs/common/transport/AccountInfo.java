/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.common.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.account.AccountException;
import com.borqs.common.transport.AccountClient;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.jcontact.JEMail;
import com.borqs.pim.jcontact.JIM;
import com.borqs.pim.jcontact.JPhone;
import com.borqs.pim.jcontact.JWebpage;
import com.borqs.pim.jcontact.JXTag;
import com.borqs.util.android.MD5;

//import com.borqs.qiupu.util.StringUtil;

public class AccountInfo {

    public static final Map<String, String> EMAIL_MATCHER = new HashMap<String, String>();
    public static final Map<String, String> PHONE_MATCHER = new HashMap<String, String>();
    public static final Map<String, String> WEB_MATCHER = new HashMap<String, String>();
    
    
    public static final Map<String, String> OTHER_MATCHER = new HashMap<String, String>();

    //contact_info
    public static final String ASSISTANT_NUM = "assistant_number";
    public static final String CALLBACK_NUM = "callback_number";
    public static final String CAR_TEL_NUM = "car_telephone_number";
    public static final String COMPANY_MAIN_TEL_NUM = "company_main_telephone_number";
    public static final String HOME_FAX_NUM = "home_fax_number";
    public static final String BUSINESS_FAX_NUM = "business_fax_number";
    public static final String HOME_TEL_NUM = "home_telephone_number";
    public static final String PRIMARY_TEL_NUM = "primary_telephone_number";

    public static final String MOBILE_TEL_NUM = "mobile_telephone_number";
    public static final String OTHER_TEL_NUM = "other_telephone_number";
    public static final String OTHER_FAX_NUM = "other_fax_number";
    public static final String PAGER_NUM = "pager_number";
    public static final String RADIO_TEL_NUM = "radio_telephone_number";
    public static final String TELEX_NUM = "telex_number";
    public static final String BUSINESS_TEL_NUM = "business_telephone_number";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String EMAIL_2_ADDRESS = "email_2_address";
    public static final String EMAIL_3_ADDRESS = "email_3_address";
    public static final String WEB_PAGE = "web_page";
    public static final String HOME_WEB_PAGE = "home_web_page";
    public static final String BUSINESS_WEB_PAGE = "business_web_page";
    public static final String X_TAG_ACCOUNT_TYPE = "x_tag_account_type";
    public static final String IM_QQ = "im_qq";
//
//    public static final String BUSINESS_LABEL = "business_label";
//    public static final String HOME_LABEL = "home_label";
//    public static final String OTHER_LABEL = "other_label";
//    public static final String HOME_2_TEL_NUM = "home_2_telephone_number";
//    public static final String BUSINESS_2_TEL_NUM = "business_2_telephone_number";
//    public static final String MOBILE_3_TEL_IMSI = "mobile_3_telephone_imsi";
//    public static final String MOBILE_2_TEL_IMSI = "mobile_2_telephone_imsi";
//    public static final String MOBILE_TEL_IMSI = "mobile_telephone_imsi";
//    public static final String MOBILE_3_TEL_IMEI = "mobile_3_telephone_imei";
//    public static final String MOBILE_2_TEL_IMEI = "mobile_2_telephone_imei";
//    public static final String MOBILE_TEL_IMEI = "mobile_telephone_imei";
//    public static final String IM_GOOGLE = "im_google";
//    public static final String IM_MSN = "im_msn";
//    public static final String MOBILE_3_TEL_NUM = "mobile_3_telephone_number";
//    public static final String MOBILE_2_TEL_NUM = "mobile_2_telephone_number";
    
    //other information
    public static final String LOGIN_EMAIL_ONE = "login_email1";
    public static final String LOGIN_EMAIL_TWO = "login_email2";
    public static final String LOGIN_EMAIL_THREE = "login_email3";
    public static final String LOGIN_PHONE_ONE = "login_phone1";
    public static final String LOGIN_PHONE_TWO = "login_phone2";
    public static final String LOGIN_PHONE_THREE = "login_phone3";
    
    public static final String DISPLAY_NAME = "display_name";
    public static final String FIRST_NAME = "first_name";
    public static final String MIDDLE_NAME = "middle_name";
    public static final String LAST_NAME = "last_name";
    
    public static final String GENDER = "gender";
    public static final String BIRTHDAY = "birthday";
    
    public static final String COMPANY = "company";
    public static final String DEPARTMENT = "department";
    public static final String JOB_TITLE = "job_title";
    public static final String OFFICE_ADDRESS = "office_address";
    public static final String PROFESSION = "profession";
    public static final String JOB_DESCRIPTION = "job_description";

    static {

        PHONE_MATCHER.put(JPhone.ASSISTANT, ASSISTANT_NUM);
        PHONE_MATCHER.put(JPhone.CALLBACK, CALLBACK_NUM);
        PHONE_MATCHER.put(JPhone.CAR, CAR_TEL_NUM);
        PHONE_MATCHER.put(JPhone.COMPANY_MAIN, COMPANY_MAIN_TEL_NUM);
        PHONE_MATCHER.put(JPhone.HOME_FAX, HOME_FAX_NUM);
        PHONE_MATCHER.put(JPhone.WORK_FAX, BUSINESS_FAX_NUM);
        PHONE_MATCHER.put(JPhone.HOME, HOME_TEL_NUM);
        PHONE_MATCHER.put(JPhone.MAIN, PRIMARY_TEL_NUM);
        PHONE_MATCHER.put(JPhone.MOBILE, MOBILE_TEL_NUM);
        PHONE_MATCHER.put(JPhone.OTHER, OTHER_TEL_NUM);
        PHONE_MATCHER.put(JPhone.OTHER_FAX, OTHER_FAX_NUM);
        PHONE_MATCHER.put(JPhone.PAGE, PAGER_NUM);
        PHONE_MATCHER.put(JPhone.RADIO, RADIO_TEL_NUM);
        PHONE_MATCHER.put(JPhone.TELEGRAPH, TELEX_NUM);
        PHONE_MATCHER.put(JPhone.WORK, BUSINESS_TEL_NUM);

        EMAIL_MATCHER.put(JEMail.WORK, EMAIL_ADDRESS);
        EMAIL_MATCHER.put(JEMail.HOME, EMAIL_2_ADDRESS);
        EMAIL_MATCHER.put(JEMail.OTHER, EMAIL_3_ADDRESS);

        WEB_MATCHER.put(JWebpage.HOMEPAGE, WEB_PAGE);
        WEB_MATCHER.put(JWebpage.HOME, HOME_WEB_PAGE);
        WEB_MATCHER.put(JWebpage.WORK, BUSINESS_WEB_PAGE);

        OTHER_MATCHER.put(JXTag.X_ACCOUNT_TYPE, X_TAG_ACCOUNT_TYPE);
        OTHER_MATCHER.put(JIM.QQ, IM_QQ);

        // PUT EXT TYPE
//        OTHER_MATCHER.put(BUSINESS_LABEL, BUSINESS_LABEL);
//        OTHER_MATCHER.put(HOME_LABEL, HOME_LABEL);
//        OTHER_MATCHER.put(OTHER_LABEL, OTHER_LABEL);
//        OTHER_MATCHER.put(HOME_2_TEL_NUM, HOME_2_TEL_NUM);
//        OTHER_MATCHER.put(BUSINESS_2_TEL_NUM, BUSINESS_2_TEL_NUM);
//        OTHER_MATCHER.put(MOBILE_3_TEL_IMSI, MOBILE_3_TEL_IMSI);
//        OTHER_MATCHER.put(MOBILE_2_TEL_IMSI, MOBILE_2_TEL_IMSI);
//        OTHER_MATCHER.put(MOBILE_TEL_IMSI, MOBILE_TEL_IMSI);
//        OTHER_MATCHER.put(MOBILE_3_TEL_IMEI, MOBILE_3_TEL_IMEI);
//        OTHER_MATCHER.put(MOBILE_2_TEL_IMEI, MOBILE_2_TEL_IMEI);
//        OTHER_MATCHER.put(MOBILE_TEL_IMEI, MOBILE_TEL_IMEI);
//        OTHER_MATCHER.put(IM_GOOGLE, IM_GOOGLE);
//        OTHER_MATCHER.put(IM_MSN, IM_MSN);
//        OTHER_MATCHER.put(MOBILE_3_TEL_NUM, MOBILE_3_TEL_NUM);
//        OTHER_MATCHER.put(MOBILE_2_TEL_NUM, MOBILE_2_TEL_NUM);
    }
    
    
    
    
    //CHANGE TYPE
//    public static final int CHANGE_TYPE_EMAIL = 1;
//    public static final int CHANGE_TYPE_MOBILE_PHONE = 2;
//    public static final int CHANGE_TYPE_WORK_PHONE = 3;
//    public static final int CHANGE_TYPE_HOME_PHONE = 4;
//    public static final int CHANGE_TYPE_OTHER_PHONE = 5;
//    public static final int CHANGE_TYPE_NAME = 6;
//    public static final int CHANGE_TYPE_HOME_EMAIL = 7;
//    public static final int CHANGE_TYPE_WORK_EMAIL = 8;
//    public static final int CHANGE_TYPE_OTHER_EMAIL = 9;

    public static final String[] CONTACT_INFO_TYPE = {
//            BUSINESS_LABEL, HOME_LABEL, OTHER_LABEL,
//            HOME_2_TEL_NUM, BUSINESS_2_TEL_NUM, MOBILE_3_TEL_IMSI,
//            MOBILE_2_TEL_IMSI, MOBILE_TEL_IMSI, MOBILE_3_TEL_IMEI,
//            MOBILE_2_TEL_IMEI, MOBILE_TEL_IMEI, IM_GOOGLE, IM_MSN,
//            MOBILE_3_TEL_NUM, MOBILE_2_TEL_NUM,

            ASSISTANT_NUM, CALLBACK_NUM, CAR_TEL_NUM, COMPANY_MAIN_TEL_NUM, HOME_FAX_NUM,
            BUSINESS_FAX_NUM, HOME_TEL_NUM, PRIMARY_TEL_NUM,
            MOBILE_TEL_NUM, OTHER_TEL_NUM, OTHER_FAX_NUM, PAGER_NUM, RADIO_TEL_NUM, TELEX_NUM,
            BUSINESS_TEL_NUM,
            EMAIL_ADDRESS, EMAIL_2_ADDRESS, EMAIL_3_ADDRESS, WEB_PAGE,
            HOME_WEB_PAGE, BUSINESS_WEB_PAGE, X_TAG_ACCOUNT_TYPE, IM_QQ
    };
    
    /**
     * get the contact_item_type by account_item_type
     * @param accountItemType
     * @return matched contact_item_type
     */
    public static String getContactItemTypeByAccountItemType(String accountItemType){
        //TODO support more type for change request,current,only support phone(mobile,work,home,other)
        //email(work,home,other),name(first,middle,last)
        Log.e("", "=============getContactItemTypeByAccountItemType accountItemType:" + accountItemType);
        if(accountItemType.endsWith("_telephone_number")){
            //phone
            if(accountItemType.startsWith("mobile_")){
                //mobile
                return JPhone.MOBILE;
            }else if(accountItemType.startsWith("home_")){
                //home
                return JPhone.HOME;
            }else if(accountItemType.startsWith("business_")){
                //work
                return JPhone.WORK;
            }else if(accountItemType.startsWith("other_")){
                //other
                return JPhone.OTHER;
            }
        }else if("email_address".equals(accountItemType)){
            //work email
            return JEMail.WORK;
        }else if("email_1_address".equals(accountItemType)){
            //home email
            return JEMail.HOME;
        }else if("email_2_address".equals(accountItemType)
                || (accountItemType.startsWith("email_") 
                && accountItemType.endsWith("_address")
                && (accountItemType.split("_")) != null
                && (accountItemType.split("_")).length > 2)){
            //other email
            return JEMail.OTHER;
        }
        return accountItemType;
    }
    
    public static boolean isInContactInfo(String type){
        String[] types = type.split("_");
        for(String data : CONTACT_INFO_TYPE){
            if(data.equals(type) || (data.startsWith(types[0]) && data.endsWith(types[types.length-1]))){
                return true;
            }
        }
        return false;
    }

    // md5 and Base64
    private static String md5Base64(String data) {
        return MD5.md5Base64(data.getBytes());
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
        return md5Base64(sign);
    }
    
    /**
     * return the phone type prefix
     * @param phoneType mobile_telephone_number/business_telephone_number...
     * @return mobile_/business...
     */
    public static String getAccuntPhoneTypePrefix(String phoneType){
        int underlineFirstIndex = phoneType.indexOf("_");
        String prefixPhoneType = phoneType.substring(0, underlineFirstIndex + 1);
        return prefixPhoneType;
    }
    
    /**
     * generate the available account phone type when the phone is added item.
     * @param phoneType specified phone type to generate
     * @param context
     * @param accountJson
     * @return
     */
    public static int generateAvailableAccountPhoneTypeIndex(String phoneType,String accountJson,Context context){
        String accountInfo = accountJson;
        //mobile_2_telephone_number
        int underlineFirstIndex = phoneType.indexOf("_");
        String prefixPhoneType = phoneType.substring(0, underlineFirstIndex + 1);//mobile_
        int maxIndex = -1;
        try {
            JSONObject account = new JSONObject(accountInfo);
            JSONObject contactInfo = account.getJSONObject("contact_info");
            
            Iterator contactInfoKeys = contactInfo.keys();
            while(contactInfoKeys.hasNext()){
                String contactInfoKey = (String)contactInfoKeys.next();
                if(contactInfoKey.startsWith(prefixPhoneType) && contactInfoKey.endsWith("_telephone_number")){
                    //filter phone type
                    //1.like mobile_telephone_number,radio_telephone_number
                    contactInfoKey = contactInfoKey.replace("_telephone_number", "");
                    if(contactInfoKey.indexOf("_") == -1){
                        maxIndex = 0;
                    }else{
                        //2.like mobile_2_telephone_number,radio_2_telephone_number
                        int indexPrefix = contactInfoKey.indexOf("_");
                        int currentIndex = Integer.parseInt(contactInfoKey.substring(indexPrefix + 1, indexPrefix + 2));
                        if(currentIndex > maxIndex){
                            maxIndex = currentIndex;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return maxIndex + 1;
    }
    
    /**
     * generate the available account email type when the email is added item.
     * @param context
     * @param accountJson
     * @return
     */
    public static int generateAvailableAccountEmailTypeIndex(Context context,String accountJson){
        String accountInfo = accountJson;
        //email_address,email_1_address
        int maxIndex = -1;
        try {
            JSONObject account = new JSONObject(accountInfo);
            JSONObject contactInfo = account.getJSONObject("contact_info");
            
            Iterator contactInfoKeys = contactInfo.keys();
            while(contactInfoKeys.hasNext()){
                String contactInfoKey = (String)contactInfoKeys.next();
                if(contactInfoKey.startsWith("email_") && contactInfoKey.endsWith("_address")){
                    //filter phone type
                    //1.like mobile_telephone_number,radio_telephone_number
                    contactInfoKey = contactInfoKey.replace("_address", "");
                    if(contactInfoKey.indexOf("_") == -1){
                        maxIndex = 0;
                    }else{
                        //2.like mobile_2_telephone_number,radio_2_telephone_number
                        int indexPrefix = contactInfoKey.indexOf("_");
                        int currentIndex = Integer.parseInt(contactInfoKey.substring(indexPrefix + 1, indexPrefix + 2));
                        if(currentIndex > maxIndex){
                            maxIndex = currentIndex;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return maxIndex + 1;
    }
    
    public static String getAccountInfo(Context context){
        String ticket = AccountAdapter.getUserData(context, AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_SESSION);
        String userid = AccountAdapter.getUserData(context, AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_UID);
        try {
            String jsonStr = new AccountClient(context, SimpleHttpClient.get()).getProfileDetail(
                    userid, ticket);
            if(!TextUtils.isEmpty(jsonStr)){
                JSONArray arrayJson = new JSONArray(jsonStr);
                JSONObject account = arrayJson.getJSONObject(0);
                return account.toString();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AccountException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * query the phone type according to the mobile number
     * @param phoneType  the type mobile belong to.
     * @param mobile the phone want to to queried
     * @param accountJson
     * @param context
     * @return
     */
    public static String getAccountPhoneTypeByValue(String phoneType,String mobile,String accountJson,Context context){
        String accountInfo = accountJson;
        int underlineFirstIndex = phoneType.indexOf("_");
        String prefixPhoneType = phoneType.substring(0, underlineFirstIndex + 1);//mobile_
        try {
            JSONObject account = new JSONObject(accountInfo);
            JSONObject contactInfo = account.getJSONObject("contact_info");
            
            Iterator contactInfoKeys = contactInfo.keys();
            while(contactInfoKeys.hasNext()){
                String contactInfoKey = (String)contactInfoKeys.next();
                 if(contactInfoKey.startsWith(prefixPhoneType) && contactInfoKey.endsWith("_telephone_number")){
                     String queriedMobile = contactInfo.getString(contactInfoKey);
                     if(TextUtils.equals(queriedMobile, mobile)){
                         return contactInfoKey;
                     }
                 }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * query the email type by the original mobile number
     * @param emailType
     * @param emailValue the email want to queried
     * @param accountJson
     * @param context
     * @return
     */
    public static String getAccountEmailTypeByValue(String emailType,String emailValue,String accountJson,Context context){
        //email_address,email_1_address,email_2_address
        String accountInfo = accountJson;
        try {
            JSONObject account = new JSONObject(accountInfo);
            JSONObject contactInfo = account.getJSONObject("contact_info");
            
            Iterator contactInfoKeys = contactInfo.keys();
            while(contactInfoKeys.hasNext()){
                String contactInfoKey = (String)contactInfoKeys.next();
                 if(contactInfoKey.startsWith("email_") && contactInfoKey.endsWith("_address")){
                     String queriedMobile = contactInfo.getString(contactInfoKey);
                     if(TextUtils.equals(queriedMobile, emailValue)){
                         return contactInfoKey;
                     }
                 }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * check the phone if is bound
     * @param phoneValue
     * @param accountJson
     * @param context
     * @return
     */
    public static boolean isBoundPhone(String phoneValue,String accountJson,Context context){
        return isBoundValue(phoneValue,"login_phone",accountJson,context);
    }
    
    /**
     * check the email if is bound
     * @param emailValue
     * @param accountJson
     * @param context
     * @return
     */
    public static boolean isBoundEmail(String emailValue,String accountJson,Context context){
        return isBoundValue(emailValue,"login_email",accountJson,context);
    }
    
    private static boolean isBoundValue(String value,String boundPrefix,String accountJson,Context context){
        String accountInfo = accountJson;
        try {
            JSONObject account = new JSONObject(accountInfo);
            Iterator contactInfoKeys = account.keys();
            List<String> boundPhones = new ArrayList<String>();
            while(contactInfoKeys.hasNext()){
                String contactInfoKey = (String)contactInfoKeys.next();
                 if(contactInfoKey.startsWith(boundPrefix)){
                     boundPhones.add(account.getString(contactInfoKey));
                 }
            }
            return boundPhones.contains(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
