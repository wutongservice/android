package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.Wbxml;
import com.borqs.syncml.ds.xml.WbxmlParser;
import com.borqs.syncml.ds.xml.WbxmlSerializer;


public class TagString implements ITag {
	private byte[] mData;
	private int mType;

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;
		
		if (mData != null) {
			s += mData.length;
		}
		return s;
	}
	
	public TagString(int type, byte[] data) {
		mType = type;
		mData = data;
	}

	public TagString(int type) {
		mType = type;
	}

	public byte[] data() {
		return mData;
	}

	public void setData(byte[] data) {
		mData = data;
	}

	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		switch (mType) {
		case XmlPullParser.TEXT:
			mData = parser.getText() == null ? null : parser.getText()
					.getBytes();
			break;
		case WbxmlParser.WAP_EXTENSION:
			Object obj = ((WbxmlParser) parser).getWapExtensionData();
			mData = (byte[]) obj;
			break;
		default:
		}
		parser.next();
	}

	public void put(XmlSerializer writer) throws IOException {
		switch (mType) {
		case WbxmlParser.WAP_EXTENSION:
			if (mData != null) {
				((WbxmlSerializer) writer).writeWapExtension(Wbxml.OPAQUE,
						mData);
			}
			break;
		default:
		}
	}

	public String name() {
		return null;
	}
}
