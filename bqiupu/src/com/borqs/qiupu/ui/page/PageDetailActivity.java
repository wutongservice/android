package com.borqs.qiupu.ui.page;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.page.PageInfoDetailFragment.PageDetailFragmentListenerCallBack;
import com.borqs.qiupu.ui.page.PageInfoFragment.PageInfoFragmentListenerCallBack;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class PageDetailActivity extends BasicActivity  implements
	PageInfoFragmentListenerCallBack, StreamListFragmentCallBack, PageDetailFragmentListenerCallBack {

	private static final String TAG = "PageDetailActivity";

    private int mCurrentpage;

    private PageInfo mPage;
    private PageInfoFragment mPageInfoFragment;
    private PageInfoDetailFragment mPageInfoDetailFragment;
    
    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();
    public static final int in_member_selectcode = 5555;
    
    private int mCurrentFragment;
    private static final int profile_main = 1;
    private static final int profile_detail = 2;
    private FragmentManager mFragmentManager;
    
    private final static String PROFILE_MAIN_TAG = "PROFILE_MAIN_TAG";
   	private final static String PROFILE_DETAIL_TAG = "PROFILE_DETAIL_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_main);

        parseActivityIntent(getIntent());

        setHeadTitle(getString(R.string.page_label));

        showLeftActionBtn(false);
        showMiddleActionBtn(true);
        overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal , gotoComposeListener);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
        
        mCurrentFragment = profile_main;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mPageInfoFragment = new PageInfoFragment();
        ft.add(R.id.fragment_content, mPageInfoFragment, PROFILE_MAIN_TAG);
        ft.commit();
    }

    @Override
    protected void createHandler() {
    	mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch()
    {
        gotoSearchActivity();
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            String requestName = null;
            Bundle bundle = intent.getExtras();
            requestName = bundle.getString(CircleUtils.CIRCLE_NAME);
            mFragmentData.mUserId = bundle.getLong("pageid", -1);
            mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);

//            setHeadTitle(requestName);
        }else {
            final String circleId = BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARHC_KEY_CIRCLEID);
            mFragmentData.mUserId = Long.parseLong(circleId);
        }
        
        //TODO
//        mPage = new PageInfo();
//        mPage.page_id = mFragmentData.mUserId;
        mPage = orm.queryOnePage(mFragmentData.mUserId);
        if (mPage == null){
        	mPage = new PageInfo();
        	mPage.page_id = mFragmentData.mUserId;
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    };

    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mCurrentpage);
        if(mPageInfoFragment != null) {
        	mPageInfoFragment.refreshPageInfo();
        }
    }

    @Override
    protected void uiLoadEnd() {
        showProgressBtn(false);
        showLeftActionBtn(false);
    }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.page + mFragmentData.mUserId;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
