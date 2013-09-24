package com.borqs.syncml.ds.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.sync.client.common.Logger;

public class SyncmlXml {
	public static void ignoreTag(XmlPullParser parser, String tag)
			throws IOException, XmlPullParserException {
		if (peek(parser, XmlPullParser.START_TAG, tag)) {
			bypassTag(parser);
		}
	}

	//
	// public static void ignoreTagList(AbstractXmlParser parser, String item)
	// throws IOException {
	// boolean leave = false;
	// do {
	// if (parser.peek(Xml.START_TAG, null, item)) {
	// parser.ignoreTree();
	// } else {
	// leave = true;
	// }
	// } while (!leave);
	// }
	//
	/**
	 * Get next tag text
	 */
	public static String readText(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String text;
		text = parser.nextText();
		parser.next();
		return text;
	}

	public static boolean peek(XmlPullParser parser, int type, String name)
			throws XmlPullParserException {
		return (parser.getEventType() == type && parser.getName().equals(name));
	}

	/**
	 * Get Target or Source LocURI text
	 * 
	 * @param parser
	 * @return LocURI text
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	// public static String getTagLocUri(MyParser parser) throws IOException {
	// String locUri = null;
	// boolean leave = false;
	// do {
	// int eventType = parser.next();
	// String name = parser.getName();
	// switch (eventType) {
	// case XmlPullParser.START_TAG: {
	// if (name.equals(SyncML.LocURI)) {
	// locUri = parser.nextText();
	// } else {
	// SyncmlXml.bypassTag(parser);
	// }
	// break;
	// }
	// case XmlPullParser.END_TAG:
	// leave = true;
	// break;
	// }
	// } while (!leave);
	//
	// return locUri;
	// }
	//
	// public static String getAnchorNext(MyParser parser) throws IOException {
	// String locUri = null;
	// boolean leave = false;
	// do {
	// int eventType = parser.next();
	// String name = parser.getName();
	// switch (eventType) {
	// case XmlPullParser.START_TAG: {
	// if (name.equals(MetInf.Next)) {
	// locUri = parser.nextText();
	// } else {
	// SyncmlXml.bypassTag(parser);
	// }
	// break;
	// }
	// case XmlPullParser.END_TAG:
	// leave = true;
	// break;
	// }
	// } while (!leave);
	//
	// return locUri;
	// //
	// // String next = null;
	// // parser.nextTag(); // last or next
	// // String name = parser.getName();
	// // if (MetInf.Last.equals(name)) {
	// // SyncmlXml.bypassTag(parser); // bypass last
	// // parser.nextTag(); // go to next
	// // name = parser.getName();
	// // }
	// //
	// // if (MetInf.Next.equals(name)) {
	// // next = parser.nextText().trim();
	// // }
	// // int tmpType = parser.next(); //
	// // if (tmpType == XmlPullParser.START_TAG) {
	// // // next
	// // SyncmlXml.bypassTag(parser); // bypass Next
	// // }
	// // SyncmlXml.bypassTag(parser); // bypass Anchor
	// //
	// // return next;
	// }
	// public static void putTagLocUri(MySerializer serializer, String tag,
	// String text) throws IOException {
	// serializer.startTag(null, tag);
	// serializer.startTag(null, SyncML.LocURI);
	// if (text != null) {
	// serializer.text(text);
	// }
	// serializer.endTag(null, SyncML.LocURI);
	// serializer.endTag(null, tag);
	// }
	//
	// public static void putFullData(OutputStream dest, InputStream src)
	// throws IOException {
	// int size = src.available();
	// while (size > 0) {
	// byte[] buffer = new byte[size];
	// src.read(buffer);
	// dest.write(buffer);
	// size = src.available();
	// }
	// }
	//
	public static void putFullData(XmlSerializer dest, InputStream src)
			throws Exception {
		boolean leave = false;
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
		parser.setInput(src, null);

		do {
			int eventType = parser.next();
			switch (eventType) {
			case XmlPullParser.START_TAG:
				dest.startTag(null, parser.getName());
				break;
			case XmlPullParser.TEXT:
				dest.text(parser.getText());
				break;
			case XmlPullParser.END_TAG:
				dest.endTag(null, null);
				break;
			case XmlPullParser.END_DOCUMENT:
				leave = true;
				break;
			}
		} while (!leave);
	}

	//
	// public static String getTagText(XmlPullParser parser)
	// throws XmlPullParserException, IOException {
	// String text = null;
	// boolean leave = false;
	// do {
	// int eventType = parser.next();
	// switch (eventType) {
	// case XmlPullParser.START_TAG: {
	// text = parser.nextText();
	// break;
	// }
	// case XmlPullParser.END_TAG:
	// leave = true;
	// break;
	// }
	// } while (!leave);
	//
	// return text;
	// }
	//
	public static void printXml(byte[] data) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			WbxmlParser parser = createParser();
			parser.setInput(in, "UTF-8");
			parserData(parser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parserData(XmlPullParser parser) {
		boolean leave = false;
		StringBuilder builder = new StringBuilder();
		int eventType;
		try {
			do {
				eventType = parser.nextToken();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					builder.append("<").append(parser.getName()).append(">");
					break;
				case XmlPullParser.TEXT:
					builder.append(parser.getText());
					break;
				case WbxmlParser.WAP_EXTENSION:
					builder.append(new String((byte[]) ((WbxmlParser) parser)
							.getWapExtensionData()));
					break;
				case XmlPullParser.END_TAG:
					builder.append("</").append(parser.getName()).append(">")
							.append("\n");
					Logger.logXML("SyncML", builder.toString());
				
					builder = new StringBuilder();
					break;
				case XmlPullParser.END_DOCUMENT:
					leave = true;
					break;
				}
			} while (!leave);			
			    Logger.logXML("SyncML", builder.toString());			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void putTagText(XmlSerializer writer, String tag, String text)
			throws IOException {
		writer.startTag(null, tag);
		if (text != null) {
			writer.text(text);
		}
		writer.endTag(null, tag);
	}

	//
	// public static void putTagText(MySerializer serializer, String tag,
	// String text) throws IOException {
	// serializer.startTag(null, tag);
	// if (text != null) {
	// serializer.text(text);
	// }
	// serializer.endTag(null, tag);
	// }
	//
	public static void putTagCdsect(XmlSerializer serializer, String tag,
			String text) throws IOException {
		serializer.startTag(null, tag);
		if (text != null) {
			serializer.cdsect(text);
		}
		serializer.endTag(null, tag);
	}

	public static void bypassTag(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		boolean leave = false;
		int depth = parser.getDepth();
		int eventType = parser.getEventType();
		do {
			switch (eventType) {
			case XmlPullParser.END_TAG:
				if (parser.getDepth() == depth) {
					parser.next();
					leave = true;
				}
				break;
			case XmlPullParser.END_DOCUMENT:
				leave = true;
				break;
			}
			if (!leave) {
				eventType = parser.next();
			}
		} while (!leave);
	}

	public static void assertSt(boolean b) {
		if (!b) {
			throw new RuntimeException("Wrong xml status.");
		}
	}

	//
	public static WbxmlParser createParser() {
		WbxmlParser p = new WbxmlParser();
		p.setTagTable(0, TAG_TABLE_0);
		p.setTagTable(1, TAG_TABLE_1);
		return p;
	}

	public static WbxmlParser devParser() {
		WbxmlParser p = new WbxmlParser();
		p.setTagTable(0, DEV_TAG_TABLE_0);
		return p;
	}
	
	//
	// public static MySerializer createSerializer() throws IOException {
	// return MySerializer.serializer();
	// // WbxmlSerializer s = new WbxmlSerializer(0xFD1);
	// // s.setTagTable(0, TAG_TABLE_0);
	// // s.setTagTable(1, TAG_TABLE_1);
	// // return s;
	// }
	//
	public static WbxmlSerializer devInfSerializer() {
		WbxmlSerializer s = new WbxmlSerializer("-//SYNCML//DTD DevInf 1.1//EN");
		s.setTagTable(0, DEV_TAG_TABLE_0);
		return s;
	}

	public static WbxmlSerializer createSerializer() {
		WbxmlSerializer s = new WbxmlSerializer("-//SYNCML//DTD SyncML 1.1//EN");
		s.setTagTable(0, TAG_TABLE_0);
		s.setTagTable(1, TAG_TABLE_1);
		return s;
	}

	// Tables

	public static final String[] TAG_TABLE_0 = { SyncML.Add,// 05
			SyncML.Alert,// 06
			SyncML.Archive,// 07
			SyncML.Atomic,// 08
			SyncML.Chal,// 09
			SyncML.Cmd,// 0A
			SyncML.CmdID,// 0B
			SyncML.CmdRef,// 0C
			SyncML.Copy,// 0D
			SyncML.Cred,// 0E
			SyncML.Data,// 0F
			SyncML.Delete,// 10
			SyncML.Exec,// 11
			SyncML.Final,// 12
			SyncML.Get,// 13
			SyncML.Item,// 14
			SyncML.Lang,// 15
			SyncML.LocName,// 16
			SyncML.LocURI,// 17
			SyncML.Map,// 18
			SyncML.MapItem,// 19
			SyncML.Meta,// 1A
			SyncML.MsgID,// 1B
			SyncML.MsgRef,// 1C
			SyncML.NoResp,// 1D
			SyncML.NoResults,// 1E
			SyncML.Put,// 1F
			SyncML.Replace,// 20
			SyncML.RespURI,// 21
			SyncML.Results,// 22
			SyncML.Search,// 23
			SyncML.Sequence,// 24
			SyncML.SessionID,// 25
			SyncML.SftDel,// 26
			SyncML.Source,// 27
			SyncML.SourceRef,// 28
			SyncML.Status,// 29
			SyncML.Sync,// 2A
			SyncML.SyncBody,// 2B
			SyncML.SyncHdr,// 2C
			SyncML.SyncML,// 2D
			SyncML.Target,// 2E
			SyncML.TargetRef,// 2F
			SyncML.Reserved,// 30
			SyncML.VerDTD,// 31
			SyncML.VerProto,// 32
			SyncML.NumberOfChanges,// 33
			SyncML.MoreData,// 34
	};

	public static final String[] TAG_TABLE_1 = { MetInf.Anchor,// 05
			MetInf.EMI,// 06
			MetInf.Format,// 07
			MetInf.FreeID,// 08
			MetInf.FreeMem,// 09
			MetInf.Last,// 0A
			MetInf.Mark,// 0B
			MetInf.MaxMsgSize,// 0C
			MetInf.Mem,// 0D
			MetInf.MetInf,// 0E
			MetInf.Next,// 0F
			MetInf.NextNonce,// 10
			MetInf.SharedMem,// 11
			MetInf.Size,// 12
			MetInf.Type,// 13
			MetInf.Version,// 14
			MetInf.MaxObjSize,// 15
	};

	public static final String[] DEV_TAG_TABLE_0 = { DevInf.CTCap,// 05
			DevInf.CTType,// 06
			DevInf.DataStore,// 07
			DevInf.DataType,// 08
			DevInf.DevID,// 09
			DevInf.DevInf,// 0A
			DevInf.DevTyp,// 0B
			DevInf.DisplayName,// 0C
			DevInf.DSMem,// 0D
			DevInf.Ext,// 0E
			DevInf.FwV,// 0F
			DevInf.HwV,// 10
			DevInf.Man,// 11
			DevInf.MaxGUIDSize,// 12
			DevInf.MaxID,// 13
			DevInf.MaxMem,// 14
			DevInf.Mod,// 15
			DevInf.OEM,// 16
			DevInf.ParamName,// 17
			DevInf.PropName,// 18
			DevInf.Rx,// 19
			DevInf.Rx_Pref,// 1A
			DevInf.SharedMem,// 1B
			DevInf.Size,// 1C
			DevInf.SourceRef,// 1D
			DevInf.SwV,// 1E
			DevInf.SyncCap,// 1F
			DevInf.SyncType,// 20
			DevInf.Tx,// 21
			DevInf.Tx_Pref,// 22
			DevInf.ValEnum,// 23
			DevInf.VerCT,// 24
			DevInf.VerDTD,// 25
			DevInf.Xnam,// 26
			DevInf.Xval,// 27
			DevInf.UTC,// 28
			DevInf.SupportNumberOfChanges,// 29
			DevInf.SupportLargeObjs,// 2A
	};

	static public class SyncML {
		public static final String Add = "Add";// 05
		public static final String Alert = "Alert";// 06
		public static final String Archive = "Archive";// 07
		public static final String Atomic = "Atomic";// 08
		public static final String Chal = "Chal";// 09
		public static final String Cmd = "Cmd";// 0A
		public static final String CmdID = "CmdID";// 0B
		public static final String CmdRef = "CmdRef";// 0C
		public static final String Copy = "Copy";// 0D
		public static final String Cred = "Cred";// 0E
		public static final String Data = "Data";// 0F
		public static final String Delete = "Delete";// 10
		public static final String Exec = "Exec";// 11
		public static final String Final = "Final";// 12
		public static final String Get = "Get";// 13
		public static final String Item = "Item";// 14
		public static final String Lang = "Lang";// 15
		public static final String LocName = "LocName";// 16
		public static final String LocURI = "LocURI";// 17
		public static final String Map = "Map";// 18
		public static final String MapItem = "MapItem";// 19
		public static final String Meta = "Meta";// 1A
		public static final String MsgID = "MsgID";// 1B
		public static final String MsgRef = "MsgRef";// 1C
		public static final String NoResp = "NoResp";// 1D
		public static final String NoResults = "NoResults";// 1E
		public static final String Put = "Put";// 1F
		public static final String Replace = "Replace";// 20
		public static final String RespURI = "RespURI";// 21
		public static final String Results = "Results";// 22
		public static final String Search = "Search";// 23
		public static final String Sequence = "Sequence";// 24
		public static final String SessionID = "SessionID";// 25
		public static final String SftDel = "SftDel";// 26
		public static final String Source = "Source";// 27
		public static final String SourceRef = "SourceRef";// 28
		public static final String Status = "Status";// 29
		public static final String Sync = "Sync";// 2A
		public static final String SyncBody = "SyncBody";// 2B
		public static final String SyncHdr = "SyncHdr";// 2C
		public static final String SyncML = "SyncML";// 2D
		public static final String Target = "Target";// 2E
		public static final String TargetRef = "TargetRef";// 2F
		public static final String Reserved = "Reserved";// 30
		public static final String VerDTD = "VerDTD";// 31
		public static final String VerProto = "VerProto";// 32
		public static final String NumberOfChanges = "NumberOfChanges";// 33
		public static final String MoreData = "MoreData";// 34
	}

	static public class MetInf {
		public static final String Anchor = "Anchor";// 05
		public static final String EMI = "EMI";// 06
		public static final String Format = "Format";// 07
		public static final String FreeID = "FreeID";// 08
		public static final String FreeMem = "FreeMem";// 09
		public static final String Last = "Last";// 0A
		public static final String Mark = "Mark";// 0B
		public static final String MaxMsgSize = "MaxMsgSize";// 0C
		public static final String Mem = "Mem";// 0D
		public static final String MetInf = "MetInf";// 0E
		public static final String Next = "Next";// 0F
		public static final String NextNonce = "NextNonce";// 10
		public static final String SharedMem = "SharedMem";// 11
		public static final String Size = "Size";// 12
		public static final String Type = "Type";// 13
		public static final String Version = "Version";// 14
		public static final String MaxObjSize = "MaxObjSize";// 15
	}

	static public class DevInf {
		public static final String CTCap = "CTCap";// 05
		public static final String CTType = "CTType";// 06
		public static final String DataStore = "DataStore";// 07
		public static final String DataType = "DataType";// 08
		public static final String DevID = "DevID";// 09
		public static final String DevInf = "DevInf";// 0A
		public static final String DevTyp = "DevTyp";// 0B
		public static final String DisplayName = "DisplayName";// 0C
		public static final String DSMem = "DSMem";// 0D
		public static final String Ext = "Ext";// 0E
		public static final String FwV = "FwV";// 0F
		public static final String HwV = "HwV";// 10
		public static final String Man = "Man";// 11
		public static final String MaxGUIDSize = "MaxGUIDSize";// 12
		public static final String MaxID = "MaxID";// 13
		public static final String MaxMem = "MaxMem";// 14
		public static final String Mod = "Mod";// 15
		public static final String OEM = "OEM";// 16
		public static final String ParamName = "ParamName";// 17
		public static final String PropName = "PropName";// 18
		public static final String Rx = "Rx";// 19
		public static final String Rx_Pref = "Rx-Pref";// 1A
		public static final String SharedMem = "SharedMem";// 1B
		public static final String Size = "Size";// 1C
		public static final String SourceRef = "SourceRef";// 1D
		public static final String SwV = "SwV";// 1E
		public static final String SyncCap = "SyncCap";// 1F
		public static final String SyncType = "SyncType";// 20
		public static final String Tx = "Tx";// 21
		public static final String Tx_Pref = "Tx-Pref";// 22
		public static final String ValEnum = "ValEnum";// 23
		public static final String VerCT = "VerCT";// 24
		public static final String VerDTD = "VerDTD";// 25
		public static final String Xnam = "Xnam";// 26
		public static final String Xval = "Xval";// 27
		public static final String UTC = "UTC";// 28
		public static final String SupportNumberOfChanges = "SupportNumberOfChanges";// 29
		public static final String SupportLargeObjs = "SupportLargeObjs";// 2
		// A
	}

}
