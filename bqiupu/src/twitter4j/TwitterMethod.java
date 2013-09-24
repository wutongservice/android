/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TwitterMethod implements java.io.Serializable {
    String name;
    private static final long serialVersionUID = 5776633408291563058L;

    private TwitterMethod() {
        throw new AssertionError();
    }

    private TwitterMethod(String name) {
        this.name = name;
        instances.put(name, this);
    }

    private static final Map<String, TwitterMethod> instances = new HashMap<String, TwitterMethod>();

    public final String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwitterMethod)) return false;

        TwitterMethod that = (TwitterMethod) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Method{" +
                "name='" + name + '\'' +
                '}';
    }

    private static TwitterMethod getInstance(String name){
        return instances.get(name);
    }

    // assures equality after deserialization
    private Object readResolve() throws ObjectStreamException {
        return getInstance(name);
    }

    /*Search API Methods*/
    public static final TwitterMethod SEARCH = getInstance("SEARCH");

    public static final TwitterMethod TRENDS = new TwitterMethod("TRENDS");
    public static final TwitterMethod CURRENT_TRENDS = new TwitterMethod("CURRENT_TRENDS");
    public static final TwitterMethod DAILY_TRENDS = new TwitterMethod("DAILY_TRENDS");
    public static final TwitterMethod WEEKLY_TRENDS = new TwitterMethod("WEEKLY_TRENDS");

    /*Timeline Methods*/
    public static final TwitterMethod PUBLIC_TIMELINE = new TwitterMethod("PUBLIC_TIMELINE");
    public static final TwitterMethod HOME_TIMELINE = new TwitterMethod("HOME_TIMELINE");
    public static final TwitterMethod FRIENDS_TIMELINE = new TwitterMethod("FRIENDS_TIMELINE");
    public static final TwitterMethod USER_TIMELINE = new TwitterMethod("USER_TIMELINE");
    public static final TwitterMethod MENTIONS = new TwitterMethod("MENTIONS");
    public static final TwitterMethod RETWEETED_BY_ME = new TwitterMethod("RETWEETED_BY_ME");
    public static final TwitterMethod RETWEETED_TO_ME = new TwitterMethod("RETWEETED_TO_ME");
    public static final TwitterMethod RETWEETS_OF_ME = new TwitterMethod("RETWEETS_OF_ME");

    /*Status Methods*/
    public static final TwitterMethod SHOW_STATUS = new TwitterMethod("SHOW_STATUS");
    public static final TwitterMethod UPDATE_STATUS = new TwitterMethod("UPDATE_STATUS");
    public static final TwitterMethod DESTROY_STATUS = new TwitterMethod("DESTROY_STATUS");
    public static final TwitterMethod RETWEET_STATUS = new TwitterMethod("RETWEET_STATUS");
    public static final TwitterMethod RETWEETS = new TwitterMethod("RETWEETS");
    public static final TwitterMethod RETWEETED_BY = new TwitterMethod("RETWEETED_BY");
    public static final TwitterMethod RETWEETED_BY_IDS = new TwitterMethod("RETWEETED_BY_IDS");
    
    /*User Methods*/
    public static final TwitterMethod SHOW_USER = new TwitterMethod("SHOW_USER");
    public static final TwitterMethod LOOKUP_USERS = new TwitterMethod("LOOKUP_USERS");
    public static final TwitterMethod SEARCH_USERS = new TwitterMethod("SEARCH_USERS");
    public static final TwitterMethod SUGGESTED_USER_CATEGORIES = new TwitterMethod("SUGGESTED_USER_CATEGORIES");
    public static final TwitterMethod USER_SUGGESTIONS = new TwitterMethod("USER_SUGGESTIONS");
    public static final TwitterMethod FRIENDS_STATUSES = new TwitterMethod("FRIENDS_STATUSES");
    public static final TwitterMethod FOLLOWERS_STATUSES = new TwitterMethod("FOLLOWERS_STATUSES");

    /*List Methods*/
    public static final TwitterMethod CREATE_USER_LIST = new TwitterMethod("CREATE_USER_LIST");
    public static final TwitterMethod UPDATE_USER_LIST = new TwitterMethod("UPDATE_USER_LIST");
    public static final TwitterMethod USER_LISTS = new TwitterMethod("USER_LISTS");
    public static final TwitterMethod SHOW_USER_LIST = new TwitterMethod("SHOW_USER_LIST");
    public static final TwitterMethod DESTROY_USER_LIST = new TwitterMethod("DELETE_USER_LIST");
    public static final TwitterMethod USER_LIST_STATUSES = new TwitterMethod("USER_LIST_STATUSES");
    public static final TwitterMethod USER_LIST_MEMBERSHIPS = new TwitterMethod("USER_LIST_MEMBERSHIPS");
    public static final TwitterMethod USER_LIST_SUBSCRIPTIONS = new TwitterMethod("USER_LIST_SUBSCRIPTIONS");

    /*List Members Methods*/
    public static final TwitterMethod LIST_MEMBERS = new TwitterMethod("LIST_MEMBERS");
    public static final TwitterMethod ADD_LIST_MEMBER = new TwitterMethod("ADD_LIST_MEMBER");
    public static final TwitterMethod DELETE_LIST_MEMBER = new TwitterMethod("DELETE_LIST_MEMBER");
    public static final TwitterMethod CHECK_LIST_MEMBERSHIP = new TwitterMethod("CHECK_LIST_MEMBERSHIP");

    /*List Subscribers Methods*/
    public static final TwitterMethod LIST_SUBSCRIBERS = new TwitterMethod("LIST_SUBSCRIBERS");
    public static final TwitterMethod SUBSCRIBE_LIST = new TwitterMethod("SUBSCRIBE_LIST");
    public static final TwitterMethod UNSUBSCRIBE_LIST = new TwitterMethod("UNSUBSCRIBE_LIST");
    public static final TwitterMethod CHECK_LIST_SUBSCRIPTION = new TwitterMethod("CHECK_LIST_SUBSCRIPTION");

    /*Direct Message Methods*/
    public static final TwitterMethod DIRECT_MESSAGES = new TwitterMethod("DIRECT_MESSAGES");
    public static final TwitterMethod SENT_DIRECT_MESSAGES = new TwitterMethod("SENT_DIRECT_MESSAGES");
    public static final TwitterMethod SEND_DIRECT_MESSAGE = new TwitterMethod("SEND_DIRECT_MESSAGE");
    public static final TwitterMethod DESTROY_DIRECT_MESSAGES = new TwitterMethod("DESTROY_DIRECT_MESSAGES");

    /*Friendship Methods*/
    public static final TwitterMethod CREATE_FRIENDSHIP = new TwitterMethod("CREATE_FRIENDSHIP");
    public static final TwitterMethod DESTROY_FRIENDSHIP = new TwitterMethod("DESTROY_FRIENDSHIP");
    public static final TwitterMethod EXISTS_FRIENDSHIP = new TwitterMethod("EXISTS_FRIENDSHIP");
    public static final TwitterMethod SHOW_FRIENDSHIP = new TwitterMethod("SHOW_FRIENDSHIP");
    public static final TwitterMethod INCOMING_FRIENDSHIPS = new TwitterMethod("INCOMING_FRIENDSHIPS");
    public static final TwitterMethod OUTGOING_FRIENDSHIPS = new TwitterMethod("OUTGOING_FRIENDSHIPS");

    /*Social Graph Methods*/
    public static final TwitterMethod FRIENDS_IDS = new TwitterMethod("FRIENDS_IDS");
    public static final TwitterMethod FOLLOWERS_IDS = new TwitterMethod("FOLLOWERS_IDS");

    /*Account Methods*/
    public static final TwitterMethod VERIFY_CREDENTIALS = new TwitterMethod("VERIFY_CREDENTIALS");
    public static final TwitterMethod RATE_LIMIT_STATUS = new TwitterMethod("RATE_LIMIT_STATUS");
    public static final TwitterMethod UPDATE_DELIVERY_DEVICE = new TwitterMethod("UPDATE_DELIVERY_DEVICE");
    public static final TwitterMethod UPDATE_PROFILE_COLORS = new TwitterMethod("UPDATE_PROFILE_COLORS");
    public static final TwitterMethod UPDATE_PROFILE_IMAGE = new TwitterMethod("UPDATE_PROFILE_IMAGE");
    public static final TwitterMethod UPDATE_PROFILE_BACKGROUND_IMAGE = new TwitterMethod("UPDATE_PROFILE_BACKGROUND_IMAGE");
    public static final TwitterMethod UPDATE_PROFILE = new TwitterMethod("UPDATE_PROFILE");

    /*Favorite Methods*/
    public static final TwitterMethod FAVORITES = new TwitterMethod("FAVORITES");
    public static final TwitterMethod CREATE_FAVORITE = new TwitterMethod("CREATE_FAVORITE");
    public static final TwitterMethod DESTROY_FAVORITE = new TwitterMethod("DESTROY_FAVORITE");

    /*Notification Methods*/
    public static final TwitterMethod ENABLE_NOTIFICATION = new TwitterMethod("ENABLE_NOTIFICATION");
    public static final TwitterMethod DISABLE_NOTIFICATION = new TwitterMethod("DISABLE_NOTIFICATION");

    /*Block Methods*/
    public static final TwitterMethod CREATE_BLOCK = new TwitterMethod("CREATE_BLOCK");
    public static final TwitterMethod DESTROY_BLOCK = new TwitterMethod("DESTROY_BLOCK");
    public static final TwitterMethod EXISTS_BLOCK = new TwitterMethod("EXISTS_BLOCK");
    public static final TwitterMethod BLOCKING_USERS = new TwitterMethod("BLOCKING_USERS");
    public static final TwitterMethod BLOCKING_USERS_IDS = new TwitterMethod("BLOCKING_USERS_IDS");

    /*Spam Reporting Methods*/
    public static final TwitterMethod REPORT_SPAM = new TwitterMethod("REPORT_SPAM");

    /*Saved Searches Methods*/
    //getSavedSearches()
    //showSavedSearch()
    //createSavedSearch()
    //destroySavedSearch()

    /*Local Trends Methods*/
    public static final TwitterMethod AVAILABLE_TRENDS = new TwitterMethod("AVAILABLE_TRENDS");
    public static final TwitterMethod LOCATION_TRENDS = new TwitterMethod("LOCATION_TRENDS");

    /*Geo Methods*/
    public static final TwitterMethod NEAR_BY_PLACES = new TwitterMethod("NEAR_BY_PLACES");
    public static final TwitterMethod REVERSE_GEO_CODE = new TwitterMethod("REVERSE_GEO_CODE");
    public static final TwitterMethod GEO_DETAILS = new TwitterMethod("GEO_DETAILS");

    /*Help Methods*/
    public static final TwitterMethod TEST = new TwitterMethod("TEST");
    
    /*AUTN_LOGIN*/
    public static final TwitterMethod AUTH_LOGIN = new TwitterMethod("AUTH_LOGIN");
    
    public static final TwitterMethod REGISTER_ACCOUNT = new TwitterMethod("REGISTER_ACCOUNT");
    public static final TwitterMethod LOGIN_BORQS = new TwitterMethod("LOGIN_BORQS");
    public static final TwitterMethod LOGOUT_BORQS = new TwitterMethod("LOGOUT_BORQS");
    public static final TwitterMethod VERIFY_ACCOUNT = new TwitterMethod("VERIFY_ACCOUNT");
    public static final TwitterMethod UPLOAD_FILE = new TwitterMethod("UPLOAD_FILE");
    public static final TwitterMethod BACKUP_APK_RECORD = new TwitterMethod("BACKUP_APK_RECORD");
    public static final TwitterMethod GET_BACKUP_FILE = new TwitterMethod("GET_BACKUP_FILE");
    public static final TwitterMethod COLLECT_PHONE_INFO = new TwitterMethod("COLLECT_PHONE_INFO");
    public static final TwitterMethod GET_BACKUP_RECORD = new TwitterMethod("GET_BACKUP_RECORD");
    public static final TwitterMethod GET_BACKUP_APK = new TwitterMethod("GET_BACKUP_APK");
    public static final TwitterMethod DOWNLOAD_FILES = new TwitterMethod("DOWNLOAD_FILES");
    public static final TwitterMethod BACKUP_DATA = new TwitterMethod("BACKUP_DATA");
    public static final TwitterMethod GET_APK_LIST = new TwitterMethod("GET_APK_LIST");
    public static final TwitterMethod DOWNLOAD_ICON = new TwitterMethod("DOWNLOAD_ICON");
    
    public static final TwitterMethod GET_APK_DETAIL_INFORMATION = new TwitterMethod("GET_APK_DETAIL_INFORMATION");
    public static final TwitterMethod GET_USERLIST_BY_SEARCHNAME = new TwitterMethod("GET_USERLIST_BY_SEARCHNAME");
    public static final TwitterMethod ADD_FRIEND = new TwitterMethod("ADD_FRIEND");
    public static final TwitterMethod GET_FRIENDS = new TwitterMethod("GET_FRIENDS");
    public static final TwitterMethod GET_REQUESTS = new TwitterMethod("GET_REQUESTS");
    
    public static final TwitterMethod SYNC_APKS_STATUS = new TwitterMethod("SYNC_APKS_STATUS");
    public static final TwitterMethod POST_SHARE = new TwitterMethod("POST_SHARE");
    public static final TwitterMethod GET_POST_TIMELINE = new TwitterMethod("GET_POST_TIMELINE");
    public static final TwitterMethod GET_POST_TOP = new TwitterMethod("GET_POST_TOP");
    public static final TwitterMethod POST_APK_COMMENT = new TwitterMethod("POST_APK_COMMENT");
    
    public static final TwitterMethod POST_WALL = new TwitterMethod("POST_WALL");
    public static final TwitterMethod POST_LINK = new TwitterMethod("POST_LINK");
    public static final TwitterMethod STATUS_UPDATE = new TwitterMethod("STATUS_UPDATE");
    public static final TwitterMethod INSTALL_UPDATE = new TwitterMethod("INSTALL_UPDATE");
    public static final TwitterMethod DOWNLOAD_UPDATE = new TwitterMethod("DOWNLOAD_UPDATE");
    public static final TwitterMethod LIKE_USERS = new TwitterMethod("LIKE_USERS");
    public static final TwitterMethod REPORT_ABUSE = new TwitterMethod("REPORT_ABUSE");
    public static final TwitterMethod REMARK_SET = new TwitterMethod("REMARK_SET");
    public static final TwitterMethod ADD_FRIEND_CONTACT = new TwitterMethod("ADD_FRIEND_CONTACT");
    public static final TwitterMethod GET_NOTIFICATION_VALUE = new TwitterMethod("GET_NOTIFICATION_VALUE");
    public static final TwitterMethod GET_ALL_ALBUM = new TwitterMethod("GET_ALL_ALBUM");
    public static final TwitterMethod GET_ALBUM = new TwitterMethod("GET_ALBUM");
    public static final TwitterMethod GET_PHOTOS_ALBUM = new TwitterMethod("GET_PHOTOS_ALBUM");
    public static final TwitterMethod GET_PHOTO_BYID = new TwitterMethod("GET_PHOTO_BYID");
    public static final TwitterMethod PHOTOS_DEL = new TwitterMethod("PHOTOS_DEL");
    public static final TwitterMethod POST_UPDATEACTION_VALUE = new TwitterMethod("POST_UPDATEACTION_VALUE");
    public static final TwitterMethod POST_OTHER_FILE_VALUE = new TwitterMethod("POST_OTHER_FILE_VALUE");
    public static final TwitterMethod GET_NEAR_BY_PEOPLE = new TwitterMethod("GET_NEAR_BY_PEOPLE");
    public static final TwitterMethod SYNC_THEME = new TwitterMethod("SYNC_THEME");
    public static final TwitterMethod GET_POLL_LIST = new TwitterMethod("GET_POLL_LIST");
    public static final TwitterMethod VOTE_POLL = new TwitterMethod("VOTE_POLL");
    public static final TwitterMethod DELETE_POLL = new TwitterMethod("DELETE_POLL");
    public static final TwitterMethod MUTE_OBJECT = new TwitterMethod("MUTE_OBJECT");
    public static final TwitterMethod POST_TOP_LIST = new TwitterMethod("POST_TOP_LIST");

    public static final TwitterMethod GET_BELONG_COMPANY = new TwitterMethod("GET_BELONG_COMPANY");
    public static final TwitterMethod COMPANY_SHOW = new TwitterMethod("COMPANY_SHOW");

}
