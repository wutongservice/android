package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.Wbxml;
import com.borqs.syncml.ds.xml.WbxmlSerializer;
import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


/**
 * 
 * @author b059 <!ELEMENT Put (CmdID, NoResp?, Lang?, Cred?, Meta?, Item+)>
 */
public class TagPut implements ITag {
	private IDeviceInfo mDeviceInfo;
	private int mSyncRequestItem;
	public String CmdID;
	public boolean NoResp;
	public String Lang;
	public TagCred Cred;
	public TagMeta Meta;
	public List<TagItem> Items;
	
	public TagPut() {
	}

	public int size(String encode) throws UnsupportedEncodingException {
		// init phase
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		//TODO:....
		return s;
	}
	
	public TagPut(IDeviceInfo deviceInfo, int syncRequestItem) {
		mDeviceInfo = deviceInfo;
		mSyncRequestItem = syncRequestItem;
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Put);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		// meta
		writer.startTag(null, SyncML.Meta);

		SyncmlXml.putTagText(writer, MetInf.Type,
				"application/vnd.syncml-devinf+wbxml");
		writer.endTag(null, SyncML.Meta);
		// meta end

		// item
		writer.startTag(null, SyncML.Item);
		writer.startTag(null, SyncML.Source);
		SyncmlXml.putTagText(writer, SyncML.LocURI, "./devinf11");
		writer.endTag(null, SyncML.Source);

		// data
		writer.startTag(null, SyncML.Data);

		((WbxmlSerializer) writer).writeWapExtension(Wbxml.OPAQUE, mDeviceInfo
				.deviceData(true, mSyncRequestItem));
		// writer.write(tag.toByteArray());

		writer.endTag(null, SyncML.Data);

		writer.endTag(null, SyncML.Item);
		// item end
		writer.endTag(null, SyncML.Put);

	}

	public String name() {
		return SyncML.Put;
	}


	// <!ELEMENT Put (CmdID, NoResp?, Lang?, Cred?, Meta?, Item+)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Put
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
		List<TagItem> items = new ArrayList<TagItem>();
		boolean leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Item)) {
				TagItem item = new TagItem();
				item.parse(parser);
				items.add(item);
			} else {
				leave = true;
			}
		} while (!leave);
		Items = items;
		// )>
		parser.nextTag();
	}}
