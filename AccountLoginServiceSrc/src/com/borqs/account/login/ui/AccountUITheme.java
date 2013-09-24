package com.borqs.account.login.ui;

import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.Utility;
import com.borqs.account.login.R;
import android.content.Context;
import android.text.InputType;

public class AccountUITheme {
    private static AccountUITheme mTheme = null;
    
    private Context mContext;
    private int mDefRegFeature = ConstData.FEATURE_SUPPORT_EMAIL 
                                | ConstData.FEATURE_SUPPORT_PHONE
                                | ConstData.FEATURE_SUPPORT_ONEKEY_REGISTER; 
    private static int mFeatures;
    
    
    private AccountUITheme(Context ctx, int features){
        mContext = ctx.getApplicationContext();
        mFeatures = features;
        if (mFeatures == 0 || mFeatures == ConstData.FEATURE_SUPPORT_ONEKEY_REGISTER){
            // default value:support both email&phone register
            mFeatures = mFeatures|mDefRegFeature;
        }
    }
    
    public static AccountUITheme create(Context ctx, int features){
        if (mTheme == null){
            mTheme = new AccountUITheme(ctx, features);
        }
        return mTheme;
    }
    
    public static AccountUITheme getTheme(Context ctx){
        if (mTheme == null){
            mTheme = create(ctx, mFeatures);
        }        
        return mTheme;
    }
    
    public void changeTheme(int features){
        mFeatures = features;
    }
    
    public String getRegisterUserNameHint(){
        BLog.d("getRegisterUserNameHint:" + mFeatures);
        String res = mContext.getString(R.string.acl_register_user_input_hint);
        
        if (!hasDefaultRegModeFeatures()){
            if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
                res = mContext.getString(R.string.acl_register_user_hint_mail);
            } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
                res = mContext.getString(R.string.acl_register_user_hint_phone);
            }
        } 
        
        return res;
    }
    
    public int getRegisterUserInputType(){
        int type = InputType.TYPE_CLASS_TEXT;
        
        if (!hasDefaultRegModeFeatures()){
            if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
                type = type|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
                type = InputType.TYPE_CLASS_PHONE;
            }
        } 
        
        return type;
    }
    
    public boolean validateRegisterName(String userName){
        boolean res = false;
        
        if (hasDefaultRegModeFeatures()){
            res = Utility.isValidPhoneNumber(userName) || Utility.isValidEmailAddress(userName);            
        } else if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
            res = Utility.isValidEmailAddress(userName);
        } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
            res = Utility.isValidPhoneNumber(userName);
        }
        
        return res;
    }
    
    public String getLoginUserNameHint(){
        BLog.d("getLoginUserNameHint:" + mFeatures);
        String res = mContext.getString(R.string.acl_register_user_input_hint);
        
        if (!hasDefaultLoginFeatures()){
            if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
                res = mContext.getString(R.string.acl_register_user_hint_mail);
            } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
                res = mContext.getString(R.string.acl_register_user_hint_phone);
            }
        } 
        
        return res;
    }
    
    public int getLoginUserInputType(){
        int type = InputType.TYPE_CLASS_TEXT;
        
        if (!hasDefaultLoginFeatures()){
            if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
                type = type|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
                type = InputType.TYPE_CLASS_PHONE;
            }
        } 
        
        return type;
    }
    
    public boolean validateLoginName(String userName){
        boolean res = false;
        
        if (hasDefaultLoginFeatures()){
            res = Utility.isValidPhoneNumber(userName) || Utility.isValidEmailAddress(userName);            
        } else if (hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)){
            res = Utility.isValidEmailAddress(userName);
        } else if (hasFeatures(ConstData.FEATURE_SUPPORT_PHONE)){
            res = Utility.isValidPhoneNumber(userName);
        }
        
        return res;
    }
    
    public boolean hasEmailRegFeature(){
        return hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL);
    }
    
    public boolean hasOnekeyRegFeature(){
        return hasFeatures(ConstData.FEATURE_SUPPORT_ONEKEY_REGISTER);
    }
    
    private boolean hasDefaultRegModeFeatures(){
        return hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)&&hasFeatures(ConstData.FEATURE_SUPPORT_PHONE);
    }
    
    private boolean hasDefaultLoginFeatures(){
        return hasFeatures(ConstData.FEATURE_SUPPORT_EMAIL)&&hasFeatures(ConstData.FEATURE_SUPPORT_PHONE);
    }
    
    private boolean hasFeatures(int feature){
        return (mFeatures&feature) != 0;
    }
}
