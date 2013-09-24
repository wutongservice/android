package com.borqs.common.listener;

import twitter4j.QiupuUser;

public interface FriendsActionListner{
	public void followUser(QiupuUser user);
	public void unFollowerUser(QiupuUser user);
}
