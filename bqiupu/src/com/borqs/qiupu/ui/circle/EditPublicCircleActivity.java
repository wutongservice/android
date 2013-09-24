package com.borqs.qiupu.ui.circle;

import java.util.HashMap;

import twitter4j.Circletemplate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.CreateEventFragment;
import com.borqs.qiupu.fragment.CreateEventFragment.CreateEventFragmentCallBack;
import com.borqs.qiupu.fragment.CreatePublicCircleFragment;
import com.borqs.qiupu.fragment.CreatePublicCircleFragment.CreatePublicCircleFragmentCallBack;
import com.borqs.qiupu.fragment.EditGroupBaseFragment;
import com.borqs.qiupu.fragment.EditGroupBaseFragment.EditGroupBaseFragmentCallBack;
import com.borqs.qiupu.fragment.EditGroupBaseInfoFragment;
import com.borqs.qiupu.fragment.EditGroupBaseSetFragment;
import com.borqs.qiupu.fragment.EditGroupContactInfoFragment;
import com.borqs.qiupu.fragment.EditPublicCircleReceiveSetFragment;
import com.borqs.qiupu.fragment.EditPublicCircleReceiveSetFragment.ReceiveSetFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;
import com.borqs.qiupu.*;

public class EditPublicCircleActivity extends BasicActivity implements EditGroupBaseFragmentCallBack
                                        , CreatePublicCircleFragmentCallBack, ReceiveSetFragmentCallBack, CreateEventFragmentCallBack{

	private final static String TAG = "EditPublicCircleActivity";
//	public static final String ISCREATE = "ISCREATE";
	private int mType;
	private UserCircle mCircle;
	
	public static final int type_create = 0;
	public static final int eidt_type_base_info = 1;
	public static final int eidt_type_base_set = 2;
	public static final int eidt_type_people_manager = 3;
	public static final int eidt_type_contact_info = 4;
	public static final int edit_type_receive_set = 5;
	public static final int type_create_event = 6;
	public EditGroupBaseFragment mEditGroupBaseFragment;
	public CreatePublicCircleFragment mCreatePublicCircleFragment;
	public EditPublicCircleReceiveSetFragment mEditPublicCircleReceiveSetFragment;
	public CreateEventFragment mCreateEventFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.public_circle_action_ui);
		
		showRightActionBtn(false);
		showRightTextActionBtn(true);

		Intent intent = getIntent();
		mType = intent.getIntExtra(CircleUtils.EdIT_TYPE, -1);
		FragmentManager frag = getSupportFragmentManager();
		
    	if(type_create == mType) {
    		String tmpSubtype = getIntent().getStringExtra("subtype");
    		if(Circletemplate.TEMPLATE_FORMAL_NAME.equals(tmpSubtype)) {
    			setHeadTitle(R.string.create_organization_title);	
    		}else if(Circletemplate.TEMPLATE_NAME_CLASS.equals(tmpSubtype)) {
    			setHeadTitle(R.string.create_class_title);
    		}else if(Circletemplate.TEMPLATE_NAME_DEPARTMENT.equals(tmpSubtype)) {
    			setHeadTitle(R.string.create_department_title);
    		}else if(Circletemplate.TEMPLATE_NAME_PROJECT.equals(tmpSubtype)) {
    			setHeadTitle(R.string.child_circle_label_project);
    		}else if(Circletemplate.TEMPLATE_NAME_APPLICATION.equals(tmpSubtype)) {
    			setHeadTitle(R.string.child_circle_label_application);
    		}else {
    			setHeadTitle(R.string.create_public_circle_title);
    		}
    		overrideRightTextActionBtn(R.string.create, createClickLisetner);
    		mCreatePublicCircleFragment = (CreatePublicCircleFragment) frag.findFragmentById(R.id.request_container);
    		if(mCreatePublicCircleFragment == null) {
    			mCreatePublicCircleFragment = new CreatePublicCircleFragment();
    			frag.beginTransaction().add(R.id.request_container, mCreatePublicCircleFragment).commit();
    		}
    		
    	}else if(eidt_type_base_info == mType) {
    	    setHeadTitle(R.string.edit_base_info_title);
    	    overrideRightTextActionBtn(R.string.label_save, editClickListener);
    	    mCircle = (UserCircle) intent.getSerializableExtra(CircleUtils.CIRCLEINFO);
    	    mEditGroupBaseFragment = (EditGroupBaseFragment) frag.findFragmentById(R.id.request_container);
    	    if(mEditGroupBaseFragment == null) {
    	    	mEditGroupBaseFragment = new EditGroupBaseInfoFragment();
    	    	frag.beginTransaction().add(R.id.request_container, mEditGroupBaseFragment).commit();
    	    }
    	}else if(eidt_type_base_set == mType) {
    	    setHeadTitle(R.string.edit_base_set_title);
    	    overrideRightTextActionBtn(R.string.label_save, editClickListener);
    	    mCircle = (UserCircle) intent.getSerializableExtra(CircleUtils.CIRCLEINFO);
    	    mEditGroupBaseFragment = (EditGroupBaseFragment) frag.findFragmentById(R.id.request_container);
    	    if(mEditGroupBaseFragment == null) {
    	    	mEditGroupBaseFragment = new EditGroupBaseSetFragment();
    	    	frag.beginTransaction().add(R.id.request_container, mEditGroupBaseFragment).commit();
    	    }
        }else if(eidt_type_people_manager == mType) {
//            mCircle = (UserCircle) intent.getSerializableExtra(CIRCLEINFO);
//            EditGroupPeopleManagerFragment peopleManagerFragment = new EditGroupPeopleManagerFragment();
//            frag.beginTransaction().add(R.id.request_container, peopleManagerFragment).commit();
        }else if(eidt_type_contact_info == mType) {
    		setHeadTitle(R.string.edit_contact_info_title);
    		overrideRightTextActionBtn(R.string.label_save, editClickListener);
    		mCircle = (UserCircle) intent.getSerializableExtra(CircleUtils.CIRCLEINFO);
    		mEditGroupBaseFragment = (EditGroupBaseFragment) frag.findFragmentById(R.id.request_container);
    	    if(mEditGroupBaseFragment == null) {
    	    	mEditGroupBaseFragment = new EditGroupContactInfoFragment();
    	    	frag.beginTransaction().add(R.id.request_container, mEditGroupBaseFragment).commit();
    	    }
    	}else if(edit_type_receive_set == mType) {
    		mCircle = (UserCircle) intent.getSerializableExtra(CircleUtils.CIRCLEINFO);
    		setHeadTitle(mCircle != null ? mCircle.name : "");
    		overrideRightTextActionBtn(R.string.label_save, editReceiveSetClickListener);
    		mEditPublicCircleReceiveSetFragment = (EditPublicCircleReceiveSetFragment) frag.findFragmentById(R.id.request_container);
    		if(mEditPublicCircleReceiveSetFragment == null) {
    			mEditPublicCircleReceiveSetFragment = new EditPublicCircleReceiveSetFragment();
    			frag.beginTransaction().add(R.id.request_container, mEditPublicCircleReceiveSetFragment).commit();
    		}
    	}else if(type_create_event == mType) {
    		setHeadTitle(R.string.create_new_event_title);
    		overrideRightTextActionBtn(R.string.create, createEventClickLisetner);
    		mCreateEventFragment = (CreateEventFragment) frag.findFragmentById(R.id.request_container);
    		if(mCreateEventFragment == null) {
    			mCreateEventFragment = new CreateEventFragment();
    			frag.beginTransaction().add(R.id.request_container, mCreateEventFragment).commit();
    		}
    	}else {
    	    Log.d(TAG, "have no this type");
    	}
    	if(mCircle == null) {
    		mCircle = new UserCircle();
    	}
	}
	
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	private final static int EDIT_PUBLIC_END = 1;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EDIT_PUBLIC_END: {
                try {
                    dismissDialog(DIALOG_SET_USER_PROCESS);
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                    QiupuHelper.updateCirclesUI();
                    showOperationSucToast(true);
                    EditPublicCircleActivity.this.finish();
                } else {
                    showOperationFailToast("", true);
                }
                break;
            }
            }
        }
    }

	boolean inEditPublicInfo;
    Object mLockEditPublicInfo = new Object();
    public void editPublicCircle(final UserCircle tmpCircle, final HashMap<String, String> map) {
        if (inEditPublicInfo == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockEditPublicInfo) {
            inEditPublicInfo = true;
        }
        
        showDialog(DIALOG_SET_USER_PROCESS);
        
        asyncQiupu.editPulbicCircle(AccountServiceUtils.getSessionID(), map, QiupuConfig.isEventIds(tmpCircle.circleid), new TwitterAdapter() {
            public void editPulbicCircle(boolean suc) {
                Log.d(TAG, "finish editPulbicCircle=" + suc);
                
                if(suc) {
                    orm.updateCircleInfo(tmpCircle);
                }
                
                Message msg = mHandler.obtainMessage(EDIT_PUBLIC_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();
                synchronized (mLockEditPublicInfo) {
                    inEditPublicInfo = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockEditPublicInfo) {
                    inEditPublicInfo = false;
                }
                Message msg = mHandler.obtainMessage(EDIT_PUBLIC_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
//    View.OnClickListener bottomViewClickListener = new OnClickListener() {
//        public void onClick(View v) {
//            if(mEditGroupBaseFragment != null) {
//                HashMap<String, String> tmpMap = mEditGroupBaseFragment.getEditGroupMap();
//                if(tmpMap != null) {
//                    if(tmpMap.size() > 0) {
//                        tmpMap.put("id", String.valueOf(mCircle.circleid));
//                        editPublicCircle(mEditGroupBaseFragment.getCopyCircle(), tmpMap);
//                    }else {
//                        EditPublicCircleActivity.this.finish();
//                    }
//                }else {
//                    Log.d(TAG, "editGroupBaseFragment map is null ");
//                }
//            }
//        }
//    };
    
	@Override
	public UserCircle getCircleInfo() {
		return mCircle;
	}

	@Override
	public void getCreatePublicCircleFragment(CreatePublicCircleFragment fragment) { }

	@Override
	public String getCircleName() {
		return getIntent().getStringExtra(CircleUtils.CIRCLE_NAME);
	}

    @Override
    public void getEditGroupBaseFragment(EditGroupBaseFragment fragment) {
        mEditGroupBaseFragment = fragment;        
    }

	@Override
	public void getREceiveSetFragment(
			EditPublicCircleReceiveSetFragment fragment) {
		
	}

	@Override
	public long getCircleId() {
		return mCircle != null ? mCircle.circleid : -1;
	}
	
	View.OnClickListener editClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mEditGroupBaseFragment != null) {
                HashMap<String, String> tmpMap = mEditGroupBaseFragment.getEditGroupMap();
                if(tmpMap != null) {
                    if(tmpMap.size() > 0) {
                        tmpMap.put("id", String.valueOf(mCircle.circleid));
                        editPublicCircle(mEditGroupBaseFragment.getCopyCircle(), tmpMap);
                    }else {
                        EditPublicCircleActivity.this.finish();
                    }
                }else {
                    Log.d(TAG, "editGroupBaseFragment map is null ");
                }
            }
        }
    };
    
    View.OnClickListener createClickLisetner = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mCreatePublicCircleFragment != null) {
        		mCreatePublicCircleFragment.createPublicCircle();
            }
        }
    };
    
    View.OnClickListener createEventClickLisetner = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mCreateEventFragment != null) {
        		mCreateEventFragment.showPrivacyDialog();
            }
        }
    };
    
    
    View.OnClickListener editReceiveSetClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mEditPublicCircleReceiveSetFragment != null) {
        		mEditPublicCircleReceiveSetFragment.editReceiveSet();
            }
        }
    };

	@Override
	public void getCreateEventFragment(CreateEventFragment fragment) {
		mCreateEventFragment = fragment;
	}

	@Override
	public String getCompanyId() {
		// TODO Auto-generated method stub
		return getIntent().getStringExtra(CircleUtils.COMPANY_ID);
	}
}
