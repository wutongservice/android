package com.borqs.qiupu.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import twitter4j.AsyncBorqsAccount;
import twitter4j.AsyncQiupu;
import twitter4j.Employee;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.PeopleLookupHelper;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.ContactUtils;

public class FriendsManager {
    private static final String TAG = "Qiupu.Alarm.FriendsManager";

    public final static int STATUS_DEFAULT = 0; //
    public final static int STATUS_DOING = STATUS_DEFAULT + 1;
    public final static int STATUS_ITERATING = STATUS_DOING + 1;
    public final static int STATUS_DO_OK = STATUS_ITERATING + 1;
    public final static int STATUS_DO_FAIL = STATUS_DO_OK + 1;
    
    public final static int SYNC_TYPE_EVENTS = 1; 
    public final static int SYNC_TYPE_FRIENDS = 2;
    public final static int SYNC_TYPE_DIRECTORY = 3;
    public final static String SYNC_TYPE = "SYNC_TYPE";

    private BorqsAccount mAccount;
    private QiupuORM orm;
    private QiupuService mService;
    private AsyncQiupu asyncTwitter;
    public static int mSyncStatus = STATUS_DEFAULT;

    private static final HashMap<String, FriendsServiceListener> listeners = new HashMap<String, FriendsServiceListener>();

    private static final int limit = 200;
    private int page = 0;

    private Handler handler;

    private int nErrorCount;

    public FriendsManager(QiupuService service, QiupuORM qiupuORM) {
        mService = service;
        orm = qiupuORM;

        mAccount = service.getBorqsAccount();
        handler = new FriendsHandler();
        asyncTwitter = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
    }

    public void start(BorqsAccount account) {
        if (QiupuConfig.LOGD) Log.d(TAG, "start=" + account);
        mAccount = account;

        if (mAccount != null) {
            nErrorCount = 0;
            if(orm.getUserInfoLastSyncTime() <= 0) {
            	rescheduleFriends(true);	
            }else {
            	rescheduleFriends(false);
            }
            if (orm.querySimpleUserInfo(mAccount.uid) == null) {
                syncMySelfInfo();
            }
            if(orm.getEventsLastSyncTime() <= 0) {
            	handler.obtainMessage(SYNC_EVENT).sendToTarget();
            }
            long lastsyncCircletime = orm.getCircleLastSyncTime(); 
            if(lastsyncCircletime <= 0 || (System.currentTimeMillis() - lastsyncCircletime - 2 * QiupuConfig.A_DAY) > 0) {
            	handler.obtainMessage(GET_CIRCLE).sendToTarget();
            }
        }
    }

    public void onCancelLogin() {
        if (mAccount != null) {
            mAccount = null;
        }
    }

    public void onLogin(BorqsAccount account) {
        if (QiupuConfig.LOGD) Log.d(TAG, "onLogin=" + account);
        start(account);
    }

