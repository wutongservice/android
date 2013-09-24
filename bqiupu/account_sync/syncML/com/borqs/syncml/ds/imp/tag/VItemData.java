package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.WbxmlParser;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class VItemData implements ITag {
	public ITag PcData;
	public VItemData() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		return CaculateTagSize.TAG_SIZE + CaculateTagSize.getSize(PcData, encode);
	}

	public VItemData(ITag data) {
		PcData = data;
	}

	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		int type = parser.nextToken();
		switch (type) {
		case XmlPullParser.TEXT:
		case WbxmlParser.WAP_EXTENSION:
			PcData = new TagString(type);
			break;
		case XmlPullParser.START_TAG:
			PcData = TagFactory.createTag(parser.getName());
			break;
		}
		if (PcData != null) {
			PcData.parse(parser);
		} else {
			SyncmlXml.bypassTag(parser);
		}
		parser.nextTag();
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Data);
		if (PcData != null) {
			PcData.put(writer);
		}
		writer.endTag(null, SyncML.Data);
	}

	public String name() {
		return SyncML.Data;
	}
}
