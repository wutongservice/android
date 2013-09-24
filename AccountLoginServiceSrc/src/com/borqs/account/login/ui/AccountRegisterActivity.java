package com.borqs.account.login.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.login.R;
import com.borqs.account.login.impl.AccountOperator;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.SimpleTask;
import com.borqs.account.login.util.Utility;

public class AccountRegisterActivity extends Activity implements OnClickListener{
    private static final String INTENT_P_CAN_BACK = "can_back";
    
    private AccountUITheme mTheme;
    private String mUserName;
    private SimpleTask mVerifyCodeTask;
    private SimpleTask mRegisterTask;
//    private String mPassword;

    public static void actionShow(Activity from, boolean canBack){
        Intent intent = new Intent(from, AccountRegisterActivity.class);
        intent.putExtra(INTENT_P_CAN_BACK, canBack);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                from.getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.acl_slide_in_right, R.anim.acl_slide_out_left);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.account_register_activity);
        mTheme = AccountUITheme.getTheme(this);
        initView();       
    }
    
    private void initView(){
        // title
        //((TextView)findViewById(R.id.acl_titlebar_tv)).setText(R.string.acl_register_title);
        //findViewById(R.id.acl_titlebar_back_img).setOnClickListener(this);
        
        //back
        findViewById(R.id.acl_backto_login).setOnClickListener(this);
        
        // user name hint
        EditText edtUser = ((EditText)findViewById(R.id.acl_register_user_id_tv));
        edtUser.setHint(mTheme.getRegisterUserNameHint());
        edtUser.setInputType(mTheme.getRegisterUserInputType());
        
        // button: get verify code
        Button btn = (Button)findViewById(R.id.acl_get_verifycode_btn);
        btn.setOnClickListener(this);
        
        // verify code input
        ((EditText)findViewById(R.id.acl_register_verifycode_tv))
                            .addTextChangedListener(new onTextChangeListener());
    }

    @Override
    public void onBackPressed() {
        if(!getIntent().getBooleanExtra(INTENT_P_CAN_BACK, false)){
            AccountLoginActivity.actionFinished(this, false, false);
        } 
        finish();
        overridePendingTransition(R.anim.acl_slide_in_left, R.anim.acl_slide_out_right);
    }

    public void onClick(View v) {
        /*if (v.getId() == R.id.acl_titlebar_back_img){
            onBackPressed();
        } else*/ if (v.getId() == R.id.acl_get_verifycode_btn){
            getVerifyCode();
        } else if (v.getId() == R.id.acl_backto_login){
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if(mRegisterTask != null && !mRegisterTask.isCancelled()){
            mRegisterTask.cancel(true);
        }
        if(mVerifyCodeTask != null && !mVerifyCodeTask.isCancelled()){
            mVerifyCodeTask.cancel(true);
        }
        super.onDestroy();
    }

    private void getVerifyCode(){
        if (!validateValues()){
            Toast.makeText(this, 
                          R.string.acl_invalid_input_content, 
                          Toast.LENGTH_SHORT)
                          .show();
            return;
        }
        
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        doGetVerifyCode(mUserName);        
    }
    
    
    private void doGetVerifyCode(final String userName){
        mVerifyCodeTask = new SimpleTask();
        final AccountOperator accountOp = new AccountOperator(getApplicationContext());
        
        AccountHelper.showProgressDialog(this, getString(R.string.acl_get_verify_code));
        
        Runnable r1 = new Runnable(){
                @Override
                public void run() {
                    mVerifyCodeTask.setBresult(accountOp.getVerifyCode(userName,
                                                    getString(R.string.acl_verifycode_format)));                
                }
            };
            
        Runnable r2 = new Runnable(){
                @Override
                public void run() {
                    AccountHelper.closeProgressDialog();
                    if(!mVerifyCodeTask.getBresult()){
                        if (!accountOp.isSmsServerWorking()){
                            if (AccountUITheme.getTheme(AccountRegisterActivity.this)
                                               .hasEmailRegFeature()){
                                Toast.makeText(getApplicationContext(), 
                                                R.string.acl_email_register_prompt, 
                                                Toast.LENGTH_LONG).show();
                            } else {
                                AccountHelper.showInfoDialog(AccountRegisterActivity.this, 
                                        getString(R.string.acl_get_verifycode_failed),
                                        getString(R.string.acl_sms_server_error));
                            }
                        } else {
                            AccountHelper.showInfoDialog(AccountRegisterActivity.this, 
                                                    getString(R.string.acl_get_verifycode_failed),                                                    
                                                    accountOp.getError());
                        }
                    }  else{
                        int resId = R.string.acl_send_verify_code_to_phone;
                        if (accountOp.getResult("verify_code_to").equals("mail")){
                            resId = R.string.acl_send_verify_code_to_mail;
                        }
                        Toast.makeText(getApplicationContext(), 
                                        getString(resId),
                                       Toast.LENGTH_LONG)
                                       .show();
                    }
                }
            };
            
            mVerifyCodeTask.execute(r1,r2);
    }
    
    private void startRegister(String verifyCode){
        if (!validateValues()){
            Toast.makeText(this, 
                          R.string.acl_invalid_input_content, 
                          Toast.LENGTH_SHORT)
                          .show();
            return;
        }
        
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        doRegister(mUserName, verifyCode);
    }
    
    private boolean validateValues(){
        TextView tvUserName = (TextView)findViewById(R.id.acl_register_user_id_tv);
        mUserName = String.valueOf(tvUserName.getText());
        if (!TextUtils.isEmpty(mUserName)){
            mUserName = mUserName.trim();
        }
        if(!mTheme.validateRegisterName(mUserName)){            
            tvUserName.setError(getString(R.string.acl_input_error_user_name));
            return false;
        }
        
//        TextView tvPassword = (TextView)findViewById(R.id.acl_register_passwd_tv);
//        mPassword = String.valueOf(tvPassword.getText());
//        if(!Utility.isValidPassword(mPassword)){
//            tvPassword.setError(getString(R.string.acl_error_input_pwd));
//            return false;
//        }
        
        return true;
    }
    
    private void doRegister(final String userName, final String code)
    {
        AccountHelper.showProgressDialog(this, getString(R.string.acl_registering));
        final AccountOperator accountOp = new AccountOperator(getApplicationContext());
        mRegisterTask = new SimpleTask();
        
        final Runnable r1 = new Runnable(){
                        @Override
                        public void run() {
                            mRegisterTask.setBresult(accountOp.doVerifyCodeLogin(userName, code));
                        }
                    };
        
        final Runnable r2 = new Runnable(){
                    @Override
                    public void run() {
                        AccountHelper.closeProgressDialog();
                        if(!mRegisterTask.getBresult()) {
                            AccountHelper.showInfoDialog(AccountRegisterActivity.this,
                                    getString(R.string.acl_register_failed), accountOp.getError());
                        } else {          
                            if (!accountOp.isNewCreated()){
                                AccountLoginActivity.showHaveAccountPrompt(AccountRegisterActivity.this);
                            }
                            AccountLoginActivity.actionFinished(AccountRegisterActivity.this, 
                                                                true, 
                                                                accountOp.isNewCreated());
                        }
                    }
                 }; 
        
                 mRegisterTask.execute(r1, r2);
    }
    
    private class onTextChangeListener implements TextWatcher{
        @Override
        public void afterTextChanged(Editable s){
            if (s != null){
                if (s.toString().length() == 4){
                    // verify code limit to 4 digits
                    startRegister(s.toString());
                }
            }
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){            
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count){            
        }
    }
}
