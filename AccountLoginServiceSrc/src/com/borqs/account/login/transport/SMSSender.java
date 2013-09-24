/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.borqs.account.login.util.BLog;

public final class SMSSender {
    public static final int ERROR_DEFAULt_ERROR = 10001;

    public static final String ACCOUNT_REQUEST_SMS_RESPONSE_ACTION = "android.com.borqs.sms.account_requst";
    // public static final String SMS_REG_NUMBER = "+8615010500427";
    public static final String ACCOUNT_REQUEST_MESSAGE = "accountrequest/verifybysim";
    public static final String ACCOUNT_REQUEST_SEPERATE = " ";

    private Context mConetxt;
    private BroadcastReceiver mReceiver;
    private int mResultCode;
    private Exception mLastError;
    private Runnable mCallbackOnResult;

    public SMSSender(Context context) {
        mConetxt = context;
    }

    public void sendMessage(String sendto, String body, Runnable resultCallback) {
        String message = ACCOUNT_REQUEST_MESSAGE + ACCOUNT_REQUEST_SEPERATE
                + body;
        Intent i = new Intent(ACCOUNT_REQUEST_SMS_RESPONSE_ACTION);
        mCallbackOnResult = resultCallback;
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(mConetxt, 0,
                i, 0);
        SmsManager smsMgr = SmsManager.getDefault();

        mReceiver = new SMSGatewayReceiver();
        mConetxt.registerReceiver(mReceiver, new IntentFilter(
                ACCOUNT_REQUEST_SMS_RESPONSE_ACTION));
        try {
            BLog.d("SMS message: " + message);
            String smsNumber = sendto;
            BLog.d("SMS number: " + smsNumber);
            if (TextUtils.isEmpty(smsNumber)) {
                throw new Exception("Unsupported service on this server!");
            }
            smsMgr.sendTextMessage(smsNumber, null, message, deliveryIntent,
                    null);
        } catch (Exception e) {
            BLog.d("sendBindPhoneNumberMessage Exception: " + e.toString());
            mResultCode = ERROR_DEFAULt_ERROR;
            mLastError = e;
            if (mCallbackOnResult != null) {
                mCallbackOnResult.run();
            }
        }
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Exception getLastError() {
        if (mLastError == null){
            mLastError = new Exception(" send result:" + String.valueOf(mResultCode));
        }
        return mLastError;
    }

    class SMSGatewayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACCOUNT_REQUEST_SMS_RESPONSE_ACTION)) {                
                mResultCode = getResultCode();
                BLog.d("Get process account request result " + mResultCode);
                if (mCallbackOnResult != null) {
                    mCallbackOnResult.run();
                }
            }
            mConetxt.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
