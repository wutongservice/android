/**
 * Copy right Borqs 2009
 */
package com.borqs.syncml.ds.imp.engine;

import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMeta;
import com.borqs.syncml.ds.imp.tag.TagString;
import com.borqs.syncml.ds.protocol.ISyncItem;
import com.borqs.syncml.ds.xml.WbxmlParser;




public class LargeObjectDownload {
	private byte[] mData;
	private int mSize;
	private int mCurPos;
	private String mSrcLocUri;
	private String mTagLocUri;
	private int mCmd;

	private boolean mOutOfData;

	public LargeObjectDownload(int cmd, TagItem item, TagMeta meta) {
		mCmd = cmd;
		mSize = Integer.parseInt(meta.Size);
		mData = new byte[mSize];
		mCurPos = 0;
		switch (mCmd) {
		case ISyncItem.CMD_REPLACE:
			mTagLocUri = item.getTarLocUri();
			break;
		case ISyncItem.CMD_ADD:
			mSrcLocUri = item.getSrcLocUri();
			break;
		}
	}

	public void addItem(TagItem item) {
		if (mOutOfData) {
			// There is issue in previous operation. Ignore the new operation.
			return;
		}

		byte[] data = item.getByteData();
		if (data != null) {
			if (mCurPos + data.length <= mSize) {
				System.arraycopy(data, 0, mData, mCurPos, data.length);
				mCurPos += data.length;
			} else {
				mOutOfData = true;
			}
		}
	}

	public TagString getFullData() {
		TagString data = new TagString(WbxmlParser.WAP_EXTENSION, mData);
		return data;
	}

	public boolean isSizeMatch() {
		return (!mOutOfData && mSize == mCurPos);
	}

	public boolean isSameItem(TagItem item) {
		switch (mCmd) {
		case ISyncItem.CMD_REPLACE:
			return mTagLocUri.equals(item.getTarLocUri());
		case ISyncItem.CMD_ADD:
			return mSrcLocUri.equals(item.getSrcLocUri());
		default:
			return false;
		}
	}

	public String getSrcLocUri() {
		return mSrcLocUri;
	}
}