    public void rescheduleFriends(boolean force) {
        AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
        long nexttime;

        long current_time = System.currentTimeMillis();
        long last_update_time = orm.getUserInfoLastSyncTime();
        long donespan = (current_time - last_update_time);
        long left_time = orm.getFriendsInterval() * (24 * 60 * 60 * 1000) - donespan;
        if (donespan < 0 || left_time <= 0) {
            long waittime = 1;
            for (int i = 0; i < nErrorCount && i < 10; i++) {
                waittime = waittime * 2;
            }
            nexttime = System.currentTimeMillis() + 20 * 1000 * waittime;
        } else {
            nexttime = System.currentTimeMillis() + left_time;
        }

        if (force == true) {
            nexttime = System.currentTimeMillis() + 20 * 1000;
        }

        if (QiupuService.TEST_LOOP) {
            nexttime = System.currentTimeMillis() + 2 * 60 * 1000;
        }


        Intent i = new Intent(mService, mService.getClass());
        i.setAction(QiupuService.INTENT_QP_SYNC_FRIENDS_INFORMATION);
        PendingIntent phonebookpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.RTC, nexttime, phonebookpi);
    }

    public void alarmFriendsComing(boolean immediately, boolean includingCircles, boolean isOnlySyncCircle) {
        Log.d(TAG, "alarmFriendsComing immediately = " + immediately + ", includingCircles = " + includingCircles);

        if (immediately) {
            shootCircleFriendSync(0, includingCircles, isOnlySyncCircle);
        } else {
            alarmFriendsComing(includingCircles);
        }
    }

    private void shootCircleFriendSync(long delay, boolean includingCircles, boolean isOnlySyncCircle) {
        if(isOnlySyncCircle) {
            handler.sendMessageDelayed(handler.obtainMessage(GET_CIRCLE), delay);
        }else {
            if(includingCircles) {
                handler.sendMessageDelayed(handler.obtainMessage(GET_CIRCLE), delay);
            }
            handler.sendMessageDelayed(handler.obtainMessage(FRIENDS_GET), delay);
        }
    }

    private void alarmFriendsComing(boolean includingCircles) {
        Log.d(TAG, "alarmFriendsComing");
        shootCircleFriendSync(5 * QiupuConfig.A_SECOND, includingCircles, false);

        long nexttime = System.currentTimeMillis() + orm.getFriendsInterval() * QiupuConfig.A_DAY;

        if (QiupuService.TEST_LOOP) {
            nexttime = System.currentTimeMillis() + 90 * 1000;
        }

        final long finaltime = nexttime;
        handler.post(new Runnable() {
            public void run() {
                AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);

                Intent i = new Intent(mService, mService.getClass());
                i.setAction(QiupuService.INTENT_QP_SYNC_FRIENDS_INFORMATION);
                PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmMgr.set(AlarmManager.RTC, finaltime, userpi);
            }
        });
    }

    public void onLogout() {
        Log.d(TAG, "logout");
        handler.post(new Runnable() {
            public void run() {
                Intent i = new Intent(mService, mService.getClass());
                i.setAction(QiupuService.INTENT_QP_SYNC_FRIENDS_INFORMATION);
                PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

                AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
                alarmMgr.cancel(userpi);
            }
        });
    }

    public void destroy() {
        onLogout();
    }

    private final static int FRIENDS_GET = 1;
    private final static int FRIENDS_GET_END = 2;
    private final static int GET_CURRENT_ACCOUNT_END = 3;
    private final static int GET_CIRCLE = 101;
    private final static int GET_CIRCLE_END = 102;

    private final static int GET_DIRECTORY = 103;
    private final static int GET_DIRECTORY_END = 104;
    
    private final static int SYNC_EVENT = 105;
    private final static int SYNC_EVENT_END = 106;

    private class FriendsHandler extends Handler {
        public FriendsHandler() {
            super();
            Log.d(TAG, "new FriendsHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FRIENDS_GET: {
                    syncfriendsInfo(page, limit);
                    break;
                }

                case FRIENDS_GET_END: {
                    boolean suc = msg.getData().getBoolean("RESULT");
                    Message backmsg = getBackMessage(SYNC_TYPE_FRIENDS);
                    if (suc) {
                        nErrorCount = 0;
                        int size = msg.getData().getInt("size");
                        
                        if (size == 0)//finish get the data
                        {
                            //set record time
                            page = 0;
                            orm.setUserInfoLastSyncTime();
                            orm.removeInvalidUser();

                            updateActivityUI(STATUS_DO_OK, backmsg);

                            gotoVerifyLookup();
                        } else {
                        	
                            updateActivityUI(STATUS_ITERATING, backmsg);
                            //re-get the left data
                            page += 1;
                            Log.d(TAG, "I still have left data, continue to get the data current page=" + page);
                            Message msd = handler.obtainMessage(FRIENDS_GET);
                            handler.sendMessageDelayed(msd, 3 * 1000);
                        }

                    } else {
                        updateActivityUI(STATUS_DO_FAIL, backmsg);

                        nErrorCount++;
                        Log.d(TAG, "Fail to get friend reschedule, current page=" + page);
                        rescheduleFriends(false);
                    }
                }
                case GET_CURRENT_ACCOUNT_END: {
                    boolean suc = msg.getData().getBoolean("RESULT", false);
                    if (suc == false) {
                        Log.d(TAG, "get myself info error");
                    }
                }
                break;
                case GET_CIRCLE: {
                    syncCircleInfo();
                    break;
                }
                case GET_CIRCLE_END: {
                    if (msg.getData().getBoolean("RESULT")) {
                    	orm.setCircleLastSyncTime();
                        updateActivityUI(STATUS_DO_OK, new Message());
                    } else {
                        updateActivityUI(STATUS_DO_FAIL, null);
                        Log.d(TAG, "sync circle info error");
                    }
                    break;
                }
                case GET_DIRECTORY:
                {
                    long circleId = msg.getData().getLong(QiupuService.INTENT_KEY_CIRCLE_ID);
                    syncDirectoryInfo(circleId, mDirectoryPage, DIRECTORY_PAGE_SIZE);
                    break;
                }
                case GET_DIRECTORY_END:
                {
                	Message backmsg = getBackMessage(SYNC_TYPE_DIRECTORY);
                    if (msg.getData().getBoolean("RESULT")) {
                        int size = msg.getData().getInt("size");
                        if (size == 0)//finish get the data
                        {
                            Log.d(TAG, "sync last directory info page = " + mDirectoryPage);

                            //set record time
                            mDirectoryPage = 0;

                            updateActivityUI(STATUS_DO_OK, backmsg);
                        } else {
                            updateActivityUI(STATUS_ITERATING, backmsg);
                            final long circleId = msg.getData().getLong(QiupuService.INTENT_KEY_CIRCLE_ID);
                            //re-get the left data
                            mDirectoryPage++;
                            Log.d(TAG, "I still have left data, continue to get the data current page=" + mDirectoryPage);
                            Message msd = handler.obtainMessage(GET_DIRECTORY);
                            msd.getData().putLong(QiupuService.INTENT_KEY_CIRCLE_ID, circleId);
                            handler.sendMessageDelayed(msd, 3 * 1000);
                        }
                    } else {
                        updateActivityUI(STATUS_DO_FAIL, backmsg);
                        Log.d(TAG, "sync directory info error");
                    }
                    break;
                }
                case SYNC_EVENT: {
                	syncEventInfo("", false);
                	break;
                }
                case SYNC_EVENT_END: {
                	boolean suc = msg.getData().getBoolean("RESULT");
                    Message backmsg = getBackMessage(SYNC_TYPE_EVENTS);
                    if (suc) {
                    	orm.setEventsLastSyncTime();
                    	updateActivityUI(STATUS_DO_OK, backmsg);
                    } else {
                        updateActivityUI(STATUS_DO_FAIL, backmsg);
                    }
                	break;
                }
            }
        }
    }


    private boolean inloadingCircle = false;
    private Object circleLock = new Object();

    private void syncCircleInfo() {
        synchronized (circleLock) {
            if (inloadingCircle == true) {
                Log.d(TAG, "in doing syncCircleInfo data");
                return;
            }
        }

        synchronized (circleLock) {
            inloadingCircle = true;
        }

        //todo
//        mtitlelistener.loadbegin();
        asyncTwitter.getUserCircle(AccountServiceUtils.getSessionID(),
                AccountServiceUtils.getBorqsAccountID(), "", false,
                new TwitterAdapter() {
                    public void getUserCircle(ArrayList<UserCircle> userCircles) {
                        Log.d(TAG, "finish getUserCircle= " + userCircles.size());

                        dogetUserCircleCallBack(true, userCircles);
                        synchronized (circleLock) {
                            inloadingCircle = false;
                        }
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG, "getUserCircle, server exception:", ex, method);
                        synchronized (circleLock) {
                            inloadingCircle = false;
                        }
                        dogetUserCircleCallBack(false, null);
                    }
                });
    }

    private void dogetUserCircleCallBack(boolean suc, ArrayList<UserCircle> userCircles) {
        if (suc) {
            updateActivityUI(STATUS_DO_OK, new Message());
//            setCircleData(userCircles);
            orm.removeAllCirclesWithOutNativeCircles();
            orm.insertCircleList(userCircles, AccountServiceUtils.getBorqsAccountID());
        } else {
            updateActivityUI(STATUS_DO_FAIL, null);
        }
        Message msg = handler.obtainMessage(GET_CIRCLE_END);
        msg.getData().putBoolean("RESULT", suc);
        msg.sendToTarget();
    }

