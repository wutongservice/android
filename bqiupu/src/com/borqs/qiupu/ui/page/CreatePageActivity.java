package com.borqs.qiupu.ui.page;

import java.util.HashMap;

import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ToastUtil;

public class CreatePageActivity extends BasicActivity {

	private static final String TAG = "CreatePageActivity";
	
	private EditText mPageName;
	private EditText mPageLocation;
	private EditText mPageDes;
	
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_page_ui);
		setHeadTitle(R.string.create_page_label);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		overrideRightTextActionBtn(R.string.create, createClickListener);
		initUi();
	}
	
	private void initUi() {
		mPageName = (EditText) findViewById(R.id.page_name);
		mPageLocation = (EditText) findViewById(R.id.page_location);
		mPageDes = (EditText) findViewById(R.id.page_description);
	}
	
	private final static int CREATE_PAGE = 101;
	private final static int CREATE_PAGE_END = 102;
	
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CREATE_PAGE: {
            	createPage();
                break;
            }
            case CREATE_PAGE_END : {
            	try {
                    dismissDialog(DIALOG_SET_USER_PROCESS);
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                    showOperationSucToast(true);
                    QiupuHelper.updatePageActivityUI(null);
                    CreatePageActivity.this.finish();
                } else {
                    showOperationFailToast("", true);
                }
				break;
			}
            }
        }
    }
    
    boolean inCreatePage;
    Object mLockCreatePage = new Object();
    public void createPage(final HashMap<String, String> map) {
        if (inCreatePage == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockCreatePage) {
        	inCreatePage = true;
        }
        
        showDialog(DIALOG_SET_USER_PROCESS);
        asyncQiupu.createPage(AccountServiceUtils.getSessionID(), map, new TwitterAdapter() {
            public void createPage(PageInfo pageinfo) {
                Log.d(TAG, "finish createPage=" );
                
//                if(suc) {
                orm.insertOnePage(pageinfo);
//                }
                
                Message msg = mHandler.obtainMessage(CREATE_PAGE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockCreatePage) {
                	inCreatePage = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockCreatePage) {
                	inCreatePage = false;
                }
                Message msg = mHandler.obtainMessage(CREATE_PAGE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    View.OnClickListener createClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mHandler.obtainMessage(CREATE_PAGE).sendToTarget();
		}
	};
	
    private void createPage() {
    	final String pageName = mPageName.getText().toString().trim();
		if(TextUtils.isEmpty(pageName)) {
			ToastUtil.showShortToast(this, mHandler, R.string.name_isnull_toast);
			mPageName.requestFocus();
			return ;
		}
		
		final String des = mPageDes.getText().toString().trim();
		final String location = mPageLocation.getText().toString().trim();
		
		HashMap<String, String> map = new HashMap<String, String>();
//		PageInfo tmpPage = new PageInfo();
		if(QiupuHelper.isZhCNLanguage(this)) {
//			tmpPage.name = pageName;
//			tmpPage.description = des;
//			tmpPage.address = location;
			
			map.put("name", pageName);
			map.put("description", des);
			map.put("address", location);
		}else {
//			tmpPage.name_en = pageName;
//			tmpPage.description_en = des;
//			tmpPage.address_en = location;
			
			map.put("name_en", pageName);
			map.put("description_en", des);
			map.put("address_en", location);
		}
		
		createPage(map/*, tmpPage*/);
//		createPublicCircle(tmpCircle, map);
    }
    
    
}
