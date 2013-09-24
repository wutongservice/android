/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.engine;

import com.borqs.syncml.ds.imp.tag.ICmdTag;
import com.borqs.syncml.ds.imp.tag.TagAdd;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMeta;
import com.borqs.syncml.ds.imp.tag.TagReplace;
import com.borqs.syncml.ds.imp.tag.TagSource;
import com.borqs.syncml.ds.imp.tag.TagString;
import com.borqs.syncml.ds.imp.tag.TagSync;
import com.borqs.syncml.ds.imp.tag.VItemData;
import com.borqs.syncml.ds.protocol.ISyncItem;
import com.borqs.syncml.ds.xml.WbxmlParser;




public class LargeObjectUpload {
	private static final int REMAIN_MSG_SIZE = 1 * 1024; // 1K BYTE

	// private SyncContentItem mSyncItem;
	private boolean mFirstPkg;
	private byte[] mContent;
	private boolean mEnd;
	private int mCurPos;

	private int mMaxPartSize;

	private ISyncItem mSyncItem;
	
	private String mTagCmdID;

	public LargeObjectUpload(int maxMsgSize) {
		mMaxPartSize = maxMsgSize - REMAIN_MSG_SIZE;
	}

	public void setSyncItem(ISyncItem syncItem) {
		mFirstPkg = true;
		mCurPos = 0;
		mSyncItem = syncItem;
		mContent = syncItem.getContent();
	}
	
	public void setTagCmdId(String tagCmdID){
	    mTagCmdID = tagCmdID;
	}

	public void upload(TagSync sync) {
		ICmdTag tag;
		TagMeta meta = new TagMeta();
		meta.Type = mSyncItem.getType();

		if (mFirstPkg) {
			meta.Size = Integer.toString(mContent.length);
			mFirstPkg = false;
		}

		switch (mSyncItem.getCmd()) {
		case ISyncItem.CMD_ADD:
			TagAdd a = new TagAdd(null, false, null, meta);
			a.Meta = meta;
			tag = a;
			break;
		case ISyncItem.CMD_REPLACE:
			TagReplace r = new TagReplace();
			r.Meta = meta;
			tag = r;
			break;
		default:
			return;
		}
		tag.setCmdId(mTagCmdID);
		tag.addItem(//
				new TagItem(//
						null,// 
						new TagSource(mSyncItem.getSrcLocUri(), null),//
						null,//
						new VItemData(new TagString(WbxmlParser.WAP_EXTENSION,
								getPart())),// 
						!isEnd()));

		sync.addSyncCmd(tag);
	}

	public boolean isEnd() {
		return mEnd;
	}

	private byte[] getPart() {
		int endIndex;

		if (mCurPos + mMaxPartSize < mContent.length) {
			endIndex = mCurPos + mMaxPartSize;
		} else {
			endIndex = mContent.length;
			mEnd = true;
		}

		byte[] data = new byte[endIndex - mCurPos];
		System.arraycopy(mContent, mCurPos, data, 0, data.length);
		
		mCurPos = endIndex;
		return data;
	}

	public static boolean isLargeObject(byte[] content, int maxMsgSize) {
		if (content != null && content.length > (maxMsgSize - REMAIN_MSG_SIZE)) {
			return true;
		} else {
			return false;
		}
	}
}
