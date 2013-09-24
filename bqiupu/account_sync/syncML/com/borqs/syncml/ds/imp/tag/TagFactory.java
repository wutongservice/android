package com.borqs.syncml.ds.imp.tag;

import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;

public class TagFactory {
	static public ITag createTag(String name) {
		if (MetInf.Anchor.equals(name)) {
			return new TagAnchor();
		} else {
			return null;
		}
	}
}
