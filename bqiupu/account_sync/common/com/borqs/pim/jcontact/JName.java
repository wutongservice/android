/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;

public class JName extends JContactProperty{
    //package constant
    public static final String FIRSTNAME = "FN";
    public static final String FIRSTNAME_PINYIN = "FN_PY";
    public static final String MIDDLENAME = "MN";
    public static final String MIDDLENAME_PINYIN = "MN_PY";
    public static final String LASTNAME = "LN";
    public static final String LASTNAME_PINYIN = "LN_PY";
    public static final String PREFIX = "PRE";
    public static final String POSTFIX = "POST";
    public static final String NICKNAME = "NN";

    @Override
    public String getType() {
        //Not a typed property
        return null;
    }

    @Override
    public Object getValue() {
        //Not a typed property
        return null;
    }

    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        return data;
    }
}
