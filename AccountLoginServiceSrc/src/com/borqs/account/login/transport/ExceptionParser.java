/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

/**
 * Date: 6/7/12
 * Time: 3:42 PM
 * Borqs project
 */

import java.io.IOException;

import android.content.Context;
import android.text.TextUtils;

import com.borqs.account.login.R;

public class ExceptionParser {

    // helper function to parse the exception cause
    public static String getCauseOfException(Context c, Exception e) {
        String cause = c.getString(R.string.acl_error_unknown);
       /* if (e == null) {
            return cause;
        }

        if (e instanceof IOException) {
            cause = c.getString(R.string.error_cause_IO_error,
                    e.getLocalizedMessage());
        } else if (e instanceof AccountException) {
            int errorCode = ((AccountException) e).getErrorCode();
            if (errorCode == AccountException.INVALID_CODE) {
                cause = c.getString(R.string.error_cause_bad_data);
            } else if (errorCode == AccountException.REGISTER_DUPLICATED_EMAIL_CODE) {
                cause = c.getString(R.string.error_register_duplicated_email);
            } else if (errorCode == AccountException.REGISTER_DUPLICATED_MOBILE_CODE) {
                cause = c.getString(R.string.error_register_duplicated_mobile);
            } else if (errorCode == AccountException.LOGIN_INVALID_USER_PASSWORD) {
                cause = c.getString(R.string.error_login_invalid_user_password);
            } else if (errorCode == AccountException.LOGIN_NAME_OR_PASSWORD_ERROR) {
                cause = c
                        .getString(R.string.error_login_invalid_login_name_or_password);
            } else if (errorCode == AccountException.LOGIN_NAME_EXISTS) {
                cause = c.getString(R.string.error_login_name_exists);
            } else if (errorCode == AccountException.USER_NOT_EXISTS) {
                cause = c.getString(R.string.error_login_account_not_exists);
            } else if (errorCode == AccountException.GENERATE_USER_ID_ERROR) {
                cause = c.getString(R.string.error_login_register_server_error);
            } else if (errorCode == AccountException.CREATE_SESSION_ERROR) {
                cause = c.getString(R.string.error_login_register_server_error);
            } else {
                cause = c.getString(R.string.error_cause_server_error,
                        e.getMessage());
            }
        }

        if (TextUtils.isEmpty(cause)) {
            cause = c.getString(R.string.error_cause_unknown);
        }*/

        return cause;
    }
}
