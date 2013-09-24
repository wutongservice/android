/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;

import com.borqs.account.login.R;
import com.borqs.account.login.service.BMSAuthenticatorService;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.BLog;

/**
 * Date: 7/4/12
 * Time: 11:07 AM
 * Borqs project
 */
public class AccountRemoveActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.account_removal);

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                android.R.drawable.ic_dialog_alert);

        setupView();
    }

    private void setupView(){
        findViewById(R.id.account_removal_conform).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.account_removal_conform){
            findViewById(R.id.account_removal_progress_container).setVisibility(View.VISIBLE);
            findViewById(R.id.account_removal_confirm_container).setVisibility(View.GONE);
            new ContactEraseTask().execute();
        }
    }

    public static Account getBorqsAccount(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        if (accounts != null) {
            for (Account account : accounts) {
                if (ConstData.BORQS_ACCOUNT_TYPE.equals(account.type)) {
                    return account;
                }
            }
        }
        return null;
    }

    private class ContactEraseTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Context context = AccountRemoveActivity.this;
            Account borqsAccount = getBorqsAccount(context);

            if(borqsAccount != null){
                //1. disable the conact sync
                ContentResolver.setIsSyncable(borqsAccount, ContactsContract.AUTHORITY, 0);
                ContentResolver.cancelSync(borqsAccount, ContactsContract.AUTHORITY);

                //2.erase contacts data
                String where = ContactsContract.RawContacts.ACCOUNT_NAME + "=? AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
                String[] args = new String[]{AccountHelper.getBorqsAccountId(AccountRemoveActivity.this), ConstData.BORQS_ACCOUNT_TYPE};

                int count = count(ContactsContract.RawContacts.CONTENT_URI, where, args);
                BLog.d("Remove contacts : " + count);
                getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,where, args);

                //3. remove account
                AccountHelper.removeBorqsAccount(context);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            BMSAuthenticatorService.onAccountLogout(getApplicationContext());
            finish();
        }

        private int count(Uri uri, String where, String[] args){
            Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{BaseColumns._ID},
                    where, args, null);
            try{
                return c.getCount();
            } finally {
                c.close();
            }
        }
    }


}
