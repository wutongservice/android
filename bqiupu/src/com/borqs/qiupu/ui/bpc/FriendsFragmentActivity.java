package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.FriendsListFragment;
import com.borqs.qiupu.fragment.FriendsListFragment.FriendsListFragmentCallBackListener;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class FriendsFragmentActivity extends BasicNavigationActivity implements
                                 OnListItemClickListener, 
                                 FriendsListFragmentCallBackListener, UsersActionListner {

    private final static String TAG = "FriendsFragmentActivity";
    private long mUserid;
    private FriendsListFragment mFriendsListFragment;
    private FragmentManager mFragmentManager;
    private ArrayList<UserCircle> localCircleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.default_fragment_activity);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        orm = QiupuORM.getInstance(this);
        localCircleList =  orm.queryLocalCircleList();
//        showTitleSpinnerIcon(true);
        setHeadTitle(R.string.friends_all);

        QiupuHelper.registerUserListener(getClass().getName(), this);
        parseActivityIntent(getIntent());

        
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mFriendsListFragment = new FriendsListFragment();
        ft.add(R.id.fragment_content, mFriendsListFragment);
        ft.commit();


//        showLeftActionBtn(true);
//        showMiddleActionBtn(true);
//        overrideMiddleActionBtn(R.drawable.actionbar_icon_search_normal, searchClickListener);
//        overrideRightActionBtn(R.drawable.friend_group_icon, addCircleListener);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
        

    }
    
