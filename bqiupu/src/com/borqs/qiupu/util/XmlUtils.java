package com.borqs.qiupu.util;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class XmlUtils {
	private final static String TAG = "XmlUtils"; 
	public XmlUtils() {
	}

	public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }
    }
    
    public static final XmlPullParser xmlPullParseXML(Context context, String xmlStr) {
    	XmlPullParser parser = Xml.newPullParser();  
    	try {
    		xmlStr = StringUtil.loadResource(context, "apps_list_ui.xml");
        	Log.d(TAG, "xmlStr: "  + xmlStr);
			parser.setInput(new StringReader(xmlStr));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return parser;
    }
}
