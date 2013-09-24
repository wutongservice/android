package com.borqs.common.listener;

import twitter4j.Stream;
import twitter4j.Stream.Comments.Stream_Post;

import java.util.ArrayList;


public interface StreamActionListener {
    public void updatePhotoStreamUI(final Stream stream);
	public void updateStreamRemovedUI(String postid, long uid);
	public void updateStreamCommentUI(final Stream stream, final int commentType, int commentCount, ArrayList<Stream_Post> streamComment);
	public void updateStreamCommentStatus(String postid, boolean canComment, boolean canLike, boolean canReshare);
}
