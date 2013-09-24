package twitter4j;

import static twitter4j.TwitterMethod.ADD_FRIEND;
import static twitter4j.TwitterMethod.ADD_FRIEND_CONTACT;
import static twitter4j.TwitterMethod.GET_ALBUM;
import static twitter4j.TwitterMethod.GET_ALL_ALBUM;
import static twitter4j.TwitterMethod.GET_APK_DETAIL_INFORMATION;
import static twitter4j.TwitterMethod.GET_APK_LIST;
import static twitter4j.TwitterMethod.GET_BACKUP_APK;
import static twitter4j.TwitterMethod.GET_BACKUP_FILE;
import static twitter4j.TwitterMethod.GET_BACKUP_RECORD;
import static twitter4j.TwitterMethod.GET_NEAR_BY_PEOPLE;
import static twitter4j.TwitterMethod.GET_NOTIFICATION_VALUE;
import static twitter4j.TwitterMethod.GET_PHOTOS_ALBUM;
import static twitter4j.TwitterMethod.GET_PHOTO_BYID;
import static twitter4j.TwitterMethod.GET_POLL_LIST;
import static twitter4j.TwitterMethod.GET_POST_TIMELINE;
import static twitter4j.TwitterMethod.GET_REQUESTS;
import static twitter4j.TwitterMethod.GET_USERLIST_BY_SEARCHNAME;
import static twitter4j.TwitterMethod.LIKE_USERS;
import static twitter4j.TwitterMethod.LOGIN_BORQS;
import static twitter4j.TwitterMethod.MUTE_OBJECT;
import static twitter4j.TwitterMethod.PHOTOS_DEL;
import static twitter4j.TwitterMethod.POST_OTHER_FILE_VALUE;
import static twitter4j.TwitterMethod.POST_SHARE;
import static twitter4j.TwitterMethod.POST_UPDATEACTION_VALUE;
import static twitter4j.TwitterMethod.REGISTER_ACCOUNT;
import static twitter4j.TwitterMethod.REMARK_SET;
import static twitter4j.TwitterMethod.REPORT_ABUSE;
import static twitter4j.TwitterMethod.SYNC_APKS_STATUS;
import static twitter4j.TwitterMethod.SYNC_THEME;
import static twitter4j.TwitterMethod.UPLOAD_FILE;
import static twitter4j.TwitterMethod.VERIFY_ACCOUNT;
import static twitter4j.TwitterMethod.GET_BELONG_COMPANY;
import static twitter4j.TwitterMethod.COMPANY_SHOW;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.Authorization;
import twitter4j.http.RequestToken;
import twitter4j.threadpool.QueuedThreadPool;
import android.util.Log;

import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.AccountListener;
import com.borqs.qiupu.QiupuConfig;

public class AsyncQiupu extends TwitterOAuthSupportBase{
	private static final long serialVersionUID = 5385459489508468900L;
	final static String TAG = "AsyncQiupu";
	private Twitter twitter;
	//private boolean shutdown = false;
	//private static transient Dispatcher dispatcher;
	
    public AsyncQiupu(Configuration conf, Authorization auth, TwitterListener listener) {
        super(conf, auth);
        
        Log.d(TAG, TAG);
        twitter = new TwitterFactory(conf).getInstance(auth);
       
    }
    /*
    public void shutdown(){
    	Log.d(TAG, "shutdown:"+shutdown);
        synchronized (AsyncQiupu.class) {
            if (shutdown) {
                throw new IllegalStateException("Already shut down");
            }
            
            getThreadPool().shutdown();
            dispatcher = null;
            super.shutdown();
            shutdown = true;
        }
    }*/
    
    static final int ImagePoolSize = 10;
    static QueuedThreadPool threadpool=null;
    public static  QueuedThreadPool getThreadPool()
	{
		synchronized(QueuedThreadPool.class)
		{
	        if(null == threadpool)
	        {
	            threadpool = new QueuedThreadPool(ImagePoolSize);
	            threadpool.setName("AyncQiupu--Thread--Pool");
	            try 
	            {
	            	threadpool.start();
	            } catch (Exception e) {}
	            
	            Runtime.getRuntime().addShutdownHook(new Thread(TAG)
	            {
	                public void run() 
	                {
	                    if(threadpool != null)
	                    {
	                        try {
	                            threadpool.stop();
	                        } catch (Exception e) {}
	                    }
	                }
	            });
	        }
		}		
	    return threadpool;
	}
   
    
    /*
    private Dispatcher getThreadPool(){
        if(shutdown){
            throw new IllegalStateException("Already shut down");
        }
        if (null == dispatcher) {
            dispatcher = new DispatcherFactory(conf).getInstance();
        }
        return dispatcher;
    }*/
    
	@Override
	public AccessToken getOAuthAccessToken() throws TwitterException {
		return twitter.getOAuthAccessToken();
	}

	@Override
	public AccessToken getOAuthAccessToken(String oauthVerifier)
			throws TwitterException {
		return twitter.getOAuthAccessToken(oauthVerifier);
	}

	@Override
	public AccessToken getOAuthAccessToken(RequestToken requestToken)
			throws TwitterException {
		return twitter.getOAuthAccessToken(requestToken);
	}

	@Override
	public AccessToken getOAuthAccessToken(RequestToken requestToken,
			String oauthVerifier) throws TwitterException {
		return twitter.getOAuthAccessToken(requestToken, oauthVerifier);
	}

	@Override
	public AccessToken getOAuthAccessToken(String token, String tokenSecret)
			throws TwitterException {
		return twitter.getOAuthAccessToken(token, tokenSecret);
	}

	@Override
	public AccessToken getOAuthAccessToken(String token, String tokenSecret,
			String pin) throws TwitterException {
		return twitter.getOAuthAccessToken(token, tokenSecret, pin);
	}

