package com.borqs.common.listener;

public interface TargetLikeActionListener {
    public void onTargetLikeCreated(String targetId, String targetType);
    public void onTargetLikeRemoved(String targetId, String targetType);
}
