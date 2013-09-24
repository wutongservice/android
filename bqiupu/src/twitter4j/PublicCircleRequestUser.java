package twitter4j;

import java.util.ArrayList;
import java.util.Comparator;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import com.borqs.qiupu.db.QiupuORM;

public class PublicCircleRequestUser extends QiupuUser{

    private static final long serialVersionUID = 1L;

    public long group_id;
    public ArrayList<QiupuUser> source;
    public int status;
    public int role_in_group;
    public String identify;
    
    public static final int STATUS_IN_CIRCLE = 3;
    public static final int STATUS_INVITE = 2;
    public static final int STATUS_APPLY = 1;
    
    public static final int ROLE_TYPE_MEMEBER = 1;
    public static final int ROLE_TYPE_MANAGER = 10;
    public static final int ROLE_TYPE_CREATER = 100;
    
    public PublicCircleRequestUser(JSONObject obj) {
        parsePublicCircleRequestUser(obj);
    }
    public PublicCircleRequestUser() {
    }
    
    public final void parsePublicCircleRequestUser(JSONObject obj) {
        group_id = obj.optLong("group_id");
        uid = obj.optLong("user_id");
        nick_name = obj.optString("display_name");
        name_pinyin = QiupuORM.getPinyin(nick_name).toLowerCase();
        status = obj.optInt("status");
        identify = obj.optString("identify");
        profile_image_url = obj.optString("image_url");
        role_in_group = obj.optInt("role_in_group");
        remark = obj.optString("remark");
        profile_privacy = obj.optBoolean("profile_privacy");
        QiupuNewUserJSONImpl.parsePerHapsName(obj, perhapsNames);
        
        JSONArray sourceObj = obj.optJSONArray("sources");
        if(sourceObj != null) {
            source = new ArrayList<QiupuUser>();
            for(int i=0; i<sourceObj.length(); i++) {
                JSONObject tmpObj = sourceObj.optJSONObject(i);
                if(tmpObj != null) {
                    QiupuUser tmpSource = new QiupuUser();
                    tmpSource.uid = tmpObj.optLong("user_id");
                    tmpSource.nick_name = tmpObj.optString("display_name");
                    tmpSource.profile_image_url = tmpObj.optString("image_url");
                    tmpSource.remark = tmpObj.optString("remark");
                    tmpSource.profile_privacy = tmpObj.optBoolean("profile_privacy");
                    QiupuNewUserJSONImpl.parsePerHapsName(tmpObj, tmpSource.perhapsNames);
                    source.add(tmpSource);
                }
            }
        }
    }
    
    public static ArrayList<PublicCircleRequestUser> createRequestPeopleList(HttpResponse response)
            throws TwitterException {
        ArrayList<PublicCircleRequestUser> list = new ArrayList<PublicCircleRequestUser>();
        JSONArray array = null;
        try {
            array = response.asJSONArray();
        } catch (TwitterException e) {
            throw e;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj;
            try {
                obj = array.getJSONObject(i);
            } catch (JSONException e) {
                throw new TwitterException(e);
            }
            list.add(new PublicCircleRequestUser(obj));
        }

        return list;
    }
    
    public static boolean isInGroup(int role) {
        return ROLE_TYPE_MEMEBER == role 
                || ROLE_TYPE_CREATER == role 
                || ROLE_TYPE_MANAGER == role;
    }
    
    public static boolean isMember(int role) {
        return ROLE_TYPE_MEMEBER == role;
    }
    
    public static boolean isManager(int role) {
        return ROLE_TYPE_MANAGER == role;
    }
    
    public static boolean isCreator(int role) {
        return ROLE_TYPE_CREATER == role;
    }
    public static final Comparator<PublicCircleRequestUser> USER_ROLE_COMPARATOR = new Comparator<PublicCircleRequestUser>() {
        public final int compare(PublicCircleRequestUser a, PublicCircleRequestUser b) {
            if (a.role_in_group > b.role_in_group) {
                return -1;
            } else if (a.role_in_group < b.role_in_group) {
                return 1;
            } 
            else  {
                return 0;               
            }
        }
    };
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PublicCircleRequestUser)) {
            return false;
        }
        PublicCircleRequestUser uc = (PublicCircleRequestUser) obj;
        return (uc.uid == uid);
    };
}
