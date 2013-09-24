package com.borqs.account.service.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import com.borqs.account.service.AccountServiceConfig;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.adapter.UserMailListAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.LoginActivity;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.util.ArrayList;

public class RegisterActivity extends BasicActivity.StatActivity implements TextWatcher {
	 private static final String TAG = "Qiupu.RegisterActivity";
	 protected BorqsAccountORM orm;    
	 private AutoCompleteTextView register_username_edt;
	 private EditText register_pwd_edt;
	 private EditText register_nickname_edt;
	 private EditText register_phonenumber_edt; 
	 private EditText register_verify_edt;
	
	 private TextView username_error_msg_txt;
	 private TextView nickname_error_msg_txt;
	 private TextView pwd_error_msg_txt;
	 private TextView phonenumber_error_msg_txt;
	 private TextView verify_error_msg_txt;
	
	 private TextView username_txv;
	 private TextView pwd_txv;
	 private TextView nickname_txv;
	 private TextView phonenumber_txv;
	 private TextView verify_txv;
	 
	 private ImageView mImageView;
	 private ListView  mListView;
	 
	 private ScrollView mScrollView;
	 
	 private CheckBox mCheckBox;

	 private Button   register_ok_btn;
	 private Button   register_cancel_btn;
	 private Handler     mHandler;
	 
	 private RadioButton manRadioButton;
	 private RadioButton womanRadioButton;
     
	 private int step = 1;
	 private Toast mToast;
     
	 private AsyncBorqsAccount asyncBorqsAccount;
	 
	 private ArrayList<String> mUserMail = new ArrayList<String>();
	 private ArrayAdapter<String> adapter;
	 private UserMailListAdapter mUserMailListAdapter;
	 
	 private String mRequestActivity;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		mUserMail = getIntent().getExtras().getStringArrayList("UserMail");
		
		register_username_edt = (AutoCompleteTextView)findViewById(R.id.register_username_edt);
		register_username_edt.setThreshold(1);
	    register_username_edt.addTextChangedListener(this);
	    
		register_pwd_edt      = (EditText)findViewById(R.id.register_pwd_edt);
		register_nickname_edt = (EditText)findViewById(R.id.register_nickname_edt);
		register_verify_edt = (EditText)findViewById(R.id.register_verify_edt);
		register_phonenumber_edt = (EditText)findViewById(R.id.register_phonenumber_edt);
		TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		if(tm.getSimState() == TelephonyManager.SIM_STATE_READY){
			register_phonenumber_edt.setText(tm.getLine1Number());
		}
		
		username_error_msg_txt = (TextView)findViewById(R.id.username_error_msg_txt);
		nickname_error_msg_txt = (TextView)findViewById(R.id.nickname_error_msg_txt);
	    pwd_error_msg_txt = (TextView)findViewById(R.id.pwd_error_msg_txt);
		phonenumber_error_msg_txt = (TextView)findViewById(R.id.phonenumber_error_msg_txt);
	    verify_error_msg_txt = (TextView)findViewById(R.id.verify_error_msg_txt);
	    username_txv = (TextView)findViewById(R.id.username_txv);
	    
	    manRadioButton   = (RadioButton) findViewById(R.id.sex_man);
	    womanRadioButton = (RadioButton) findViewById(R.id.sex_woman);
	    
	    mImageView = (ImageView) findViewById(R.id.login_list_mail);
	    mImageView.setOnClickListener(new ListMailBtnClickListener());
	    mListView  = (ListView)findViewById(R.id.register_mail_content);
	    mUserMailListAdapter = new UserMailListAdapter(this);
		mListView.setAdapter(mUserMailListAdapter);
		mListView.setOnItemClickListener(new ListMailItemClickListener());
	    
		mScrollView = (ScrollView) findViewById(R.id.register_scroolview);
		mScrollView.setOnTouchListener(new ScroolViewTouichListener());
		
		mCheckBox = (CheckBox) findViewById(R.id.show_password);
		mCheckBox.setOnCheckedChangeListener(new CheckBoxCheckedChangeListener());
		
