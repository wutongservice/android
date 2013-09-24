package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class TagSyncHdr implements ITag {
	public String VerDTD;
	public String VerProto;
	public String SessionID;
	public String MsgID;
	public TagTarget Target;
	public TagSource Source;
	public String RespURI;
	public boolean NoResp;
	public TagCred Cred;
	public TagMeta Meta;

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(VerDTD, encode);
		s += CaculateTagSize.getSize(VerProto, encode);
		s += CaculateTagSize.getSize(SessionID, encode);
		s += CaculateTagSize.getSize(MsgID, encode);
		s += CaculateTagSize.getSize(Target, encode);
		s += CaculateTagSize.getSize(Source, encode);
		s += CaculateTagSize.getSize(RespURI, encode);
		s += CaculateTagSize.getSize(NoResp);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		
		return s;
	}

	// <!ELEMENT SyncHdr (VerDTD, VerProto, SessionID, MsgID, Target, Source,
	// RespURI?, NoResp?, Cred?, Meta?)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// SyncHdr
		parser.nextTag();
		// VerDTD
		VerDTD = SyncmlXml.readText(parser);
		// VerProto
		VerProto = SyncmlXml.readText(parser);
		// SessionID
		SessionID = SyncmlXml.readText(parser);
		// MsgID
		MsgID = SyncmlXml.readText(parser);
		// Target
		Target = new TagTarget(parser);
		// Source
		Source = new TagSource(parser);

		// RespURI?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.RespURI)) {
			RespURI = SyncmlXml.readText(parser);
		}
		// NoResp?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.NoResp)) {
			NoResp = true;
			SyncmlXml.bypassTag(parser);
		}
		// Cred?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Cred)) {
			Cred = new TagCred(parser);
		}
		// Meta?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Meta)) {
			Meta = new TagMeta();
			Meta.parse(parser);
		}
		parser.nextTag();
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.SyncHdr);
		SyncmlXml.putTagText(writer, SyncML.VerDTD, VerDTD);
		SyncmlXml.putTagText(writer, SyncML.VerProto, VerProto);
		SyncmlXml.putTagText(writer, SyncML.SessionID, SessionID);
		SyncmlXml.putTagText(writer, SyncML.MsgID, MsgID);
		Target.put(writer);
		Source.put(writer);

		if(Cred != null){
			Cred.put(writer);
		}
		
		if (Meta != null) {
			Meta.put(writer);
		}

		writer.endTag(null, SyncML.SyncHdr);
	}

	public String name() {
		return SyncML.SyncHdr;
	}
}
