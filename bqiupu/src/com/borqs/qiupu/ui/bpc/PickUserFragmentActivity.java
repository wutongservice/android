package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.adapter.PickUserAdapter;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.ContactUserFragment;
import com.borqs.qiupu.fragment.ContactUserFragment.CallBackContactuserFragmentListener;
import com.borqs.qiupu.fragment.FixedTabsView;
import com.borqs.qiupu.fragment.LocalUserListFragment;
import com.borqs.qiupu.fragment.LocalUserListFragment.CallBackLocalUserListFragmentListener;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;

public class PickUserFragmentActivity extends BasicActivity implements CallBackLocalUserListFragmentListener, 
                                    CallBackContactuserFragmentListener{
	private static final String TAG = "PickUserFragmentActivity";
	private PickUserAdapter mAdapter;
	private ViewPager mPager;
//	private TitlePageIndicator mIndicator;
	private EditText keyEdit;
	private int mCurrentpage;
	private String localOldEditContent;
	private String conactOldEditContent;
	private boolean isNoNeedDoSearch = false;
	private boolean selectAllLocalUser = false;
	private boolean selectAllContats = false;
	private LocalUserListFragment mLocalUserListFragment;
	private ContactUserFragment mContactUserFragment;
    private boolean mFromExchange = false;
    private String mUids;
    private int mCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_user_view);

        String from = getIntent().getStringExtra("from_exchange");
        mUids = getIntent().getStringExtra("uids");
        if ("from_exchange".equals(from)) {
            mFromExchange = true;
            setHeadTitle(R.string.qiupu_invite);
        } else {
            setHeadTitle(R.string.string_select_user);
        }

		mAdapter = new PickUserAdapter(getSupportFragmentManager(), this);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		
		FixedTabsView mIndicator = (FixedTabsView) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setAdapter(mAdapter);
        mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);
		