	@Override
	public RequestToken getOAuthRequestToken() throws TwitterException {
		return twitter.getOAuthRequestToken();
	}

	@Override
	public RequestToken getOAuthRequestToken(String callbackUrl)
			throws TwitterException {
		return twitter.getOAuthRequestToken(callbackUrl);
	}

	@Override
	public void setOAuthAccessToken(AccessToken accessToken) {
		twitter.setOAuthAccessToken(accessToken);
	}

	@Override
	public void setOAuthAccessToken(String token, String tokenSecret) {
		twitter.setOAuthAccessToken(token, tokenSecret);
	}

	@Override
	public void setOAuthConsumer(String consumerKey, String consumerSecret) {
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
	}
	
    abstract class AsyncTask implements Runnable {
        TwitterListener listener;
        TwitterMethod method;

        AsyncTask(TwitterMethod method, TwitterListener listener) {
            this.method = method;
            this.listener = listener;
        }

        abstract void dispatch(TwitterListener listener) throws TwitterException;

        public void run() 
        {
            try 
            {
                   dispatch(listener);
            } 
            catch (TwitterException te) 
            {
                if (null != listener) 
                {
                	if(alistener != null)
                	{
                		AccountListener mAlistener = alistener.get();
                		if(mAlistener != null)
                		{
                			mAlistener.filterInvalidException(te);
                		}
                	}                	
                	listener.onException(te,method);
                }
            }            
        }
    }

    public void registerAccount(final String username, final String pwd, final String nickname,
			final String phonenumber, final String urlname, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(REGISTER_ACCOUNT, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.registerAccount(twitter.registerAccount(username,pwd,nickname,urlname, phonenumber));
            }
        });
	}
    
    public void loginBorqs(final String username, final String pwd,
			TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.loginBorqs(twitter.loginBorqs(username,pwd));
            }
        });		
	}
    
    public void getUserPasswrod(final String username,TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getUserpassword(twitter.getUserPassword(username));
            }
        });		
	}
    
    public void updateUserPasswrod(final String sessionid,final String newpassword, final String oldpassword, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
           public void dispatch(TwitterListener listener) throws TwitterException {
               listener.updateUserpassword(twitter.updateUserPassword(sessionid, newpassword, oldpassword));
           }
       });		
	}
    
    public void setApkPermission(final String sessionid,final String packageName, final int visibility, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
          public void dispatch(TwitterListener listener) throws TwitterException {
              listener.setApkPermission(twitter.setApkPermission(sessionid, packageName, visibility));
          }
      });		
	}
    
    public void setGlobalApksPermission(final String sessionid,final int visibility, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.setApkPermission(twitter.setGlobalApksPermission(sessionid, visibility));
    		}
    	});		
    }
    
    public void getGlobalApksPermission(final String sessionid, final long uid, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.getGlobalApksPermission(twitter.getGlobalApksPermission(sessionid, uid));
    		}
    	});		
    }
    
    public void setPhoneBookPrivacy(final String sessionid,final HashMap<String, String> visibility, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(LOGIN_BORQS, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.setPhoneBookPrivacy(twitter.setPhoneBookPrivacy(sessionid, visibility));
    		}
    	});		
    }
    
	public void verifyAccountRegister(final String username, final String verifycode,
			TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(VERIFY_ACCOUNT, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.verifyAccountRegister(twitter.verifyAccountRegister(username,verifycode));
            }
        });	
	}
	
	public void uploadLocalFile(final File file,final String session_id,final String type,TwitterListener listener){
		getThreadPool().dispatch(new AsyncTask(UPLOAD_FILE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.uploadLocalFile(twitter.uploadLocalFile(file,session_id,type));
            }
        });	
	}
	
	public void uploadLocalFile(final File file,final String session_id,final String type,final String apkinfo,TwitterListener listener){
		getThreadPool().dispatch(new AsyncTask(UPLOAD_FILE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.uploadLocalFile(twitter.uploadLocalFile(file,session_id,type,apkinfo));
            }
        });	
	}
	
