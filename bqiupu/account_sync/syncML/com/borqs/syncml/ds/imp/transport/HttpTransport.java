package com.borqs.syncml.ds.imp.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import android.os.Build;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParamBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.borqs.common.transport.TrafficStatsConstant;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.engine.SyncResponse;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.IRequest;
import com.borqs.syncml.ds.protocol.IResponse;
import com.borqs.syncml.ds.protocol.ITransportAgent;
import com.borqs.syncml.ds.xml.SyncmlXml;

import android.net.TrafficStats;
import android.text.TextUtils;
import android.util.Log;


public class HttpTransport implements ITransportAgent {
	private static final String  TAG = "HttpTransport";
	private DefaultHttpClient httpclient;
	private HttpHost httpHost;
	private IDeviceInfo mDeviceInfo;
	private ClientConnectionManager mCcm;
	private IProfile mProfile;
	private boolean mShutDown;
	private boolean mPrintLog;
	
	public HttpTransport(IProfile profile) throws MalformedURLException {
		mProfile = profile;
		mDeviceInfo = profile.getDeviceInfo();
		mPrintLog = profile.getDeviceInfo().isPrintLog();
		init();
	}

	private void init() throws MalformedURLException {

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");

		mCcm = new ThreadSafeClientConnManager(params, schemeRegistry);

		httpclient = new DefaultHttpClient(mCcm, null);

		ConnManagerParamBean bean = new ConnManagerParamBean(params);
		bean.setTimeout(10 * 60 * 1000);
		HttpConnectionParams.setConnectionTimeout(params, 10 * 60 * 1000);
		HttpConnectionParams.setSoTimeout(params, 10 * 60 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, mDeviceInfo
				.getMaxMsgSize());
		httpclient.setParams(params);

		httpclient
				.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
						3, false));
		URL url = new URL(mProfile.getServerUrl());
		httpHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());

		if (!TextUtils.isEmpty(mProfile.proxyName())) {
			final HttpHost proxy = new HttpHost(mProfile.proxyName(), mProfile
					.proxyPort(), url.getProtocol());
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
	}

	public IResponse post(IRequest request) throws DsException {
		IResponse returnData = null;
		//tag network operation for ddms monitor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            TrafficStats.setThreadStatsTag(TrafficStatsConstant.TRAFFIC_TAG_SYNC);
		try {
			returnData = oldPost(request);
		} catch (DsException e) {
			// reset and try again.
			if (!mShutDown) {
				try {
					if (mCcm != null) {
						mCcm.shutdown();
					}

					init();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				if(e.getTryAgain()){
					returnData = oldPost(request);
				} else{
					throw e;
				}
			} else {
				throw e;
			}
		} finally {
		  //clear network operation tag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                TrafficStats.clearThreadStatsTag();
		}
		return returnData;
		// return newPost(request);
	}

	private IResponse oldPost(IRequest request) throws DsException {
		try {
			HttpPost httppost = new HttpPost(request.getUrl());
	
			httppost.addHeader(HTTP.USER_AGENT, mDeviceInfo.getUserAgent());
			httppost.addHeader(HTTP.CONTENT_TYPE, "application/vnd.syncml+wbxml");
			httppost.addHeader("Accept-Charset", HTTP.UTF_8);
			httppost.addHeader("Cache-Control", "no-store");
			httppost.addHeader("accept-language", "zh-cn,en");
			httppost.addHeader("accept",
					"application/vnd.syncml+wbxml, text/x-vcard, text/x-vcalendar");

			if (mPrintLog) {
				// send out package
				//if(Config.DEBUG){
					Log.d(TAG, "Thread ID:" + Thread.currentThread().getId()
							+ "Send out package:");
				//}				
				SyncmlXml.printXml(request.getBody());
			}

			ByteArrayEntity reqEntity = new ByteArrayEntity(request.getBody());
			// InputStreamEntity reqEntity = new InputStreamEntity(
			// new ByteArrayInputStream(request.getBody()), -1);
			httppost.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(httpHost, httppost);
			DsException.checkHttpStatus(response);
			HttpEntity resEntity = response.getEntity();
			Header contenType = resEntity.getContentType();
			if (!contenType.getValue().equals("text/xml;charset=utf-8")
				&& !contenType.getValue().equals("text/vnd.wap.wml;charset=utf-8")
				&& !contenType.getValue().equals("application/vnd.syncml+wbxml")
				&& !contenType.getValue().equals("application/xml;charset=utf-8")){
				throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
						DsException.VALUE_MALFORMED_URL);
				
			}
			
			SyncResponse ret;
			if (mPrintLog) {
				byte[] body = EntityUtils.toByteArray(resEntity);
				// receive out package
				//if(Config.DEBUG){
					Log.d(TAG, "Thread ID:" + Thread.currentThread().getId()
							+ "Receive package:");
				//}				
				SyncmlXml.printXml(body);
				ByteArrayInputStream inputBody = new ByteArrayInputStream(body);
				ret = new SyncResponse(inputBody);
			} else {
				ret = new SyncResponse(resEntity.getContent());
			}

			return ret;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
					DsException.VALUE_ACCESS_SERVER, true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
					DsException.VALUE_ACCESS_SERVER, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
					DsException.VALUE_MALFORMED_URL);
		} catch(IllegalStateException e){
			e.printStackTrace();
			throw new DsException(DsException.CATEGORY_CLIENT_SETTING,
					DsException.VALUE_ACCESS_SERVER);
		}
	}

	// Socket clientSocket;
	//
	// private void ensureOpen() throws UnknownHostException, IOException {
	// if (clientSocket == null || clientSocket.isClosed()) {
	// URL url = new URL(mProfile.getServerUrl());
	//
	// clientSocket = new Socket(url.getHost(), url.getPort() > 0 ? url
	// .getPort() : 80);
	// }
	// }
	//
	// private IResponse newPost(IRequest request) {
	// try {
	// ensureOpen();
	// HttpParams params = new BasicHttpParams();
	// HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	// HttpProtocolParams.setContentCharset(params, "UTF-8");
	// ConnManagerParamBean bean = new ConnManagerParamBean(params);
	// bean.setTimeout(30 * 60 * 1000);
	// HttpConnectionParams.setConnectionTimeout(params, 30 * 60 * 1000);
	// HttpConnectionParams.setSoTimeout(params, 30 * 60 * 1000);
	// HttpConnectionParams.setSocketBufferSize(params, mDeviceInfo
	// .getMaxMsgSize());
	//
	// SocketOutputBuffer socketOutputBuffer = new SocketOutputBuffer(
	// clientSocket, 1024, params);
	// SocketInputBuffer socketInputBuffer = new SocketInputBuffer(
	// clientSocket, 1024, params);
	//
	// BasicLineFormatter formatter = new BasicLineFormatter();
	// HttpRequestWriter httpRequestWritter = new HttpRequestWriter(
	// socketOutputBuffer, formatter, params);
	//
	// URL url = new URL(request.getUrl());
	// HttpPost httppost = new HttpPost(url.getPath());
	//
	// httppost.addHeader("Host", url.getHost());
	// httppost.addHeader(HTTP.USER_AGENT, mDeviceInfo.getUserAgent());
	// httppost.addHeader(HTTP.CONTENT_TYPE,
	// "application/vnd.syncml+wbxml; charset=\"utf-8\"");
	// httppost.addHeader("Accept", "application/vnd.syncml+wbxml");
	// httppost.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
	// httppost.addHeader("Accept-Charset", HTTP.UTF_8);
	// httppost.addHeader("Cache-Control", "no-store");
	// httppost.addHeader("Accept-Encodings", HTTP.CHUNK_CODING);
	// httppost.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING);
	//
	// BasicSchemeFactory basicSchemeFactory = new BasicSchemeFactory();
	// AuthScheme authScheme = basicSchemeFactory.newInstance(params);
	// UsernamePasswordCredentials credentails = new
	// UsernamePasswordCredentials(
	// mProfile.getUserName(), mProfile.getPassword());
	// httppost.addHeader(authScheme.authenticate(credentails, httppost));
	//
	// // "Authorization"
	// // httppost.addHeader(HTTP.CONTENT_LEN, Integer.toString(request
	// // .getBody().length));
	//
	// httpRequestWritter.write(httppost);
	//
	// ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(
	// socketOutputBuffer);
	// byte[] data = request.getBody();
	// // for (int i = 0; i < data.length; i += 100) {
	// // chunkedOutputStream.write(data, i, data.length > i + 100 ? 100
	// // : data.length - i);
	// // chunkedOutputStream.flush();
	// // socketOutputBuffer.flush();
	// // }
	// chunkedOutputStream.write(data);
	// chunkedOutputStream.flush();
	// chunkedOutputStream.finish();
	// chunkedOutputStream.close();
	// socketOutputBuffer.flush();
	// clientSocket.getOutputStream().close();
	//
	// DefaultHttpResponseFactory httpResponseFactory = new
	// DefaultHttpResponseFactory();
	// HttpResponseParser httpResponseParser = new HttpResponseParser(
	// socketInputBuffer, null, httpResponseFactory, params);
	//
	// HttpResponse response = (HttpResponse) httpResponseParser.parse();
	// EntityDeserializer entityDeserializer = new EntityDeserializer(
	// new LaxContentLengthStrategy());
	//
	// Header[] headers = response.getAllHeaders();
	// HttpEntity entity = entityDeserializer.deserialize(
	// socketInputBuffer, response);
	// SyncResponse ret = new SyncResponse(entity.getContent());
	//
	// return ret;
	//
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (UnknownHostException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (HttpException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

	public void shutDown() {
		mShutDown = true;
		if (mCcm != null) {
			mCcm.shutdown();
		}

		// if (clientSocket != null) {
		// try {
		// clientSocket.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// clientSocket = null;
		// }
	}
}
