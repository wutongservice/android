package com.borqs.common.listener;

public interface TargetTopActionListener {
    public void onTargetTopCreated(String group_id, String stream_id);

    public void onTargetTopCancel(String group_id, String stream_id);
}
