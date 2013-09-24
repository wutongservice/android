package com.borqs.notification;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.DelayInformation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


class LinxClient {
    static String TAG = "LinxClient";
    //public static String DEFAULT_SERVER = "push.borqs.com";
    public static int DEFAULT_PORT = 5222;

    private Account mAccount;
    private FileTransfers mFileTranfers;

    //private boolean mConnected = false;
    
    //XMPP objects
    XMPPConnection mConnection;
    ConnectionListener mConnListener;
    MyPacketListener mPacketListener;
    MyPacketFilter mPacketFilter;
    ConnectionConfiguration mConnConfig;
    Timer mTimer;
    ContentResolver mContentResolver;

    public LinxClient(Account account) {
        mAccount = account;
        //mServiceContext = AccountService.getInstance();
        mContentResolver = NotificationService.getInstance().getContentResolver();
        init();
    }

    /*package*/boolean isConnected() {
	if(mConnection == null){
            return false;
        }

        if (Constants.LOGV_ENABLED) {
            Log.i(TAG, "mConnection is: " + mConnection + " : " + mConnection.isConnected());
        }
        return mConnection != null && mConnection.isConnected();
    }

    /*package*/boolean isLogin() {
        if (Constants.LOGV_ENABLED) {
            if(mConnection == null) {
                Log.d(TAG, "isLogin: Connection is null.");
            }
        }
        return mConnection != null && mConnection.isAuthenticated();
    }

