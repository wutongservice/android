/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.transport;

import android.content.Context;
import android.net.TrafficStats;

import android.os.Build;
import com.borqs.account.login.util.BLog;
import com.borqs.common.account.AccountException;
import com.borqs.common.util.HttpLog;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class SyncHttpRequestExecutor {
    private HttpClient mHttpClient;    
    private HttpRequestRunner mWorkThread;
    protected Context mContext;
    
    /**
     * Call back interface for asynchronous calling
     * @author Borqs
     *
     * @param <T>
     */
    public interface OnResultCallback<T>{
        public void onResult(T result, Exception e);
    };
    
    public SyncHttpRequestExecutor(Context context, HttpClient httpClient){
        mContext = context;
        mHttpClient = httpClient;
    }
    
    protected abstract String getHostServer();
    
    protected HttpResponse doRequest(HttpRequestBase request) throws ClientProtocolException, IOException{
        if(mWorkThread != null && mWorkThread.isAlive()){
            throw new IllegalStateException("Running task: " + mWorkThread.getName());
        }
        
        return executeRequest(request);
    }
    
    protected void doRequestAsync(HttpRequestBase request, OnResultCallback<HttpResponse> callback){
        if(mWorkThread != null && mWorkThread.isAlive()){
            throw new IllegalStateException("Running task: " + mWorkThread.getName());
        }
        
        mWorkThread = new HttpRequestRunner(mHttpClient, request, callback);        
        mWorkThread.start();        
    }
    
    private HttpResponse executeRequest(HttpRequestBase request) throws ClientProtocolException, IOException{
        //tag network operation for ddms monitor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            TrafficStats.setThreadStatsTag(TrafficStatsConstant.TRAFFIC_TAG_HTTP_RESET_ACCESS);
        try{
            return mHttpClient.execute(request);
        }finally{
            //clear network operation tag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                TrafficStats.clearThreadStatsTag();
        }
    }
    
    /**
     * Cancel the current running task
     */
    public void cancel(){
        if(mWorkThread == null || !mWorkThread.isAlive()){
            return;
        }       
        mWorkThread.cancel();
    }
        
    /**
     * wait for a million second for task running
     * @param timeout
     * @throws InterruptedException
     */
    public void waitForOperationCompleted(long timeout){        
        try {
            mWorkThread.join(timeout);
        } catch (InterruptedException e) {}
    }
    
    protected String asString(HttpResponse response)
        throws IOException{
            if(response == null ){
                BLog.e("Got null response.");
                return null;
            }
            
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ){
                BLog.e("response status is not ok,is :" + response.getStatusLine().getStatusCode());
                return null;
            }
            
            HttpEntity entity = response.getEntity();
            if(entity == null){
                BLog.e("Got emtpy response data.");
                return null;
            }
            
            try{
                InputStream is = entity.getContent();
                Header contentEncoding = response.getFirstHeader("content-encoding");
                //Borqs server is using 'gzip' encode content by default
                if(contentEncoding!=null && "gzip".equalsIgnoreCase(contentEncoding.getValue())){
                    is = new GZIPInputStream(is);
                }
                
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer buf = new StringBuffer();
                String line;
                while (null != (line = br.readLine())) {
                    buf.append(line).append("\n");
                }
                is.close();
                HttpLog.d("Response: " + buf.toString());
                
                return buf.toString();
            }catch(IllegalStateException ise){
                BLog.e("Got invalid response data.");
            }
            return null;
    }
    
    protected InputStream asStream(HttpResponse response)
            throws IOException{
                if(response == null ){
                    BLog.e("Got null response.");
                    return null;
                }
                
                if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ){
                    BLog.e("response status is not ok,is :" + response.getStatusLine().getStatusCode());
                    return null;
                }
                
                HttpEntity entity = response.getEntity();
                if(entity == null){
                    BLog.e("Got emtpy response data.");
                    return null;
                }
                
                try{
                    InputStream is = entity.getContent();
                    Header contentEncoding = response.getFirstHeader("content-encoding");
                    //Borqs server is using 'gzip' encode content by default
                    if(contentEncoding!=null && "gzip".equalsIgnoreCase(contentEncoding.getValue())){
                        is = new GZIPInputStream(is);
                    }
                    
                    return is;
                }catch(IllegalStateException ise){
                    BLog.e("Got invalid response data.");
                }
                return null;
        }
    
    
    protected boolean parseBooleanResult(HttpResponse response) throws IOException, AccountException{
        String result = asString(response);
        HttpLog.d("SyncAppClient, server response " + result);
        try {
            JSONObject jresult = new JSONObject(result);
            return Boolean.valueOf(jresult.getString("result"));
        } catch (JSONException e) {            
            e.printStackTrace();
            throw throwError(result);
        }        
    }
        
    protected String paraseValueInJsonResult(HttpResponse response, String result_key) throws IOException, AccountException{
        String result = asString(response);
        HttpLog.d("SyncAppClient, server response " + result);
        try {
            JSONObject jr = new JSONObject(result);
            return jr.getString(result_key);
        } catch (JSONException e) {            
            e.printStackTrace();
            throw throwError(result);
        }        
    }
    
    protected String paraseErrorMsg(HttpResponse response) throws IOException, AccountException {
        String result = asString(response);
        HttpLog.d("AccountClient, server response " + result);
        try {
            JSONObject error = new JSONObject(result);
            return error.getString("error_msg");
        } catch (JSONException e) {
            e.printStackTrace();
            throw throwError(result);
        }
    }
    
    protected AccountException throwError(String jError) {
        int errorCode = 0;
        String errorMsg = "";
        try {
            JSONObject err = new JSONObject(jError);            
            errorCode = err.getInt("error_code");
            errorMsg = err.getString("error_msg");
        } catch (JSONException ie) {
            return new AccountException(ie);
        }
        return new AccountException(errorCode, errorMsg);
    }
    
    
    /**
     * Builder class for http request
     * @author Borqs
     * example: 
     * GET http://cloud.borqs.com/register?phonenumber=&username=Cxt2%40gmail.com&imsi=&nickname=Cxt2&password=MTIzNDU2&imei=860403000057260
     * GET http://cloud.borqs.com/login?password=MTIzNDU2&username=cxt%40gmail.com
     */
    protected class HttpRequestBuilder{
        public static final String GET = HttpGet.METHOD_NAME;
        public static final String POST = HttpPost.METHOD_NAME;
        
        private HashMap<String, String> mParams;
        private String mMethod;
        private String mCommand;
        private HttpEntity mEntity;
        public HttpRequestBuilder(String method, String command){
            mParams = new HashMap<String,String>();
            mMethod = method;
            mCommand = command;
        }
        
        public HttpRequestBuilder parameter(String name, String value){
            mParams.put(name, value);
            return this;
        }
        
        public HttpRequestBuilder parameter(Map<String ,String> paramMap){
            Set<String> keys = paramMap.keySet();
            Iterator<String> it = keys.iterator();
            while(it.hasNext()){
                String key = it.next();
                mParams.put(key, paramMap.get(key));
            }
            return this;
        } 
        
        public HttpRequestBuilder entity(HttpEntity entity){
            if(GET.equalsIgnoreCase(mMethod)){
                throw new IllegalArgumentException("HttpEntity should be used with HttpPost");
            }
            mEntity = entity;
            return this;
        } 
        
        public HttpRequestBase create(){
            String serverHost = getHostServer();
            String requestURL = serverHost + mCommand + "?" + encodeParameters(mParams);

            BLog.d("Server request: " + requestURL);
                        
            HttpRequestBase request = null;
            if(GET.equalsIgnoreCase(mMethod)){
                request = new HttpGet(requestURL);
            } else if(POST.equalsIgnoreCase(mMethod)) {
                request = new HttpPost(requestURL);
                ((HttpPost)request).setEntity(mEntity);
            }
            
            if(request!=null){
                request.addHeader("User-Agent", "qiupu version:7");
                return request;
            }
            
            throw new IllegalArgumentException("Method: " + mMethod);
        }
        
        private String encodeParameters(HashMap<String, String> httpParams) {
            if (null == httpParams) {
                return "";
            }
            Set<String> keys = httpParams.keySet();
            
            StringBuffer buf = new StringBuffer();
            for (String key : keys) {
                if (buf.length() > 0) {
                    buf.append("&");
                }
                try {
                    buf.append(URLEncoder.encode(key, "UTF-8"))
                            .append("=").append(URLEncoder.encode(httpParams.get(key), "UTF-8"));
                } catch (java.io.UnsupportedEncodingException neverHappen) {
                }
            }
            return buf.toString();
        }
    }   
    
    private class HttpRequestRunner extends Thread{     
        private boolean mIsCanceled = false;    
        private HttpRequestBase mRequest;
        private OnResultCallback<HttpResponse> mCallback;
        
        public HttpRequestRunner(HttpClient client, HttpRequestBase request, OnResultCallback<HttpResponse> callback){
            super("HttpRequest");
            mIsCanceled = false;
            mRequest = request;
            mCallback = callback;
        }       
        
        @Override
        public void run() {
            BLog.d("Task start.");
            if (mIsCanceled) {
                BLog.d("Task is canceled.");
                return;
            }
            HttpResponse resp = null;
            Exception ex = null;
            try{
                resp = executeRequest(mRequest);
            }catch(Exception e){
                ex = e;
            } finally {
                if (mIsCanceled) {
                    BLog.d("Task is canceled.");
                    resp = null;
                    ex = new InterruptedException("Canceled.");                 
                }
                if(mCallback != null){
                    mCallback.onResult(resp, ex);
                }
            }

            BLog.d("Task is completed.");
        }

        public void cancel(){
            mIsCanceled = true;
            if(mRequest != null){
                mRequest.abort();
            }
        }
    }
}
