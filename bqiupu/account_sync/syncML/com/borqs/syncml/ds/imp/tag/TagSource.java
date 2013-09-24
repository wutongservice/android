package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class TagSource implements ITag {
	public String LocURI;
	public String LocName;

	public TagSource() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(LocURI, encode);
		s += CaculateTagSize.getSize(LocName, encode);

		return s;
	}
	
	public TagSource(String uri, String name) {
		LocURI = uri;
		LocName = name;
	}

	public TagSource(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parse(parser);
	}

	// <!ELEMENT Source (LocURI, LocName?)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Source (
		parser.nextTag();
		// LocURI
		LocURI = SyncmlXml.readText(parser);
		// , LocName?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.LocName)) {
			LocName = SyncmlXml.readText(parser);
		}
		parser.nextTag();
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Source);
		SyncmlXml.putTagText(writer, SyncML.LocURI, LocURI);
		if (LocName != null) {
			SyncmlXml.putTagText(writer, SyncML.LocName, LocName);
		}
		writer.endTag(null, SyncML.Source);
	}

	public static void putLocUri(XmlSerializer serializer, String uri)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(null, SyncML.Source);
		SyncmlXml.putTagText(serializer, SyncML.LocURI, uri);
		serializer.endTag(null, SyncML.Source);
	}

	public String name() {
		return SyncML.Source;
	}
}
