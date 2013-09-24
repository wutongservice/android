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

import static twitter4j.internal.http.HttpParameter.getParameterArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import twitter4j.Stream.Comments;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.UserCircle.RecieveSet;
import twitter4j.api.AccountMethods;
import twitter4j.api.BlockMethods;
import twitter4j.api.DirectMessageMethods;
import twitter4j.api.FavoriteMethods;
import twitter4j.api.FriendsFollowersMethods;
import twitter4j.api.FriendshipMethods;
import twitter4j.api.GeoMethods;
import twitter4j.api.HelpMethods;
import twitter4j.api.ListMembersMethods;
import twitter4j.api.ListMethods;
import twitter4j.api.ListSubscribersMethods;
import twitter4j.api.LocalTrendsMethods;
import twitter4j.api.NotificationMethods;
import twitter4j.api.SavedSearchesMethods;
import twitter4j.api.SearchMethods;
import twitter4j.api.SpamReportingMethods;
import twitter4j.api.StatusMethods;
import twitter4j.api.TimelineMethods;
import twitter4j.api.TrendsMethods;
import twitter4j.api.UserMethods;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.Authorization;
import twitter4j.http.BasicAuthorization;
import twitter4j.http.RequestToken;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.util.MD5;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.Base64;
import com.borqs.qiupu.util.StringUtil;

