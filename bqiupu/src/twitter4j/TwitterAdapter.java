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
 * A handy adapter of TwitterListener.
 * @see twitter4j.AsyncTwitter
 * @see twitter4j.TwitterListener
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class TwitterAdapter implements TwitterListener {
    public TwitterAdapter() {
    }
    /*Search API Methods*/
    public void searched(QueryResult result){
    }
    /**
     * @since Twitter4J 2.0.2
     */
    public void gotTrends(Trends trends) {
    }
    /**
     * @since Twitter4J 2.0.2
     */
    public void gotCurrentTrends(Trends trends) {
    }
    /**
     * @since Twitter4J 2.0.2
     */
    public void gotDailyTrends(List<Trends> trendsList) {
    }
    /**
     * @since Twitter4J 2.0.2
     */
    public void gotWeeklyTrends(List<Trends> trendsList) {
    }

    /*Timeline Methods*/
    public void gotPublicTimeline(ResponseList<Status> statuses){
    }

    /**
     * @since Twitter4J 2.0.10
     */
    public void gotHomeTimeline(ResponseList<Status> statuses) {
    }

    public void gotFriendsTimeline(ResponseList<Status> statuses){
    }
    public void gotUserTimeline(ResponseList<Status> statuses){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void gotMentions(ResponseList<Status> statuses){
    }
    /**
     * @since Twitter4J 2.0.10
     */
    public void gotRetweetedByMe(ResponseList<Status> statuses) {
    }
    /**
     * @since Twitter4J 2.0.10
     */
    public void gotRetweetedToMe(ResponseList<Status> statuses) {
    }
    /**
     * @since Twitter4J 2.0.10
     */
    public void gotRetweetsOfMe(ResponseList<Status> statuses) {
    }

    /*Status Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    public void gotShowStatus(Status statuses){
    }
    public void updatedStatus(Status statuses){
    }
    public void destroyedStatus(Status destroyedStatus){
    }
    /**
     * @since Twitter4J 2.0.10
     */
    public void retweetedStatus(Status retweetedStatus){
    }
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotRetweets(ResponseList<Status> retweets){
    }
    /**
     * @since Twitter4J 2.1.3
     */
    public void gotRetweetedBy(ResponseList<User> users) {

    }
    /**
     * @since Twitter4J 2.1.3
     */
    public void gotRetweetedByIDs(IDs ids) {

    }
    /*User Methods*/
    public void gotUserDetail(User user){
    }

    /**
     * @since Twitter4J 2.1.1
     */
    public void lookedupUsers(ResponseList<User> users) {
    }

    /**
     * @since Twitter4J 2.1.0
     */
    public void searchedUser(ResponseList<User> userList) {
    }
    /**
     * @since Twitter4J 2.1.1
     */
    public void gotSuggestedUserCategories(ResponseList<Category> categories) {
    }

    /**
     * @since Twitter4J 2.1.1
     */
    public void gotUserSuggestions(ResponseList<User> users) {
    }

    public void gotFriendsStatuses(PagableResponseList<User> users){
    }
    public void gotFollowersStatuses(PagableResponseList<User> users){
    }
    /*List Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    public void createdUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void updatedUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserLists(PagableResponseList<UserList> userLists) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotShowUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void destroyedUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserListStatuses(ResponseList<Status> statuses) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserListMemberships(PagableResponseList<UserList> userLists) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserListSubscriptions(PagableResponseList<UserList> userLists) {}

    /*List Members Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserListMembers(PagableResponseList<User> users) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void addedUserListMember(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void deletedUserListMember(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void checkedUserListMembership(User user) {}

    /*List Subscribers Methods*/
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotUserListSubscribers(PagableResponseList<User> users) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void subscribedUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void unsubscribedUserList(UserList userList) {}
    /**
     * @since Twitter4J 2.1.0
     */
    public void checkedUserListSubscription(User user) {}

    /*Direct Message Methods*/
    public void gotDirectMessages(ResponseList<DirectMessage> messages){
    }
    public void gotSentDirectMessages(ResponseList<DirectMessage> messages){
    }
    public void sentDirectMessage(DirectMessage message){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void destroyedDirectMessage(DirectMessage message){
    }

    /*Friendship Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    public void createdFriendship(User user){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void destroyedFriendship(User user){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void gotExistsFriendship(boolean exists) {
    }
    /**
     * @since Twitter4J 2.1.0
     */
    public void gotShowFriendship(Relationship relationship) {
    }
    /**
     * @since Twitter4J 2.1.2
     */
    public void gotIncomingFriendships(IDs ids) {
    }
    /**
     * @since Twitter4J 2.1.2
     */
    public void gotOutgoingFriendships(IDs ids) {
    }


    /*Social Graph Methods*/
    public void gotFriendsIDs(IDs ids){
    }

    public void gotFollowersIDs(IDs ids){
    }

    /*Account Methods*/

	public void verifiedCredentials(User user) {
	}
	
    public void gotRateLimitStatus(RateLimitStatus status) {
    }

    public void updatedDeliveryDevice(User user) {
    }

    public void updatedProfileColors(User user) {
    }
    /**
     * @since Twitter4J 2.1.0
     */
    public void updatedProfileImage(User user) {
    }
    /**
     * @since Twitter4J 2.1.0
     */
    public void updatedProfileBackgroundImage(User user) {
    }
    /**
     * @since Twitter4J 2.0.2
     */
    public void updatedProfile(User user){
    }
    /*Favorite Methods*/
    public void gotFavorites(ResponseList<Status> statuses){
    }
    public void createdFavorite(Status status){
    }
    public void destroyedFavorite(Status status){
    }

    /*Notification Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    public void enabledNotification(User user){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void disabledNotification(User user){
    }
    /*Block Methods*/
    /**
     * @since Twitter4J 2.0.1
     */
    public void createdBlock(User user){
    }
    /**
     * @since Twitter4J 2.0.1
     */
    public void destroyedBlock(User user){
    }
    /**
     * @since Twitter4J 2.0.4
     */
    public void gotExistsBlock(boolean blockExists){
    }

    /**
     * @since Twitter4J 2.0.4
     */
    public void gotBlockingUsers(ResponseList<User> blockingUsers){
    }

    /**
     * @since Twitter4J 2.0.4
     */
    public void gotBlockingUsersIDs(IDs blockingUsersIDs) {
    }

    /*Spam Reporting Methods*/

    public void reportedSpam(User reportedSpammer) {
    }


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
    public void gotAvailableTrends(ResponseList<Location> locations) {
    }

    /**
     * @param trends trends
     * @since Twitter4J 2.1.1
     */
    public void gotLocationTrends(Trends trends){
    }

    /*Geo Methods*/
    public void gotNearByPlaces(ResponseList<Place> places){
    }
    public void gotReverseGeoCode(ResponseList<Place> places){
    }
    public void gotGeoDetails(Place place) {
    }

    /*Help Methods*/
    public void tested(boolean test){
    }

    /**
     * @param ex TwitterException
     * @param method
     */
    public void onException(TwitterException ex, TwitterMethod method) {
    }
    public void authLogin(AccessToken accessToken) {     
    }

	public void registerAccount(UserSession registerAccount) {

	}
	public void loginBorqs(UserSession loginBorqs) {
		
	}
	public void verifyAccountRegister(UserSession verifyAccountRegister) {
		
	}
	public void uploadLocalFile(BackupResponse uploadLocalFile) {
		
	}
	public void getBackupList(List<BackupResponse> backupList) {
		
	}
	public void collectPhoneInfo(Object collectPhoneInfo) {
		
	}
	public void backupApk(BackupResponse backupApk) {
		
	}

	public void updateProcess(long processedsize, long filesize) {
		
	}
	
	public void beginDownload(String apk, HttpURLConnection connection)
    {
		
    }
    public void endDownload(String apk)
    {
    	
    }
	public void startProcess() {
		
	}
	
	public void getBackupRecord(List<BackupRecord> backupRecord) {
		
	}
	public void getBackupApk(List<ApkResponse> backupApk) {
		
	}
	
	public void downloadFiles(boolean result) {
		
	}
	public void connectionFailed() {
		
	}
	
	public void getApksList(List<ApkResponse> apksList) {
	}

	public void getApkDetailInformation(ApkResponse apkInfo){
		
	}
	
	public void getUserListWithSearchName(ArrayList<QiupuUser> users) {
		
	}
	
	public void getAPKListWithSort(List<ApkResponse> apkinfo) {
		
	}
	public void addFriend(QiupuUser user) {
		
	}
	
	public void getFriendsList(List<QiupuUser> users) {
		
	}
	public void syncApksStatus(HashMap<String, SyncResponse> syncApksStatus) {
		
	}
	public void deleteFriend(QiupuUser user) {
		
	}
	public void logoutBorqs(UserSession logoutBorqs) {
		
	}
	public void getPoolAppsList(List<ApkResponse> apkinfo) {
		
	}
	public void getRecommendsAppsList(List<ApkResponse> recommendsAppsList)
	{
		
	}

	public void getLatestsAppsList(List<ApkResponse> latestsAppsList)
	{
		
	}
	
	public void getLatestsPublicAppsList(List<ApkResponse> latestsAppsList)
	{
		
	}	

	public void getBorqsAppsList(List<ApkResponse> borqsAppsList)
	{
		
	}
	
	public void backupApkRecord(BackupResponse backupApkRecord) {
		
	}
	public void deleteSelectFriends(boolean flag) {
		
	}
	public void getFriendsCount(int count) {
		
	}
	public void getUserpassword(boolean result) {
		
	}
	public void updateUserpassword(boolean result) {
		
	}
	public void setApkPermission(boolean result) {
		
	}
	public void getUserDetail(QiupuUser user) {
		
	}
	public void getApkIdInServerDB(ApkResponse info) {
		
	}
	public void postShare(Stream post) {
	}
	public void getPostTimeLine(List<Stream> posts) {
	}
	
	public void loginBorqsAccount(BorqsUserSession loginBorqs) {
		
	}
	public void verifyAccountRegister(BorqsUserSession registerAccount) {
		
	}
	public void registerAccount(BorqsUserSession registerAccount) {
		
	}
	public void getBorqsUserPassword(boolean borqsUserPassword) {
		
	}
	public void getUserFromContact(List<QiupuUser> users) {
		
	}
	public void registerBorqsAccount(boolean registerAccount) {
		
	}
	public void getUserYouMayKnow(ArrayList<QiupuUser> users) {
		
	}
	public void getPostComment(Comments.Stream_Post cominfo) {
		
	}
	public void getCommentsList(List<Stream_Post> commentlist) {
		
	}
	
	public void postToWall(Stream post) {}
	public void postLink(Stream post) {}
	public void updateQiupuStatus(Stream ret) {	
		
	}
	public void postLike(boolean suc) {
		
	}
	public void postUnLike(boolean suc) {		
	}
	
	public void postRemoveFavorite(boolean suc)
	{
		
	}
	public void postAddFavorite(boolean suc)
	{
		
	}
	
	public void postRetweet(Stream retweet) {}
	
	public void installIncrease(boolean installIncrease){}

	public void downloadIncrease(boolean downloadIncrease){}
	
	public void getRecommendAppsPackageName(String pacakgeName) {
		
	}
	public void editUserProfile(boolean flag) {
		
	}
	public void editUserProfileImage(String flag) {
		
	}
	public void getFavoritesAppsList(List<ApkResponse> latestsAppsList) {
		
	}
	public void recommendFriends(boolean flag) {
		
	}
	public void postQiupuShare(Stream postShare) {
	}
	public void deleteComments(boolean flag) {
	}
	public void getSerachAppsList(List<ApkResponse> latestsAppsList) {
	}
	public void deleteApps(boolean suc) {
	}
	public void inviteWithMail(boolean suc) {
	}
	public void deletePost(boolean flag) {
	}
	public void getFriendsBilateral(ArrayList<QiupuUser> users) {
	}
	public void getCategoryAppsList(List<ApkResponse> latestsAppsList) {
	}
	public void setCircle(QiupuUser user) {
	}
	public void getUserInfo(QiupuUser user) {
	}
	public void updateUserInfo(boolean suc) {}
	public void getInstalledUserList(ArrayList<QiupuSimpleUser> installedUserList) {
	}
	public void logoutAccount(boolean result) {
	}
	public void syncUserFromContact(boolean res)
	{
	}
	public void getRecommendCategoryList(
			ArrayList<RecommendHeadViewItemInfo> recommendCategoryInfo)
	{
	}
	public void getMasterCategoryList(ArrayList<QiupuSimpleUser> userinfo)
	{}
	public void getUserCircle(ArrayList<UserCircle> circles)
	{}

    @Override
    public void getDirectoryInfo(ArrayList<Employee> members) {
    }

    public void createCircle(long circleId)
	{}
	public void setPhoneBookPrivacy(boolean result)
	{}
	public void deleteCircle(boolean suc)
	{}
	public void refuseUser(boolean suc)
	{	}
	public void getRequests(ArrayList<Requests> reqeusts) {}
	public void usersSet(boolean suc)
	{}
	public void doneRequests(boolean flag)
	{}
	public void sendApproveRequest(boolean suc)
	{}
	@Override
	public void sendChangeRequest(boolean suc)
	{}
	@Override
	public void gotoBind(boolean flag)
	{}
	@Override
	public void setNotification(HashMap<String, String> suc)
	{}
	@Override
	public void getNotificationValue(ArrayList<NotificationInfo> info)
	{}

    @Override
    public void getStreamWithComments(Stream stream) {
    }
	@Override
	public void getGlobalApksPermission(int result) {
	}
    @Override
    public void syncUserFromContact(Set<ContactSimpleInfo> res) {
        
    }
    @Override
    public void photoShare(Stream photoShare)
    {
    	
    }
	@Override
	public void addFriendsContact(QiupuUser user) {
	}
	
	@Override
	public void getLikeUsers(ArrayList<QiupuSimpleUser> likeUsers){
		
	}
	@Override
	public void getUsersList(ArrayList<QiupuSimpleUser> users) {
	}
    @Override
    public void remarkSet(boolean result) {  }
	@Override
	public void createPublicCircle(UserCircle circle) { }
	@Override
	public void publicInvitePeople(ArrayList<Long> joinIds) { }
	@Override
	public void editPulbicCircle(boolean suc) { }
	@Override
	public void syncPublicCirclInfo(UserCircle circle) {
	}
    @Override
    public void muteObject(boolean result) {}
    @Override
    public void reportAbuse(boolean result) {}
	@Override
	public void getAllAlbums(ArrayList<QiupuAlbum> albums) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void getPhotosByAlbumId(ArrayList<QiupuPhoto> photos) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void postUpdateSetting(boolean result) {
    }

    @Override
    public void fileShare(Stream stream) {
    }

    @Override
    public void getRequestPeople(ArrayList<PublicCircleRequestUser> arraylist) { }
    @Override
    public void editPublicCircleImage(boolean flag) { }
	@Override
	public void getAlbum(QiupuAlbum album) {
		// TODO Auto-generated method stub
		
	}
    @Override
    public void applyInPublicCircle(int result) { }
    @Override
    public void searchPublicCircles(ArrayList<UserCircle> circles) {
    }
    @Override
    public void approvepublicCirclePeople(ArrayList<Long> ids) {
    }
    @Override
    public void deletePublicCirclePeople(boolean result) {
    }
    @Override
    public void grantPublicCirclePeople(boolean result) {
    }
    @Override
    public void ignorepublicCirclePeople(ArrayList<Long> ids) {
    }

    @Override
    public void getLBSUsersInfo(ArrayList<QiupuUser> lbsUsers) {
    }

    @Override
    public void exchangeVcard(QiupuUser user) {
    }

    @Override
    public void getNearByPeopleList(ArrayList<QiupuUser> users) {
    }
	@Override
	public void getCircleReceiveSet(RecieveSet set) {
	}
	@Override
	public void setCircleReceiveSet(boolean result) {
	}
	@Override
	public void deletePhoto(boolean result) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void syncEventInfo(ArrayList<UserCircle> circles) {
	}
	@Override
	public void SearchPublicCirclePeople(
			ArrayList<PublicCircleRequestUser> arraylist) {
	}
	@Override
	public void createEvent(UserCircle circle) {
	}
	@Override
	public void syncEventThemes(ArrayList<EventTheme> themes) {
	}
    @Override
    public void getPhotoById(QiupuPhoto photo) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void getPollList(ArrayList<PollInfo> pollList) {
    }
    @Override
    public void vote(PollInfo pollInfo) {
    }
    @Override
    public void getUserPollList(ArrayList<PollInfo> pollList) {
    }
    @Override
    public void getFriendPollList(ArrayList<PollInfo> pollList) {
    }
    @Override
    public void getPublicPollList(ArrayList<PollInfo> pollList) {
    }
    @Override
    public void getPostTop(List<Stream> posts) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void deletePoll(boolean suc) {
        
    }
	@Override
	public void getBelongCompany(ArrayList<Company> companys) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void getCompanyInfo(Company company) {
		// TODO Auto-generated method stub
		
	}
    @Override
    public void setTopList(ArrayList<String> topIdList) {
        
    }
	@Override
	public void getCompanyCircle(ArrayList<UserCircle> circles) {
		// TODO Auto-generated method stub
		
	}
    @Override
    public void addPollItems(PollInfo pollInfo) {
    }
	@Override
	public void syncPageList(ArrayList<PageInfo> pageList) {
		
	}
	@Override
	public void createPage(PageInfo pageinfo) {
		
	}
	@Override
	public void syncPageInfo(PageInfo pageinfo) {
	}
	@Override
	public void editPage(PageInfo pageinfo) {
	}
	@Override
	public void editPageCover(PageInfo pageinfo) {
	}
	@Override
	public void editPageLogo(PageInfo pageinfo) {
	}
    @Override
    public void createPoll(PollInfo pollInfo) {
        
    }
	@Override
	public void deletePage(boolean result) {
	}
	@Override
	public void searchPage(ArrayList<PageInfo> pageList) {
	}
	@Override
	public void followPage(PageInfo pageinfo) {
	}
    @Override
    public void getRequestSummary(HashMap<String, Integer> requestMap) {
    }
	@Override
	public void circleAsPage(PageInfo pageinfo) {
	}
	@Override
	public void syncPublicCircles(ArrayList<UserCircle> circles) {
	}
	@Override
	public void syncCircleEventInfo(ArrayList<UserCircle> circles) {
	}
	@Override
	public void syncChildCircles(ArrayList<UserCircle> circles) {
	}
	@Override
	public void SearchStream(List<Stream> posts) {
	}
	@Override
	public void addCategory(ArrayList<InfoCategory> infocatergory) {
	}
	@Override
	public void syncTopCircle(ArrayList<UserCircle> circles) {
	}

}
