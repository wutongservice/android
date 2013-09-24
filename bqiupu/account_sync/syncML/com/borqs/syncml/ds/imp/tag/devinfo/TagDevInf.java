package com.borqs.syncml.ds.imp.tag.devinfo;

import java.io.IOException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.imp.tag.ITag;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.DevInf;


//<!ELEMENT DevInf (VerDTD, Man?, Mod?, OEM?, FwV?, SwV?, 
//HwV?, DevID, DevTyp, UTC?, SupportLargeObjs?, SupportNumberOfChanges?, 
//DataStore+, CTCap*, Ext*)>
public class TagDevInf implements ITag {
	public String VerDTD;
	public String Man;
	public String Mod;
	public String OEM;
	public String FwV;
	public String SwV;
	public String HwV;
	public String DevID;
	public String DevTyp;

	public boolean UTC;
	public boolean SupportLargeObjs;
	public boolean SupportNumberOfChanges;

	public String name() {
		return "DevInf";
	}

	// <!ELEMENT DevInf (VerDTD, Man?, Mod?, OEM?, FwV?, SwV?,
	// HwV?, DevID, DevTyp, UTC?, SupportLargeObjs?, SupportNumberOfChanges?,
	// DataStore+, CTCap*, Ext*)>
	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		// DevInf
		parser.nextTag();

		parser.nextTag();
		// VerDTD
		VerDTD = SyncmlXml.readText(parser);
		// , Man?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.Man)) {
			Man = SyncmlXml.readText(parser);
		}
		// , Mod?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.Mod)) {
			Mod = SyncmlXml.readText(parser);
		}
		// , OEM?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.OEM)) {
			OEM = SyncmlXml.readText(parser);
		}
		// , FwV?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.FwV)) {
			FwV = SyncmlXml.readText(parser);
		}
		// , SwV?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.SwV)) {
			SwV = SyncmlXml.readText(parser);
		}
		// , HwV?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.HwV)) {
			HwV = SyncmlXml.readText(parser);
		}
		// DevID
		DevID = SyncmlXml.readText(parser);
		// DevTyp
		DevTyp = SyncmlXml.readText(parser);
		// , UTC?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG, DevInf.UTC)) {
			UTC = true;
			SyncmlXml.ignoreTag(parser, DevInf.UTC);
		}
		// , SupportLargeObjs?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
				DevInf.SupportLargeObjs)) {
			SupportLargeObjs = true;
			SyncmlXml.ignoreTag(parser, DevInf.SupportLargeObjs);
		}
		// , SupportNumberOfChanges?
		if (SyncmlXml.peek(parser, XmlPullParser.START_TAG,
				DevInf.SupportNumberOfChanges)) {
			SupportNumberOfChanges = true;
			SyncmlXml.ignoreTag(parser, DevInf.SupportNumberOfChanges);
		}

	}

	public void put(XmlSerializer writer) throws IOException {
		// TODO Auto-generated method stub

	}

	public int size(String encoding) {
		// TODO Auto-generated method stub
		return 0;
	}

}
