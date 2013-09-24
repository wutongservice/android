package com.borqs.qiupu.ui.bpc;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import com.borqs.common.util.PushingServiceAgent;
import com.borqs.qiupu.ui.AboutActivity;
import com.borqs.qiupu.ui.BasicActivity;
import twitter4j.AsyncQiupu;
import twitter4j.ErrorResponse;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.util.StringUtil;

public class BpcSettingsActivity extends BasicActivity.StatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private String TAG = "BpcSettingsActivity";
    private static final int UPDATE_PASSWORD = 101;
    private static final int CLEAR_CACHE = 103;

    private AsyncQiupu asyncQiupu;
    private HandlerLoad handler;
    private Dialog mSelectDialog;
    private View mSelectView;
    private View mProgressView;

    private QiupuORM orm;
    private Preference notification_list;
    private Preference notification_vibrate;
    private Preference ntfSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bpc_settings_preference);

        orm = QiupuORM.getInstance(this);
        handler = new HandlerLoad();
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);

        if (StringUtil.isValidString(AccountServiceUtils.getSessionID())) {
            Preference updatepwd = findPreference("key_update_password");
            if (null != updatepwd) {
                updatepwd.setOnPreferenceClickListener(preferenceClickLister);
            }
        }

//        EditTextPreference trendPref = (EditTextPreference)findPreference("key_stream_view_timeout");
//        if (null != trendPref) {
//            trendPref.setOnPreferenceChangeListener(this);
//            long trendtimeout = orm.getStreamInterval();
//            final String interval = String.valueOf(trendtimeout);
//            trendPref.setSummary(interval);
//            trendPref.setText(interval);
//        }

        EditTextPreference friendsPref = (EditTextPreference)findPreference("key_friends_view_timeout");
        if (null != friendsPref) {
            friendsPref.setOnPreferenceChangeListener(this);
            long friendstimeout = orm.getFriendsInterval();
            final String interval = String.valueOf(friendstimeout);
            friendsPref.setSummary(interval);
            friendsPref.setText(interval);
        }

        Preference clearCache = findPreference("key_clear_cache");
        if (null != clearCache) {
            clearCache.setOnPreferenceClickListener(preferenceClickLister);
        }

