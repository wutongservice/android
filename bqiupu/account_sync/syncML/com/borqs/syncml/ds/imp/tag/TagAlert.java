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


/**
 * 
 * 
 * @author b059 <!ELEMENT Alert (CmdID, NoResp?, Cred?, Data?, Item*)>
 */
public class TagAlert implements ITag {
	public String CmdID;
	public boolean NoResp;
	public TagCred Cred;
	public String Data;
	public List<TagItem> Item;

	public TagAlert() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		s += CaculateTagSize.getSize(NoResp);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Data, encode);
		s += CaculateTagSize.getSize(Item, encode);

		return s;
	}
	
	public TagAlert(String cmdId, String data) {
		CmdID = cmdId;
		Data = data;
	}

	public void addItem(TagItem item) {
		if (Item == null) {
			Item = new ArrayList<TagItem>();
		}
		Item.add(item);
	}

	public void parse(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		// Alert
		parser.nextTag();

		// CmdID
		CmdID = SyncmlXml.readText(parser);
		// NoResp?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.NoResp)) {
			NoResp = true;
			SyncmlXml.ignoreTag(parser, SyncML.NoResp);
		}
		// Cred?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Cred)) {
			Cred = new TagCred(parser);
		}
		// Data?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Data)) {
			Data = SyncmlXml.readText(parser);
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
		Item = items;
		parser.nextTag();
	}

	// <!ELEMENT Alert (CmdID, NoResp?, Cred?, Data?, Item*)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Alert);
		// CmdID
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		// NoResp?
		if (NoResp) {
			SyncmlXml.putTagText(writer, SyncML.NoResp, null);
		}
		// Cred?
		if (Cred != null) {
			Cred.put(writer);
		}
		// Data?
		if (Data != null) {
			SyncmlXml.putTagText(writer, SyncML.Data, Data);
		}
		// Item*
		if (Item != null) {
			for (TagItem tagItem : Item) {
				tagItem.put(writer);
			}
		}

		// writer.startTag(SyncML.Item);
		// // target
		// writer.startTag(SyncML.Target);
		// SyncmlXml.putTagText(writer, SyncML.LocURI, src.getSourceUri());
		// // TODO:handle sync filter
		// writer.endTag();
		//
		// // source
		// writer.startTag(SyncML.Source);
		// SyncmlXml.putTagText(writer, SyncML.LocURI, src.getName());
		// writer.endTag();
		//
		// // Meta
		// writer.startTag(SyncML.Meta);
		// writer.startTag(MetInf.Anchor);
		//
		// if (src.getLastAnchor() != 0l) {
		// SyncmlXml.putTagText(writer, MetInf.Last, Long.toString(src
		// .getLastAnchor()));
		// }
		//
		// SyncmlXml.putTagText(writer, MetInf.Next, Long.toString(src
		// .getNextAnchor()));
		//
		// writer.endTag();
		// writer.endTag();
		// // Meta end
		//
		// writer.endTag();
		// // Item end

		writer.endTag(null, SyncML.Alert);
	}

	public TagAnchor getItemMetaAnchor() {
		TagAnchor anchor = null;
		if (Item != null) {
			for (TagItem item : Item) {
				if (item.Meta != null && item.Meta.getAnchor() != null) {
					anchor = item.Meta.getAnchor();
					break;
				}
			}
		}
		return anchor;
	}

	public String name() {
		return SyncML.Alert;
	}
}
