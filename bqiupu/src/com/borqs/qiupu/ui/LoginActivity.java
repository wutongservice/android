package com.borqs.qiupu.ui;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.borqs.account.service.AccountServiceConfig;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.account.service.ui.BorqsAccountConfig;
import com.borqs.account.service.ui.RegisterActivity;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.adapter.UserMailListAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.UserAccountObserver;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.util.ArrayList;

public class LoginActivity extends BasicActivity.StatActivity implements TextWatcher{
	private static final String TAG = "Qiupu.LoginActivity";
    private AutoCompleteTextView login_username_edt;
    private EditText login_pwd_edt;
    private TextView   login_ok_btn;
    private TextView sign_up_txv;
    private TextView get_password;
    private ImageView mImageView;
    private ListView mListView;
    private TextView Casual_look;
    private TextView hot_app;
    
    private AsyncBorqsAccount asyncBorqsAccount;
    
    private Handler mHandler;
    private BorqsAccountORM orm;    
    
    private ArrayAdapter<String> adapter;  
    private ArrayList<String> UserMail = new ArrayList<String>();
    private UserMailListAdapter mUserMailListAdapter;
    
    public static final String BUNDLE_REQUEST_LOGIN_ACTIVITY = "borqs.request.login";
    public static final String BUNDLE_REQUEST_LOGIN_OK = "borqs.request.login.ok";
    private String mRequestActivity;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qiupu_login);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

		//start qiupu service to make sure the system ready
		Intent loginIntent = new Intent();
		loginIntent.setClassName(getPackageName(), "com.borqs.qiupu.service.QiupuService");
		startService(loginIntent);
		
		asyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(),null,null);
		orm = BorqsAccountORM.getInstance(getApplicationContext());
		
		login_username_edt = (AutoCompleteTextView)findViewById(R.id.login_username_edt);
		login_pwd_edt      = (EditText)findViewById(R.id.login_pwd_edt);
		
		String username = getIntent().getStringExtra("username");
		String pwd = getIntent().getStringExtra("pwd");
		if(StringUtil.isValidString(username))
		{
			login_username_edt.setText(username);
			login_pwd_edt.setText(pwd);
		}
		else
		{
			initTextView();
		}
		
		login_ok_btn       = (TextView)findViewById(R.id.login_ok_btn);
		sign_up_txv        = (TextView)findViewById(R.id.sign_up_txv);
		get_password       = (TextView) findViewById(R.id.get_password);
		mImageView         = (ImageView) findViewById(R.id.login_list_mail);
		mListView          = (ListView)findViewById(R.id.login_mail_content);
		Casual_look        = (TextView)findViewById(R.id.Casual_look);
		Casual_look.setOnClickListener(showpublicLineClick);
		
		hot_app            = (TextView)findViewById(R.id.hot_app);
		
		mUserMailListAdapter = new UserMailListAdapter(this);
		mListView.setAdapter(mUserMailListAdapter);
		
		login_username_edt.addTextChangedListener(this);
		
		login_ok_btn.setOnClickListener(loginListener);
		sign_up_txv.setOnClickListener(signUpListener);
		mImageView.setOnClickListener(new ListMailBtnClickListener());
		mListView.setOnItemClickListener(new ListMailItemClickListener());
		get_password.setOnClickListener(getpasswordListener);
		mHandler = new LoginHandler();
		Intent intent = getIntent();
		mRequestActivity = intent.getStringExtra(BUNDLE_REQUEST_LOGIN_ACTIVITY);
		if(BorqsAccountConfig.LOGD)Log.d(TAG, "onCreate mRequestActivity:"+mRequestActivity);
		getGmailAccount();
		
		if(isEmpty(username) == false && isEmpty(pwd) == false)
		{
			doLogin();
		}
	}
	
	private boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}
	
	private void initTextView() {
		String accountEmail = orm.getSettingValue("last_login_user");
		if(accountEmail != null)
		{
			login_username_edt.setText(accountEmail);
			UserMail.add(accountEmail);
		}
	}
	
	View.OnClickListener showpublicLineClick = new View.OnClickListener() {		
		public void onClick(View arg0) {
            BpcApiUtils.startStreamActivityWithStreamType(getApplicationContext(), BpcApiUtils.ALL_TYPE_POSTS);
		}
	};
	
	private void doLogin()
	{
        if (!QiupuService.verifyAccountLogin(getApplication())) {
            Log.e(TAG, "doLogin exit while account service is not ready.");
            return;
        }

		String username = login_username_edt.getText().toString();
		String pwd      = login_pwd_edt.getText().toString();
		if(!StringUtil.isEmpty(username) && !StringUtil.isEmpty(pwd))
        {
			login_ok_btn.setEnabled(false);
			//remember username to db
	        Message message = mHandler.obtainMessage(VERIFY_USER);
	        message.getData().putString(USERNAME_KEY, username);
	        message.getData().putString(PWD_KEY, pwd);
	        message.sendToTarget();
        }
		else if(StringUtil.isEmpty(username))
		{
			Toast.makeText(LoginActivity.this, getString(R.string.qiupu_login_input_username), Toast.LENGTH_SHORT);
			return ;
		}
		else if(StringUtil.isEmpty(pwd)){
			Toast.makeText(LoginActivity.this, getString(R.string.qiupu_login_input_password), Toast.LENGTH_SHORT);
			return ;
		}
		
	}
	
	View.OnClickListener loginListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			doLogin();			
		}
	};
	
	View.OnClickListener signUpListener = new View.OnClickListener() {
		public void onClick(View v) {
			  Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
			  Bundle bundle = new Bundle();
			  bundle.putStringArrayList("UserMail", UserMail);
			  intent.putExtras(bundle);
			  intent.putExtra(BUNDLE_REQUEST_LOGIN_ACTIVITY, mRequestActivity);
	          startActivity(intent);
	          LoginActivity.this.finish();
		}
	};
	
	private void getPassword(){
		String str = login_username_edt.getText().toString().trim();
		if(str !=null && str.length() > 0){
			asyncBorqsAccount.getBorqsUserPassword(str, new TwitterAdapter(){
                
				public void getBorqsUserPassword(boolean result) {
                	if(result){
                		Log.d(TAG, "result :"+ result);
                		Message msg = mHandler.obtainMessage(GET_PASSWORD_OK);
                		msg.sendToTarget();
                	}
				}
                public void onException(TwitterException ex,TwitterMethod method) {
                	Log.d(TAG, "getpassword exception "+ex.getMessage()+"==method="+method.name());
					Message msg = mHandler.obtainMessage(GET_PASSWORD_FAILED);
					msg.sendToTarget();
                }
	    	    
	    	});
		}
	}
	
	View.OnClickListener getpasswordListener = new View.OnClickListener() {
		public void onClick(View v) {
			String str = login_username_edt.getText().toString().trim();
			if(str != null && str.length()>0){
				showDialog(DIALOG_GETPASSWORD);
			}
			else{
				Toast.makeText(LoginActivity.this, R.string.toast_input_email, Toast.LENGTH_LONG).show();
			}
		}
	};
	
	protected void onPause() {
		super.onPause();
	}

	
	protected void onResume() {
		super.onResume();
	}
	
	
	protected void onDestroy() {
		super.onDestroy();		
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{         
        	Log.d(TAG, "KEYCODE_BACK coming");     
	    }
		return super.onKeyDown(keyCode, event);
		
	}
	
	private final String USERNAME_KEY          = "username";
	private final String PWD_KEY               = "pwd";
	private final String IS_LOGIN_SUCCESS_KEY  = "is_login_success";
	private final String ERROR_CODE_KEY        = "error_code";

	private final int VERIFY_USER         = 1;
    private final int VERIFY_USER_END     = 2;
    private final int GET_PASSWORD_OK     = 3;
    private final int GET_PASSWORD_FAILED = 4;
	private class LoginHandler extends Handler{
	    public void handleMessage(Message msg) {
	    	switch(msg.what){
	    	    case VERIFY_USER:{
	    	    	showDialog(DIALOG_LOGIN);
	    	    	final String username = msg.getData().getString(USERNAME_KEY);
	    	    	final String pwd      = msg.getData().getString(PWD_KEY);
	    	    	asyncBorqsAccount.loginBorqsAccount(username, pwd, new TwitterAdapter(){
                        @Override
						public void loginBorqsAccount(BorqsUserSession loginBorqs) {
							if(loginBorqs != null){
								//login successfully
								BorqsAccount account = new BorqsAccount();
								account.sessionid = loginBorqs.sessionid;
								account.uid = loginBorqs.uid;
								account.username = loginBorqs.username;
								account.createtime = loginBorqs.createtime;
								account.modifytime = loginBorqs.modifytime;
								account.nickname = loginBorqs.nickname;
								account.screenname = loginBorqs.screenname;
								account.verify = loginBorqs.verify;
								Message msg = mHandler.obtainMessage(VERIFY_USER_END);
								try{
									orm.updateAccount(account);
									AccountServiceUtils.login(account);
//									try{
//										IBorqsAccountService mAccountService = ((QiupuApplication)getApplication()).getBorqsAccountService();
//								    	if(mAccountService != null) {
//								    		mAccountService.login(account);
//								    	}
//									}catch(Exception ne){}
									
									//send login broadcase
									Intent loginIntent = new Intent("ACTION_BORQS_LOGIN_SUCCESS");
									loginIntent.putExtra("Account", account);
									loginIntent.setClassName(getPackageName(), "com.borqs.qiupu.service.QiupuService");
									LoginActivity.this.startService(loginIntent);							    	
									Log.d(TAG, "account:"+account);
		                            msg.getData().putBoolean(IS_LOGIN_SUCCESS_KEY, true);
		                            startCollectPhoneInfo();
								}catch(Exception e){
									Log.d(TAG, "e!!!!!!!!!!!!!"+e);
									msg.getData().putBoolean(IS_LOGIN_SUCCESS_KEY, false);
								}
								 
	                            msg.sendToTarget();
	                            
							}
						}

						@Override
                        public void onException(TwitterException ex,TwitterMethod method) {
                            Log.d(TAG, "login exception "+ex.getMessage()+"==method="+method.name());
                            Message msg = mHandler.obtainMessage(VERIFY_USER_END);
                            msg.getData().putBoolean(IS_LOGIN_SUCCESS_KEY, false);
                            int error_code = ex.getStatusCode();
                            msg.getData().putInt(ERROR_CODE_KEY, error_code);
                            msg.sendToTarget();
                        }
	    	    	    
	    	    	});
	    		    break;
	    	    }
	    	    case VERIFY_USER_END:{
	    	    	dismissDialog(DIALOG_LOGIN);
	    	    	boolean is_login_success = msg.getData().getBoolean(IS_LOGIN_SUCCESS_KEY);
	    	    	Log.d(TAG,"verify user end login success:"+is_login_success);
	    	    	
	    	    	login_ok_btn.setEnabled(true);
	    	    	if(is_login_success)
	    	    	{
	    	    		//LoginActivity.this.finish();	
	    	    		String last_login_name = orm.getSettingValue(BorqsAccountORM.LAST_LOGIN_USER);
	    	    		String current_login_name = orm.getAccount().username;    
	    	    		Log.d(TAG, "current_login_name :"+ current_login_name);
	    	    		if(last_login_name == null || (!StringUtil.isEmpty(current_login_name))){
	    	    			orm.setLastLoginUser(orm.getAccount().username);
	    	    		}
	    	    		
	    	    		Log.d(TAG, "===== login ok, start activity:"+mRequestActivity);
	    	    		UserAccountObserver.login(); 
	    	    		
	    	    		if(mRequestActivity != null) {
	    	    		    Intent intent = new Intent(mRequestActivity);
	    	    		    intent.putExtra(BUNDLE_REQUEST_LOGIN_OK, true);
	    	    		    setResult(Activity.RESULT_OK);
	    	    		    LoginActivity.this.startActivity(intent);
	    	    		}
	    	    		LoginActivity.this.finish();
	    	    	}
	    	    	else
	    	    	{
	    	    		int error_code = msg.getData().getInt(ERROR_CODE_KEY);
	    	    		if(error_code == ErrorResponse.NOT_VERIFY)
	    	    		{
	    	    			//got to verify 
	    	    			Intent intent = new Intent("com.borqs.account.service.Register");
	    	    			intent.putExtra("step", 2);
	    	    			String username = login_username_edt.getText().toString();
	    	    			String pwd      = login_pwd_edt.getText().toString();
	    	    			intent.putExtra("username",username);
	    	    			intent.putExtra("password", pwd);
	    	    			startActivity(intent);    	    			
	    	    		}
	    	    		else
	    	    		{
		    	    		Toast.makeText(LoginActivity.this, getString(R.string.login_error_msg), Toast.LENGTH_SHORT).show();
	    	    		}
	    	    	}
	    	    	break;
	    	    }
	    	    case GET_PASSWORD_OK:
	    	    {
	    	    	Toast.makeText(LoginActivity.this, R.string.toast_get_password_success, Toast.LENGTH_LONG).show();
	    	    	break;
	    	    }
	    	    case GET_PASSWORD_FAILED:
	    	    {
	    	    	Toast.makeText(LoginActivity.this, R.string.toast_get_password_failed, Toast.LENGTH_LONG).show();
	    	    	break;
	    	    }
	    	    
	    	}
	    }
		
	}
	
	private class ListMailBtnClickListener implements OnClickListener{

		public void onClick(View arg0) {
			
			if(mListView.getVisibility() == View.GONE){
				mListView.setVisibility(View.VISIBLE);
			}else{
				mListView.setVisibility(View.GONE);
			}
			if(UserMail.size() > 0){
				mUserMailListAdapter.setItems(UserMail);
				mUserMailListAdapter.notifyDataSetChanged();
			}
		}
	}

	private class ListMailItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			login_username_edt.setText(UserMail.get(position).toString());
			mListView.setVisibility(View.GONE);
			
		}
	}
	private void getGmailAccount(){
		
		try{
	        Cursor cursor = this.getContentResolver()
	            .query(Uri.parse("content://com.android.email.provider/account"),
	                    new String[] {"emailAddress"}, null, null, null);
	
	        if (null!=cursor && cursor.moveToFirst()) {
	            for (int i=0; i<cursor.getCount(); i++) {
	                String addr =
	                    cursor.getString(cursor.getColumnIndexOrThrow("emailAddress"));
	                UserMail.add(addr);
	                cursor.moveToNext();
	            }
	        }
		}catch(Exception ne){}
	}
	
	public static final int RESULT_LOGIN = 1001;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mListView.getVisibility()==View.VISIBLE){
			mListView.setVisibility(View.GONE);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
//		setResult(RESULT_EXIT);
		finish();
	}
	
	protected void startCollectPhoneInfo(){
        IntentUtil.shootCollectPhoneInfoIntent(getApplicationContext());
	}
	
	private static final int DIALOG_GETPASSWORD = 101;
	private static final int DIALOG_LOGIN       = 102;
	
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DIALOG_GETPASSWORD:
	        {
	            return getpasswordDialog();
	        }
	        case DIALOG_LOGIN:
	        {
	        	ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setMessage(getString(R.string.processbar_login_in));
	            dialog.setCanceledOnTouchOutside(false);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	        }
        }
        return null;
    }
	
	private Dialog getpasswordDialog(){
    	return new AlertDialog.Builder(LoginActivity.this)
         .setTitle(R.string.dialog_get_password_toast)
         .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
            	 getPassword();
             }
         })
         .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             }
         })
         .create();
    }

	public void afterTextChanged(Editable arg0) {
	} 

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}
	
	private String[] mailSuffix = new String[AccountServiceConfig.mailSuf.length];
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		String str = login_username_edt.getText().toString();
		int len = str.length();
		int size = AccountServiceConfig.mailSuf.length;
		if(str.indexOf("@")==str.lastIndexOf("@")){
			if(len > 0 && str.charAt(len - 1)=='@'){
				login_username_edt.setThreshold(len + 1);
				for(int i = 0;i <size ; i++){
					mailSuffix[i] = str + AccountServiceConfig.mailSuf[i];
				}
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,mailSuffix);
				login_username_edt.setAdapter(adapter);
			}
		}
	}
}
