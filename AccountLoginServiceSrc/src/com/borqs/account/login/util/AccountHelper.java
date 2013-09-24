package com.borqs.account.login.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.borqs.account.login.R;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.ui.AccountLoginActivity;
import com.borqs.account.login.ui.AccountRemoveActivity;
import com.borqs.account.login.ui.InfoDialogActivity;

public class AccountHelper {
    // sync appid
    public static final String BORQS_SYNC_APP_ID = "10";
    // sync appSecret
    public static final String BORQS_SYNC_APP_SECRET = "appSecret10";

    private static final String INTENT_EXTRA_FEATURE_ID = "feature_id";
    //private static final String INTENT_EXTRA_REFRESH_ACCOUNT = "refresh_account";
    private static final String DEFAULT_QIUPU_FEATURE_ID = "1";

    public static Intent actionLoginBorqsAccountIntent(Context context,
            String requestAppId) {
        Intent intent = new Intent(context, AccountLoginActivity.class);
        intent.putExtra(INTENT_EXTRA_FEATURE_ID,
                requestAppId == null ? DEFAULT_QIUPU_FEATURE_ID : requestAppId);
        return intent;
    }

    public static Intent actionUpdateCredentialsIntent(Context context,
            Account account) {
        Intent intent = new Intent(context, AccountLoginActivity.class);
        //intent.putExtra(INTENT_EXTRA_REFRESH_ACCOUNT, account);
        intent.putExtra(ConstData.OPTIONS_RELOGIN, true);
        intent.putExtra(ConstData.ACCOUNT_USER_ID, account.name);
        return intent;
    }

