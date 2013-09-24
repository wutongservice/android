package com.borqs.qiupu.ui.bpc;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.AddressPadNoteActionListener;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.PickAudienceBaseFragment;
import com.borqs.qiupu.fragment.PickAudienceBaseFragment.PickAudienceBaseFragmentCallBack;
import com.borqs.qiupu.fragment.PickAudienceEmailFragment;
import com.borqs.qiupu.fragment.PickAudienceEmailFragment.PickAudienceEmailFragmentCallbackListener;
import com.borqs.qiupu.fragment.PickAudiencePhoneFragment;
import com.borqs.qiupu.fragment.PickAudiencePhoneFragment.PickPeoplePhoneFragmentCallbackListener;
import com.borqs.qiupu.fragment.PickFriendsFragment;
import com.borqs.qiupu.fragment.PickFriendsFragment.CallBackPickFriendsFragmentListener;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class PickAudienceActivity extends BasicActivity implements PickAudienceBaseFragmentCallBack, CallBackPickFriendsFragmentListener, 
                PickPeoplePhoneFragmentCallbackListener, PickAudienceEmailFragmentCallbackListener, AddressPadNoteActionListener{
	private static final String TAG = "PickAudienceActivity";
	private FragmentManager mFragmentManager;
	private PickAudienceBaseFragment mPickAudienceBaseFragment;
	private PickFriendsFragment mPickFriendsFragment;
	private PickAudiencePhoneFragment mPickContactPhoneFragment;
	private PickAudienceEmailFragment mPickContactEmailFragment;
	
	private final static String PICK_MAIN_TAG = "PICK_MAIN_TAG";
	private final static String PICK_PHONE_TAG = "PICK_PHONE_TAG";
	private final static String PICK_EMAIL_TAG = "PICK_EMAIL_TAG";
	private int mCurrentFragment;
    private static final int fragment_main = 1;
    private static final int fragment_phone = 2;
    private static final int fragment_email = 3;
    
    public final static String RECEIVE_ADDRESS = "RECEIVE_ADDRESS";
    public final static String RECEIVE_SELECTUSERCIRCLE_NAME = "RECEIVE_SELECTUSERCIRCLE_NAME";
    public final static String RECEIVE_SELECTPHONEEMAIL_NAME = "RECEIVE_SELECTPHONEEMAIL_NAME";
    protected String[] receiveUserAdds;
    
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
    private HashSet<Long> mSelectedCircle = new HashSet<Long>();
    private HashSet<String> mSelectedEmail = new HashSet<String>();
    private HashSet<String> mSelectedPhone = new HashSet<String>();
    private HashMap<Long, String> mSelectedUserCircleNameMap = new HashMap<Long, String>();
    private HashMap<String, String> mSelectedPhoneEmailNameMap = new HashMap<String, String>();
    
    public final static int PICK_FROM_EVNET = 1;
    public final static int PICK_FROM_COMPOSE = 2;
    public final static int PICK_FROM_POLL = 3;
    public final static int PICK_FROM_CREATE_CIRCLE = 4;
    public final static String PICK_FROM = "PICK_FROM";
    
    private long mInviteId;
    private Dialog mprogressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_people_main);

		showLeftActionBtn(false);
		showRightActionBtn(false);
		showRightTextActionBtn(true);

		AddressPadMini.registerNoteActionListener(getClass().getName(), this);
		parseIntent();
		mFragmentManager = getSupportFragmentManager();
		mCurrentFragment = fragment_main;
		mPickFriendsFragment = new PickFriendsFragment();
		mFragmentManager.beginTransaction().add(R.id.request_container, mPickFriendsFragment, PICK_MAIN_TAG).commit();
//		mHandler.post(new Runnable() {
//    		@Override
//    		public void run() {
    			generateAllSelectData(receiveUserAdds);
