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

import android.util.Log;
import com.borqs.qiupu.AccountListener;
import twitter4j.api.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.Authorization;
import twitter4j.http.RequestToken;
import twitter4j.internal.async.Dispatcher;
import twitter4j.internal.async.DispatcherFactory;
import twitter4j.internal.org.json.JSONException;

import java.io.File;
import java.util.Date;

import static twitter4j.TwitterMethod.*;

//import android.app.backup.BackupDataInput;

/**
 * Twitter API with a series of asynchronous APIs.<br>
 * With this class, you can call TwitterAPI asynchronously.<br>
 * Note that currently this class is NOT compatible with Google App Engine as it is maintaining threads internally.<br>
 * Currently this class is not carefully designed to be extended. It is suggested to extend this class only for mock testing purporse.<br>
 * @see twitter4j.AsyncTwitter
 * @see twitter4j.TwitterListener
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class AsyncTwitter extends TwitterOAuthSupportBase
        implements java.io.Serializable,
        SearchMethodsAsync,
        TrendsMethodsAsync,
        TimelineMethodsAsync,
        StatusMethodsAsync,
        UserMethodsAsync,
        ListMethodsAsync,
        ListMembersMethodsAsync,
        ListSubscribersMethodsAsync,
        DirectMessageMethodsAsync,
        FriendshipMethodsAsync,
        FriendsFollowersMethodsAsync,
        AccountMethodsAsync,
        FavoriteMethodsAsync,
        NotificationMethodsAsync,
        BlockMethodsAsync,
        SpamReportingMethodsAsync,
        SavedSearchesMethodsAsync,
        LocalTrendsMethodsAsync,
        GeoMethodsAsync,
        HelpMethodsAsync {
    private static final long serialVersionUID = -2008667933225051907L;
    private Twitter twitter;
    //private TwitterListener listener;

    /**
     * Creates a basic authenticated AsyncTwitter instance.
     * @param screenName screen name
     * @param password password
     * @param listener listener
     * @deprecated use {@link AsyncTwitterFactory#getInstance(String,String)} instead.
     */
    public AsyncTwitter(String screenName, String password, TwitterListener listener) {
        super(ConfigurationContext.getInstance(), screenName, password);
        twitter = new TwitterFactory().getInstance(auth);
       // this.listener = listener;
    }

    /*package*/
    public AsyncTwitter(Configuration conf, Authorization auth, TwitterListener listener) {
        super(conf, auth);
        twitter = new TwitterFactory(conf).getInstance(auth);
       // this.listener = listener;
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
        return twitter.getScreenName();
    }

    /**
     * Returns authenticating user's user id.<br>
     * This method may internally call verifyCredentials() on the first invocation if<br>
     * - this instance is authenticated by Basic and email address is supplied instead of screen name, or
     * - this instance is authenticated by OAuth.<br>
     *
     * @return the authenticating user's id
     * @throws TwitterException when verifyCredentials threw an exception.
     * @throws IllegalStateException if no credentials are supplied. i.e.) this is an anonymous Twitter instance
     * @since Twitter4J 2.1.1
     */
    public int getId() throws TwitterException, IllegalStateException {
        return twitter.getId();
    }

    /**
     * {@inheritDoc}
     */
    public void search(final Query query,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEARCH, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.searched(twitter.search(query));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getTrends(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotTrends(twitter.getTrends());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getCurrentTrends(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CURRENT_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotCurrentTrends(twitter.getCurrentTrends());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getCurrentTrends(final boolean excludeHashTags,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CURRENT_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotCurrentTrends(twitter.getCurrentTrends(excludeHashTags));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getDailyTrends(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DAILY_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotDailyTrends(twitter.getDailyTrends());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getDailyTrends(final Date date, final boolean excludeHashTags,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DAILY_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotDailyTrends(twitter.getDailyTrends(date, excludeHashTags));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getWeeklyTrends(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(WEEKLY_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotWeeklyTrends(twitter.getWeeklyTrends());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getWeeklyTrends(final Date date, final boolean excludeHashTags,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(WEEKLY_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws
                    TwitterException {
                listener.gotWeeklyTrends(twitter.getWeeklyTrends(date, excludeHashTags));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getPublicTimeline(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(PUBLIC_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotPublicTimeline(twitter.getPublicTimeline());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getHomeTimeline(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(HOME_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotHomeTimeline(twitter.getHomeTimeline());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getHomeTimeline(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(HOME_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotHomeTimeline(twitter.getHomeTimeline(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsTimeline(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsTimeline(twitter.getFriendsTimeline());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsTimeline(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsTimeline(twitter.getFriendsTimeline(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(final String screenName, final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.gotUserTimeline(twitter.getUserTimeline(screenName,
                        paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(final int userId, final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.gotUserTimeline(twitter.getUserTimeline(userId, paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.gotUserTimeline(twitter.getUserTimeline(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(final String screenName,TwitterListener listener) {
        getUserTimeline(screenName, new Paging(),listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(final int userId,TwitterListener listener) {
        getUserTimeline(userId, new Paging(),listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getUserTimeline(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserTimeline(twitter.getUserTimeline());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getMentions(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(MENTIONS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotMentions(twitter.getMentions());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getMentions(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(MENTIONS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotMentions(twitter.getMentions(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedByMe(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedByMe(twitter.getRetweetedByMe());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedByMe(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedByMe(twitter.getRetweetedByMe(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedToMe(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_TO_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedToMe(twitter.getRetweetedToMe());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedToMe(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_TO_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedToMe(twitter.getRetweetedToMe(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetsOfMe(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETS_OF_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetsOfMe(twitter.getRetweetsOfMe());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetsOfMe(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETS_OF_ME, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetsOfMe(twitter.getRetweetsOfMe(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void showStatus(final long id,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotShowStatus(twitter.showStatus(id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final String status,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedStatus(twitter.updateStatus(status));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final String status, final GeoLocation location,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedStatus(twitter.updateStatus(status, location));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final String status, final long inReplyToStatusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedStatus(twitter.updateStatus(status, inReplyToStatusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final String status, final long inReplyToStatusId, final GeoLocation location,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedStatus(twitter.updateStatus(status, inReplyToStatusId, location));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final StatusUpdate latestStatus,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedStatus(twitter.updateStatus(latestStatus));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyStatus(final long statusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedStatus(twitter.destroyStatus(statusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void retweetStatus(final long statusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEET_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.retweetedStatus(twitter.retweetStatus(statusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweets(final long statusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweets(twitter.getRetweets(statusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedBy(final long statusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedBy(twitter.getRetweetedBy(statusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedBy(final long statusId, final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedBy(twitter.getRetweetedBy(statusId, paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedByIDs(final long statusId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedByIDs(twitter.getRetweetedByIDs(statusId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRetweetedByIDs(final long statusId, final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRetweetedByIDs(twitter.getRetweetedByIDs(statusId, paging));
            }
        });
    }

    /* User Methods */

    /**
     * {@inheritDoc}
     */
    public void showUser(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_USER, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserDetail(twitter.showUser(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void showUser(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_USER, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserDetail(twitter.showUser(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void lookupUsers(final String[] screenNames,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(LOOKUP_USERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.lookedupUsers(twitter.lookupUsers(screenNames));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void lookupUsers(final int[] ids,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(LOOKUP_USERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.lookedupUsers(twitter.lookupUsers(ids));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void searchUsers(final String query, final int page,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEARCH_USERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.searchedUser(twitter.searchUsers(query, page));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getSuggestedUserCategories(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SUGGESTED_USER_CATEGORIES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotSuggestedUserCategories(twitter.getSuggestedUserCategories());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserSuggestions(final String categorySlug,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_SUGGESTIONS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserSuggestions(twitter.getUserSuggestions(categorySlug));
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsStatuses(twitter.getFriendsStatuses());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsStatuses(twitter.getFriendsStatuses(cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(final String screenName,TwitterListener listener) {
        getFriendsStatuses(screenName, -1l,listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(final int userId,TwitterListener listener) {
        getFriendsStatuses(userId, -1l,listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(final String screenName, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsStatuses(twitter.getFriendsStatuses(screenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsStatuses(final int userId, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsStatuses(twitter.getFriendsStatuses(userId, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(TwitterListener listener) {
        getFollowersStatuses(-1l,listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersStatuses(twitter.getFollowersStatuses(cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(final String screenName,TwitterListener listener) {
        getFollowersStatuses(screenName, -1l,listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(final int userId,TwitterListener listener) {
        getFollowersStatuses(userId, -1l,listener);
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(final String screenName, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersStatuses(twitter.getFollowersStatuses(screenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersStatuses(final int userId, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersStatuses(twitter.getFollowersStatuses(userId, cursor));
            }
        });
    }

    /*List Methods*/
    /**
     * {@inheritDoc}
     */
    public void createUserList(final String listName, final boolean isPublicList, final String description,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(CREATE_USER_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdUserList(twitter.createUserList(listName, isPublicList, description));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateUserList(final int listId, final String newListName, final boolean isPublicList, final String newDescription,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(UPDATE_USER_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedUserList(twitter.updateUserList(listId, newListName, isPublicList, newDescription));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserLists(final String listOwnerScreenName, final long cursor,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(USER_LISTS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserLists(twitter.getUserLists(listOwnerScreenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void showUserList(final String listOwnerScreenName, final int id,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(UPDATE_USER_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotShowUserList(twitter.showUserList(listOwnerScreenName, id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyUserList(final int listId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(DESTROY_USER_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedUserList(twitter.destroyUserList(listId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserListStatuses(final String listOwnerScreenName, final int id, final Paging paging,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(USER_LIST_STATUSES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserListStatuses(twitter.getUserListStatuses(listOwnerScreenName, id, paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getUserListMemberships(final String listOwnerScreenName, final long cursor,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(USER_LIST_MEMBERSHIPS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserListMemberships(twitter.getUserListMemberships(listOwnerScreenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
	public void getUserListSubscriptions(final String listOwnerScreenName, final long cursor,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(USER_LIST_SUBSCRIPTIONS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserListSubscriptions(twitter.getUserListSubscriptions(listOwnerScreenName, cursor));
            }
        });
    }

    /*List Members Methods*/
    /**
     * {@inheritDoc}
     */
    public void getUserListMembers(final String listOwnerScreenName, final int listId, final long cursor,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(LIST_MEMBERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserListMembers(twitter.getUserListMembers(listOwnerScreenName, listId, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addUserListMember(final int listId, final int userId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(ADD_LIST_MEMBER, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.addedUserListMember(twitter.addUserListMember(listId, userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void deleteUserListMember(final int listId, final int userId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(DELETE_LIST_MEMBER, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.deletedUserListMember(twitter.deleteUserListMember(listId, userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void checkUserListMembership(final String listOwnerScreenName, final int listId, final int userId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(CHECK_LIST_MEMBERSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.checkedUserListMembership(twitter.checkUserListMembership(listOwnerScreenName, listId, userId));
            }
        });
    }

    /*List Subscribers Methods*/

    /**
     * {@inheritDoc}
     */
    public void getUserListSubscribers(final String listOwnerScreenName, final int listId, final long cursor,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(LIST_SUBSCRIBERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotUserListSubscribers(twitter.getUserListSubscribers(listOwnerScreenName, listId, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void subscribeUserList(final String listOwnerScreenName, final int listId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(SUBSCRIBE_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.subscribedUserList(twitter.subscribeUserList(listOwnerScreenName, listId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void unsubscribeUserList(final String listOwnerScreenName, final int listId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(UNSUBSCRIBE_LIST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.unsubscribedUserList(twitter.unsubscribeUserList(listOwnerScreenName, listId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void checkUserListSubscription(final String listOwnerScreenName, final int listId, final int userId,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(CHECK_LIST_SUBSCRIPTION, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.checkedUserListSubscription(twitter.checkUserListSubscription(listOwnerScreenName, listId, userId));
            }
        });
    }

    /*Direct Message Methods */

    /**
     * {@inheritDoc}
     */
    public void getDirectMessages(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotDirectMessages(twitter.getDirectMessages());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getDirectMessages(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotDirectMessages(twitter.getDirectMessages(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getSentDirectMessages(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SENT_DIRECT_MESSAGES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotSentDirectMessages(twitter.getSentDirectMessages());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getSentDirectMessages(final Paging paging,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SENT_DIRECT_MESSAGES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotSentDirectMessages(twitter.getSentDirectMessages(paging));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void sendDirectMessage(final String screenName, final String text,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEND_DIRECT_MESSAGE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.sentDirectMessage(twitter.sendDirectMessage(screenName, text));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void sendDirectMessage(final int userId, final String text,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEND_DIRECT_MESSAGE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.sentDirectMessage(twitter.sendDirectMessage(userId, text));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyDirectMessage(final int id,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_DIRECT_MESSAGES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedDirectMessage(twitter.destroyDirectMessage(id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createFriendship(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdFriendship(twitter.createFriendship(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createFriendship(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdFriendship(twitter.createFriendship(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createFriendship(final String screenName, final boolean follow,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdFriendship(twitter.createFriendship(screenName, follow));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createFriendship(final int userId, final boolean follow,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdFriendship(twitter.createFriendship(userId, follow));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyFriendship(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedFriendship(twitter.destroyFriendship(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyFriendship(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedFriendship(twitter.destroyFriendship(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void existsFriendship(final String userA, final String userB,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotExistsFriendship(twitter.existsFriendship(userA, userB));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void showFriendship(final String sourceScreenName, final String targetScreenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotShowFriendship(twitter.showFriendship(sourceScreenName, targetScreenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void showFriendship(final int sourceId, final int targetId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_FRIENDSHIP, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotShowFriendship(twitter.showFriendship(sourceId, targetId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getIncomingFriendships(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(INCOMING_FRIENDSHIPS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotIncomingFriendships(twitter.getIncomingFriendships(cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getOutgoingFriendships(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(OUTGOING_FRIENDSHIPS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotOutgoingFriendships(twitter.getOutgoingFriendships(cursor));
            }
        });
    }


    /* Social Graph Methods */

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs(cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(final int userId, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs(userId, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFriendsIDs(final String screenName, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.gotFriendsIDs(twitter.getFriendsIDs(screenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs(cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(final int userId, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs(userId, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFollowersIDs(final String screenName, final long cursor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFollowersIDs(twitter.getFollowersIDs(screenName, cursor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void verifyCredentials(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(VERIFY_CREDENTIALS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.verifiedCredentials(twitter.verifyCredentials());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateProfile(final String name, final String email, final String url
            , final String location, final String description,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedProfile(twitter.updateProfile(name, email, url,
                        location, description));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getRateLimitStatus(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RATE_LIMIT_STATUS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotRateLimitStatus(twitter.getRateLimitStatus());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateDeliveryDevice(final Device device,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_DELIVERY_DEVICE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.updatedDeliveryDevice(twitter.updateDeliveryDevice(device));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateProfileColors(
            final String profileBackgroundColor, final String profileTextColor,
            final String profileLinkColor, final String profileSidebarFillColor,
            final String profileSidebarBorderColor,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE_COLORS,
                listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.updatedProfileColors(twitter.updateProfileColors(
                        profileBackgroundColor, profileTextColor,
                        profileLinkColor, profileSidebarFillColor,
                        profileSidebarBorderColor));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateProfileImage(final File image,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE_IMAGE,
                listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.updatedProfileImage(twitter.updateProfileImage(image));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void updateProfileBackgroundImage(final File image
            , final boolean tile,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE_BACKGROUND_IMAGE,
                listener) {
            public void invoke(TwitterListener listener)
                    throws TwitterException {
                listener.updatedProfileBackgroundImage(twitter.
                        updateProfileBackgroundImage(image, tile)
                );
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFavorites(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFavorites(twitter.getFavorites());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFavorites(final int page,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFavorites(twitter.getFavorites(page));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFavorites(final String id,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFavorites(twitter.getFavorites(id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getFavorites(final String id, final int page,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotFavorites(twitter.getFavorites(id, page));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createFavorite(final long id,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FAVORITE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdFavorite(twitter.createFavorite(id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyFavorite(final long id,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_FAVORITE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedFavorite(twitter.destroyFavorite(id));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void enableNotification(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(ENABLE_NOTIFICATION, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.enabledNotification(twitter.enableNotification(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void enableNotification(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(ENABLE_NOTIFICATION, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.enabledNotification(twitter.enableNotification(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void disableNotification(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DISABLE_NOTIFICATION, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.disabledNotification(twitter.disableNotification(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void disableNotification(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DISABLE_NOTIFICATION, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.disabledNotification(twitter.disableNotification(userId));
            }
        });
    }

    /* Block Methods */

    /**
     * {@inheritDoc}
     */
    public void createBlock(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdBlock(twitter.createBlock(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void createBlock(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.createdBlock(twitter.createBlock(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyBlock(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedBlock(twitter.destroyBlock(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroyBlock(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.destroyedBlock(twitter.destroyBlock(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void existsBlock(final String screenName,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotExistsBlock(twitter.existsBlock(screenName));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void existsBlock(final int userId,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS_BLOCK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotExistsBlock(twitter.existsBlock(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getBlockingUsers(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(BLOCKING_USERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotBlockingUsers(twitter.getBlockingUsers());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getBlockingUsers(final int page,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(BLOCKING_USERS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotBlockingUsers(twitter.getBlockingUsers(page));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getBlockingUsersIDs(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(BLOCKING_USERS_IDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotBlockingUsersIDs(twitter.getBlockingUsersIDs());
            }
        });
    }

    /* Spam Reporting Methods */

    /**
     * {@inheritDoc}
     */
    public void reportSpam(final int userId,TwitterListener listener) throws TwitterException{
        getDispatcher().invokeLater(new AsyncTask(REPORT_SPAM, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.reportedSpam(twitter.reportSpam(userId));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void reportSpam(final String screenName,TwitterListener listener) throws TwitterException{
        getDispatcher().invokeLater(new AsyncTask(REPORT_SPAM, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.reportedSpam(twitter.reportSpam(screenName));
            }
        });
    }
    /* Saved Searches Methods */
    /* Local Trend Methods */

    /**
     * {@inheritDoc}
	 */
    public void getAvailableTrends(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(AVAILABLE_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotAvailableTrends(twitter.getAvailableTrends());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getAvailableTrends(final GeoLocation location,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(AVAILABLE_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotAvailableTrends(twitter.getAvailableTrends(location));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getLocationTrends(final int woeid,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(LOCATION_TRENDS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotLocationTrends(twitter.getLocationTrends(woeid));
            }
        });
    }

    /* Geo Methods */

    /**
     * {@inheritDoc}
     */
    public void getNearbyPlaces(final GeoQuery query,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(NEAR_BY_PLACES, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotNearByPlaces(twitter.getNearbyPlaces(query));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void reverseGeoCode(final GeoQuery query,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(REVERSE_GEO_CODE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotReverseGeoCode(twitter.reverseGeoCode(query));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void getGeoDetails(final String id,TwitterListener listener){
        getDispatcher().invokeLater(new AsyncTask(GEO_DETAILS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.gotGeoDetails(twitter.getGeoDetails(id));
            }
        });
    }

    /* Help Methods */

    /**
     * {@inheritDoc}
     */
    public void test(TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(TEST, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.tested(twitter.test());
            }
        });
    }

    private static transient Dispatcher dispatcher;
    private boolean shutdown = false;

    /**
     * Shuts down internal dispatcher thread.
     *
     * @since Twitter4J 2.0.2
     */
    public void shutdown(){
        synchronized (AsyncTwitter.class) {
            if (shutdown) {
                throw new IllegalStateException("Already shut down");
            }
            getDispatcher().shutdown();
            dispatcher = null;
            super.shutdown();
            shutdown = true;
        }
    }
    private Dispatcher getDispatcher(){
        if(shutdown){
            throw new IllegalStateException("Already shut down");
        }
        if (null == dispatcher) {
            dispatcher = new DispatcherFactory(conf).getInstance();
        }
        return dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOAuthConsumer(String consumerKey, String consumerSecret) {
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestToken getOAuthRequestToken() throws TwitterException {
        return twitter.getOAuthRequestToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl) throws TwitterException {
        return twitter.getOAuthRequestToken(callbackUrl);
    }

    /**
     * {@inheritDoc}
     * Basic authenticated instance of this class will try acquiring an AccessToken using xAuth.<br>
     * In order to get access acquire AccessToken using xAuth, you must apply by sending an email to <a href="mailto:api@twitter.com">api@twitter.com</a> all other applications will receive an HTTP 401 error.  Web-based applications will not be granted access, except on a temporary basis for when they are converting from basic-authentication support to full OAuth support.<br>
     * Storage of Twitter usernames and passwords is forbidden. By using xAuth, you are required to store only access tokens and access token secrets. If the access token expires or is expunged by a user, you must ask for their login and password again before exchanging the credentials for an access token.
     * @see <a href="http://apiwiki.twitter.com/Twitter-REST-API-Method%3A-oauth-access_token-for-xAuth">Twitter REST API Method: oauth access_token for xAuth</a>
     * @throws TwitterException When Twitter service or network is unavailable, when the user has not authorized, or when the client application is not permitted to use xAuth
     */
    @Override
    public AccessToken getOAuthAccessToken() throws TwitterException {
        return twitter.getOAuthAccessToken();
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public AccessToken getOAuthAccessToken(String oauthVerifier) throws TwitterException {
        return twitter.getOAuthAccessToken(oauthVerifier);
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public AccessToken getOAuthAccessToken(RequestToken requestToken) throws TwitterException {
        return twitter.getOAuthAccessToken(requestToken);
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public AccessToken getOAuthAccessToken(RequestToken requestToken, String oauthVerifier) throws TwitterException {
        return twitter.getOAuthAccessToken(requestToken, oauthVerifier);
    }

    /**
     * {@inheritDoc}
     * @deprecated Use {@link AsyncTwitterFactory#getInstance(Authorization)}
     */
    @Override
    public void setOAuthAccessToken(AccessToken accessToken) {
        twitter.setOAuthAccessToken(accessToken);
    }

    /**
     * {@inheritDoc}
     * @deprecated Use {@link AsyncTwitterFactory#getInstance(Authorization)}
     */
    @Override
    public AccessToken getOAuthAccessToken(String token, String tokenSecret) throws TwitterException {
        return twitter.getOAuthAccessToken(token, tokenSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessToken getOAuthAccessToken(String token, String tokenSecret, String pin) throws TwitterException {
        return twitter.getOAuthAccessToken(token, tokenSecret, pin);
    }

    /**
     * Sets the access token
     *
     * @param token       access token
     * @param tokenSecret access token secret
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     * @since Twitter 2.0.0
     * @deprecated Use {@link AsyncTwitterFactory#getInstance(Authorization)}
     */
    @Override
    public void setOAuthAccessToken(String token, String tokenSecret) {
        twitter.setOAuthAccessToken(token, tokenSecret);
    }

    abstract class AsyncTask implements Runnable {
        TwitterListener listener;
        TwitterMethod method;
        AsyncTask(TwitterMethod method, TwitterListener listener) {
            this.method = method;
            this.listener = listener;
        }

        abstract void invoke(TwitterListener listener) throws TwitterException, JSONException;
        
        public void run() {
            try {
                   invoke(listener);
            } catch (TwitterException te) {
                if (null != listener) {
                	if(alistener != null){
                		AccountListener mAlistener = alistener.get();
                		if(mAlistener != null){
                			mAlistener.filterInvalidException(te);
                		}
                	}
                	
                	listener.onException(te,method);
                }
            } catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
    
    public void authLogin(final String username, final String pwd,TwitterListener listener) {
        getDispatcher().invokeLater(new AsyncTask(AUTH_LOGIN, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.authLogin(twitter.authLogin(username,pwd));
            }
        });
    }

	public void registerAccount(final String username, final String pwd, final String nickname,
			final String phonenumber,final String urlname, TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(REGISTER_ACCOUNT, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.registerAccount(twitter.registerAccount(username,pwd,nickname, urlname, phonenumber));
            }
        });
	}

	public void loginBorqs(final String username, final String pwd,
			TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(LOGIN_BORQS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.loginBorqs(twitter.loginBorqs(username,pwd));
            }
        });		
	}

	public void verifyAccountRegister(final String username, final String verifycode,
			TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(VERIFY_ACCOUNT, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.verifyAccountRegister(twitter.verifyAccountRegister(username,verifycode));
            }
        });	
	}
	
	public void uploadLocalFile(final File file,final String session_id,final String type,TwitterListener listener){
		getDispatcher().invokeLater(new AsyncTask(UPLOAD_FILE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.uploadLocalFile(twitter.uploadLocalFile(file,session_id,type));
            }
        });	
	}
	
	public void uploadLocalFile(final File file,final String session_id,final String type,final String apkinfo,TwitterListener listener){
		getDispatcher().invokeLater(new AsyncTask(UPLOAD_FILE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.uploadLocalFile(twitter.uploadLocalFile(file,session_id,type,apkinfo));
            }
        });	
	}
	
	public void getBackupList(final String sessionid, final String type,TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(GET_BACKUP_FILE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.getBackupList(twitter.getBackupList(sessionid,type));
            }
        });	
	}

	public void collectPhoneInfo(final String mMSISDN, final String mIMEI, final String mIMSI,
			final String mFirmwareVersion, final String mModel, final String mWifiMacAddress,
			final String mEmail, final TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(GET_BACKUP_FILE, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.collectPhoneInfo(twitter.collectPhoneInfo(mMSISDN,mIMEI,mIMSI,mFirmwareVersion,mModel,mWifiMacAddress,mEmail));
            }
        });	
	}
	
	public void getBackupRecord(final String sessionid, final long maxid,TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(GET_BACKUP_RECORD, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.getBackupRecord(twitter.getBackupRecord(sessionid,maxid));
            }
        });		
    
	}

	public void getBackupApk(final String sessionid, final long recordid, final long maxid,final String localPath, TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(GET_BACKUP_APK, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.getBackupApk(twitter.getBackupApk(sessionid,recordid,maxid,localPath));
            }
        });				
	}	
}
