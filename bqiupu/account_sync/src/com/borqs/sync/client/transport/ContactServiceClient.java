/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.transport;

import android.content.Context;
import com.borqs.common.account.AccountException;
import com.borqs.common.account.Configuration;
import com.borqs.common.transport.SyncHttpRequestExecutor;
import com.borqs.common.util.BLog;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ContactServiceClient extends SyncHttpRequestExecutor {
    
    public ContactServiceClient(Context context, HttpClient httpClient){
        super(context, httpClient);
    }
    
    @Override
    protected String getHostServer() {
        return Configuration.getWebAgentServerHost(mContext);
    }


    /**
     * query every contact's borqsid and write into Contact app
     * if the contact has register Borqs Account. 
     * @param user
     * @return the contacts's borqsids
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     */
    public String getContactBorqsIDs(String user) throws ClientProtocolException, IOException{
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_CONTACT_BORQSIDS)
                .parameter("oid", user)
                .parameter("formated", "false")
                .parameter("cols", "cid,bid")
                .create();
        BLog.d("getContactBorqsIDs url:" + request.getURI().toString());
        return asString(doRequest(request));
    }

    /**
     * query the sourceId by local raw_contact id
     * @param user
     * @param deviceID
     * @param localIDs
     * @return the sourceID json
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     */
    public String getSourceIDsByLuid(String user,String deviceID,List<String> localIDs) throws ClientProtocolException, IOException{
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < localIDs.size(); i++) {
            if (i == localIDs.size() - 1) {
                sb.append(localIDs.get(i));
            } else {
                sb.append(localIDs.get(i)).append(",");
            }
        }
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.POST,
                Servlet.COMMAND_QUERY_CONTACT_SOURCEID).parameter("userid", user)
                .parameter("device", deviceID).entity(new StringEntity(sb.toString())).create();
        return asString(doRequest(request));
    }

    /**
     * query the config file by file name from ContactsServer
     * @return
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     */
    public InputStream queryStaticConfFile(String fileName) throws ClientProtocolException, IOException {
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_STATIC_CONFIG_QUERY)
        .parameter("filename",fileName)
        .create();
        return asStream(doRequest(request));
    }

    public String countContacts(String borqsId) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_CONTACT_COUNT)
                .parameter("userid", borqsId)
                .create();
        return paraseValueInJsonResult(doRequest(request), "result");
    }
    
    private final static class Servlet{
        private static final String COMMAND_FETCH_CHANGE_REQUEST = "profilesuggestion/query_detail";
        private static final String COMMAND_CHANGE_REQUEST_IGNORE = "profilesuggestion/ignore_item";
        private static final String COMMAND_CONFIG_QUERY = "configuration/query";
        private static final String COMMAND_QUERY_CONTACT_REAL_NAME = "contact/real_name";
        private static final String COMMAND_QUERY_CONTACT_BORQSIDS = "contacts/borqsids";
        private static final String COMMAND_QUERY_CONTACT_SOURCEID = "contact/query_sourceid";
        private static final String COMMAND_QUERY_CONTACT_UNCONNECTED = "contact/query_unconnected";
        private static final String COMMAND_PLUS_CONFIG_QUERY = "configfile/query";
        private static final String COMMAND_STATIC_CONFIG_QUERY = "configuration/query_static";
        private static final String COMMAND_QUERY_CONTACT_COUNT = "contact/count";
    }

    
}
