/**
 * Copy right Borqs 2009
 */
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

import android.text.TextUtils;


/**
 * <!ELEMENT Status (CmdID, MsgRef, CmdRef, Cmd, TargetRef*, SourceRef*, Cred?,
 * Chal?, Data, Item*)>
 * 
 * @author b059
 * 
 */
public class TagStatus implements ITag {
	private String mCmdID;

	private String mMsgRef;

	private String mCmdRef;

	private String mCmd;

	public List<String> mTargetRefs;

	public List<String> mSourceRefs;

	public TagCred Cred;

	public TagChal Chal;

	private int mData;

	public List<TagItem> mItems;

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(mCmdID, encode);
		s += CaculateTagSize.getSize(mMsgRef, encode);
		s += CaculateTagSize.getSize(mCmdRef, encode);
		s += CaculateTagSize.getSize(mCmd, encode);
		s += CaculateTagSize.getSize(mTargetRefs, encode);
		s += CaculateTagSize.getSize(mSourceRefs, encode);
		s += CaculateTagSize.getSize(Cred, encode);
		s += CaculateTagSize.getSize(Chal, encode);
		s += CaculateTagSize.getSize(mData, encode);
		s += CaculateTagSize.getSize(mItems, encode);

		return s;
	}

	public List<TagItem> getItems() {
		return mItems;
	}

	public void addItem(TagItem item) {
		if (mItems == null) {
			mItems = new LinkedList<TagItem>();
		}
		mItems.add(item);
	}

	public TagStatus() {
		this(null, null, null, null, null, null, StatusValue.SUCCESS);
	}

	public TagStatus(String cmdId, String msgref, String cmdref, String cmd,
			String src, String tgt, int status) {
		mSourceRefs = new LinkedList<String>();
		mTargetRefs = new LinkedList<String>();
		mItems = new LinkedList<TagItem>();

		setCmdId(cmdId);
		setMsgRef(msgref);
		setCmdRef(cmdref);
		setCmd(cmd);
		if (src != null) {
			addSrcRef(src);
		}
		if (tgt != null) {
			addTgtRef(tgt);
		}

		mData = status;
	}

	public String getCmdId() {
		return mCmdID;
	}

	public void setCmdId(String cmdId) {
		mCmdID = cmdId;
	}

	public String getMsgRef() {
		return mMsgRef;
	}

	public void setMsgRef(String msgRef) {
		mMsgRef = msgRef;
	}

	public String getCmdRef() {
		return mCmdRef;
	}

	public void setCmdRef(String cmdRef) {
		mCmdRef = cmdRef;
	}

	public String getCmd() {
		return mCmd;
	}

	public void setCmd(String cmd) {
		mCmd = cmd;
	}

	public List<String> getSrcRef() {
		return mSourceRefs;
	}

	public void addSrcRef(String srcRef) {
		mSourceRefs.add(srcRef);
	}

	public List<String> getTgtRef() {
		return mTargetRefs;
	}

	public void addTgtRef(String tgtRef) {
		mTargetRefs.add(tgtRef);
	}

	public int getStatus() {
		return mData;
	}

	public void setStatus(int status) {
		mData = status;
	}

	public boolean isSuccess() {
		return StatusValue.isSuccess(mData);
	}

	// <!ELEMENT Status (CmdID, MsgRef, CmdRef, Cmd, TargetRef*, SourceRef*,
	// Cred?, Chal?, Data, Item*)>
	public void put(XmlSerializer writer) throws IllegalArgumentException,
			IllegalStateException, IOException {
		writer.startTag(null, SyncML.Status);
		SyncmlXml.putTagText(writer, SyncML.CmdID, mCmdID);
		SyncmlXml.putTagText(writer, SyncML.MsgRef, mMsgRef);
		SyncmlXml.putTagText(writer, SyncML.CmdRef, mCmdRef);
		SyncmlXml.putTagText(writer, SyncML.Cmd, mCmd);
		if (mTargetRefs != null) {
			for (String tgt : mTargetRefs) {
				if (!TextUtils.isEmpty(tgt)) {
					SyncmlXml.putTagText(writer, SyncML.TargetRef, tgt);
				}
			}
		}
		if (mSourceRefs != null) {
			for (String src : mSourceRefs) {
				if (!TextUtils.isEmpty(src)) {
					SyncmlXml.putTagText(writer, SyncML.SourceRef, src);
				}
			}
		}
		SyncmlXml.putTagText(writer, SyncML.Data, Integer.toString(mData));
		if (mItems != null) {
			for (TagItem item : mItems) {
				item.put(writer);
			}
		}
		writer.endTag(null, SyncML.Status);
	}

	// <!ELEMENT Status (CmdID, MsgRef, CmdRef, Cmd, TargetRef*, SourceRef*,
	// Cred?, Chal?, Data, Item*)>
	public void parse(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		boolean leave = false;
		parser.nextTag();
		// CmdID
		mCmdID = SyncmlXml.readText(parser);
		// MsgRef
		mMsgRef = SyncmlXml.readText(parser);
		// CmdRef
		mCmdRef = SyncmlXml.readText(parser);
		// , Cmd//
		mCmd = SyncmlXml.readText(parser);
		// , TargetRef*
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.TargetRef)) {
				mTargetRefs.add(SyncmlXml.readText(parser));
			} else {
				leave = true;
			}
		} while (!leave);
		// , SourceRef*,
		leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
					SyncML.SourceRef)) {
				mSourceRefs.add(SyncmlXml.readText(parser));
			} else {
				leave = true;
			}
		} while (!leave);
		// Cred?
		SyncmlXml.ignoreTag(parser, SyncML.Cred);
		// , Chal?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Chal)) {
			Chal = new TagChal();
			Chal.parse(parser);
		}
		// , Data
		mData = Integer.parseInt(SyncmlXml.readText(parser));
		// , Item*
		leave = false;
		do {
			if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, SyncML.Item)) {
				TagItem item = new TagItem();
				item.parse(parser);
				addItem(item);
			} else {
				leave = true;
			}
		} while (!leave);
		parser.nextTag();
	}

	public String name() {
		return SyncML.Status;
	}
}