//    private void setCircleData(ArrayList<UserCircle> userCircles) {
//        ArrayList<UserCircle> tmpCircleList = new ArrayList<UserCircle>();
//        for (int i = 0; i < userCircles.size(); i++) {
//            final UserCircle tmpcircle = userCircles.get(i);
//            if (orm.updateCircleInfo(tmpcircle) <= 0) {
//                orm.insertCircleInfo(tmpcircle);
//            }
//
//            if (!QiupuHelper.inFilterCircle(String.valueOf(tmpcircle.circleid))) {
//                tmpCircleList.add(tmpcircle);
//            }

//            if(tmpcircle.circleid == QiupuConfig.ADDRESS_BOOK_CIRCLE)
//            {
//                handler.post(new Runnable()
//                {
//                    public void run()
//                    {
//                        if (Privacy_circle_text == null) {
//                            return;
//                        }
//                        if (getActivity() != null) {
//                            // TODO: work-round for fix crash.
//                            final String txt = getString(R.string.address_book_circle) + " (" + tmpcircle.memberCount + ")";
//                            Privacy_circle_text.setText(txt);
//                        }
//
//                    }
//                });
//            }
//        }

//        if(mCircles != null)
//        {
//            mCircles.clear();
//            mCircles.addAll(tmpCircleList);
//            Collections.sort(mCircles);
//        }
//    }

    private boolean isLoadingFriends;
    private final Object mLockLoadFriends = new Object();

    private void syncfriendsInfo(final int page, final int count) {
        if (mAccount == null) {
            Log.d(TAG, "syncFansInfo, mAccount is null exit");
            return;
        }
        if (isLoadingFriends) {
            Log.d(TAG, "is Loading friends ");
            return;
        }

        synchronized (mLockLoadFriends) {
            isLoadingFriends = true;
        }

        updateActivityUI(STATUS_DOING, getBackMessage(SYNC_TYPE_FRIENDS));

        asyncTwitter.getFriendsListPage(AccountServiceUtils.getSessionID(), mAccount.uid, "", page, count, true, new TwitterAdapter() {
            public void getFriendsList(List<QiupuUser> users) {
                Log.d(TAG, "finish getFriendsList=" + users.size());

                if (page == 0) {
                	orm.revertUserStatus();
//                    orm.removeAllUsers();
                }
                orm.insertFriendsList(users);
                if (users != null && users.size() > 0) {
//                    orm.bullInsertFriendsList(users);
                }

                synchronized (mLockLoadFriends) {
                    isLoadingFriends = false;
                }

                Message mds = handler.obtainMessage(FRIENDS_GET_END);
                mds.getData().putBoolean("RESULT", true);
                mds.getData().putInt("size", users.size());
                handler.sendMessage(mds);

                users.clear();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getFriendsListPage, server exception:", ex, method);

                synchronized (mLockLoadFriends) {
                    isLoadingFriends = false;
                }

                Message mds = handler.obtainMessage(FRIENDS_GET_END);
                mds.getData().putBoolean("RESULT", false);
                handler.sendMessage(mds);
            }
        });
    }

    public interface FriendsServiceListener {
        public void updateUI(int msgcode, Message msg);
    }

    public static void registerFriendsServiceListener(String key, FriendsServiceListener listener) {
        if (listeners.get(key) == null) {
            synchronized (listeners) {
                listeners.put(key, listener);
            }
        }
    }

    public static void unregisterFriendsServiceListener(String key) {
        synchronized (listeners) {
            listeners.remove(key);
        }
    }

    private void updateActivityUI(int msgcode, Message msg) {
        if (QiupuConfig.LOGD) Log.d(TAG, "updateActivityUI msgcode:" + msgcode + " listener count:" + listeners.size());
        synchronized (listeners) {
        	mSyncStatus = msgcode;
            Set<String> set = listeners.keySet();
            for (String key : set) {
                FriendsServiceListener listener = listeners.get(key);
                if (listener != null) {
                    listener.updateUI(msgcode, msg);
                }
            }
        }
    }

    public void filterInvalidException(TwitterException ne) {
    }

    private void gotoVerifyLookup() {
        BorqsAccountORM accountOrm = BorqsAccountORM.getInstance(mService);
        AsyncBorqsAccount mAsyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(), null, null);
        PeopleLookupHelper mBookSync = PeopleLookupHelper.getInstance(mService, accountOrm, mAsyncBorqsAccount);