//	public void backupApk(final File file, final File iconFile, final String uid,final String session_id,final String apkinfo,final long identifyid,TwitterListener listener){
//		getThreadPool().dispatch(new AsyncTask(UPLOAD_FILE, listener) {
//            public void dispatch(TwitterListener listener) throws TwitterException {
//                try {
//					listener.backupApk(twitter.backupApk(file,iconFile,uid,session_id,apkinfo,identifyid,listener));
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
//            }
//        });
//	}
	
	public void getBackupList(final String sessionid, final String type,TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(GET_BACKUP_FILE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getBackupList(twitter.getBackupList(sessionid,type));
            }
        });	
	}

	public void collectPhoneInfo(final String mMSISDN, final String mIMEI, final String mIMSI,
			final String mFirmwareVersion, final String mModel, final String mWifiMacAddress,
			final String mEmail, final TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(GET_BACKUP_FILE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.collectPhoneInfo(twitter.collectPhoneInfo(mMSISDN,mIMEI,mIMSI,mFirmwareVersion,mModel,mWifiMacAddress,mEmail));
            }
        });	
	}
	
	public void getBackupRecord(final String sessionid, final long maxid,TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(GET_BACKUP_RECORD, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getBackupRecord(twitter.getBackupRecord(sessionid,maxid));
            }
        });		
    
	}

	public void getBackupApk(final String sessionid, final long recordid, final long maxid,final String localPath, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(GET_BACKUP_APK, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getBackupApk(twitter.getBackupApk(sessionid,recordid,maxid,localPath));
            }
        });				
	}

	//get backuped apk list by userid
	public void getApksList(final String sessionid,final String uid,final String reason,final int page,final int pagenumber,TwitterListener listener){
		getThreadPool().dispatch(new AsyncTask(GET_APK_LIST, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
            	Configuration conf = ConfigurationContext.getInstance();
                listener.getApksList(twitter.getApksList(sessionid,uid,reason,page,pagenumber));
            }
        });	
	}
	
	public void getApkDetailInformation(final String session_id, final String apkid, final boolean needsubversion, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(GET_APK_DETAIL_INFORMATION, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getApkDetailInformation(twitter.getApkDetailInformation(session_id, apkid, needsubversion));
            }
        });			
	}
	
	public void getUserListWithSearchName(final String sessionid,final String userName,
								final String nickName, final String screenName, final int page, final int count, TwitterListener listener) {
		 
		 getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getUserListWithSearchName(twitter.getUserListWithSearchName(sessionid, userName,nickName,screenName, page, count));
			 }
		 });	
	}
	
	public void searchPublicCircles(final String sessionid,final String circleName, final int page, final int count, TwitterListener listener) {
	    getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
	        public void dispatch(TwitterListener listener) throws TwitterException {
	            listener.searchPublicCircles(twitter.searchPublicCircles(sessionid, circleName, page, count));
	        }
	    });    
	}
	
	public void getInstalledUserList(final String sessionid,final String packagename,final String reason,final int page, final int count, TwitterListener listener) {

			getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
			public void dispatch(TwitterListener listener) throws TwitterException {
			    listener.getInstalledUserList(twitter.getInstalledUserList(sessionid, packagename,reason, page, count));
			}
			});	
    }

	public void postFeedbackAsync(final String sessionid, final String content,
                                  final String appData, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(TwitterMethod.POST_WALL, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
              listener.postToWall(twitter.postFeedback(sessionid, content, appData));
			 }
		 });	
	}
	public void postToMultiWallAsync(final String sessionid, final String friendIds,
                                     final String content, final String appData, final boolean issecretly,
                                     final boolean canComment, final boolean canLike, final boolean canShare,
                                     final boolean isTop, final boolean sendEmail, final boolean sendSms, final String categoryId, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(TwitterMethod.POST_WALL, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                 // TODO: if BpcApiUtils.APK_POST is proper value for all case.
            listener.postToWall(twitter.postToMultiWall(sessionid, friendIds,content,
                    appData, BpcApiUtils.TEXT_POST, issecretly, canComment, canLike, canShare, isTop, sendEmail, sendSms, categoryId));
			 }
		 });	
	}
	
	public void postPhotoAsync(final String sessionid, final String message, final String friendIds,
			final File file,final String photo_id,
			final String caption, final String appData,
			final boolean issecretly, final boolean canComment,
			final boolean canLike, final boolean canShare, final boolean isTop, 
			final boolean sendEmail, final boolean sendSms, final String categoryId, TwitterListener listener) {
		        getThreadPool().dispatch( new AsyncTask(TwitterMethod.POST_WALL, listener) {
					    public void dispatch(TwitterListener listener)throws TwitterException 
					    {						
					    	listener.photoShare(twitter.photoShare(sessionid, message,
									friendIds, file, photo_id, BpcApiUtils.IMAGE_POST, caption, appData,
									issecretly, canComment, canLike, canShare, isTop, sendEmail, sendSms, categoryId));
						        }});
	}
	
	
	
	public void postLinkAsync(final String sessionid,final long fromId, final String friendIds,final String content,
                              final String title, final String url, final boolean issecretly, final boolean canComment,
                              final boolean canLike, final boolean canShare, final boolean isTop, final boolean sendEmail,
                              final boolean sendSms, final String categoryId, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(TwitterMethod.POST_LINK, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                 listener.postLink(twitter.postLink(sessionid, fromId, friendIds,content, title, url,
                         issecretly, canComment, canLike, canShare, isTop, sendEmail, sendSms, categoryId));
			 }
		 });	
	}
	
	public void statusUpdateAsync(final String sessionid,final long fromId,final String content,
			final boolean isSendPost,TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(TwitterMethod.STATUS_UPDATE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
            listener.updateQiupuStatus(twitter.statusupdate(sessionid, fromId,content,isSendPost));
			 }
		 });	
	}

	
	public void recommendFriends(final String sessionid,final long touid,final String selectuid,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
             listener.recommendFriends(twitter.recommendFriends(sessionid, touid,selectuid));
			 }
		 });	
	}
	
	public void editUserProfileImage(final String sessionid,final File file,TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
            listener.editUserProfileImage(twitter.editUserProfileImage(sessionid, file));
			 }
		 });	
	}
	
	public void editPublicCircleImage(final String sessionid,final long circleId, final File file,TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
           listener.editPublicCircleImage(QiupuConfig.isEventIds(circleId) ? twitter.editEventImage(sessionid, circleId, file) : twitter.editPublicCircleImage(sessionid, circleId, file));
            }
        });    
   }
	
	public void getFriendsBilateral(final String sessionid,final long otheruid, final int page,final int count, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getFriendsBilateral(twitter.getFriendsBilateral(sessionid, otheruid, page, count));
			 }
		 });
	}
	
	public void getFriendsListPage(final String sessionid,final long uid, final String circles, final int page,final int count,final boolean isfollowing, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getFriendsList(twitter.getFriendsListPage(sessionid, uid, circles, page, count,isfollowing));
			 }
		 });
	}
	
	public void getCircleReceiveSet(final String ticket, final long circleid, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getCircleReceiveSet(QiupuConfig.isEventIds(circleid) ? twitter.getEventReceiveSet(ticket, circleid) : twitter.getCircleReceiveSet(ticket, circleid));
			 }
		 });
	}
	
	public void setCircleReceiveSet(final String ticket, final long circleid, final int enable, final String phone, final String email, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.setCircleReceiveSet(QiupuConfig.isEventIds(circleid) ? twitter.setEventReceiveSet(ticket, circleid, enable, phone, email) : twitter.setCircleReceiveSet(ticket, circleid, enable, phone, email));
			 }
		 });
	}
	
	public void getRequestPeople(final String sessionid, final long circleId, final int status, final int page, final int count, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getRequestPeople(QiupuConfig.isEventIds(circleId) ? twitter.getEventRequestPeople(sessionid, circleId, status, page, count) 
                		                                                   : twitter.getRequestPeople(sessionid, circleId, status, page, count));
            }
        });
   }
	
	public void SearchPublicCirclePeople(final String sessionid, final long circleId, final int status, final String key, final int page, final int count, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.SearchPublicCirclePeople(QiupuConfig.isEventIds(circleId) ? twitter.SearchEventPeople(sessionid, circleId, status, key, page, count) 
                		                                                   : twitter.searchPublicCirclePeople(sessionid, circleId, status, key, page, count));
            }
        });
   }
	
	public void approvepublicCirclePeople(final String sessionid, final long circleId, final String userIds, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.approvepublicCirclePeople(QiupuConfig.isEventIds(circleId) ? twitter.approveEventPeople(sessionid, circleId, userIds)
                		                                                            : twitter.approvepublicCirclePeople(sessionid, circleId, userIds));
            }
        });
   }
	
	public void ignorepublicCirclePeople(final String sessionid, final long circleId, final String userIds, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.ignorepublicCirclePeople(QiupuConfig.isEventIds(circleId) ? twitter.ignoreEventPeople(sessionid, circleId, userIds)
                		                                                           : twitter.ignorepublicCirclePeople(sessionid, circleId, userIds));
            }
        });
   }
	
	public void deletePublicCirclePeople(final String sessionid, final long circleId, final String userIds, final String admins, TwitterListener listener){
       getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
           public void dispatch(TwitterListener listener) throws TwitterException {
               listener.deletePublicCirclePeople(QiupuConfig.isEventIds(circleId) ? twitter.deleteEventPeople(sessionid, circleId, userIds, admins)
            		                                                              : twitter.deletePublicCirclePeople(sessionid, circleId, userIds, admins));
           }
       });
  }
	
	public void grantPublicCirclePeople(final String sessionid, final long circleId, final String adminIds, final String memberIds, TwitterListener listener){
	       getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
	           public void dispatch(TwitterListener listener) throws TwitterException {
	               listener.grantPublicCirclePeople(QiupuConfig.isEventIds(circleId) ? twitter.grantEventPeople(sessionid, circleId, adminIds, memberIds)
	            		                                                             : twitter.grantPublicCirclePeople(sessionid, circleId, adminIds, memberIds));
	           }
	       });
	  }
	
	public void getRequests(final String sessionid,final String types, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getRequests(twitter.getRequests(sessionid, types));
			 }
		 });
	}
	
	public void gotoBind(final String sessionid,final String type, final String value, final long userid, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.gotoBind(twitter.gotoBind(sessionid, type, value, userid));
			 }
		 });
	}
	
	public void doneRequests(final String sessionid,final String requestid, final int type, final String data, final boolean isaccept, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.doneRequests(twitter.doneRequests(sessionid, requestid, type, data, isaccept));
			 }
		 });
	}
	
	public void getUserYouMayKnow(final String sessionid,final int maxCount, final boolean getback, TwitterListener listener){
		getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			public void dispatch(TwitterListener listener) throws TwitterException {
				listener.getUserYouMayKnow(twitter.getUserYouMayKnow(sessionid, maxCount, getback));
			}
		});
	}
	
	public void getBelongCompany(final String sessionid, TwitterListener listener){
		getThreadPool().dispatch(new AsyncTask(GET_BELONG_COMPANY, listener) {
			public void dispatch(TwitterListener listener) throws TwitterException {
				listener.getBelongCompany(twitter.getBelongCompany(sessionid));
			}
		});
	}
	public void getCompanyInfo(final String sessionid,final long company_id, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(COMPANY_SHOW, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getCompanyInfo(twitter.getCompanyInfo(sessionid,company_id));
			 }
		 });
	}
	
	public void getPoolAppsList(final String sessionid, final String category,final  String sort,final int page, final int count, final boolean is_get_topic, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getPoolAppsList(twitter.getPoolAppsList(sessionid, category,sort, page, count, is_get_topic));
			 }
		 });
	}
	
	//TODO custom api for get Recommend category
	public void getRecommendCategoryList(final String sessionid, final boolean isSuggest, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getRecommendCategoryList(twitter.getRecommendCategoryList(sessionid, isSuggest));
			 }
		 });
	}
	
	public void getMasterCategoryList(final String sessionid, final String sub_category, final int page, final int count, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getMasterCategoryList(twitter.getMasterCategoryList(sessionid, sub_category, page, count));
			 }
		 });
	}
	
	public void getRecommendAppsList(final String sessionid,final int page,final int count, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getRecommendsAppsList(twitter.getRecommendLatestedApksList(sessionid, page,count));
			 }
		 });
	}
	
	public void getSerachAppsList(final String sessionid,final String apkname,final int page, final int pagenumber,TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getSerachAppsList(twitter.getSerachAppsList(sessionid, apkname,page, pagenumber));
			 }
		 });
	}
	
	public void getFavoritesAppsList(final String sessionid,final long uid,final String reason, final int page,final int pagenumber,TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getFavoritesAppsList(twitter.getFavoritesAppsList(sessionid, uid, reason, page, pagenumber));
			 }
		 });
	}
	
	public void syncApksStatus(final String sessionid, final String apps,final boolean all, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(SYNC_APKS_STATUS, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                listener.syncApksStatus(twitter.syncApksStatus(sessionid, apps, all));
			 }
		 });	
	}

