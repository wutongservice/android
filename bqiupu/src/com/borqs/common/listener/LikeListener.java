package com.borqs.common.listener;

public interface LikeListener {
    public boolean likePost(String post_id, String apk_id);
    public boolean unLikePost(String post_id, String apk_id);
    public boolean retweet(String post_id, String tos, String addedContent, boolean canComment,
                           boolean canLike, boolean canShare, boolean privacy);
}
