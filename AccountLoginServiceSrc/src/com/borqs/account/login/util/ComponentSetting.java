package com.borqs.account.login.util;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.borqs.account.login.service.AccountDataService;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.service.BMSAuthenticatorService;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.service.EnableComponentsReceiver;

/**
 * a util class to get/set Account login components state.
 * some of the components are loginbySimActivity, AccountService etc.
 * @author linxh
 *
 */
public class ComponentSetting {
    static long MainUiThreadID;

    static void checkNotUIThread() {
        if (Thread.currentThread().getId() == MainUiThreadID) {
            throw new IllegalAccessError(
                    "function cannot be called in UI thread!");
        }
    }

    public static boolean isComponentAvailable(Context context){
        boolean res = false;
        
        if (isBorqsAccountServiceEnabledInOtherPkg(context)){
            BLog.d("other apk componets availabe");
            res = true;
        } else if (isServiceAvailable(context)){
            BLog.d("self componets available");
            res = true;
        }
        
        return res;
    }
    
    public static void ensureEnableAccountComponents(Context context) {
        MainUiThreadID = Thread.currentThread().getId();
        /*if (isBorqsAccountServiceEnabledInOtherPkg(context)) {
            BLog.d("disable componets");
            doDisableComponents(context);
        } else {
            BLog.d("enable componets");
            doEnableComponents(context);
        }*/
    }
    
    private static boolean isBorqsAccountServiceEnabledInOtherPkg(
            Context context) {
        AuthenticatorDescription[] authenticators = AccountManager.get(context)
                .getAuthenticatorTypes();
        for (AuthenticatorDescription au : authenticators) {
            if (au != null && ConstData.BORQS_ACCOUNT_TYPE.equals(au.type)) {
                if (!context.getPackageName().equals(au.packageName))
                    return true;
            }
        }
        return false;
    }
    
    private static boolean isServiceAvailable(Context context){
        boolean res = false;
        PackageManager pm = context.getPackageManager();
        int enable = pm.getComponentEnabledSetting(new ComponentName(context,
                BMSAuthenticatorService.class));
        if (PackageManager.COMPONENT_ENABLED_STATE_DISABLED != enable){
            res = true;
        }
        return res;
    }
    
    public static void doEnableComponents(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context,
                AccountDataService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context,
                BMSAuthenticatorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context,
                EnableComponentsReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    public static void doDisableComponents(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context,
                AccountService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context,
                BMSAuthenticatorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context,
                EnableComponentsReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }
}
