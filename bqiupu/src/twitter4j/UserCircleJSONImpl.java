package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class UserCircleJSONImpl extends UserCircle {
	private static final long serialVersionUID = 6662628759814282916L;

	final static String TAG = "UserCircleJSONImpl";

	public UserCircleJSONImpl(JSONObject obj) throws TwitterException {
	    circleid = obj.optLong("circle_id", -1);
	    name = obj.optString("circle_name", "");
	    memberCount = obj.optInt("member_count", 0);
	    type = obj.optInt("type", 0);

	    if(UserCircle.CIRLCE_TYPE_PUBLIC == type) {
	    	parseGroup(obj);
	    }
	}
	
	public UserCircleJSONImpl(JSONObject obj, int circleType) throws TwitterException {
	    circleid = obj.optLong("circle_id", -1);
	    name = obj.optString("circle_name", "");
	    memberCount = obj.optInt("member_count", 0);
	    invitedMembersCount = obj.optInt("invited_count", 0);
	    applyedMembersCount = obj.optInt("applied_count" , 0);
	    if(UserCircle.CIRLCE_TYPE_PUBLIC == circleType || UserCircle.CIRCLE_TYPE_EVENT == circleType) {
	        uid = circleid;  //TODO used to parse circle contactInfo
	    	type = circleType;
	    	parseMembers(obj);
	    	parseCircles(obj);
	    	parseGroup(obj);
	    	parseEventPoll(obj);
	    	parseCategories(obj);
	    }
	}
	
	private void parseCategories(JSONObject obj) {
		JSONArray categoryObj = obj.optJSONArray("categories");
		if(categoryObj != null && categoryObj.length() > 0) {
			categories = new ArrayList<InfoCategory>();
			for(int i=0; i<categoryObj.length(); i++) {
				JSONObject tmpobj = categoryObj.optJSONObject(i);
				if(tmpobj != null) {
					categories.add(parseCategory(tmpobj));
				}
			}
		}
	}
	
	private InfoCategory parseCategory(JSONObject obj) {
		InfoCategory ic = new InfoCategory();
		ic.categoryId = obj.optLong("category_id");
		ic.categoryName = obj.optString("category");
		ic.creatorId = obj.optLong("user_id");
		ic.scopeId = obj.optLong("scope");
		return ic;
	}

	private void parseMembers(JSONObject obj) throws TwitterException {
	    JSONArray inMembers = obj.optJSONArray("profile_members");
	    if(inMembers != null && inMembers.length() > 0) {
	        inMembersImageList = new ArrayList<UserImage>();
	        for(int i=0;i<inMembers.length();i++) {
                JSONObject memberobj = inMembers.optJSONObject(i);
                if(memberobj != null) {
                    inMembersImageList.add(QiupuNewUserJSONImpl.createUserImageItemResponse(memberobj, circleid));
                }
            }
	    }
	    
	    JSONArray applyedMembers = obj.optJSONArray("profile_applied");
        if(applyedMembers != null && applyedMembers.length() > 0) {
            applyedMembersList = new ArrayList<UserImage>();
            for(int i=0;i<applyedMembers.length();i++) {
                JSONObject memberobj = applyedMembers.optJSONObject(i);
                if(memberobj != null) {
                    applyedMembersList.add(QiupuNewUserJSONImpl.createUserImageItemResponse(memberobj, circleid));
                }
            }
        }
        
        JSONArray invitedMembers = obj.optJSONArray("profile_invited");
        if(invitedMembers != null && invitedMembers.length() > 0) {
            invitedMembersList = new ArrayList<UserImage>();
            for(int i=0;i<invitedMembers.length();i++) {
                JSONObject memberobj = invitedMembers.optJSONObject(i);
                if(memberobj != null) {
                    invitedMembersList.add(QiupuNewUserJSONImpl.createUserImageItemResponse(memberobj, circleid));
                }
            }
        }
	}
	
	private void parseCircles(JSONObject obj) throws TwitterException {
//	    JSONArray childcircles = obj.optJSONArray("circles");
//	    if(childcircles != null && childcircles.length() > 0) {
//	    	childCirclesList = new ArrayList<UserImage>();
//	        for(int i=0;i<childcircles.length();i++) {
//                JSONObject childobj = childcircles.optJSONObject(i);
//                if(childobj != null) {
//                	childCirclesList.add(createChildCirclesItemResponse(childobj));
//                }
//            }
//	    }
	    
	    JSONObject freeCirclesObj = obj.optJSONObject("free_circles");
	    if(freeCirclesObj != null) {
	    	freeCirclesCount = freeCirclesObj.optInt("count");
	    	JSONArray freeCircles = freeCirclesObj.optJSONArray("circles");
	    	if(freeCircles != null && freeCircles.length() > 0) {
	    		freeCirclesList = new ArrayList<UserImage>();
	    		for(int i=0;i<freeCircles.length();i++) {
	    			JSONObject childobj = freeCircles.optJSONObject(i);
	    			if(childobj != null) {
	    				freeCirclesList.add(createChildCirclesItemResponse(childobj));
	    			}
	    		}
	    	}
	    }
	    
	    JSONObject formalCirclesObj = obj.optJSONObject("formal_circles");
	    if(formalCirclesObj != null) {
	    	formalCirclesCount = formalCirclesObj.optInt("count");
	    	JSONArray formalCircles = formalCirclesObj.optJSONArray("circles");
	    	if(formalCircles != null && formalCircles.length() > 0) {
	    		formalCirclesList = new ArrayList<UserImage>();
	    		for(int i=0;i<formalCircles.length();i++) {
	    			JSONObject childobj = formalCircles.optJSONObject(i);
	    			if(childobj != null) {
	    				formalCirclesList.add(createChildCirclesItemResponse(childobj));
	    			}
	    		}
	    	}
	    }
	}
	
	private void parseEventPoll(JSONObject obj) throws TwitterException {
		JSONObject eventobj = obj.optJSONObject("profile_events");
		if(eventobj != null) {
			if(mGroup == null) {
				mGroup = new Group();
			}
			mGroup.event_count = eventobj.optInt("count");
			JSONArray eventslist = eventobj.optJSONArray("events");
			if(eventslist != null && eventslist.length() > 0) {
				simpleEventList = new ArrayList<UserCircle>();
				for(int i=0; i<eventslist.length(); i++) {
					JSONObject childobj = eventslist.optJSONObject(i);
	    			if(childobj != null) {
	    				simpleEventList.add(new UserCircleJSONImpl(childobj, CIRCLE_TYPE_EVENT));
	    			}
				}
			}
		}
		
		JSONObject pollObj = obj.optJSONObject("profile_polls");
		if(pollObj != null) {
			if(mGroup == null) {
				mGroup = new Group();
			}
			mGroup.top_poll_count = pollObj.optInt("count");
			JSONArray pollslist = pollObj.optJSONArray("polls");
			if(pollslist != null && pollslist.length() > 0) {
				simplePoll = new ArrayList<PollInfo>();
				for(int i=0; i<pollslist.length(); i++) {
					JSONObject childobj = pollslist.optJSONObject(i);
	    			if(childobj != null) {
	    				simplePoll.add(new PollJSONImpl(childobj,false));
	    			}
				}
			}
		}
	}
	
	private UserImage createChildCirclesItemResponse(JSONObject obj) throws TwitterException {
		UserImage info = new UserImage();
//		try {	
		info.user_id = obj.optLong("id");
		info.image_url = obj.optString("image_url");
		info.userName = obj.optString("name");
		
//		} catch (JSONException jsone) {}
		
		return info;
	}
	
	private void parseGroup(JSONObject obj) {
		parseQiupuAccountInfo(obj);
		mGroup = new Group();
		mGroup.member_limit = obj.optInt("member_limit", -1);
		mGroup.is_stream_public = obj.optInt("is_stream_public", -1);
		mGroup.can_search = obj.optInt("can_search", -1);
		mGroup.can_view_members = obj.optInt("can_view_members" , -1);
		mGroup.can_join = obj.optInt("can_join", -1);
		mGroup.can_member_invite = obj.optInt("can_member_invite", -1);
		mGroup.can_member_approve = obj.optInt("can_member_approve", -1);
		mGroup.can_member_post = obj.optInt("can_member_post", -1);
		mGroup.can_member_quit = obj.optInt("can_member_quit", -1);
		mGroup.need_invite_confirm = obj.optInt("need_invited_confirm", -1);
		mGroup.bulletin = obj.optString("bulletin");
		mGroup.bulletin_updated_time = obj.optLong("bulletin_updated_time");
		mGroup.created_time = obj.optLong("created_time", 0);
		mGroup.updated_time = obj.optLong("updated_time", 0);
		mGroup.destroyed_time = obj.optLong("destroyed_time", 0);
		mGroup.role_in_group = obj.optInt("role_in_group", -1);
		mGroup.viewer_can_update = obj.optBoolean("viewer_can_update");
		mGroup.viewer_can_destroy = obj.optBoolean("viewer_can_destroy");
		mGroup.viewer_can_remove = obj.optBoolean("viewer_can_remove");
		mGroup.viewer_can_grant = obj.optBoolean("viewer_can_grant");
		mGroup.viewer_can_quit = obj.optBoolean("viewer_can_quit");
		mGroup.invited_ids = obj.optString("invited_ids");
		mGroup.startTime = obj.optLong("start_time");
		mGroup.endTime = obj.optLong("end_time");
		mGroup.coverUrl = obj.optString("theme_image");
		mGroup.repeat_type = obj.optInt(REPEAT_TYPE);
		mGroup.reminder_time = obj.optInt(REMINDER_TIME);
		mGroup.pageid = obj.optLong(PAGE_ID);
		mGroup.formal = obj.optInt(FORMAL);
		mGroup.subtype = obj.optString(SUBTYPE);
		if(obj.has("parent_id")) {
			mGroup.parent_id = obj.optLong("parent_id");
		}else if(obj.has("parent_ids")) {
			mGroup.parent_id = obj.optLong("parent_ids");
		}
		mGroup.child_circleids = obj.optString("circle_ids");
		if(obj.has("top_posts")) {
		    JSONObject top_obj = obj.optJSONObject("top_posts");
		    if(top_obj != null) {
		        mGroup.top_post_name = top_obj.optString("name");
		        mGroup.top_post_count = top_obj.optInt("count");
		    }
		}
//		if (obj.has("shared_count")) {
//		    JSONObject top_obj = obj.optJSONObject("shared_count");
//		    if (top_obj != null) {
//		        mGroup.top_poll_count = top_obj.optInt("shared_poll");
//		        mGroup.event_count = top_obj.optInt("shared_event");
//		    }
//		    
//		}
		JSONObject creator = obj.optJSONObject("creator");
		if(creator != null) {
			try {
				mGroup.creator = QiupuNewUserJSONImpl.createSimpleUserResponse(creator);
			} catch (TwitterException e) { }
		}
	}
	
	public UserCircleJSONImpl() {
	}
	
	public static ArrayList<UserCircle> createUserCircleList(HttpResponse response)
			throws TwitterException {
		ArrayList<UserCircle> list = new ArrayList<UserCircle>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createQiupuUserList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new UserCircleJSONImpl(obj));
		}

		return list;
	}
	
	public static ArrayList<UserCircle> createPublicCircleList(HttpResponse response)
            throws TwitterException {
        ArrayList<UserCircle> list = new ArrayList<UserCircle>();
        JSONArray array = null;
        try {
            array = response.asJSONArray();
        } catch (TwitterException e) {
            throw e;
        }

        Log.d(TAG, "createQiupuUserList size:" + array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj;
            try {
                obj = array.getJSONObject(i);
            } catch (JSONException e) {
                throw new TwitterException(e);
            }
            list.add(new UserCircleJSONImpl(obj, UserCircle.CIRLCE_TYPE_PUBLIC));
        }

        return list;
    }
	
	public static ArrayList<UserCircle> createEventList(HttpResponse response)
            throws TwitterException {
        ArrayList<UserCircle> list = new ArrayList<UserCircle>();
        JSONArray array = null;
        try {
            array = response.asJSONArray();
        } catch (TwitterException e) {
            throw e;
        }

        Log.d(TAG, "createQiupuUserList size:" + array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj;
            try {
                obj = array.getJSONObject(i);
            } catch (JSONException e) {
                throw new TwitterException(e);
            }
            list.add(new UserCircleJSONImpl(obj, UserCircle.CIRCLE_TYPE_EVENT));
        }

        return list;
    }
	
	public static long createCircle(HttpResponse response)
	throws TwitterException {
		long result;
		try {
			JSONObject obj = response.asJSONObject();
			result = obj.getLong("result");
		} catch (JSONException e) {
			throw new TwitterException(e);
		}
		
		return result;
	}
	
	public static UserCircle createPublicCircle(HttpResponse response) throws TwitterException {
	    UserCircle circle = new UserCircle();
	    JSONObject obj = response.asJSONObject();
	    circle.circleid = obj.optLong("group_id");
	    JSONArray joinArray = obj.optJSONArray("users");
	    circle.joinIds = new ArrayList<Long>();
	    if(joinArray != null) {
	        if(joinArray.length() > 0) {
	            for(int i=0; i<joinArray.length(); i++) {
	                JSONObject tmpobj = joinArray.optJSONObject(i);
	                if(tmpobj != null) {
	                    int status = tmpobj.optInt("status");
	                    if(UserCircle.STATUS_JOINED == status) {
	                        long uid = obj.optLong("user_id");
	                        circle.joinIds.add(uid);
	                    }
	                }
	            }
	        }
	    }
	    return circle;
	}
	
	public static UserCircle createOneUserCircleResponse(HttpResponse response, final int circleType)
			throws TwitterException {
		UserCircle circle = new UserCircle();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createQiupuUserList size:" + array.length());
		if(array.length() > 0) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(0);
				circle = new UserCircleJSONImpl(obj, circleType);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return circle;
	}
	
	public static ArrayList<Long> parsePublicCircleInviteResponse(HttpResponse response)
            throws TwitterException {

	    ArrayList<Long> joinIds = new ArrayList<Long>();
	    JSONArray array = null;
        try {
            array = response.asJSONArray();
        } catch (TwitterException e) {
            throw e;
        }

        if(array.length() > 0) {
            for(int i=0; i<array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if(obj != null) {
                    int status = obj.optInt("status");
                    if(UserCircle.STATUS_JOINED == status) {
                        long uid = obj.optLong("user_id");
                        joinIds.add(uid);
                    }
                }
            }
        }
        return joinIds;
    }
	
	public static RecieveSet createRecieveSetResponse(HttpResponse response) throws TwitterException {
		RecieveSet set = new RecieveSet();
		 JSONObject obj = response.asJSONObject();
        try {
        	set.enable = obj.getInt("recv_notif");
        	set.phone = obj.getString("notif_phone");
        	set.email = obj.getString("notif_email");

        } catch (JSONException jsone) {}

        return set;
    }
}
