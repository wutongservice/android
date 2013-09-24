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


public class TagSync implements ITag {
	public String CmdID;
	public boolean NoResp;
	public TagCred Cred;
	public TagTarget Target;
	public TagSource Source;
	public TagMeta Meta;
	public String NumberOfChanges;

	public List<ITag> mSyncCmds;

	public TagSync() {
		mSyncCmds = new ArrayList<ITag>();
	}


	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(CmdID, encode);
		s += CaculateTagSize.getSize(NoResp);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Target, encode);
		s += CaculateTagSize.getSize(Source, encode);
		s += CaculateTagSize.getSize(Meta, encode);
		s += CaculateTagSize.getSize(NumberOfChanges, encode);
		s += CaculateTagSize.getSize(mSyncCmds, encode);

		return s;
	}
	
	// <!ELEMENT Sync (CmdID, NoResp?, Cred?, Target?, Source?, Meta?,
	// NumberOfChanges?, (Add | Atomic | Copy | Delete | Replace | Sequence)*)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// Sync (
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
		// Target?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Target)) {
			Target = new TagTarget(parser);
		}
		// Source?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Source)) {
			Source = new TagSource(parser);
		}
		// Meta?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Meta)) {
			Meta = new TagMeta();
			Meta.parse(parser);
		}
		// NumberOfChanges?,
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
				SyncML.NumberOfChanges)) {
			NumberOfChanges = SyncmlXml.readText(parser);
		}
		// (
		boolean leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Add)) {
				// Add |
				TagAdd cmd = new TagAdd();
				cmd.parse(parser);
				addSyncCmd(cmd);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Atomic)) {
				// Atomic |
				SyncmlXml.ignoreTag(parser, SyncML.Atomic);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Copy)) {
				// Copy |
				SyncmlXml.ignoreTag(parser, SyncML.Copy);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Delete)) {
				// Delete |
				TagDelete cmd = new TagDelete();
				cmd.parse(parser);
				addSyncCmd(cmd);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Replace)) {
				// Replace |
				TagReplace cmd = new TagReplace();
				cmd.parse(parser);
				addSyncCmd(cmd);
			} else if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.Sequence)) {
				// Sequence
				SyncmlXml.ignoreTag(parser, SyncML.Sequence);
			} else {
				leave = true;
			}

			// )*
		} while (!leave);
		// )
		parser.nextTag();
	}

	public String name() {
		return SyncML.Sync;
	}

	public void addSyncCmd(ITag tag) {
		if (mSyncCmds == null) {
			mSyncCmds = new ArrayList<ITag>();
		}
		mSyncCmds.add(tag);
	}

	// <!ELEMENT Sync (CmdID, NoResp?, Cred?, Target?, Source?, Meta?,
	// NumberOfChanges?, (Add | Atomic | Copy | Delete | Replace | Sequence)*)>
	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Sync);
		SyncmlXml.putTagText(writer, SyncML.CmdID, CmdID);
		if (NoResp) {
			SyncmlXml.putTagText(writer, SyncML.NoResp, null);
		}
		if (Target != null) {
			Target.put(writer);
		}
		if (Source != null) {
			Source.put(writer);
		}
		if (Meta != null) {
			Meta.put(writer);
		}
		if (NumberOfChanges != null) {
			SyncmlXml.putTagText(writer, SyncML.NumberOfChanges,
					NumberOfChanges);
		}
		if (mSyncCmds != null) {
			for (ITag tag : mSyncCmds) {
				tag.put(writer);
			}
		}
		writer.endTag(null, SyncML.Sync);
	}
}
