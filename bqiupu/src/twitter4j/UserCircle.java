package twitter4j;

import java.util.ArrayList;




public class UserCircle extends QiupuAccountInfo implements java.io.Serializable, Comparable<UserCircle>{
	private static final long serialVersionUID = -2406027434411629477L;

	public long id;
//	public long uid;
	public long  circleid;
//	public String mCircleId;
	public String name;
    public boolean selected;
    public Group mGroup;
    public int type;
    
    public int memberCount;
    public int applyedMembersCount;
    public int invitedMembersCount;
    public ArrayList<UserImage>  inMembersImageList;
    public ArrayList<UserImage>  applyedMembersList;
    public ArrayList<UserImage>  invitedMembersList;
    
    public int freeCirclesCount;
    public int formalCirclesCount;
//    public ArrayList<UserImage>  childCirclesList;
    public ArrayList<UserImage>  freeCirclesList;
    public ArrayList<UserImage>  formalCirclesList;
    
//    public int eventsCount;
    public ArrayList<UserCircle> simpleEventList;
    
//    public int pollsCount;
    public ArrayList<PollInfo> simplePoll;
    
    public ArrayList<Long> joinIds;
    
    public ArrayList<InfoCategory> categories;

    public static final int DEFAULT_VALUE = 0;
    public static final int VALUE_ALLOWED = 1;
    
    public static final int CIRCLE_TYPE_LOCAL = 0;
    public static final int CIRLCE_TYPE_PUBLIC = 1;
    
    public static final int CIRCLE_TYPE_EVENT = 2;
    
    public static final int PRIVACY_OPEN = 1;
    public static final int PRIVACY_CLOSED = 2;
    public static final int PRIVACY_SECRET = 3;
    
    public static final int JOIN_PREMISSION_VERIFY = 0;
    public static final int JOIN_PERMISSION_DERECT_ADD = 1;
    public static final int JOIN_PERMISSION_FORBID = 2;
    
    public static final int APPROVE_MAMANGER = 0;
    public static final int APPROVE_MEMBER = 1;
    
    public static final int INVITE_PERMISSION_NEED_CONFIRM = 1;
    public static final int INVITE_PERMISSION_NOT_NEED_CONFIRM = 0;
    
    public static final int STATUS_NONE = 0;  //与指定圈子没有任何的关系
    public static final int STATUS_APPLIED = 1;  //已申请，未处理
    public static final int STATUS_INVITED = 2; //已邀请，未处理
    public static final int STATUS_JOINED = 3;  //已加入
    public static final int STATUS_REJECTED = 4; //已拒绝
    public static final int STATUS_KICKED = 5; //被踢出圈子
    public static final int STATUS_QUIT = 6; //自己退出圈子
    
    
    // for event
    public static final String REPEAT_TYPE = "repeat_type";
    public static final String REMINDER_TIME = "reminder_time";
    
    public static final String getExpendColumns () { 
    	return REPEAT_TYPE + "," + REMINDER_TIME;
    }
    
    public static final long CREATE_CIRCLE_DEFAULT_PAGE_ID = -1000;
    public static final String CIRCLE_CREATE_TYPE = "CIRCLE_CREATE_TYPE";
    public static final int circle_free = 0;
    public static final int circle_top_formal = 1;
    public static final int circle_sub_formal = 2;
    
//    public static final String SUBTYPE_COMPANY = "template.formal.company";
//    public static final String SUBTYPE_SCHOOL = "template.formal.school";
    
    
    // circle expend columns
    public static final String PAGE_ID = "page_id";
    public static final String FORMAL = "formal";
    public static final String SUBTYPE = "subtype";
    public static final String PARENT_ID = "parent_id";
    public static final String PARENT_IDS = "parent_ids";
    
    public static final String CIRCLE_IDS = "circle_ids";
    public static final String CIRCLES = "circles";
    
    public static final String getExpendCircleColumns () { 
    	return PAGE_ID + "," + FORMAL + "," + SUBTYPE + "," + PARENT_ID;
    }
    
    public static final String getCircleDetailExpendColumns() {
    
    	return getExpendCircleColumns() + "," +CIRCLE_IDS + "," + CIRCLES;
    }
    
