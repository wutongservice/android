package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.Circletemplate;
import twitter4j.PageInfo;
import twitter4j.UserCircle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.adapter.BpcFriendsFragmentAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.AllCirclesListFragment;
import com.borqs.qiupu.fragment.AllCirclesListFragment.CallBackCircleListFragmentListener;
import com.borqs.qiupu.fragment.FixedTabsView;
import com.borqs.qiupu.fragment.PageListFragment;
import com.borqs.qiupu.fragment.PageListFragment.CallBackPageListFragmentListener;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.page.CreateCircleMainActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class BpcFriendsFragmentActivity extends BasicNavigationActivity implements
                                 OnListItemClickListener,CallBackCircleListFragmentListener,
                                 CallBackPageListFragmentListener{

    private final static String TAG = "BpcFriendsFragmentActivity";
//    private ViewPager mPager;
//    private PageIndicator mIndicator;
    private long mUserid;
    private int mConcernType = 0;
    private BpcFriendsFragmentAdapter mAdapter;
    private AllCirclesListFragment mCirclesListFragment;
//    private FriendsListFragment mFriendsListFragment;
    private PageListFragment mPageListFragment;
    public static int current_type_page = 1;
    public static int current_type_circle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_friends_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        parseActivityIntent(getIntent());
        Log.d(TAG, "mConcernType: " + mConcernType);

        mAdapter = new BpcFriendsFragmentAdapter(getSupportFragmentManager(), this);

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mConcernType);
//        showMiddleActionBtn(true);
//        overrideMiddleActionBtn(R.drawable.actionbar_icon_search_normal, searchListener);

        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
        
        FixedTabsView mIndicator = (FixedTabsView) findViewById(R.id.indicator);
        if (null != mIndicator) {
            mIndicator.setViewPager(mPager);
            mIndicator.setAdapter(mAdapter);
            if (mAdapter.getCount() > 1) {
                mIndicator.setSelectTab(mConcernType);
                mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);
                mAdapter.setTabBtnBg(mConcernType);
            } else {
                mIndicator.setVisibility(View.GONE);
                showRightActionBtn(false);
            }
        }

        showLeftActionBtn(true);

        if (fromtab) {
            setHeadTitle(R.string.tab_friends);
        } else {
//            setHeadTitle(getUserNickname(mUserid));
            setHeadTitle(R.string.user_circles);
        }
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch() 
    {
    	showSearhView();
//    	Intent intent = new Intent(this, PageSearchActivity.class) ;
//    	startActivity(intent);
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
                mConcernType = bundle.getInt(USER_CONCERN_TYPE, QiupuConfig.USER_INDEX_FRIENDS);
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
                mConcernType = TextUtils.isEmpty(tab) ? 0 : Integer
                        .parseInt(tab);
            }
        }
    }
    
    ViewPager.OnPageChangeListener pagerOnPageChangeListener = new OnPageChangeListener() {
        
        @Override
        public void onPageSelected(int page) {
        	hideSearhView();
            mConcernType = page;
            mAdapter.setTabBtnBg(mConcernType);
            if(mConcernType == current_type_page) {
            	setHeadTitle(R.string.page_label);
//            	overrideRightActionBtn(R.drawable.create_page_icon, addPageListener);
            }else if(mConcernType == current_type_circle) {
            	setHeadTitle(R.string.user_circles);
//            	overrideRightActionBtn(R.drawable.ic_add_circles, addCircleListener);
            }
        }
        
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        
        @Override
        public void onPageScrollStateChanged(int page) {
        }
    };
    
    
    public long getUserId(){
        return mUserid;
    }
    
    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mConcernType);
        if(mConcernType == 1 && null != mPageListFragment){
        	mPageListFragment.loadRefresh();
        }else if(mConcernType == 0 && null != mCirclesListFragment){
        	mCirclesListFragment.loadRefresh();
        }
    }

    @Override
    protected void uiLoadEnd()  {
        if((mCirclesListFragment != null && mCirclesListFragment.getLoadStatus())
                || (mPageListFragment != null && mPageListFragment.getLoadStatus())){
            Log.d(TAG, "is loading ");
        }
        else {
            super.uiLoadEnd();
        }
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {
        if (CircleItemView.class.isInstance(view)) {
            CircleItemView civ = (CircleItemView) view;
            if(civ.getCircle() != null)
            {
                IntentUtil.startCircleDetailIntent(this, civ.getCircle(), fromtab);
            }
            else
            {
            	Log.d(TAG, "circleItemView circle info is null");
//                CirclesListFragment circlefg = (CirclesListFragment) fg;
//                circlefg.showAddCircleDialog();
//                circlefg.setButtonEnable(false);
            }
        }
        else if(PickPeopleItemView.class.isInstance(view)) {
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
    public void getCircleListFragment(AllCirclesListFragment fragment) {
        mCirclesListFragment = fragment;
    }

    @Override
    protected void doCircleActionCallBack(boolean isdelete) {
       if(mCirclesListFragment != null) {
           mCirclesListFragment.refreshUi();
       }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.friends_circle);
//    }
    
//    View.OnClickListener addCircleListener = new OnClickListener() {
//        public void onClick(View v) {
//        	Intent intent = new Intent(BpcFriendsFragmentActivity.this, CreateCircleMainActivity.class);
//        	intent.putExtra(CreateCircleMainActivity.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATE);
//        	intent.putExtra(PageInfo.PAGE_ID, UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID);
//        	startActivity(intent);
////        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
////        	items.add(new SelectionItem("", getString(R.string.create_public_circle_title)));
////        	items.add(new SelectionItem("", getString(R.string.create_group_label)));
////        	showCorpusSelectionDialog(items);
//        }
//    };
    
    View.OnClickListener searchListener = new OnClickListener() {
        public void onClick(View v) {
        	loadSearch();
        }
    };
    
//    View.OnClickListener addPageListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			IntentUtil.startCreatePageActivity(BpcFriendsFragmentActivity.this);
//		}
//	};
    
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
    	if(getString(R.string.menu_title_search).equals(value)) {
    		loadSearch();
    	}else if(getString(R.string.label_refresh).equals(value)) {
    		loadRefresh();
    	}else if(getString(R.string.create_page_label).equals(value)) {
    		IntentUtil.startCreatePageActivity(BpcFriendsFragmentActivity.this);
        }else if(getString(R.string.create_public_circle_title).equals(value)) {
        	Intent intent = new Intent(BpcFriendsFragmentActivity.this, CreateCircleMainActivity.class);
        	intent.putExtra(CreateCircleMainActivity.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATE);
        	intent.putExtra(PageInfo.PAGE_ID, UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID);
        	
//        	final String homeid = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
//        	long homeScene = TextUtils.isEmpty(homeid) ? -1 : Long.parseLong(homeid);
//        	
//        	intent.putExtra(CircleUtils.INTENT_SCENE, homeScene);
        	startActivity(intent);
        }else {
            Log.d(TAG, "unsupported item action!");
        }
    }
    private AlertDialog mAlertDialog;
    public void showAddCircleDialog(){
        LayoutInflater factory = LayoutInflater.from(this);  
        final View textEntryView = factory.inflate(R.layout.create_circle_dialog, null);  
        final EditText textContext = (EditText) textEntryView.findViewById(R.id.new_circle_edt);

//        final CheckBox select_public_circle = (CheckBox) textEntryView.findViewById(R.id.select_public_circle);
//        if(orm.isOpenPublicCircle()) {
//            select_public_circle.setVisibility(View.VISIBLE);
//        }
//        select_public_circle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                setDialogButtonEnable(!isChecked);
//                if(isChecked) {
//                    IntentUtil.gotoEditPublicCircleActivity(BpcFriendsFragmentActivity.this, textContext.getText().toString().trim(), null, EditPublicCircleActivity.type_create);
//                    mAlertDialog.dismiss();
//                }
//            }
//        });
//        
        textContext.addTextChangedListener(new ButtonWatcher()); 
        
        mAlertDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.new_circle_dialog_title)
        .setView(textEntryView)
        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String textString = textContext.getText().toString().trim();
                boolean hasCirecle = false;
                if(textString.length() > 0)
                {
                    Cursor cursor = orm.queryAllCircleinfo(AccountServiceUtils.getBorqsAccountID());
                    final int size = null == cursor ? 0 : cursor.getCount();
                    for(int i=0; i < size; i++)
                    {
                        cursor.moveToPosition(i);
                        UserCircle tmpCircle = QiupuORM.createCircleInformation(cursor);
                        if(tmpCircle != null && tmpCircle.name != null && tmpCircle.name.equals(textString))
                        {
                            hasCirecle = true;
                            Toast.makeText(BpcFriendsFragmentActivity.this, getString(R.string.circle_exists), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if (null != cursor) {
                        cursor.close();
                    }

                    if(!hasCirecle) {
                        mAlertDialog.dismiss();
                        Message msg = mHandler.obtainMessage(CREATE_LOCAL_CIRCLE);
                        msg.getData().putString("circleName",textString);
                        msg.sendToTarget();
                    }
                }
                else {
                    Toast.makeText(BpcFriendsFragmentActivity.this, getString(R.string.input_content), Toast.LENGTH_SHORT).show();
                }
                
            }
        })
        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .create();
        mAlertDialog.show();
        setDialogButtonEnable(false);
    }
    
    private class ButtonWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() > 0) {
                setDialogButtonEnable(true);
            } else {
                setDialogButtonEnable(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }
    }
    
    private void setDialogButtonEnable(boolean flag) {
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
    }

	@Override
	public void getPageListFragment(PageListFragment fragment) {
		mPageListFragment = fragment;
	}

