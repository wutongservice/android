package com.borqs.common.listener;

import twitter4j.ComposeShareData;

public interface RefreshComposeItemActionListener {
    public void refreshComposeItemUI(ComposeShareData data);
}
