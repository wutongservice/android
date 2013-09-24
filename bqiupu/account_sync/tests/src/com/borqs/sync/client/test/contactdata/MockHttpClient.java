/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.test.contactdata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class MockHttpClient implements HttpClient{
	private long mTimeout = 0;
	private HttpResponse mResposne;
	private IOException mE;
	
	public void expectTimeout(long timeout){
		mTimeout = timeout;
	}
	
	public void expectResponse(String entity){
		expect(200, entity);
	}
	
	public void expectStatuscode(int status){
		expect(200, "");
	}
	
	public void expectException(IOException e){
		mResposne = null;
		mE = e;
	}
	
	public void expect(int statusCode, String entity){
		DefaultHttpResponseFactory respFactory = new DefaultHttpResponseFactory();
		HttpResponse resp = respFactory.newHttpResponse(
				new ProtocolVersion("HTTP", 1, 1), statusCode, null);
		StringEntity se = null;
		try {
			se = new StringEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		resp.setEntity(se);
		mResposne = resp;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		if(mTimeout > 0){
			try {
				Thread.sleep(mTimeout);
			} catch (InterruptedException e) {
			}
		}
		
		if(mE != null){
			throw mE;
		}
		
		if(mResposne == null){
			throw new IOException();
		}
		return mResposne;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request,
			HttpContext context) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest arg0,
			ResponseHandler<? extends T> arg1, HttpContext arg2)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2, HttpContext arg3)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
