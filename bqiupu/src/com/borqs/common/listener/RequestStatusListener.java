package com.borqs.common.listener;

import java.util.HashMap;

public interface RequestStatusListener {
    public void updateUserInfo(HashMap<String, String> infoMap);

    public void setCircle(final long uid, final String circleid,
            final String circleName);
}
