package com.borqs.syncml.ds.imp.tag;

public interface ICmdTag extends ITag {
	void setCmdId(String id);

	void addItem(TagItem item);
}
