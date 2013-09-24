package com.borqs.syncml.ds.imp.tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.imp.tag.devinfo.TagDevInf;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.Wbxml;
import com.borqs.syncml.ds.xml.WbxmlParser;
import com.borqs.syncml.ds.xml.WbxmlSerializer;
import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;


/**
 * 
 * @author b059 <!ELEMENT Results (CmdID, MsgRef?, CmdRef, Meta?, TargetRef?,
 *         SourceRef?, Item+)>
 */
public class TagResults implements ITag {
	public String CmdID;
	public String MsgRef;
	public String CmdRef;
	public TagMeta Meta;
	public String TargetRef;
	public String SourceRef;
	public List<TagItem> mItems;

	private IDeviceInfo mDeviceInfo;
	private int mSyncRequestItem;

	public TagResults() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		// only use in handle
		int s = CaculateTagSize.TAG_SIZE;
		//TODO:....		
		return s;
	}
	
	public TagResults(IDeviceInfo deviceInfo, int syncRequestItem) {
		mDeviceInfo = deviceInfo;
		mSyncRequestItem = syncRequestItem;
	}

	public void put(XmlSerializer writer) throws IOException {
		// set result
		writer.startTag(null, SyncML.Results);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		if (MsgRef != null) {
			SyncmlXml.putTagText(writer, SyncML.MsgRef, MsgRef);
		}
		SyncmlXml.putTagText(writer, SyncML.CmdRef, CmdRef);

		writer.startTag(null, SyncML.Meta);
		SyncmlXml.putTagText(writer, MetInf.Type,
				"application/vnd.syncml-devinf+wbxml");
		writer.endTag(null, SyncML.Meta); // Meta
		SyncmlXml.putTagText(writer, SyncML.SourceRef, "./devinf11");

		writer.startTag(null, SyncML.Item);
		writer.startTag(null, SyncML.Data);
		((WbxmlSerializer) writer).writeWapExtension(Wbxml.OPAQUE, mDeviceInfo
				.deviceData(true, mSyncRequestItem));
		writer.endTag(null, SyncML.Data); // Data

		writer.endTag(null, SyncML.Item); // Item
		writer.endTag(null, SyncML.Results); // Results

	}

	public String name() {
		return SyncML.Results;
	}

	// <!ELEMENT Results (CmdID, MsgRef?, CmdRef, Meta?, TargetRef?,SourceRef?,
	// Item+)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Get
		parser.nextTag();

		// CmdID
		CmdID = SyncmlXml.readText(parser);

		// MsgRef?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.MsgRef)) {
			MsgRef = SyncmlXml.readText(parser);
		}

		// CmdRef
		CmdRef = SyncmlXml.readText(parser);

		// Meta?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Meta)) {
			Meta = new TagMeta();
			Meta.parse(parser);
		}

		// TargetRef?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.TargetRef)) {
			TargetRef = SyncmlXml.readText(parser);
		}

		// SourceRef?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.SourceRef)) {
			SourceRef = SyncmlXml.readText(parser);
		}

		// Item+
		boolean leave = false;
		mItems = new LinkedList<TagItem>();
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Item)) {
				TagItem item = new TagItem();
				item.parse(parser);
				mItems.add(item);
			} else {
				leave = true;
			}
		} while (!leave);
		parser.nextTag();
	}

	public TagDevInf getDevInf() {
		if (mItems != null && mItems.size() > 0) {
			TagDevInf devInf = new TagDevInf();

			TagItem item = mItems.get(0);
			WbxmlParser parser = SyncmlXml.devParser();
			ByteArrayInputStream input = new ByteArrayInputStream(item
					.getByteData());

			try {
				parser.setInput(input, null);
				devInf.parse(parser);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}

			return devInf;
		}
		return null;
	}
	public void addItem(TagItem item) {
		if (mItems == null) {
			mItems = new ArrayList<TagItem>();
		}
		mItems.add(item);
	}
}