//        Preference debugPreference = findPreference("key_configure_debug_option");
//        if (null != debugPreference) {
//            debugPreference.setOnPreferenceClickListener(preferenceClickLister);
//        }

        Preference httpLog = findPreference("key_borqs_debug_log");
        if (httpLog != null) {
            httpLog.setOnPreferenceChangeListener(this);
            boolean isDebugMode = orm.isDebugMode();
            ((CheckBoxPreference) (httpLog)).setChecked(isDebugMode);
        }

        final boolean isEnableGetRequest = orm.isEnableNotification();

        notification_vibrate = findPreference("key_notification_vibrate");
        if (null != notification_vibrate) {
            notification_vibrate.setOnPreferenceChangeListener(this);
            boolean isEnableVibrate = orm.isEnableVibrate();
            ((CheckBoxPreference) (notification_vibrate)).setChecked(isEnableVibrate);
            notification_vibrate.setEnabled(isEnableGetRequest);
        }

        Preference notification_enable = findPreference("key_request_enable");
        if (null != notification_enable) {
            notification_enable.setOnPreferenceChangeListener(this);
            ((CheckBoxPreference) (notification_enable)).setChecked(isEnableGetRequest);
        }
        
        Preference push_enable = findPreference("key_push_service_enable");
        if (null != push_enable) {
            push_enable.setOnPreferenceChangeListener(this);
            ((CheckBoxPreference)push_enable).setChecked(orm.isEnablePushService());
        }

        notification_list = findPreference("key_notification_list");
        if (null != notification_list) {
            notification_list.setDefaultValue(orm.getRequestsInterval());
            notification_list.setOnPreferenceChangeListener(this);
            notification_list.setEnabled(isEnableGetRequest);
        }
        
        final boolean isEnableNotification = orm.isEnableGetNotification();
        Preference key_notification_enable = findPreference("key_notification_enable");
        if(null != key_notification_enable)
        {
        	key_notification_enable.setOnPreferenceChangeListener(this);
        	((CheckBoxPreference)key_notification_enable).setChecked(isEnableNotification);
        }

        ntfSet = findPreference("key_notification_set");
        if (null != ntfSet) {
            ntfSet.setOnPreferenceClickListener(preferenceClickLister);
            ntfSet.setEnabled(isEnableNotification);
        }

        Preference showPic = findPreference("key_data_flow_auto_save_mode");
        if (null != showPic) {
            showPic.setOnPreferenceChangeListener(this);
            boolean showpic = orm.isDataFlowAutoSaveMode();
            ((CheckBoxPreference) (showPic)).setChecked(showpic);
        }

        Preference about = findPreference("key_app_about");
        if (null != about) {
            about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    gotoAboutActivity();
                    return true;
                }
            });
        }
    }

    public boolean onPreferenceChange(Preference pref, Object value) {
        final String key = pref.getKey();
        if (key.equals("key_request_enable")) {
            boolean show = (Boolean) value;
            orm.setEnableNotification(show);

            ((CheckBoxPreference) (pref)).setChecked(show);
            notification_list.setEnabled(show);
            notification_vibrate.setEnabled(show);

            if (show && QiupuService.mRequestsService != null)// TODO every open have the need to send
            {
                QiupuService.mRequestsService.rescheduleRequests(true);
            }
        } else if (key.equals("key_notification_vibrate")) {
            boolean show = (Boolean) value;
            orm.setEnableVibrate(show);

            ((CheckBoxPreference) (pref)).setChecked(show);
        } else if (key.equals("key_notification_list")) {
            String hz = (String) value;
            orm.setRequestsInterval(Integer.parseInt(hz));
            if (QiupuService.mRequestsService != null) {
                QiupuService.mRequestsService.rescheduleRequests(true);
            }
            return true;
        }
        else if(key.equals("key_notification_enable"))
        {
        	boolean show = (Boolean) value;
            orm.setEnableGetNotification(show);

            ((CheckBoxPreference) (pref)).setChecked(show);
            ntfSet.setEnabled(show);
        } else if (key.endsWith("key_borqs_debug_log")) {
            boolean show = (Boolean) value;
            orm.setDebugMode(show);

            ((CheckBoxPreference) (pref)).setChecked(show);
//        } else if (key.equals("key_stream_view_timeout")) {
//            String interval = (String)value;
//            pref.setSummary(interval);
//            ((EditTextPreference)pref).setText(interval);
//            orm.setStreamInterval(Integer.parseInt(interval));
        } else if (key.equals("key_friends_view_timeout")) {
            String interval = (String)value;
            pref.setSummary(interval);
            ((EditTextPreference)pref).setText(interval);
            orm.setFriendsInterval(Integer.parseInt(interval));
        } else if (key.equals("key_push_service_enable")) {
            boolean enabled = (Boolean) value;
            orm.setEnablePushService(enabled);
            ((CheckBoxPreference) (pref)).setChecked(enabled);
            
            PushingServiceAgent.enablePushServiceComponent(this, enabled);
        } else if (key.equals("key_data_flow_auto_save_mode")) {
            boolean show = (Boolean) value;
            orm.setDataFlowAutoSaveMode(show);

            ((CheckBoxPreference) (pref)).setChecked(show);
        } else {
            Log.w(TAG, "onPreferenceChange, no response to unexpected key:" + key);
        }

        return false;
    }

    OnPreferenceClickListener preferenceClickLister = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference pref) {
            final String key = pref.getKey();
            boolean ret = true;
            if (key.equals("key_clear_cache")) {
                showDialog(CLEAR_CACHE);
            } else if (key.equals("key_update_password")) {
                showDialog(UPDATE_PASSWORD);
            } else if (key.equals("key_notification_set")) {
                NotificationSettingActivity.startActivity(BpcSettingsActivity.this, false);
//            } else if (key.equals("key_configure_debug_option")) {
//                Intent intent = new Intent(getApplicationContext(), TestServerSetActivity.class);
//                startActivity(intent);
            } else {
                ret = false;
            }

            return ret;
        }
    };

    private final int GET_USERID = 1;
    private final int GET_USERID_END = 2;
    private final int UPDATE_USER_PASSWORD = 3;
    private final int UPDATE_USER_PASSWORD_OK = 4;
    private final int UPDATE_USER_PASSWORD_FAILED = 5;

    private class HandlerLoad extends Handler {
        public HandlerLoad() {
            super();

            Log.d(TAG, "new HandlerLoad");
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_USERID: {
                    break;
                }
                case GET_USERID_END: {
                    break;
                }
                case UPDATE_USER_PASSWORD: {
                    updatePassword(msg.getData().getString("newpassword"), msg.getData().getString("oldpassword"));
                    break;
                }
                case UPDATE_USER_PASSWORD_OK: {
                    Toast.makeText(BpcSettingsActivity.this, R.string.update_password_ok, Toast.LENGTH_LONG).show();
                    break;
                }
                case UPDATE_USER_PASSWORD_FAILED: {
                	if(msg.getData().getInt("errorcode") == ErrorResponse.BACKUP_FAILED)// TODO
                		Toast.makeText(BpcSettingsActivity.this, R.string.input_old_password_tutorial, Toast.LENGTH_SHORT).show();
                	else
                		Toast.makeText(BpcSettingsActivity.this, R.string.update_password_failed, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    private void showPasswordUpdateProgress(boolean show) {
        if (null != mProgressView) {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void dismissUpdateDialog(final boolean updated) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    showPasswordUpdateProgress(false);
                    if (updated) {
                        final EditText oldpwdtv = (EditText) mSelectView.findViewById(R.id.old_password_edt);
                        final EditText newpwdtv = (EditText) mSelectView.findViewById(R.id.new_password_edt);
                        oldpwdtv.setText("");
                        newpwdtv.setText("");
                    }
                    mSelectDialog.dismiss();
                } catch (Exception e) {
                }
            }
        });
    }
    private void updatePassword(String newpassword, String oldpassword) {
        showPasswordUpdateProgress(true);
        asyncQiupu.updateUserPasswrod(AccountServiceUtils.getSessionID(), newpassword, oldpassword,
                new TwitterAdapter() {
                    public void updateUserpassword(boolean result) {
                        if (result) {
                            Log.d(TAG, "updatePassword, result :" + result);
                            Message msg = handler.obtainMessage(UPDATE_USER_PASSWORD_OK);
                            msg.sendToTarget();
                        }
                        dismissUpdateDialog(result);
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        Log.d(TAG, "updatePassword, exception " + ex.getMessage() + "==method=" + method.name() + " " + ex.getStatusCode());
                        Message msg = handler.obtainMessage(UPDATE_USER_PASSWORD_FAILED);
                        msg.getData().putInt("errorcode", ex.getStatusCode());
                        msg.sendToTarget();
                        dismissUpdateDialog(false);
                    }
                });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case UPDATE_PASSWORD: {
                return showSelectDialog(UPDATE_PASSWORD);
            }
            case CLEAR_CACHE: {
                AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.clear_cache)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                QiupuHelper.ClearCache();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        })
                        .create();
                return dialog;
            }
        }
        return null;
    }


    private Dialog showSelectDialog(int id) {
        switch (id) {
            case UPDATE_PASSWORD: {
                initDialogView();
                mSelectDialog = new AlertDialog.Builder(BpcSettingsActivity.this)
                        .setTitle(R.string.update_password)
                        .setView(mSelectView)
                        .setCancelable(false)
                        .create();
                break;
            }
            default:
                break;
        }
        return mSelectDialog;
    }

    private void initDialogView() {
        mSelectView = LayoutInflater.from(this).inflate(R.layout.update_password, null);

        mProgressView = mSelectView.findViewById(R.id.update_progress);

        final EditText oldpwdtv = (EditText) mSelectView.findViewById(R.id.old_password_edt);
        final EditText newpwdtv = (EditText) mSelectView.findViewById(R.id.new_password_edt);
        final Button okButton = (Button) mSelectView.findViewById(R.id.dialog_ok);
        final Button cancelButton = (Button) mSelectView.findViewById(R.id.dialog_cancel);
        final CheckBox cb = (CheckBox) mSelectView.findViewById(R.id.show_password);
        
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{
				if(cb.isChecked()){
					oldpwdtv.setInputType(0x90);
					newpwdtv.setInputType(0x90);
				}
				else{
					oldpwdtv.setInputType(0x81);
					newpwdtv.setInputType(0x81);
				}
				
				if(newpwdtv.getText().length() > 0){
					int position = newpwdtv.getText().length(); 
					Selection.setSelection(newpwdtv.getText(), position);
				}
				
			}
		});

        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String oldpwd = oldpwdtv.getText().toString().trim();
                String newpwd = newpwdtv.getText().toString().trim();
                
                //check old password is valid 
                if (!StringUtil.isValidPwd(oldpwd)) {
                    Toast.makeText(BpcSettingsActivity.this, R.string.input_old_password_tutorial, Toast.LENGTH_LONG).show();
                    oldpwdtv.requestFocus();
                    return;
                } 
                //check new password is valid
                if (!StringUtil.isValidPwd(newpwd)) {
                    Toast.makeText(BpcSettingsActivity.this, R.string.input_new_password_tutorial, Toast.LENGTH_LONG).show();
                    newpwdtv.requestFocus();
                    return;
                }
                //check new password is same as old password
                if(oldpwd.equals(newpwd))
                {
                	Toast.makeText(BpcSettingsActivity.this, R.string.same_password_tutorial, Toast.LENGTH_LONG).show();
                    newpwdtv.requestFocus();
                    return;
                }

                Message msg = handler.obtainMessage(UPDATE_USER_PASSWORD);
                msg.getData().putString("newpassword", newpwd);
                msg.getData().putString("oldpassword", oldpwd);
                msg.sendToTarget();
                }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                mSelectDialog.dismiss();
            }
        });
    }

    private void gotoAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}

