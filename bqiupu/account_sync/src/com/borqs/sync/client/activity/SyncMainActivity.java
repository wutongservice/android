/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, or disclosed, or disseminated outside of Borqs 
 * in whole or in part without Borqs 's permission.
 */
package com.borqs.sync.client.activity;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.common.contact.ContactService;
import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.ContactLock;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.client.common.SyncHelper.SyncResult;
import com.borqs.sync.service.SyncIntent;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.protocol.IDataChangeListener.ContactsChangeData;

import com.borqs.sync.client.activity.ContactSlider.BuddyData;

//Depends on account_sync
public class SyncMainActivity extends FragmentActivity implements SyncDialogFragment.DismissListener {
    
    private TextView mTitle;
//    private QuerySyncLogTask mQueryLogTask;
    private LoadSyncLogTask mLogTask;
    private SyncReceiver mSyncReceiver;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sync_main_view);
        setHeadTitle(R.string.contact_sync);
        mTitle = (TextView) findViewById(R.id.head_title);
        mSyncReceiver = new SyncReceiver();
        findViewById(R.id.sync_view).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                if(!ContactLock.isInIdle()){
                    Toast.makeText(SyncMainActivity.this, ContactLock.getErrorMsg(SyncMainActivity.this), Toast.LENGTH_LONG).show();
                } else {
                    doSync();
                  }
             }
        });
        installSyncListener();
        mLogTask = new LoadSyncLogTask();
        mLogTask.execute();
    }
    
    protected void setHeadTitle(final int resid) {
        new Handler().post(new Runnable() {
            public void run() {
                if (mTitle != null)
                    mTitle.setText(resid);
                else
                    setTitle(resid);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLogTask != null){
            mLogTask.cancel(true);
        }
        
        mSyncReceiver.unRegister();
        mSyncReceiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSyncInfo();
    }
    
    private void refreshSyncResult(){
        SyncResult lastSyncResult = SyncHelper.getLastSyncResult(this);
        String result = null;
        if(lastSyncResult.getResult() == SyncHelper.SYNC_SUCCESS){
            result = String.format(getString(R.string.contact_sync_result), getString(R.string.sync_log_successfully));
        }else if (lastSyncResult.getResult() == SyncHelper.SYNC_FAIL){
            String excetionStr = SyncHelper.getDsExceptionString(this, new DsException(lastSyncResult.getExceptionCategory(), lastSyncResult.getExceptionCode()));
            result = String.format(getString(R.string.contact_sync_result), excetionStr);
        }
        TextView resultText = (TextView)findViewById(R.id.last_sync_result);
        if(!TextUtils.isEmpty(result)){
            resultText.setVisibility(View.VISIBLE);
            resultText.setText(result);
        }else{
            resultText.setVisibility(View.GONE);
        }
    }
    
   private void showDialog() {
        // Create the fragment and show it as a dialog.
        DialogFragment newFragment = SyncDialogFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);    
        newFragment.show(ft, "sync_dialog");
    }
   
   private void dismissDialog(){
       Fragment fragement = getSupportFragmentManager().findFragmentByTag("sync_dialog");
       if(fragement != null){
          if(((SyncDialogFragment)fragement).getDialog() != null && ((SyncDialogFragment)fragement).getDialog().isShowing()){ 
              ((SyncDialogFragment)fragement).dismiss();
          }
       }
   }
    
    private void doSync() {
        Account a = ContactSyncHelper.getBorqsAccount(this);
        if( a != null){
            ContactSyncHelper.requestContactsSyncOnAccount(a);
        }
    }
    
    private void refreshSyncInfo(){
        SyncDeviceContext device = new SyncDeviceContext(this);
        String lastSync = getTimeStr(device.getLastSyncAnchor(), this);
        //last sync time
        ((TextView)findViewById(R.id.last_sync_time_text)).setText(lastSync);
        //last sync content
        if(mLogTask != null){
            mLogTask.cancel(true);
            mLogTask = new LoadSyncLogTask();
            mLogTask.execute();
        }
        if(device.getLastSyncAnchor() > 0){
            refreshSyncResult();
        }
    }
    
    public static void actionShow(Context ctx){
        Intent intent = new Intent(ctx, SyncMainActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    public void onDismiss() {
        refreshSyncInfo();
    }

    @Override
    public void onSelectRunBackground() {
       finish();
    }

    private void installSyncListener() {
        mSyncReceiver.register();
    }

    private void onSyncBegin(){
        showDialog();
    }

    private void onSyncEnd(){
        dismissDialog();
        if(mLogTask != null){
            mLogTask.cancel(true);
        }
        mLogTask = new LoadSyncLogTask();
        mLogTask.execute();
    }

    private void listMore(){
        Intent intent = new Intent("com.android.contacts.action.LIST_CONTACTS");
        startActivity(intent);
    }

    private class LoadSyncLogTask extends AsyncTask<Void , Void , Object[]>{
        private TextView addMsgInfo;
        private TextView updateMsgInfo;
        private TextView deleteMsgInfo;
        
        private static final int MAX_COUNT = 6;
        
        @Override
        protected void onPostExecute(Object[] result) {
            //add
            ContactSlider addSlider = (ContactSlider)findViewById(R.id.contact_add_slider);
            List<BuddyData> addList = (ArrayList<BuddyData>)result[1];
            if(addList.isEmpty()){
                addSlider.setVisibility(View.GONE);
            }else{
                addSlider.setVisibility(View.VISIBLE);
                addSlider.setContactData(addList);
             }
            if(!addList.isEmpty()){
                addMsgInfo.setVisibility(View.VISIBLE);
                addMsgInfo.setText(getString(R.string.contact_sync_add_detail_msg));
            }else{
                addMsgInfo.setVisibility(View.GONE);
            }
            
            TextView add = (TextView)findViewById(R.id.add_more);
            int addTotal = Integer.valueOf(result[0]+"");
            if(addTotal > MAX_COUNT){
                add.setText(String.format(getString(R.string.contact_sync_change_other_more), addTotal-MAX_COUNT));
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        listMore();
                    }
                });
            }else{
                add.setVisibility(View.GONE);
            }
            
            //update
            ContactSlider updateSlider = (ContactSlider)findViewById(R.id.contact_update_slider);
            List<BuddyData> updateList = (ArrayList<BuddyData>)result[3];
            if(updateList.isEmpty()){
                updateSlider.setVisibility(View.GONE);
            }else{
                updateSlider.setVisibility(View.VISIBLE);
                updateSlider.setContactData(updateList);
            }
            if(!updateList.isEmpty()){
                updateMsgInfo.setVisibility(View.VISIBLE);
                updateMsgInfo.setText(getString(R.string.contact_sync_update_detail_msg));
            }else{
                updateMsgInfo.setVisibility(View.GONE);
            }
            TextView update = (TextView)findViewById(R.id.update_more);
            int updateTotal = Integer.valueOf(result[2]+"");
            if( updateTotal> MAX_COUNT){
                update.setVisibility(View.VISIBLE);
                update.setText(String.format(getString(R.string.contact_sync_change_other_more), updateTotal-MAX_COUNT));
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listMore();
                    }
                });
            }else{
                update.setVisibility(View.GONE);
            }
            
            //delete
            TextView delView = (TextView)findViewById(R.id.delete_slider_msg);
            if(!TextUtils.isEmpty(result[5]+"")){
                deleteMsgInfo.setVisibility(View.VISIBLE);
                deleteMsgInfo.setText(String.format(getString(R.string.contact_sync_delete_detail_msg), result[5]));
                delView.setVisibility(View.VISIBLE);
                
                String head = getString(R.string.contact_sync_delete_detail_msg);
                String end = Integer.valueOf(result[4]+"") > MAX_COUNT ? getString(R.string.contact_sync_count_more):"";
                String totalDel =  head+":" + result[5] +end;
                delView.setText(buildNameString(head,totalDel,end));
            }else{
                deleteMsgInfo.setVisibility(View.GONE);
                delView.setVisibility(View.GONE);
            }
            TextView del = (TextView)findViewById(R.id.del_more);
            int delTotal = Integer.valueOf(result[4]+"");
            if( delTotal > MAX_COUNT){
                del.setVisibility(View.VISIBLE);
                del.setText(String.format(getString(R.string.contact_sync_change_other_more), delTotal-MAX_COUNT));
                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listMore();
                    }
                });
            }else{
                del.setVisibility(View.GONE);
            }
        }
        
        @Override
        protected void onPreExecute() {
            addMsgInfo = (TextView)findViewById(R.id.add_slider_title);
            updateMsgInfo = (TextView)findViewById(R.id.update_slider_title);
            deleteMsgInfo = (TextView)findViewById(R.id.delete_slider_title);
        }

        @Override
        protected Object[] doInBackground(Void... params) {
            
            Object[] obj = new Object[6];
            com.borqs.common.contact.ContactService.SyncLog log = ContactService
                .getSyncLog(SyncMainActivity.this);

            List<ContactsChangeData> caList = log.getClientAdds();
            List<ContactsChangeData> cuList = log.getClientUpdates();
            List<ContactsChangeData> cdList = log.getClientDeletes();
            List<ContactsChangeData> saList = log.getServerAdds();
            List<ContactsChangeData> suList = log.getServerUpdates();
            List<ContactsChangeData> sdList = log.getServerDeletes();
            String addName = "";
            String updateName = "";
            String delName = "";
            // add
            List<BuddyData> addlist = new ArrayList<BuddyData>();
            if (!saList.isEmpty()){
                for (int i = 0 ;i<saList.size();i++){
                    ContactsChangeData data = saList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_SERVER);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                    }
                    if(i == 0){
                        addName += data.name;
                        addlist.add(b);
                    }else{
                        if(addlist.size() < MAX_COUNT){
                            addName += "、"+data.name;
                            addlist.add(b);
                        }
                    }
                }
            }
            if (!caList.isEmpty()){
                for (int i = 0 ;i<caList.size();i++){
                    ContactsChangeData data = caList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_LOCAL);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                    }
                    
                    if(addlist.size() < MAX_COUNT){
                        if(i == 0 && TextUtils.isEmpty(addName)){
                            addName += data.name;
                            addlist.add(b);
                        }else{
                            addName += "、"+data.name;
                            addlist.add(b);
                        }
                    }
                }
            }
            
            obj[0] = saList.size() + caList.size();
            obj[1] = addlist;

            // update
            List<BuddyData> updatelist = new ArrayList<BuddyData>();
            if (!suList.isEmpty()){
                
                for (int i=0;i<suList.size();i++){
                    ContactsChangeData data = suList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_SERVER);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                       }
                    if(i == 0){
                        updateName += data.name;
                        updatelist.add(b);
                    }else{
                        if(updatelist.size()< MAX_COUNT){
                            updateName += "、"+data.name;
                            updatelist.add(b);
                        }
                    }
                }
            }
            
            if (!cuList.isEmpty()){
                
                for (int i=0;i<cuList.size();i++){
                    ContactsChangeData data = cuList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_LOCAL);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                       }
                    if(updatelist.size() < MAX_COUNT){
                        if(i == 0){
                            updateName += data.name;
                            updatelist.add(b);
                        }else{
                            updateName += "、"+data.name;
                            updatelist.add(b);
                        }
                    }
                }
            }
            obj[2] = suList.size()+cuList.size();
            obj[3] = updatelist;

            // delete
            if (!sdList.isEmpty()){
                for (int i=0;i<sdList.size();i++){
                    ContactsChangeData data = sdList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_SERVER);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                       }
                    if(i == 0){
                        delName += data.name;
                    }else{
                        if(i < MAX_COUNT){
                        delName += "、"+data.name;
                        }
                    }
                }
            }
            
            if (!cdList.isEmpty()){
                for (int i=0;i<cdList.size();i++){
                    ContactsChangeData data = cdList.get(i);
                    BuddyData b = new BuddyData();
                    b.setmName(data.name);
                    b.setmType(BuddyData.TYPE_FROM_LOCAL);
                    Bitmap bitmap = getPhoto(data.contactId);
                    if(bitmap != null){
                        b.setmPhoto(bitmap);
                       }
                    
                    if(sdList.size()+i+1 <= MAX_COUNT){
                        if(i == 0 && TextUtils.isEmpty(delName)){
                            delName += data.name;
                        }else{
                            delName += "、"+data.name;
                        }
                    }
                    
                }
            }
            obj[4] = sdList.size() + cdList.size();
            obj[5] = delName;

            return obj;
        }
        
    }
    
    private SpannableStringBuilder  buildNameString(String head,String full,String end){
      SpannableStringBuilder style=new SpannableStringBuilder(full); 
      style.setSpan(new ForegroundColorSpan(Color.GRAY), head.length(), full.length() - end.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE ); 
      return style;
    }
    
    private Bitmap getPhoto(long contactId){
        Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactId);
        InputStream input = 
            ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), rawContactUri); 
        try{
            return loadImage(InputStream2Byte(input));
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    
    private class SyncReceiver extends BroadcastReceiver{
        public void register(){
            IntentFilter filter = new IntentFilter(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END);
            filter.addAction(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN);
            registerReceiver(this, filter);
        }
        public void unRegister(){
            unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN.equals(intent.getAction())){
                onSyncBegin();
            } else if(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END.equals(intent.getAction())){
                onSyncEnd();
            }

        }
    }


    private static String getTimeStr(Long time,Context context) {
        if (time == null || time == 0){
            return context.getString(R.string.last_sync_none);
        }

        SimpleDateFormat dsf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dsf2 = new SimpleDateFormat("yyyy-MM-dd a h:mm");
        SimpleDateFormat dsf3 = new SimpleDateFormat("MM-dd a h:mm");
        SimpleDateFormat dsf4 = new SimpleDateFormat(" a h:mm");

        Date date = new Date(time);

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        String paramYear = "" + cal1.get(Calendar.YEAR);
        String paramTime = dsf.format(date);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE, -1);
        String yesterday = dsf.format(cal2.getTime());

        Calendar cal3 = Calendar.getInstance();
        String nowYear = "" + cal3.get(Calendar.YEAR);

        String hoursMinutes = dsf4.format(date);

        if (paramTime.equals(dsf.format(new Date()))) {
            return context.getString(R.string.last_sync_today)+hoursMinutes;
        } else if (paramTime.equals(yesterday)) {
            return context.getString(R.string.last_sync_yesterday)+hoursMinutes;
        } else if (paramYear.equals(nowYear)) {
            return dsf3.format(date);
        } else {
            return dsf2.format(date);
        }
    }
    public static Bitmap loadImage(byte[] bytes) {
        Bitmap bitmap = null;
        if (bytes != null){
            try{
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        null);
            }catch (OutOfMemoryError e){
                // Do nothing - the photo will to be missing
            }
        }
        return bitmap;
    }

    /**
     *  InputStream to byte[]
     * @param in InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] InputStream2Byte(InputStream in) throws IOException{
        if(in == null)
            return null;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[100];
        int count = -1;
        while((count = in.read(data,0,100)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }
}
