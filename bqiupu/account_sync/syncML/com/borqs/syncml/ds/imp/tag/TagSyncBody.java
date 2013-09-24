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


public class TagSyncBody implements ITag {
	public boolean Final;

	public List<ITag> BodyCmds;

	// private List<TagAlert> mAlert;
	// private List<TagPut> mPut;
	// private List<TagStatus> mStatus;
	// private List<TagResults> mResults;

	// private List<TagMap> mMap;

	public TagSyncBody() {
	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;
		
		s += CaculateTagSize.getSize(Final);
		s += CaculateTagSize.getSize(BodyCmds, encode);

		return s;
	}

	// <!ELEMENT SyncBody ((Alert | Atomic | Copy | Exec | Get | Map | Put |
	// Results | Search | Sequence | Status | Sync | Add | Replace | Delete)+,
	// Final?)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		boolean leave = false;
		parser.nextTag();

		do {
			// Alert
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Alert)) {
				TagAlert tag = new TagAlert();
				tag.parse(parser);
				addChild(tag);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Atomic)) { // Atomic
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Copy)) { // Copy
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Exec)) { // Exec
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Get)) { // Get
				TagGet tag = new TagGet();
				tag.parse(parser);
				addChild(tag);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Map)) { // Map
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Put)) { // Put
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Results)) { // Results
				TagResults result = new TagResults();
				result.parse(parser);
				addChild(result);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Search)) { // Search
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Sequence)) { // Sequence
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Status)) { // Status
				TagStatus status = new TagStatus();
				status.parse(parser);
				addChild(status);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Sync)) { // Sync
				TagSync sync = new TagSync();
				sync.parse(parser);
				addChild(sync);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Add)) { // Add
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Replace)) { // Replace
				SyncmlXml.bypassTag(parser);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Delete)) { // Delete
				SyncmlXml.bypassTag(parser);
			} else {
				leave = true;
			}
		} while (!leave);
		// Final?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Final)) {
			SyncmlXml.ignoreTag(parser, SyncML.Final);
			Final = true;
		} else {
			Final = false;
		}
		parser.nextTag();
	}

	// <!ELEMENT SyncBody ((Alert | Atomic | Copy | Exec | Get | Map | Put |
	// Results | Search | Sequence | Status | Sync | Add | Replace | Delete)+,
	// Final?)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.SyncBody);

		// if (mStatus != null) {
		// for (TagStatus tag : mStatus) {
		// tag.put(writer);
		// }
		// }
		// if (mAlert != null) {
		// for (TagAlert alert : mAlert) {
		// alert.put(writer);
		// }
		// }
		// if (mPut != null) {
		// for (TagPut tag : mPut) {
		// tag.put(writer);
		// }
		// }
		// if (mResults != null) {
		// for (TagResults tag : mResults) {
		// tag.put(writer);
		// }
		// }
		if (BodyCmds != null) {
			for (ITag tag : BodyCmds) {
				tag.put(writer);
			}
		}
		if (Final) {
			SyncmlXml.putTagText(writer, SyncML.Final, null);
		}
		writer.endTag(null, SyncML.SyncBody);
	}

	public void addChild(ITag tag) {
		if (BodyCmds == null) {
			BodyCmds = new ArrayList<ITag>();
		}
		BodyCmds.add(tag);
	}

	// public void addAlert(TagAlert alert) {
	// if (mAlert == null) {
	// mAlert = new ArrayList<TagAlert>();
	// }
	// mAlert.add(alert);
	// }

	public String name() {
		return SyncML.SyncBody;
	}
	//
	// public void addPut(TagPut tag) {
	// if (mPut == null) {
	// mPut = new ArrayList<TagPut>();
	// }
	// mPut.add(tag);
	// }
	//
	// public void addStatus(TagStatus tag) {
	// if (mStatus == null) {
	// mStatus = new ArrayList<TagStatus>();
	// }
	// mStatus.add(tag);
	// }
	//
	// public void addResult(TagResults tag) {
	// if (mResults == null) {
	// mResults = new ArrayList<TagResults>();
	// }
	// mResults.add(tag);
	// }
}
