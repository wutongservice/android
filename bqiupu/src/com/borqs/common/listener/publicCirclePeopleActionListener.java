package com.borqs.common.listener;



public interface publicCirclePeopleActionListener {
	public void deleteMember(String userIds);
	public void approveMember(String userIds);
	public void grantMember(String userIds, int role, String display_name);
	public void ignoreMembar(String userIds);
}
