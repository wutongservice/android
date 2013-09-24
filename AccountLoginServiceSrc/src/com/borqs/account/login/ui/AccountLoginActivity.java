package com.borqs.account.login.ui;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.login.R;
import com.borqs.account.login.impl.AccountOperator;
import com.borqs.account.login.intf.DeviceFactory;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.Utility;

public class AccountLoginActivity extends AccountAuthenticatorActivity implements OnClickListener{
    private static final String ACTION_FINISH = "action_finish";
    private static final String ACTION_FINISH_P_RESULT = "action_finish_result";
    private static final String ACTION_FINISH_P_BY_REGISTER = "action_finish_by_register";
    
    private String mUserName;
    private String mPassword;
    private boolean mRelogin;
    private AccountUITheme mTheme;
    private LoginTask mLoginTask;
    private LoginTask mRegisterTask;
    
    public static void actionFinished(Activity from, boolean loginFinished, boolean loginAsNew){
        Intent intent = new Intent(from, AccountLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ACTION_FINISH, true);
        intent.putExtra(ACTION_FINISH_P_BY_REGISTER, loginAsNew);
        intent.putExtra(ACTION_FINISH_P_RESULT, loginFinished);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                from.getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.acl_slide_in_left, R.anim.acl_slide_out_right);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.account_login_activity);
        if (handleIntent(getIntent())){
            finish();
            return;
        }        
        
        mTheme = AccountUITheme.getTheme(this);
        initView();
    }

    private void setResponseResult(boolean result, boolean byRegister){
        if(result){
            Bundle data = new Bundle();
            data.putBoolean(ConstData.REGISTER_RESPONSE_RESULT, byRegister);
            setAccountAuthenticatorResult(data);
        }
    }
    
    private boolean handleIntent(Intent it){
        if (it == null){
            return false;
        }
        
        boolean res = false;
        if (it.getBooleanExtra(ACTION_FINISH, false)){
            boolean result = it.getBooleanExtra(ACTION_FINISH_P_RESULT, false);
            BLog.d("login result:" + result);
            if (result){
                boolean byRegister = it.getBooleanExtra(ACTION_FINISH_P_BY_REGISTER, false);
                BLog.d("login register:" + byRegister);
                setResponseResult(result, byRegister);
                res = true;
            }
        }
        
        if (!res){
            mUserName = it.getStringExtra(ConstData.ACCOUNT_USER_ID);
            if (!TextUtils.isEmpty(mUserName)){
                mRelogin = it.getBooleanExtra(ConstData.OPTIONS_RELOGIN, false);
            }
            
            int features = it.getIntExtra(ConstData.LOGIN_REGISTER_FEATURE, 0);
            BLog.d("login feature:" + features);
            AccountUITheme.create(this, features);            
        }
        
        return res;
    }

    private void initView(){
        
        // login button
        Button btnSign = (Button)findViewById(R.id.acl_signin_btn);
        btnSign.setOnClickListener(this);
        
        // register button & declare textview
        TextView btnRegister = (TextView)findViewById(R.id.acl_register_btn);
        int registerStringId = R.string.acl_register_text;
       
        
        //TextView tvDeclare = (TextView)findViewById(R.id.acl_sms_charge_declare_tv);
        TextView tvUserName = (TextView)findViewById(R.id.acl_user_id_tv);  
        if (mRelogin){
            //not show reg&declare content
            btnRegister.setVisibility(View.GONE);
            //tvDeclare.setVisibility(View.GONE);            
            
            tvUserName.setText(mUserName);
            tvUserName.setEnabled(false);
        } /*else if (isSupportOnekeyRegister()){
            registerStringId = R.string.acl_onekey_register_text;
            //tvDeclare.setVisibility(View.VISIBLE);
            //tvDeclare.setText(Html.fromHtml(getString(R.string.acl_sms_charge_declare)));
        } */else {
            if (isLimitEmailRegister()){                  
            	mTheme.changeTheme(ConstData.FEATURE_SUPPORT_EMAIL);
            }

            registerStringId = R.string.acl_register_text;
            //tvDeclare.setVisibility(View.GONE);
        }
        showLink(btnRegister,new URLSpan(""){
            @Override
            public void onClick(View widget) {
                startRegister();
            }
        }, registerStringId);
        
        if (!mRelogin){
        	tvUserName.setHint(mTheme.getLoginUserNameHint());
        	tvUserName.setInputType(mTheme.getLoginUserInputType());
        }


        // forgot button
        TextView tvForgotPwd = (TextView)findViewById(R.id.acl_forgot_password_tv);
        showLink(tvForgotPwd,new URLSpan(""){
                @Override
                public void onClick(View widget) {
                    AccountResetPwdActivity.actionShow(AccountLoginActivity.this, true);
                }
        },R.string.acl_user_forgot_password);
    }

    private void showLink(TextView view, URLSpan span, int textId){
        String strPrompt = getString(textId);
        SpannableString sp = new SpannableString(strPrompt);
        sp.setSpan(span,0, strPrompt.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        view.setText(sp);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onClick(View v) {
        if (v.getId() == R.id.acl_signin_btn){
            startLogin();
        }
    }

    @Override
    protected void onDestroy() {
        if(mRegisterTask!=null && !mRegisterTask.isCancelled()){
            mRegisterTask.cancel(true);
        }
        if(mLoginTask!=null && !mLoginTask.isCancelled()){
            mLoginTask.cancel(true);
        }
        super.onDestroy();
    }

    private boolean hasSimcardIn(){
        String imsi = DeviceFactory.getDefaultDevice(this).getImsi();
        return !TextUtils.isEmpty(imsi);
    }
    
    private boolean hasChinaSimcardIn(){
        boolean res = false;
        String imsi = DeviceFactory.getDefaultDevice(this).getImsi();
        if (!TextUtils.isEmpty(imsi)){
            if (imsi.startsWith("460")){
                res = true;
            }
        }
        return res;
    }
    
    private boolean isSupportOnekeyRegister(){
        return hasChinaSimcardIn()
                && AccountUITheme.getTheme(this).hasOnekeyRegFeature();
    }
    
    private boolean isLimitEmailRegister(){
        // international card, only enable email register
        return hasSimcardIn() && !hasChinaSimcardIn();
    }
    
    private void startLogin(){
         if (!validateValues()){
             Toast.makeText(this, 
                           R.string.acl_invalid_input_content, 
                           Toast.LENGTH_SHORT)
                           .show();
             return;
         }
        
         ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
         
         mLoginTask = new LoginTask(this, mUserName, mPassword);
         mLoginTask.execute();
    }
    
    private boolean validateValues(){
    	if (!mRelogin){
	        TextView tvUserName = (TextView)findViewById(R.id.acl_user_id_tv);
	        mUserName = String.valueOf(tvUserName.getText());
	        if (!TextUtils.isEmpty(mUserName)){
	           mUserName = mUserName.trim();
	        }
	        if(!mTheme.validateLoginName(mUserName)){
	            tvUserName.setError(getString(R.string.acl_input_error_user_name));
	            return false;
	        }
    	}
        
        TextView tvPassword = (TextView)findViewById(R.id.acl_passwd_tv);
        mPassword = String.valueOf(tvPassword.getText());
        if(!Utility.isValidPassword(mPassword)){
            tvPassword.setError(getString(R.string.acl_error_input_pwd));
            return false;
        }
        
        return true;
    }
    
    private void startRegister(){
        if (isSupportOnekeyRegister()){
            // one key register
            mRegisterTask = new LoginTask(this);
            mRegisterTask.execute();
        } else {
            // verify code register
            AccountRegisterActivity.actionShow(this, true);
        }
    }
    
    public class LoginTask extends AsyncTask<Void, Void, Void>{
        private AccountOperator mLogin;
        private Context mContext;
        private String mUserName;
        private String mPassword;

        public LoginTask(Context context, String name, String pwd){
            mContext = context;
            mUserName = name;
            mPassword = pwd;
            mLogin = new AccountOperator(context);
        }
        
        public LoginTask(Context context){
            this(context, null, null);
        }
        
        public void doCancel() {
            mLogin.cancel();
            this.cancel(true);
        }
        
        private boolean isRegister(){
            return TextUtils.isEmpty(mUserName);
        }
        
        private void handleSmsServerError(){
            // register failed, if sms server crushed and support email register feature
            // prompt user register by email, else prompt an error
            // same action used in register activity and change pwd activity
            if (isRegister()){
                if (AccountUITheme.getTheme(mContext).hasEmailRegFeature()){
                    Toast.makeText(mContext, R.string.acl_email_register_prompt, 
                                   Toast.LENGTH_LONG).show();
                    AccountRegisterActivity.actionShow(AccountLoginActivity.this, true);
                } else {
                    AccountHelper.showInfoDialog(mContext, 
                            getString(R.string.acl_login_failed),
                            getString(R.string.acl_sms_server_error));
                }
            }
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            if (isRegister()){
                mLogin.doFastLogin();                
            } else {
                mLogin.doNormalLogin(mUserName, mPassword);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (isRegister()){
                AccountHelper.showProgressDialog(mContext, 
                        mContext.getString(R.string.acl_registering));
            } else {
                AccountHelper.showProgressDialog(mContext, 
                                mContext.getString(R.string.acl_logining));
            }
        }
        
        @Override
        protected void onPostExecute(Void data) {
            AccountHelper.closeProgressDialog();
            if(null != mLogin.getError()) {
                if (!mLogin.isSmsServerWorking()){
                    handleSmsServerError();
                } else {
                    AccountHelper.showInfoDialog(mContext, 
                                            getString(R.string.acl_login_failed),
                                            mLogin.getError());
                }
            } else { 
                if (isRegister() && !mLogin.isNewCreated()){
                    // register but already have account with the current phone
                    showHaveAccountPrompt(mContext);
                    
                }
                setResponseResult(true, mLogin.isNewCreated());
                finish();
            }
        }
    }
    
    public static void showHaveAccountPrompt(Context ctx){
        Toast.makeText(ctx, 
                R.string.acl_have_account_prompt, 
                Toast.LENGTH_LONG)
                .show();
    }
}

