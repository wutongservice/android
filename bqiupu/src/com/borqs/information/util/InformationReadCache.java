package com.borqs.information.util;

import android.util.Log;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.util.StringUtil;

import twitter4j.Stream;
import twitter4j.Stream.Comments.Stream_Post;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-2-4
 * Time: 下午1:29
 * To change this template use File | Settings | File Templates.
 */
public class InformationReadCache {
    private static final String TAG = "InformationReadCache";
    private static final boolean DEBUG = false || QiupuConfig.DBLOGD;

    public static class ReadStreamCache {
        private static Set<Long> mObjectIds;
        private static Map<Long, Set<Long>> mCommentIdsMap;
        private static HashSet<Long> mUnReadNtfIds;
        static {
            mCommentIdsMap = new HashMap<Long, Set<Long>>();
            mObjectIds = new HashSet<Long>();
            mUnReadNtfIds = new HashSet<Long>();
        }

        /***
         * not instance
         */
        private ReadStreamCache() {
        }

        public static void cache(final List<Stream> posts) {
            if (DEBUG) Log.d(TAG, String.format("cache, enter, posts/ids/comment_map size: %d/%d/%d.",
                    posts.size(), mObjectIds.size(), mCommentIdsMap.size()));
            long id;
            List<Stream_Post> commentList;
            for (Stream stream : posts) {
                commentList = (null == stream.comments) ? null : stream.comments.getCommentList();
                id = Long.parseLong(stream.post_id);
                cache(id, commentList);
            }
            if (DEBUG) Log.d(TAG, String.format("cache, exit, posts/ids/comment_map size: %d/%d/%d.",
                    posts.size(), mObjectIds.size(), mCommentIdsMap.size()));
        }

        public static void cache(long id, List<Stream_Post> commentList) {
            if (DEBUG) Log.d(TAG, String.format("cache, enter, comments/ids/comment_map size: %d/%d/%d.",
                    commentList.size(), mObjectIds.size(), mCommentIdsMap.size()));
            long commentId;
            if (null == commentList || commentList.isEmpty()) {
                mObjectIds.add(id);
            } else {
                for (Stream_Post comment : commentList) {
                    commentId = comment.id;
                    cache(id, commentId);
                }
            }
            if (DEBUG) Log.d(TAG, String.format("cache, exit, comments/ids/comment_map size: %d/%d/%d.",
                    commentList.size(), mObjectIds.size(), mCommentIdsMap.size()));
        }

        public static void cache(long id, long commentId) {
            if (DEBUG) Log.d(TAG, String.format("cache, enter, id/commentId/ids/comment_map size: %d/%d/%d/%d.",
                    id, commentId, mObjectIds.size(), mCommentIdsMap.size()));
            mObjectIds.add(id);
            Set<Long> commentIdSet = mCommentIdsMap.get(id);
            if (null == commentIdSet) {
                commentIdSet = new HashSet<Long>();
                mCommentIdsMap.put(id, commentIdSet);
            }
            commentIdSet.add(commentId);
            if (DEBUG) Log.d(TAG, String.format("cache, exit, id/commentId/ids/comment_map size: %d/%d/%d/%d.",
                    id, commentId, mObjectIds.size(), mCommentIdsMap.size()));
        }

        public static boolean hitTest(long id) {
            if (DEBUG) Log.d(TAG, String.format("hitTest, result/id: %s/%d.",
                    mObjectIds.contains(id), id));
            return mObjectIds.contains(id);
        }

        public static boolean hitTest(long id, long commentId) {
            if (DEBUG) Log.d(TAG, String.format("hitTest, enter, id/commentId/ids/comment_map size: %d/%d/%d/%d.",
                    id, commentId, mObjectIds.size(), mCommentIdsMap.size()));
            boolean hit = false;
            if (mCommentIdsMap.containsKey(id)) {
                if (commentId > 0) {
                    hit = mCommentIdsMap.get(id).contains(commentId);
                } else {
                    hit = hitTest(id);
                }
            }
            if (DEBUG) Log.d(TAG, String.format("hitTest, exit, result: id/commentId/result is %d/%d/%s",
                    id, commentId, hit));
            return hit;
        }
        
        public static void cacheUnReadNtfIdsWithoutDb(long id) {
        	mUnReadNtfIds.add(id);
        }
        
        public static String getCacheUnReadNtfIds() {
        	Iterator<Long> it = mUnReadNtfIds.iterator();
            StringBuilder ids = new StringBuilder();

            while (it.hasNext()) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                ids.append(it.next());
            }
            if (DEBUG) Log.d(TAG, "getCacheUnReadNtfIds, ids: " + ids.toString());
            return ids.toString();
        }
        
        public static void removeNtfCacheWithIds(String ids) {
        	if (DEBUG) Log.d(TAG, "removeNtfCacheWithIds, ids: " + ids);
        	if(StringUtil.isValidString(ids)) {
        		String[] idsArray = ids.split(",");
        		for(int i = 0; i<idsArray.length; i++) {
        			mUnReadNtfIds.remove(idsArray[i]);
        		}
        	}
        }
    }
}
