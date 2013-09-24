/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.qiupu.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.quickaction.ActionItem;
import com.borqs.common.quickaction.QuickPeopleActivity;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.bpc.InviteContactActivity;
import com.borqs.qiupu.ui.bpc.ProfileActionGatewayActivity;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.StringUtil;

/** A fragment that shows the list of resolve items below a tab */
public class QuickPeopleListFragment extends BasicFragment {
	private static final String TAG = "QuickPeopleListFragment";
    private ListView mListView;
    private ArrayList<ActionItem> mActions;
    private Activity mActivity;
    private Handler mHandler;

    public QuickPeopleListFragment() {
        setRetainInstance(true);
    }
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	mActivity = activity;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mHandler = new MainHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
    	mListView = (ListView) inflater.inflate(R.layout.default_listview,
                container, false);
        mListView.setItemsCanFocus(true);

        configureAdapter();
        return mListView;
    }

    public void setActions(ArrayList<ActionItem> actions) {
    	mActions = actions;
    }

    private void configureAdapter() {
        if (mActions == null || mListView == null) return;

        mListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mActions.size();
            }

            @Override
            public Object getItem(int position) {
                return mActions.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Set action title based on summary value
            	final ActionItem action = (ActionItem) getItem(position);
            	final String type = action.getType();
            	final View resultView = convertView != null ? convertView
            			: getActivity().getLayoutInflater().inflate(
            					QuickPeopleActivity.QUICK_TYPE_PHONE.equals(type) ?
            							R.layout.quick_action_phone_view :
            								R.layout.quick_action_email_view,
            								parent, false);
            	
            	final TextView contentView = (TextView) resultView.findViewById(R.id.content);
            	final String content = action.getTitle();
            	contentView.setText(content);
            	contentView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(QuickPeopleActivity.QUICK_TYPE_PHONE.equals(type)) {
							gotoCallDial(content);
						}else if(QuickPeopleActivity.QUICK_TYPE_EMAIL.equals(type)) {
							gotoEmail(content);
						}else if(ProfileActionGatewayActivity.isSensitiveActionList(type)) {
							gotoStartProfileActionGateWay(action);
						}else if(ProfileActionGatewayActivity.isInviteContactAction(type)){
							gotoStartInviteActivity(action);
						}else if(QuickPeopleActivity.QUICK_TYPE_EXCHANGEVCARD.equals(type)){
						    if(getString(R.string.friends_item_request_exchange).equals(content)) {
						        sendExchangeRequest(action);
						    }
						}else {
							Log.d(TAG, "have no this type : " + type);
						}
					}
				});
            	
            	final ImageView send_message = (ImageView) resultView.findViewById(R.id.send_message);
            	if(send_message != null){
            		send_message.setOnClickListener(new OnClickListener() {
            			@Override
            			public void onClick(View v) {
            				gotoMessage(content);
            			}
            		});
            	}
            	return resultView;
            }
        });
    }
    private void gotoCallDial(String phonenumber) {
    	Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phonenumber));
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
    
    private void gotoMessage(String phonenumber) {
    	Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + phonenumber));
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
    
    private void gotoEmail(String email) {
    	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        
        String mySubject = getString(R.string.email_comefrom_qiupu);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySubject);
        String myBodyText = "";
        
        startActivity(emailIntent);	
    }
    
    private void gotoStartProfileActionGateWay(ActionItem action) {
    	Intent intent = new Intent(mActivity, ProfileActionGatewayActivity.class);
    	intent.setAction(action.getType());
    	if(StringUtil.isValidString(action.getUserId())){
    		intent.putExtra("uid", action.getUserId());
    	}else {
    		String dataId = ContactUtils.getDataIdWithActionType(mActivity, action.getContactId(), action.getType());
    		Uri url =Uri.parse("content://com.android.contacts/data/" + dataId);
    		intent.setData(url);
    	}
    	startActivity(intent);
    }
    
    private void gotoStartInviteActivity(ActionItem action) {
    	Log.d(TAG, "gotoStartInviteActivity : " + action.getContactId());
    	Intent intent = new Intent(mActivity, InviteContactActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putString("contactId", action.getContactId());
    	intent.putExtras(bundle);
    	startActivity(intent);
    }
    
    public void sendExchangeRequest(ActionItem action) {

        synchronized (QiupuHelper.userlisteners) {
            Log.d(TAG,
                    "userlisteners.size() : "
                            + QiupuHelper.userlisteners.size());
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<UsersActionListner> ref = QiupuHelper.userlisteners
                        .get(key);
                if (ref != null && ref.get() != null) {
                    QiupuUser tmpUser = new QiupuUser();
                    tmpUser.uid = Long.parseLong(action.getUserId());
                    ref.get().sendRequest(tmpUser);
                }
            }
        }
        mActivity.finish();
    }
}
