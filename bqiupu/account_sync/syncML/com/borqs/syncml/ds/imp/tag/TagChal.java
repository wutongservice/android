package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


//<!-- Authentication Challenge -->
//<!ELEMENT Chal (Meta)>
public class TagChal implements ITag {
	public TagMeta Meta;

	public void parse(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.nextTag();
		Meta = new TagMeta();
		Meta.parse(parser);
		parser.nextTag();
	}

	public void put(XmlSerializer writer) {
		// TODO Auto-generated method stub

	}

	public String name() {
		return SyncML.Chal;
	}

	public int size(String encode) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 0;
	}

}