//        	items.add(new SelectionItem("", getString(R.string.home_album)));
        	if(mPage.viewer_can_update) {
        		items.add(new SelectionItem("", getString(R.string.edit_string)));
        	}
        	if(mPage.creatorId == AccountServiceUtils.getBorqsAccountID()) {
        		if(mPage.associated_id > 0 && !TextUtils.isEmpty(mPage.free_circle_ids)) {
        			Log.d(TAG, "page already create formal&free circles ");
        		}else {
        			items.add(new SelectionItem("", getString(R.string.create_public_circle_title)));
        		}
        		
        		items.add(new SelectionItem("", getString(R.string.delete)));
        	}
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    View.OnClickListener gotoComposeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	gotoComposeAcitvity();
        }
    };

    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
        if(mMiddleActionBtn != null) {
            int location[] = new int[2];
            mMiddleActionBtn.getLocationInWindow(location);
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
	
	private void gotoComposeAcitvity() {
		HashMap<String, String> receiverMap = new HashMap<String, String>();
		String receiverid = String.valueOf(mPage.page_id);
		receiverMap.put(receiverid, mPage.name);
		IntentUtil.startComposeIntent(PageDetailActivity.this, receiverid, true, receiverMap);
	}
	
	private final static int PAGE_DELETE_END = 101;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case PAGE_DELETE_END: {
                try {
                    dismissDialog(DIALOG_DELETE_CIRCLE_PROCESS);
                } catch (Exception ne) {}
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret)
                {
                    showOperationSucToast(true);
                    QiupuHelper.updatePageActivityUI(null);
                    finish();
                } else {
                    ToastUtil.showOperationFailed(PageDetailActivity.this, mHandler, true);
                }
                break;
            }
            }
        }
    }
	
    public void onCorpusSelected(String value) {
        if(getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
        }else if(getString(R.string.home_album).equals(value)) {
        	IntentUtil.startAlbumIntent(this, mPage.page_id, mPage.name);
        }else if(getString(R.string.edit_string).equals(value)) {
        	Intent intent = new Intent(this, EditPageActivity.class);
        	intent.putExtra(PageInfo.PAGE_INFO, mPage);
        	startActivity(intent);
        }else if(getString(R.string.create_public_circle_title).equals(value)) {
        	if(mPageInfoFragment != null) {
        		mPageInfoFragment.gotoCreateCircles();
        	}
        }else if(getString(R.string.delete).equals(value)) {
        	DialogUtils.showConfirmDialog(this, R.string.delete, R.string.delete_page_message, 
                  R.string.label_ok, R.string.label_cancel, deletepageListener);
        }
//        else if(getString(R.string.public_circle_shortcut).equals(value))
//        {
//        	final Intent intent = createShortcutIntent(mCircle.uid,  mCircle.name, mCircle.profile_image_url);            
//            Activity a = getParent() == null ? this : getParent();
//            a.sendBroadcast(intent);         
//        }else if(getString(R.string.public_circle_receive_set).equals(value)) {
//        	IntentUtil.gotoEditPublicCircleActivity(this, mCircle.name, mCircle, EditPublicCircleActivity.edit_type_receive_set);
//        }else if(getString(R.string.edit_string).equals(value)) {
//        	Intent intent = new Intent(this, EditEventActivity.class);
//        	intent.putExtra(CircleUtils.CIRCLEINFO, mCircle);
//        	startActivity(intent);
//        }else if(getString(R.string.home_album).equals(value)) {
//        	IntentUtil.startAlbumIntent(this, mCircle.circleid,mCircle.name);
//        }else if(getString(R.string.delete).equals(value)) {
//        	int dialogMes = -1;
//        	if(QiupuConfig.isEventIds(mCircle.circleid)) {
//        		dialogMes = R.string.delete_event_message;
//        	}else {
//        		dialogMes = R.string.delete_public_circle_message;
//        	}
//        	DialogUtils.showConfirmDialog(this, R.string.delete, dialogMes, 
//                    R.string.label_ok, R.string.label_cancel, ExitNeutralListener);
//        }
    }
    
	@Override
	public void gotoProfileDetail() {
		if(mCurrentFragment != profile_detail){
			showSlideToggle(overrideSlideToggleClickListener);
			mCurrentFragment = profile_detail;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mPageInfoFragment != null && !mPageInfoFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mPageInfoFragment).commit();
			}
			
			mPageInfoDetailFragment = (PageInfoDetailFragment) mFragmentManager.findFragmentByTag(PROFILE_DETAIL_TAG);
			if(mPageInfoDetailFragment == null){
				mPageInfoDetailFragment = new PageInfoDetailFragment();
				mFragmentTransaction.add(R.id.fragment_content, mPageInfoDetailFragment, PROFILE_DETAIL_TAG);
			}else {
				mFragmentTransaction.show(mPageInfoDetailFragment);
			}
			mFragmentTransaction.commit();
		}
	}
	
	View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			handlerBackKey(false);
		}
	};
	
	private void handlerBackKey(boolean isBackKey) {
		if(mCurrentFragment == profile_detail){
			mCurrentFragment = profile_main;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mPageInfoDetailFragment != null && !mPageInfoDetailFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mPageInfoDetailFragment).commit();
			}
			
			mPageInfoFragment = (PageInfoFragment) mFragmentManager.findFragmentByTag(PROFILE_MAIN_TAG);
			
			if(mPageInfoFragment == null){
				mPageInfoFragment = new PageInfoFragment();
				mFragmentTransaction.add(R.id.fragment_content, mPageInfoFragment, PROFILE_MAIN_TAG);
			}else {
				mFragmentTransaction.show(mPageInfoFragment);
			}
			mFragmentTransaction.commit();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
	    handlerBackKey(true);
	}

	@Override
	public void getPageDetailInfoFragment(PageInfoDetailFragment fragment) {
		mPageInfoDetailFragment = fragment;
	}

	@Override
	public void getPageInfoFragment(PageInfoFragment fragment) {
		mPageInfoFragment = fragment;
	}

	@Override
	public PageInfo getPageInfo() {
		return mPage;
	}

	@Override
	public void refreshPageInfo(PageInfo page) {
		mPage = page;
	    if(mPageInfoDetailFragment != null) {
	    	mPageInfoDetailFragment.refreshPage(page);
	    }
	}

	DialogInterface.OnClickListener deletepageListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			deletePageFromServer(mPage.page_id);
		}
	};
	
	boolean inDeletePage;
    Object mLockdeletePage = new Object();
    
    public void deletePageFromServer(final long pageid) {
        if (inDeletePage == true) {
            Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
            return ;
        }
        
        synchronized (mLockdeletePage) {
        	inDeletePage = true;
        }
        showDialog(DIALOG_DELETE_CIRCLE_PROCESS);
        asyncQiupu.deletePage(AccountServiceUtils.getSessionID(), pageid, new TwitterAdapter() {
            public void deletePage(boolean suc) {
                Log.d(TAG, "finish deletePage=" + suc);
                if(suc) {
                    //delete page in DB
                	orm.deletePageByPageId(pageid);
                    //update circle info with page
                	orm.removeCirclePageId(pageid);
                	
                }
                
                Message msg = mHandler.obtainMessage(PAGE_DELETE_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();
                
                synchronized (mLockdeletePage) {
                	inDeletePage = false;
                }
            }
            
            public void onException(TwitterException ex,TwitterMethod method) {
                synchronized (mLockdeletePage) {
                	inDeletePage = false;
                }
                
                Message msg = mHandler.obtainMessage(PAGE_DELETE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
}