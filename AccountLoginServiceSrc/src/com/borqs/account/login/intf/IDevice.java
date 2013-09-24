package com.borqs.account.login.intf;


public interface IDevice {
    
    /**
     * get the device unique id
     */
    public String getDeviceId(); 
    
    /**
     * get the device number if possible
     */
    public String getPhoneNumber();
    
    /**
     * get the device IMSI code
     */
    public String getImsi();
    
    /**
     * get the device IMEI code
     */
    public String getImei();
   
    
    /**
     * get the Sim Card Serial Number
     */
    public String getSimSerial();
}
