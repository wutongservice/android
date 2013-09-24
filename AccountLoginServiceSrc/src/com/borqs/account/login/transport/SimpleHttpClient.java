/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.borqs.account.login.util.BLog;

public class SimpleHttpClient {
    public static final int HTTP_IO_TIMEOUT = 30000; // 30s
    private static final int CONNECTION_TIMEOUT = 10000; // 10s

    private static ClientConnectionManager sClientConnectionManager = null;

    /**
     * get a HttpClient instance with timeout settings
     * 
     * @return
     */
    public static HttpClient get() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, HTTP_IO_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClient client = new DefaultHttpClient(getClientConnectionManager(),
                params);
        return client;
    }

    /**
     * shut down and release resource
     */
    public static synchronized void shutdown() {
        if (sClientConnectionManager != null) {
            BLog.d("Shutting down ClientConnectionManager");
            sClientConnectionManager.shutdown();
            sClientConnectionManager = null;
        }
    }

    private static synchronized ClientConnectionManager getClientConnectionManager() {
        if (sClientConnectionManager == null) {
            // After two tries, kill the process. Most likely, this will happen
            // in the background
            // The service will restart itself after about 5 seconds
            // Create a registry for our three schemes; http and https will use
            // built-in factories
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", SSLSocketFactory
                    .getSocketFactory(), 443));

            // Use "insecure" socket factory.
            SSLSocketFactory sf = new SSLSocketFactory(
                    SSLUtils.getSSLSocketFactory(true));
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            // Register the httpts scheme with our factory
            registry.register(new Scheme("httpts", sf, 443));
            // And create a ccm with our registry
            HttpParams params = new BasicHttpParams();
            params.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 25);
            params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                    new ConnPerRoute() {
                        public int getMaxForRoute(HttpRoute route) {
                            return 8;
                        }
                    });
            sClientConnectionManager = new ThreadSafeClientConnManager(params,
                    registry);
        }
        // Null is a valid return result if we get an exception
        return sClientConnectionManager;
    }

}
