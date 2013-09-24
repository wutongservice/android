package com.borqs.qiupu;

import twitter4j.TwitterException;

public interface AccountListener {
    public void onLogin();
    public void onLogout();
	public void onCancelLogin();	
	public void filterInvalidException(TwitterException ne);
}