//    		}
//    	});
	}

	private void parseIntent() {
		Intent intent = getIntent();
		receiveUserAdds = intent.getStringArrayExtra(RECEIVE_ADDRESS);
		HashMap<Long, String> tmpUserMap = (HashMap<Long, String>) intent.getSerializableExtra(RECEIVE_SELECTUSERCIRCLE_NAME);
		HashMap<String, String> tmpPhoneEmailMap = (HashMap<String, String>) intent.getSerializableExtra(RECEIVE_SELECTPHONEEMAIL_NAME);
		if(tmpUserMap != null) {
			mSelectedUserCircleNameMap.clear();
			mSelectedUserCircleNameMap.putAll(tmpUserMap);
		}
		if(tmpPhoneEmailMap != null) {
			mSelectedPhoneEmailNameMap.clear();
			mSelectedPhoneEmailNameMap.putAll(tmpPhoneEmailMap);
		}
		mInviteId = intent.getLongExtra(CircleUtils.CIRCLE_ID, -1);
		if(mInviteId > 0) {
			overrideRightTextActionBtn(R.string.qiupu_invite, inviteClickListener);
		}else {
			overrideRightTextActionBtn(R.string.label_ok, pickClickListener);
		}
		if(intent.getIntExtra(PICK_FROM, -1) == PICK_FROM_COMPOSE) {
			setHeadTitle(R.string.string_select_user);
		}else {
			setHeadTitle(R.string.invite_people_title);
		}
	}
	
	View.OnClickListener inviteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	invitePeople();
        }
    };
    
	View.OnClickListener pickClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mPickFriendsFragment != null) {
        		Intent data = new Intent();
    			data.putExtra(RECEIVE_ADDRESS, mPickFriendsFragment.getAllSelectString());
    			data.putExtra(RECEIVE_SELECTUSERCIRCLE_NAME, mSelectedUserCircleNameMap);
    			data.putExtra(RECEIVE_SELECTPHONEEMAIL_NAME, mSelectedPhoneEmailNameMap);
    			PickAudienceActivity.this.setResult(Activity.RESULT_OK, data);
    			PickAudienceActivity.this.finish();
        	}else {
        		Log.d(TAG, "pick friends fragment is null");
        	}
        }
    };
    
	@Override
	protected void createHandler() {
	    mHandler = new MainHandler();
	}

	@Override
	public void getPickFriendsFragment(PickFriendsFragment fragment) {
		mPickFriendsFragment = fragment;
	}

	@Override
	public void gotoPickContactFragment(int type) {
		Log.d(TAG, "gotoPickContactFragment: " + type);
		if(type == PickAudienceBaseFragment.PICK_TYPE_PHONE) {
			if(mCurrentFragment != fragment_phone){
				showSlideToggle(overrideSlideToggleClickListener);
				mCurrentFragment = fragment_phone;
				if(mPickFriendsFragment != null && !mPickFriendsFragment.isHidden()) {
					mFragmentManager.beginTransaction().hide(mPickFriendsFragment).commit();
				}
				mPickContactPhoneFragment = (PickAudiencePhoneFragment) mFragmentManager.findFragmentByTag(PICK_PHONE_TAG);
				if(mPickContactPhoneFragment == null) {
					mPickContactPhoneFragment = new PickAudiencePhoneFragment();
					mFragmentManager.beginTransaction().add(R.id.request_container, mPickContactPhoneFragment, PICK_PHONE_TAG).commit();
				}else {
					mFragmentManager.beginTransaction().show(mPickContactPhoneFragment).commit();
					mPickContactPhoneFragment.setAddress();
				}
			}
		}else if(type == PickAudienceBaseFragment.PICK_TYPE_EMAIL) {
			if(mCurrentFragment != fragment_email){
				showSlideToggle(overrideSlideToggleClickListener);
				mCurrentFragment = fragment_email;
				if(mPickFriendsFragment != null && !mPickFriendsFragment.isHidden()) {
					mFragmentManager.beginTransaction().hide(mPickFriendsFragment).commit();
				}
				mPickContactEmailFragment = (PickAudienceEmailFragment) mFragmentManager.findFragmentByTag(PICK_EMAIL_TAG);
				if(mPickContactEmailFragment == null) {
					mPickContactEmailFragment = new PickAudienceEmailFragment();
					mFragmentManager.beginTransaction().add(R.id.request_container, mPickContactEmailFragment, PICK_EMAIL_TAG).commit();
				}else {
					mFragmentManager.beginTransaction().show(mPickContactEmailFragment).commit();
					mPickContactEmailFragment.setAddress();
				}
			}
		}
	}
	
	View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			handlerBackKey();
		}
	};
	
	private void handlerBackKey() {
		if(mCurrentFragment == fragment_phone){
			mCurrentFragment = fragment_main;
			if(mPickContactPhoneFragment != null && !mPickContactPhoneFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mPickContactPhoneFragment).commit();
			}
			mPickFriendsFragment = (PickFriendsFragment) mFragmentManager.findFragmentByTag(PICK_MAIN_TAG);
			if(mPickFriendsFragment == null){
				mPickFriendsFragment = new PickFriendsFragment();
				mFragmentManager.beginTransaction().add(R.id.request_container, mPickFriendsFragment, PICK_MAIN_TAG).commit();
			}else {
				mFragmentManager.beginTransaction().show(mPickFriendsFragment).commit();
				mPickFriendsFragment.setAddress();
			}
		} else if(mCurrentFragment == fragment_email) {
			mCurrentFragment = fragment_main;
			if(mPickContactEmailFragment != null && !mPickContactEmailFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mPickContactEmailFragment).commit();
			}
			mPickFriendsFragment = (PickFriendsFragment) mFragmentManager.findFragmentByTag(PICK_MAIN_TAG);
			if(mPickFriendsFragment == null){
				mPickFriendsFragment = new PickFriendsFragment();
				mFragmentManager.beginTransaction().add(R.id.request_container, mPickFriendsFragment, PICK_MAIN_TAG).commit();
			}else {
				mFragmentManager.beginTransaction().show(mPickFriendsFragment).commit();
				mPickFriendsFragment.setAddress();
			}
		}else {
			if(mCurrentFragment == fragment_main) {
				if(mPickFriendsFragment != null) {
					if(mPickFriendsFragment.isBackSearch()) {
						Log.d(TAG, "is back from search mode");
					}else {
						setResult(Activity.RESULT_CANCELED);
						super.onBackPressed();
					}
				}else {
					setResult(Activity.RESULT_CANCELED);
					super.onBackPressed();
				}
			}else {
				setResult(Activity.RESULT_CANCELED);
				super.onBackPressed();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		handlerBackKey();
	}
	
	@Override
	protected void onDestroy() {
		if(mPickFriendsFragment != null) {
			mPickFriendsFragment.clearAddress();
		}
		AddressPadMini.unregisterNoteActionListener(getClass().getName());
		super.onDestroy();
	}
	
	private void generateAllSelectData(final String[] receiveAddr) {
		mSelectedUser.clear();
		mSelectedCircle.clear();
		mSelectedEmail.clear();
		mSelectedPhone.clear();
		
		if (receiveAddr != null) {
			for (int i = 0; i < receiveAddr.length; i++) {
				String id = receiveAddr[i];
				if (id.contains("#")) {
					int index = id.indexOf("#");
					id = id.substring(index + 1, id.length());

					mSelectedCircle.add(Long.parseLong(id));
				} else if(id.startsWith("*") && id.contains("@")) {
					int index = id.indexOf("*");
					id = id.substring(index + 1, id.length());
					mSelectedEmail.add(id);
				} else if(id.startsWith("*") && !id.contains("@")){
					int index = id.indexOf("*");
					id = id.substring(index + 1, id.length());

					mSelectedPhone.add(id);
				}else {
					try {
						mSelectedUser.add(Long.parseLong(id));

					} catch (Exception e) {
						Log.d(TAG, "initSelectUser : it is not id ");
					}
				}
			}
			if(mPickFriendsFragment != null) {
				PickAudienceBaseFragment.mainSelectAdds = mPickFriendsFragment.getselectCircleUserValue();
				PickAudienceBaseFragment.contactEmailSelectAdds = mPickFriendsFragment.getSelectValue(mSelectedEmail);
				PickAudienceBaseFragment.contactPhoneSelectAdds = mPickFriendsFragment.getSelectValue(mSelectedPhone);
			}
		}
	}

	@Override
	public void getPickPeoplePhoneFragment(PickAudiencePhoneFragment fragment) {
		mPickContactPhoneFragment = fragment;
	}

	@Override
	public HashSet<String> getSelectPhone() {
		return mSelectedPhone;
	}

	@Override
	public void getPickAudienceEmailFragment(PickAudienceEmailFragment fragment) {
		mPickContactEmailFragment = fragment;
	}

	@Override
	public HashSet<String> getSelectEmail() {
		return mSelectedEmail;
	}

	@Override
	public HashSet<Long> getSelectUser() {
		return mSelectedUser;
	}

	@Override
	public HashSet<Long> getSelectCircle() {
		return mSelectedCircle;
	}

	@Override
	public HashMap<String, String> getSelectPhoneEmailNameMap() {
		return mSelectedPhoneEmailNameMap;
	}

	@Override
	public void getPickAduienceBaseFragment(PickAudienceBaseFragment fragment) {
		mPickAudienceBaseFragment = fragment;
	}

	@Override
	public HashMap<Long, String> getSelectUserCircleNameMap() {
		return mSelectedUserCircleNameMap;
	}

	@Override
	public void noteRemove(String notStr) {
		if(QiupuConfig.DBLOGD)Log.d(TAG, "noteRemove: " + notStr);
		if(mPickFriendsFragment != null && mPickContactEmailFragment == null) {
			if(notStr.startsWith("*") && notStr.contains("@")) {
				int index = notStr.indexOf("*");
				notStr = notStr.substring(index + 1, notStr.length());
				mSelectedEmail.remove(notStr);
			}
			PickAudienceBaseFragment.contactEmailSelectAdds = mPickFriendsFragment.getSelectValue(mSelectedEmail);
		}
		if(mPickFriendsFragment != null && mPickContactPhoneFragment == null) {
			if(notStr.startsWith("*") && !notStr.contains("@")){
				int index = notStr.indexOf("*");
				notStr = notStr.substring(index + 1, notStr.length());
				mSelectedPhone.remove(notStr);

			}
			PickAudienceBaseFragment.contactPhoneSelectAdds = mPickFriendsFragment.getSelectValue(mSelectedPhone);
		}
	}
	
	private String getAllSelectName () {
		StringBuilder tmpString = new StringBuilder();
        Iterator iter = mSelectedUserCircleNameMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String val = (String) entry.getValue();
            if (tmpString.length() > 0) {
                tmpString.append(",");
            }
            tmpString.append(val);
        }
        if(tmpString.length() > 0) {
        	tmpString.append(",");
        }
        
        Iterator iter1 = mSelectedPhoneEmailNameMap.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry entry = (Map.Entry) iter1.next();
            String val = (String) entry.getValue();
            if (tmpString.length() > 0) {
                tmpString.append(",");
            }
            tmpString.append(val);
        }
        return tmpString.toString();
	}
	
	private boolean mSendEmailStatus = true;
    private boolean mSendSmsStatus = false;
	private void invitePeople() {
		if(mPickFriendsFragment == null) {
			return ;
		}
        final String ids = mPickFriendsFragment.getAllSelectString();
        final String names = /*mPickFriendsFragment.getSelectNames()*/getAllSelectName();
        if (QiupuConfig.LOGD) Log.d(TAG, "selectedValue : " + ids + " selectName: " + names);
        if (ids.length() > 0) {
            final Dialog dialog = new Dialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            View view = getLayoutInflater().inflate(R.layout.invitation_composer, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.invitation_message_title);
            final TextView recipientView = (TextView) dialog.findViewById(R.id.invitation_recipient);
            if (null != recipientView) {
                recipientView.setText(names);
            }

            final CheckBox sendEmail = (CheckBox) dialog.findViewById(R.id.send_email_checkbox);
            sendEmail.setVisibility(View.VISIBLE);
            if (null != sendEmail) {
                sendEmail.setChecked(mSendEmailStatus);
                sendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSendEmailStatus = !mSendEmailStatus;
                        sendEmail.setChecked(mSendEmailStatus);
                    }
                });
            }

            final CheckBox sendSms = (CheckBox) dialog.findViewById(R.id.send_sms_checkbox);
            sendSms.setVisibility(View.VISIBLE);
            if (null != sendSms) {
                sendSms.setChecked(mSendSmsStatus);
                sendSms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSendSmsStatus = !mSendSmsStatus;
                        sendSms.setChecked(mSendSmsStatus);
                    }
                });
            }

            final TextView messageView = (TextView) dialog.findViewById(R.id.invitation_message);
            if (null != messageView) {
                final View sendView = dialog.findViewById(R.id.invitation_btn_send);
                if (null != sendView) {
                    sendView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String message = messageView.getText().toString();
//                            if (TextUtils.isEmpty(message)) {
//                                Toast.makeText(PickAudienceActivity.this, R.string.toast_invalid_input_content, Toast.LENGTH_SHORT).show();
//                                messageView.requestFocus();
//                            } else {
                                invitePeopleInPublicCircle(String.valueOf(mInviteId), ids, names, message, mSendEmailStatus, mSendSmsStatus);
                                dialog.dismiss();
//                            }
                        }
                    });
                }
                final View skipView = dialog.findViewById(R.id.invitation_btn_skip);
                if (null != skipView) {
                    skipView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            final String message = messageView.getText().toString();
//                            if (TextUtils.isEmpty(message)) {
//                                invitePeopleInPublicCircle(String.valueOf(mInviteId), ids, names, "", mSendEmailStatus, mSendSmsStatus);
                                dialog.dismiss();
//                            } else {
//                                DialogUtils.showConfirmDialog(PickAudienceActivity.this, R.string.invitation_discard_title,
//                                        R.string.invitation_discard_message,
//                                        R.string.label_ok, R.string.label_cancel,
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                invitePeopleInPublicCircle(String.valueOf(mInviteId), ids, names, "", mSendEmailStatus, mSendSmsStatus);
//                                                dialog.dismiss();
//                                            }
//                                        }
//                                );
//                            }
                        }
                    });
                }
            }
            dialog.show();
        } else {
            ToastUtil.showCustomToast(PickAudienceActivity.this, R.string.invite_have_no_select_toast);
        }
    }
	
	boolean inInvitePeople;
    Object mLockInvitePeople = new Object();
    private void invitePeopleInPublicCircle(final String circleId, final String toids, final String toNames, final String message,
            final boolean sendEmail, final boolean sendSms) {
        if (inInvitePeople == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockInvitePeople) {
            inInvitePeople = true;
        }

        mprogressDialog = DialogUtils.showProgressDialog(this, -1, getString(R.string.invite_progress_dialog_message));

        asyncQiupu.publicInvitePeople(AccountServiceUtils.getSessionID(), circleId, toids, toNames, message,
                sendEmail, sendSms, new TwitterAdapter() {
            public void publicInvitePeople(ArrayList<Long> joinIds) {
                Log.d(TAG, "finish invitePeopleInPublicCircle=" + joinIds.size());
                invitePeopleCallback(joinIds);

                Message msg = mHandler.obtainMessage(INVIT_PEOPLE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockInvitePeople) {
                    inInvitePeople = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockInvitePeople) {
                    inInvitePeople = false;
                }
                Message msg = mHandler.obtainMessage(INVIT_PEOPLE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    private void invitePeopleCallback(ArrayList<Long> joinIds) {
        if(joinIds.size() > 0) {
        	StringBuilder ids = new StringBuilder();
        	for(int i=0; i<joinIds.size(); i++) {
        		if(ids.length() > 0) {
        			ids.append(",");
        		}
        		ids.append(joinIds.get(i));
        	}
        	if(QiupuConfig.LOGD)Log.d(TAG, "invitePeopleCallback: " + ids.toString());
            orm.updateInviteIds(ids.toString(), mInviteId);
        }
    }
    
    private static final int INVIT_PEOPLE_END = 101;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INVIT_PEOPLE_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        Log.d(TAG, "invite people end ");
                        ToastUtil.showOperationOk(PickAudienceActivity.this, mHandler, true);
                        updateActivityUI();
                        PickAudienceActivity.this.finish();
                    } else {
                        ToastUtil.showOperationFailed(PickAudienceActivity.this, mHandler, true);
                    }
                    break;
                }
            }
        }
    }
    
    public static final HashMap<String, WeakReference<inviteListeners>> invitelisteners = new HashMap<String, WeakReference<inviteListeners>>();

    public static void registerInviteListener(String key, inviteListeners listener) {
        synchronized (invitelisteners) {
            WeakReference<inviteListeners> ref = invitelisteners.get(key);
            if (ref != null && ref.get() != null) {
                ref.clear();
            }
            invitelisteners.put(key, new WeakReference<inviteListeners>(
                    listener));
        }
    }

    public static void unregisterInviteListener(String key) {
        synchronized (invitelisteners) {
            WeakReference<inviteListeners> ref = invitelisteners.get(key);
            if (ref != null && ref.get() != null) {
                ref.clear();
            }
            invitelisteners.remove(key);
        }
    }
    
    public interface inviteListeners {
        public void refreshInfo();
    }

    private void updateActivityUI() {
        synchronized (invitelisteners) {
            Log.d(TAG, "invitelisteners.size() : " + invitelisteners.size());
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<inviteListeners> ref = invitelisteners.get(key);
                if (ref != null && ref.get() != null) {
                	inviteListeners listener = ref.get();
                    if (listener != null) {
                        listener.refreshInfo();
                    }
                }
            }
        }
    }
}
