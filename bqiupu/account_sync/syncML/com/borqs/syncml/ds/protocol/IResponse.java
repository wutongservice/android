package com.borqs.syncml.ds.protocol;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public interface IResponse {

	XmlPullParser parser() throws XmlPullParserException;

	void close() throws IOException;
}
