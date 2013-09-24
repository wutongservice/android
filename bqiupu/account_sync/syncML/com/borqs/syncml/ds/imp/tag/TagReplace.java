package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


//<!-- Replace operation. -->
//<!ELEMENT Replace (CmdID, NoResp?, Cred?, Meta?, Item+)>
public class TagReplace implements ICmdTag {
	public String CmdID;
	public boolean NoResp;
	public TagCred Cred;
	public TagMeta Meta;
	public List<TagItem> items;

	public TagReplace() {
	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		s += CaculateTagSize.getSize(NoResp);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		s += CaculateTagSize.getSize(items, encode);

		return s;
	}
	// <!-- Replace operation. -->
	// <!ELEMENT Replace (CmdID, NoResp?, Cred?, Meta?, Item+)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Add (
		parser.nextTag();
		// CmdID,
		CmdID = SyncmlXml.readText(parser);
		// NoResp?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.NoResp)) {
			NoResp = true;
			SyncmlXml.ignoreTag(parser, SyncML.NoResp);
		}
		// Cred?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Cred)) {
			Cred = new TagCred(parser);
		}
		// Meta?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Meta)) {
			Meta = new TagMeta();
			Meta.parse(parser);
		}
		// Item+
		boolean leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Item)) {
				TagItem item = new TagItem();
				item.parse(parser);
				addItem(item);
			} else {
				leave = true;
			}
		} while (!leave);
		// )
		parser.nextTag();
	}

	public void setCmdId(String id) {
		CmdID = id;
	}

	public String name() {
		return SyncML.Replace;
	}

	// <!-- Replace operation. -->
	// <!ELEMENT Replace (CmdID, NoResp?, Cred?, Meta?, Item+)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Replace);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		if (NoResp) {
			SyncmlXml.putTagText(writer, SyncML.NoResp, null);
		}
		if (NoResp) {
			SyncmlXml.putTagText(writer, SyncML.NoResp, null);
		}
		if (Cred != null) {
			Cred.put(writer);
		}
		if (Meta != null) {
			Meta.put(writer);
		}
		if (items != null) {
			for (ITag tag : items) {
				tag.put(writer);
			}
		}
		writer.endTag(null, SyncML.Replace);
	}

	public void addItem(TagItem item) {
		if (items == null) {
			items = new LinkedList<TagItem>();
		}

		items.add(item);
	}
}