//    public void getLastSyncedApk(final String sessionid, final boolean history_version, TwitterListener listener) {
//        getThreadPool().dispatch(new AsyncTask(GET_LAST_SYNCED_APK, listener) {
//            public void dispatch(TwitterListener listener) throws TwitterException {
//               listener.syncApksStatus(twitter.getLastSyncedApk(sessionid, history_version));
//            }
//        });    
//    }

	public void getApkIdInServerDB(final String session_id, final String packagename,
			final int versioncode, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(GET_APK_DETAIL_INFORMATION, listener) {
             public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getApkIdInServerDB(twitter.getApkIdInServerDB(session_id, packagename, versioncode));
             }
         });		
	}

//	public void postShare(final String session_id, final String uid, final String message, final int attachment_type,
//			final long attachment_id, final String package_name , final String source, final String extended_info, TwitterListener listener) {
//		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
//			 public void dispatch(TwitterListener listener) throws TwitterException {
//                listener.postShare(twitter.postShare(session_id, uid, message, attachment_type, 
//                		attachment_id, package_name, source, extended_info));
//			 }
//		 });
//	}
	public void postQiupuShare(final String session_id, final String to_id, final String message, final int filter_type,
			final String apk_id, final String package_name , final boolean privacy, final boolean isTop,
			final boolean sendEmail, final boolean sendSms, final String categoryId, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                listener.postQiupuShare(twitter.postQiupuShare(session_id, to_id, message, filter_type, 
                		apk_id, package_name, privacy, isTop, sendEmail, sendSms, categoryId));
			 }
		 });
	}
	
