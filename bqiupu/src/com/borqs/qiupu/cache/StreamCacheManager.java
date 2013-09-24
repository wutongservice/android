package com.borqs.qiupu.cache;

import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import twitter4j.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-12-3
 * Time: 下午3:21
 * To change this template use File | Settings | File Templates.
 */
public class StreamCacheManager {
    private static final String TAG = "StreamCacheManager";

    private static final boolean DISABLE_CACHE = true;

    private static final int MAX_STREAM_ITEM_COUNT = 200;
    /// cached stream item within a LRU cache.
    private static LruCache<String, Stream> streamItemCache = new LruCache<String, Stream>(MAX_STREAM_ITEM_COUNT);
    /// map cached file name to stream id list that was sorted by created time.
    private static HashMap<String, ArrayList<String>> streamCacheMap = new HashMap<String, ArrayList<String>>();

    public static ArrayList<String> getCache(String key) {
        if (DISABLE_CACHE || TextUtils.isEmpty(key)) {
            return null;
        }

        return streamCacheMap.get(key);
    }

    public static void addCache(String key, List<Stream> values) {
        if (DISABLE_CACHE || TextUtils.isEmpty(key) || null == values || values.isEmpty()) {
            return;
        }

        if (streamCacheMap.containsKey(key)) {
            Log.d(TAG, "addCache, merge with existing cache.key="+key);
            ArrayList<String> cachedList = streamCacheMap.get(key);
            cachedList.clear();
            for (Stream stream : values) {
                cachedList.add(stream.post_id);
                streamItemCache.put(stream.post_id, stream);
            }
        } else {
        	Log.d(TAG, "addCache,key="+key);
            ArrayList<String> cachedList = new ArrayList<String>();
            for (Stream stream : values) {
                cachedList.add(stream.post_id);
                streamItemCache.put(stream.post_id, stream);
            }
            streamCacheMap.put(key, cachedList);
        }
    }

    public static Stream getCacheItem(Long id) {
        return getCacheItem(String.valueOf(id));
    }

    public static Stream getCacheItem(String id) {
        if (DISABLE_CACHE || TextUtils.isEmpty(id)) {
            return null;
        }

        return streamItemCache.get(id);
    }

    public static void addCacheItem(Stream stream) {
        if (DISABLE_CACHE || null == stream) {
            return;
        }

        streamItemCache.put(stream.post_id, stream);
    }

    public static void updateItemComments(Stream stream, List<Stream.Comments.Stream_Post> comments) {
        if (DISABLE_CACHE || null == stream) {
            return;
        }

        Stream post = streamItemCache.get(stream.post_id);
        if (null == post || stream.updated_time > post.updated_time) {
            stream.comments.alterCommentList(comments, stream.comments.getCount());
            addCacheItem(stream);
        } else if (null != post) {
            if (null != post.comments) {
                post.comments.alterCommentList(comments, post.comments.getCount());
            }
        }
    }

    public static ArrayList<Stream> getCacheStreamList(String key) {
        if (DISABLE_CACHE) {
            return null;
        }

        ArrayList<String> idList = getCache(key);
        if (null == idList) {
            return null;
        }

        Log.d(TAG, "getCacheStreamList is not null,key=" + key);
        Stream post;
        ArrayList<Stream> cachedList = new ArrayList<Stream>(idList.size());
        for (String id : idList) {
            post = getCacheItem(id);
            if (null == post) {
                // TODO: load the cache missed item rather than simply fail.
                Log.d(TAG, "getCacheStreamList, missing item id:" + id);
                break;
            }
            cachedList.add(post);
        }

        return cachedList;
    }
}
