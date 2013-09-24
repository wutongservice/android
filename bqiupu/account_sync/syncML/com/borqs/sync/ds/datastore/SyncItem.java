package com.borqs.sync.ds.datastore;

import java.io.UnsupportedEncodingException;

import com.borqs.syncml.ds.imp.tag.ICmdTag;
import com.borqs.syncml.ds.imp.tag.TagAdd;
import com.borqs.syncml.ds.imp.tag.TagDelete;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMeta;
import com.borqs.syncml.ds.imp.tag.TagReplace;
import com.borqs.syncml.ds.imp.tag.TagSource;
import com.borqs.syncml.ds.imp.tag.TagString;
import com.borqs.syncml.ds.imp.tag.VItemData;
import com.borqs.syncml.ds.protocol.ISyncItem;
import com.borqs.syncml.ds.xml.WbxmlParser;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;

import android.util.Log;


public class SyncItem implements ISyncItem {
	private static final String TAG = "SyncItem";
	private static final int EMPTY_ITEM_SIZE = 31;
	private String type;
	private int cmd;
	private byte[] content;
	private long hash;

	private String srcLocUri;
	private String tagLocUri;
	private TagItem curTagItem;

	public SyncItem(String src, String tag, String type, int cmd,
			byte[] content, long hash) {
		this.srcLocUri = src;
		this.tagLocUri = tag;
		this.type = type;
		this.cmd = cmd;
		this.content = content;
		this.hash = hash;
		this.curTagItem = null;
	}

	public byte[] getContent() {
		return content;
	}

	public long getHash() {
		return hash;
	}

	public void setSrcLocUri(String srcLocUri) {
		this.srcLocUri = srcLocUri;
	}

	public void setTarLocUri(String tagLocUri) {
		this.tagLocUri = tagLocUri;
	}

	public int getCmd() {
		return cmd;
	}

	public String getSrcLocUri() {
		return srcLocUri;
	}

	public boolean isValid() {
		if ((cmd == CMD_ADD || cmd == CMD_REPLACE) && content == null) {
			return false;
		} else {
			return true;
		}
	}

	public ICmdTag getCmdTag(ICmdTag lastTag) {
		ICmdTag tag = null;
		if (lastTag != null) {
			switch (cmd) {
			case CMD_ADD:
				if (lastTag.name().equals(SyncML.Add)) {
					tag = lastTag;
				}
				break;
			case CMD_REPLACE:
				if (lastTag.name().equals(SyncML.Replace)) {
					tag = lastTag;
				}
				break;
			case CMD_DELETE:
				if (lastTag.name().equals(SyncML.Delete)) {
					tag = lastTag;
				}
				break;
			}
		}

		if (tag == null) {
			TagMeta meta = new TagMeta();
			meta.Type = type;

			switch (cmd) {
			case CMD_ADD:
				tag = new TagAdd(null, false, null, meta);
				break;
			case CMD_REPLACE:
				TagReplace r = new TagReplace();
				r.Meta = meta;
				tag = r;
				break;
			case CMD_DELETE:
				TagDelete t = new TagDelete();
				t.Meta = meta;
				tag = t;
				break;
			default:
				Log.e(TAG, "Wrong cmd:" + cmd);
				return null;
			}
		}
		
		if(curTagItem == null){
			curTagItem = new TagItem(//
					null,// 
					new TagSource(srcLocUri, null),//
					null,//
					cmd == CMD_DELETE ? null : new VItemData(new TagString(
							WbxmlParser.WAP_EXTENSION, content)),// 
					false);
		}
		tag.addItem(curTagItem);
		return tag;
	}

	public int getSize(String encode) throws UnsupportedEncodingException{
		if(curTagItem == null){
			curTagItem = new TagItem(//
					null,// 
					new TagSource(srcLocUri, null),//
					null,//
					cmd == CMD_DELETE ? null : new VItemData(new TagString(
							WbxmlParser.WAP_EXTENSION, content)),// 
					false);
		}
		if(cmd == CMD_DELETE){
			return curTagItem.size(encode) + EMPTY_ITEM_SIZE;
		} else {
			return curTagItem.size(encode);
		}
	}

	public String getType() {
		return type;
	}
}
