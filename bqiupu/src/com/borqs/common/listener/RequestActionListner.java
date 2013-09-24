package com.borqs.common.listener;

import twitter4j.Requests;




public interface RequestActionListner{
	public void acceptRequest(Requests request, int type);
	public void refuseRequest(Requests request);
}
