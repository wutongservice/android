package com.borqs.common.util;

import android.app.Dialog;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.ToastUtil;
import twitter4j.AsyncQiupu;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.os.Message;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import twitter4j.conf.ConfigurationContext;


public class AsyncApiUtils {
    private static final String TAG = "AsyncApiUtils";
    public static final String RESULT = "result";
    public static final String ERROR = "error";
    public static AsyncQiupu asyncInstance;
    static {
        asyncInstance = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
    }

	public interface AsyncApiSendRequestCallBackListener {
    	public void sendRequestCallBackBegin();
    	public void sendRequestCallBackEnd(boolean result, long uid);
    }
	
	public static void sendApproveRequest(final long uid, final String message, final AsyncQiupu asyncQiupu, 
			                       final AsyncApiSendRequestCallBackListener callback) {
		
		callback.sendRequestCallBackBegin();
		asyncQiupu.sendApproveRequest(AccountServiceUtils.getSessionID(), String.valueOf(uid), message, new TwitterAdapter() {
			public void sendApproveRequest(boolean result) {
				Log.d(TAG, "finish sendApproveRequest :" + result);
				callback.sendRequestCallBackEnd(result, uid);
			}
			
			public void onException(TwitterException ex,TwitterMethod method) {
				Message msg = new Message();
				msg.getData().putBoolean(RESULT, false);
				callback.sendRequestCallBackEnd(false, uid);
			}
		});
	}
}
