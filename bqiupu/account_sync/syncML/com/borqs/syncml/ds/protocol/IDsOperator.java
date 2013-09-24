package com.borqs.syncml.ds.protocol;

import java.io.IOException;


import org.xmlpull.v1.XmlPullParserException;

import com.borqs.syncml.ds.exception.DsException;


public interface IDsOperator {
	void sync(IDatastore datastore) throws DsException, IOException,
			XmlPullParserException;
}
