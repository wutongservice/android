package com.borqs.notification;


public class Contacter {

	protected String mBorqsId;
	protected String mNickName;
	protected String mPresence;
	protected String mPhoneNumber;
	protected String mEmail;

	public Contacter() {
		
	}
	
	public Contacter(String id, String nick) {
	    mBorqsId = id;
		   mNickName = nick;
	}

	public String getNickName() {
		return mNickName;
	}

	public String getUserName() {
		return mBorqsId;
	}

	public String getPresence() {
		return mPresence;
	}
}
