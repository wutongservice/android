package com.borqs.common.listener;

import twitter4j.QiupuUser;



public interface UsersActionListner{
	public void updateItemUI(QiupuUser user);
	public void addFriends(QiupuUser user);
	public void refuseUser(long uid);
	public void deleteUser(QiupuUser user);
	public void sendRequest(QiupuUser user);
}