//		mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
//		mIndicator.setViewPager(mPager);
//		mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);

        keyEdit = (EditText) this.findViewById(R.id.search_span);
        if (null != keyEdit) {
            keyEdit.addTextChangedListener(new MyWatcher());
        }

		Button select_ok = (Button) this.findViewById(R.id.select_ok);
		Button select_cancel = (Button) this.findViewById(R.id.select_cancel);

		if (mFromExchange) {
		    select_ok.setText(R.string.public_circle_invite);
            select_ok.setOnClickListener(exchangeListener);

            queryFriendsNotExchangeVcard();
            if (mCount > 0) {
                showLeftActionBtn(true);
                select_ok.setClickable(true);
            } else {
                showLeftActionBtn(false);
                select_ok.setClickable(false);
            }
		} else {
		    showLeftActionBtn(true);
    		select_ok.setOnClickListener(doSelectClick);
		}

		select_cancel.setOnClickListener(doCancel);
		
		
		overrideLeftActionBtn(R.drawable.ic_btn_choice, selectAllClickListener);
		showRightActionBtn(false);

	}

	private void queryFriendsNotExchangeVcard() {
	    Cursor cursor = null;
	    try {
	        cursor = orm.queryUserNotInCircle(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
	        if (cursor != null && cursor.getCount() > 0) {
	            mCount = cursor.getCount();
	        }
	    } finally {
	        QiupuORM.closeCursor(cursor);
	    }
	}

	private View.OnClickListener exchangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String selectUseraddress = "";
            String selectCircleaddress = "";
            if(mLocalUserListFragment != null){
                selectUseraddress = parseLocalCircleUser(mLocalUserListFragment.getLocalSelectuser(), false);
                selectCircleaddress = parseLocalCircleUser(mLocalUserListFragment.getLocalSelectCircle(), true);
            }else{
                Log.d(TAG, "localUserLIstFreagment is null .");
            }

            ArrayList<ContactSimpleInfo> phoneList = new ArrayList<ContactSimpleInfo>();
            ArrayList<ContactSimpleInfo> emailList = new ArrayList<ContactSimpleInfo>();

            if(mContactUserFragment != null){
                HashSet<String> phoneSet = mContactUserFragment.getSelectPhone();
                if (phoneSet != null) {
                    Iterator<String> iter = phoneSet.iterator();
                    while(iter.hasNext()) {
                        ContactSimpleInfo csi = new ContactSimpleInfo();
                        csi.phone_number = iter.next();
                        phoneList.add(csi);
                    }
                }

                HashSet<String> emailSet = mContactUserFragment.getSeleceEmail();
                if (emailSet != null) {
                    Iterator<String> iter = emailSet.iterator();
                    while(iter.hasNext()) {
                        ContactSimpleInfo csi = new ContactSimpleInfo();
                        csi.email = iter.next();
                        emailList.add(csi);
                    }
                }
            }

            if(selectUseraddress.length() <= 0 && selectCircleaddress.length() <= 0 &&
                    phoneList.size() <= 0 && emailList.size() <= 0) {
                Toast.makeText(PickUserFragmentActivity.this, R.string.selected_one_user, Toast.LENGTH_SHORT).show();
            } else {
                if (selectUseraddress.length() > 0 || selectCircleaddress.length() > 0) {
                    circleUpdate(selectUseraddress, String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE), true);
                }

                if (phoneList.size() > 0) {
                    sendMessage(phoneList, true);
                }

                if (emailList.size() > 0) {
                    sendEmail(emailList, true);
                }
            }
        }
    };

    @Override
    protected void onMessageInvited() {
        finish();
    };

    @Override
    protected void onEmailInvited() {
        finish();
    };

    @Override
    protected void doUsersSetCallBack(String uid, boolean isadd) {
        finish();
    }

	@Override
	protected void createHandler() {
	}

	private class MyWatcher implements TextWatcher {
		public void afterTextChanged(Editable s) {
			// do search
		    if(isNoNeedDoSearch == false){
		        doSearch(s.toString().trim());
		    }
		    isNoNeedDoSearch = false;
		    changeOldEditTextContent(s.toString().trim());
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}

	private void doSearch(String key) {
		if(mCurrentpage == 0){
			if(mLocalUserListFragment != null) {
			    if (mFromExchange) {
			        mLocalUserListFragment.doSearch(key, mUids);
			    } else {
			        mLocalUserListFragment.doSearch(key);
			    }
				
			}
		}else if(mCurrentpage == 1){
			if(mContactUserFragment != null)
				mContactUserFragment.doSearch(key);
		}
	}

	View.OnClickListener doSelectClick = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			String selectUseraddress = "";
			String selectCircleaddress = "";
			if(mLocalUserListFragment != null){
				selectUseraddress = parseLocalCircleUser(mLocalUserListFragment.getLocalSelectuser(), false);
				selectCircleaddress = parseLocalCircleUser(mLocalUserListFragment.getLocalSelectCircle(), true);
			}else{
				Log.d(TAG, "localUserLIstFreagment is null .");
			}
			
			String selectPhone = "" ;
			String selectEmail = "" ;
			HashSet<Long> selectUid = new HashSet<Long>() ;
			if(mContactUserFragment != null){
				selectPhone = parsePhoneOrEmail(mContactUserFragment.getSelectPhone());
				StringUtil.stripMobilePhoneNumber(selectPhone);
				selectEmail = parsePhoneOrEmail(mContactUserFragment.getSeleceEmail());
				
				selectUid.addAll(mContactUserFragment.getSelectUid());
				
				if(QiupuConfig.LOGD)Log.d(TAG, "selected address : " + selectUseraddress + " " + selectCircleaddress
						+ "selectPhone: " + selectPhone + " selectEmail: " + selectEmail);
			}
			
			Intent data = new Intent();
			data.putExtra("address", selectUseraddress);
			data.putExtra("circles", selectCircleaddress);
			data.putExtra("phones", selectPhone);
			data.putExtra("emails", selectEmail);
			data.putExtra("contactids", selectUid);
			
			PickUserFragmentActivity.this.setResult(Activity.RESULT_OK, data);
			PickUserFragmentActivity.this.finish();
		}
	};
	
	View.OnClickListener doCancel = new View.OnClickListener() {
		
		public void onClick(View arg0){
			PickUserFragmentActivity.this.setResult(Activity.RESULT_CANCELED);
			PickUserFragmentActivity.this.finish();
		}
	};
	
	private String parseLocalCircleUser(HashSet<Long> set, boolean isgetCircles)
	{
		StringBuilder address = new StringBuilder();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			String id = String.valueOf(it.next());
			if(address.length()>0)
			{
				address.append(',');				    
			}
			
			if(isgetCircles)
			{
				address.append("#" + id);
			}
			else
			{
				address.append(id);
			}
			
		}
		return address.toString();
	}
	
	private String parsePhoneOrEmail(HashSet<String> set)
	{
		StringBuilder address = new StringBuilder();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			String id = String.valueOf(it.next());
			if(address.length()>0)
			{
				address.append(',');				    
			}
			
			address.append("*" + String.valueOf(id));
		}
		return address.toString();
	}
	
	ViewPager.OnPageChangeListener pagerOnPageChangeListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int page) {
			mCurrentpage = page;
			isNoNeedDoSearch = true;
			
			if(mCurrentpage == 0) {
			    if (mFromExchange) {
    			    if (mCount > 0) {
    			        showLeftActionBtn(true);
    			    } else {
    			        showLeftActionBtn(false);
    			        findViewById(R.id.select_ok).setClickable(false);
    			    }
			    } else {
			        showLeftActionBtn(true);
			        findViewById(R.id.select_ok).setClickable(true);
			    }
			} else {
			    showLeftActionBtn(false);
			    findViewById(R.id.select_ok).setClickable(true);
			}
			
			changeEditTextContent();
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		@Override
		public void onPageScrollStateChanged(int page) {
		}
	};
	
	private void changeOldEditTextContent(String content){
		if(mCurrentpage == 0){
			localOldEditContent = content;
		}else{
			conactOldEditContent = content;
		}
	}
    
    private void changeEditTextContent(){
        if(mCurrentpage == 0){
            keyEdit.setText(localOldEditContent);
        }else{
            keyEdit.setText(conactOldEditContent);
        }
        keyEdit.setSelection(keyEdit.getText().toString().length());
    }
    
    
    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        
    }
    
    View.OnClickListener selectAllClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCurrentpage == 0){
                selectAllLocalUser = !selectAllLocalUser; 
                setLeftActionImageRes(selectAllLocalUser ? R.drawable.ic_btn_choice_press : R.drawable.ic_btn_choice);
                mLocalUserListFragment.selectAllUser(selectAllLocalUser);
            }else if(mCurrentpage == 1){
                
            }
        }
    };
    
	@Override
	public void getLocalUserListFragment(LocalUserListFragment fragment) {
	    fragment.setFromType(mFromExchange);
	    mLocalUserListFragment = fragment;
	}

	@Override
	public void getContactuserFragment(ContactUserFragment fragment) {
		mContactUserFragment = fragment;		
	}
}
