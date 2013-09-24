package com.borqs.common.listener;

import twitter4j.ComposeShareData;

public interface ComposeActionListener {
    public void deleteItem(ComposeShareData shareData);
    public void retryItem(ComposeShareData shareData);
}