//	@Override
//    public boolean onQueryTextSubmit(String query) {
//    	Log.d(TAG, "IntentUtil onQueryTextSubmit: " + query);
//    	if(query != null && query.length() > 0) {
//    		if(mConcernType == current_type_page) {
//    			IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_PAGE);
//    		}else if(mConcernType == current_type_circle) {
//    			IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_CIRCLE);
//    		}
//		}else {
//			Log.d(TAG, "onQueryTextSubmit, query is null " );
//			ToastUtil.showShortToast(this, mHandler, R.string.search_recommend);
//		}
//    	return super.onQueryTextSubmit(query);
//    }
	
	@Override
    public boolean onQueryTextChange(String newText) {
		if(mConcernType == current_type_page) {
			if(mPageListFragment != null) {
				mPageListFragment.doSearch(newText);
			}
		}else if(mConcernType == current_type_circle) {
			if(mCirclesListFragment != null) {
				mCirclesListFragment.doSearch(newText);
			}
		}
    	return super.onQueryTextChange(newText);
    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
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
            if(mConcernType == current_type_page) {
            	items.add(new SelectionItem("", getString(R.string.create_page_label)));
            }else if(mConcernType == current_type_circle) {
            	items.add(new SelectionItem("", getString(R.string.create_public_circle_title)));
            }
        	
        	showCorpusSelectionDialog(items);
        }
    };
	
}