package com.borqs.syncml.ds.imp.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.imp.tag.ITag;
import com.borqs.syncml.ds.protocol.IRequest;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class SyncRequest implements IRequest {
	private static final String TAG = "SyncRequest";
	private XmlSerializer serializer;
	private ByteArrayOutputStream mOutputStream;
	private String mUrl;

	SyncRequest(String url) {
		mUrl = url;
	}

	public void begin() throws IOException {
		serializer = SyncmlXml.createSerializer();
		mOutputStream = new ByteArrayOutputStream();
		serializer.setOutput(mOutputStream, "UTF-8");
		serializer.startDocument("UTF-8", null);
		serializer.startTag(null, SyncML.SyncML);		
	}

	public void end() throws IOException {
		serializer.endTag(null, SyncML.SyncML);
		serializer.endDocument();
	}

	public void write(ITag tag) throws IOException {
		tag.put(serializer);
	}

	public byte[] getBody() {
//		Log.d(TAG, "Send package:");
		byte[] body = mOutputStream.toByteArray();
//		SyncmlXml.printXml(body);
		return body;
	}

	public String getUrl() {
		return mUrl;
	}
	
	public String toString(){
	    String res = "";
	    if (mOutputStream != null){
	        res = mOutputStream.toString();
	    }
	    return res;
	}
}
