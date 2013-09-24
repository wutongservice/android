/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.account.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.login.R;
import com.borqs.account.login.impl.AccountOperator;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.Utility;

public class AccountResetPwdActivity extends Activity implements OnClickListener{
    private String mUserName;
    private MakeNewPwdTask mNewPwdTask;
    
    public static void actionShow(Activity from, boolean canBack){
        Intent intent = new Intent(from, AccountResetPwdActivity.class);
        from.startActivity(intent);   
        from.overridePendingTransition(android.R.anim.fade_in, 0);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_get_new_password);
        
        initView();
    }

    private void initView(){
         // title
         //TextView title = (TextView)findViewById(R.id.acl_titlebar_tv);
         //title.setText(R.string.acl_reset_pwd_title);
         
         Button btnGetPwd = (Button)findViewById(R.id.acl_get_pwd_btn);
         btnGetPwd.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.acl_get_pwd_btn){
            startGetNewPwd();
        }
    }

    @Override
    protected void onDestroy() {
        if(mNewPwdTask != null && !mNewPwdTask.isCancelled()){
            mNewPwdTask.cancel(true);
        }
        super.onDestroy();
    }

    private void startGetNewPwd(){
        if (!validateValues()){
            Toast.makeText(this, 
                    R.string.acl_invalid_input_content, 
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        
        mNewPwdTask = new MakeNewPwdTask(this, mUserName);
        mNewPwdTask.execute();
    }
    
    private boolean validateValues(){
        TextView tvUserName = (TextView)findViewById(R.id.acl_rpd_user_id_tv);
        mUserName = String.valueOf(tvUserName.getText());
        if (!TextUtils.isEmpty(mUserName)){
            mUserName = mUserName.trim();
        }
        if(!Utility.isValidPhoneNumber(mUserName)){
            if(!Utility.isValidEmailAddress(mUserName)){
                tvUserName.setError(getString(R.string.acl_input_error_user_name));
                return false;
            }
        }
                
        return true;
    }
    
    public class MakeNewPwdTask extends AsyncTask<Void, Void, Void>{
        private AccountOperator mLogin;
        private Context mContext;
        private String mUserName;

        public MakeNewPwdTask(Context context, String name){
            mContext = context;
            mUserName = name;
            mLogin = new AccountOperator(context);
        }
                
        public void doCancel() {
            mLogin.cancel();
            this.cancel(true);
        }
        
        
        @Override
        protected Void doInBackground(Void... params) {
            mLogin.getNewPassword(mUserName);   
            return null;
        }

        @Override
        protected void onPreExecute() {
            AccountHelper.showProgressDialog(mContext, 
                     mContext.getString(R.string.acl_getting_new_password));
        }
        
        @Override
        protected void onPostExecute(Void data) {
            AccountHelper.closeProgressDialog();
            if(null != mLogin.getError()) {
                if (!mLogin.isSmsServerWorking()){                    
                    Toast.makeText(mContext, 
                                    R.string.acl_email_change_pwd_prompt, 
                                    Toast.LENGTH_LONG).show();
                } else {
                    AccountHelper.showInfoDialog(mContext, 
                                            getString(R.string.acl_error_get_new_pwd),
                                            mLogin.getError());
                }
            } else {
                int resId = R.string.acl_send_new_pwd_to_phone;
                if (mLogin.getResult("pwd_to").equals("mail")){
                    resId = R.string.acl_send_new_pwd_to_mail;
                } 
                Toast.makeText(mContext, getString(resId),
                              Toast.LENGTH_LONG)
                     .show();
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        }
    }
}
