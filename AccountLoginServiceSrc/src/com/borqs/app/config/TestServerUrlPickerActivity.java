package com.borqs.app.config;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.borqs.account.login.R;
import com.borqs.account.login.service.AccountService;
import com.borqs.app.Env;

import java.net.URL;



public class TestServerUrlPickerActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "TestServerUrlPickerActivity";
    private static final int FAILED_LOAD_MSG = 120;
    
    private Button close_ui;
    private RadioGroup mRadioGroup;

    private static final int INDEX_FORMAL_SERVER_URL = 0;
    private static final int INDEX_DEBUG_SERVER_URL = 1;
    private static final int INDEX_PRERELEASE_SERVER_URL = 2;
    private static final int INDEX_TEST_SERVER_URL = 3;

    private static URL imageurl;
    private static boolean mLocalConfigToggled;

    private int mInitChecked;
    private BasicHandler mHandler;
    private Config mConfig;
    private HostLoader mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_test_server_url_picker_activity);
        setTitle(R.string.account_pref_borqs_url_select);

        mRadioGroup = (RadioGroup) findViewById(R.id.upload_radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);

        mConfig = new Config(this);
        mLoader = new HostLoader(this);

        try {
            imageurl = new URL(HostLoader.HOST_CONFIG_URL);
        } catch (Exception e) {

        }

        if (Env.SERVER_MODE_RELEASE == mConfig.getServerMode()) {
            mInitChecked = INDEX_FORMAL_SERVER_URL;
        }else if(Env.SERVER_MODE_DEV == mConfig.getServerMode()) {
            mInitChecked = INDEX_DEBUG_SERVER_URL;
        }else if(Env.SERVER_MODE_PRE_RELEASE == mConfig.getServerMode()) {
            mInitChecked = INDEX_PRERELEASE_SERVER_URL;
        }else if(Env.SERVER_MODE_TEST == mConfig.getServerMode()) {
            mInitChecked = INDEX_TEST_SERVER_URL;
        }else{
        	mInitChecked = INDEX_FORMAL_SERVER_URL;
        }

        mHandler = new BasicHandler();

        mRadioGroup.check(mRadioGroup.getChildAt(mInitChecked).getId());

        final TextView textView = (TextView)findViewById(R.id.config_source_label);
        if (!TextUtils.isEmpty(imageurl.toString())) {
            textView.setText(imageurl.toString());
            if (mLoader.hasLocalDebugConfig()) {
                Log.d(TAG, "have local config!!!");
                showAlertDialog();
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLocalConfigToggled = !mLocalConfigToggled;
                        if (mLocalConfigToggled) {
                            textView.setText(HostLoader.HOST_CONFIG_FILE_PATH);
                        } else {
                            textView.setText(imageurl.toString());
                        }
                    }
                });
            }
        } else if (mLoader.hasLocalDebugConfig()) {
            showAlertDialog();
            textView.setText(HostLoader.HOST_CONFIG_FILE_PATH);
        }

        close_ui = (Button) findViewById(R.id.close_button);

        close_ui.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                final Context context = getApplicationContext();
                if(!check()){
                    return;
                }

                new Thread() {
                    public void run() {
                        final int checkedId = mRadioGroup.getCheckedRadioButtonId();
                        final int index = mRadioGroup.indexOfChild(findViewById(checkedId));
                        final int oldSetIndex = mConfig.getServerMode();
                        Log.v(TAG, String.format("close with choice: id/index/old index(%d/%d/%d).", checkedId, index, oldSetIndex));
                        mConfig.setServerMode(index);

                        String url = textView.getText().toString();
                        if (mLoader.load(url, mHandler)) {
                            Log.d(TAG, "Ok, doDefaultConfigurationLoadingTask return true.");
                        } else {
                            // revert to old set index.
                           // mRadioGroup.check(oldSetIndex);
                           // mConfig.setServerMode(oldSetIndex);
                            Message msg = mHandler.obtainMessage(FAILED_LOAD_MSG);
                            msg.arg1 = oldSetIndex;
                            mHandler.sendMessage(msg);
                            Log.w(TAG, "failed, doDefaultConfigurationLoadingTask return false.");
                        }
                    }
                }.start();
            }
        });
        close_ui.setText(android.R.string.ok);

        dumpCurrent();
    }

    private boolean check(){
        AccountService service = new AccountService(this);
        if(service.isAccountLogin()){
            new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.account_have_account_alert)
                    .create()
                    .show();
            return  false;
        }

        return true;
    }

    private void removeAccount(){
        AccountManager am = AccountManager.get(this);

        Account[] accounts = am.getAccountsByType("com.borqs");
        if (accounts.length == 0) {
            return;
        }
        am.removeAccount(accounts[0], null, null);
    }

    private void dumpCurrent(){
        ((TextView)findViewById(R.id.status_text)).setText(mLoader.listConfig());
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (null != close_ui) {
            close_ui.setText(android.R.string.ok);
        }
    }

    class BasicHandler extends Handler {
    	@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HostLoader.BEGIN_LOAD:
				((TextView)findViewById(R.id.status_text)).setText("begin loading configuration...");
				Toast.makeText(getApplicationContext(), "begin load configuration", Toast.LENGTH_LONG).show();
				break;
            case HostLoader.FAILED_LOAD:
                ((TextView)findViewById(R.id.status_text)).setText("failed loading configuration");
                Toast.makeText(getApplicationContext(), "failed load configuration", Toast.LENGTH_LONG).show();
                break;
			case HostLoader.FINISH_LOAD:
				((TextView)findViewById(R.id.status_text)).setText("finish loading configuration");
				Toast.makeText(getApplicationContext(), "finish load configuration", Toast.LENGTH_LONG).show();
				finish();
				break;
			case FAILED_LOAD_MSG:
			    mRadioGroup.check(msg.arg1);
                mConfig.setServerMode(msg.arg1);
                break;
			}
    	}
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    //BEGIN_INCLUDE(activity)
    private void showAlertDialog() {
        final String fileName = HostLoader.HOST_CONFIG_FILE_PATH;
        if (!TextUtils.isEmpty(fileName)) {
            final String message = getString(R.string.account_dialog_local_config_message, fileName);
            new AlertDialog.Builder(this).setMessage(message)
                    .create().show();
        }
    }
}
