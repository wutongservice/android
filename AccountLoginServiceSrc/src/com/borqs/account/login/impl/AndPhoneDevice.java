package com.borqs.account.login.impl;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.borqs.account.login.intf.IDevice;
/**
 * android phone device interface implements class
 * @author linxh
 *
 */
public class AndPhoneDevice implements IDevice {
    private Context mContext;
    private String mDeviceId;
    
    public AndPhoneDevice(Context ctx){
        mContext = ctx;
    }
    
    @Override
    public String getDeviceId(){
        if (mDeviceId == null){
            mDeviceId = getImsi();
        }
        return (mDeviceId==null)?"":mDeviceId;
    }
    
    @Override
    public String getPhoneNumber() {
        TelephonyManager telephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getLine1Number();
    }

    @Override
    public String getImsi() {
        TelephonyManager telephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telephonyMgr.getSubscriberId();
        return imsi==null? "" : imsi;
    }

    @Override
    public String getImei() {
        TelephonyManager telephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyMgr.getDeviceId();
        return imei==null? "" : imei;
    }

    @Override
    public String getSimSerial() {
        TelephonyManager telephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String sn = telephonyMgr.getSimSerialNumber();        
        return sn==null? "" : sn;
    }

}
