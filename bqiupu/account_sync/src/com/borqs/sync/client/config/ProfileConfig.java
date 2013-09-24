/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.config;

import android.content.Context;
import android.text.TextUtils;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.account.Configuration;
import com.borqs.contacts_plus.R;
import com.borqs.syncml.ds.imp.common.CryptUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class ProfileConfig {
    
    public static String getSyncMLServerUrl(Context context) {
        
        String syncMLHost = Configuration.getSyncMLHost(context);
        if (syncMLHost != null && !syncMLHost.endsWith("/")){
            return syncMLHost + "/" + "funambol/ds";
        }else if(TextUtils.isEmpty(syncMLHost)){
            try {
                InputStream inStream = context.getResources().openRawResource(
                        R.raw.syncml_ds_preload_profiles);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(inStream, null);
                while (true) {
                    int type = parser.next();
                    if (type == XmlPullParser.END_TAG && parser.getName().equals("profiles")) {
                        break;
                    } else if (type == XmlPullParser.START_TAG) {
                        String name = parser.getName();
                        if("profile".equals(name)){
                            return parseProfile(parser);
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String();
        }
        else{
            return syncMLHost + "funambol/ds";
        }
    }

    private static String parseProfile(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("profile")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if ("server_url".equals(name)) {
                    return parser.nextText().trim();
                }
            }
        }
        return new String();
    }

    public static String getProfileName(Context context) {
        return AccountAdapter.getLoginID(context);
    }

    public static String getPassword(Context context) {
        String sessionId = AccountAdapter.getUserData(context,
                AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_SESSION);
        String encodedPassword = new CryptUtil().encrypt(sessionId);
        return encodedPassword;
    }

    public static String getUserName(Context context) {
        String uid = AccountAdapter.getUserData(context, AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_UID);
        return uid;
    }
}
