package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


public class TagGet implements ITag {
	public String CmdID;
	public boolean NoResp;
	public String Lang;
	public TagCred Cred;
	public TagMeta Meta;
	public List<TagItem> Items;

	public void addItem(TagItem item) {
		if (Items == null) {
			Items = new ArrayList<TagItem>();
		}

		Items.add(item);
	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		s += CaculateTagSize.getSize(NoResp);
		s += CaculateTagSize.getSize(Lang, encode);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		s += CaculateTagSize.getSize(Items, encode);

		return s;
	}
	
	// <!ELEMENT Get (CmdID, NoResp?, Lang?, Cred?, Meta?, Item+)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Get
		parser.nextTag();

		// CmdID
		CmdID = SyncmlXml.readText(parser);
		// NoResp?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.NoResp)) {
			NoResp = true;
			SyncmlXml.ignoreTag(parser, SyncML.NoResp);
		}
		// Lang?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Lang)) {
			Lang = SyncmlXml.readText(parser);
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
		// Item*
		// List<SyncMLItem> items = new ArrayList<SyncMLItem>();
		boolean leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Item)) {
				SyncmlXml.ignoreTag(parser, SyncML.Item);
				// SyncMLItem item = new SyncMLItem();
				// item.parse(parser);
				// items.add(item);
			} else {
				leave = true;
			}
		} while (!leave);

		parser.nextTag();
	}

	// <!ELEMENT Get (CmdID, NoResp?, Lang?, Cred?, Meta?, Item+)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Get);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		if (Meta != null) {
			Meta.put(writer);
		}
		if (Items != null) {
			for (TagItem item : Items) {
				item.put(writer);
			}
		}
		writer.endTag(null, SyncML.Get);
	}

	public String name() {
		return SyncML.Get;
	}
}
