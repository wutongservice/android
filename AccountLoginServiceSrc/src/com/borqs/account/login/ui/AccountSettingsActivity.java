/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.borqs.account.login.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 8/23/12
 * Time: 11:22 AM
 * Borqs project
 */
public class AccountSettingsActivity extends Activity implements AdapterView.OnItemClickListener {
    private String ACTIVITY_DATA_FILTER = "com.borqs.account.action.SETTINGS_PLUGIN";

    private ItemAdapter mItemAdapters;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lookupActivity();
    }

    private void lookupActivity(){
        PackageManager pm = getPackageManager();
        Intent filterIntent = new Intent(ACTIVITY_DATA_FILTER);
        List<ResolveInfo> result = pm.queryIntentActivities(filterIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if(result.size() == 1){
            gotoActivity(result.get(0).activityInfo);
            finish();
        } else if(result.size()==0){
            Toast.makeText(this, R.string.account_settings_no_options, Toast.LENGTH_LONG).show();
            finish();
        } else{
            mItemAdapters = new ItemAdapter(result);
            showDialog(mItemAdapters);
        }
    }

    private void showDialog(ItemAdapter adapter){
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setPadding(3, 10, 3, 20);
        new AlertDialog.Builder(this)
                .setTitle(R.string.account_settings_options)
                .setView(listView)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    private void gotoActivity(ActivityInfo activity){
        Intent i = new Intent();
        i.setClassName(activity.packageName, activity.name);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mItemAdapters==null){
            return;
        }
        ActivityInfo activity = (ActivityInfo)mItemAdapters.getItem(position);
        gotoActivity(activity);
        finish();
    }

    private class ItemAdapter extends BaseAdapter {
        List<ActivityInfo> mActivities = new ArrayList<ActivityInfo>();

        private ItemAdapter(List<ResolveInfo> activities){
            for(ResolveInfo r: activities){
                mActivities.add(r.activityInfo);
            }
        }

        @Override
        public int getCount() {
            return mActivities.size();
        }

        @Override
        public Object getItem(int position) {
            return mActivities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = LayoutInflater.from(AccountSettingsActivity.this)
                        .inflate(R.layout.account_settings_layout_item, null);
            }

            ImageView logo = (ImageView)itemView.findViewById(R.id.action_logo);
            TextView title = (TextView)itemView.findViewById(R.id.action_title);
            TextView summary = (TextView)itemView.findViewById(R.id.action_summary);

            PackageManager pm = AccountSettingsActivity.this.getPackageManager();
            ActivityInfo info = (ActivityInfo)getItem(position);
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(info.packageName, 0);
                logo.setImageDrawable(appInfo.loadIcon(pm));
                title.setText(appInfo.loadLabel(pm));
                summary.setText(info.loadLabel(pm));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return itemView;
        }
    }
}
