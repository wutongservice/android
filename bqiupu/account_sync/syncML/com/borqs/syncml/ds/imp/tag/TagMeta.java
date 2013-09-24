package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


//<!-- Root Element -->
//<!ELEMENT MetInf (Format?, Type?, Mark?, Size?, Anchor?, Version?, NextNonce?, MaxMsgSize?, 
//MaxObjSize?, EMI*, Mem?)>
public class TagMeta implements ITag {
	public String Format;
	public String Type;
	public String Mark;
	public String Size;
	public TagAnchor Anchor;
	public String Version;
	public String NextNonce;
	public String MaxMsgSize;
	public String MaxObjSize;

	public TagAnchor getAnchor() {
		return Anchor;
	}

	public void setAnchor(TagAnchor anchor) {
		Anchor = anchor;
	}

	public TagMeta() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(Format, encode);
		s += CaculateTagSize.getSize(Type, encode);
		s += CaculateTagSize.getSize(Mark, encode);
		s += CaculateTagSize.getSize(Size, encode);
		s += CaculateTagSize.getSize(Anchor, encode);
		s += CaculateTagSize.getSize(Version, encode);
		s += CaculateTagSize.getSize(NextNonce, encode);
		s += CaculateTagSize.getSize(MaxMsgSize, encode);
		s += CaculateTagSize.getSize(MaxObjSize, encode);

		return s;
	}
	
	// <!ELEMENT MetInf (Format?, Type?, Mark?, Size?, Anchor?, Version?,
	// NextNonce?, MaxMsgSize?, MaxObjSize?, EMI*, Mem?)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.nextTag();
		// Format?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Format)) {
			Format = SyncmlXml.readText(parser);
		}
		// , Type?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Type)) {
			Type = SyncmlXml.readText(parser);
		}
		// , Mark?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Mark)) {
			SyncmlXml.bypassTag(parser);
		}
		// , Size?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Size)) {
			Size = SyncmlXml.readText(parser);
		}
		// , Anchor?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Anchor)) {
			if (Anchor == null) {
				Anchor = new TagAnchor();
			}
			Anchor.parse(parser);
		}
		// , Version?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Version)) {
			SyncmlXml.bypassTag(parser);
		}
		// NextNonce?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.NextNonce)) {
			NextNonce = SyncmlXml.readText(parser);
		}
		// , MaxMsgSize?
		SyncmlXml.ignoreTag(parser, MetInf.MaxMsgSize);
		// , MaxObjSize?
		SyncmlXml.ignoreTag(parser, MetInf.MaxObjSize);
		// , EMI*
		SyncmlXml.ignoreTag(parser, MetInf.EMI);
		// , Mem?
		SyncmlXml.ignoreTag(parser, MetInf.Mem);
		
		parser.nextTag();
	}

	// <!ELEMENT MetInf (Format?, Type?, Mark?, Size?, Anchor?, Version?,
	// NextNonce?, MaxMsgSize?, MaxObjSize?, EMI*, Mem?)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Meta);
		if (Format != null) {
			SyncmlXml.putTagText(writer, MetInf.Format, Format);
		}
		if (Type != null) {
			SyncmlXml.putTagText(writer, MetInf.Type, Type);
		}
		if (Mark != null) {
			SyncmlXml.putTagText(writer, MetInf.Mark, Mark);
		}
		if (Size != null) {
			SyncmlXml.putTagText(writer, MetInf.Size, Size);
		}
		if (Anchor != null) {
			Anchor.put(writer);
		}
		if (Version != null) {
			SyncmlXml.putTagText(writer, MetInf.Version, Version);
		}
		if (NextNonce != null) {
			SyncmlXml.putTagText(writer, MetInf.NextNonce, NextNonce);
		}
		if (MaxMsgSize != null) {
			SyncmlXml.putTagText(writer, MetInf.MaxMsgSize, MaxMsgSize);
		}
		if (MaxObjSize != null) {
			SyncmlXml.putTagText(writer, MetInf.MaxObjSize, MaxObjSize);
		}
		writer.endTag(null, SyncML.Meta);
	}

	public String name() {
		return SyncML.Meta;
	}
}
