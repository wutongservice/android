package com.borqs.common.listener;

import twitter4j.UserCircle;

public interface CircleActionListner{
	public void deleteCircle(UserCircle circle);
	public void changeSelect(long circleid, boolean selected, boolean isUseritem);
}
