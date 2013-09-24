package com.borqs.syncml.ds.imp.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;


// <!ELEMENT Anchor (Last?, Next)>
public class TagAnchor implements ITag {
	private String mLast;
	private String mNext;

	public TagAnchor() {

	}

	public int size(String encode) throws UnsupportedEncodingException {
		int s = CaculateTagSize.TAG_SIZE;

		s += CaculateTagSize.getSize(mLast, encode);
		s += CaculateTagSize.getSize(mNext, encode);

		return s;
	}
	public TagAnchor(String last, String next) {
		mLast = last;
		mNext = next;
	}

	public String getLast() {
		return mLast;
	}

	public void setLast(String last) {
		mLast = last;
	}

	public String getNext() {
		return mNext;
	}

	public void setNext(String next) {
		mNext = next;
	}

	public void parse(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.nextTag();
		// Last?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, MetInf.Last)) {
			mLast = SyncmlXml.readText(parser);
		}
		// , Next
		mNext = SyncmlXml.readText(parser);
		parser.nextTag();
	}

	public void put(XmlSerializer writer) throws IllegalArgumentException,
			IllegalStateException, IOException {
		writer.startTag(null, MetInf.Anchor);
		if (mLast != null) {
			SyncmlXml.putTagText(writer, MetInf.Last, mLast);
		}
		SyncmlXml.putTagText(writer, MetInf.Next, mNext);
		writer.endTag(null, MetInf.Anchor);
	}

	public String name() {
		return MetInf.Anchor;
	}
}
