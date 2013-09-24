/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.common;

import android.text.TextUtils;

public class Util {
    
    public static String trimString(String str){
        if(TextUtils.isEmpty(str)){
            return str;
        }
        return str.trim();
    }

}