//    View.OnClickListener addCircleListener = new OnClickListener() {
//        public void onClick(View v) {
//        	IntentUtil.startCircleActivity(FriendsFragmentActivity.this, mUserid);
//        }
//    };
//    
//    View.OnClickListener searchClickListener = new OnClickListener() {
//		public void onClick(View v) {
//			loadSearch();
//		}
//	};

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch() 
    {
    	showSearhView();
//        gotoSearchActivity();
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                mUserid = bundle.getLong(BpcApiUtils.User.USER_ID, -1L);
                if (mUserid == -1L) {
                    mUserid = AccountServiceUtils.getBorqsAccountID();
                    // mUserName = AccountServiceUtils.getAccountNickName();
                } else {
                    mUserid = bundle.getLong(BpcApiUtils.User.USER_ID);
                    // mUserName = bundle.getString(USER_NICKNAME);
                }
//                mConcernType = bundle.getInt(USER_CONCERN_TYPE, QiupuConfig.USER_INDEX_FRIENDS);
            } else {
                mUserid = AccountServiceUtils.getBorqsAccountID();
                // mUserName = AccountServiceUtils.getAccountNickName();
            }
        } else {
            final String uid = BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARCH_KEY_UID);
            if (TextUtils.isEmpty(uid) || !TextUtils.isDigitsOnly(uid)) {
                mUserid = AccountServiceUtils.getBorqsAccountID();
                // mUserName = AccountServiceUtils.getAccountNickName();
            } else {
                mUserid = Long.parseLong(uid);
                final String tab = BpcApiUtils.parseSchemeValue(intent,
                        BpcApiUtils.SEARCH_KEY_TAB);
//                mConcernType = TextUtils.isEmpty(tab) ? 0 : Integer
//                        .parseInt(tab);
            }
        }
    }
    
    
    
    public long getUserId(){
        return mUserid;
    }
    
    @Override
    protected void loadRefresh() {
       if(null != mFriendsListFragment){
            mFriendsListFragment.loadRefresh(true);
        }
    }

    @Override
    protected void uiLoadEnd()  {
        if(mFriendsListFragment != null && mFriendsListFragment.getLoadStatus()){
            Log.d(TAG, "is loading ");
        }
        else {
            super.uiLoadEnd();
        }
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {
        if(PickPeopleItemView.class.isInstance(view)) {
            PickPeopleItemView item = (PickPeopleItemView) view;
            if(item != null){
                IntentUtil.startContactDetailIntent(this, item.getContactSimpleInfo().mContactId);
            }
        }
    }

    private final int CREATE_LOCAL_CIRCLE = 1;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CREATE_LOCAL_CIRCLE: {
                createCircle(msg.getData().getString("circleName"));
                break;
            }
            }
        }
    }
    

    @Override
    public void getFriendsListFragment(FriendsListFragment fragment) {
        mFriendsListFragment = fragment;
    }

    @Override
    public long getCircleId() {
        return QiupuConfig.CIRCLE_ID_ALL;
    }

    @Override
    public void updateItemUI(QiupuUser user) {
        if(mFriendsListFragment != null) {
            mFriendsListFragment.updateListUI(true);
        }
    }

    @Override
    public void addFriends(QiupuUser user) { }

    @Override
    public void refuseUser(long uid) { }

    @Override
    public void deleteUser(QiupuUser user) { }

    @Override
    public void sendRequest(QiupuUser user) { }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
    }

    
    @Override
    protected void showCorpusSelectionDialog(View view) {
    	int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items =  getCircleNameArray();
        DialogUtils.showCorpusSelectionDialog(this, x, y, items, circleListItemClickListener);
    }

    AdapterView.OnItemClickListener circleListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                switchCircle(item.getText(), item.getItemId());
            }
        }
    };
    
    private void switchCircle(String label, String id) {
        Log.d(TAG, "switchCircleStream, circle label = " + label + ", id = " + id);

        setHeadTitle(label);

        long circleId = Long.parseLong(id);

        if (null != mFriendsListFragment) {
            mFriendsListFragment.switchCircle(circleId);
        }
    }

    private ArrayList<SelectionItem> getCircleNameArray() {
        ArrayList<SelectionItem> circleNames = new ArrayList<SelectionItem>();
        circleNames.add(new SelectionItem(String.valueOf(QiupuConfig.CIRCLE_ID_ALL),getString(R.string.friends_all)));

        for (int i = 0; i < localCircleList.size(); i++) {
            final UserCircle tmpcircle = localCircleList.get(i);
            if (!QiupuHelper.inFilterCircle(String.valueOf(tmpcircle.circleid))) {
                circleNames.add(new SelectionItem(
                		String.valueOf(tmpcircle.circleid),
                		CircleUtils.getLocalCircleName(this, tmpcircle.circleid, tmpcircle.name)
                		
                		));
            }
        }

        return circleNames;
    }
    
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//    	Log.d(TAG, "IntentUtil onQueryTextSubmit: " + query);
//    	if(query != null && query.length() > 0) {
//			IntentUtil.startPeopleSearchIntent(this, query);
//		}else {
//			Log.d(TAG, "onQueryTextSubmit, query is null " );
//			ToastUtil.showShortToast(this, mHandler, R.string.search_recommend);
//		}
//    	return super.onQueryTextSubmit(query);
//    }
    
    @Override
    public boolean onQueryTextChange(String newText) {
    	if(mFriendsListFragment != null) {
    		mFriendsListFragment.doSearch(newText);
    	}
    	return super.onQueryTextChange(newText);
    }
    
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
    	Log.d(TAG, "onKeyUp: " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			loadSearch();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
    
    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.menu_title_search)));
            items.add(new SelectionItem("", getString(R.string.label_refresh)));
            items.add(new SelectionItem("", getString(R.string.group_management)));
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(mRightActionBtn != null) {
	        int location[] = new int[2];
	        mRightActionBtn.getLocationInWindow(location);
	        int x = location[0];
	        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
	        
	        DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
	    }
	}
    
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    private void onCorpusSelected(String value) {
        if (getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
        }else if(getString(R.string.menu_title_search).equals(value)) {
        	loadSearch();
        }else if(getString(R.string.group_management).equals(value)){
        	IntentUtil.startCircleActivity(FriendsFragmentActivity.this, mUserid);
        }else {
            Log.d(TAG, "unsupported item action!");
        }
    }

}