	    pwd_txv = (TextView)findViewById(R.id.pwd_txv);
	    phonenumber_txv = (TextView)findViewById(R.id.phonenumber_txv);
	    nickname_txv = (TextView)findViewById(R.id.nickname_txv);
	    verify_txv = (TextView)findViewById(R.id.verify_txv);
	    
		register_ok_btn       = (Button)findViewById(R.id.register_ok_btn);
		register_cancel_btn   = (Button)findViewById(R.id.register_cancel_btn);
	    mHandler = new RegisterHandler();
	    mToast = Toast.makeText(this.getApplicationContext(), "", Toast.LENGTH_SHORT);
	    hideVerifyView();
	    
	    orm = BorqsAccountORM.getInstance(getApplicationContext());
	    asyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(),null,null);
//	    UserAccountObserver.registerAccountListener(getClass().getName(), this);
	    
		register_cancel_btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(step == 2){
					//skip verify
					String last_login_name = orm.getSettingValue(BorqsAccountORM.LAST_LOGIN_USER);
    	    		String current_login_name = orm.getAccount().username;
    	    		if(last_login_name!=null && current_login_name!=null && !last_login_name.equals(current_login_name)){
    	    			orm.setLastLoginUser(orm.getAccount().username);
    	    		}
    	    		
//				    UserAccountObserver.login();
				}else{
					RegisterActivity.this.finish();
				}
			}
		});
		
		register_ok_btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
