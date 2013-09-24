package com.borqs.qiupu.ui.page;

import java.util.ArrayList;

import twitter4j.Circletemplate;
import twitter4j.Circletemplate.TemplateInfo;
import twitter4j.CircletemplateJSONImpl;
import twitter4j.PageInfo;
import twitter4j.UserCircle;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.borqs.common.adapter.CreateCircleAdapter;
import com.borqs.common.view.CreateCircleItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public class CreateCircleMainActivity extends BasicActivity {

	private static final String TAG = "CreateCircleMainActivity";
//	private View mFormal_circle_rl;
//	private View mFree_circle_rl;
	private GridView mGridView;
	private CreateCircleAdapter mAdapter;
	private long mPageId;
	private long mParentId;
	private long mSceneId;
	private int mCreateStatus;
	public static final int CREATE_STATUS_DEFAULT = 0;
	public static final int CREATE_STATUS_ONLYFORMAL = 1;
	public static final int CREATE_STATUS_ONLYFREE = 2;
	public static final String CREATE_STATUS = "CREATE_STATUS";
	public static final String SUBTYPE = "SubType";
	private static final int createCirclesRequestCode = 11111;
	private int mSelectCreate; 
	private String mSubType;
	private ArrayList<TemplateInfo> mInfoList = new ArrayList<TemplateInfo>();
	
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_circle_main_view);
		setHeadTitle(R.string.create_public_circle_title);
		showRightActionBtn(false);
		Intent intent = getIntent();
		mPageId = intent.getLongExtra(PageInfo.PAGE_ID, 0);
		mParentId = intent.getLongExtra(UserCircle.PARENT_ID, 0);
		mSceneId = intent.getLongExtra(CircleUtils.INTENT_SCENE, 0);
		mSubType = intent.getStringExtra(SUBTYPE);
		mCreateStatus = intent.getIntExtra(CREATE_STATUS, CREATE_STATUS_DEFAULT);
		parseTemplate();
		mGridView = (GridView) findViewById(R.id.circle_tem_grid);
		mAdapter = new CreateCircleAdapter(this);
		mGridView.setAdapter(mAdapter);
		
		if(mPageId > 0) {
			if(mCreateStatus == CREATE_STATUS_ONLYFORMAL) {
				for(int i=0; i<mInfoList.size(); i++) {
					if(Circletemplate.TEMPLATE_FREE_NAME.equals(mInfoList.get(i).name)) {
						mInfoList.remove(i);
						break;
					}
				}
				mSelectCreate = CREATE_STATUS_ONLYFORMAL;
			}else if(mCreateStatus == CREATE_STATUS_ONLYFREE) {
				for(int i=0; i<mInfoList.size(); i++) {
					if(Circletemplate.TEMPLATE_FORMAL_NAME.equals(mInfoList.get(i).name)) {
						mInfoList.remove(i);
						break;
					}
				}
				mSelectCreate = CREATE_STATUS_ONLYFREE;
			}else {
			}
		}
		
		mAdapter.alertData(mInfoList);
		mGridView.setOnItemClickListener(listItemClickListener);
	}
	
    
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
            }
    }
    
    private void gotoCreateCircle(TemplateInfo info) {
//    	if(mPageId == 0) {
//    		Log.d(TAG, "page is null , create circle failed");
//    		return ;
//    	}
    	Intent intent = new Intent(this, EditPublicCircleActivity.class);
    	intent.putExtra(CircleUtils.EdIT_TYPE, EditPublicCircleActivity.type_create);
    	intent.putExtra(UserCircle.CIRCLE_CREATE_TYPE, info.formal);
    	intent.putExtra("subtype", info.name);
    	intent.putExtra(PageInfo.PAGE_ID, mPageId);
    	intent.putExtra(UserCircle.PARENT_ID, mParentId);
    	intent.putExtra(CircleUtils.INTENT_SCENE, mSceneId);
    	startActivityForResult(intent, createCirclesRequestCode);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != Activity.RESULT_OK) {
    		return ;
    	}
    	if(requestCode == createCirclesRequestCode) {
    		Log.d(TAG, "onActivityResult : " + mSelectCreate);
    		if(mPageId <= 0) {
    			Log.d(TAG, "is not page, no need refresh item");
    			return ;
    		}
    		if(mSelectCreate == CREATE_STATUS_ONLYFORMAL) {
    			for(int i=0; i<mInfoList.size(); i++) {
    				if(Circletemplate.TEMPLATE_FORMAL_NAME.equals(mInfoList.get(i).name)) {
    					mInfoList.remove(i);
    					break;
    				}
    			}
    		}else if(mSelectCreate == CREATE_STATUS_ONLYFREE) {
    			for(int i=0; i<mInfoList.size(); i++) {
    				if(Circletemplate.TEMPLATE_FREE_NAME.equals(mInfoList.get(i).name)) {
    					mInfoList.remove(i);
    					break;
    				}
    			}
    		}
    		mAdapter.alertData(mInfoList);
    		//finish the activity when there have not circle to create
    		if(mInfoList.size() <=0) {
    			finish();
    		}
    	}
    	
    }
    
    OnItemClickListener listItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(CreateCircleItemView.class.isInstance(view)) {
				CreateCircleItemView item = (CreateCircleItemView) view;
				TemplateInfo info = item.getItem();
				if(info != null) {
					if(Circletemplate.TEMPLATE_FORMAL_NAME.equals(info.name)) {
						mSelectCreate = CREATE_STATUS_ONLYFORMAL;
						info.formal = UserCircle.circle_top_formal;
					}else if(Circletemplate.TEMPLATE_FREE_NAME.equals(info.name)) {
						mSelectCreate = CREATE_STATUS_ONLYFREE;
						info.formal = UserCircle.circle_free;
					}
					gotoCreateCircle(info);
				}
			}else {
	            Log.d(TAG, "the page is null.");
	        }
		}
	};
	
	
    private void parseTemplate() {
    	String templateJson = StringUtil.loadResource(this);
    	try {
    		mInfoList.clear();
    		mInfoList.addAll(CircletemplateJSONImpl.parseTemplateJsonArrayWithSubType(new JSONObject(templateJson), mSubType));
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    
}