	public UserCircle clone() {
		UserCircle circle = new UserCircle();
		circle.id = id;
		circle.uid = uid;
		circle.circleid = circleid;
		circle.name = name;
        circle.memberCount = memberCount;
        circle.applyedMembersCount = applyedMembersCount;
        circle.invitedMembersCount = invitedMembersCount;
        if(inMembersImageList != null) {
            circle.inMembersImageList = new ArrayList<UserImage>();
            for(int i=0;i<inMembersImageList.size();i++) {
                circle.inMembersImageList.add(inMembersImageList.get(i).clone());
            }
        }
        if(applyedMembersList != null) {
            circle.applyedMembersList = new ArrayList<UserImage>();
            for(int i=0;i<applyedMembersList.size();i++) {
                circle.applyedMembersList.add(applyedMembersList.get(i).clone());
            }
        }
        if(invitedMembersList != null) {
            circle.invitedMembersList = new ArrayList<UserImage>();
            for(int i=0;i<invitedMembersList.size();i++) {
                circle.invitedMembersList.add(invitedMembersList.get(i).clone());
            }
        }
        super.clone(circle);
        if(mGroup != null) {
        	circle.mGroup = mGroup.clone();
        }
        circle.type = type;

		return circle;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserCircle)) {
			return false;
		}
		UserCircle uc = (UserCircle) obj;
		return (uc.id == id);
	}
	
	@Override
	public String toString() {
		return " id           = "+id +
				"circleid     = "+ circleid +
	           " uid          = " +uid+
	           " name         = "+name+
	           " memberCount  = "+memberCount + super.toString();
	}

    @Override
    public int compareTo(UserCircle another) {
        long delta = circleid - another.circleid;
        if (delta == 0) {
            return 0;
        } else if (delta > 0) {
            return 1;
        } else {
            return -1;
        }
    }
    
