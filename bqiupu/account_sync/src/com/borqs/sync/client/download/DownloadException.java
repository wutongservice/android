/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.download;

public class DownloadException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final int DOWNLOAD_EXCEPTION_ERROR_UNKNOWN = 0;

    public static final int DOWNLOAD_EXCEPTION_ERROR_NO_SDCARD = 1;
    
    public static final int DOWNLOAD_EXCEPTION_ERROR_NO_NETWORK = 2;

    public static final int DOWNLOAD_EXCEPTION_ERROR_INVALID_URL = 3;

    private int mErrorCode;

    private String mErrorMsg;

    public DownloadException(int errorCode, String exceptionMsg) {
        mErrorCode = errorCode;
        mErrorMsg = exceptionMsg;
    }

    public DownloadException(int errorCode) {
        mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getMessage() {
        return mErrorMsg;
    }
    
}
