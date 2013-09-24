package com.borqs.qiupu;

import twitter4j.TwitterException;

public interface AccountServiceConnectListener {
    public void onAccountServiceDisconnected();
    public void onAccountServiceConnected();
}
