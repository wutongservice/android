package twitter4j;

import java.io.Serializable;

public class ChatInfo implements Serializable {
    private static final long serialVersionUID = -8476093514287584623L;

    public long uid;
    public String msg;
    public int type;
    public String profile_url;
    public boolean unread;
    public long created_time;
    public String display_name;

    public ChatInfo() {
        
    }

    public ChatInfo(long uid, String msg, int type, String profile_url, boolean unRead, long created_time, String display_name) {
        this.uid = uid;
        this.msg = msg;
        this.type = type;
        this.profile_url = profile_url;
        this.unread = unRead;
        this.created_time = created_time;
        this.display_name = display_name;
    }

    public String toString() {
        return "uid = " + uid + 
               "\nmsg = " + msg +
               "\ntype = " + type +
               "\nprofile_url = " + profile_url +
               "\nunread = " + unread +
               "\ncreated_time = " + created_time +
               "\ndisplay_name = " + display_name;
    }

}
