package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public interface ITag {
	String name();

	int size(String encode) throws UnsupportedEncodingException;

	void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException;

	void put(XmlSerializer writer) throws IOException;
}
