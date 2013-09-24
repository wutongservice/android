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

import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import twitter4j.Stream.Comments;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.UserCircle.RecieveSet;
import twitter4j.http.AccessToken;

import com.borqs.account.service.ContactSimpleInfo;

/**
 * A listner for receiving asynchronous responses from Twitter Async APIs.
 *
 * @see twitter4j.AsyncTwitter
 * @see twitter4j.TwitterAdapter
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public interface TwitterListener {
    /*Search API Methods*/
    void searched(QueryResult queryResult);

    /**
     * @since Twitter4J 2.0.2
     */
    void gotTrends(Trends trends);

    /**
     * @since Twitter4J 2.0.2
     */
    void gotCurrentTrends(Trends trends);

    /**
     * @since Twitter4J 2.0.2
     */
    void gotDailyTrends(List<Trends> trendsList);

    /**
     * @since Twitter4J 2.0.2
     */
    void gotWeeklyTrends(List<Trends> trendsList);

    /*Timeline Methods*/
    void gotPublicTimeline(ResponseList<Status> statuses);

    /**
     * @since Twitter4J 2.0.10
     */
    void gotHomeTimeline(ResponseList<Status> statuses);

    void gotFriendsTimeline(ResponseList<Status> statuses);

    void gotUserTimeline(ResponseList<Status> statuses);
    /**
     * @since Twitter4J 2.0.1
     */
    void gotMentions(ResponseList<Status> statuses);
    /**
     * @since Twitter4J 2.0.10
     */
    void gotRetweetedByMe(ResponseList<Status> statuses);
    /**
     * @since Twitter4J 2.0.10
     */
    void gotRetweetedToMe(ResponseList<Status> statuses);
    /**
     * @since Twitter4J 2.0.10
     */
    void gotRetweetsOfMe(ResponseList<Status> statuses);


    /*Status Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    void gotShowStatus(Status status);

    void updatedStatus(Status status);

    void destroyedStatus(Status destroyedStatus);

    /**
     * @since Twitter4J 2.0.10
     */
    void retweetedStatus(Status retweetedStatus);

    /**
     * @since Twitter4J 2.1.0
     */
    void gotRetweets(ResponseList<Status> retweets);

    /**
     * @since Twitter4J 2.1.3
     */
    void gotRetweetedBy(ResponseList<User> users);

    /**
     * @since Twitter4J 2.1.3
     */
    void gotRetweetedByIDs(IDs ids);

    /*User Methods*/
    void gotUserDetail(User user);

    /**
     * @since Twitter4J 2.1.1
     */
    void lookedupUsers(ResponseList<User> users);

    /**
     * @since Twitter4J 2.1.0
     */
    void searchedUser(ResponseList<User> userList);

    /**
     * @since Twitter4J 2.1.1
     */
    void gotSuggestedUserCategories(ResponseList<Category> category);

    /**
     * @since Twitter4J 2.1.1
     */
    void gotUserSuggestions(ResponseList<User> users);


    void gotFriendsStatuses(PagableResponseList<User> users);

    void gotFollowersStatuses(PagableResponseList<User> users);

    /*List Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    void createdUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void updatedUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserLists(PagableResponseList<UserList> userLists);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotShowUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void destroyedUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserListStatuses(ResponseList<Status> statuses);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserListMemberships(PagableResponseList<UserList> userLists);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserListSubscriptions(PagableResponseList<UserList> userLists);

    /*List Members Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserListMembers(PagableResponseList<User> users);
    /**
     * @since Twitter4J 2.1.0
     */
    void addedUserListMember(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void deletedUserListMember(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void checkedUserListMembership(User users);

    /*List Subscribers Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    void gotUserListSubscribers(PagableResponseList<User> users);
    /**
     * @since Twitter4J 2.1.0
     */
    void subscribedUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void unsubscribedUserList(UserList userList);
    /**
     * @since Twitter4J 2.1.0
     */
    void checkedUserListSubscription(User user);

    /*Direct Message Methods*/
    void gotDirectMessages(ResponseList<DirectMessage> messages);

    void gotSentDirectMessages(ResponseList<DirectMessage> messages);

    void sentDirectMessage(DirectMessage message);

    /**
     * @since Twitter4J 2.0.1
     */
    void destroyedDirectMessage(DirectMessage message);

    /*Friendship Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    void createdFriendship(User user);

    /**
     * @since Twitter4J 2.0.1
     */
    void destroyedFriendship(User user);

    /**
     * @since Twitter4J 2.0.1
     */
    void gotExistsFriendship(boolean exists);
    /**
     * @since Twitter4J 2.1.0
     */
    void gotShowFriendship(Relationship relationship);

    /**
     * @since Twitter4J 2.1.2
     */
    void gotIncomingFriendships(IDs ids);

    /**
     * @since Twitter4J 2.1.2
     */
    void gotOutgoingFriendships(IDs ids);

    /*Social Graph Methods*/
    void gotFriendsIDs(IDs ids);

    void gotFollowersIDs(IDs ids);

    /*Account Methods*/
    
    void verifiedCredentials(User user);
    
    void gotRateLimitStatus(RateLimitStatus rateLimitStatus);

    void updatedDeliveryDevice(User user);

    void updatedProfileColors(User user);

    /**
     * @since Twitter4J 2.1.0
     */
    void updatedProfileImage(User user);

    /**
     * @since Twitter4J 2.1.0
     */
    void updatedProfileBackgroundImage(User user);

    /**
     * @since Twitter4J 2.0.2
     */
    void updatedProfile(User user);

    /*Favorite Methods*/
    void gotFavorites(ResponseList<Status> statuses);

    void createdFavorite(Status status);

    void destroyedFavorite(Status status);

    /*Notification Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    void enabledNotification(User user);

    /**
     * @since Twitter4J 2.0.1
     */
    void disabledNotification(User user);

    /*Block Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    void createdBlock(User user);

    /**
     * @since Twitter4J 2.0.1
     */
    void destroyedBlock(User user);

    /**
     * @since Twitter4J 2.0.4
     */
    void gotExistsBlock(boolean blockExists);
    
    /**
     * @since Twitter4J 2.0.4
     */
    void gotBlockingUsers(ResponseList<User> blockingUsers);

    /**
     * @since Twitter4J 2.0.4
     */
    void gotBlockingUsersIDs(IDs blockingUsersIDs);

    /*Spam Reporting Methods*/
    void reportedSpam(User reportedSpammer);

    /*Saved Searches Methods*/
    //getSavedSearches()
    //showSavedSearch()
    //createSavedSearch()
    //destroySavedSearch()

    /*Local Trends Methods*/

    /**
     * @param locations the locations
     * @since Twitter4J 2.1.1
     */
    void gotAvailableTrends(ResponseList<Location> locations);

    /**
     * @param trends trends
     * @since Twitter4J 2.1.1
     */
    void gotLocationTrends(Trends trends);
    /*Geo Methods*/
    void gotNearByPlaces(ResponseList<Place> places);
    void gotReverseGeoCode(ResponseList<Place> places);
    void gotGeoDetails(Place place);

    /*Help Methods*/
    void tested(boolean test);

    /**
     * @param te     TwitterException
     * @param method
     */
    void onException(TwitterException te, TwitterMethod method);
    /*Search API Methods*/
    TwitterMethod SEARCH = TwitterMethod.SEARCH;

    TwitterMethod TRENDS = TwitterMethod.TRENDS;
    TwitterMethod CURRENT_TRENDS = TwitterMethod.CURRENT_TRENDS;
    TwitterMethod DAILY_TRENDS = TwitterMethod.DAILY_TRENDS;
    TwitterMethod WEEKLY_TRENDS = TwitterMethod.WEEKLY_TRENDS;

    /*Timeline Methods*/
    TwitterMethod PUBLIC_TIMELINE = TwitterMethod.PUBLIC_TIMELINE;
    TwitterMethod HOME_TIMELINE = TwitterMethod.HOME_TIMELINE;
    TwitterMethod FRIENDS_TIMELINE = TwitterMethod.FRIENDS_TIMELINE;
    TwitterMethod USER_TIMELINE = TwitterMethod.USER_TIMELINE;
    TwitterMethod MENTIONS = TwitterMethod.MENTIONS;
    TwitterMethod RETWEETED_BY_ME = TwitterMethod.RETWEETED_BY_ME;
    TwitterMethod RETWEETED_TO_ME = TwitterMethod.RETWEETED_TO_ME;
    TwitterMethod RETWEETS_OF_ME = TwitterMethod.RETWEETS_OF_ME;

    /*Status Methods*/
    TwitterMethod SHOW_STATUS = TwitterMethod.SHOW_STATUS;
    TwitterMethod UPDATE_STATUS = TwitterMethod.UPDATE_STATUS;
    TwitterMethod DESTROY_STATUS = TwitterMethod.DESTROY_STATUS;
    TwitterMethod RETWEET_STATUS = TwitterMethod.RETWEET_STATUS;
    TwitterMethod RETWEETS = TwitterMethod.RETWEETS;
    TwitterMethod RETWEETED_BY = TwitterMethod.RETWEETED_BY;
    TwitterMethod RETWEETED_BY_IDS = TwitterMethod.RETWEETED_BY_IDS;


    /*User Methods*/
    TwitterMethod SHOW_USER = TwitterMethod.SHOW_USER;
    TwitterMethod LOOKUP_USERS = TwitterMethod.LOOKUP_USERS;
    TwitterMethod SEARCH_USERS = TwitterMethod.SEARCH_USERS;
    TwitterMethod SUGGESTED_USER_CATEGORIES = TwitterMethod.SUGGESTED_USER_CATEGORIES;
    TwitterMethod USER_SUGGESTIONS = TwitterMethod.USER_SUGGESTIONS;
    TwitterMethod FRIENDS_STATUSES = TwitterMethod.FRIENDS_STATUSES;
    TwitterMethod FOLLOWERS_STATUSES = TwitterMethod.FOLLOWERS_STATUSES;

    /*List Methods*/
    TwitterMethod CREATE_USER_LIST = TwitterMethod.CREATE_USER_LIST;
    TwitterMethod UPDATE_USER_LIST = TwitterMethod.UPDATE_USER_LIST;
    TwitterMethod USER_LISTS = TwitterMethod.USER_LISTS;
    TwitterMethod SHOW_USER_LIST = TwitterMethod.SHOW_USER_LIST;
    TwitterMethod DSTROY_USER_LIST = TwitterMethod.DESTROY_USER_LIST;
    TwitterMethod USER_LIST_STATUSES = TwitterMethod.USER_LIST_STATUSES;
    TwitterMethod USER_LIST_MEMBERSHIPS = TwitterMethod.USER_LIST_MEMBERSHIPS;
    TwitterMethod USER_LIST_SUBSCRIPTIONS = TwitterMethod.USER_LIST_SUBSCRIPTIONS;

    /*List Members Methods*/
    TwitterMethod LIST_MEMBERS = TwitterMethod.LIST_MEMBERS;
    TwitterMethod ADD_LIST_MEMBER = TwitterMethod.ADD_LIST_MEMBER;
    TwitterMethod DELETE_LIST_MEMBER = TwitterMethod.DELETE_LIST_MEMBER;
    TwitterMethod CHECK_LIST_MEMBERSHIP = TwitterMethod.CHECK_LIST_MEMBERSHIP;

    /*List Subscribers Methods*/
    TwitterMethod LIST_SUBSCRIBERS = TwitterMethod.LIST_SUBSCRIBERS;
    TwitterMethod SUBSCRIBE_LIST = TwitterMethod.SUBSCRIBE_LIST;
    TwitterMethod UNSUBSCRIBE_LIST = TwitterMethod.UNSUBSCRIBE_LIST;
    TwitterMethod CHECK_LIST_SUBSCRIPTION = TwitterMethod.CHECK_LIST_SUBSCRIPTION;

    /*Direct Message Methods*/
    TwitterMethod DIRECT_MESSAGES = TwitterMethod.DIRECT_MESSAGES;
    TwitterMethod SENT_DIRECT_MESSAGES = TwitterMethod.SENT_DIRECT_MESSAGES;
    TwitterMethod SEND_DIRECT_MESSAGE = TwitterMethod.SEND_DIRECT_MESSAGE;
    TwitterMethod DESTROY_DIRECT_MESSAGES = TwitterMethod.DESTROY_DIRECT_MESSAGES;

    /*Friendship Methods*/
    TwitterMethod CREATE_FRIENDSHIP = TwitterMethod.CREATE_FRIENDSHIP;
    TwitterMethod DESTROY_FRIENDSHIP = TwitterMethod.DESTROY_FRIENDSHIP;
    TwitterMethod EXISTS_FRIENDSHIP = TwitterMethod.EXISTS_FRIENDSHIP;
    TwitterMethod SHOW_FRIENDSHIP = TwitterMethod.SHOW_FRIENDSHIP;
    TwitterMethod INCOMING_FRIENDSHIP = TwitterMethod.INCOMING_FRIENDSHIPS;
    TwitterMethod OUTGOING_FRIENDSHIPS = TwitterMethod.OUTGOING_FRIENDSHIPS;

    /*Social Graph Methods*/
    TwitterMethod FRIENDS_IDS = TwitterMethod.FRIENDS_IDS;
    TwitterMethod FOLLOWERS_IDS = TwitterMethod.FOLLOWERS_IDS;

    /*Account Methods*/
    TwitterMethod VERIFY_CREDENTIALS = TwitterMethod.VERIFY_CREDENTIALS;
    TwitterMethod RATE_LIMIT_STATUS = TwitterMethod.RATE_LIMIT_STATUS;
    TwitterMethod UPDATE_DELIVERY_DEVICE = TwitterMethod.UPDATE_DELIVERY_DEVICE;
    TwitterMethod UPDATE_PROFILE_COLORS = TwitterMethod.UPDATE_PROFILE_COLORS;
    TwitterMethod UPDATE_PROFILE_IMAGE = TwitterMethod.UPDATE_PROFILE_IMAGE;
    TwitterMethod UPDATE_PROFILE_BACKGROUND_IMAGE = TwitterMethod.UPDATE_PROFILE_BACKGROUND_IMAGE;
    TwitterMethod UPDATE_PROFILE = TwitterMethod.UPDATE_PROFILE;

    /*Favorite Methods*/
    TwitterMethod FAVORITES = TwitterMethod.FAVORITES;
    TwitterMethod CREATE_FAVORITE = TwitterMethod.CREATE_FAVORITE;
    TwitterMethod DESTROY_FAVORITE = TwitterMethod.DESTROY_FAVORITE;

    /*Notification Methods*/
    TwitterMethod ENABLE_NOTIFICATION = TwitterMethod.ENABLE_NOTIFICATION;
    TwitterMethod DISABLE_NOTIFICATION = TwitterMethod.DISABLE_NOTIFICATION;

    /*Block Methods*/
    TwitterMethod CREATE_BLOCK = TwitterMethod.CREATE_BLOCK;
    TwitterMethod DESTROY_BLOCK = TwitterMethod.DESTROY_BLOCK;
    TwitterMethod EXISTS_BLOCK = TwitterMethod.EXISTS_BLOCK;
    TwitterMethod BLOCKING_USERS = TwitterMethod.BLOCKING_USERS;
    TwitterMethod BLOCKING_USERS_IDS = TwitterMethod.BLOCKING_USERS_IDS;

    /*Spam Reporting Methods*/
    TwitterMethod REPORT_SPAM = TwitterMethod.REPORT_SPAM;

    /*Saved Searches Methods*/
    //getSavedSearches()
    //showSavedSearch()
    //createSavedSearch()
    //destroySavedSearch()

    /*Local Trends Methods*/
    TwitterMethod AVAILABLE_TRENDS = TwitterMethod.AVAILABLE_TRENDS;
    TwitterMethod LOCATION_TRENDS =  TwitterMethod.LOCATION_TRENDS;

    /*Geo Methods*/
    TwitterMethod NEAR_BY_PLACES = TwitterMethod.NEAR_BY_PLACES;
    TwitterMethod REVERSE_GEO_CODE = TwitterMethod.REVERSE_GEO_CODE;
    TwitterMethod GEO_DETAILS = TwitterMethod.GEO_DETAILS;

    /*Help Methods*/
    TwitterMethod TEST = TwitterMethod.TEST;

    void authLogin(AccessToken accessToken);

	void registerAccount(UserSession registerAccount);

	void loginBorqs(UserSession loginBorqs);

	void verifyAccountRegister(UserSession verifyAccountRegister);

	void uploadLocalFile(BackupResponse uploadLocalFile);

	void getBackupList(List<BackupResponse> backupList);

	void collectPhoneInfo(Object collectPhoneInfo);

	void backupApk(BackupResponse backupApk);

	void beginDownload(String apk, HttpURLConnection connection);
	void updateProcess(long processedsize, long filesize);
	void endDownload(String apk);

	void startProcess();

	void getBackupRecord(List<BackupRecord> backupRecord);

	void getBackupApk(List<ApkResponse> backupApk);

	void downloadFiles(boolean result);

	void connectionFailed();

	void getApksList(List<ApkResponse> apksList);
	
	void getApkDetailInformation(ApkResponse response);
	
	void getUserListWithSearchName(ArrayList<QiupuUser> users);
	
	void searchPublicCircles(ArrayList<UserCircle> circles);
	
	void getAPKListWithSort(List<ApkResponse> apkinfo);
	
	void addFriend(QiupuUser user);
	
	void deleteFriend(QiupuUser user);
	
	void deleteSelectFriends(boolean flag);
	
	void doneRequests(boolean flag);
	
	void gotoBind(boolean flag);
	
	void getFriendsList(List<QiupuUser> users);
	
	void getRequests(ArrayList<Requests> reqeusts);
	void getRequestSummary(HashMap<String, Integer> requestMap);

	void getFriendsBilateral(ArrayList<QiupuUser> users);
	void getPoolAppsList(List<ApkResponse> apkinfo);
	
	void getRecommendCategoryList(ArrayList<RecommendHeadViewItemInfo> recommendCategoryInfo);
	
	void getMasterCategoryList(ArrayList<QiupuSimpleUser> userinfo);

	void syncApksStatus(HashMap<String, SyncResponse> syncApksStatus);

	void logoutBorqs(UserSession logoutBorqs);
	void logoutAccount(boolean result);

	void backupApkRecord(BackupResponse backupApkRecord);
	
	void getFriendsCount(int count);
	
	void getUserpassword(boolean result);
	
	void updateUserpassword(boolean result);
	
	void setApkPermission(boolean result);
	
	void getGlobalApksPermission(int result);
	
	void setPhoneBookPrivacy(boolean result);
	
	void getUserDetail(QiupuUser user);
	
	void recommendFriends(boolean flag);
	
	void editUserProfile(boolean flag);
	
	void editUserProfileImage(String flag);
	
	void editPublicCircleImage(boolean flag);

	void getApkIdInServerDB(ApkResponse apkIdInServerDB);

	void postShare(Stream postShare);
	
	void postQiupuShare(Stream postShare);

	void getPostTimeLine(List<Stream> posts);
	void getPostTop(List<Stream> posts);

	void loginBorqsAccount(BorqsUserSession loginBorqs);

	void getBorqsUserPassword(boolean borqsUserPassword);

	void verifyAccountRegister(BorqsUserSession registerAccount);

	void registerAccount(BorqsUserSession registerAccount);

	void getUserFromContact(List<QiupuUser> users);
	
	void syncUserFromContact(Set<ContactSimpleInfo> res);

	void registerBorqsAccount(boolean registerBorqsAccount);
	
	void getUserYouMayKnow(ArrayList<QiupuUser> users);
	
	void getBelongCompany(ArrayList<Company> companys);
	
	void getCompanyInfo(Company company);
	
	void getPostComment(Comments.Stream_Post cominfo);
	
	void getCommentsList(List<Stream_Post> commentlist);
	void deleteComments(boolean flag);
	
	void deletePost(boolean flag);
	void updateQiupuStatus(Stream ret);
	void postToWall(Stream ret);
	void postLink(Stream ret);
	
	void postLike(boolean suc);
	void postRetweet(Stream ret);	
	void postUnLike(boolean suc);
	
	void deleteApps(boolean suc);
	
	void postRemoveFavorite(boolean suc);
	void postAddFavorite(boolean suc);
	void inviteWithMail(boolean suc);
	
	void setCircle(QiupuUser user);
	void exchangeVcard(QiupuUser user);
	
	void usersSet(boolean suc);
	
	void sendApproveRequest(boolean suc);
	
	void updateUserInfo(boolean suc);
	
	void getUserCircle(ArrayList<UserCircle> circles);
    void getDirectoryInfo(ArrayList<Employee> members);
	
	void getCompanyCircle(ArrayList<UserCircle> circles);
	
	void syncEventInfo(ArrayList<UserCircle> circles);
	void syncCircleEventInfo(ArrayList<UserCircle> circles);
	
	void createCircle(long circleId);
	void createPublicCircle(UserCircle circle);
	void editPulbicCircle(boolean suc);
	void syncPublicCirclInfo(UserCircle circle);
	void createEvent(UserCircle circle);
	
	void publicInvitePeople(ArrayList<Long> joinIds);
	
	void deletePoll(boolean suc);
	
	void deleteCircle(boolean suc);
	
	void refuseUser(boolean suc);
	
	void sendChangeRequest(boolean suc);
	
	void setNotification(HashMap<String, String> suc);
	
	void getUserInfo(QiupuUser user);
	
	void getUsersList(ArrayList<QiupuSimpleUser> users);
	

	void installIncrease(boolean installIncrease);

	void downloadIncrease(boolean downloadIncrease);

	void getRecommendsAppsList(List<ApkResponse> recommendsAppsList);
	
	void getRecommendAppsPackageName(String pacakgeName);

	void getLatestsAppsList(List<ApkResponse> latestsAppsList);
	
	void getSerachAppsList(List<ApkResponse> latestsAppsList);
	void getCategoryAppsList(List<ApkResponse> latestsAppsList);
	
	void getFavoritesAppsList(List<ApkResponse> latestsAppsList);
	
	void getLatestsPublicAppsList(List<ApkResponse> latestsAppsList);

	void getBorqsAppsList(List<ApkResponse> borqsAppsList);

	void getInstalledUserList(ArrayList<QiupuSimpleUser> installedUserList);
	
	void getNotificationValue(ArrayList<NotificationInfo> info);

    void getStreamWithComments(Stream stream);

	void photoShare(Stream photoShare);
	
	void addFriendsContact(QiupuUser user);

	void getLikeUsers(ArrayList<QiupuSimpleUser> likeUsers);
	
	void remarkSet(boolean result);

    void muteObject(boolean result);
	void reportAbuse(boolean result);
	
	void getAllAlbums(ArrayList<QiupuAlbum> albums);
	
	void getAlbum(QiupuAlbum album);
	
	void getPhotosByAlbumId(ArrayList<QiupuPhoto> photos);
    void getPhotoById(QiupuPhoto photo);

    void deletePhoto(boolean result);
    
    void postUpdateSetting(boolean result);

    void fileShare(Stream stream);
    
    void getRequestPeople(ArrayList<PublicCircleRequestUser> arraylist);
    void SearchPublicCirclePeople(ArrayList<PublicCircleRequestUser> arraylist);
    
    void applyInPublicCircle(int result);
    
    void approvepublicCirclePeople(ArrayList<Long> ids);
    void ignorepublicCirclePeople(ArrayList<Long> ids);
    void deletePublicCirclePeople(boolean result);
    void grantPublicCirclePeople(boolean result);

    void getLBSUsersInfo(ArrayList<QiupuUser> lbsUsers);
    void getNearByPeopleList(ArrayList<QiupuUser> users);
    
    void getCircleReceiveSet(RecieveSet set);
    void setCircleReceiveSet(boolean result);
    
    void syncEventThemes(ArrayList<EventTheme> themes);
    void getPollList(ArrayList<PollInfo> pollList);
    void createPoll(PollInfo pollInfo);
    void vote(PollInfo pollInfo);
    void getUserPollList(ArrayList<PollInfo> pollList);
    void getFriendPollList(ArrayList<PollInfo> pollList);
    void getPublicPollList(ArrayList<PollInfo> pollList);
    void setTopList(ArrayList<String> topIdList);
    void addPollItems(PollInfo pollInfo);
    void syncPageList(ArrayList<PageInfo> pageList);
    void createPage(PageInfo pageinfo);
    void syncPageInfo(PageInfo pageinfo);
    void editPage(PageInfo pageinfo);
    void editPageCover(PageInfo pageinfo);
    void editPageLogo(PageInfo pageinfo);
    void deletePage(boolean result);
    void searchPage(ArrayList<PageInfo> pageList);
    void followPage(PageInfo pageinfo);
    void circleAsPage(PageInfo pageinfo);
    
    void syncPublicCircles(ArrayList<UserCircle> circles);
    void syncChildCircles(ArrayList<UserCircle> circles);
    
    void SearchStream(List<Stream> posts);
    void addCategory(ArrayList<InfoCategory> infocatergory);
    
    void syncTopCircle(ArrayList<UserCircle> circles);
}
