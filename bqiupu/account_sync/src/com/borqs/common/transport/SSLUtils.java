/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.transport;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.net.ssl.SSLSocketFactory;

class SSLUtils {
    private static int HANDSHAKE_TIMEOUT = 10000;//10s, gc is 10s    
    private static SSLSocketFactory sInsecureFactory;
    private static SSLSocketFactory sSecureFactory;

    /**
     * Returns a {@link SSLSocketFactory}.  Optionally bypass all SSL certificate checks.
     *
     * @param insecure if true, bypass all SSL certificate checks
     */
    public synchronized static final SSLSocketFactory getSSLSocketFactory(boolean insecure) {
        if (insecure) {
            if (sInsecureFactory == null) {
            	sInsecureFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            }
            return sInsecureFactory;
        } else {
            if (sSecureFactory == null) {
            	sInsecureFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            }
            return sSecureFactory;
        }
    }
}
