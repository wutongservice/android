package com.borqs.account.login.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class AccountInfo {
    // contact_info
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
}
