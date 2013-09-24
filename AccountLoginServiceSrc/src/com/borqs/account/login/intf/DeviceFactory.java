package com.borqs.account.login.intf;

import android.content.Context;

import com.borqs.account.login.impl.AndPhoneDevice;

public class DeviceFactory {
    public static IDevice getDefaultDevice(Context ctx){
        return new AndPhoneDevice(ctx.getApplicationContext());
    }
}
