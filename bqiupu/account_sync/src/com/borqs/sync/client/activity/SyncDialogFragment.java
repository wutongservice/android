/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, or disclosed, or disseminated outside of Borqs 
 * in whole or in part without Borqs 's permission.
 */
package com.borqs.sync.client.activity;

import android.app.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.borqs.contacts_plus.R;

public class SyncDialogFragment extends DialogFragment {

    private TextView mSyncMsg;
    private Button mOperateBtn;
    private DismissListener mListener;
    
    
    @Override
    public void dismiss() {
        super.dismiss();
        if(getActivity() != null){
            mListener.onDismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(!(activity instanceof DismissListener)){
            try{
                throw new Exception("Attach activity must implements DismissListener");
            }catch (Exception e){
                e.printStackTrace();
             }
         }
        mListener = (DismissListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(R.style.sync_dialog, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
//        Account a = ContactSyncHelper.getBorqsAccount(mContext);
//        if( a != null){
//            ContactSyncHelper.requestContactsSyncOnAccount(a);
//        }
       setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sync_progress_dialog, container, false);
        mSyncMsg = (TextView)v.findViewById(R.id.sync_progress_text);
        mSyncMsg.setText(R.string.change_log_sync);
        
        mOperateBtn = (Button)v.findViewById(R.id.sync_operation_btn);
        mOperateBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                String str = String.valueOf(((Button)view).getText());
                if(getString(R.string.sync_run_at_background).equals(str)){
                    if(SyncDialogFragment.this.getActivity() != null){
                        mListener.onSelectRunBackground();
                       }
                  }
                SyncDialogFragment.this.dismiss();
            }
        });
        
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public static DialogFragment newInstance() {
        return new SyncDialogFragment();
    }

    
    public interface DismissListener{
        void onDismiss();
        void onSelectRunBackground();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(getActivity() != null){
            mListener.onDismiss();
        }
    }
}
