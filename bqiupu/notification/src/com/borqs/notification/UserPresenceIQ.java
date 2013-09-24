package com.borqs.notification;

import org.jivesoftware.smack.packet.IQ;



class UserPresenceIQ extends IQ {

    public static final String IQ_USER_PRESENCE_NAME = "userstatus";
    public static final String IQ_USER_PRESENCE_NAMESPACE = "com:borqs:account";
    public static final String IQ_USER_PRESENCE_URL = "userpresence.account.borqs.com";

    private String mUserName;
	private String mFrom;
    private String mStatus;
    private String mIp;

    UserPresenceIQ() {
        
    }

    UserPresenceIQ(String name, String from) {
		mUserName = name;
		mFrom = from;
	}

	void setStatus(String s) {
	    mStatus = s;
	}

    void setIp(String i) {
        mIp = i;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getIp() {
        return mIp;
    }

    @Override
	public String getChildElementXML() {
        StringBuffer child = new StringBuffer();
        child.append("<" + IQ_USER_PRESENCE_NAME //+">");
                + " xmlns=" + "\"" + IQ_USER_PRESENCE_NAMESPACE + "\">");
        child.append("<item name=\"username\">").append(mUserName).append("</item>");
        child.append("<item name=\"from\">").append(mFrom).append("</item>");
        child.append("</" + IQ_USER_PRESENCE_NAME + ">");
		return child.toString();
	}
	
}