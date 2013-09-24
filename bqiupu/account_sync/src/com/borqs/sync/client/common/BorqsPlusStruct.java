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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BorqsPlusStruct {

    // contains zh and en entry(label and action)
    public static final String ENTRY_TYPE_ZH = Locale.CHINESE.toString();

    public static final String ENTRY_TYPE_EN = Locale.ENGLISH.toString();

    public class PlusEntry {

        public String mLabel;

        public String mAction;

        public PlusEntry(String label, String action) {
            mLabel = label;
            mAction = action;
        }
        
        @Override
        public String toString() {
            return "label:" + mLabel + ",action:" + mAction;
        }
    }

    /*
     * the Borqs plus pakcagename ,like com.borqs.qiupu means Wutong
     */
    private String mPackageName;

    /*
     * The mimeType of the Activity who will receive action send by
     * BorqsPlusTransit
     */
    private String mActivityMimeType;
    
    /*
     * the plus's mimeType related the Borqs Account
     */
    private String mMimeType;

    /*
     * both en and zh contain an entry list
     */
    
    private Map<String, List<PlusEntry>> entryMap = new HashMap<String, List<PlusEntry>>();

    public void addEntry(String entryType, List<PlusEntry> plusEntries) {
        entryMap.put(entryType, plusEntries);
    }

    public Map<String, List<PlusEntry>> getEntryList() {
        return entryMap;
    }
    
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getPackageName() {
        return mPackageName;
    }
    
    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setActivityMimeType(String activityMimeType) {
        mActivityMimeType = activityMimeType;
    }

    public String getActivityMimeType() {
        return mActivityMimeType;
    }

}
