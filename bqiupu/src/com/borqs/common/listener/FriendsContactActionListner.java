package com.borqs.common.listener;




public interface FriendsContactActionListner{
	public void addFriends(long uid);
	public void inviteFriends(long contactid, String display_name);
    public void editFriendsCircle(long uid,String circleids);
}
