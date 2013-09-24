package com.borqs.syncml.ds.imp.engine;

import java.io.IOException;
import java.io.InputStream;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.syncml.ds.protocol.IResponse;
import com.borqs.syncml.ds.xml.SyncmlXml;


public class SyncResponse implements IResponse {
	// private String TAG = "SyncResponse";
	// private byte[] mBody;
	private InputStream mContent;

	public SyncResponse(InputStream content) {
		mContent = content;
	}

	// public void setBody(byte[] byteArray) {
	// mBody = byteArray;
	// Log.d(TAG, "Recieve package:");
	// SyncmlXml.printXml(mBody);
	// }

	public XmlPullParser parser() throws XmlPullParserException {
		// ByteArrayInputStream in = new ByteArrayInputStream(mBody);
		XmlPullParser parser = SyncmlXml.createParser();
		parser.setInput(mContent, "UTF-8");
		return parser;
	}

	public void close() throws IOException {
		if (mContent != null) {
			mContent.close();
		}
	}
}
