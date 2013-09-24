package com.borqs.syncml.ds.protocol;

import java.io.UnsupportedEncodingException;

import com.borqs.syncml.ds.imp.tag.ICmdTag;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;




public interface ISyncItem {

	int CMD_ADD = 0;
	int CMD_REPLACE = 1;
	int CMD_DELETE = 2;

	public static String CMD_STRING[] = new String[] {// 
	SyncML.Add,//
			SyncML.Replace,//
			SyncML.Delete // 
	};

	byte[] getContent();

	long getHash();

	int getCmd();

	String getSrcLocUri();

	boolean isValid();

	ICmdTag getCmdTag(ICmdTag lastCmd);

	int getSize(String encode) throws UnsupportedEncodingException;

	/**
	 * 
	 * @return the date type of the sync item
	 */
	String getType();
}
