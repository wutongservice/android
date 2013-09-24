package twitter4j;

import android.util.Log;
import com.borqs.qiupu.AccountListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.Authorization;
import twitter4j.http.RequestToken;
import twitter4j.internal.async.Dispatcher;
import twitter4j.internal.async.DispatcherFactory;
import twitter4j.internal.org.json.JSONException;

import java.io.File;

import static twitter4j.TwitterMethod.*;

public class AsyncBorqsAccount extends TwitterOAuthSupportBase{
	private static final long serialVersionUID = 5385459489508468900L;
	final static String TAG = "AsyncQiupu";
	private Twitter twitter;
	private boolean shutdown = false;
	private static transient Dispatcher dispatcher;
	
    public AsyncBorqsAccount(Configuration conf, Authorization auth, TwitterListener listener) {
        super(conf, auth);
        
        Log.d(TAG, TAG);
        twitter = new TwitterFactory(conf).getInstance(auth);
       
    }
    
    public void shutdown(){
    	Log.d(TAG, "shutdown:"+shutdown);
        synchronized (AsyncBorqsAccount.class) {
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

    public void registerAccount(final String username, final String pwd, final String nickname,
			final String phonenumber, final String urlname, TwitterListener listener) {
		getDispatcher().invokeLater(new AsyncTask(REGISTER_ACCOUNT, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.registerAccount(twitter.registerAccount(username,pwd,nickname,urlname, phonenumber));
            }  
        });
	}
    
    public void registerBorqsAccount(final String username, final String pwd, final String nickname,final boolean gender,
    		final String phonenumber, final String urlname, final String imei, final String imsi, TwitterListener listener) {
    	getDispatcher().invokeLater(new AsyncTask(REGISTER_ACCOUNT, listener) {
    		public void invoke(TwitterListener listener) throws TwitterException {
    			listener.registerBorqsAccount(twitter.registerBorqsAccount(username,pwd,nickname,gender,urlname, phonenumber, imei, imsi));
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
    
    public void loginBorqsAccount(final String username, final String pwd,
			TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(LOGIN_BORQS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.loginBorqsAccount(twitter.loginBorqsAccount(username,pwd));
            }
        });		
	}
    
    public void getUserPasswrod(final String username,TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(LOGIN_BORQS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.getUserpassword(twitter.getUserPassword(username));
            }
        });		
	}
    
    public void getBorqsUserPassword(final String username,TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(LOGIN_BORQS, listener) {
           public void invoke(TwitterListener listener) throws TwitterException {
               listener.getBorqsUserPassword(twitter.getBorqsUserPassword(username));
           }
       });		
	}
    
    public void setGlobalApksPermission(final String sessionid,final int visibility, TwitterListener listener) {
    	getDispatcher().invokeLater(new AsyncTask(LOGIN_BORQS, listener) {
    		public void invoke(TwitterListener listener) throws TwitterException {
    			listener.setApkPermission(twitter.setGlobalApksPermission(sessionid, visibility));
    		}
    	});		
    }
    
    public void logoutAccount(final String ticket, TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(LOGOUT_BORQS, listener) {
            public void invoke(TwitterListener listener) throws TwitterException {
                listener.logoutAccount(twitter.logoutAccount(ticket));
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

	public void getApkIdInServerDB(final String session_id, final String packagename,
			final int versioncode, TwitterListener listener) {
		 getDispatcher().invokeLater(new AsyncTask(GET_APK_DETAIL_INFORMATION, listener) {
             public void invoke(TwitterListener listener) throws TwitterException {
                listener.getApkIdInServerDB(twitter.getApkIdInServerDB(session_id, packagename, versioncode));
             }
         });		
	}


	public void getUserFromContact(final String sessionid,final String contactJason, TwitterListener listener){
		 getDispatcher().invokeLater(new AsyncTask(ADD_FRIEND, listener) {
			 public void invoke(TwitterListener listener) throws TwitterException, JSONException {
				 listener.syncUserFromContact(twitter.getUserFromContact(sessionid, contactJason));
			 }
		 });
	}
}