//	public void getPostFriendsTimeLine(final String sessionid, final int limit, final long time, final boolean newpost,
//                                       final String filter_app, final int filter_type, TwitterListener listener) {
//		getThreadPool().dispatch(new AsyncTask(GET_POST_TIMELINE, listener) {
//			 public void dispatch(TwitterListener listener) throws TwitterException {
//				 listener.getPostTimeLine(twitter.getPostFriendsTimeLine(sessionid,limit, time, newpost, filter_app, filter_type));
//			 }
//		 });
//	}
	
//	public void getPostCircleTimeLine(final String sessionid, final int limit, final long time, final boolean newpost,
//			final String filter_app, final int filter_type, final int circleid, TwitterListener listener) {
//		getThreadPool().dispatch(new AsyncTask(GET_POST_TIMELINE, listener) {
//			public void dispatch(TwitterListener listener) throws TwitterException {
//				listener.getPostTimeLine(twitter.getPostCircleTimeLine(sessionid,limit, time, newpost, filter_app, filter_type, circleid));
//			}
//		});
//	}
	
	public void getPostTimeLine(final String sessionid, final long userid, final long circleId, final int limit, final String time, final boolean newpost,
	        final String filter_app, final int filter_type, final long categoryId, final int fromHome, TwitterListener listener) {
	    getThreadPool().dispatch(new AsyncTask(GET_POST_TIMELINE, listener) {
	        public void dispatch(TwitterListener listener) throws TwitterException {
	            listener.getPostTimeLine(twitter.getPostTimeLine(sessionid, userid, circleId, limit, time, newpost, filter_app, filter_type, categoryId, fromHome));
	        }
	    });
	}
	public void getPostTop(final String sessionid, final long id, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(TwitterMethod.GET_POST_TOP, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getPostTop(twitter.getPostTop(sessionid,id));
			 }
		 });
	}
	
//	private void getPublicTimeLine(final String sessionid, final int limit, final long time, final boolean newpost,
//                                  final String filter_app, final int filter_type, TwitterListener listener) {
//		getThreadPool().dispatch(new AsyncTask(GET_POST_TIMELINE, listener) {
//			 public void dispatch(TwitterListener listener) throws TwitterException {
//				 listener.getPostTimeLine(twitter.getPostPublicTimeLine(sessionid, limit, time, newpost, filter_app, filter_type));
//			 }
//		 });
//	}
	
	public void postStreamComment(final String sessionid,final String streamid, final String referredId,
			final String content, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			public void dispatch(TwitterListener listener) throws TwitterException {
				listener.getPostComment(twitter.postStreamComment(sessionid, streamid, referredId, content));
			}
		});
	}
	public void postStreamComment(final String sessionid,final String streamid, final String referredId,
			final String content,final String type, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getPostComment(twitter.postStreamComment(sessionid, streamid, referredId, content,type));
			 }
		 });
	}
	
	public void postApkComment(final String sessionid,final String apkid, final String referredId,
			final String content, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getPostComment(twitter.postApkComment(sessionid, apkid, referredId, content));
			 }
		 });
	}

	public void getCommentsList(final String sessionid, final String obj_type, final String objectid, final int page,
			final int count, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getCommentsList(twitter.getComments(sessionid, obj_type, objectid,page,count));
			 }
		 });
	}
	
	public void deleteComments(final String sessionid, final String obj_type, final long commentId, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.deleteComments(twitter.deleteComments(sessionid, obj_type, commentId));
			 }
		 });
	}
	
	public void deletePost(final String sessionid, final String postId, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.deletePost(twitter.deletePost(sessionid, postId));
			 }
		 });
	}

//    public void postApkUnLike(final String sessionid, final String objectid, final String type, TwitterListener listener) {
//        getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
//            public void dispatch(TwitterListener listener) throws TwitterException {
//                listener.postUnLike(twitter.removeLike(sessionid, objectid, type));
//            }
//        });
//    }

    public void postUnLike(final String sessionid, final String objectid, final String type, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.postUnLike(twitter.removeLike(sessionid, objectid, type));
			 }
		 });
	}
    
    public void getLikeUsers(final String sessionid, final String objectid, final String type,final int page, final int count, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(LIKE_USERS, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
                 listener.getLikeUsers(twitter.getLikeUsers(sessionid, objectid, type, page, count));
			 }
		 });
	}
	
	public void postRetweet(final String sessionid, final String objectid, final String tos, final String addedContent,
                            final boolean canComment, final boolean canLike, final boolean canShare, final boolean privacy,
                            TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.postRetweet(twitter.postRetweet(sessionid, objectid, tos, addedContent, canComment, canLike, canShare, privacy));
			 }
		 });
	}
	