    public boolean login() {
        if (Constants.LOGV_ENABLED) {
            Log.i("queryUser","start to login");
        }
        connectIfNeeded();
        if(!isConnected()) {
            //mTimer = new Timer();
            //mTimer.schedule(new RetryTask(), 0,5*1000);
            if(!isConnected()){
                if (Constants.LOGV_ENABLED) {
                    Log.d(TAG, "Failed to connect to server.");
                }
                return false;
            }
        }
        if(null == mAccount) {
            return false;
        }

        boolean ret = false;
        try {
            // Why we need to check isAuthenticated() ?
            // 1. if it never connected to server, isAuthenticated returns false
            // 2. if it is reconnecting to server, isAuthenticated may return true.
Log.e("000000000000000", "login: " + mAccount.getUserName()+"/"+
                        mAccount.getServerHost());
            if (!mConnection.isAuthenticated()) {
                //SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                mConnection.login(mAccount.getUserName(), 
                        mAccount.getPassword(), 
                        getResource());
            }
            //service.notifyUIConnected();
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "login OK.");
            }
            ret = true;
            if(ret) {
                //TODO: execute this in a separate thread.
                onLoggedIn();
            }
        } catch (XMPPException e) {
            Log.d(TAG, "XMPPException. -- login failed: ", e);
            ret = false;
        } catch (IllegalStateException e1) {
            Log.d(TAG, "IllegalStateException. login failed: ", e1);
            ret = false;
        }/* catch (Exception e) {
            Log.d(TAG, "Exception. login failed: " + e.getMessage());
            ret = false;
        }*/
        
        // reset connection in case of failure.
        if(!ret && mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }

        return ret;
    }

    /*
     * Do some stuffs here after logged in, if needed.
     */
    private void onLoggedIn() {
        //1. Set my presence. Do we have to do this?
        Presence myPresence = new Presence(Presence.Type.available);   
        myPresence.setMode(Presence.Mode.available); 
        myPresence.setStatus("Gone fishing");
        mConnection.sendPacket(myPresence);       

        //2. Config roster
        Roster roster = mConnection.getRoster();
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all); 
        roster.addRosterListener(new MyRosterListener());

        // For debug only.
        if (Constants.LOGV_ENABLED) {
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            Collection<RosterEntry> entries = roster.getEntries();
            for(RosterEntry r:entries) {
                Presence presence = roster.getPresence(r.getUser().toString());
                Log.d(TAG, "Entry: " + r.getUser() + " / " + presence);
                Presence presence1 = new Presence(Presence.Type.subscribe);   
                presence1.setTo(r.getUser());  
                 
                Presence presence2 = new Presence(Presence.Type.subscribed);   
                presence2.setTo(r.getUser()); 
                
                
                mConnection.sendPacket(presence1);
                mConnection.sendPacket(presence2);
            }
        }

        resetPresenceProvider();
        initPresenceProvider();

        asycContact(Constants.DEFAULT_SERVER,mAccount.getUserName());
    }

    /*
     * Reset/clear presence provider.
     */
    private void resetPresenceProvider() {
        Cursor cursor = mContentResolver.query(
                Uri.parse("content://com.borqs.notification/user"), new String[] {
                        "borqs_id","status" }, null, null, null);
        Log.i(TAG,"delete all friends");
        if (null != cursor && cursor.getCount() > 0) {
            mContentResolver.delete(Uri.parse("content://com.borqs.notification/user"), null,null);
        }
    }

    /*
     * Read roster list to init presence provider.
     */
    private void initPresenceProvider() {
        Roster roster = mConnection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry r : entries) {
            Presence presence3 = roster.getPresence(r.getUser().toString());
            Log.d(TAG, "Entry: " + r.getUser() + " / " + presence3);
            String ss[] = new String[2];
            ss= r.getUser().toString().split("@");
            ContentValues values = new ContentValues();
            values.put("borqs_id", ss[0]);
            if (presence3.toString().startsWith("available")) {
                values.put("status", "1");
            } else { //if(presence3.toString().startsWith("unavailable")){
                values.put("status", "0");
            }
            mContentResolver.insert(UserContentProvider.CONTENT_URI, values); 
        }
    }

    class RetryTask extends TimerTask{
        
        int retryTimes = 3;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if(retryTimes > 0) {
                Log.d(TAG, "this is the "+retryTimes+"time retry");
                connectIfNeeded();
                if(isConnected()){
                    mTimer.cancel();
                }
                retryTimes--;
            }else{
                mTimer.cancel();
            }
        }
    }

    public void logout() {
        if (Constants.LOGV_ENABLED) {
            Log.i("queryUser","logout");
        }
        if(mAccount == null || !isConnected()) {
            return;
        }
        mConnection.disconnect();
        //mConnected = false;
    }

    public boolean register() {
        if(mAccount == null) {
        	Log.w("queryUser","mAccount is null");
            return false;
        }

        connectIfNeeded();
        if(!isConnected() || null == mConnection) {
        	Log.w("queryUser","not connect or connection is null");
            return false;
        }

        try {
            AccountManager accountManager = mConnection.getAccountManager();
            accountManager.createAccount(mAccount.getUserName(), 
                    mAccount.getPassword());
            // TODO: create account with useful attributes.
            //accountManager.createAccount(username, password, attributes);
            mAccount.setRegistered(true);
            return true;
        } catch (XMPPException e) {
            Log.w(TAG, "register failed: " + e.getMessage(), e);
            // 409: conflict means the user already exists.
            if( e.getXMPPError() != null ) {
            	if(e.getXMPPError().getCode() == 409) {
            		mAccount.setRegistered(true);
            		return true;
            	}
            }
            return false;
        } catch (IllegalStateException e1) {
            // when reconnecting, connect() will try to login in meantime,
            // but horrible network may cause connect OK then login failed,
            // catch the exception and report failure to delegate. 
            Log.e(TAG, "register failed: " + e1.getMessage(), e1);
        } catch (Exception ee) {
            Log.e(TAG, "register failed: " + ee.getMessage(), ee);
        }
        return false;
    }

    boolean TEST_TEST = true;
    
    Presence queryUserPresence(String name) {
        if(mConnection == null || !mConnection.isConnected() || mAccount == null) {
            return null;
        }

        Roster roster = mConnection.getRoster();
        if(TEST_TEST) {
            Collection<RosterEntry> entries = roster.getEntries();
            //roster.getPresence()
            Log.d(TAG, "getEntries: " + entries.size());
            Iterator<RosterEntry> iterator = entries.iterator();
            while(iterator.hasNext()) {
                RosterEntry entry = iterator.next();
                Log.d(TAG, "getEntries: " + entry.getUser() + "/" + entry.getName());
                // Convert to JID.
                if(name.equals(entry.getName())) {
                    //name = entry.getUser();
                }
            }
        }
        name = name + "@b052-desktop";
        Presence presence = roster.getPresence(name);
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "queryUserPresence: " + name + " " + presence.getMode());
        }
        return presence;
    }
    
    static int QUERY_TIMEOUT = 3000; // Default is 5000ms
    
    UserInfo queryUser(String name) {
        UserInfo userInfo = new UserInfo(name);
        
        if(name == null || name.length() < 2) { // 2 is a magic number
        	Log.w(TAG, "name is null or name length<2");
            return null;
        }

        if(!isConnected() || mAccount == null) {
        	Log.w(TAG,"not connect or account is null");
            return null;
        }
        UserPresenceIQ userPresence = new UserPresenceIQ(
                name, mAccount.getUserName());
        userPresence.setFrom(mConnection.getUser());
        userPresence.setType(IQ.Type.GET);
        //Log.w("===  queryUserPresence send ===", userPresence.toString());
        //Log.w("===  queryUserPresence send ===", userPresence.toXML());
        
        try {
            //mConnection.sendPacket(userPresence); 
            PacketFilter filter = new AndFilter(
                    new PacketIDFilter(userPresence.getPacketID()),
                    new PacketTypeFilter(IQ.class));
            PacketCollector collector = mConnection.createPacketCollector(filter);
            mConnection.sendPacket(userPresence);
            //IQ result = (IQ)collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            IQ result = (IQ)collector.nextResult(QUERY_TIMEOUT);
            // Stop queuing results
            collector.cancel();
            if (result == null) {
                Log.w(TAG, "queryUserPresence result is empty, likely timeout.");
                onIqRequestFailed();
                return null;
            } else {
                if(!(result instanceof UserPresenceIQ)) {
                    Log.e(TAG, "queryUserPresence unknown result instance.");
                    return null;
                }
                if (result.getType() == IQ.Type.ERROR) {
                    Log.w(TAG, "queryUserPresence result is error.");
                    return null;
                }
            }
            String presence = ((UserPresenceIQ)result).getStatus();
            //userInfo.mPresence = "1".equals(presence) ? 1 : 0;
            userInfo.mPresence = Integer.parseInt(presence);
            userInfo.mIpAddress = ((UserPresenceIQ)result).getIp();

            //Log.w(TAG, "==== queryUserPresence result - " + result.toXML());
            //Log.w(TAG, "==== queryUserPresence - " + name + ": " + presence + ", " + 
            //        userInfo.mIpAddress);
            
            //return new Presence("1".equals(presence) ? Type.available : Type.unavailable);
            if (Constants.LOGV_ENABLED) {
                Log.i("queryUser","userinfo is:"+userInfo);
            }
            return userInfo;
        } catch (IllegalStateException e1) {
            Log.w(TAG, "queryUserPresence exception: ", e1);
        }
        return null;
    }

    public void sendNotification(String to, String msg) {
        
    }

    private void onIqRequestFailed() {
        ConnectivityManager connectivity = (ConnectivityManager) NotificationService
                .getInstance().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            Log.i("queryUser",
                    "NetworkInfo state is:" + info.getState() + ", Detailed: " + info.getDetailedState());
            Log.i("queryUser", "NetworkInfo type is:" + info.getType() + " Connected: " + info.isConnected());
        }
        Handler handler = NotificationService.getInstance().getHandler();
        handler.sendMessageDelayed(
                handler.obtainMessage(NotificationService.MSG_DETECT_NETWORK), 0);

    }

    static String CLIENT_RESOURCE = "com.borqs.notification";
    public String getResource() {
        return CLIENT_RESOURCE;
    }

    void connectIfNeeded() {
        if(mConnection == null) {
            init();
            if(mConnection == null) {
                Log.w(TAG, "Connection init failed.");
                return;
            }
        }
        if(mConnection.isConnected()) {
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "Already connected.");
            }
            return;
        }

        try {
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "connectIfNeeded. Connecting...");
            }
            mConnection.connect();
            mConnection.addConnectionListener(mConnListener);
            mConnection.addPacketListener(mPacketListener, mPacketFilter);
            //mConnected = true;
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "Connected.");
            }
        } catch (XMPPException e) {
            Log.e(TAG, "XMPPException. connect failed: " + e.getMessage(), e);
        } catch (IllegalStateException e1) {
            // when reconnecting, connect() will try to login in meantime,
            // but horrible network may cause connect OK then login failed,
            // catch the exception and report failure to delegate. 
            Log.e(TAG, "IllegalStateException. connect failed: " + e1.getMessage(), e1);
        } catch (Exception ee) {
            Log.e(TAG, "Exception. connect failed: " + ee.getMessage(), ee);
        }
    }

    private void init() {

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(UserPresenceIQ.IQ_USER_PRESENCE_NAME, 
                UserPresenceIQ.IQ_USER_PRESENCE_NAMESPACE, 
                new UserPresenceIQProvider());

        mConnConfig = new ConnectionConfiguration(mAccount.getServerHost(),
                mAccount.getServerPort(), getResource());
        // we need a secure connection.
        mConnConfig.setSecurityMode(SecurityMode.disabled);
        //mConnConfig.setSecurityMode(SecurityMode.required);
        //mConnConfig.setSASLAuthenticationEnabled(false);
        mConnConfig.setTruststoreType("BKS");
        mConnConfig.setTruststorePath("/system/etc/security/cacerts.bks");

        mConnection = new XMPPConnection(mConnConfig);
        mConnListener = new MyConnectionListener();
        mPacketFilter = new MyPacketFilter();
        mPacketListener = new MyPacketListener();
    }
    
    //static String AT_HOST = "b052-desktop";
    static String AT_HOST = Constants.DEFAULT_SERVER;
    void sendMessage(int toAppId, String toUserName, String data, String fromp) {
        // TODO: generate correct JID.
        Log.i(TAG,"toAppId is:"+toAppId+"and toUserName is:"+toUserName+"and dta is:"+data+"and fromp is:"+fromp);
        String jid = toUserName + "@" + AT_HOST;
        try {
            Chat chat = mConnection.getChatManager().createChat(
                    jid, new MessageListener() {
    
                public void processMessage(Chat chat, Message message) {
                    Log.e(TAG, "Received ack message: " + message);
                }
            });

            // Create the msg and send it.
            Message msg = new Message();
            msg.setBody(composeMessage(toAppId, data, fromp));
            //msg("favoriteColor", "red");
            chat.sendMessage(msg);
        } catch (XMPPException e) {
            e.printStackTrace();
        }

    }

    private String composeMessage(int appId, String data, String fromp) {
        return "app_id=" + appId + "&" + "data=" + data + "&fromp=" + fromp;
    }

    private void createChatListener() {
        mConnection.getChatManager().addChatListener(
                new ChatManagerListener() {
                    public void chatCreated(Chat chat, boolean createdLocally)
                    {
                        if (!createdLocally)
                            chat.addMessageListener(new MessageListener() {

                                @Override
                                public void processMessage(Chat chat,
                                        Message message) {
                                    Log.e(TAG, "Received message: " + message);
                                    
                                }});;
                    }
                });
    }

    private void sendReponse(String tag) {
        if (Constants.LOGD_ENABLED) {
            Log.d(TAG, "sendReponse, tag=" + tag);
        }

        if (null == mConnection || !mConnection.isConnected() || 
            !mConnection.isAuthenticated()) {
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "connection is not ready, cannot send response");
            }
            return;
        }

        try {
            org.jivesoftware.smack.packet.Message msg = 
                new org.jivesoftware.smack.packet.Message();
            msg.setFrom(mConnection.getUser());
            msg.setTo(Constants.MSG_PUSH_CLIENT_RESPONSE);
            msg.setBody("action=" + Constants.MSG_PUSH_CLIENT_RESPONSE_ACTION 
                    + "&tag=" + tag);
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "send client response tag="+tag);
            }
            mConnection.sendPacket(msg);
        } catch (Exception e) {
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "send client response failed", e);
            }
        }
        
    }

    /*
     * Return true if we should handle the msg internally.
     */
    private boolean handleIntentInternal(Intent intent) {
        Bundle b = intent.getExtras();
        if (null != b) {
            // 1. See if an internal msg.
            String appId = b.getString("app_id");

           if(appId != null && 
                    appId.equals(Integer.toString(Constants.APP_ID_SELF))) {
                // We must have a msg type!
                int type = Integer.parseInt(b.getString("type"));
                switch(type) {
                case Constants.MSG_TYPE_KEEP_WORKING:
                    break;

                case Constants.MSG_TYPE_NEW_FILE:
                    String data = b.getString("data");
                    final String fromuser = b.getString("from");
                    int  i = data.indexOf("**");
                    final String filename = java.net.URLDecoder.decode(data.substring(0, i));
                    final String sendtime = data.substring(i+2);
                    final String fromp = b.getString("fromp");
                    if(mFileTranfers == null) {
                        mFileTranfers = new FileTransfers();
                    }
                    new Runnable() {
                        @Override
                        public void run() {
                            mFileTranfers.getFile(sendtime, filename, fromuser, fromp); 
                        }}.run();
                    break;
                
                case Constants.MSG_TYPE_DEFAULT:
                    return false; // Should we return TRUE?

                default:
                    if (Constants.LOGD_ENABLED) {
                        Log.w(TAG, "Unknown message type: " + type);
                    }
                    break;
                }
                return true;
            }

            // 2. See if a keep walking msg. 
            // TODO: move keep walking msg to internal msg.
            String action = b.getString("action");
            if (null != action && action.equals("keeptalking")) {
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "action keeptalking, will handle it internally");
                }
                if (NotificationService.getInstance().isModelSupportLongConnection() || 
                        NotificationService.getInstance().inDebugMode()) {
                    (new ReplyKeepTalkingMsg(mAccount.getUserName(), b.getString("tag"))).start();
                }
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////
    // 
    private static final String KEEP_TALKING_RESP_URL = 
        "http://xdevice.borqs.com/borqs/keep_talking_resp.php";
    private class ReplyKeepTalkingMsg extends Thread {
        private String mUname;
        private String mTag;
        public ReplyKeepTalkingMsg(String uname, String tag) {
            mUname = uname;
            mTag = tag;
        }

        public void run() {
            try {
                URL rUrl = new URL(KEEP_TALKING_RESP_URL + "?uname="
                    + mUname + "&tag=" + mTag);
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "rUrl="+rUrl.toString());
                }
                URLConnection conn = rUrl.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line=in.readLine()) != null) {
                    buffer.append(line);
                }
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "keep_talking: server side response for keep_talking("
                        + mUname + ", " + mTag + ") is:" + buffer.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "keep_talking: reply msg failed, ", e);
            }
        }
    };

    ///////////////////////////////////////////////////////////////////////
    // Interface from Smack: RosterListener
    private class MyRosterListener implements RosterListener {
        public void entriesAdded(Collection<String> arg0) {
            // TODO Auto-generated method stub
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "add a friend");
                Log.d(TAG,"friend is:"+arg0);
            }
            //String serverName="@"+mConnection.getServiceName();
            String user = "";  
            Iterator<String> it = arg0.iterator();  
            ContentValues values = new ContentValues();
            Cursor cursor = null;
            if(it.hasNext()){
                user=it.next();
                Presence presence = new Presence(Presence.Type.subscribe);   
                presence.setTo(user);  

                Presence presence2 = new Presence(Presence.Type.subscribed);   
                presence2.setTo(user); 

                mConnection.sendPacket(presence);
                mConnection.sendPacket(presence2);
                Log.i(TAG,"user is:"+user);
                String ss[] = new String[2];
                ss = user.toString().split("@");
                String borqs_id = ss[0];
                values.put("borqs_id", borqs_id);   
                values.put("status", "0"); 
                cursor = mContentResolver.query(
                        UserContentProvider.CONTENT_URI, new String[] {
                                "borqs_id"}, "borqs_id=?", new String[]{borqs_id}, null);
                if (null == cursor || cursor.getCount() < 1) {
                    Log.d(TAG, "insert a friend");
                    mContentResolver.insert(UserContentProvider.CONTENT_URI, values); 
                }else{
                    Log.d(TAG, "update a friend");
                    mContentResolver.update(UserContentProvider.CONTENT_URI, values, "borqs_id=?", new String[]{borqs_id});
                }
                cursor.close();
            }
        }

        public void entriesDeleted(Collection<String> arg0) {
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "delete a friend");
            }
            String user = "";  
            Iterator<String> it = arg0.iterator();  
            ContentValues values = new ContentValues();
            String borqs_id="";
            Cursor cursor = null;
            if(it.hasNext()){
                user=it.next();
                
                Log.i(TAG,"user is:"+user);
                String ss[] = new String[2];
                ss = user.toString().split("@");
                borqs_id = ss[0];
                Log.i(TAG,"Delete borqs_id is:"+borqs_id);
                values.put("borqs_id", borqs_id); 
                cursor = mContentResolver.query(
                        UserContentProvider.CONTENT_URI, new String[] {
                                "borqs_id"}, "borqs_id=?", new String[]{borqs_id}, null);
                if (null != cursor && cursor.getCount() > 0) {
                    mContentResolver.delete(UserContentProvider.CONTENT_URI, "borqs_id=?", new String[]{borqs_id});
                }
            }  
            cursor.close();
            
        }

        public void entriesUpdated(Collection<String> arg0) {
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "update a friend");
            }
        }

        public void presenceChanged(Presence arg0) {
            String status = arg0.toString();
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "presenceChanged: " + arg0.getFrom() + " - " + status);
            }

            String ss[] = new String[2];
            ss=arg0.getFrom().toString().split("@");
            String userId = ss[0];
            if(status.startsWith("available")){
                ContentValues values = new ContentValues();
                values.put("borqs_id",userId);
                values.put("status", "1");
                mContentResolver.delete(UserContentProvider.CONTENT_URI, "borqs_id=?", new String[]{userId});
                mContentResolver.insert(UserContentProvider.CONTENT_URI, values); 
                //mContentResolver.update(UserContentProvider.CONTENT_URI, values, "borqs_id=?", new String[]{userId});
            } else if(status.startsWith("unavailable")){
                ContentValues values = new ContentValues();
                values.put("borqs_id",userId);
                values.put("status", "0");
                mContentResolver.delete(UserContentProvider.CONTENT_URI, "borqs_id=?", new String[]{userId});
                mContentResolver.insert(UserContentProvider.CONTENT_URI, values);
                //mContentResolver.update(UserContentProvider.CONTENT_URI, values, "borqs_id=?", new String[]{userId});
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // Interface from Smack: ConnectionListener
    private class MyConnectionListener implements ConnectionListener {

        @Override
        public void connectionClosed() {
            // TODO Auto-generated method stub
            Log.i("queryUser", "connectionClosed", new Exception());
            if (mAccount != null) {
                Log.i("queryUser", "logout");
                mAccount.logout();
            } else {
                Log.i("queryUser", "mAccount is null");
            }
            if (NotificationService.getInstance() != null) {
                Log.i("queryUser", "NotificationService retry");
                NotificationService.getInstance().reTry();
            } else {
                Log.i("queryUser", "NotificationService is null");
            }
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            // TODO Auto-generated method stub
            Log.i("queryUser", "connectionClosedOnError" + " " + e.toString(),
                    new Exception());

            if (mAccount != null) {
                Log.i("queryUser", "logout");
                mAccount.logout();
            } else {
                Log.i("queryUser", "mAccount is null");
            }
            if (NotificationService.getInstance() != null) {
                Log.i("queryUser", "NotificationService retry");
                NotificationService.getInstance().reTry();
                Log.i("queryUser","*****************************************");
            } else {
                Log.i("queryUser", "NotificationService is null");
            }
        }

        @Override
        public void reconnectingIn(int seconds) {
            // TODO Auto-generated method stub
            Log.i("queryUser", "reconnectingIn time is:" + seconds,
                    new Exception());
            if (NotificationService.getInstance() != null) {
                NotificationService.getInstance().reTry();
            } else {
                Log.i("queryUser", "NotificationService is null");
            }
        }

        @Override
        public void reconnectionSuccessful() {
            // TODO Auto-generated method stub
        	Log.i(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            // TODO Auto-generated method stub
        	Log.i(TAG,"reconnectionFailed: " + e.toString());
        }
    }

    // Interface from Smack: PacketFilter
    private class MyPacketFilter implements PacketFilter {
        public boolean accept(Packet packet) {
            //packet.
            return true;
        }
    }

    // Interface from Smack: PacketListener
    private class MyPacketListener implements PacketListener {

        @Override
        public void processPacket(Packet packet) {
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "process packet: " + packet);
            }
            
            if (packet instanceof IQ) {
                // TODO: deal with IQ
                IQ iq = (IQ) packet;
                processIQ(iq);
            } else if (packet instanceof Presence) {
                // TODO: deal with Presence
                Presence p = (Presence) packet;
                processPresence(p);
            } else if (packet instanceof Message) {
                Message msg = (Message) packet;
                processMessage(msg);
            } else {
                // TODO: default situation, error
            }
        }

        private void processMessage(Message msg) {
            String body = msg.getBody();
            String from = msg.getFrom();
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "From " + from + ": " + body);
            }

            try {
                StringTokenizer tokenizer = new StringTokenizer(body, "&");
                Intent intent = new Intent(Constants.BORQS_NOTIFICATION_INTENT);
                // Trick: put the default msg type here.
                intent.putExtra("type", Integer.toString(Constants.MSG_TYPE_DEFAULT));
                String tag = null;
                while (tokenizer.hasMoreTokens()) {
                    String part = tokenizer.nextToken();
                    int idx = part.indexOf("=");
                    if (-1 != idx) {
                        String key = part.substring(0, idx).toLowerCase();
                        String value = part.substring(idx+1);

                        if (key.equals("to")) {
                            // ignore: to
                            if (Constants.LOGD_ENABLED) {
                                Log.d(TAG, "msg to=" + value);
                            }
                        } else if(key.equals("app_id")) {
                            intent.putExtra("app_id", value);
                        } else if(key.equals("type")) {
                            intent.putExtra("type", value);
                        } else if(key.equals("data")) {
                            intent.putExtra("data", value);
                        } else if(key.equals("fromp")) {
                            intent.putExtra("fromp", value);
                        } else {
                            intent.putExtra(key, URLDecoder.decode(value, "UTF-8"));
                            if (key.equals("tag")){
                                tag = value;
                            }
                        }
                    }
                }
                intent.putExtra("time", getTimestamp(msg));
                if (null != intent && !handleIntentInternal(intent)) {
                    //mServiceContext.sendNotification(intent);
                    if (Constants.LOGV_ENABLED) {
                        Log.d(TAG, "Send Intent: " + intent);
                    }
                    if(from != null) {
                        int index = from.indexOf("@");
                        if(index > 0) {
                            from = from.substring(0, index);
                        }
                    }
                    Log.d("------------", "from : " + from);
                    intent.putExtra("from", from);
                    if(null != NotificationService.getInstance()) {
                        NotificationService.getInstance().sendBroadcast(intent);
                    }
                }

                // process msg done, send ack to server, mark msg is delivered
                if (null != tag && 0 != tag.length()) {
                    //android.os.Message m = mDelegateHandler.obtainMessage(MSG_CLIENT_RESPONSE, tag);
                    //mDelegateHandler.sendMessage(m);
//                    sendReponse(tag);
                }

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "processMessage UnsupportedEncodingException ", e);
            } catch (NoSuchElementException e) {
                Log.e(TAG, "processMessage NoSuchElementException ", e);
            } catch (Exception e) {
                Log.e(TAG, "processMessage Exception ", e);
            }
        }
        
        private void processPresence(Presence p) {
            // TODO: process presence
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "From " + p.getFrom() + ": " + p.getStatus());
            }
            Presence.Type type = p.getType();
            type = Type.subscribe;
            if(type == Type.subscribe) {
            }
             
        }
        
        private void processIQ(IQ iq) {
            // TODO: process IQ
//Log.e("----------", "Process IQ: " + iq.toXML(), new Exception());
        }
    }
    private void asycContact(String server,String borqs_id){
        Log.i(TAG,"asycContact");
        String sURL = "http://"+server+":9090/plugins/xDevice/change?userId="+borqs_id+"&operate=null&friend=null";
        Log.i(TAG,"url is:"+sURL);
        URL url;
        int responceCode;
        try {
            url = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setConnectTimeout(30000); // 毫秒
            con.setReadTimeout(30000);
            con.connect();
            responceCode = con.getResponseCode();
            if (responceCode == java.net.HttpURLConnection.HTTP_OK) {
                Log.i(TAG,"HTTP OK");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private long getTimestamp(Message msg) {
        DelayInformation df = null;
        long timestamp = new Date().getTime();
        try {
            df = (DelayInformation) msg.getExtension("x", "jabber:x:delay");
        } catch(Exception e) {
            
        }
        if(df != null) {
            timestamp = df.getStamp().getTime();
        }
        return timestamp;
    }
}