//		orm.clearLookupTable();
        mBookSync.verifyContactsTypeWithSync4(ContactUtils.getContacts(mService), false);
    }

    public void syncMySelfInfo() {
        asyncTwitter.getUserInfo(mAccount.uid, AccountServiceUtils.getSessionID(), new TwitterAdapter() {
            public void getUserInfo(QiupuUser user) {
                Log.d(TAG, "finish getUserDetail");
                orm.insertUserinfo(user);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getUserDetail, server exception:", ex, method);
                Message msg = handler.obtainMessage(GET_CURRENT_ACCOUNT_END);
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        });
    }

    public void alarmCircleDirectoryComing(final long circleId, boolean force) {
        final Long currentTime = System.currentTimeMillis();
        final long lastTime = orm.getDirectoryInfoLastSyncTime(circleId);
//        if (!force && currentTime < lastTime) {
//            Log.i(TAG, "ignore syncing while the data is not expired for circle id: " + circleId);
//            return;
//        }
        
        if(force|| lastTime <=0  
        		|| (currentTime - lastTime - orm.getFriendsInterval() * QiupuConfig.A_DAY) >= 0) {
        	Message msg = handler.obtainMessage(GET_DIRECTORY);
        	msg.getData().putLong(QiupuService.INTENT_KEY_CIRCLE_ID, circleId);
        	handler.sendMessage(msg);
        	
        	long nexttime = currentTime + orm.getFriendsInterval() * QiupuConfig.A_DAY;
        	final long finaltime = nexttime;
        	handler.post(new Runnable() {
        		public void run() {
        			AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
        			Intent i = new Intent(mService, mService.getClass());
        			i.putExtra(QiupuService.INTENT_KEY_CIRCLE_ID, circleId);
        			i.putExtra(QiupuService.INTENT_KEY_SYNC_FLAGS, true);
        			i.setAction(QiupuService.INTENT_QP_SYNC_DIRECTORY_INFORMATION);
        			PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        			alarmMgr.set(AlarmManager.RTC, finaltime, userpi);
        		}
        	});
        }

    }

    public void alarmSyncEventComing(boolean force) {
//        final Long currentTime = System.currentTimeMillis();
//        final long lastTime = orm.getDirectoryInfoLastSyncTime(circleId);
//        if (!force && currentTime < lastTime) {
//            Log.i(TAG, "ignore syncing while the data is not expired for circle id: " + circleId);
//            return;
//        }

        Message msg = handler.obtainMessage(SYNC_EVENT);
        handler.sendMessage(msg);

//        long nexttime = currentTime + orm.getFriendsInterval() * QiupuConfig.A_DAY;
//        final long finaltime = nexttime;
//        handler.post(new Runnable() {
//            public void run() {
//                AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
//                Intent i = new Intent(mService, mService.getClass());
//                i.putExtra(QiupuService.INTENT_KEY_CIRCLE_ID, circleId);
//                i.putExtra(QiupuService.INTENT_KEY_SYNC_FLAGS, true);
//                i.setAction(QiupuService.INTENT_QP_SYNC_DIRECTORY_INFORMATION);
//                PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//                alarmMgr.set(AlarmManager.RTC, finaltime, userpi);
//            }
//        });
    }

    // ugly code patch for ugly api, temp use for adapting BORQS formal circle to
    // original borqs company, should be removed when the directory has been ready
    // in formal circles.
    private static HashMap<Long, Long> circleToCompany;
    static {
        circleToCompany = new HashMap<Long, Long>();
        circleToCompany.put(new Long(10000000072L), new Long(15000000016L));
    }
    private static String circleToCompany(long circleId) {
        Set<Long> keySet = circleToCompany.keySet();
        if (keySet.contains(circleId)) {
            return String.valueOf(circleToCompany.get(new Long(circleId)));
        }
        return String.valueOf(circleId);
    }
    // end of ugly patch.

    private boolean inloadingDirectory = false;
    private Object directoryLock = new Object();
    private int mDirectoryPage = 0;
//    private static final int DIRECTORY_PAGE_SIZE = 512;
    private static final int DIRECTORY_PAGE_SIZE = 256;

    private void syncDirectoryInfo(final long circleId, final int page, final int count) {
        synchronized (directoryLock) {
            if (inloadingDirectory == true) {
                Log.d(TAG, "in doing syncCircleInfo data");
                return;
            }
        }

        synchronized (directoryLock) {
            inloadingDirectory = true;
        }

        updateActivityUI(STATUS_DOING, getBackMessage(SYNC_TYPE_DIRECTORY));
//        final String directoryOwner = circleToCompany(circleId);
        asyncTwitter.getDirectoryInfo(AccountServiceUtils.getSessionID(),
        		circleId, "", page, count, "",
                new TwitterAdapter() {
                    public void getDirectoryInfo(ArrayList<Employee> members) {
                        Log.d(TAG, "finish syncDirectoryInfo= " + members.size());
                        synchronized (directoryLock) {
                            inloadingDirectory = false;
                        }

                        onDirectorySynchronized(true, circleId, members);
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG, "syncDirectoryInfo, server exception:", ex, method);
                        synchronized (directoryLock) {
                            inloadingDirectory = false;
                        }
                        onDirectorySynchronized(false, circleId, null);
                    }
                });
    }

    private void onDirectorySynchronized(boolean suc, final long circleId, ArrayList<Employee> members) {
        Message msg = handler.obtainMessage(GET_DIRECTORY_END);
        msg.getData().putBoolean("RESULT", suc);

        if (suc) {
            final int count = members.size();
            msg.getData().putInt("size", count);
            msg.getData().putLong(QiupuService.INTENT_KEY_CIRCLE_ID, circleId);

            if (mDirectoryPage == 0) {
                orm.revertDirectoryMemberList(circleId);
            }

            if (count == 0) {
                orm.setDirectoryInfoLastSyncTime(circleId);
                orm.removeInvalidDirectory(circleId);
                orm.updateLocalDirectory();
            }

            orm.insertDirectoryMemberListNoDelete(circleId, members);
        }

        msg.sendToTarget();
    }
    
    boolean inLoadingEvent;
    Object mLockSyncEventInfo = new Object();
    public void syncEventInfo(final String circleId, final boolean with_member) {
//        if (!ToastUtil.testValidConnectivity(this)) {
//            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
//            return;
//        }
        
    	if (inLoadingEvent == true) {
    		return;
    	}
    	
    	synchronized (mLockSyncEventInfo) {
    		inLoadingEvent = true;
    	}
//    	begin();
    	updateActivityUI(STATUS_DOING, getBackMessage(SYNC_TYPE_EVENTS));
    	
    	asyncTwitter.syncEventInfo(AccountServiceUtils.getSessionID(), circleId, with_member, new TwitterAdapter() {
    		public void syncEventInfo(ArrayList<UserCircle> circles) {
    			Log.d(TAG, "finish syncEventInfo=" + circles.size());
    			
    			if (circles.size() > 0) {
//    				orm.insertEventList(circles);
    				orm.insertEventsList(mService, circles);
                }
    			
    			Message msg = handler.obtainMessage(SYNC_EVENT_END);
    			msg.getData().putBoolean("RESULT", true);
    			msg.sendToTarget();
    			synchronized (mLockSyncEventInfo) {
    				inLoadingEvent = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockSyncEventInfo) {
    		    	inLoadingEvent = false;
    			}
    			Message msg = handler.obtainMessage(SYNC_EVENT_END);
    			msg.getData().putBoolean("RESULT", false);
    			msg.sendToTarget();
    		}
    	});
    }
    
    public boolean getLoadStatus(int type) {
    	if(type == SYNC_TYPE_EVENTS) {
    		return inLoadingEvent;
    	} else if(type == SYNC_TYPE_FRIENDS) {
    		return isLoadingFriends;
    	} else if(type == SYNC_TYPE_DIRECTORY) {
    		return inloadingDirectory;
    	} else {
    		return false;
    	}
    }
    
    private Message getBackMessage(int type) {
    	Message msg = new Message();
    	msg.getData().putInt(SYNC_TYPE, type);
    	return msg;
    }
}