/**
 * A java representation of the <a href="http://apiwiki.twitter.com/">Twitter API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is suggested to extend this class only for mock testing purporse.<br>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Twitter extends TwitterOAuthSupportBaseImpl
        implements java.io.Serializable,
        SearchMethods,
        TrendsMethods,
        TimelineMethods,
        StatusMethods,
        UserMethods,
        ListMethods,
        ListMembersMethods,
        ListSubscribersMethods,
        DirectMessageMethods,
        FriendshipMethods,
        FriendsFollowersMethods,
        AccountMethods,
        FavoriteMethods,
        NotificationMethods,
        BlockMethods,
        SpamReportingMethods,
        SavedSearchesMethods,
        LocalTrendsMethods,
        GeoMethods,
        HelpMethods {
    private static final long serialVersionUID = -1486360080128882436L;
	private static final String TAG = "Qiupu.Twitter";
	private static final String COLFULLBASE = "@full";
	private static final String COLSTD = "@std"; // default column
	private static final String COLXFRIENDS = "@xfriend";
	private static final String COLPRIVACY = "profile_privacy" + ",pending_req_types";

    private static final String API_ACCOUNT_LOGIN = "account/login";
    private static final String API_ACCOUNT_LOGOUT = "account/logout";
    
    private static final String API_ACCOUNT_SHOW_USER = "user/show";
    private static final String API_ACCOUNT_CHANGE_PASSWORD = "account/change_password";
    


    private static final String API_FRIEND_SHOW_FRIEND = "friend/show";
    private static final String API_FRIEND_SHOW_FOLLOWER = "follower/show";
    private static final String API_FRIEND_SHOW_MUTUAL = "friend/both";

    /**
     * Depressed api, will be replace by separated ADD/REMOVE in new platform, see
     * to API_CIRCLE_MEMBER_ADD and API_CIRCLE_MEMBER_REMOVE in TwitterNew.
     */
    private static final String API_CIRCLE_ALTER_MEMBER = "friend/usersset";
    private static final String API_CIRCLE_SET_CIRCLE = "friend/circlesset";
    private static final String API_EXCHAGNE_VCARD = "friend/exchange_vcard";
    private static final String API_CIRCLE_SHOW = "circle/show";
    private static final String API_CIRCLE_CREATE = "circle/create";
    private static final String API_CIRCLE_REMOVE = "circle/destroy";
    
    private static final String API_EVENT_CREATE = "v2/event/create";
    private static final String API_EVENT_EDIT = "v2/event/update";
    private static final String API_EVENT_REMOVE = "v2/event/destroy";
    private static final String API_EVENT_INVITE = "v2/event/invite";
    private static final String API_EVENT_SHOW = "v2/event/show";
    private static final String API_EVENT_USERS = "v2/event/users";
    private static final String API_EVENT_EDIT_IMAGE = "v2/event/upload_profile_image";
    private static final String API_EVENT_APPLY_JOIN = "v2/event/join";
    private static final String API_EVENT_SEARCH = "v2/event/search";
    private static final String API_EVENT_APPROVE = "v2/event/approve";
    private static final String API_EVENT_IGNORE = "v2/event/ignore";
    private static final String API_EVENT_REMOVE_MEMBER = "v2/event/remove";
    private static final String API_EVENT_GRANT_MEMBER = "v2/event/grant";
    private static final String API_EVENT_GET_RECEIVE_SET = "v2/event/get_notif";
    private static final String API_EVENT_SET_RECEIVE = "v2/event/update_notif";
    private static final String API_EVENT_SEARCH_PEOPLE = "v2/event/users"; // the api same to API_EVENT_USERS. add param "key"
    private static final String API_EVENT_SYNC_THEME = "v2/event/themes";

    private static final String API_PUBLIC_CIRCLE_CREATE = "v2/public_circle/create";
    private static final String API_PUBLIC_CIRCLE_EDIT = "v2/public_circle/update";
    private static final String API_PUBLIC_CIRCLE_REMOVE = "v2/public_circle/destroy";
    private static final String API_PUBLIC_CIRCLE_INVITE = "v2/public_circle/invite";
    private static final String API_PUBLIC_CIRCLE_SHOW = "v2/public_circle/show";
    private static final String API_PUBLIC_CIRCLE_USERS = "v2/public_circle/users";
    private static final String API_PUBLIC_CIRCLE_EDIT_IMAGE = "v2/public_circle/upload_profile_image";
    private static final String API_PUBLIC_CIRCLE_APPLY_JOIN = "v2/public_circle/join";
    private static final String API_PUBLIC_CIRCLE_SEARCH = "v2/public_circle/search";
    private static final String API_PUBLIC_CIRCLE_APPROVE = "v2/public_circle/approve";
    private static final String API_PUBLIC_CIRCLE_IGNORE = "v2/public_circle/ignore";
    private static final String API_PUBLIC_CIRCLE_REMOVE_MEMBER = "v2/public_circle/remove";
    private static final String API_PUBLIC_CIRCLE_GRANT_MEMBER = "v2/public_circle/grant";
    private static final String API_PUBLIC_CIRCLE_GET_RECEIVE_SET = "v2/public_circle/get_notif";
    private static final String API_PUBLIC_CIRCLE_SET_RECEIVE = "v2/public_circle/update_notif";
    private static final String API_PUBLIC_CIRCLE_SEARCH_PEOPLE = "v2/public_circle/users";// the api same to API_PUBLIC_CIRCLE_USERS. add param "key"
    private static final String API_PUBLIC_CIRCLE_EVENTS = "v2/public_circle/events";
    private static final String API_PUBLIC_CIRCLE_SUBCIRCLS_SHOW = "v2/public_circle/subcircles/show";
    private static final String API_SYNC_TOP_CIRCLE = "v2/public_circle/top/show";
    
    private static final String API_SUGGEST_GET = "suggest/get";
    private static final String API_SUGGEST_REJECT = "suggest/refuse";
    private static final String API_SUGGEST_RECOMMEDN_USER = "suggest/recommend";
    
    private static final String API_REMARK_SET = "remark/set";
    private static final String API_REPORT_ABUSE = "post/report_abuse";
    private static final String API_MUTE_OBJECT = "ignore/create";
    
    private static final String API_ALBUM_ALL = "album/all";
    private static final String API_ALBUM_GET = "album/get";
    private static final String API_PHOTO_ALBUM_GET = "photo/album_get";
    private static final String API_PHOTO_GET = "photo/get";
    private static final String API_PHOTO_DELETE = "photo/delete";
    private static final String API_POST_UPDATE_SETTING = "post/updateaction";
    private static final String API_POST_TOP_GET = "post/top_posts_get";

    private static final String API_GET_LBS_USERS = "user/shaking";
    private static final String API_GET_NEARBY_USERS = "user/nearby";
    private static final String API_GET_POLL = "poll/get";
    private static final String API_ADD_POLL_ITEMS = "poll/add_items";
    private static final String API_VOTE = "poll/vote";
    private static final String API_CREATE_POLL = "poll/create";
    private static final String API_GET_USER_POLL_LIST = "poll/list/user";
    private static final String API_GET_FRIEND_POLL_LIST = "poll/list/friends";
    private static final String API_GET_PUBLIC_POLL_LIST = "poll/list/public";
    private static final String API_POLL_DESTROY = "poll/destroy";
    
    private static final String API_COMPANY_BELONG = "company/belongs";
    private static final String API_COMPANY_SHOW = "company/show";
    private static final String API_COMPANY_CIRCLE_SHOW = "company/department_circles";
    private static final String API_SET_TOP = "post/top_posts_set";
    
    private static final String API_PAGE_SHOW = "page/show";
    private static final String API_PAGE_CREATE = "page/create";
    private static final String API_PAEG_SHOW_ONE = "page/show1";
    private static final String API_PAEG_EDIT = "page/update";
    private static final String API_PAEG_UPLOAD_COVER = "page/upload_cover";
    private static final String API_PAEG_UPLOAD_LOGO = "page/upload_logo";
    private static final String API_PAEG_REMOVE = "page/destroy";
    private static final String API_PAEG_SEARCH = "page/search";
    private static final String API_PAEG_FOLLOW = "page/follow";
    private static final String API_PAEG_UNFOLLOW = "page/unfollow";
    private static final String API_PAEG_CIRCLE_AS_PAGE = "page/create_from";
    private static final String API_PAGE_EVENTS = "page/events";
    
    private static final String API_OBJECT_SEARCH = "objects/search";
    
    private static final String API_CATEGORY_CREATE = "categorytype/create";
    private static final String API_CATEGORY_UPDATE = "categorytype/update";
    private static final String API_CATEGORY_DESTROY = "categorytype/destroy";
    
    
    
    public static Twitter newInstance(Configuration conf, Authorization auth) {
        return new Twitter(conf, auth);
    }

    private Twitter(Configuration conf) {
        super(conf);
    }


    /**
     * Creates an unauthenticated Twitter instance
     *
     * @deprecated use {@link TwitterFactory#getInstance()} instead
     */
    private Twitter() {
        super(ConfigurationContext.getInstance());
    }

    /**
     * Creates a Twitter instance with supplied id
     *
     * @param screenName the screen name of the user
     * @param password   the password of the user
     * @deprecated use {@link TwitterFactory#getInstance(String,String)} instead
     */
    private Twitter(String screenName, String password) {
        super(ConfigurationContext.getInstance(), screenName, password);
    }
    /*package*/

    private Twitter(Configuration conf, String screenName, String password) {
        super(conf, screenName, password);
    }
    /*package*/

    private Twitter(Configuration conf, Authorization auth) {
        super(conf, auth);
    }

    private HttpParameter[] mergeParameters(HttpParameter[] params1, HttpParameter[] params2) {
        if (null != params1 && null != params2) {
            HttpParameter[] params = new HttpParameter[params1.length + params2.length];
            System.arraycopy(params1, 0, params, 0, params1.length);
            System.arraycopy(params2, 0, params, params1.length, params2.length);
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (null != params1) {
            return params1;
        } else {
            return params2;
        }
    }

    /**
     * Returns authenticating user's screen name.<br>
     * This method may internally call verifyCredentials() on the first invocation if<br>
     * - this instance is authenticated by Basic and email address is supplied instead of screen name, or
     * - this instance is authenticated by OAuth.<br>
     * Note that this method returns a transiently cached (will be lost upon serialization) screen name while it is possible to change a user's screen name.<br>
     *
     * @return the authenticating screen name
     * @throws TwitterException      when verifyCredentials threw an exception.
     * @throws IllegalStateException if no credentials are supplied. i.e.) this is an anonymous Twitter instance
     * @since Twitter4J 2.1.1
     */
    public String getScreenName() throws TwitterException, IllegalStateException {
        if (!auth.isEnabled()) {
            throw new IllegalStateException(
                    "Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
        }
        if (null == screenName) {
            if (auth instanceof BasicAuthorization) {
                screenName = ((BasicAuthorization) auth).getUserId();
                if (-1 != screenName.indexOf("@")) {
                    screenName = null;
                }
            }
            if (null == screenName) {
                // retrieve the screen name if this instance is authenticated with OAuth or email address
                verifyCredentials();
            }
        }
        return screenName;
    }

    /**
     * Returns authenticating user's user id.<br>
     * This method may internally call verifyCredentials() on the first invocation if<br>
     * - this instance is authenticated by Basic and email address is supplied instead of screen name, or
     * - this instance is authenticated by OAuth.<br>
     *
     * @return the authenticating user's id
     * @throws TwitterException      when verifyCredentials threw an exception.
     * @throws IllegalStateException if no credentials are supplied. i.e.) this is an anonymous Twitter instance
     * @since Twitter4J 2.1.1
     */
    public int getId() throws TwitterException, IllegalStateException {
        if (!auth.isEnabled()) {
            throw new IllegalStateException(
                    "Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
        }
        if (0 == id) {
            verifyCredentials();
        }
        // retrieve the screen name if this instance is authenticated with OAuth or email address
        return id;
    }


    /**
     * {@inheritDoc}
     */
    public QueryResult search(Query query) throws TwitterException {
        try {
            return new QueryResultJSONImpl(http.get(conf.getSearchBaseURL() + "search.json", query.asHttpParameterArray(), null));
        } catch (TwitterException te) {
            if (404 == te.getStatusCode()) {
                return new QueryResultJSONImpl(query);
            } else {
                throw te;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Trends getTrends() throws TwitterException {
        return TrendsJSONImpl.createTrends(http.get(conf.getSearchBaseURL() + "trends.json"));
    }

    /**
     * {@inheritDoc}
     */
    public Trends getCurrentTrends() throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL() + "trends/current.json")).get(0);
    }

    /**
     * {@inheritDoc}
     */
    public Trends getCurrentTrends(boolean excludeHashTags) throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL() + "trends/current.json"
                + (excludeHashTags ? "?exclude=hashtags" : ""))).get(0);
    }

    /**
     * {@inheritDoc}
     */
    public List<Trends> getDailyTrends() throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL() + "trends/daily.json"));
    }

    /**
     * {@inheritDoc}
     */
    public List<Trends> getDailyTrends(Date date, boolean excludeHashTags) throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL()
                + "trends/daily.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : "")));
    }

    private String toDateStr(Date date) {
        if (null == date) {
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * {@inheritDoc}
     */
    public List<Trends> getWeeklyTrends() throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL()
                + "trends/weekly.json"));
    }

    /**
     * {@inheritDoc}
     */
    public List<Trends> getWeeklyTrends(Date date, boolean excludeHashTags) throws TwitterException {
        return TrendsJSONImpl.createTrendsList(http.get(conf.getSearchBaseURL()
                + "trends/weekly.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : "")));
    }

    /* Status Methods */

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getPublicTimeline() throws
            TwitterException {
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() +
                "statuses/public_timeline.json?include_rts=" + conf.isIncludeRTsEnabled(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getHomeTimeline() throws
            TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + "statuses/home_timeline.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getHomeTimeline(Paging paging) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + "statuses/home_timeline.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFriendsTimeline() throws
            TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + "statuses/friends_timeline.json?include_rts=" + conf.isIncludeRTsEnabled(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFriendsTimeline(Paging paging) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/friends_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("include_rts", conf.isIncludeRTsEnabled())}
                        , paging.asPostParameterArray()), auth));

    }


    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline(String screenName, Paging paging)
            throws TwitterException {
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("screen_name", screenName)
                        , new HttpParameter("include_rts", conf.isIncludeRTsEnabled())
                        , new HttpParameter("include_entities", "true")}
                        , paging.asPostParameterArray()), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline(int userId, Paging paging)
            throws TwitterException {
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("user_id", userId)
                        , new HttpParameter("include_rts", conf.isIncludeRTsEnabled())
                        , new HttpParameter("include_entities", "true")}
                        , paging.asPostParameterArray()), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline(String screenName) throws TwitterException {
        return getUserTimeline(screenName, new Paging());
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline(int userId) throws TwitterException {
        return getUserTimeline(userId, new Paging());
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline() throws
            TwitterException {
        return getUserTimeline(new Paging());
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserTimeline(Paging paging) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() +
                "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("include_rts", conf.isIncludeRTsEnabled())}
                        , paging.asPostParameterArray()), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getMentions() throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() +
                "statuses/mentions.json?include_rts=" + conf.isIncludeRTsEnabled(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getMentions(Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/mentions.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("include_rts", conf.isIncludeRTsEnabled())}
                        , paging.asPostParameterArray()), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetedByMe() throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweeted_by_me.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetedByMe(Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweeted_by_me.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetedToMe() throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweeted_to_me.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetedToMe(Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() +
                "statuses/retweeted_to_me.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetsOfMe() throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweets_of_me.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweetsOfMe(Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweets_of_me.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> getRetweetedBy(long statusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL()
                + "statuses/" + statusId + "/retweeted_by.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> getRetweetedBy(long statusId, Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL()
                + "statuses/" + statusId + "/retweeted_by.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getRetweetedByIDs(long statusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return IDsJSONImpl.getBlockIDs(http.get(conf.getRestBaseURL()
                + "statuses/" + statusId + "/retweeted_by/ids.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getRetweetedByIDs(long statusId, Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return IDsJSONImpl.getBlockIDs(http.get(conf.getRestBaseURL()
                + "statuses/" + statusId + "/retweeted_by/ids.json",paging.asPostParameterArray(), auth));    
    }

    /**
     * {@inheritDoc}
     */
    public Status showStatus(long id) throws TwitterException {
        return new StatusJSONImpl(http.get(conf.getRestBaseURL() + "statuses/show/" + id + ".json",
                new HttpParameter[] { new HttpParameter("include_entities", "true") },
                auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status updateStatus(String status) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "statuses/update.json",
                new HttpParameter[]{new HttpParameter("status", status), new HttpParameter("source", conf.getSource())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status updateStatus(String status, GeoLocation location) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "statuses/update.json",
                new HttpParameter[]{new HttpParameter("status", status),
                        new HttpParameter("lat", location.getLatitude()),
                        new HttpParameter("long", location.getLongitude()),
                        new HttpParameter("source", conf.getSource())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status updateStatus(String status, long inReplyToStatusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "statuses/update.json",
                new HttpParameter[]{new HttpParameter("status", status), new HttpParameter("in_reply_to_status_id", inReplyToStatusId), new HttpParameter("source", conf.getSource())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status updateStatus(String status, long inReplyToStatusId
            , GeoLocation location) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "statuses/update.json",
                new HttpParameter[]{new HttpParameter("status", status),
                        new HttpParameter("lat", location.getLatitude()),
                        new HttpParameter("long", location.getLongitude()),
                        new HttpParameter("in_reply_to_status_id", inReplyToStatusId),
                        new HttpParameter("source", conf.getSource())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status updateStatus(StatusUpdate latestStatus) throws TwitterException{
        ensureAuthorizationEnabled();
        HttpParameter[] array = latestStatus.asHttpParameterArray();
        HttpParameter[] combined = new HttpParameter[array.length + 1];
        System.arraycopy(array, 0, combined, 0, array.length);
        combined[combined.length - 1] = new HttpParameter("source", conf.getSource());
        return new StatusJSONImpl(http.post(conf.getRestBaseURL()
                + "statuses/update.json", combined, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status destroyStatus(long statusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL()
                + "statuses/destroy/" + statusId + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status retweetStatus(long statusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "statuses/retweet/" + statusId + ".json",
                new HttpParameter[]{new HttpParameter("source", conf.getSource())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getRetweets(long statusId) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "statuses/retweets/" + statusId + ".json?count=100", auth));
    }

    /* User Methods */

    /**
     * {@inheritDoc}
     */
    public User showUser(String screenName) throws TwitterException {
        return new UserJSONImpl(http.get(conf.getRestBaseURL() + "users/show.json?screen_name="
                + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User showUser(int userId) throws TwitterException {
        return new UserJSONImpl(http.get(conf.getRestBaseURL() + "users/show.json?user_id="
                + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> lookupUsers(String[] screenNames) throws TwitterException {
        ensureAuthorizationEnabled();
        StringBuffer buf = new StringBuffer(screenNames.length * 8);
        for (String screenName : screenNames) {
            if (buf.length() != 0) {
                buf.append(",");
            }
            buf.append(screenName);
        }
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL() +
                "users/lookup.json", new HttpParameter[]{
                new HttpParameter("screen_name", buf.toString())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> lookupUsers(int[] ids) throws TwitterException {
        ensureAuthorizationEnabled();
        StringBuffer buf = new StringBuffer(ids.length * 8);
        for (int id : ids) {
            if (buf.length() != 0) {
                buf.append(",");
            }
            buf.append(id);
        }
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL() +
                "users/lookup.json", new HttpParameter[]{
                new HttpParameter("user_id", buf.toString())}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> searchUsers(String query, int page) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL() +
                "users/search.json", new HttpParameter[]{
                new HttpParameter("q", query),
                new HttpParameter("per_page", 20),
                new HttpParameter("page", page)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Category> getSuggestedUserCategories() throws TwitterException {
        return CategoryJSONImpl.createCategoriesList(http.get(conf.getRestBaseURL() +
                "users/suggestions.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> getUserSuggestions(String categorySlug) throws TwitterException {
        HttpResponse res = http.get(conf.getRestBaseURL() + "users/suggestions/"
                + categorySlug + ".json", auth);
        try {
            return UserJSONImpl.createUserList(res.asJSONObject().getJSONArray("users"), res);
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses() throws TwitterException {
        return getFriendsStatuses(-1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses(long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/friends.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses(String screenName) throws TwitterException {
        return getFriendsStatuses(screenName, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses(int userId) throws TwitterException {
        return getFriendsStatuses(userId, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses(String screenName, long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/friends.json?screen_name=" + screenName + "&cursor="
                + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFriendsStatuses(int userId, long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/friends.json?user_id=" + userId + "&cursor=" + cursor
                , null, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses() throws TwitterException {
        return getFollowersStatuses(-1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses(long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/followers.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses(String screenName) throws TwitterException {
        return getFollowersStatuses(screenName, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses(int userId) throws TwitterException {
        return getFollowersStatuses(userId, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses(String screenName, long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/followers.json?screen_name=" + screenName
                + "&cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getFollowersStatuses(int userId, long cursor) throws TwitterException {
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL()
                + "statuses/followers.json?user_id=" + userId + "&cursor=" + cursor, auth));
    }

    /*List Methods*/

    /**
     * {@inheritDoc}
     */
    public UserList createUserList(String listName, boolean isPublicList, String description) throws TwitterException {
        ensureAuthorizationEnabled();
        List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
        httpParams.add(new HttpParameter("name", listName));
        httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
        if (description != null) {
            httpParams.add(new HttpParameter("description", description));
        }
        return new UserListJSONImpl(http.post(conf.getRestBaseURL() + getScreenName() +
                "/lists.json",
                httpParams.toArray(new HttpParameter[httpParams.size()]),
                auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList updateUserList(int listId, String newListName, boolean isPublicList, String newDescription) throws TwitterException {
        ensureAuthorizationEnabled();
        List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
        if (newListName != null) {
            httpParams.add(new HttpParameter("name", newListName));
        }
        httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
        if (newDescription != null) {
            httpParams.add(new HttpParameter("description", newDescription));
        }
        return new UserListJSONImpl(http.post(conf.getRestBaseURL() + getScreenName() + "/lists/"
                + listId + ".json", httpParams.toArray(new HttpParameter[httpParams.size()]), auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<UserList> getUserLists(String listOwnerScreenName, long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserListJSONImpl.createUserListList(http.get(conf.getRestBaseURL() +
                listOwnerScreenName + "/lists.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList showUserList(String listOwnerScreenName, int id) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.get(conf.getRestBaseURL() + listOwnerScreenName + "/lists/"
                + id + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList destroyUserList(int listId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.delete(conf.getRestBaseURL() + getScreenName() +
                "/lists/" + listId + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getUserListStatuses(String listOwnerScreenName, int id, Paging paging) throws TwitterException {
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + listOwnerScreenName +
                "/lists/" + id + "/statuses.json", paging.asPostParameterArray(Paging.SMCP, Paging.PER_PAGE), auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<UserList> getUserListMemberships(String listOwnerScreenName, long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserListJSONImpl.createUserListList(http.get(conf.getRestBaseURL() +
                listOwnerScreenName + "/lists/memberships.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<UserList> getUserListSubscriptions(String listOwnerScreenName, long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserListJSONImpl.createUserListList(http.get(conf.getRestBaseURL() +
                listOwnerScreenName + "/lists/subscriptions.json?cursor=" + cursor, auth));
    }

    /*List Members Methods*/

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getUserListMembers(String listOwnerScreenName, int listId
            , long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL() +
                listOwnerScreenName + "/" + listId + "/members.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList addUserListMember(int listId, int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.post(conf.getRestBaseURL() + getScreenName() +
                "/" + listId + "/members.json?id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList deleteUserListMember(int listId, int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.delete(conf.getRestBaseURL() + getScreenName() +
                "/" + listId + "/members.json?id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User checkUserListMembership(String listOwnerScreenName, int listId, int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.get(conf.getRestBaseURL() + listOwnerScreenName + "/" + listId
                + "/members/" + userId + ".json", auth));
    }

    /*List Subscribers Methods*/

    /**
     * {@inheritDoc}
     */
    public PagableResponseList<User> getUserListSubscribers(String listOwnerScreenName
            , int listId, long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createPagableUserList(http.get(conf.getRestBaseURL() +
                listOwnerScreenName + "/" + listId + "/subscribers.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList subscribeUserList(String listOwnerScreenName, int listId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.post(conf.getRestBaseURL() + listOwnerScreenName +
                "/" + listId + "/subscribers.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public UserList unsubscribeUserList(String listOwnerScreenName, int listId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserListJSONImpl(http.delete(conf.getRestBaseURL() + listOwnerScreenName +
                "/" + listId + "/subscribers.json?id=" + verifyCredentials().getId(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public User checkUserListSubscription(String listOwnerScreenName, int listId, int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.get(conf.getRestBaseURL() + listOwnerScreenName + "/" + listId
                + "/subscribers/" + userId + ".json", auth));
    }

    /*Direct Message Methods */

    /**
     * {@inheritDoc}
     */
    public ResponseList<DirectMessage> getDirectMessages() throws TwitterException {
        ensureAuthorizationEnabled();
        return DirectMessageJSONImpl.createDirectMessageList(http.get(conf.getRestBaseURL() + "direct_messages.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
        ensureAuthorizationEnabled();
        return DirectMessageJSONImpl.createDirectMessageList(http.get(conf.getRestBaseURL()
                + "direct_messages.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<DirectMessage> getSentDirectMessages() throws
            TwitterException {
        ensureAuthorizationEnabled();
        return DirectMessageJSONImpl.createDirectMessageList(http.get(conf.getRestBaseURL() +
                "direct_messages/sent.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<DirectMessage> getSentDirectMessages(Paging paging) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return DirectMessageJSONImpl.createDirectMessageList(http.get(conf.getRestBaseURL() +
                "direct_messages/sent.json", paging.asPostParameterArray(), auth));
    }

    /**
     * {@inheritDoc}
     */
    public DirectMessage sendDirectMessage(String screenName, String text) throws TwitterException {
        ensureAuthorizationEnabled();
        return new DirectMessageJSONImpl(http.post(conf.getRestBaseURL() + "direct_messages/new.json",
                new HttpParameter[]{new HttpParameter("screen_name", screenName),
                        new HttpParameter("text", text)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public DirectMessage sendDirectMessage(int userId, String text)
            throws TwitterException {
        ensureAuthorizationEnabled();
        return new DirectMessageJSONImpl(http.post(conf.getRestBaseURL() + "direct_messages/new.json",
                new HttpParameter[]{new HttpParameter("user_id", userId),
                        new HttpParameter("text", text)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public DirectMessage destroyDirectMessage(int id) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return new DirectMessageJSONImpl(http.post(conf.getRestBaseURL() +
                "direct_messages/destroy/" + id + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public User createFriendship(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/create.json?screen_name=" + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User createFriendship(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/create.json?user_id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User createFriendship(String screenName, boolean follow) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/create.json?screen_name=" + screenName
                + "&follow=" + follow, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User createFriendship(int userId, boolean follow) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/create.json?user_id=" + userId
                + "&follow=" + follow, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User destroyFriendship(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/destroy.json?screen_name="
                + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User destroyFriendship(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "friendships/destroy.json?user_id="
                + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public boolean existsFriendship(String userA, String userB) throws TwitterException {
        return -1 != http.get(conf.getRestBaseURL() + "friendships/exists.json",
                getParameterArray("user_a", userA, "user_b", userB), auth).
                asString().indexOf("true");
    }

    /**
     * {@inheritDoc}
     */
    public Relationship showFriendship(String sourceScreenName, String targetScreenName) throws TwitterException {
        return new RelationshipJSONImpl(http.get(conf.getRestBaseURL() + "friendships/show.json",
                getParameterArray("source_screen_name", sourceScreenName,
                        "target_screen_name", targetScreenName), auth));
    }

    /**
     * {@inheritDoc}
     */
    public Relationship showFriendship(int sourceId, int targetId) throws TwitterException {
        return new RelationshipJSONImpl(http.get(conf.getRestBaseURL() + "friendships/show.json",
                getParameterArray("source_id", sourceId, "target_id", targetId), auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getIncomingFriendships(long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "friendships/incoming.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getOutgoingFriendships(long cursor) throws TwitterException {
        ensureAuthorizationEnabled();
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "friendships/outgoing.json?cursor=" + cursor, auth));
    }

    /* Social Graph Methods */

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs() throws TwitterException {
        return getFriendsIDs(-1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs(long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "friends/ids.json?cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs(int userId) throws TwitterException {
        return getFriendsIDs(userId, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs(int userId, long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "friends/ids.json?user_id=" + userId +
                "&cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs(String screenName) throws TwitterException {
        return getFriendsIDs(screenName, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFriendsIDs(String screenName, long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "friends/ids.json?screen_name=" + screenName
                + "&cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs() throws TwitterException {
        return getFollowersIDs(-1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs(long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "followers/ids.json?cursor=" + cursor
                , auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs(int userId) throws TwitterException {
        return getFollowersIDs(userId, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs(int userId, long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "followers/ids.json?user_id=" + userId
                + "&cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs(String screenName) throws TwitterException {
        return getFollowersIDs(screenName, -1l);
    }

    /**
     * {@inheritDoc}
     */
    public IDs getFollowersIDs(String screenName, long cursor) throws TwitterException {
        return IDsJSONImpl.getFriendsIDs(http.get(conf.getRestBaseURL() + "followers/ids.json?screen_name="
                + screenName + "&cursor=" + cursor, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User verifyCredentials() throws TwitterException {
        User user = new UserJSONImpl(http.get(conf.getRestBaseURL() + "account/verify_credentials.json"
                , auth));
        this.screenName = user.getScreenName();
        this.id = user.getId();
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public User updateProfile(String name, String email, String url
            , String location, String description) throws TwitterException {
        ensureAuthorizationEnabled();
        List<HttpParameter> profile = new ArrayList<HttpParameter>(5);
        addParameterToList(profile, "name", name);
        addParameterToList(profile, "email", email);
        addParameterToList(profile, "url", url);
        addParameterToList(profile, "location", location);
        addParameterToList(profile, "description", description);
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "account/update_profile.json"
                , profile.toArray(new HttpParameter[profile.size()]), auth));
    }

    /**
     * {@inheritDoc}
     */
    public RateLimitStatus getRateLimitStatus() throws TwitterException {
        return RateLimitStatusJSONImpl.createFromJSONResponse(http.get(conf.getRestBaseURL() + "account/rate_limit_status.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public User updateDeliveryDevice(Device device) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "account/update_delivery_device.json", new HttpParameter[]{new HttpParameter("device", device.getName())}, auth));
    }


    /**
     * {@inheritDoc}
     */
    public User updateProfileColors(
            String profileBackgroundColor,
            String profileTextColor,
            String profileLinkColor,
            String profileSidebarFillColor,
            String profileSidebarBorderColor)
            throws TwitterException {
        ensureAuthorizationEnabled();
        List<HttpParameter> colors = new ArrayList<HttpParameter>(5);
        addParameterToList(colors, "profile_background_color"
                , profileBackgroundColor);
        addParameterToList(colors, "profile_text_color"
                , profileTextColor);
        addParameterToList(colors, "profile_link_color"
                , profileLinkColor);
        addParameterToList(colors, "profile_sidebar_fill_color"
                , profileSidebarFillColor);
        addParameterToList(colors, "profile_sidebar_border_color"
                , profileSidebarBorderColor);
        return new UserJSONImpl(http.post(conf.getRestBaseURL() +
                "account/update_profile_colors.json",
                colors.toArray(new HttpParameter[colors.size()]), auth));
    }

    private void addParameterToList(List<HttpParameter> colors,
                                    String paramName, String color) {
        if (null != color) {
            colors.add(new HttpParameter(paramName, color));
        }
    }

    /**
     * {@inheritDoc}
     */
    public User updateProfileImage(File image) throws TwitterException {
        checkFileValidity(image);
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL()
                + "account/update_profile_image.json",
                new HttpParameter[]{new HttpParameter("image", image)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User updateProfileBackgroundImage(File image, boolean tile)
            throws TwitterException {
        ensureAuthorizationEnabled();
        checkFileValidity(image);
        return new UserJSONImpl(http.post(conf.getRestBaseURL()
                + "account/update_profile_background_image.json",
                new HttpParameter[]{new HttpParameter("image", image),
                        new HttpParameter("tile", tile)}, auth));
    }

    /**
     * Check the existence, and the type of the specified file.
     *
     * @param image image to be uploaded
     * @throws TwitterException when the specified file is not found (FileNotFoundException will be nested)
     *                          , or when the specified file object is not representing a file(IOException will be nested).
     */
    private void checkFileValidity(File image) throws TwitterException {
        if (!image.exists()) {
            //noinspection ThrowableInstanceNeverThrown
            throw new TwitterException(new FileNotFoundException(image + " is not found."));
        }
        if (!image.isFile()) {
            //noinspection ThrowableInstanceNeverThrown
            throw new TwitterException(new IOException(image + " is not a file."));
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFavorites() throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL()
                + "favorites.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFavorites(int page) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + "favorites.json"
                , new HttpParameter[]{new HttpParameter("page", page)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFavorites(String id) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() +
                "favorites/" + id + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Status> getFavorites(String id, int page) throws TwitterException {
        ensureAuthorizationEnabled();
        return StatusJSONImpl.createStatusList(http.get(conf.getRestBaseURL() + "favorites/" + id + ".json",
                getParameterArray("page", page), auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status createFavorite(long id) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "favorites/create/" + id + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public Status destroyFavorite(long id) throws TwitterException {
        ensureAuthorizationEnabled();
        return new StatusJSONImpl(http.post(conf.getRestBaseURL() + "favorites/destroy/" + id + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public User enableNotification(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "notifications/follow.json?screen_name=" + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User enableNotification(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "notifications/follow.json?userId=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User disableNotification(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "notifications/leave.json?screen_name=" + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User disableNotification(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "notifications/leave.json?user_id=" + userId, auth));
    }

    /* Block Methods */

    /**
     * {@inheritDoc}
     */
    public User createBlock(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "blocks/create.json?screen_name=" + screenName, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User createBlock(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "blocks/create.json?user_id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User destroyBlock(String screen_name) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "blocks/destroy.json?screen_name=" + screen_name, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User destroyBlock(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "blocks/destroy.json?user_id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public boolean existsBlock(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        try {
            // @todo this method looks to be always returning false as it's expecting an XML format.
            return -1 == http.get(conf.getRestBaseURL() + "blocks/exists.json?screen_name=" + screenName, auth).
                    asString().indexOf("<error>You are not blocking this user.</error>");
        } catch (TwitterException te) {
            if (te.getStatusCode() == 404) {
                return false;
            }
            throw te;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean existsBlock(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        try {
            return -1 == http.get(conf.getRestBaseURL() + "blocks/exists.json?user_id=" + userId, auth).
                    asString().indexOf("<error>You are not blocking this user.</error>");
        } catch (TwitterException te) {
            if (te.getStatusCode() == 404) {
                return false;
            }
            throw te;
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> getBlockingUsers() throws
            TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL() +
                "blocks/blocking.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<User> getBlockingUsers(int page) throws
            TwitterException {
        ensureAuthorizationEnabled();
        return UserJSONImpl.createUserList(http.get(conf.getRestBaseURL() +
                "blocks/blocking.json?page=" + page, auth));
    }

    /**
     * {@inheritDoc}
     */
    public IDs getBlockingUsersIDs() throws TwitterException {
        ensureAuthorizationEnabled();
        return IDsJSONImpl.getBlockIDs(http.get(conf.getRestBaseURL() + "blocks/blocking/ids.json", auth));
    }

    /* Spam Reporting Methods */

    /**
     * {@inheritDoc}
     */
    public User reportSpam(int userId) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "report_spam.json?user_id=" + userId, auth));
    }

    /**
     * {@inheritDoc}
     */
    public User reportSpam(String screenName) throws TwitterException {
        ensureAuthorizationEnabled();
        return new UserJSONImpl(http.post(conf.getRestBaseURL() + "report_spam.json?screen_name=" + screenName, auth));
    }

    /* Saved Searches Methods */

    /**
     * {@inheritDoc}
     */
    public List<SavedSearch> getSavedSearches() throws TwitterException {
        ensureAuthorizationEnabled();
        return SavedSearchJSONImpl.createSavedSearchList(http.get(conf.getRestBaseURL() + "saved_searches.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public SavedSearch showSavedSearch(int id) throws TwitterException {
        ensureAuthorizationEnabled();
        return new SavedSearchJSONImpl(http.get(conf.getRestBaseURL() + "saved_searches/show/" + id
                + ".json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public SavedSearch createSavedSearch(String query) throws TwitterException {
        ensureAuthorizationEnabled();
        return new SavedSearchJSONImpl(http.post(conf.getRestBaseURL() + "saved_searches/create.json"
                , new HttpParameter[]{new HttpParameter("query", query)}, auth));
    }

    /**
     * {@inheritDoc}
     */
    public SavedSearch destroySavedSearch(int id) throws TwitterException {
        ensureAuthorizationEnabled();
        return new SavedSearchJSONImpl(http.post(conf.getRestBaseURL()
                + "saved_searches/destroy/" + id + ".json", auth));
    }
    /* Local Trends Methods */

    /**
     * {@inheritDoc}
     */
    public ResponseList<Location> getAvailableTrends() throws TwitterException {
        return LocationJSONImpl.createLocationList(http.get(conf.getRestBaseURL()
                + "trends/available.json", auth));
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Location> getAvailableTrends(GeoLocation location) throws TwitterException {
        return LocationJSONImpl.createLocationList(http.get(conf.getRestBaseURL()
                + "trends/available.json",
                new HttpParameter[]{new HttpParameter("lat", location.getLatitude())
                        ,new HttpParameter("long", location.getLongitude())
                }, auth));
    }

    /**
     * {@inheritDoc}
     */
    public Trends getLocationTrends(int woeid) throws TwitterException {
        try {
            HttpResponse res = http.get(conf.getRestBaseURL()
                    + "trends/" + woeid + ".json", auth);
            return TrendsJSONImpl.createTrends(res.asJSONArray().getJSONObject(0), res);
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    /* Geo Methods */

    /**
     * {@inheritDoc}
     */
    public ResponseList<Place> getNearbyPlaces(GeoQuery query) throws TwitterException {
        try{
            return PlaceJSONImpl.createPlaceList(http.get(conf.getRestBaseURL()
                    + "geo/nearby_places.json", query.asHttpParameterArray(), auth));
        }catch(TwitterException te){
            if(te.getStatusCode() == 404){
                return new ResponseListImpl<Place>(0, null);
            }else{
                throw te;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseList<Place> reverseGeoCode(GeoQuery query) throws TwitterException {
        try{
            return PlaceJSONImpl.createPlaceList(http.get(conf.getRestBaseURL()
                    + "geo/reverse_geocode.json", query.asHttpParameterArray(), auth));
        }catch(TwitterException te){
            if(te.getStatusCode() == 404){
                return new ResponseListImpl<Place>(0, null);
            }else{
                throw te;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Place getGeoDetails(String id) throws TwitterException {
        return new PlaceJSONImpl(http.get(conf.getRestBaseURL() + "geo/id/" + id
                + ".json", auth));
    }

    /* Help Methods */

    /**
     * {@inheritDoc}
     */
    public boolean test() throws TwitterException {
        return -1 != http.get(conf.getRestBaseURL() + "help/test.json").
                asString().indexOf("ok");
    }


    @Override
    public String toString() {
        return "Twitter{" +
                "auth='" + auth + '\'' +
                '}';
    }


    public AccessToken authLogin(String username, String pwd) throws TwitterException{
        RequestToken rt = getOAuthRequestToken();
       // Map<String,String> props = new HashMap<String,String>();
        String oauth_url = rt.getAuthorizationURL()+"&oauth_callback=json&userId="+username+"&passwd="+pwd;
        HttpResponse response = http.get(oauth_url);
        String resStr = response.asString();
       // Log.d("sns-Auhtlogin1","res Str="+resStr);
        JSONObject obj;
        String pin = "";
        try {
            obj = new JSONObject(resStr);
            pin = obj.getString("oauth_verifier");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        AccessToken accesstoken = getOAuthAccessToken(rt.getToken(), rt.getTokenSecret(), pin); 

        User user = verifyCredentials();
        
        accesstoken.screenName = user.getScreenName();
        accesstoken.userId = user.getId();
        
        return accesstoken;
    }


	public UserSession registerAccount(String username, String pwd, String nickname, String urlname, String phonenumber) throws TwitterException {
		String pwdBase64 = new String(Base64.encode(pwd.getBytes(), Base64.DEFAULT));
		HttpResponse response = http.post(getBorqsURL()+"register",new HttpParameter[]{
			    new HttpParameter("username",username),
				new HttpParameter("password",pwdBase64),
				new HttpParameter("nickname",nickname),
				new HttpParameter("screenname",urlname),
				new HttpParameter("phonenumber",phonenumber)}
		);
//	    Log.d(" register", response.asString());
	    
	    return UserSessionJSONImpl.createUserSession(response);
	}
	
	public boolean registerBorqsAccount(String username, String pwd, String nickname,boolean gender, String urlname, String phonenumber, String imei, String imsi) throws TwitterException {
		String pwdBase64 = new String(MD5.toMd5Upper(pwd.getBytes()));
		HttpResponse response = http.post(getBorqsURL()+"account/create",new HttpParameter[]{
			new HttpParameter("login_email",username),
			new HttpParameter("password",pwdBase64),
			new HttpParameter("display_name",nickname),
			new HttpParameter("gender",gender ? "m" : "f"),
//			new HttpParameter("screenname",urlname),
			new HttpParameter("appid", QiupuConfig.APP_ID_QIUPU),
			new HttpParameter("login_phone",phonenumber),
			new HttpParameter("imei",imei),
			new HttpParameter("imsi",imsi)}
		);
		
//		return BorqsUserSessionJSONImpl.createUserSession(response);
		return JsonResult.getBooleanResult(response);
	}


	public UserSession loginBorqs(String username, String pwd) throws TwitterException {
		String pwdBase64 = new String(Base64.encode(pwd.getBytes(), Base64.DEFAULT));
		HttpResponse response = http.get(getBorqsURL()+"login",
				new HttpParameter[]{new HttpParameter("username",username),
				new HttpParameter("password",pwdBase64)});
	    Log.d("login", response.asString());
	    
	    return UserSessionJSONImpl.createUserSession(response);
	}
	
	public boolean getUserPassword(String username) throws TwitterException {
		HttpResponse response = http.get(getBorqsURL()+"getPassword",
				new HttpParameter[]{new HttpParameter("username",username)});
	    
	    return UserSessionJSONImpl.getUserpassword(response);
	}
    public BorqsUserSession loginBorqsAccount(String username, String pwd) throws TwitterException {
        String pwdBase64 = MD5.toMd5(pwd.getBytes());
        pwdBase64 = pwdBase64.toUpperCase();

        HttpResponse response;
        response = http.get(getBorqsURL() + API_ACCOUNT_LOGIN,
                new HttpParameter[]{new HttpParameter("login_name", username),
                        new HttpParameter("appid", QiupuConfig.APP_ID_QIUPU),
                        new HttpParameter("password", pwdBase64)});
        Log.d("login", response.asString());

        return BorqsUserSessionJSONImpl.createUserSession(response);
    }
	
	public boolean getBorqsUserPassword(String username) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("login_name", username);
		HttpResponse response = call("account/reset_password", map, null, "", true);
		
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean updateUserPassword(String ticket, String newpassword, String oldpassword) throws TwitterException {
		String pwdBase64 = new String(MD5.toMd5Upper(newpassword.getBytes()));
		String oldpwd = new String(MD5.toMd5Upper(oldpassword.getBytes()));
		HttpResponse response = http.get(getBorqsURL()+ API_ACCOUNT_CHANGE_PASSWORD,
		new HttpParameter[]{
			new HttpParameter("ticket",ticket),
			new HttpParameter("newPassword",pwdBase64),
			new HttpParameter("oldPassword", oldpwd)});
	    
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean setGlobalApksPermission(String ticket, int visibility) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("value", String.valueOf(visibility));
		HttpResponse response = call("qiupu/user/setting", map, null, ticket, true);
	    
		return JsonResult.getBooleanResult(response);
	}
	
	public int getGlobalApksPermission(String ticket, long uid) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userId", String.valueOf(uid));
		HttpResponse response = call("qiupu/user/getsetting", map, null, ticket, true);
	    
		return JsonResult.getIntResult(response);
	}
	
	public boolean setPhoneBookPrivacy(String ticket, HashMap<String, String> privacyMap) throws TwitterException {
		HttpResponse response = call("privacy/set", privacyMap, null, ticket, true);
	    
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean setApkPermission(String ticket, String packageName, int visibility) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("value", String.valueOf(visibility));
		map.put("app", packageName);
		HttpResponse response = call("qiupu/app/setting", map, null, ticket, true);
		
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean logoutAccount(String ticket) throws TwitterException {
	    Log.d(TAG, "logoutBorqs enter");
		HttpResponse response = http.get(getBorqsURL()+ API_ACCOUNT_LOGOUT,
				new HttpParameter[]{new HttpParameter("ticket",ticket)});
	    Log.d(TAG, "logoutBorqs response:"+response.asString());
	    
	    return JsonResult.getBooleanResult(response);
	}

	public UserSession verifyAccountRegister(String username, String verifycode) throws TwitterException {
		String verifyStr = username+"&"+verifycode;		
		verifyStr = new String(Base64.encode(verifyStr.getBytes(), Base64.DEFAULT));
		Log.d(TAG, "verifyAccountRegister str:"+verifyStr);
		HttpResponse response = http.get(getBorqsURL()+"verify",
				new HttpParameter[]{new HttpParameter("verifystr",verifyStr),
				new HttpParameter("type","1")});
		
	    Log.d(TAG, "verifyAccountRegister response:"+ response.asString());
	    
	    return UserSessionJSONImpl.createUserSession(response);
	}


	public BackupResponse uploadLocalFile(File file,String sessionId,String type) throws TwitterException{
		checkFileValidity(file);
		HttpResponse response = http.post(getBorqsURL()+"upload",
				new HttpParameter[]{new HttpParameter("sessionid",sessionId),
				new HttpParameter("type",type),
				new HttpParameter("file",file)});
		 Log.d(" uploadLocalFile", response.asString());
        return BackupResponseJSONImpl.createBackupResponse(response);
	}
	

	public List<BackupResponse> getBackupList(String sessionid, String type) throws TwitterException{
		
		HttpResponse response = http.get(getBorqsURL()+"backuplist",
				new HttpParameter[]{new HttpParameter("sessionid",sessionid),
				new HttpParameter("type",type)});
		 Log.d(" getBackupList", response.asString());
        return BackupResponseJSONImpl.createBackupResponseList(response);
	}

	public Object collectPhoneInfo(String mMSISDN, String mIMEI, String mIMSI,
			String mFirmwareVersion, String mModel, String mWifiMacAddress,
			String mEmail) throws TwitterException{
		HttpResponse response = http.get(getBorqsURL()+"collectphoneinfo",
				new HttpParameter[]{new HttpParameter("msisdn",mMSISDN),
			    new HttpParameter("imsi",mIMSI),
				new HttpParameter("imei",mIMEI),
				new HttpParameter("firewareversion",mFirmwareVersion),
				new HttpParameter("model",mModel),
				new HttpParameter("mwifimacaddress",mWifiMacAddress),
				new HttpParameter("email",mEmail)});
		return null;
	}
	
	public BackupResponse uploadLocalFile(File file, String sessionId,String type, String apkinfo) throws TwitterException {
		HttpResponse response = http.post(getBorqsURL()+"upload",
				new HttpParameter[]{new HttpParameter("sessionid",sessionId),
				new HttpParameter("type",type),
				new HttpParameter("apkinfo",apkinfo),
				new HttpParameter("file",file)});
		 Log.d(" uploadLocalFile", response.asString());
        //return BackupResponseJSONImpl.createBackupResponse(response);
		 return BackupResponseJSONImpl.createBackupResponse(response);
	}


	public BackupResponse backupApk(final String pkgName, final String appName, final String versionCode,
                                    final String versionName, File file, File iconFile,String ticket,
                                    final String appData, TwitterListener listener)
            throws TwitterException, FileNotFoundException{
		HttpResponse response = null;

		if(conf.isUseFTP()){
			/*
			if(res.indexOf("true") != -1)
			{
				 //jiangcs 10.12.23
			    //get ftp's arguments
			    HttpResponse ftpArgRep = http.post(getBorqsURL() + "ftpArg");
				FTPArgumentResponse ftpArgument = FTPArgumentResponseJSONImpl.createFTPArgumentResponse(ftpArgRep);

				//jiangcs 10.12.23
				//get ftp's arguments
				//upload file
				InputStream in = new FileInputStream(file);
				InputStream iconis = new FileInputStream(iconFile);
				FtpConnection ftpConn = new FtpConnection(ftpArgument.host);
				ConnectionAdapter connAdapter = new ConnectionAdapter(listener, file.length());			
				ftpConn.addConnectionListener(connAdapter);
				ftpConn.login(ftpArgument.user, ftpArgument.pass);					
				ftpConn.chdir(ftpConn.getPWD() + "/BorqsAccount");
				ftpConn.mkdir(uid);
				ftpConn.chdir(ftpConn.getPWD() + "/" + uid);
				ftpConn.mkdir("" + identifyid);
				ftpConn.chdir(ftpConn.getPWD() + "/" + identifyid);
				ftpConn.upload(file.getName(), in);
				ftpConn.disconnect();
				
				//upload icon				
				FtpConnection ftpConn1 = new FtpConnection(ftpArgument.host);				
				ftpConn1.login(ftpArgument.user, ftpArgument.pass);					
				ftpConn1.chdir(ftpConn1.getPWD() + "/BorqsAccount");
				ftpConn1.mkdir(uid);
				ftpConn1.chdir(ftpConn1.getPWD() + "/" + uid);
				ftpConn1.mkdir("" + identifyid);
				ftpConn1.chdir(ftpConn1.getPWD() + "/" + identifyid);				
				ftpConn1.upload(iconFile.getName(), iconis);				
				ftpConn1.disconnect();
			}
			response = http.get(getBorqsURL()+"backup",
					new HttpParameter[]{new HttpParameter("sessionid",sessionId),
				    new HttpParameter("uploadfile","1"),
				    new HttpParameter("identifyid",identifyid),
					new HttpParameter("apkinfo",apkinfo),
					new HttpParameter("filename",file.getName())});
					
		    */
		}
		else
		{
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("package", pkgName);
            map.put("app_name", appName);
            final String validCode = versionCode == null ? "0" : versionCode;
            map.put("version_code", validCode);
            map.put("version_name", versionName == null ? validCode : versionName);
            if (!TextUtils.isEmpty(appData)) {
                map.put("app_data", appData);
            }
//            map.put("min_sdk", "3");
//            map.put("target_sdk", "");

            HashMap<String, File> fileMap = new HashMap<String,File>();
            fileMap.put("icon", iconFile);
            fileMap.put("apk", file);
            response = call("qiupu/upload", map, fileMap, ticket, false);
        }

		Log.d("backupApk", response.asString());
		return BackupResponseJSONImpl.createBackupResponse(response);	
	}
	
	public boolean dowloadFile(String url,String filepath,String filename,long filesize,TwitterListener listener)  throws TwitterException{
		/*
		//for monitoring download progress
		http.attachListener(listener);
		try{
			if(listener != null){

			    listener.startProcess();
	        }
		    boolean result = false;
		     
		    //jiangcs 2011.01.06
		    long availableMemory = MemoryStatus.getAvailableInternalMemorySize(); 
		    if(2*filesize < availableMemory)
		    {
		    	
			    try
			    {
			    	//jiangcs 2010.12.29		    
			    	int index = url.lastIndexOf("/");
			    	String remotePath = url.substring(0, index);
			    	String fileName = url.substring(index + 1);
	//		    	String newName = "" + System.currentTimeMillis() + ".apk";
				
			    	//get ftp's arguments
			    	HttpResponse ftpArgRep = http.post(getBorqsURL() + "ftpArg");
			    	FTPArgumentResponse ftpArgument = FTPArgumentResponseJSONImpl.createFTPArgumentResponse(ftpArgRep);
				
			    	//download file			
			    	FtpConnection ftpConn = new FtpConnection(ftpArgument.host);
			    	ConnectionAdapter connAdapter = new ConnectionAdapter(listener, filesize);			
			    	ftpConn.addConnectionListener(connAdapter);
			    	ftpConn.login(ftpArgument.user, ftpArgument.pass);					
			    	ftpConn.chdir(ftpConn.getPWD() + "/BorqsAccount");
			    	ftpConn.chdir(ftpConn.getPWD() + remotePath);
			    	ftpConn.setLocalPath(filepath);
			    	ftpConn.download(fileName);
			    	ftpConn.disconnect();
			    	
			    	try {
		               String command = "chmod 777 " + filepath + fileName;
		               Log.i("chmod", "command = " + command);
		               Runtime runtime = Runtime.getRuntime();
		               Process proc = runtime.exec(command);
			    	} 
			    	catch (IOException e) {
			    		Log.i("chmod","chmod fail!!!!");
			    		e.printStackTrace();
			    	}
			    	
			    	result = true;
			    }
			    catch(Exception ex)
			    {
			    	
			    }
		    }
		    return result;
		}finally{
			http.detachListener(listener);
			Log.d(" backupApk","detach apk listener");
		}
		*/
		return false;
		   
	}
	
	
	public List<BackupRecord> getBackupRecord(String sessionid, long maxid)  throws TwitterException {
		HttpResponse response = http.post(getBorqsURL() + "backuprecord",
                new HttpParameter[]{new HttpParameter("method", "getbackuprecord"),
                        new HttpParameter("sessionid", sessionid),
                        new HttpParameter("maxid", maxid)});
		 Log.d(" getBackupRecord", response.asString());	 
		return BackupRecordJSONImpl.createBackupRecordResponse(response);
	}
	
	//zw: used just for user sync apk status on server. 
	public BackupResponse backupApkRecord(String sessionid, String apkinfo, long identifyid)  throws TwitterException {
		HttpResponse response = http.get(getBorqsURL() + "backup",
                new HttpParameter[]{
                        new HttpParameter("uploadfile", "0"),
                        new HttpParameter("sessionid", sessionid),
                        new HttpParameter("apkinfo", apkinfo),
                        new HttpParameter("identifyid", identifyid)});
		
		return BackupRecordJSONImpl.createOneBackupRecordResponse(response);
	}
	
	public List<ApkResponse> getBackupApk(String sessionid, long recordid, long maxid, String localPath)  throws TwitterException{
		
		HttpResponse response = http.post(getBorqsURL()+"backuprecord",
				new HttpParameter[]{new HttpParameter("method","getapkinfo"),
			    new HttpParameter("sessionid",sessionid),
			    new HttpParameter("recordid",recordid),
			    new HttpParameter("maxid",maxid)});
		 Log.d("getBackupRecord", response.asString());	 
		List<ApkResponse> list = ApkResponseJSONImpl.createBackupedApkResponse(response);
		
		/*
		//jiangcs 2011.01.06
		//get ftp's arguments
    	HttpResponse ftpArgRep = http.post(getBorqsURL() + "ftpArg");
    	FTPArgumentResponse ftpArgument = FTPArgumentResponseJSONImpl.createFTPArgumentResponse(ftpArgRep);
//    	String localPath = "/data/data/com.android.borqsaccount/temp/";    	 
    	
    	FtpConnection ftpConn = new FtpConnection(ftpArgument.host);
    	ftpConn.login(ftpArgument.user, ftpArgument.pass);					
    	ftpConn.chdir(ftpConn.getPWD() + "/BorqsAccount");
    	String remotePath = "";
    	if(list.size() > 0)
    	{
    		ApkResponse temp = list.get(0);
    		int index = temp.filepath.lastIndexOf("/");
    		remotePath = temp.filepath.substring(0, index);
    	}
    	ftpConn.chdir(ftpConn.getPWD() + remotePath);
    	ftpConn.setLocalPath(localPath);
    	    	
    	//download apk icons
    	for(ApkResponse apkResponse : list)
    	{
    		int idx = apkResponse.filepath.lastIndexOf("/");	    	
	    	String fileName = apkResponse.filepath.substring(idx + 1).replaceFirst(".apk", ".png");
	    	
	    	File iconFile = new File(localPath + fileName);
	    	if(!iconFile.exists())
	    	{
	    		ftpConn.download(fileName); 
	    	}
    	}
    	ftpConn.disconnect();
		*/
		return list;
	}
	
	public  UserSession verifySessionID(String sessionid) throws TwitterException{
		HttpResponse response = http.get(getBorqsURL()+"account",
				new HttpParameter[]{new HttpParameter("method","verify"),
			    new HttpParameter("sessionid",sessionid)});
		 Log.d(TAG, "verifySessionID:"+response.asString());	 
		 return UserSessionJSONImpl.createUserSession(response);
	}

    private List<ApkResponse> createAPkListResponse(final String ticket,final String uid,final String reason,final int page,final int count)throws TwitterException
    {
    	HashMap<String, String> map = new HashMap<String, String>();
		map.put("user", uid);
		map.put("reason", reason);
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
		HttpResponse response = call("qiupu/app/for",map, null, ticket, true);
		
		map = null;
		return ApkResponseJSONImpl.createBackupedApkResponse(response);
    }
	/**
	 * get Apk list which was backuped by user
	 */
	public List<ApkResponse> getApksList(final String ticket,final String uid,final String reason,final int page,final int count) throws TwitterException{
		
		return createAPkListResponse(ticket, uid, reason, page, count);
	}
	/**
	 * get latested Apk list by user
	 */
	public List<ApkResponse> getRecommendLatestedApksList(final String ticket,final int page,final int count ) throws TwitterException{
		
		 HashMap<String, String> map = new HashMap<String, String>();
	     map.put("page", String.valueOf(page));
	     map.put("count", String.valueOf(count));
	     HttpResponse response = call("qiupu/suggest",map, null, ticket, true);
	     
	     map = null;
		
		return ApkResponseJSONImpl.createBackupedApkResponse(response);
	}
	
	public ApkResponse getApkDetailInformation(String ticket, String apkid, boolean needsubversion) throws TwitterException{
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("apps", apkid);
		map.put("history_version", needsubversion==true?"true":"false");
		
		HttpResponse response = call("qiupu/app/get", map,null, ticket, true);
		return ApkResponseJSONImpl.createApkResponseList(response);
	}

	public ArrayList<QiupuUser> getUserListWithSearchName(final String ticket,final String userName,
			final String nickName, final String screenName, final int page, final int count) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username",nickName);
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
		HttpResponse response = call("account/search", map, null, ticket, true);
		
		return QiupuNewUserJSONImpl.createQiupuUserList(response);
	}

	public ArrayList<UserCircle> searchPublicCircles(final String ticket,final String circleName, final int page, final int count) throws TwitterException {
        return serachGroups(API_PUBLIC_CIRCLE_SEARCH, ticket, circleName, page, count);
    }

    public ArrayList<UserCircle> searchEvents(final String ticket, final String circleName) throws TwitterException {
        return serachGroups(API_EVENT_SEARCH, ticket, circleName, -1, -1);
    }

    private ArrayList<UserCircle> serachGroups(final String groupUrl, final String ticket,final String circleName, final int page, final int count) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name",circleName);
        if(page >= 0) {
        	map.put("page", String.valueOf(page));
        }
        if(count >= 0){
        	map.put("count", String.valueOf(count));
        }
        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return UserCircleJSONImpl.createPublicCircleList(response);
    }

	public ArrayList<QiupuSimpleUser> getInstalledUserList(final String ticket,final String packagename,final String reason, final int page, final int count) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("app", packagename);
		map.put("reason", reason);
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
		HttpResponse response = call("qiupu/user",map, null, ticket, true);
		
		return QiupuNewUserJSONImpl.createInstallUserList(response);
	}
	
	
	public Stream statusupdate(final String ticket,final long fromid,final String status,final boolean isSendPost) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("newStatus", status);
		map.put("post", String.valueOf(isSendPost));
		HttpResponse response = call("user/status/update",map, null, ticket, false);
		if(isSendPost) {
			return StreamJSONImpl.createPostResponse(response);
		}else {
			return null;
		}
	}
	
	public Stream photoShare(final String ticket, final String message, final String to_id, final File file,final String photo_id,final int postType,
			final String caption, final String appData,
			final boolean issecretly, final boolean canComment,
			final boolean canLike, final boolean canShare, final boolean isTop,
			final boolean sendEmail, final boolean sendSms, final String categoryId) throws TwitterException {
		HashMap<String, File> fileMap = null;
        
		HashMap<String, String> map = new HashMap<String, String>();
		if(!TextUtils.isEmpty(photo_id)) {
			map.put("photo_id", photo_id);
		}else {
			if(file != null) {
				checkFileValidity(file);	
				fileMap = new HashMap<String,File>();
				fileMap.put("photo_image", file);
			}
		}
		// map.put("type", String.valueOf(QiupuConfig.APK_POST));
		if (!TextUtils.isEmpty(appData)) {
			map.put(Stream.UP_APP_DATA, appData);
		}
		map.put("type", String.valueOf(postType));
		map.put("msg", message);
		if(!TextUtils.isEmpty(message)) {
			map.put("caption", message);
		}
//		map.put("apkId", "");
		map.put("mentions", to_id);
		map.put("secretly", String.valueOf(issecretly));
		map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
		map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        map.put("is_top", String.valueOf(isTop));
        map.put("send_email", String.valueOf(sendEmail));
        map.put("send_sms", String.valueOf(sendSms));
        if(!TextUtils.isEmpty(categoryId)) {
        	map.put("category_id", categoryId);
        }
        
		HttpResponse response = call("post/create", map, fileMap, ticket, false);
		return StreamJSONImpl.createPostResponse(response);
	}
	
//	private Stream createPostLinkMessage(final String ticket,final String to_id,final String message, final String url, final boolean issecretly)throws TwitterException{
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("msg",message);
//		map.put("url",url);
//		map.put("mentions", to_id);
//		map.put("secretly",String.valueOf(issecretly));
//		HttpResponse response = call("link/create", map, null, ticket, false);
//		return StreamJSONImpl.createPostResponse(response);
//	}
	
	public Stream postFeedback(final String ticket, final String message,
                               final String appData) throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(appData)) {
            map.put(Stream.UP_APP_DATA, appData);
        }

        map.put("msg",message);
        HttpResponse response = call("feedback/create", map, null, ticket, false);
        return StreamJSONImpl.createPostResponse(response);
	}
	
	public Stream postToMultiWall(final String ticket, final String to_ids,final String message,
                                  final String appData, final int postType, boolean issecretly,
                                  final boolean canComment, final boolean canLike, final boolean canShare,
                                  final boolean isTop, final boolean sendEmail, final boolean sendSms, final String categoryId)
            throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(appData)) {
            map.put(Stream.UP_APP_DATA, appData);
        }
        map.put("type", String.valueOf(postType));
        map.put("msg",message);
        map.put("apkId","");
        map.put("mentions", to_ids);
//        map.put("package", "");
        map.put("secretly",String.valueOf(issecretly));
        map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
        map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        map.put("is_top", String.valueOf(isTop));
        map.put("sendEmail", String.valueOf(sendEmail));
        map.put("sendSms", String.valueOf(sendSms));
        if(!TextUtils.isEmpty(categoryId)) {
        	map.put("category_id", categoryId);
        }
        
        HttpResponse response = call("post/create", map, null, ticket, false);
        return StreamJSONImpl.createPostResponse(response);
	}

	/**
	 * public Stream photoShare(final String ticket, final String message, final String to_id, final File file,final int postType,
            final String caption, final String appData,
            final boolean issecretly, final boolean canComment,
            final boolean canLike, final boolean canShare) throws TwitterException {
        
        Log.d("test", "file = " + file.toString());
        checkFileValidity(file);    
        HashMap<String, File> fileMap = new HashMap<String,File>();
        fileMap.put("photo_image", file);
        
        HashMap<String, String> map = new HashMap<String, String>();
        // map.put("type", String.valueOf(QiupuConfig.APK_POST));
        if (!TextUtils.isEmpty(appData)) {
            map.put(Stream.UP_APP_DATA, appData);
        }
        map.put("type", String.valueOf(postType));
        map.put("msg", message);
        map.put("apkId", "");
        map.put("mentions", to_id);
        map.put("caption", "");     
        map.put("secretly", String.valueOf(issecretly));
        map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
        map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        
        HttpResponse response = call("post/create", map, fileMap, ticket, false);
        return StreamJSONImpl.createPostResponse(response);
    }

	 */
    public Stream fileShare(final String ticket, final String to_ids,
            final String message, final String appData, final int postType,
            boolean issecretly, final boolean canComment, final boolean canLike, 
            final boolean canShare, final String summary, final String description, 
            final File file, final String file_name, final File screenShotFile, 
            final String content_type, final boolean isTop, final boolean sendEmail, final boolean sendSms, final String categoryId) throws TwitterException {

        if (file != null) {
            Log.d(TAG, "file.getPath() = " + file.getPath());
        }
        if (screenShotFile != null) {
            Log.d(TAG, "screenShotFile.getPath() = " + screenShotFile.getPath());
        }

        checkFileValidity(file);
        HashMap<String, File> fileMap = new HashMap<String, File>();
        fileMap.put("file", file);
        if (screenShotFile != null) {
            fileMap.put("screen_shot", screenShotFile);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(appData)) {
            map.put(Stream.UP_APP_DATA, appData);
        }

        map.put("type", String.valueOf(postType));
        map.put("msg", message);
        map.put("apkId", "");
        map.put("mentions", to_ids);
        // map.put("package", "");
        map.put("secretly", String.valueOf(issecretly));
        map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
        map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        map.put("is_top", String.valueOf(isTop));
        map.put("send_email", String.valueOf(sendEmail));
        map.put("send_sms", String.valueOf(sendSms));

        map.put("summary", summary);
        map.put("description", description);
        map.put("content_type", content_type);
        map.put("file_name", file_name);
        if(!TextUtils.isEmpty(categoryId)) {
        	map.put("category_id", categoryId);
        }

        HttpResponse response = call("v2/file/share", map, fileMap, ticket, false);
        return StreamJSONImpl.createPostResponse(response);
    }

	public Stream postLink(final String ticket,final long fromid,final String to_ids,final String message,
                           final String title, final String url, boolean issecretly, final boolean canComment,
                           final boolean canLike, final boolean canShare, final boolean isTop, final boolean sendEmail,
                           final boolean sendSms, final String categoryId) throws TwitterException{
//		return createPostLinkMessage(ticket,to_ids,message, url, issecretly);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("msg",message);
        if (!TextUtils.isEmpty(title)) {
            map.put("title", title);
        }
        map.put("url",url);
        map.put("mentions", to_ids);
        map.put("secretly",String.valueOf(issecretly));
        map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
        map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        map.put("is_top", String.valueOf(isTop));
        map.put("send_email", String.valueOf(sendEmail));
        map.put("send_sms", String.valueOf(sendSms));
        if(!TextUtils.isEmpty(categoryId)) {
        	map.put("category_id", categoryId);
        }
        HttpResponse response = call("link/create", map, null, ticket, false);
        return StreamJSONImpl.createPostResponse(response);
	}	
	
	public String editUserProfileImage(final String ticket,final File file) throws TwitterException{
		checkFileValidity(file);
		HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, File> fileMap = new HashMap<String,File>();
        fileMap.put("profile_image", file);
        HttpResponse response = call("account/upload_profile_image", map, fileMap, ticket, false);
        
//		return JsonResult.getBooleanResult(response);
        return QiupuNewUserJSONImpl.createProfileImageUrlRespose(response);
	}

	public boolean editPublicCircleImage(final String ticket,final long circleid, final File file) throws TwitterException{
        return editGroupImage(API_PUBLIC_CIRCLE_EDIT_IMAGE, ticket, circleid, file);
    }

    public boolean editEventImage(final String ticket,final long circleid, final File file) throws TwitterException{
        return editGroupImage(API_EVENT_EDIT_IMAGE, ticket, circleid, file);
    }

    private boolean editGroupImage(final String groupUrl, final String ticket,final long circleid, final File file) throws TwitterException{
        checkFileValidity(file);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleid));
        HashMap<String, File> fileMap = new HashMap<String,File>();
        fileMap.put("profile_image", file);
        HttpResponse response = call(groupUrl, map, fileMap, ticket, false);

        return JsonResult.getBooleanResult(response);
    }

	
	public boolean recommendFriends(final String ticket,final long touid,final String selectuid) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("touser", String.valueOf(touid));
		map.put("suggestedusers", String.valueOf(selectuid));
		HttpResponse response = call(API_SUGGEST_RECOMMEDN_USER, map, null, ticket,false);
		
		return JsonResult.getBooleanResult(response);
	}
	
	public ArrayList<QiupuUser> getFriendsBilateral(final String ticket,final long otheruid, final int page, final int count)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("user", String.valueOf(otheruid));
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));

		HttpResponse response = call(API_FRIEND_SHOW_MUTUAL, map, null, ticket, true);
		
		return QiupuNewUserJSONImpl.createQiupuUserList(response);
	}

    public HashMap<String, Integer> getRequestSummary(final String ticket) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        HttpResponse response = call("request/summary", map, null, ticket, true);
        return RequestJSONImpl.createRequestMap(response);
    }

	public ArrayList<Requests> getRequests(final String ticket,final String types)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		if(StringUtil.isValidString(types)) {
			map.put("type", types);
		}
		String url = "request/get";
        HttpResponse response = call(url,map,null, ticket, true);
		
        return RequestJSONImpl.createReqeustList(response);
	}
	
	public boolean doneRequests(final String ticket,final String requestid, final int type, final String data, final boolean isaccept)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("requests", requestid);
		if(type > 0) {
			map.put("type", String.valueOf(type));
			map.put("data", String.valueOf(data));
			map.put("accept", String.valueOf(isaccept));
		}
		String url = "request/done";
        HttpResponse response = call(url,map,null, ticket, true);
		
        return JsonResult.getBooleanResult(response);
	}
	
	public boolean gotoBind(final String ticket,final String type, final String value, final long userid)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userId", String.valueOf(userid));
		map.put(type, value);
		String url = "account/bind";
        HttpResponse response = call(url,map,null, ticket, true);
		
        return JsonResult.getBooleanResult(response);
	}

    public ArrayList<QiupuUser> getFriendsListPage(final String ticket, final long uid, final String circles, final int page, final int count, final boolean isfollowing) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        String url = "";
        if (isfollowing) {
        	//TODO used to sync public circle
        	url = API_FRIEND_SHOW_FRIEND;
//        	map.put("with_public_circles", String.valueOf(QiupuHelper.isOpenPublicCircle()));
        	map.put("with_public_circles", String.valueOf(false));// do not sync public circle people
        	map.put("in_public_circles", String.valueOf(false)); // sync friend's public circle info 
        }
        else {
        	url = API_FRIEND_SHOW_FOLLOWER;
        }

        if (!TextUtils.isEmpty(circles)) {
            map.put("circles", circles);
        }

        map.put("columns", "#full");

        map.put("user", String.valueOf(uid));
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));

        HttpResponse response = call(url, map, null, ticket, true);

        return QiupuNewUserJSONImpl.createQiupuUserList(response);
    }

    public RecieveSet getCircleReceiveSet(final String ticket, final long circleid) throws TwitterException {
        return getGroupReceiveSet(API_PUBLIC_CIRCLE_GET_RECEIVE_SET, ticket, circleid);
    }

    public RecieveSet getEventReceiveSet(final String ticket, final long circleid) throws TwitterException {
        return getGroupReceiveSet(API_EVENT_GET_RECEIVE_SET, ticket, circleid);
    }

    private RecieveSet getGroupReceiveSet(final String groupUrl, final String ticket, final long circleid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleid));

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return UserCircleJSONImpl.createRecieveSetResponse(response);
    }

    public boolean setCircleReceiveSet(final String ticket, final long circleid, final int enable, final String phone, final String email) throws TwitterException {
        return setGroupReceiveSet(API_PUBLIC_CIRCLE_SET_RECEIVE, ticket, circleid, enable, phone, email);
    }

    public boolean setEventReceiveSet(final String ticket, final long circleid, final int enable, final String phone, final String email) throws TwitterException {
        return setGroupReceiveSet(API_EVENT_SET_RECEIVE, ticket, circleid, enable, phone, email);
    }

    private boolean setGroupReceiveSet(final String groupUrl, final String ticket, final long circleid, final int enable, final String phone, final String email) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleid));
        map.put("recv_notif", String.valueOf(enable));
        if(phone != null) {
        	map.put("notif_phone", phone);
        }
        if(email != null) {
        	map.put("notif_email", email);
        }

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }


    public ArrayList<QiupuUser> getNearByPeopleListPage(final String ticket, final int page, final int count) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));

        HttpResponse response = call(API_GET_NEARBY_USERS, map, null, ticket, true);
        Log.d(TAG,  "getNearByPeopleListPage() ---->>> response = " + response);

        return QiupuNewUserJSONImpl.createQiupuUserList(response);
    }

    public ArrayList<PublicCircleRequestUser> getRequestPeople(final String ticket, final long circleId, final int status,
                                                               final int page, final int count) throws TwitterException {
        return getGroupRequestPeople(API_PUBLIC_CIRCLE_USERS, ticket, circleId, status, page, count);
    }

    public ArrayList<PublicCircleRequestUser> getEventRequestPeople(final String ticket, final long circleId, final int status,
                                                                   final int page, final int count) throws TwitterException {
        return getGroupRequestPeople(API_EVENT_USERS, ticket, circleId, status, page, count);
    }

    private ArrayList<PublicCircleRequestUser> getGroupRequestPeople(final String groupUrl, final String ticket, final long circleId, final int status,
                                                                   final int page, final int count) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleId));
        map.put("status", String.valueOf(status));
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return PublicCircleRequestUser.createRequestPeopleList(response);
    }
    
    public ArrayList<PublicCircleRequestUser> searchPublicCirclePeople(final String ticket, final long circleId, final int status, final String key,
    		final int page, final int count) throws TwitterException {
    	return searchGroupPeople(API_PUBLIC_CIRCLE_SEARCH_PEOPLE, ticket, circleId, status, key, page, count);
    }
    
    public ArrayList<PublicCircleRequestUser> SearchEventPeople(final String ticket, final long circleId, final int status, final String key,
    		final int page, final int count) throws TwitterException {
    	return searchGroupPeople(API_EVENT_SEARCH_PEOPLE, ticket, circleId, status, key, page, count);
    }
    
    private ArrayList<PublicCircleRequestUser> searchGroupPeople(final String groupUrl, final String ticket, final long circleId, final int status, final String key,
    		final int page, final int count) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("id", String.valueOf(circleId));
    	map.put("status", String.valueOf(status));
    	map.put("key", key);
    	map.put("page", String.valueOf(page));
    	map.put("count", String.valueOf(count));
    	
    	HttpResponse response = call(groupUrl, map, null, ticket, true);
    	
    	return PublicCircleRequestUser.createRequestPeopleList(response);
    }

    public ArrayList<Long> approvepublicCirclePeople(final String ticket, final long circleId, final String userIds) throws TwitterException {
        return approveGroupPeople(API_PUBLIC_CIRCLE_APPROVE, ticket, circleId, userIds);
    }

    public ArrayList<Long> approveEventPeople(final String ticket, final long circleId, final String userIds) throws TwitterException {
        return approveGroupPeople(API_EVENT_APPROVE, ticket, circleId, userIds);
    }

    private ArrayList<Long> approveGroupPeople(final String groupUrl, final String ticket, final long circleId, final String userIds) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleId));
        map.put("user_ids", userIds);

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return UserCircleJSONImpl.parsePublicCircleInviteResponse(response);
    }

    public ArrayList<Long> ignorepublicCirclePeople(final String ticket, final long circleId, final String userIds) throws TwitterException {
        return ignoreGroupPeople(API_PUBLIC_CIRCLE_IGNORE, ticket, circleId, userIds);
    }

    public ArrayList<Long> ignoreEventPeople(final String ticket, final long circleId, final String userIds) throws TwitterException {
        return ignoreGroupPeople(API_EVENT_IGNORE, ticket, circleId, userIds);
    }

    private ArrayList<Long> ignoreGroupPeople(final String groupUrl, final String ticket, final long circleId, final String userIds) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleId));
        map.put("user_ids", userIds);

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return UserCircleJSONImpl.parsePublicCircleInviteResponse(response);
    }

    public boolean deletePublicCirclePeople(final String ticket, final long circleId, final String userIds, final String admins) throws TwitterException {
        return deleteGroupPeople(API_PUBLIC_CIRCLE_REMOVE_MEMBER, ticket, circleId, userIds, admins);
    }

    public boolean deleteEventPeople(final String ticket, final long circleId, final String userIds, final String admins) throws TwitterException {
        return deleteGroupPeople(API_EVENT_REMOVE_MEMBER, ticket, circleId, userIds, admins);
    }

    private boolean deleteGroupPeople(final String groupUrl, final String ticket, final long circleId, final String userIds, final String admins) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleId));
        map.put("members", userIds);
        if(StringUtil.isValidString(admins)) {
            map.put("admins", admins);
        }
        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    public boolean grantPublicCirclePeople(final String ticket, final long circleId, final String adminIds, final String memberIds) throws TwitterException {
        return grantGroupPeople(API_PUBLIC_CIRCLE_GRANT_MEMBER, ticket, circleId, adminIds, memberIds);
    }

    public boolean grantEventPeople(final String ticket, final long circleId, final String adminIds, final String memberIds) throws TwitterException {
        return grantGroupPeople(API_EVENT_GRANT_MEMBER, ticket, circleId, adminIds, memberIds);
    }

    private boolean grantGroupPeople(final String groupUrl, final String ticket, final long circleId, final String adminIds, final String memberIds) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleId));
        map.put("admins", adminIds);
        map.put("members", memberIds);

        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

	public ArrayList<QiupuUser> getUserYouMayKnow(final String ticket,final int count, final boolean getback)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("count",String.valueOf(count));
		map.put("getback", String.valueOf(getback));
		HttpResponse response = call(API_SUGGEST_GET, map, null, ticket, true);
		
//		String url = getBorqsURL()+"peopleyoumayknow";
//		HttpResponse response = http.post(url,
//				new HttpParameter[]{
//					new HttpParameter("sessionid",sessionid),
//					new HttpParameter("maxCount",maxCount)});
		
		return QiupuNewUserJSONImpl.getUserYouMayKnow(response);
	}	
	
	
	public Set<ContactSimpleInfo> getUserFromContact(final String ticket,final String contactListjson)throws TwitterException, JSONException{
		String contactBase64 = new String(Base64.encode(contactListjson.getBytes(), Base64.NO_WRAP));
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("contactinfo", contactBase64);
		HttpResponse response = call("socialcontact/upload", map, null, ticket, false);
		
		return JsonResult.getSystemUsers(response);
	}	
	
	public List<ApkResponse> getPoolAppsList(final String ticket, final String category,final  String sort,final int page, final int count, final boolean is_get_topic)throws TwitterException{
		 if(is_get_topic)
		 {
			 return getprefecturAppsList(ticket, category, page, count);
		 }
		 else
		 {
			 return getAppsList(ticket, category, sort, page, count);
		 }
	}
	
	public List<ApkResponse> getAppsList(final String ticket, final String category,final  String sort,final int page, final int count)throws TwitterException{
		 HashMap<String, String> map = new HashMap<String, String>();
		 if(category.length()>0)
		 {
			 map.put("category", category);
		 }
		 map.put("sort",sort);
		 map.put("page", String.valueOf(page));
		 map.put("count", String.valueOf(count));
		 HttpResponse response = call("qiupu/app/all",map, null, "", true);
		 
		 return ApkResponseJSONImpl.createBackupedApkResponse(response);
	
	}
	
	public List<ApkResponse> getprefecturAppsList(final String ticket, final String category, final int page, final int count)throws TwitterException{
		 HashMap<String, String> map = new HashMap<String, String>();
		 map.put("type", category);
	     map.put("page", String.valueOf(page));
	     map.put("count", String.valueOf(count));
	     HttpResponse response = call("qiupu/app/prefectur/get",map, null, "", true);
		
		return ApkResponseJSONImpl.createBackupedApkResponse(response);
	}
	
	
	//TODO get recommend category api,Only the custom api
	public ArrayList<RecommendHeadViewItemInfo> getRecommendCategoryList(final String ticket, final boolean isSuggest)throws TwitterException{
		 HashMap<String, String> map = new HashMap<String, String>();
		 map.put("ifsuggest", String.valueOf(isSuggest));
	     HttpResponse response = call("qiupu/app/policy/get",map, null, ticket, true);
		return RecommendHeadViewItemInfoJSONImpl.createRecommendCategoryList(response);
	}
	
	public ArrayList<QiupuSimpleUser> getMasterCategoryList(final String ticket, final String sub_category, final int page, final int count)throws TwitterException{
		 HashMap<String, String> map = new HashMap<String, String>();
		 map.put("sub_category", sub_category);
		 map.put("page", String.valueOf(page));
		 map.put("count", String.valueOf(count));
	     HttpResponse response = call("qiupu/strongmen/get",map, null, ticket, true);
		return QiupuNewUserJSONImpl.createInstallUserList(response);
	}
	
	public List<ApkResponse> getSerachAppsList(final String sessionid,final String keyword, final int page,final int count)throws TwitterException{
        HashMap<String , String> map = new HashMap<String, String>();
		map.put("value", keyword);
		map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));

		HttpResponse response = call("qiupu/search", map, null, sessionid, true);
		
		return ApkResponseJSONImpl.createBackupedApkResponse(response);
	}
	
	public List<ApkResponse> getFavoritesAppsList(final String ticket,final long uid,final String reason,final int page, final int count)throws TwitterException{
		
		return createAPkListResponse(ticket, String.valueOf(uid), reason, page, count);
	}
	

	public HashMap<String, SyncResponse> syncApksStatus(final String ticket,final String apps,final boolean all)throws TwitterException{
		HashMap<String , String> map = new HashMap<String, String>();
		map.put("apps",apps);
		map.put("all", String.valueOf(all));
		
		HttpResponse response = call("qiupu/sync",map,null,ticket, false);
		
		return SyncResponseJSONImpl.createSyncResponseList(response);
	}

	public ApkResponse getApkIdInServerDB(String sessionid, String packagename, int versioncode)  throws TwitterException{
		String url = getBorqsURL()+"apks/getApkinfo";
		
		HttpResponse response = http.get(url,
				new HttpParameter[]{
					new HttpParameter("sessionid",sessionid),
					new HttpParameter("packagename",packagename),
					new HttpParameter("versioncode",versioncode)});
		
		return ApkResponseJSONImpl.createApkResponse(response);
	}

	private HttpResponse createCommentResponse(String ticket, String targetId, String referredId, String content, String commentType) throws TwitterException
	{
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("object", commentType);
		map.put("target",targetId);
		map.put("message",content);
        if (!TextUtils.isEmpty(referredId)) {
            map.put("parent_id", referredId);
        }
		return call("comment/create", map, null, ticket, true);
	}
	public Comments.Stream_Post postStreamComment(String ticket, String targetId, String referredId, String content) throws TwitterException{
//		HttpResponse response = createCommentResponse(ticket, targetId, referredId, content, QiupuConfig.TYPE_STREAM);
//		return StreamJSONImpl.createCommentResponse(response);
        return postStreamComment(ticket, targetId, referredId, content, QiupuConfig.TYPE_STREAM);
	}
	
	public Comments.Stream_Post postStreamComment(String ticket, String targetId, String referredId, String content,String type) throws TwitterException{
		HttpResponse response = createCommentResponse(ticket, targetId, referredId, content, type);
		return StreamJSONImpl.createCommentResponse(response);
	}

	public Comments.Stream_Post postApkComment(String ticket, String targetId, String referredId,String content) throws TwitterException{
		HttpResponse response = createCommentResponse(ticket, targetId, referredId, content, QiupuConfig.TYPE_APK);
		return ApkResponseJSONImpl.createApkCommentResponse(response);
	}
	
	public List<Stream_Post> getComments(String ticket,String obj_type, String objectid,int page,int count) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("object", obj_type);
		map.put("target", objectid);
		map.put("columns","");//TODO need all cols 
		map.put("page", String.valueOf(page));
		map.put("count",String.valueOf(count));
		
		HttpResponse response = call("comment/for", map, null, ticket, true);
		return StreamJSONImpl.getCommentsResponse(response);
	}
	
	public boolean deleteComments(String ticket,String obj_type, long commentId) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("object", obj_type);
		map.put("comments", String.valueOf(commentId));
		
		HttpResponse response = call("comment/destroy", map, null, ticket, true);
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean deletePost(String ticket,String postId) throws TwitterException{
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("postIds", postId);
		HttpResponse response = call("post/delete", map, null, ticket, true);
		
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean createLike(String ticket, String targetId, String type) throws TwitterException
	{
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("object", type);
		map.put("target",targetId);
		HttpResponse response = call("like/like", map, null, ticket, true);
		return JsonResult.getBooleanResult(response);
	}

	public Stream postRetweet(String ticket, String objectid, String tos, String addedContent,
                              final boolean canComment, final boolean canLike, final boolean canShare,
                              boolean privacy) throws TwitterException{
		//Log.d(TAG,"addedContent :" + addedContent);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("postId", objectid);
		map.put("to", tos);
		map.put("newmsg", addedContent);
        map.put(Stream.STREAM_PROPERTY_CAN_COMMENT, String.valueOf(canComment));
        map.put(Stream.STREAM_PROPERTY_CAN_LIKE, String.valueOf(canLike));
        map.put(Stream.STREAM_PROPERTY_CAN_RESHARE, String.valueOf(canShare));
        map.put("secretly",String.valueOf(privacy));
		HttpResponse response = call("post/repost", map, null, ticket, true);
		return StreamJSONImpl.createPostResponse(response);
	}
	
//	public boolean postApkLike(String ticket, String objectid) throws TwitterException{
//		return createLike(ticket, objectid, QiupuConfig.TYPE_APK);
//	}
//
//    public boolean postLike(String ticket, String objectid) throws TwitterException{
//
//        return createLike(ticket, objectid, QiupuConfig.TYPE_STREAM);
//    }

    public boolean deleteApps(String sessionid, String appList) throws TwitterException{
        if (TextUtils.isEmpty(appList)) {
            Log.e(TAG, "deleteApps with invalid list of app name.");
            return false;
        }

        HashMap<String, String> map = new HashMap<String, String>();
		map.put("apps", appList);

        HttpResponse response = call("qiupu/remove_app",map, null, sessionid, false);

		return JsonResult.getBooleanResult(response);
	}

    public boolean removeLike(String ticket, String targetId, String type) throws TwitterException
	{
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("object", type);
		map.put("target",targetId);
		HttpResponse response = call("like/unlike", map, null, ticket, true);
		return JsonResult.getBooleanResult(response);
	}
    
    public ArrayList<QiupuSimpleUser> getLikeUsers(String ticket, String targetId, String type, int page, int count) throws TwitterException
	{
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("object", type);
		map.put("target", targetId);
		map.put("page",   String.valueOf(page));
		map.put("count",  String.valueOf(count));
		
		HttpResponse response = call("like/users", map, null, ticket, true);
		return QiupuNewUserJSONImpl.createInstallUserList(response);
	}
    

//    public boolean postApkUnLike(String ticket, String objectid, String type) throws TwitterException{
//        return removeLike(ticket, objectid, type);
//    }
//
//	public boolean postUnLike(String ticket, String objectid, String type) throws TwitterException{
//        return removeLike(ticket, objectid, type);
//	}

    private boolean toggleFavoriteFlag(String ticket, String objectid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("app", objectid);

        HttpResponse response = call("qiupu/favorite", map, null, ticket, true);
        return JsonResult.getBooleanResult(response);
	}


	public boolean postAddFavorite(String ticket, String objectid) throws TwitterException{
        return  toggleFavoriteFlag(ticket, objectid);
    }
	
	public boolean postRemoveFavorite(String ticket, String objectid) throws TwitterException{
        return  toggleFavoriteFlag(ticket, objectid);

//		String url = getBorqsURL()+"apk_remove_favorite";
//
//		HttpResponse response = http.get(url,
//				new HttpParameter[]{
//					new HttpParameter("sessionid",       sessionid),
//					new HttpParameter("apkid",         objectid)});
//
//		return JsonResult.getBooleanResult(response);
	}
	
	public boolean inviteWithMail(String ticket, String phoneNumbers,String emails,String names, String message, boolean exchange_vcard) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("emails", emails);
		map.put("phones", phoneNumbers);
		map.put("names", names);
		map.put("message", message);
		map.put("exchange_vcard", String.valueOf(exchange_vcard));
		HttpResponse response = call("account/email_invite", map, null, ticket,true);
		return JsonResult.getBooleanResult(response);
	}

	//TODO
    // Post a 'share' stream to specific recipient.
    // Para: sessionid, a identifier string.
    //          to_id, comma-separated string addressing a recipients list.
    //          message, a comment text string
    //          filter_type, server definition about share content type, e.g, APK share is 1
    //          apk_id,
    //          package_name,
    //          privacy,
    // Examples: auto share post while install apk
    //                 manually share an Apk in library.
	public Stream postQiupuShare(String ticket, String to_id, String message, int filter_type,
			String apk_id, String package_name, boolean privacy, boolean isTop,
			boolean sendEmail, boolean sendSms, final String categoryId) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("type", String.valueOf(filter_type));
		map.put("msg",message);
		map.put("apkId",apk_id);
		map.put("mentions", to_id);
		map.put("package", package_name);
		map.put("secretly",String.valueOf(privacy));
		map.put("is_top", String.valueOf(isTop));
		map.put("send_email", String.valueOf(sendEmail));
		map.put("send_sms", String.valueOf(sendSms));
		if(!TextUtils.isEmpty(categoryId)) {
        	map.put("category_id", categoryId);
        }
		HttpResponse response = call("post/create", map, null, ticket, false);
		
		return StreamJSONImpl.createPostResponse(response);
	}

	public List<Stream> getPostTimeLine(String ticket, long userid, final long circleid, int count , String time, boolean newpost,
	        final String filter_app, final int filter_type, final long categoryId, final int fromHome) throws TwitterException{
	    HashMap<String, String> map = new HashMap<String, String>();
	    String urlSuffix; 
	    map.put(newpost ? "start_time" : "end_time", time);
	    map.put("count", String.valueOf(count));
//		map.put("type",  String.valueOf(QiupuConfig.ALL_APK_POST));
	    
	    if(circleid > 0) {
	    	userid = circleid;
	    }
	    if (userid > 0) {
	        map.put("users", String.valueOf(userid));
	        
	        if (filter_type == BpcApiUtils.ALL_TYPE_POSTS) {
	            urlSuffix = "post/qiupuusertimeline";
	            if(fromHome == QiupuConfig.FROM_HOME) {
	            	urlSuffix = "post/qiupuusertimeline_home";	
	            }
	        } else {
	            urlSuffix = "post/myshare";
	        }
	    } else {
	        if (circleid == QiupuConfig.CIRCLE_ID_ALL) {
	            urlSuffix = "post/qiupufriendtimeline";
	        } else if (circleid == QiupuConfig.CIRCLE_ID_PUBLIC) {
	            urlSuffix = "post/qiupupublictimeline";
	        } else if (circleid == QiupuConfig.CIRCLE_ID_HOT) {
	            urlSuffix = "post/hot";
	        } else if (circleid == QiupuConfig.CIRCLE_ID_NEAR_BY) {
	            urlSuffix = "post/nearby";
	        } else {
	            urlSuffix = "post/qiupufriendtimeline";
	            Log.w(TAG, "getPostTimeLine, why take me here? circle Id: " + circleid + ", user Id: " + userid);
	        }
	    }
	    
	    if (BpcApiUtils.isValidStreamType(filter_type)) {
	        map.put("type",  String.valueOf(filter_type));
	    }
	    
	    if(categoryId > 0) {
	    	map.put("category_id", String.valueOf(categoryId));
	    }
	    HttpResponse response = call(urlSuffix, map, null, ticket, true);
	    
	    return StreamJSONImpl.createPostResponseList(response);
	}
	
	public List<Stream> getPostTop(String ticket, long id) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", String.valueOf(id));

		HttpResponse response = call(API_POST_TOP_GET, map, null, ticket, true);
		
		return StreamJSONImpl.createPostResponseList(response);
	}

//	private List<Stream> getPostPublicTimeLine(final String sessionid, int limit, long time, boolean newpost,
//                                              final String filter_app, final int filter_type) throws TwitterException{
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put(newpost ? "start_time" : "end_time", String.valueOf(time));
//		map.put("count", String.valueOf(limit));
//
////        if (!StringUtil.isEmpty(filter_app)) {
////            map.put("apps", filter_app);
////        }
//
//        if (BpcApiUtils.isValidStreamType(filter_type)) {
//            map.put("type",  String.valueOf(filter_type));
//        }
//
//		HttpResponse response = call("post/qiupupublictimeline", map, null, sessionid, true);
//		return StreamJSONImpl.createPostResponseList(response);
//	}

    public QiupuUser setCircle(final String ticket, final long uid, final String circleid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("friendId", String.valueOf(uid));
        map.put("circleIds", circleid);

        HttpResponse response = call(API_CIRCLE_SET_CIRCLE, map, null, ticket, false);

        return QiupuNewUserJSONImpl.creatFriend(response);
    }

    public QiupuUser exchangeVcard(final String ticket, final long uid, final boolean send_request, final String circleid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("friendId", String.valueOf(uid));
        map.put("circleIds", circleid);
        map.put("send_request", String.valueOf(send_request));

        HttpResponse response = call(API_EXCHAGNE_VCARD, map, null, ticket, false);
        Log.d(TAG, "exchangeVcard() respones = " + response);

        return QiupuNewUserJSONImpl.creatFriend(response);
    }

    public boolean usersSet(final String ticket, final String uids, final String circleid, final boolean isadd) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put("friendIds", uids);
        map.put("circleId", circleid);
        map.put("isadd", String.valueOf(isadd));

        HttpResponse response = call(API_CIRCLE_ALTER_MEMBER, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }
	
	public boolean sendApproveRequest(final String ticket,final String uids, final String message)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("to", uids);
		map.put("message", message);
			
        HttpResponse response = call("request/profile_access_approve",map, null, ticket, true);
		
        return JsonResult.getBooleanResult(response);
	}
	
	/**
	 * get user detail information
	 * param:ticket,a identifier string.
	 *       userid,the userid which will get.
	 * return user infomation.
	 */
    public QiupuUser getUserInfo(final String ticket, final long userid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("users", String.valueOf(userid));
        map.put("columns", "#full");

        //TODO used to sync public circle
//        map.put("with_public_circles", String.valueOf(QiupuHelper.isOpenPublicCircle()));
        map.put("with_public_circles", String.valueOf(false));
        HttpResponse response = call(API_ACCOUNT_SHOW_USER, map, null, ticket, true);

        return QiupuNewUserJSONImpl.creatUsersInfo(response);
    }

    public ArrayList<QiupuUser> getLBSUsersInfo(final String ticket, final long userid) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        HttpResponse response = call(API_GET_LBS_USERS, map, null, ticket, true);
        return QiupuNewUserJSONImpl.createQiupuUserList(response);
    }

    public ArrayList<QiupuSimpleUser> getUsersInfowithIds(final String ticket,final String ids) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("users", ids);
        HttpResponse response = call(API_ACCOUNT_SHOW_USER, map, null, ticket, true);
        return QiupuNewUserJSONImpl.creatQiupuSimpleInfoList(response);
    }

    public ArrayList<UserCircle> getUserCircle(final String ticket, final long uid, final String circles, final boolean with_users) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("user", String.valueOf(uid));
    	map.put("with_members", String.valueOf(with_users));
    	map.put("circles", circles);
    	//TODO used to sync public circle
    	map.put("with_public_circles", String.valueOf(QiupuHelper.isOpenPublicCircle()));
    	map.put("columns", UserCircle.getExpendCircleColumns());
    	
    	HttpResponse response = call(API_CIRCLE_SHOW, map, null, ticket, true);
    	
    	return UserCircleJSONImpl.createUserCircleList(response);
    }

    public ArrayList<UserCircle> getCompanyCircle(final String ticket,  final String company_id) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("company",company_id);

        HttpResponse response = call(API_COMPANY_CIRCLE_SHOW, map, null, ticket, true);

        return UserCircleJSONImpl.createPublicCircleList(response);
    }
    
	public long createCircle(final String ticket,final String circleName)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", circleName);
			
        HttpResponse response = call(API_CIRCLE_CREATE, map, null, ticket,true);
		
        return UserCircleJSONImpl.createCircle(response);
	}

	public UserCircle createPulbicCircle(final String ticket,final HashMap<String, String> map)throws TwitterException{
        return createGroup(API_PUBLIC_CIRCLE_CREATE, ticket, map);
    }

    public UserCircle createEvent(final String ticket,final HashMap<String, String> map)throws TwitterException{
        return createGroup(API_EVENT_CREATE, ticket, map);
    }

    private UserCircle createGroup(final String groupUrl, final String ticket,final HashMap<String, String> map)throws TwitterException{
        HttpResponse response = call(groupUrl, map, null, ticket,false);
        return UserCircleJSONImpl.createPublicCircle(response);
	}

    public boolean editPulbicCircle(final String ticket,final HashMap<String, String> map)throws TwitterException{
        return editGroup(API_PUBLIC_CIRCLE_EDIT, ticket, map);
    }

    public boolean editEvent(final String ticket,final HashMap<String, String> map)throws TwitterException{
        return editGroup(API_EVENT_EDIT, ticket, map);
    }

	private boolean editGroup(final String groupUrl, final String ticket,final HashMap<String, String> map)throws TwitterException{
        HttpResponse response = call(groupUrl, map, null, ticket,false);
        return JsonResult.getBooleanResult(response);
	}

	public ArrayList<UserCircle> syncPublicCircles(final String ticket,  final String circleids) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ids",circleids);
		
		HttpResponse response = call(API_PUBLIC_CIRCLE_SHOW, map, null, ticket, false);
		
		return UserCircleJSONImpl.createPublicCircleList(response);
	}
	
	public ArrayList<UserCircle> syncChildCircles(final String ticket, final long circleid, final int formal) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("circle_id",String.valueOf(circleid));
		if(formal >= 0) {
			map.put("formal",String.valueOf(formal));
		}
		
		HttpResponse response = call(API_PUBLIC_CIRCLE_SUBCIRCLS_SHOW, map, null, ticket, true);
		return UserCircleJSONImpl.createPublicCircleList(response);
	}
	
	
	 
    public UserCircle syncPublicCirclInfo(final String ticket, final String circleids, final boolean with_members) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("columns", UserCircle.getCircleDetailExpendColumns());
        return syncGroupInfo(API_PUBLIC_CIRCLE_SHOW, ticket, circleids, with_members, map, UserCircle.CIRLCE_TYPE_PUBLIC);
    }

    public UserCircle syncEventInfo(final String ticket, final String circleids, final boolean with_members) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
		map.put("columns", UserCircle.getExpendColumns());
        return syncGroupInfo(API_EVENT_SHOW, ticket, circleids, with_members, map, UserCircle.CIRCLE_TYPE_EVENT);
    }

	private UserCircle syncGroupInfo(final String groupUrl, final String ticket, final String circleids, final boolean with_members, final HashMap<String, String> map, final int circletyle)throws TwitterException{
		map.put("with_members", String.valueOf(with_members));
		map.put("ids", circleids);
		
        HttpResponse response = call(groupUrl, map, null, ticket,true);
        return UserCircleJSONImpl.createOneUserCircleResponse(response, circletyle);
	}
	
	public ArrayList<UserCircle> syncEventListInfo(final String ticket, final String circleids, final boolean with_members)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("event_ids", circleids);
		map.put("with_members", String.valueOf(with_members));
		map.put("columns", UserCircle.getExpendColumns());
        HttpResponse response = call(API_EVENT_SHOW, map, null, ticket,true);
        return UserCircleJSONImpl.createEventList(response);
	}
	
	public ArrayList<UserCircle> syncCircleEventListInfo(final String ticket, final long circleid, final boolean with_members, final int page, final int count)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		String url = "";
		if(QiupuConfig.isPageId(circleid)) {	
			url = API_PAGE_EVENTS;
			map.put("page_id", String.valueOf(circleid));
		}else {
			url = API_PUBLIC_CIRCLE_EVENTS;
			map.put("circle_id", String.valueOf(circleid));
		}
		map.put("with_members", String.valueOf(with_members));
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
        HttpResponse response = call(url, map, null, ticket,true);
        return UserCircleJSONImpl.createEventList(response);
	}

	public ArrayList<PageInfo> syncPageList(final String ticket, final String pageids)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("pages", pageids);
        HttpResponse response = call(API_PAGE_SHOW, map, null, ticket,true);
        return PageInfoJSONImpl.createPageList(response);
	}
	
	
    public ArrayList<PollInfo> getUserPollList(final String ticket, final int type, final int page, final int count, final long user_id, final boolean with_items)
            throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", String.valueOf(type));
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));
        if (user_id > 0) {
            map.put("user_id", String.valueOf(user_id));
        }
        HttpResponse response = call(API_GET_USER_POLL_LIST, map, null, ticket, true);

        return PollJSONImpl.createPollList(response, with_items);
    }

    public ArrayList<PollInfo> getFriendPollList(final String ticket, final long uid, final int type, final int page, final int count, final boolean with_items)
            throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("user_id", String.valueOf(uid));
        map.put("type", String.valueOf(type));
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));
        HttpResponse response = call(API_GET_FRIEND_POLL_LIST, map, null, ticket, true);

        return PollJSONImpl.createPollList(response, with_items);
    }

    public ArrayList<PollInfo> getPublicPollList(final String ticket, final int page, final int count, final boolean with_items)
            throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));
        HttpResponse response = call(API_GET_PUBLIC_POLL_LIST, map, null, ticket, true);

        return PollJSONImpl.createPollList(response, with_items);
    }

    public PollInfo createPoll(final String ticket, final String recipient, final String title, final String description, 
            final long startTime, final long endTime, final ArrayList<String> pollItemList, final int canVoteCount, final int mode,
            final boolean canAddItem, final boolean canSendEmail, final boolean sendSms, final boolean isPrivate, final long parentId)throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();

        map.put("target", recipient);
        map.put("title", title);
        map.put("description", description);
        map.put("start_time", String.valueOf(startTime));
        map.put("end_time", String.valueOf(endTime));
        map.put("multi", String.valueOf(canVoteCount));
        map.put("mode", String.valueOf(mode));

        int can_add_items = canAddItem ? 1 : 0;
        map.put("can_add_items", String.valueOf(can_add_items));
        map.put("send_email", String.valueOf(canSendEmail));
        map.put("send_sms", String.valueOf(sendSms));
        int is_private = isPrivate ? 2 : 0;
        map.put("privacy", String.valueOf(is_private));
        
        if(parentId > 0) {
        	map.put("parent_ids", String.valueOf(parentId));
        }

        if (pollItemList != null && pollItemList.size() > 0) {
            for (int i = 0; i < pollItemList.size(); i++) {
                map.put("message" + (i + 1), pollItemList.get(i));
            }
        }

        HttpResponse response = call(API_CREATE_POLL, map, null, ticket,false);
        return PollJSONImpl.createPollInfo(response, false);
    }

    public ArrayList<PollInfo> getPollListInfo(final String ticket, final String poll_ids, final boolean with_items)throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ids", poll_ids);
        map.put("with_items", String.valueOf(with_items));
        HttpResponse response = call(API_GET_POLL, map, null, ticket,true);
        return PollJSONImpl.createPollList(response, with_items);
    }

    public PollInfo addPollItems(final String ticket, final String poll_id, final String item_ids, ArrayList<String> msgList)throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", poll_id);
        map.put("item_ids", item_ids);
        if(msgList != null && msgList.size() > 0) {
            for (int i = 0; i < msgList.size(); i++) {
                map.put("message" + (i + 1), msgList.get(i));
            }
        }
        HttpResponse response = call(API_ADD_POLL_ITEMS, map, null, ticket, true);
        return PollJSONImpl.createPollInfo(response, true);
    }

    public PollInfo vote(final String ticket, final String poll_id, final String item_ids)throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("poll_id", poll_id);
        map.put("item_ids", item_ids);
        HttpResponse response = call(API_VOTE, map, null, ticket, true);
        return PollJSONImpl.createPollInfo(response, true);
    }

    public ArrayList<Long> publicInvitePeople(final String ticket, final String circleid, final String uids, final String toNames, 
            final String message, final boolean sendEmail, final boolean sendSms) throws TwitterException {
        return inviteGroupPeople(API_PUBLIC_CIRCLE_INVITE, ticket, circleid, uids, toNames, message, sendEmail, sendSms);
    }

    public ArrayList<Long> inviteEventPeople(final String ticket, final String circleid, final String uids, final String toNames, 
            final String message) throws TwitterException {
        // server don't care send_email and send_sms value, so set false by default.
        return inviteGroupPeople(API_EVENT_INVITE, ticket, circleid, uids, toNames, message, false, false);
    }

    private ArrayList<Long> inviteGroupPeople(final String groupUrl, final String ticket, final String circleid, final String uids, 
            final String toNames, final String message, final boolean sendEmail, final boolean sendSms) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("to", uids);
        map.put("message", message);
        map.put("id", circleid);
        map.put("names", toNames);
        map.put("send_email", String.valueOf(sendEmail));
        map.put("send_sms", String.valueOf(sendSms));

        HttpResponse response = call(groupUrl, map, null, ticket, false);
        return UserCircleJSONImpl.parsePublicCircleInviteResponse(response);
	}

    public int applyInPublicCircle(final String ticket, final String circleid, final String message) throws TwitterException {
        return applyInGroup(API_PUBLIC_CIRCLE_APPLY_JOIN, ticket, circleid, message);
    }

    public int applyInEvent(final String ticket, final String circleid, final String message) throws TwitterException {
        return applyInGroup(API_EVENT_APPLY_JOIN, ticket, circleid, message);
    }

	private int applyInGroup(final String groupUrl, final String ticket,final String circleid, final String message)throws TwitterException{
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", String.valueOf(circleid));
        map.put("message", message);
        HttpResponse response = call(groupUrl, map, null, ticket,true);
        return JsonResult.getIntResult(response);
    }

	public boolean deletePoll(final String ticket, final String poll_id) throws TwitterException {
	    
	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put("ids", poll_id);
	    HttpResponse response = call(API_POLL_DESTROY, map, null, ticket, true);
	    
	    return JsonResult.getBooleanResult(response);
	}
    public boolean deleteCircle(final String ticket, final String circleId, final int type) throws TwitterException {
        if(UserCircle.CIRLCE_TYPE_PUBLIC == type) {
            return deletePublicCircle(ticket, circleId);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        String url = "";
        if(UserCircle.CIRCLE_TYPE_LOCAL == type) {
        	map.put("circles", circleId);
        	url = API_CIRCLE_REMOVE;
        }else  {
            Log.e(TAG, "deleteCircle, unexpected circle type: " + type);
        }
        HttpResponse response = call(url, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    public boolean deletePublicCircle(final String ticket, final String circleId) throws TwitterException {
        return deleteGroup(API_PUBLIC_CIRCLE_REMOVE, ticket, circleId);
    }

    public boolean deleteEvent(final String ticket, final String circleId) throws TwitterException {
        return deleteGroup(API_EVENT_REMOVE, ticket, circleId);
    }

    public boolean deleteGroup(final String groupUrl, final String ticket, final String circleId) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ids", circleId);
        HttpResponse response = call(groupUrl, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    public boolean refuseUser(final String ticket,final long uid)throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("suggested", String.valueOf(uid));
        HttpResponse response = call(API_SUGGEST_REJECT,map, null, ticket, true);
		
        return JsonResult.getBooleanResult(response);
	}
	
	public boolean updateUserInfo(final String ticket,final HashMap<String,String> columnsMap) throws TwitterException {
		HttpResponse response = call("account/update",columnsMap, null, ticket, true);
		return JsonResult.getBooleanResult(response);
	}
	
	public boolean sendChangeRequest(final String ticket,final HashMap<String,String> columnsMap) throws TwitterException {
		HttpResponse response = call("request/change_profile",columnsMap, null, ticket, true);
		return JsonResult.getBooleanResult(response);
	}
	
	public HashMap<String, String> setNotification(final String ticket,final String key, final boolean value) throws TwitterException {
		if(QiupuConfig.DBLOGD)Log.d(TAG, "coming key and value " + key + " " + value);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(key, value ? "0" : "1");
		HttpResponse response = call("preferences/set", map, null, ticket, true);
		boolean result = JsonResult.getBooleanResult(response);
		
		if(result)
		{
			HashMap<String, String> resultMap = new HashMap<String, String>();
			if(QiupuConfig.DBLOGD)Log.d(TAG, "back key and value " + key + " " + value);
			resultMap.put(key, value ? "0" : "1");
			return resultMap;
		}
		else
		{
			return null;
		}
	}
	
	public ArrayList<NotificationInfo> getNotificationValue(final String ticket,final String key) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("starts", key);
		HttpResponse response = call("preferences/get_by_starts",map, null, ticket, true);
		return NotificationJSONImpl.createnotificationList(response);
	}

    public boolean remarkSet(final String ticket, final long remarkUserid, final String remark) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("friend", String.valueOf(remarkUserid));
        map.put("remark", remark);

        HttpResponse response = call(API_REMARK_SET, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    public boolean muteObject(final String ticket, final String objectId, int type) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("target_ids", objectId);
        map.put("target_type", String.valueOf(type));

        HttpResponse response = call(API_MUTE_OBJECT, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    public boolean reportAbusedObject(final String ticket, final String objectId) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("post_id", objectId);

        HttpResponse response = call(API_REPORT_ABUSE, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }
    
    public ArrayList<QiupuAlbum> getAllAlbums(final String ticket,final long user_id,final boolean with_photo_ids) throws TwitterException{
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("user_id", String.valueOf(user_id));
    	HttpResponse response = call(API_ALBUM_ALL, map, null, ticket,true);
    	
    	return QiupuAlbumJSONImpl.createAlbumList(response);
    }
    
    public QiupuAlbum getAlbum(final String ticket,final long album_id,final long user_id,final boolean with_photo_ids) throws TwitterException{
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("album_id", String.valueOf(album_id));
    	map.put("user_id", String.valueOf(user_id));
    	HttpResponse response = call(API_ALBUM_GET, map, null, ticket,true);
    	
    	return QiupuAlbumJSONImpl.createAlbumResponse(response.asJSONObject());
    }
    
    public ArrayList<QiupuPhoto> getPhotosByAlbumId(final String ticket,final long album_ids,final int page,final int count) throws TwitterException{
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("album_ids", String.valueOf(album_ids));
        map.put("page", String.valueOf(page));
        map.put("count", String.valueOf(count));
        HttpResponse response = call(API_PHOTO_ALBUM_GET, map, null, ticket,true);
        
        return QiupuPhotoJSONImpl.createPhotoList(response);
    }
    
    public QiupuPhoto getPhotoById(final String ticket,final String photo_ids) throws TwitterException{
    	
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("photo_ids", photo_ids);
        HttpResponse response = call(API_PHOTO_GET, map, null, ticket,true);
        
        ArrayList<QiupuPhoto> photos = QiupuPhotoJSONImpl.createPhotoList(response);
        if(photos != null && photos.size()>0) {
            return photos.get(0);
        }else {
            return null;
        }
    }

    public boolean deletePhoto(final String ticket, final String photo_ids,final boolean deleteAll) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("photo_ids", photo_ids);
    	map.put("delete_all", String.valueOf(deleteAll));
    	HttpResponse response = call(API_PHOTO_DELETE, map, null, ticket, true);
    	
    	return JsonResult.getBooleanResult(response);
    }
    
    public boolean postUpdateSetting(final String ticket, final String postId, final boolean canComment, 
            final boolean canLike, final boolean canReshare) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("postId", postId);

        map.put("can_comment", String.valueOf(canComment));
        map.put("can_like", String.valueOf(canLike));
        map.put("can_reshare", String.valueOf(canReshare));

        HttpResponse response = call(API_POST_UPDATE_SETTING, map, null, ticket, true);

        return JsonResult.getBooleanResult(response);
    }

    private String generateSignature(HashMap<String, String> params, HashMap<String, File> fileMap) {
		HashSet<String> allKeySet = new HashSet<String>();
        allKeySet.addAll(params.keySet());
        if (null != fileMap) {
            allKeySet.addAll(fileMap.keySet());
        }

		TreeSet<String> sortedParamNames = new TreeSet<String>(allKeySet);
		Iterator<String> itr = sortedParamNames.iterator();
		StringBuilder sb = new StringBuilder();
	    while (itr.hasNext()){
	      sb.append(itr.next());
	    }

	    String str = QiupuConfig.APP_SECRECT_QIUPU + sb.toString() + QiupuConfig.APP_SECRECT_QIUPU;
		Log.d(TAG, "sign key tmp string :"+str);
		str = MD5.md5Base64(str.getBytes()).replace("\n", "");
        return str;
    }

    private HttpParameter[] generateHttpParameter(HashMap<String, String> params, HashMap<String, File> fileMap) {
        int allSize = params.size();
        if (null != fileMap) {
            allSize += fileMap.size();
        }

        HttpParameter[] parameter = new HttpParameter[allSize];
        int index = 0;
        Iterator iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            HttpParameter tmpParameter = new HttpParameter(key, val);
            parameter[index] = tmpParameter;
            ++index;
        }

        if (null != fileMap) {
            iter = fileMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                File val = (File) entry.getValue();
                HttpParameter tmpParameter = new HttpParameter(key, val);
                parameter[index] = tmpParameter;
                ++index;
            }
        }

//        parameter[index] = new HttpParameter(HttpParameter.CALL_ID, Long.toString(System.currentTimeMillis()));

        return parameter;
    }

    HttpResponse call(String urlSuffix, HashMap<String, String> params, HashMap<String, File> fileMap,
                              String ticket, boolean isGetMethod) throws TwitterException {
        if (null == params) {
            Log.e(TAG, "HTTP parameter should not be null here.");
            return null;
        }

        params.put(HttpParameter.CALL_ID, Long.toString(System.currentTimeMillis()));

        //add scene
        final String homeid = QiupuHelper.getSceneId();
        if(!TextUtils.isEmpty(homeid)) {
        	params.put("scene", homeid);
        }
        
        params.put("sign", generateSignature(params, fileMap));
        if (StringUtil.isValidString(ticket)) {
        	params.put("ticket", ticket);
        }
//        params.put(HttpParameter.CALL_ID, Long.toString(System.currentTimeMillis()));

        params.put("appid", String.valueOf(QiupuConfig.APP_ID_QIUPU));
        params.put("sign_method", "md5");
        
        HttpParameter[] parameter = generateHttpParameter(params, fileMap);

        HttpResponse response = null;
        if (QiupuConfig.LowPerformance)
            Log.v(TAG, "call url suffix=" + urlSuffix + ", params:" + parameter);
        try {
            if (isGetMethod) {
                response = http.get(getBorqsURL() + urlSuffix, parameter);
            } else {
                response = http.post(getBorqsURL() + urlSuffix, parameter);
            }
        } catch (TwitterException te) {
            if (te.getStatusCode() == ErrorResponse.FORCE_VERSION_UPDATE) {
                // TODO: force application upgrade.
            }
            throw te;
        }

        return response;
    }
	
	public Stream getStreamWithComments(String ticket, String objectid) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();

//		map.put("object", QiupuConfig.TYPE_STREAM);
		map.put("postIds", objectid);
//		map.put("columns","");//TODO need all cols
//		map.put("page", String.valueOf(page));
//		map.put("count",String.valueOf(count));

		HttpResponse response = call("post/qiupuget", map, null, ticket, true);
        List<Stream> streamsList = StreamJSONImpl.createPostResponseList(response);

		return streamsList.size() > 0 ? streamsList.get(0) : null;
	}
	
	public QiupuUser addFriendsContact(String ticket, String name, String circleid, String content) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("circleIds", circleid);
		map.put("content", content);
			
        HttpResponse response = call("friend/contactset", map, null, ticket, false);
        return QiupuNewUserJSONImpl.creatFriend(response);
	}
	
	public ArrayList<EventTheme> syncEventThemes(String ticket, int page, int count) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
        HttpResponse response = call(API_EVENT_SYNC_THEME, map, null, ticket, true);
        return EventThemeJSONImpl.createEventThemeList(response);
	}
	
	public ArrayList<Company> getBelongCompany(final String ticket)throws TwitterException{
        HttpResponse response = http.get(getBorqsURL()+ API_COMPANY_BELONG,
    			new HttpParameter[]{new HttpParameter("ticket",ticket)});
        return CompanyJSONImpl.createCompanyList(response);
    }
	
	public Company getCompanyInfo(final String ticket, final long company_id) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("companies", String.valueOf(company_id));

        HttpResponse response = call(API_COMPANY_SHOW, map, null, ticket, true);

        return CompanyJSONImpl.createCompanyInfo(response);
    }
	
	public List<Stream> SearchStream(final String ticket, final String searchKey, final HashMap<String, String> searchMap, final int searchPage, final int searchCount) throws TwitterException{
		searchMap.put("q", searchKey);
		searchMap.put("page", String.valueOf(searchPage));
		searchMap.put("count", String.valueOf(searchCount));
	    
	    HttpResponse response = call(API_OBJECT_SEARCH, searchMap, null, ticket, true);
	    
	    return StreamJSONImpl.createSearchPostResponseList(response);
	}
	
	public ArrayList<InfoCategory> addCategory(final String ticket, final long scopeid, final String categoryName) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
		map.put("scope", String.valueOf(scopeid));
		map.put("category", categoryName);
		HttpResponse response = call(API_CATEGORY_CREATE, map, null, ticket,true);
        return InfoCategoryResponseJSONImpl.createInfoCategoryListResponse(response);
    }
	
	public ArrayList<UserCircle> syncTopCircle(final String ticket) throws TwitterException {
    	HashMap<String, String> map = new HashMap<String, String>();
		HttpResponse response = call(API_SYNC_TOP_CIRCLE, map, null, ticket,true);
        return UserCircleJSONImpl.createPublicCircleList(response);
    }

    private static class TwitterNew extends Twitter {
        // renamed api in new server API platform.
        private static final String API_FRIEND_FRIEND_MUTUAL = "friend/common";

        /**
         * new api pair to replace API_CIRCLE_ALTER_MEMBER in old server platform.
         */
        private static final String API_CIRCLE_MEMBER_ADD = "circle/add_friends";
        private static final String API_CIRCLE_MEMBER_REMOVE = "circle/remove_friends";
        // alternatives api pair of API_CIRCLE_ALTER_MEMBER

        private static final String API_CIRCLE_SET_CIRCLE = "friend/set_circles";
        
        private static final String API_SUGGEST_REJECT = "suggest/reject";

        public TwitterNew(Configuration conf, Authorization auth) {
            super(conf, auth);
        }

        @Override
        public BorqsUserSession loginBorqsAccount(String username, String pwd) throws TwitterException {
            String pwdBase64 = MD5.toMd5(pwd.getBytes());
            pwdBase64 = pwdBase64.toUpperCase();

            HttpResponse response;
            response = http.get(getBorqsURL() + API_ACCOUNT_LOGIN,
                    new HttpParameter[]{new HttpParameter("name", username),
                            new HttpParameter("appid", QiupuConfig.APP_ID_QIUPU),
                            new HttpParameter("password", pwdBase64)});

            return BorqsUserSessionJSONImpl.createUserSession(response);
        }

        @Override
        public ArrayList<QiupuUser> getFriendsListPage(final String ticket, final long uid, final String circles, final int page, final int count, final boolean isfollowing) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();
            final String url = isfollowing ? API_FRIEND_SHOW_FRIEND : API_FRIEND_SHOW_FOLLOWER;

            if (circles.length() > 0) {
                map.put("circles", circles);
            }
            if (uid == AccountServiceUtils.getBorqsAccountID()) {
                map.put("columns", COLFULLBASE + "," + COLXFRIENDS + "," + COLPRIVACY);
            } else {
                map.put("columns", COLSTD + "," + COLXFRIENDS + "," + COLPRIVACY);
            }

            map.put("user", String.valueOf(uid));
            map.put("page", String.valueOf(page));
            map.put("count", String.valueOf(count));

            HttpResponse response = call(url, map, null, ticket, true);

            return QiupuNewUserJSONImpl.createQiupuUserList(response);
        }

        @Override
        public ArrayList<QiupuUser> getFriendsBilateral(final String ticket, final long otheruid, final int page, final int count) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("user", String.valueOf(otheruid));
            map.put("page", String.valueOf(page));
            map.put("count", String.valueOf(count));
            map.put("cols", COLSTD + "," + COLXFRIENDS); // on new platform, back response default is simpleUser

            HttpResponse response = call(API_FRIEND_FRIEND_MUTUAL, map, null, ticket, true);

            return QiupuNewUserJSONImpl.createQiupuUserList(response);
        }

        @Override
        public boolean usersSet(final String ticket, final String uids, final String circleid, final boolean isadd) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();

            map.put("friends", uids);
            map.put("circle", circleid);

            final String url = isadd ? API_CIRCLE_MEMBER_ADD : API_CIRCLE_MEMBER_REMOVE;
            HttpResponse response = call(url, map, null, ticket, true);

            return JsonResult.getBooleanResult(response);
        }

        @Override
        public QiupuUser setCircle(final String ticket, final long uid, final String circleid) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("friend", String.valueOf(uid));
            map.put("circles", circleid);

            HttpResponse response = call(API_CIRCLE_SET_CIRCLE, map, null, ticket, false);

            return QiupuNewUserJSONImpl.creatFriend(response);
        }

        @Override
        public ArrayList<UserCircle> getUserCircle(final String ticket, final long uid, final String circles, final boolean with_users) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("with_users", String.valueOf(with_users));
            map.put("circles", circles);

            HttpResponse response = call(API_CIRCLE_SHOW, map, null, ticket, true);

            return UserCircleJSONImpl.createUserCircleList(response);
        }

        @Override
        public boolean deleteCircle(final String ticket, final String circleId, final int type) throws TwitterException {
            if (UserCircle.CIRCLE_TYPE_LOCAL == type) {
                HashMap<String, String> map = new HashMap<String, String>();
                String url = "";
                map.put("circle", circleId);
                url = API_CIRCLE_REMOVE;
                HttpResponse response = call(url, map, null, ticket, true);

                return JsonResult.getBooleanResult(response);
            } else {
                return deletePublicCircle(ticket, circleId);
            }
        }

        @Override
        public QiupuUser getUserInfo(final String ticket, final long userid) throws TwitterException {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("users", String.valueOf(userid));
            map.put("columns", COLFULLBASE + "," + COLXFRIENDS + "," + COLPRIVACY);

            HttpResponse response = call(API_ACCOUNT_SHOW_USER, map, null, ticket, true);

            return NewPlatformUserJSONImpl.creatUsersInfo(response);
        }
        
        public ArrayList<QiupuUser> getUserYouMayKnow(final String ticket,final int count, final boolean getback)throws TwitterException{
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("count",String.valueOf(count));
            HttpResponse response = call(API_SUGGEST_GET, map, null, ticket, true);
            
            return NewPlatformUserJSONImpl.getUserYouMayKnow(response);
        }  
        
        public boolean refuseUser(final String ticket,final long uid)throws TwitterException{
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("suggested", String.valueOf(uid));
            HttpResponse response = call(API_SUGGEST_REJECT, map, null, ticket, true);
            
            return JsonResult.getBooleanResult(response);
        }
        
        public boolean recommendFriends(final String ticket,final long touid,final String selectuid) throws TwitterException{
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("to", String.valueOf(touid));
            map.put("suggested", String.valueOf(selectuid));
            HttpResponse response = call(API_SUGGEST_RECOMMEDN_USER, map, null, ticket,false);
            
            return JsonResult.getBooleanResult(response);
        }
    }

    public ArrayList<String> setTopList(String ticket, String group_id, String stream_ids, boolean setTop) throws TwitterException {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("id", group_id);
        if (setTop == true) {
            map.put("set",stream_ids);
        } else {
            map.put("unset",stream_ids);
        }

        HttpResponse response = call(API_SET_TOP, map, null, ticket, true);
        return StreamJSONImpl.createTopListIds(response);
    }

    public static String getBorqsURL() {
        return QiupuHelper.getBorqsURL();
    }

	public PageInfo createPage(String ticket, HashMap<String, String> columnsMap) throws TwitterException {
		HttpResponse response = call(API_PAGE_CREATE, columnsMap, null, ticket,false);
        return PageInfoJSONImpl.createPageResponse(response);
	}
	
	public PageInfo syncPageInfo(String ticket, long pageid) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("page", String.valueOf(pageid));
		HttpResponse response = call(API_PAEG_SHOW_ONE, map, null, ticket,true);
        return PageInfoJSONImpl.createPageResponse(response);
	}
	
	public PageInfo editPage(final String ticket,final HashMap<String, String> map)throws TwitterException{
		HttpResponse response = call(API_PAEG_EDIT, map, null, ticket,false);
		return PageInfoJSONImpl.createPageResponse(response);
    }	

	public PageInfo editPageCover(final String ticket,final long pageid, final File file) throws TwitterException{
		return editPageImage(ticket, API_PAEG_UPLOAD_COVER, pageid, file);
	}
	
	public PageInfo editPageLogo(final String ticket,final long pageid, final File file) throws TwitterException{
		return editPageImage(ticket, API_PAEG_UPLOAD_LOGO, pageid, file);
	}
	
	private PageInfo editPageImage(final String ticket, final String url, final long pageid, final File file) throws TwitterException {
		checkFileValidity(file);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("page", String.valueOf(pageid));
        HashMap<String, File> fileMap = new HashMap<String,File>();
        fileMap.put("file", file);
        HttpResponse response = call(url, map, fileMap, ticket, false);

        return PageInfoJSONImpl.createPageResponse(response);
	}
	
	public boolean deletePage(final String ticket, final long pageId) throws TwitterException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("page", String.valueOf(pageId));
		HttpResponse response = call(API_PAEG_REMOVE, map, null, ticket, true);
		
		return JsonResult.getBooleanResult(response);
	}
	 
	public ArrayList<PageInfo> searchPage(final String ticket,final String searchKey, final int page, final int count) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("kw",searchKey);
		map.put("page", String.valueOf(page));
		map.put("count", String.valueOf(count));
		HttpResponse response = call(API_PAEG_SEARCH, map, null, ticket, true);
		
		return PageInfoJSONImpl.createPageList(response);
	}
	
	public PageInfo followPage(final String ticket,final long pageid, final boolean isfollow) throws TwitterException{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("page", String.valueOf(pageid));
		HttpResponse response = call(isfollow ? API_PAEG_FOLLOW : API_PAEG_UNFOLLOW, map, null, ticket, true);
		
		return PageInfoJSONImpl.createPageResponse(response);
	}
	
	public PageInfo circleAsPage(final String ticket,final long id, final HashMap<String, String> infoMap) throws TwitterException{
		infoMap.put("circle", String.valueOf(id));
		HttpResponse response = call(API_PAEG_CIRCLE_AS_PAGE, infoMap, null, ticket, false);
		
		return PageInfoJSONImpl.createPageResponse(response);
	}


//    private static final String API_DIRECTORY_GET = "company/employee/list";
    private static final String API_DIRECTORY_GET = API_PUBLIC_CIRCLE_USERS;
    private static final String DIRECTORY_OWNER = "company";
    private static final String DIRECTORY_SORT = "sort";
    private static final String DIRECTORY_PAGE = "page";
    private static final String DIRECTORY_COUNT = "count";
    private static final String DIRECTORY_SEARCH_KEY = "key";
    public ArrayList<Employee> getDirectoryInfo(final String ticket, final long circleid, final String sort,
                                                   final int page, final int count, final String searchKey) throws TwitterException {
        HashMap<String, String> map = new HashMap<String, String>();
        if(circleid > 0) {
        	map.put("id", String.valueOf(circleid));
        }
        if (!TextUtils.isEmpty(sort)) {
            map.put(DIRECTORY_SORT, sort);
        }
        map.put(DIRECTORY_PAGE, String.valueOf(page));
        map.put(DIRECTORY_COUNT, String.valueOf(count));
        
        if (!TextUtils.isEmpty(searchKey)) {
            map.put(DIRECTORY_SEARCH_KEY, searchKey);
        }
        HttpResponse response = call(API_DIRECTORY_GET, map, null, ticket, true);

        return EmployeeJsonImpl.createEmployeeList(response);
    }
}