//    public static ContentValues toContentValues(UserCircle circle) {
//    	ContentValues cv = QiupuAccountInfo.toContentValues(circle);
//    	cv.put(CircleColumns.USERID, circle.uid);
//		cv.put(CircleColumns.CIRCLE_ID, circle.circleid);
//		cv.put(CircleColumns.CIRCLE_NAME, circle.name);
//		cv.put(CircleColumns.MEMBER_COUNT, circle.memberCount);
//		
//    }
//    
//    public static UserCircle createCircleInformation(UserCircle result, Cursor cursor) {
//    	QiupuAccountInfo.createUserInformation(result, cursor);
//    	result.uid = cursor.getLong(cursor.getColumnIndex(CircleColumns.USERID));
//    	result.circleid = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
//    	result.name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
//    	result.memberCount = cursor.getInt(cursor.getColumnIndex(CircleColumns.MEMBER_COUNT));
//    	result.type = cursor.getInt(cursor.getColumnIndex(CircleColumns.TYPE));
//    	result.mGroup = Group.createGroupInformation(new Group(), cursor);
//      return result;
//  }
    
    public static class Group implements java.io.Serializable
	{
    	private static final long serialVersionUID = 1L;
    	public int member_limit;
        public int is_stream_public;
        public int can_search;
        public int can_view_members;
        public int can_join;
        public int can_member_invite;
        public int can_member_approve;
        public int can_member_post;
        public int can_member_quit;  // 
        public int need_invite_confirm;
        public String bulletin;
        public long bulletin_updated_time;
        public long created_time;
        public long updated_time;
        public long destroyed_time;
        public int role_in_group;
        public String invited_ids;
        public String child_circleids;
        public boolean viewer_can_update;
        public boolean viewer_can_destroy;
        public boolean viewer_can_remove;
        public boolean viewer_can_grant;
        public boolean viewer_can_quit; // member can or not to quit, if role is admin and only one admin, you must licensed to others.
        public QiupuSimpleUser creator;
        public long startTime;//just for event
        public long endTime;//just for event
        public String coverUrl; 
        public String top_post_name;
        public int top_post_count;
        public int top_poll_count;
        public int event_count;
        public int repeat_type;// for event
        public int reminder_time;// for event
        
        public long pageid;
        public int formal;
        public String subtype;
        public long parent_id;
    	
    	public Group()
    	{ }
    	
    	public void despose()
    	{ 
    		creator = null;
    	}
    	
    	public Group clone() {
    		Group group = new Group();
    		group.member_limit = member_limit;
    		group.is_stream_public = is_stream_public;
    		group.can_search = can_search;
    		group.can_view_members = can_view_members;
    		group.can_join = can_join;
    		group.can_member_invite = can_member_invite;
    		group.can_member_approve = can_member_approve;
    		group.can_member_post = can_member_post;
    		group.can_member_quit = can_member_quit;
    		group.need_invite_confirm = need_invite_confirm;
    		group.bulletin = bulletin;
    		group.bulletin_updated_time = bulletin_updated_time;
    		group.created_time = created_time;
    		group.updated_time = updated_time;
    		group.destroyed_time = destroyed_time;
    		group.role_in_group = role_in_group;
    		group.viewer_can_update = viewer_can_update;
    		group.viewer_can_destroy = viewer_can_destroy;
    		group.viewer_can_remove = viewer_can_remove;
    		group.viewer_can_grant = viewer_can_grant;
    		group.viewer_can_quit = viewer_can_quit;
    		group.invited_ids = invited_ids;
    		if(creator != null){
    			group.creator = creator.clone();
    		}
    		group.startTime = startTime;
    		group.endTime = endTime;
    		group.coverUrl = coverUrl;
    		group.top_post_name = top_post_name;
    		group.top_post_count = top_post_count;
    		group.repeat_type = repeat_type;
    		group.reminder_time = reminder_time;
    		group.pageid = pageid;
    		group.subtype = subtype;
    		group.formal = formal;
    		group.parent_id = parent_id;
    		group.child_circleids = child_circleids;
    		group.event_count = event_count;
    		    
    		return group;
    	}
    	
//    	public static ContentValues toContentValues(Group groupInfo) {
//            ContentValues cv = new ContentValues();
//            cv.put(GroupColumns.MEMBER_LIMIT, groupInfo.member_limit);
//    		cv.put(GroupColumns.IS_STREAM_PUBLIC, groupInfo.is_stream_public);
//    		cv.put(GroupColumns.CAN_SEARCH, groupInfo.can_search);
//    		cv.put(GroupColumns.CAN_VIEW_MEMBERS, groupInfo.can_view_members);
//    		cv.put(GroupColumns.CAN_JOIN, groupInfo.can_join);
//    		cv.put(GroupColumns.CREATED_TIME, groupInfo.created_time);
//    		cv.put(GroupColumns.UPDATED_TIME, groupInfo.updated_time);
//    		cv.put(GroupColumns.DESTROYED_TIME, groupInfo.destroyed_time);
//    		cv.put(GroupColumns.ROLE_IN_GROUP, groupInfo.role_in_group);
//    		cv.put(GroupColumns.VIEWER_CAN_UPDATE, groupInfo.viewer_can_update ? 1 : 0);
//    		cv.put(GroupColumns.VIEWER_CAN_DESTROY, groupInfo.viewer_can_destroy ? 1 : 0);
//    		cv.put(GroupColumns.VIEWER_CAN_REMOVE, groupInfo.viewer_can_remove ? 1 : 0);
//    		cv.put(GroupColumns.VIEWER_CAN_GRANT, groupInfo.viewer_can_grant ? 1 : 0);
//            
//            return cv;
//        }
    	
//    	public static Group createGroupInformation(Group result, Cursor cursor) {
//    		result.member_limit = cursor.getInt(cursor.getColumnIndex(GroupColumns.MEMBER_LIMIT));
//    		result.is_stream_public = cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_STREAM_PUBLIC));
//    		result.can_search = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_SEARCH));
//    		result.can_view_members = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_VIEW_MEMBERS));
//    		result.can_join = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_JOIN));
//    		result.role_in_group = cursor.getInt(cursor.getColumnIndex(GroupColumns.ROLE_IN_GROUP));
//    		result.viewer_can_destroy = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_DESTROY)) == 1 ? true : false;
//    		result.viewer_can_grant = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_GRANT)) == 1 ? true : false;
//    		result.viewer_can_remove = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_REMOVE)) == 1 ? true : false;
//    		result.viewer_can_update = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_UPDATE)) == 1 ? true : false;
//    		return result;
//    	}
    	
    	@Override
    	public String toString() {
    		return " member_limit           = "+member_limit +
    				" is_stream_public          = " +is_stream_public+
    				" can_search         = "+can_search+
    				" can_view_members         = "+can_view_members+
    				" can_join         = "+can_join+
    				" can_member_invite         = "+can_member_invite+
    				" can_member_approve         = "+can_member_approve+
    				" can_member_post         = "+can_member_post+
    				"can_member_quit          = " + can_member_quit + 
    				" created_time         = "+created_time+
    				" updated_time         = "+updated_time+
    				" destroyed_time         = "+destroyed_time+
    				" role_in_group         = "+role_in_group+
    				" viewer_can_update         = "+viewer_can_update+
    				" viewer_can_destroy         = "+viewer_can_destroy+
    				" viewer_can_remove         = "+viewer_can_remove+
    				" viewer_can_grant         = "+viewer_can_grant + 
    				" coverUrl         = "+coverUrl + 
    				" top_post_name         = "+top_post_name + 
    				"top_post_count        = " + top_post_count;
    	} 
	}
    
    public static final boolean isPrivacyOpen(Group group) {
        return group.is_stream_public == UserCircle.VALUE_ALLOWED
                && group.can_search == UserCircle.VALUE_ALLOWED
                && group.can_view_members == UserCircle.VALUE_ALLOWED;
    }
    public static final boolean isPrivacyClosed(Group group) {
        return group.is_stream_public == UserCircle.DEFAULT_VALUE
                && group.can_search == UserCircle.VALUE_ALLOWED
                && group.can_view_members == UserCircle.VALUE_ALLOWED;
    }
    public static final boolean isPrivacySecret(Group group) {
        return group.is_stream_public == UserCircle.DEFAULT_VALUE
                && group.can_search == UserCircle.DEFAULT_VALUE
                && group.can_view_members == UserCircle.DEFAULT_VALUE;
    }
    
    public static final boolean canApproveInvite(Group group) {
        return group.can_member_approve == VALUE_ALLOWED 
                || group.can_member_invite == VALUE_ALLOWED;
    }
    
    public static final void setPrivacyValue(int privacy, Group group) {
        if(PRIVACY_OPEN == privacy) {
            group.is_stream_public = VALUE_ALLOWED;
            group.can_search = VALUE_ALLOWED;
            group.can_view_members = VALUE_ALLOWED;
        }else if(PRIVACY_CLOSED == privacy) {
            group.is_stream_public = DEFAULT_VALUE;
            group.can_search = VALUE_ALLOWED;
            group.can_view_members = VALUE_ALLOWED;
        }else if(PRIVACY_SECRET == privacy) {
            group.is_stream_public = DEFAULT_VALUE;
            group.can_search = DEFAULT_VALUE;
            group.can_view_members = DEFAULT_VALUE;
        }
    }
    
    public static final void setApproveValue(int approve, Group group) {
        if(APPROVE_MAMANGER == approve) {
            group.can_member_approve = DEFAULT_VALUE;
            group.can_member_invite = DEFAULT_VALUE;
        }else if(APPROVE_MEMBER == approve) {
            group.can_member_approve = VALUE_ALLOWED;
            group.can_member_invite = VALUE_ALLOWED;
        }
    }
    
    public static class RecieveSet implements java.io.Serializable
	{
    	private static final long serialVersionUID = 1L;
    	public static final int EBABLE_NOTIFICATION = 1;
    	public static final int DISABLE_NOTIFICATION = 0;
    	public int enable;
        public String phone;
        public String email;
    	public RecieveSet() { }
    	
		public void despose() {
			phone = null;
			email = null;
		}
    	
    	public RecieveSet clone() {
    		RecieveSet receiveSet = new RecieveSet();
    		receiveSet.enable = enable;
    		receiveSet.phone = phone;
    		receiveSet.email = email;
    		return receiveSet;
    	}
    	
    	@Override
    	public String toString() {
    		return " phone           = "+phone +
    				" email         = "+email;
    	} 
    	
	}
    
}
