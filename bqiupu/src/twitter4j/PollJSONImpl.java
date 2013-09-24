package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class PollJSONImpl extends PollInfo {

    private static final long serialVersionUID = -488483723665317082L;

    final static String       TAG              = "PollJSONImpl";

    public PollJSONImpl(JSONObject obj) throws TwitterException {

    }

    public PollJSONImpl(JSONObject obj, boolean with_items) throws TwitterException {
        try {
            poll_id = obj.optString("id");
            title = obj.optString("title");
            description = obj.optString("description");
            multi = obj.optInt("multi");
            limit = obj.optInt("limit");
            privacy = obj.optInt("privacy");
            created_time = obj.optLong("created_time");
            end_time = obj.optLong("end_time");
            updated_time = obj.optLong("updated_time");
            destroyed_time = obj.optLong("destroyed_time");
            attend_status = obj.optLong("status");
            attend_count = obj.optLong("count");
            left_time = obj.optLong("left");
            viewer_can_vote = obj.optBoolean("viewer_can_vote");
            mode = obj.optInt("mode");
            has_voted = obj.optBoolean("has_voted");
            viewer_left = obj.optInt("viewer_left");
            can_add_items = obj.optInt("can_add_items") == 0 ? false : true;
            JSONObject comment_obj = obj.optJSONObject("comments");
            if (comment_obj != null) {
                comment_count = comment_obj.optInt("count");
                //TODO: parse latest two comments
            }
//            comment_count = obj.optInt("comment_count");

            JSONObject sponsorObj = obj.getJSONObject("source");
            if (sponsorObj != null) {
            	sponsor = new QiupuSimpleUser();
                sponsor.nick_name = sponsorObj.optString("display_name");
                sponsor.profile_image_url = sponsorObj.optString("image_url");
                sponsor.uid = sponsorObj.optLong("user_id");
            }

            /**
             * target, we save target_id.
             */
            JSONArray targetArray = obj.getJSONArray("target");
            StringBuilder targetBuilder = new StringBuilder();
            if (targetArray != null) {
                for (int i = 0; i < targetArray.length(); i++) {
                    if (i > 0) {
                        targetBuilder.append(",");
                    }
                    JSONObject targetObj = targetArray.getJSONObject(i);
                    // save circle id
                    if (targetObj.has("id")) {
                        targetBuilder.append(targetObj.optString("id", ""));
                    }
                    // save user id
                    if (targetObj.has("user_id")) {
                        targetBuilder.append(targetObj.optString("user_id", ""));
                    }
                }
                target_id = targetBuilder.toString();
//                Log.d(TAG, "target_id = " + target_id);
            }

            if (with_items == true) {
                JSONArray array = obj.getJSONArray("items");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject element = array.getJSONObject(i);
                    pollItemList.add(parsePollItemInfo(element));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public PollItemInfo parsePollItemInfo(JSONObject obj) {
        try {
            PollItemInfo itemInfo = new PollItemInfo();
            if (obj != null) {
                itemInfo.item_id = obj.optString("id");
                itemInfo.message = obj.optString("message");
                itemInfo.count = obj.optInt("count");
                itemInfo.viewer_voted = obj.optBoolean("viewer_voted");
                itemInfo.selected = itemInfo.viewer_voted;

                JSONArray array = obj.getJSONArray("participants");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject element = array.getJSONObject(i);
                    itemInfo.userList.add(parseQiupuUser(element.getJSONObject("user")));
                }
            }
            return itemInfo;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public QiupuSimpleUser parseQiupuUser(JSONObject obj) {
        try {
            QiupuSimpleUser user = new QiupuSimpleUser();
            if (obj != null) {
                user.profile_image_url = obj.optString("image_url");
                user.nick_name = obj.optString("display_name");
                user.uid = obj.optLong("user_id");
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<PollInfo> createPollList(HttpResponse response, boolean with_items)
            throws TwitterException {
        ArrayList<PollInfo> list = new ArrayList<PollInfo>();
        JSONArray array = null;
        try {
            array = response.asJSONArray();
        } catch (TwitterException e) {
            throw e;
        }

        Log.d(TAG, "createPollList size:" + array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj;
            try {
                obj = array.getJSONObject(i);
            } catch (JSONException e) {
                throw new TwitterException(e);
            }
            list.add(new PollJSONImpl(obj, with_items));
        }

        return list;
    }

    public static PollInfo createPollInfo(HttpResponse response, boolean with_items)
            throws TwitterException {
        PollInfo pollInfo = new PollInfo();
        JSONObject obj = null;
        try {
            obj = response.asJSONObject();
            pollInfo = new PollJSONImpl(obj, with_items);
        } catch (TwitterException e) {
            throw e;
        }
        return pollInfo;
    }
    
//    public static PollInfo createSimplePollInfo(JSONObject obj) {
//    	PollInfo info = new PollInfo();
//    	info.poll_id = obj.optString("id");
//        info.title = obj.optString("title");    
//        return info;
//    }

}