//				register_ok_btn.setEnabled(false);
//				register_cancel_btn.setEnabled(false);
				
				if(step == 2){
					verifyAccount();
					verify_error_msg_txt.setVisibility(View.GONE);
				}
				else
				{
					registerAccount();
				}
			}	
		});
		
		Intent intent = getIntent();
		initViewByIntent(intent);
		
		mRequestActivity = intent.getStringExtra(LoginActivity.BUNDLE_REQUEST_LOGIN_ACTIVITY);
	}
	
	private class CheckBoxCheckedChangeListener implements  OnCheckedChangeListener{

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if(mCheckBox.isChecked()){
				register_pwd_edt.setInputType(0x90);
			}
			else{
				register_pwd_edt.setInputType(0x81);
			}
			if(register_pwd_edt.getText().length() > 0){
				int position = register_pwd_edt.getText().length(); 
				Selection.setSelection(register_pwd_edt.getText(), position);
			}
			
		}
		
	}
    
	private class ScroolViewTouichListener implements OnTouchListener{

		public boolean onTouch(View arg0, MotionEvent arg1) {
			Resources res = getResources();
			float density = res.getDisplayMetrics().density;
			LayoutParams params = new LayoutParams(mListView.getWidth(), mListView.getHeight());
			params.topMargin = (int) ((90 * density) - mScrollView.getScrollY());
			params.leftMargin = (int) (20 * density);
			params.rightMargin = (int) (20 * density);
			mListView.setLayoutParams(params);
			mListView.setVisibility(View.GONE);
			username_error_msg_txt.setVisibility(View.GONE);
			pwd_error_msg_txt.setVisibility(View.GONE);
			nickname_error_msg_txt.setVisibility(View.GONE);
			return false;
		}
		
	}
	
	private class ListMailItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			register_username_edt.setText(mUserMail.get(position).toString());
			mListView.setVisibility(View.GONE);
			
		}
	}
	
	private class ListMailBtnClickListener implements OnClickListener{

		public void onClick(View arg0) {
			
			if(mListView.getVisibility() == View.GONE){
				mListView.setVisibility(View.VISIBLE);
			}else{
				mListView.setVisibility(View.GONE);
			}
			
			if(mUserMail.size() > 0){
				mUserMailListAdapter.setItems(mUserMail);
				mUserMailListAdapter.notifyDataSetChanged();
			}
		}
	}
	
	
	
	private void showToastMsg(String messsg){
		Message msg = mHandler.obtainMessage(TOAST_MSG);
		msg.getData().putString("message", messsg);
		msg.sendToTarget();
	}

	private void initViewByIntent(Intent intent) {
		step = intent.getIntExtra("step",1);
		String username = intent.getStringExtra("username");
		String password = intent.getStringExtra("password");
		register_username_edt.setText(username);
		register_pwd_edt.setText(password);
		if(step == 2){
			hideOtherViews();
			showVerifyView();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initViewByIntent(intent);
	}

	
	private void verifyAccount() {
		final String username = register_username_edt.getText().toString();
		final String pwd = register_pwd_edt.getText().toString();
		final String verifyCode = register_verify_edt.getText().toString();
		asyncBorqsAccount.verifyAccountRegister(username,verifyCode,new TwitterAdapter(){
			public void verifyAccountRegister(BorqsUserSession registerAccount) {
				Log.d(TAG, "verifyAccountRegister successfully");
				
				BorqsAccount account = new BorqsAccount();
				account.sessionid = registerAccount.sessionid;
				account.uid = registerAccount.uid;
				account.username = registerAccount.username;
				account.createtime = registerAccount.createtime;
				account.modifytime = registerAccount.modifytime;
				account.nickname = registerAccount.nickname;
				account.screenname = registerAccount.screenname;
				account.verify = registerAccount.verify;
				
				Message msg = mHandler.obtainMessage(VERIFY_END);
				
				try{
					orm.updateAccount(account);
                    msg.getData().putBoolean(IS_VERIFY_SUCCESS_KEY, true);
				}catch(Exception e){
					msg.getData().putBoolean(IS_VERIFY_SUCCESS_KEY, false);
				}			
                msg.sendToTarget();				
			}
			
			public void onException(TwitterException ex,TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "verifyAccount, server exception:", ex, method);

				Message msg = mHandler.obtainMessage(VERIFY_END);
				msg.getData().putBoolean(IS_VERIFY_SUCCESS_KEY, false);
				msg.getData().putInt(STATUS_CODE_KEY, ex.getStatusCode());
                msg.sendToTarget();
               
			}
		});
	}

	private void registerAccount() {
		final String username = register_username_edt.getText().toString();
		final String pwd = register_pwd_edt.getText().toString();
		final String nickname = register_nickname_edt.getText().toString();
		final String phonenumber = register_phonenumber_edt.getText().toString();
		
		boolean valid = true;
		if(StringUtil.isEmpty(username) || !StringUtil.isValidEmail(username)){
			valid = false;
			username_error_msg_txt.setVisibility(View.VISIBLE);
			username_error_msg_txt.setText(R.string.username_error_msg);
			return;
		} else {
			username_error_msg_txt.setVisibility(View.GONE);
		}
		
		if(StringUtil.isEmpty(nickname)){
			valid = false;
			nickname_error_msg_txt.setVisibility(View.VISIBLE);
			nickname_error_msg_txt.setText(R.string.nickname_error_msg);
			return;
		}
		else
		{
			nickname_error_msg_txt.setVisibility(View.GONE);
		}

		if(StringUtil.isEmpty(pwd) || !StringUtil.isValidPwd(pwd)){
			valid = false;
			pwd_error_msg_txt.setVisibility(View.VISIBLE);
			pwd_error_msg_txt.setText(R.string.pwd_error_msg);
			return;
		} else {
			pwd_error_msg_txt.setVisibility(View.GONE);
		}

		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		String imsi = mTelephonyMgr.getSubscriberId();
		String urlname = "";
		
		if(imei == null) {
			imei = "";
		}
		
		if(imsi == null) {
			imsi = "";
		}
		
		if(BorqsAccountConfig.LOGD)Log.d(TAG, "registerAccount  imsi:"+imsi+"  imei:"+imei);
		boolean gender = false;    
		if (manRadioButton.isChecked()) 
		{
			gender = true;
		}
		else if (womanRadioButton.isChecked()) 
		{
			gender = false;
		}
		asyncBorqsAccount.registerBorqsAccount(username,pwd,nickname,gender,phonenumber, urlname, imei, imsi, new TwitterAdapter(){
			public void registerBorqsAccount(boolean suc) {
				Log.d(TAG, "register successfully");
//				BorqsAccount account = new BorqsAccount();
//				account.sessionid = registerAccount.sessionid;
//				account.uid = registerAccount.uid;
//				account.username = registerAccount.username;
//				account.createtime = registerAccount.createtime;
//				account.modifytime = registerAccount.modifytime;
//				account.nickname = registerAccount.nickname;
//				account.screenname = registerAccount.screenname;
//				account.verify = registerAccount.verify;
				
				Message msg = mHandler.obtainMessage(REGISTER_END);
				msg.getData().putBoolean(IS_REGISTER_SUCCESS_KEY, suc);
				try{
//					orm.updateAccount(account);
					
					//send login broadcase
//					Intent loginIntent = new Intent("ACTION_BORQS_LOGIN_SUCCESS");
//					loginIntent.putExtra("Account", account);
//					loginIntent.setClassName(getPackageName(), "com.borqs.qiupu.service.QiupuService");
//					RegisterActivity.this.startService(loginIntent);							    	
//					Log.d(TAG, "account:"+account);
					
                    
                    startCollectPhoneInfo();
				}catch(Exception e){
//					msg.getData().putBoolean(IS_REGISTER_SUCCESS_KEY, false);
				}
			
                msg.sendToTarget();
			}

			public void onException(TwitterException ex,TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "registerAccount, server exception:", ex, method);

				Message msg = mHandler.obtainMessage(REGISTER_END);
				msg.getData().putBoolean(IS_REGISTER_SUCCESS_KEY, false);
				msg.getData().putInt(STATUS_CODE_KEY, ex.getStatusCode());
                msg.sendToTarget();
			}
		});
	}
	
	protected void startCollectPhoneInfo(){
        IntentUtil.shootCollectPhoneInfoIntent(getApplicationContext());
	}
	
	protected void onDestroy() {
		super.onDestroy();
//		UserAccountObserver.unregisterAccountListener(getClass().getName());
	}
	
	protected void onPause() {
		super.onPause();
	}
	
	protected void onResume() {
		super.onResume();
	}
    
	private final int REGISTER_END = 0;
	private final int VERIFY_END = 1;
	private final String IS_REGISTER_SUCCESS_KEY = "is_register_success";
	private final String IS_VERIFY_SUCCESS_KEY = "is_verify_success";
	private final String STATUS_CODE_KEY = "STATUS_CODE";
	private final int TOAST_MSG = 2;
	private class RegisterHandler extends Handler{
	    public void handleMessage(Message msg) {
	    	switch(msg.what){ 
		    	case REGISTER_END:{
			    	boolean is_register_success = msg.getData().getBoolean(IS_REGISTER_SUCCESS_KEY);
			    	Log.d(TAG,"verify user end login success:"+is_register_success);
			    	
			    	if(is_register_success)
			    	{
			    		Toast.makeText(RegisterActivity.this, R.string.register_ok_tutorial, Toast.LENGTH_LONG).show();

			    		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
			    		startActivity(intent);
			    		
                        RegisterActivity.this.finish();
			    	} else {
			    		int status_code = msg.getData().getInt(STATUS_CODE_KEY);
			    		if(status_code == ErrorResponse.USER_EXISTED)
			    		{
			    			username_error_msg_txt.setVisibility(View.VISIBLE);
			    			username_error_msg_txt.setText(R.string.user_exist_error_msg);
			    		}
			    		
			    		//register failed
			    		showToastMsg(getString(R.string.registe_failed_msg));
			    	}
			    	
			    	register_ok_btn.setEnabled(true);
					register_cancel_btn.setEnabled(true);
					
			    	break;
			    }
		    	case VERIFY_END:
		    	{
		    		boolean is_verify_success = msg.getData().getBoolean(IS_VERIFY_SUCCESS_KEY);
			    	Log.d(TAG,"verify user end login success:"+is_verify_success);
			    	if(is_verify_success){
//			    		UserAccountObserver.login();
			    	}
			    	else
			    	{
			    		verify_error_msg_txt.setVisibility(View.VISIBLE);
			    		verify_error_msg_txt.setText(R.string.verify_code_error);
			    	}
			    	showToastMsg(getString(R.string.verify_failed_msg));
			    	register_ok_btn.setEnabled(true);
					register_cancel_btn.setEnabled(true);
					
		    	}
		    	case TOAST_MSG:{
		    		String msg_str = msg.getData().getString("message");
		    		mToast.setText(msg_str);
		    		mToast.show();
		    		break;
		    	}
	    	}
	    }
		
	}
   
	private void hideVerifyView(){
		register_verify_edt.setVisibility(View.GONE);
		verify_error_msg_txt.setVisibility(View.GONE);
		verify_txv.setVisibility(View.GONE);
	}
	
	private void showVerifyView(){
		register_verify_edt.setVisibility(View.VISIBLE);
		verify_txv.setVisibility(View.VISIBLE);
	}
	
	private void hideOtherViews(){
		register_username_edt.setVisibility(View.GONE);
		register_pwd_edt.setVisibility(View.GONE);
		register_nickname_edt.setVisibility(View.GONE);
		register_phonenumber_edt.setVisibility(View.GONE);
 
		username_error_msg_txt.setVisibility(View.GONE);
		nickname_error_msg_txt.setVisibility(View.GONE);
		pwd_error_msg_txt.setVisibility(View.GONE);
		phonenumber_error_msg_txt.setVisibility(View.GONE);
		
		username_txv.setVisibility(View.GONE);
		pwd_txv.setVisibility(View.GONE);
		nickname_txv.setVisibility(View.GONE);
		phonenumber_txv.setVisibility(View.GONE);
	}
	
	private void showOtherView(){
		register_username_edt.setVisibility(View.VISIBLE);
		register_pwd_edt.setVisibility(View.VISIBLE);
		register_nickname_edt.setVisibility(View.VISIBLE);
		register_phonenumber_edt.setVisibility(View.VISIBLE);
 
//		username_error_msg_txt.setVisibility(View.GONE);
//		nickname_error_msg_txt.setVisibility(View.GONE);
//		pwd_error_msg_txt.setVisibility(View.GONE);
//		confirmpwd_error_msg_txt.setVisibility(View.GONE);
//		phonenumber_error_msg_txt.setVisibility(View.GONE);
		
		username_txv.setVisibility(View.VISIBLE);
		pwd_txv.setVisibility(View.VISIBLE);
		nickname_txv.setVisibility(View.VISIBLE);
		phonenumber_txv.setVisibility(View.VISIBLE);
	}
    
	public void onLogin() {
		// TODO Auto-generated method stub
		RegisterActivity.this.finish();
	}

	
	public void onLogout() {
		// TODO Auto-generated method stub
		
	}

	public void filterInvalidException(TwitterException ne) {
		// TODO Auto-generated method stub
		
	}

	public void onCancelLogin() {
		// TODO Auto-generated method stub
		
	}

	public void afterTextChanged(Editable arg0) {
		
	}

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}
	private String[] mailSuffix = new String[AccountServiceConfig.mailSuf.length];
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		String str = register_username_edt.getText().toString();
		int len = str.length();
		int size = AccountServiceConfig.mailSuf.length;
		Log.d(TAG, "len: "+len);
		if(str.indexOf("@")==str.lastIndexOf("@")){
			if(len > 0 && str.charAt(len - 1)=='@'){
				Log.d(TAG, "str: "+str.charAt(len - 1));
				register_username_edt.setThreshold(len + 1);
				for(int i = 0;i <size ; i++){
					mailSuffix[i] = str + AccountServiceConfig.mailSuf[i];
				}
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,mailSuffix);
				register_username_edt.setAdapter(adapter);
			}
		}
	}
}
