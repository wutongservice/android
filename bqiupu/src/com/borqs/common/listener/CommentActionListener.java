package com.borqs.common.listener;

import twitter4j.Stream;

public interface CommentActionListener {
    public void commentItemListener(final Stream.Comments.Stream_Post comment);
}
