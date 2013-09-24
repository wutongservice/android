/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.common.transport;

/**
 * created for traffic status tag define
 * @author b211
 *
 */
public class TrafficStatsConstant {
    
    public static final int TRAFFIC_TAG_HTTP_RESET_ACCESS = 0XA0AA;//for httpclient REST ACCESS
    public static final int TRAFFIC_TAG_HTTP_URL_CONNECTION = 0XA0AB;//for httpurlconnection
    public static final int TRAFFIC_TAG_SYNC = 0XA0AC;//for sync process
}
