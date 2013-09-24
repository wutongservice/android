package com.borqs.account.login.test.impl;

import com.borqs.account.login.intf.IDevice;

public class MockDevice implements IDevice {
    private String mDeviceId;
    
    public void setDeviceId(String devID){
        mDeviceId = devID;
    }
    
    @Override
    public String getDeviceId() {
        // TODO Auto-generated method stub
        return mDeviceId;
    }

    @Override
    public String getPhoneNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImsi() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImei() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSimSerial() {
        // TODO Auto-generated method stub
        return null;
    }

}
