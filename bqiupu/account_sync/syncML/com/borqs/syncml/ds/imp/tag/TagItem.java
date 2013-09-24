/**
 * Borqs 2008
 */
package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


/**
 * <!-- Item element type --> <!ELEMENT Item (Target?, Source?, Meta?, Data?,
 * MoreData?)>
 * 
 * @author b059
 * 
 */
public class TagItem implements ITag {
	public TagTarget Target;
	public TagSource Source;
	public TagMeta Meta;
	private VItemData Data;
	public boolean MoreData;

	public TagItem(TagTarget Target, TagSource Source, TagMeta Meta,
			VItemData Data, boolean MoreData) {
		this.Target = Target;
		this.Source = Source;
		this.Meta = Meta;
		this.Data = Data;
		this.MoreData = MoreData;
	}

	public TagItem() {
	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;
		
		s += CaculateTagSize.getSize(Target, encode);
		s += CaculateTagSize.getSize(Source, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		s += CaculateTagSize.getSize(Data, encode);
		s += CaculateTagSize.getSize(MoreData);

		return s;
	}
	
	public byte[] getByteData() {
		if (Data != null && Data.PcData != null
				&& Data.PcData instanceof TagString) {
			return ((TagString) (Data.PcData)).data();
		} else {
			return null;
		}
	}

	public void setData(ITag data) {
		if (Data == null) {
			Data = new VItemData();
		}
		Data.PcData = data;
	}

	public ITag getData() {
		if (Data != null) {
			return Data.PcData;
		} else {
			return null;
		}
	}

	public String getSrcLocUri() {
		if (Source == null) {
			return null;
		} else {
			return Source.LocURI;
		}
	}

	public String getTarLocUri() {
		if (Target == null) {
			return null;
		} else {
			return Target.LocURI;
		}
	}

	// <!ELEMENT Item (Target?, Source?, Meta?, Data?, MoreData?)>
	public void parse(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		// Item (
		parser.nextTag();
		// Target?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Target)) {
			Target = new TagTarget(parser);
		}
		// , Source?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Source)) {
			Source = new TagSource(parser);
		}
		// , Meta?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Meta)) {
			Meta = new TagMeta();
			Meta.parse(parser);
		}
		// , Data?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Data)) {
			Data = new VItemData();
			Data.parse(parser);
			//
			// if (Meta == null) {
			// SyncmlXml.ignoreTag(parser, SyncML.Data);
			// } else {
			// content = SyncmlXml.readText(parser);
			// }
		}
		// , MoreData?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.MoreData)) {
			MoreData = true;
			SyncmlXml.ignoreTag(parser, SyncML.MoreData);
		}
		// )
		parser.nextTag();
	}

	// <!ELEMENT Item (Target?, Source?, Meta?, Data?, MoreData?)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Item);
		if (Target != null) {
			Target.put(writer);
		}
		if (Source != null) {
			Source.put(writer);
		}
		if (Meta != null) {
			Meta.put(writer);
		}
		if (Data != null) {
			Data.put(writer);
		}
		if (MoreData) {
			SyncmlXml.putTagText(writer, SyncML.MoreData, null);
		}
		writer.endTag(null, SyncML.Item);
	}

	public String name() {
		return SyncML.Item;
	}
}