    public static Intent actionRemoveAccountIntent(Context context){
        Intent intent = new Intent(context, AccountRemoveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
        //return new Intent("com.borqs.account.ui.InfoDialogActivity");
    }

    public static Intent actionInfoDialogIntent(Context context) {                           
        return new Intent(context, InfoDialogActivity.class);
    }

    public static void showInfoDialog(Context context, String title,
            String message) {
        try
        {
            AlertDialog ad = new AlertDialog.Builder(context)
                    .setIcon(R.drawable.account_borqs_icon)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.acl_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                    /*
                     * .setNeutralButton(R.string.alert_dialog_something, new
                     * DialogInterface.OnClickListener() { public void
                     * onClick(DialogInterface dialog, int whichButton) {
                     * 
                     * 
                     * } }) .setNegativeButton(R.string.alert_dialog_cancel, new
                     * DialogInterface.OnClickListener() { public void
                     * onClick(DialogInterface dialog, int whichButton) {
                     * 
                     * 
                     * } })
                     */
                    .create();
            ad.setCanceledOnTouchOutside(false);
            ad.show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static ProgressDialog sLoginDialog;
    
    public static void showProgressDialog(Context context, String promptText) {
        closeProgressDialog();
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        dialog.setIcon(R.drawable.account_borqs_icon);//设置图标
        dialog.setMessage(promptText);
        dialog.setIndeterminate(false);//设置进度条是否为不明确
        dialog.setCancelable(true);//设置进度条是否可以按退回键取消
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        sLoginDialog = dialog;
    }
    
    public static void closeProgressDialog()
    {
        if(null!=sLoginDialog && sLoginDialog.isShowing())
        {
            sLoginDialog.dismiss();
            sLoginDialog = null;
        }
    }
    
    public static String GetErrorStringValue(Context context,String name,String defValue)
    {
        int id = context.getResources().getIdentifier(name,"string",context.getPackageName()); 
        if(id!=0)
            return context.getResources().getString(id);
        else
            return defValue;
    }
    
    public static String GetErrorDesc(Context ctx, int errCode)
    {
        return getErrorDesc(ctx, null, errCode);
    }
    
    public static String getErrorDesc(Context ctx, Exception exp, int errCode){
        String res = null;
        switch (errCode){
        case ConstData.ERROR_SERVER_CONNECT:
            res = GetErrorStringValue(ctx, "acl_error_connect_server", null);
            break;
        case ConstData.ERROR_SERVER_RSP_DATA:
            res = GetErrorStringValue(ctx, "acl_error_server_rsp_data", null);
            break;
        case ConstData.ERROR_REGISTER_TIME_OUT:
            res = GetErrorStringValue(ctx, "acl_error_reg_time_out", null);
            break;
        case ConstData.ERROR_SEND_REGISTER_SMS:
            res = GetErrorStringValue(ctx, "acl_error_sed_reg_send_sms", null);
            break;            
        case ConstData.ERROR_NO_SIM_CARD:
            res = GetErrorStringValue(ctx, "acl_error_no_sim_card", null);
            break;
        case ConstData.ERROR_SERVER_ERROR:
            res = GetErrorStringValue(ctx, "acl_error_server_unknown_error", null);
            break;
        case ConstData.ERROR_TICKET_INVALID:
            res = GetErrorStringValue(ctx, "acl_error_ticket_invalid", null);
            break;
        case ConstData.ERROR_USER_PWD_INVALID:
            res = GetErrorStringValue(ctx, "acl_error_usr_pwd_invalid", null);
            break;
        case ConstData.ERROR_NO_PHONE:
            res = GetErrorStringValue(ctx, "acl_error_no_phone", null);
            break;
        case ConstData.ERROR_USER_NOT_EXISTS:
            res = GetErrorStringValue(ctx, "acl_error_no_user", null);
            break;
        case ConstData.ERROR_NO_USER_RECORD:
            res = GetErrorStringValue(ctx, "acl_error_no_record", null);
            break;
        case ConstData.ERROR_NO_INPUT_VERFIY_CODE:
            res = GetErrorStringValue(ctx, "acl_error_code_is_empty", null);
            break;
        case ConstData.ERROR_VERIFY_CODE:
            res = GetErrorStringValue(ctx, "acl_error_cmp_verifycode_failed", null);
            break;
        case ConstData.ERROR_VERFIY_CODE_OUT:
            res = GetErrorStringValue(ctx, "acl_error_cmp_code_gt_3", null);
            break;
        case ConstData.ERROR_INVALID_USER:
            res = GetErrorStringValue(ctx, "acl_error_invalid_user", null);
            break;
        case ConstData.ERROR_SERVER_EXCEPTION:
            res = GetErrorStringValue(ctx, "acl_error_server_exception", null);
            break;
        case ConstData.ERROR_UNKNOWN:
            res = GetErrorStringValue(ctx, "acl_error_unknown", null);
            break;            
        }         
        
        if (res == null){
            res = GetErrorStringValue(ctx, "acl_error_unknown", null);
            if (exp != null){
                res = res + exp.toString();
            }
        }
        
        return res;
    }
    
    
    /*****************Account Helper function**********************************/
    /**
     * Helper function to check if there is authenticator for account type
     * 'com.borqs'
     * 
     * @return true - Ok for borqs account login/register by uniform ui 
     *         false - The application need to do login/register itself
     */
    public static boolean isBorqsAccountServicePreloaded(Context context) {
        AuthenticatorDescription[] authenticators 
                        = AccountManager.get(context).getAuthenticatorTypes();
        
        for (AuthenticatorDescription au : authenticators) {            
            if (au != null && ConstData.BORQS_ACCOUNT_TYPE.equals(au.type)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isBorqsAccountLogin(Context context){
        boolean res = false;
        if (getBorqsAccountId(context) != null){
            res = true;
        }
        return res;
    }
   
    /**
     * Remove the Borqs account from system. Require
     * "android.permission.MANAGE_ACCOUNTS"
     * 
     * @param context - Android context
     */
    public static void removeBorqsAccount(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            am.removeAccount(accounts[0], null, null);
        }
    }

    /**
     * Retrieve the account Id/Name in the system. Require
     * "android.permission.GET_ACCOUNTS"
     * 
     * @param context
     * @return borqs account id, null if no borqs account
     */
    public static String getBorqsAccountId(Context context) {
        Account[] accounts = AccountManager.get(context)
                                           .getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
        if (accounts.length == 0) {
            return null;
        }

        return accounts[0].name;
    }
    
    /**
     * return the Borqs account
     * 
     * @param context
     * @return null if no Borqs account
     */
    public static Account getBorqsAccount(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        for (Account account : accounts) {
            if (ConstData.BORQS_ACCOUNT_TYPE.equals(account.type)) {
                return account;
            }
        }
        return null;
    }

}
