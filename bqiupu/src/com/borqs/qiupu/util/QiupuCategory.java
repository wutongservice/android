package com.borqs.qiupu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.borqs.qiupu.R;

public class QiupuCategory {

    private static HashMap<String, Long> appmap = new HashMap<String, Long>();
    private static HashMap<String, Long> gamemap = new HashMap<String, Long>();
    private static HashMap<String, Long> topicmap = new HashMap<String, Long>();
    private static HashMap<String, Long> mastermap = new HashMap<String, Long>();
    private static HashMap<Long, String> appvaluekey = new HashMap<Long, String>();
    private static HashMap<Long, String> gamevaluekey = new HashMap<Long, String>();
    private static HashMap<Long, String> topicvaluekey = new HashMap<Long, String>();
    private static HashMap<Long, String> mastervaluekey = new HashMap<Long, String>();
    private static ArrayList<String> apparr = new ArrayList<String>();
    private static ArrayList<String> gamearr = new ArrayList<String>();
    private static ArrayList<String> topicarr = new ArrayList<String>();
    private static ArrayList<String> masterarr = new ArrayList<String>();

    private static HashMap<String, Long> allmap = new HashMap<String, Long>();

    public static long APPTYPE_APPLICATION = 0x100;
    public static long APPTYPE_GAME = 0x200;
    private static boolean mIsInitial = false;
    private static QiupuCategory mQiupuCategory;

    private QiupuCategory(Context context) {
        init(context);
    }

    public static QiupuCategory getInstance(Context context) {
        if (mQiupuCategory == null) {
            mQiupuCategory = new QiupuCategory(context);
        }
        return mQiupuCategory;
    }

    private void init(Context context) {
        mIsInitial = true;
        String[] apps = context.getResources().getStringArray(R.array.apps_category);
        String[] games = context.getResources().getStringArray(R.array.games_category);
        String[] special = context.getResources().getStringArray(R.array.special_category);
        String[] daren = context.getResources().getStringArray(R.array.daren_category);
        int[] appsId = context.getResources().getIntArray(R.array.apps_id);
        int[] gamesId = context.getResources().getIntArray(R.array.games_id);
        int[] specialId = context.getResources().getIntArray(R.array.special_id);
        int[] darenId = context.getResources().getIntArray(R.array.daren_id);

        for (int i = 0, len = apps.length; i < len; i++) {
            appmap.put(apps[i], Long.valueOf(appsId[i]));
        }

        for (int i = 0, len = games.length; i < len ; i++) {
            gamemap.put(games[i], Long.valueOf(gamesId[i]));
        }

        for (int i = 0, len = special.length; i < len; i++) {
            topicmap.put(special[i], Long.valueOf(specialId[i]));
        }

        for (int i = 0, len = daren.length; i < len; i++) {
            mastermap.put(daren[i], Long.valueOf(darenId[i]));
        }

        allmap.putAll(appmap);
        allmap.putAll(gamemap);

        // app value-key
        Iterator iter = appmap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            Long value = (Long)entry.getValue();
            appvaluekey.put(value, key);
            apparr.add(key);
        }

        // game value-key
        iter = gamemap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            Long value = (Long)entry.getValue();
            gamevaluekey.put(value, key);
            gamearr.add(key);
        }

        // topic value-key
        iter = topicmap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            Long value = (Long)entry.getValue();
            topicvaluekey.put(value, key);
            topicarr.add(key);
        }

        // master value-key
        iter = mastermap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            Long value = (Long)entry.getValue();
            mastervaluekey.put(value, key);
            masterarr.add(key);
        }
    }

    public static long getAllCategoryidByAppName(String category) {
        return allmap.get(category);
    }

    public static long getCategoryidByAppName(String category) {
        return appmap.get(category);
    }

    public static long getCategoryidByGameName(String category) {
        return gamemap.get(category);
    }

    public static long getCategoryidBytopicName(String category) {
        return topicmap.get(category);
    }

    public static long getCategoryidByMasterName(String category) {
        return mastermap.get(category);
    }

    public static String getAppNameByCategoryid(long id) {
        return appvaluekey.get(id);
    }

    public static String getGameNameByCategoryid(long id) {
        return gamevaluekey.get(id);
    }

    public static String getTopicNameByCategoryid(long id) {
        return topicvaluekey.get(id);
    }

    public static String getMasterNameByCategoryid(long id) {
        return mastervaluekey.get(id);
    }

    public static ArrayList<String> getAppArray() {
        return apparr;
    }

    public static ArrayList<String> getGameArray() {
        return gamearr;
    }

    public static ArrayList<String> getTopicArray() {
        return topicarr;
    }

    public static ArrayList<String> getMasterArray() {
        return masterarr;
    }
}