//	public void postApkLike(final String sessionid, final String objectid, final String type, TwitterListener listener) {
//		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
//			 public void dispatch(TwitterListener listener) throws TwitterException {
//			 listener.postLike(twitter.createLike(sessionid, objectid, type));
//			 }
//		 });
//	}

    public void postLike(final String sessionid, final String targetId, final String type, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.postLike(twitter.createLike(sessionid, targetId, type));
            }
        });
    }

    public void deleteApps(final String sessionid, final String appList, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
			 listener.deleteApps(twitter.deleteApps(sessionid, appList));
			 }
		 });
	}

	public void postAddFavorite(final String sessionid, final String objectid, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
			 listener.postAddFavorite(twitter.postAddFavorite(sessionid, objectid));
			 }
		 });
	}
	
	
	public void postRemoveFavorite(final String sessionid, final String objectid, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.postRemoveFavorite(twitter.postRemoveFavorite(sessionid, objectid));
			 }
		 });
	}
	
	public void inviteWithMail(final String sessionid, final String phoneNumbers,final String emails,final String names,
	        final String message, final boolean exchange_vcard, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.inviteWithMail(twitter.inviteWithMail(sessionid, phoneNumbers, emails, names, message, exchange_vcard));
			 }
		 });
	}
	
	public void setCircle(final String sessionid,final long uid,final String circleid, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.setCircle(twitter.setCircle(sessionid, uid, circleid));
			 }
		 });
	}
	
	public void exchangeVcard(final String sessionid,final long uid, final boolean send_request, final String circleid, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.exchangeVcard(twitter.exchangeVcard(sessionid, uid, send_request, circleid));
            }
        });
   }
	
	public void usersSet(final String sessionid,final String uids,final String circleid, final boolean isadd, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.usersSet(twitter.usersSet(sessionid,uids, circleid, isadd));
			 }
		 });
	}
	
	public void sendApproveRequest(final String sessionid,final String uids, final String message, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.sendApproveRequest(twitter.sendApproveRequest(sessionid,uids, message));
			 }
		 });
	}
	
	public void getUserInfo(final long userid,final String ticket,TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
             listener.getUserInfo(twitter.getUserInfo(ticket, userid));
			 }
		 });
	}
	
	public void getUsersList(final String ticket, final String ids, TwitterListener listener) {
		getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			public void dispatch(TwitterListener listener) throws TwitterException {
				listener.getUsersList(twitter.getUsersInfowithIds(ticket, ids));
			}
		});
	}
	
	 public void updateUserInfo(final String ticket,final HashMap<String,String> columnsMap,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
            listener.updateUserInfo(twitter.updateUserInfo(ticket,columnsMap));
			 }
		 });
	}

	 public void getUserCircle(final String sessionid,final long uid, final String circles,final boolean with_users, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getUserCircle(twitter.getUserCircle(sessionid, uid, circles, with_users));
			 }
		 });
	 }

    public void getDirectoryInfo(final String sessionid, final long circleid, final String sort, final int page, final int count, final String searchKey, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getDirectoryInfo(twitter.getDirectoryInfo(sessionid, circleid, sort, page, count, searchKey));
            }
        });
    }


    public void getCompanyCircle(final String sessionid, final String company_id, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getCompanyCircle(twitter.getCompanyCircle(sessionid, company_id));
			 }
		 });
	}
	 
	 public void createCircle(final String sessionid,final String circleName, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.createCircle(twitter.createCircle(sessionid, circleName));
			 }
		 });
	}
	 
	 public void createPublicCircle(final String sessionid,final HashMap<String, String> map, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.createPublicCircle(twitter.createPulbicCircle(sessionid, map));
			 }
		 });
	}
	 
	 public void createEvent(final String sessionid,final HashMap<String, String> map, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.createEvent(twitter.createEvent(sessionid, map));
			 }
		 });
	}
	 
	 public void editPulbicCircle(final String sessionid,final HashMap<String, String> map, final boolean isEvent, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.editPulbicCircle(isEvent ? twitter.editEvent(sessionid, map)
						                           : twitter.editPulbicCircle(sessionid, map));
			 }
		 });
	}
	 
	 public void syncPublicCirclInfo(final String sessionid,final String circleids, final boolean with_members,final boolean isEvent, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncPublicCirclInfo(isEvent ? twitter.syncEventInfo(sessionid, circleids, with_members) : twitter.syncPublicCirclInfo(sessionid, circleids, with_members));
			 }
		 });
	 }
	 
	 public void syncPublicCircles(final String sessionid,final String circleids, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncPublicCircles(twitter.syncPublicCircles(sessionid, circleids));
			 }
		 });
	 }
	 
	 public void syncChildCircles(final String sessionid,final long circleid, final int formal, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncChildCircles(twitter.syncChildCircles(sessionid, circleid, formal));
			 }
		 });
	 }
	 
	 public void syncEventInfo(final String sessionid,final String circleids, final boolean with_members, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncEventInfo(twitter.syncEventListInfo(sessionid, circleids, with_members));
			 }
		 });
	 }
	 
	 public void syncCircleEventInfo(final String sessionid,final long circleid, final boolean with_members, final int page, final int count, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncCircleEventInfo(twitter.syncCircleEventListInfo(sessionid, circleid, with_members, page, count));
			 }
		 });
	 }
	 
	 public void syncPageList(final String sessionid,final String pageids, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.syncPageList(twitter.syncPageList(sessionid, pageids));
			 }
		 });
	 }

    public void getUserPollList(final String ticket, final int type, final int page, final int count, final long user_id, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener)
                    throws TwitterException {
                listener.getUserPollList(twitter.getUserPollList(ticket, type, page, count, user_id, false));
            }
        });
    }

    public void getFriendPollList(final String ticket, final long user_id, final int type, final int page, final int count, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener)
                    throws TwitterException {
                listener.getFriendPollList(twitter.getFriendPollList(ticket, user_id, type, page, count, false));
            }
        });
    }

    public void getPublicPollList(final String ticket, final int page, final int count, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener)
                    throws TwitterException {
                listener.getPublicPollList(twitter.getPublicPollList(ticket, page, count, false));
            }
        });
    }

    public void syncPollInfo(final String sessionid, final String poll_ids, final boolean with_items, TwitterListener listener){
         getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
             public void dispatch(TwitterListener listener) throws TwitterException {
                 listener.getPollList(twitter.getPollListInfo(sessionid, poll_ids, with_items));
             }
         });
     }

    public void addPollItems(final String sessionid, final String poll_id, final String item_ids, final ArrayList<String> msgList, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.addPollItems(twitter.addPollItems(sessionid, poll_id, item_ids, msgList));
            }
        });
    }

    public void createPoll(final String sessionid, final String recipient, final String title, final String description, 
            final long startTime, final long endTime, final ArrayList<String> pollItemList, final int canVoteCount, final int mode,
            final boolean canAddItem, final boolean canSendEmail, final boolean sendSms, final boolean isPrivate, final long parentId, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.createPoll(twitter.createPoll(sessionid, recipient, title, description, startTime,
                        endTime, pollItemList, canVoteCount, mode, canAddItem, canSendEmail, sendSms, isPrivate, parentId));
            }
        });
    }

    public void vote(final String sessionid, final String poll_id, final String item_ids, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_POLL_LIST, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.vote(twitter.vote(sessionid, poll_id, item_ids));
            }
        });
    }

	 public void publicInvitePeople(final String sessionid,final String circleid, final String uids, final String toNames, final String message,
	         final boolean sendEmail, final boolean sendSms, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.publicInvitePeople(QiupuConfig.isEventIds(circleid) ? twitter.inviteEventPeople(sessionid, circleid, uids, toNames, message)
						                                                       : twitter.publicInvitePeople(sessionid, circleid, uids, toNames, message, sendEmail, sendSms));
			 }
		 });
	}
	 
	 public void applyInPublicCircle(final String sessionid,final String circleid, final String message, TwitterListener listener){
         getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
             public void dispatch(TwitterListener listener) throws TwitterException {
                 listener.applyInPublicCircle(QiupuConfig.isEventIds(circleid) ? twitter.applyInEvent(sessionid, circleid, message)
                		                                                       : twitter.applyInPublicCircle(sessionid, circleid, message));
             }
         });
    }
	 
	 public void deleteCircle(final String sessionid,final String circleId, final int type, TwitterListener listener){
	     getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
	         public void dispatch(TwitterListener listener) throws TwitterException {
	             listener.deleteCircle(QiupuConfig.isEventIds(circleId) ? twitter.deleteEvent(sessionid, circleId)
	                     : twitter.deleteCircle(sessionid, circleId, type));
	         }
	     });
	 }
	 public void deletePoll(final String sessionid,final String poll_id, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(TwitterMethod.DELETE_POLL, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.deletePoll(twitter.deletePoll(sessionid, poll_id));
			 }
		 });
	}
	 
	 public void refuseUser(final String sessionid,final long uid, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.refuseUser(twitter.refuseUser(sessionid, uid));
			 }
		 });
	}
	 
	 public void sendChangeRequest(final String ticket,final HashMap<String,String> columnsMap,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
           listener.sendChangeRequest(twitter.sendChangeRequest(ticket,columnsMap));
			 }
		 });
	}
	 
	 public void setNotification(final String ticket,final String key, final boolean value,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
          listener.setNotification(twitter.setNotification(ticket, key, value));
			 }
		 });
	}
	 
	 public void getNotificationValue(final String ticket,final String key,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(GET_NOTIFICATION_VALUE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
         listener.getNotificationValue(twitter.getNotificationValue(ticket, key));
			 }
		 });
	}

    public void getStreamWithComments(final String sessionid, final String objectid, TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(POST_SHARE, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.getStreamWithComments(twitter.getStreamWithComments(sessionid, objectid));
			 }
		 });
	}
    
    public void addFriendsContact(final String sessionid,final String name, final String circleid, final String content, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND_CONTACT, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.addFriendsContact(twitter.addFriendsContact(sessionid, name, circleid, content));
			 }
		 });
	}

    public void remarkSet(final long remarkUserid, final String ticket, final String remark, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(REMARK_SET, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.remarkSet(twitter.remarkSet(ticket, remarkUserid, remark));
            }
        });
    }

    public void muteObject(final String ticket, final String objectId, final int type, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(MUTE_OBJECT, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.muteObject(twitter.muteObject(ticket, objectId, type));
            }
        });
    }

    public void reportAbusedObject(final String ticket, final String objectId, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(REPORT_ABUSE, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.reportAbuse(twitter.reportAbusedObject(ticket, objectId));
    		}
    	});
    }
    public void getAllAlbums(final String ticket,final long user_id,final boolean with_photo_ids,
    		TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(GET_ALL_ALBUM, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.getAllAlbums(twitter.getAllAlbums(ticket, user_id, with_photo_ids));
    		}
    	});	
    }
    
    public void getAlbum(final String ticket,final long album_id,final long user_id,final boolean with_photo_ids,
    		TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(GET_ALBUM, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.getAlbum(twitter.getAlbum(ticket, album_id, user_id, with_photo_ids));
    		}
    	});	
    }
    
    public void getPhotosByAlbumId(final String ticket,final long album_ids,final int page,final int count,
            TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(GET_PHOTOS_ALBUM, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getPhotosByAlbumId(twitter.getPhotosByAlbumId(ticket, album_ids, page, count));
            }
        });	
    }
    public void getPhotoById(final String ticket,final String photo_ids,
			  TwitterListener listener) {
		 getThreadPool().dispatch(new AsyncTask(GET_PHOTO_BYID, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
           listener.getPhotoById(twitter.getPhotoById(ticket, photo_ids));
			 }
		 });	
	}

    public void deletePhoto(final String ticket, final String photo_ids,final boolean deleteAll, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(PHOTOS_DEL, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.deletePhoto(twitter.deletePhoto(ticket, photo_ids,deleteAll));
    		}
    	});
    }
    
    public void updateStreamSetting(final String ticket, final String postId, final boolean canComment, final boolean canLike,
            final boolean canReshare, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(POST_UPDATEACTION_VALUE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.postUpdateSetting(twitter.postUpdateSetting(ticket, postId, canComment, canLike, canReshare));
            }
        });
    }

    public void fileShare(final String ticket, final String friendIds, final String content, final String appData, 
            final boolean issecretly, final boolean canComment, final boolean canLike, final boolean canShare,
            final String summary, final String description, final File file, final String file_name, final File screen_shot_file, final String content_type, 
            final boolean isTop, final boolean sendEmail, final boolean sendSms, final String categoryId, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(POST_OTHER_FILE_VALUE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.fileShare(twitter.fileShare(ticket, friendIds, content, appData, BpcApiUtils.TEXT_POST, 
                        issecretly, canComment, canLike, canShare, summary, description, file, file_name, screen_shot_file,
                        content_type, isTop, sendEmail, sendSms, categoryId));
            }
        });
    }

    public void getLBSUsersInfo(final long userid,final String ticket,TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
            listener.getLBSUsersInfo(twitter.getLBSUsersInfo(ticket, userid));
            }
        });
   }

    public void getRequestSummary(final String ticket, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
            listener.getRequestSummary(twitter.getRequestSummary(ticket));
            }
        });
    }

    public void getNearByPeopleListPage(final String sessionid, final int page,final int count, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_NEAR_BY_PEOPLE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.getNearByPeopleList(twitter.getNearByPeopleListPage(sessionid, page, count));
            }
        });
   }

    public void setTopList(final String sessionid, final String group_id, final String stream_ids, final boolean setTop, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(GET_NEAR_BY_PEOPLE, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.setTopList(twitter.setTopList(sessionid, group_id, stream_ids, setTop));
            }
        });
    }

    public void syncThemes(final String sessionid, final int page,final int count, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(SYNC_THEME, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.syncEventThemes(twitter.syncEventThemes(sessionid, page, count));
            }
        });
   }
    
    public void createPage(final String sessionid, final HashMap<String,String> columnsMap, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(SYNC_THEME, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.createPage(twitter.createPage(sessionid, columnsMap));
            }
        });
   }
    
    public void syncPageInfo(final String sessionid, final long pageid, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(SYNC_THEME, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.syncPageInfo(twitter.syncPageInfo(sessionid, pageid));
            }
        });
   }
    public void editPage(final String sessionid,final HashMap<String, String> map, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.editPage(twitter.editPage(sessionid, map));
			 }
		 });
	}
    
    public void editPageCover(final String sessionid,final long pageid, final File file,TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
           listener.editPageCover(twitter.editPageCover(sessionid, pageid, file));
            }
        });    
   }
    
    public void editPageLogo(final String sessionid,final long pageid, final File file, TwitterListener listener) {
        getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
           listener.editPageLogo(twitter.editPageLogo(sessionid, pageid, file));
            }
        });    
   }
    public void deletePage(final String sessionid,final long pageId, TwitterListener listener){
	     getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
	         public void dispatch(TwitterListener listener) throws TwitterException {
	             listener.deletePage(twitter.deletePage(sessionid, pageId));
	         }
	     });
	 }
    
    public void serachPage(final String sessionid,final String searchKey, final int page, final int count, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.searchPage(twitter.searchPage(sessionid, searchKey, page, count));
    		}
    	});	
    }   
    
    public void followPage(final String sessionid,final long pageid, final boolean isfollow, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.followPage(twitter.followPage(sessionid, pageid, isfollow));
    		}
    	});	
    } 
    
    public void circleAsPage(final String sessionid,final long id, final HashMap<String, String> infoMap, TwitterListener listener) {
    	getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.circleAsPage(twitter.circleAsPage(sessionid, id, infoMap));
    		}
    	});	
    }
    
    public void searchStream(final String sessionid,final String searchKey,
    		final HashMap<String, String> searchMap, final int searchPage, final int searchCount, TwitterListener listener) {
    	
    	getThreadPool().dispatch(new AsyncTask(GET_USERLIST_BY_SEARCHNAME, listener) {
    		public void dispatch(TwitterListener listener) throws TwitterException {
    			listener.SearchStream(twitter.SearchStream(sessionid, searchKey, searchMap, searchPage, searchCount));
    		}
    	});	
    }
    
    public void addCategory(final String sessionid,final long scopeid, final String categoryName, TwitterListener listener){
		 getThreadPool().dispatch(new AsyncTask(ADD_FRIEND, listener) {
			 public void dispatch(TwitterListener listener) throws TwitterException {
				 listener.addCategory(twitter.addCategory(sessionid, scopeid, categoryName));
			 }
		 });
	 }
    
    public void syncTopCircle(final String sessionid, TwitterListener listener){
        getThreadPool().dispatch(new AsyncTask(GET_REQUESTS, listener) {
            public void dispatch(TwitterListener listener) throws TwitterException {
                listener.syncTopCircle(twitter.syncTopCircle(sessionid));
            }
        });
   }
}
