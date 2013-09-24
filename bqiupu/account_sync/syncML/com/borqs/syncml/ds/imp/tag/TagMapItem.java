package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


//<!ELEMENT MapItem (Target, Source)>
public class TagMapItem implements ITag {
	public TagTarget Target;
	public TagSource Source;

	
	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;
		
		s += CaculateTagSize.getSize(Target, encode);
		s += CaculateTagSize.getSize(Source, encode);
		
		return s;
	}

	public TagMapItem(TagTarget Target, TagSource Source) {
		this.Target = Target;
		this.Source = Source;
	}

	public String name() {
		return SyncML.MapItem;
	}

	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.MapItem);
		Target.put(writer);
		Source.put(writer);
		writer.endTag(null, SyncML.MapItem);
	}
}
