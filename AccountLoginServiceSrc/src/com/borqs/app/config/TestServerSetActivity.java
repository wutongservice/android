package com.borqs.app.config;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import com.borqs.account.login.R;
import com.borqs.app.Env;

/**
 * dial "*#*#338#*#*" within dialer to invoke this setting UI to
 * switch the server between release/test.
 */
public class TestServerSetActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "TestServerSetActivity";
    private static boolean mIsPowerCycleChecked;

    public static final String API_URL_KEY = "api_host";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.account_debug_set_preference);
        Preference BorqsUrl = findPreference("key_select_borqs_url");
        if (null != BorqsUrl) {
            BorqsUrl.setOnPreferenceClickListener(preferenceClickLister);
        }

        Preference httpLog = findPreference("key_borqs_debug_log");
        if (httpLog != null) {
            httpLog.setOnPreferenceChangeListener(this);
            boolean isDebugMode = Env.isDebugMode(this);
                    ((CheckBoxPreference) (httpLog)).setChecked(isDebugMode);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                new HostLoader(TestServerSetActivity.this).initIfNecessary();
            }
        }).start();
    }

    Preference.OnPreferenceClickListener preferenceClickLister = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference pref) {
            final String key = pref.getKey();
            boolean ret = true;
            if (key.equals("key_select_borqs_url")) {
                Intent intent = new Intent(getApplicationContext(), TestServerUrlPickerActivity.class);
                startActivity(intent);
            }

            return ret;
        }
    };

    public boolean onPreferenceChange(Preference pref, Object value) {
        final String key = pref.getKey();
        if (key.endsWith("key_borqs_debug_log")) {
            boolean checked = (Boolean) value;

            Config config = new Config(this);
            config.setDebugMode(checked);
            ((CheckBoxPreference) (pref)).setChecked(checked);
        }
        else {
            Log.w(TAG, "onPreferenceChange, no response to unexpected key:" + key);
        }

        return false;
    }
}
