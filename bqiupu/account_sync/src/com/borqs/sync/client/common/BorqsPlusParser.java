/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;

import com.borqs.common.util.BLog;
import com.borqs.sync.client.common.BorqsPlusStruct.PlusEntry;

/**
 * created for borqs_plus.xml parser
 * 
 * @author b211
 */
public class BorqsPlusParser {

    public static final String TAG_BORQS_PLUS = "borqs_plus";

    public static final String TAG_WUTONG_PLUS = "wutong_plus";

    public static final String TAG_OPENFACE_PLUS = "openface_plus";

    public static final String TAG_PLUS_PACKAGE = "package";

    public static final String TAG_PLUS_MIME_TYPE = "mimetype";

    public static final String TAG_PLUS_ACTIVITY_MIME_TYPE = "activity_mimetype";

    public static final String TAG_PLUS_ENTRIES = "entries";

    public static final String TAG_PLUS_ENTRIES_ZH = "zh";

    public static final String TAG_PLUS_ENTRIES_EN = "en";

    public static final String TAG_PLUS_ENTRIES_ENTRY = "entry";

    public static final String TAG_PLUS_ENTRIES_ENTRY_LABEL = "label";

    public static final String TAG_PLUS_ENTRIES_ENTRY_ACTION = "action";

    /**
     * parse the borqs plus by inputstream
     * 
     * @param inStream
     * @return the BorqsPlusStruct
     */
    public static List<BorqsPlusStruct> parseBorqsPlus(InputStream inStream) throws Exception {
        List<BorqsPlusStruct> bpses = new ArrayList<BorqsPlusStruct>();
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inStream, null);

            while (true) {
                int type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals(TAG_BORQS_PLUS)) {
                    break;
                } else if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (TAG_WUTONG_PLUS.equals(name)) {
                        bpses.add(parsePlusDetail(parser, name));
                    } else if (TAG_OPENFACE_PLUS.equals(name)) {
                        bpses.add(parsePlusDetail(parser, name));
                    }
                } else if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new IllegalStateException("xml parse error!," + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("xml parse IO error!," + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("parseBorqsPlus exception,!," + e.getMessage());
        }
        return bpses;

    }

    private static BorqsPlusStruct parsePlusDetail(XmlPullParser parser, String tag)
            throws Exception {
        BorqsPlusStruct bps = new BorqsPlusStruct();
        while (true) {
            try {
                int type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals(tag)) {
                    break;
                } else if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (TAG_PLUS_PACKAGE.equals(name)) {
                        String packageName = parser.nextText().trim();
                        if (TextUtils.isEmpty(packageName)) {
                            throw new IllegalStateException("packageName is null,error!");
                        }
                        bps.setPackageName(packageName);
                    } else if (TAG_PLUS_MIME_TYPE.equals(name)) {
                        String mimeType = parser.nextText().trim();
                        if (TextUtils.isEmpty(mimeType)) {
                            throw new IllegalStateException("mimeType is null,error!");
                        }
                        bps.setMimeType(mimeType);
                    } else if (TAG_PLUS_ACTIVITY_MIME_TYPE.equals(name)) {
                        String activityMimeType = parser.nextText().trim();
                        if (TextUtils.isEmpty(activityMimeType)) {
                            throw new IllegalStateException("activityMimeType is null,error!");
                        }
                        bps.setActivityMimeType(activityMimeType);
                    } else if (TAG_PLUS_ENTRIES.equals(name)) {
                        parseEntries(parser, bps);
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse error!," + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse IO error!," + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("parsePlusDetail exception,!," + e.getMessage());
            }
        }
        return bps;
    }

    private static void parseEntries(XmlPullParser parser, BorqsPlusStruct bps) throws Exception {
        while (true) {
            try {
                int type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals(TAG_PLUS_ENTRIES)) {
                    break;
                } else if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (TAG_PLUS_ENTRIES_ZH.equals(name)) {
                        bps.addEntry(BorqsPlusStruct.ENTRY_TYPE_ZH, parserEntry(parser, name, bps));
                    } else if (TAG_PLUS_ENTRIES_EN.equals(name)) {
                        bps.addEntry(BorqsPlusStruct.ENTRY_TYPE_EN, parserEntry(parser, name, bps));
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse error!," + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse IO error!," + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("parsePlusDetail exception,!," + e.getMessage());
            }
        }
    }

    private static List<PlusEntry> parserEntry(XmlPullParser parser, String tag, BorqsPlusStruct bps)
            throws Exception {
        List<PlusEntry> entries = new ArrayList<BorqsPlusStruct.PlusEntry>();
        while (true) {
            try {
                int type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals(tag)) {
                    break;
                } else if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (TAG_PLUS_ENTRIES_ENTRY.equals(name)) {
                        PlusEntry entry = parserEntryDetail(parser, bps);
                        entries.add(entry);
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse error!," + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse IO error!," + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("parsePlusDetail exception,!," + e.getMessage());
            }
        }
        BLog.d("entryies:" + entries.toString());
        return entries;
    }

    private static PlusEntry parserEntryDetail(XmlPullParser parser, BorqsPlusStruct bps)
            throws Exception {
        String label = null;
        String action = null;
        while (true) {
            try {
                int type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("entry")) {
                    break;
                } else if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (TAG_PLUS_ENTRIES_ENTRY_LABEL.equals(name)) {
                        label = parser.nextText().trim();
                        if (TextUtils.isEmpty(label)) {
                            throw new IllegalStateException("label is null,error!");
                        }
                    } else if (TAG_PLUS_ENTRIES_ENTRY_ACTION.equals(name)) {
                        action = parser.nextText().trim();
                        if (TextUtils.isEmpty(action)) {
                            throw new IllegalStateException("action is null,error!");
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse error!," + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("xml parse IO error!," + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("parsePlusDetail exception,!," + e.getMessage());
            }
        }
        return bps.new PlusEntry(label, action);
    }
}
