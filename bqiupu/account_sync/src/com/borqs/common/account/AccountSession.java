/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

import org.json.JSONException;
import org.json.JSONObject;

public final class AccountSession {
    public String uid;
    public String username;
    public String session_id;
    public String display_name;
    public String urlname = "";
    
	/*
	 * Example for the user session response
	 * {"nickname":"cat",
	 *  "uid":"my1234",
	 *  "sessionid":"s0123456789",
	 *  "username":"myusername",
	 *  "screenname":"myscreenname"
	 * }
	 */
    public static AccountSession from(JSONObject data) throws JSONException{
    	AccountSession user = new AccountSession();
    	user.display_name = data.getString("display_name");
    	user.session_id = data.getString("ticket");
    	user.uid = data.getString("user_id");
    	user.username = data.getString("login_name");
    	
    	return user;
    }
}
