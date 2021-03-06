package twitter4j;

public class NotificationInfo {
    
	public static final String NTF_ACCEPT_SUGGEST = "ntf.accept_suggest"; 
	public static final String NTF_MY_APP_COMMENT = "ntf.my_app_comment"; 
	public static final String NTF_MY_APP_LIKE = "ntf.my_app_like";  
	public static final String NTF_NEW_FOLLOWER = "ntf.new_follower";
	public static final String NTF_PROFILE_UPDATE = "ntf.profile_update";
	public static final String NTF_APP_SHARE = "ntf.app_share";
	public static final String NTF_OTHER_SHARE = "ntf.other_share";
	public static final String NTF_MY_STREAM_COMMENT = "ntf.my_stream_comment";
	public static final String NTF_MY_STREAM_LIKE = "ntf.my_stream_like";
	public static final String NTF_MY_STREAM_RETWEET = "ntf.my_stream_retweet";
	public static final String NTF_SUGGEST_USER = "ntf.suggest_user";
	
	public static final String NTF_PHOTO_COMMENT = "ntf.photo_comment"; 
    public static final String NTF_PHOTO_LIKE = "ntf.photo_like"; 
    public static final String NTF_PHOTO_SHARE = "ntf.photo_share";
    public static final String NTF_PEOPLE_YOU_MAY_KNOW = "ntf.people_you_may_know";
    public static final String NTF_NEW_REQUEST = "ntf.new_request";
    public static final String NTF_REQUEST_ATTENTION = "ntf.request_attention";
    public static final String NTF_CREATE_ACCOUNT = "ntf.create_account"; 
    public static final String NTF_REPORT_ABUSE = "ntf.report_abuse"; 
	
	//for group
	public static final String NTF_GROUP_INVITE = "ntf.group_invite"; 
	public static final String NTF_GROUP_APPLY = "ntf.group_apply";
	
	public static final String NTF_START_NTF = "ntf";
	
	//unused type 
	public static final String NTF_QIUPU_UPDATE = "ntf.qiupu_update";
	public static final String NTF_APP_UPDATE = "ntf.app_update";
	public static final String NTF_NEW_AREA = "ntf.new_area"; 
	public static final String NTF_NEW_APP = "ntf.new_app";
	public static final String NTF_APP_DAREN = "ntf.app_daren";
	public static final String NTF_MY_APP_RETWEET = "ntf.my_app_retweet"; 
	public static final String NTF_INVOLVED_STREAM_COMMENT = "ntf.involved_stream_comment";
	public static final String NTF_INVOLVED_STREAM_LIKE = "ntf.involved_stream_like"; 
	public static final String NTF_INVOLVED_APP_COMMENT = "ntf.involved_app_comment";
	public static final String NTF_INVOLVED_APP_LIKE = "ntf.involved_app_like";
	public static final String NTF_FRIENDS_ONLINE = "ntf.friends_online";
	public static final String NTF_NEW_MESSAGE = "ntf.new_message"; 
	public static final String NTF_BIND_SEND = "ntf.bind_send";
	public static final String NTF_REQ_PROFILE_ACCESS = "ntf.req_profile_access";
	public static final String SOCIALCONTACT_AUTO_ADD = "socialcontact.autoaddfriend";
	public static final String EMAIL_APK_COMMENT = "email.apk_comment"; 
	public static final String EMAIL_APK_LIKE = "email.apk_like"; 
	public static final String EMAIL_STREAM_COMMENT = "email.stream_comment"; 
	public static final String EMAIL_STREAM_LIKE = "email.stream_like"; 
	public static final String EMAIL_ESSENTIAL = "email.essential"; 
	public static final String EMAIL_SHARE_TO = "email.share_to";
	
	public String ntftype;
    public String ntfvalue;
    
    
    public final static int TAB_NTF_MEMTIONME = 0;
    public final static int TAB_NTF_STREAM = 1;
    public static final String getNtfMentionMeTypes() {
        StringBuilder builder = new StringBuilder();
        builder.append("'").append(NTF_ACCEPT_SUGGEST).append("'").append(",")
               .append("'").append(NTF_MY_APP_COMMENT).append("'").append(",")
               .append("'").append(NTF_MY_APP_LIKE).append("'").append(",")
               .append("'").append(NTF_NEW_FOLLOWER).append("'").append(",")
               .append("'").append(NTF_PROFILE_UPDATE).append("'").append(",")
               .append("'").append(NTF_APP_SHARE).append("'").append(",")
               .append("'").append(NTF_OTHER_SHARE).append("'").append(",")
               .append("'").append(NTF_SUGGEST_USER).append("'").append(",")
               .append("'").append(NTF_GROUP_INVITE).append("'").append(",")
               .append("'").append(NTF_GROUP_APPLY).append("'");
        return builder.toString();
    }
    
    public static final String getNtfStreamTypes() {
        StringBuilder builder = new StringBuilder();
        builder.append("'").append(NTF_MY_STREAM_COMMENT).append("'").append(",")
               .append("'").append(NTF_MY_STREAM_LIKE).append("'").append(",")
               .append("'").append(NTF_PHOTO_COMMENT).append("'").append(",")
               .append("'").append(NTF_PHOTO_LIKE).append("'");
        return builder.toString();
    }
    
}

