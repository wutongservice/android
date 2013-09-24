package com.borqs.qiupu.ui.page;

import twitter4j.PageInfo;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.adapter.GridPeopleSimpleAdapter;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.BasicFragment;
import com.borqs.qiupu.util.StringUtil;

public class PageInfoDetailFragment extends BasicFragment implements PageActionListener {
	private final static String TAG = "PageInfoDetailFragment";
	private Activity mActivity; 
	private QiupuORM orm;
	private PageDetailFragmentListenerCallBack mCallBackListener;
	private TextView mPage_title;
	private TextView mAddressDetail;
	private View mPhone_info;
	private View mEmail_info;
	private TextView mPage_des;
	private ImageView mProfile_img_ui;
	 private TextView mPageFans;
	
    private PageInfo mPage;
    
    private View mContentView;
    private View mFanRl;
    private GridView grid_fans;
    
    private ImageView mCover;
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof PageDetailFragmentListenerCallBack) {
        	mCallBackListener = (PageDetailFragmentListenerCallBack)activity;
        	mCallBackListener.getPageDetailInfoFragment(this);
        	mPage = mCallBackListener.getPageInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	orm = QiupuORM.getInstance(mActivity);
    	QiupuHelper.registerPageListener(getClass().getName(), this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	
    	mContentView = inflater.inflate(R.layout.page_info_view, container, false);
    	mPage_title = (TextView) mContentView.findViewById(R.id.id_page_title);
    	mPhone_info = mContentView.findViewById(R.id.phone_info_ll);
    	mEmail_info = mContentView.findViewById(R.id.email_info_ll);
    	mPage_des = (TextView) mContentView.findViewById(R.id.id_description);
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.page_logo);
    	mPageFans = (TextView) mContentView.findViewById(R.id.id_page_fan_count);
    	mCover = (ImageView) mContentView.findViewById(R.id.page_cover);
    	
    	mFanRl = mContentView.findViewById(R.id.fans_rl);
    	mFanRl.setOnClickListener(fansClickListener);
    	
    	grid_fans = (GridView) mContentView.findViewById(R.id.grid_fans);
    	
    	return mContentView;
    }
    
    
    private View findFragmentViewById(int id) {
        if(mContentView != null) {
            return mContentView.findViewById(id);
        }
        return null;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        refreshPageInfoUi();
    }

    private void refreshPageInfoUi() {
    	if(mPage != null && isDetached() == false && getActivity() != null) {
    	    refreshHeadUi();
    		refreshOtherInfoUi();
    		refreshFansUi();
		}
    }
    
    private void refreshOtherInfoUi() {
    	//address & des
    	String addressinfo = "";
    	String des = "";
    	if(QiupuHelper.isZhCNLanguage(mActivity)) {
    		if(StringUtil.isValidString(mPage.address)) {
    			addressinfo = mPage.address;
    		}else {
    			addressinfo = mPage.address_en;
    		}
    		if(StringUtil.isValidString(mPage.description)) {
    			des = mPage.description;
    		}else {
    			des = mPage.description_en;
    		}
    		
    	}else {
    		if(StringUtil.isValidString(mPage.address_en)) {
    			addressinfo = mPage.address_en;
    		}else {
    			addressinfo = mPage.address;
    		}
    		if(StringUtil.isValidString(mPage.description_en)) {
    			des = mPage.description_en;
    		}else {
    			des = mPage.description;
    		}
    	}
    	
        if(StringUtil.isValidString(addressinfo)) {
        	findFragmentViewById(R.id.address_info_title).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.span_view2).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.address_info_ll).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.address_info_ll).setOnClickListener(addressClickListener);
        	mAddressDetail = (TextView) findFragmentViewById(R.id.address_info);
        	mAddressDetail.setText(addressinfo);
        } else {
        	findFragmentViewById(R.id.address_info_ll).setVisibility(View.GONE);
            findFragmentViewById(R.id.address_info_title).setVisibility(View.GONE);
        	findFragmentViewById(R.id.span_view2).setVisibility(View.GONE);
        }
        
        if(StringUtil.isValidString(des)) {
        	mPage_des.setVisibility(View.VISIBLE);
        	SNSItemView.attachHtml(mPage_des, des);
        }else {
        	mPage_des.setVisibility(View.GONE);
        }
        
        boolean isshowContactTitle = false;
        //phone
        if(StringUtil.isValidString(mPage.tel)) {
        	isshowContactTitle = true;
            mPhone_info.setVisibility(View.VISIBLE);
            mPhone_info.setOnClickListener(PhoneClickListener);
            
            TextView phone = (TextView) findFragmentViewById(R.id.phone_info);
            phone.setText(mPage.tel);
        }else {
            mPhone_info.setVisibility(View.GONE);
        }
        //email
        if(StringUtil.isValidString(mPage.email)) {
        	isshowContactTitle = true; 
            mEmail_info.setVisibility(View.VISIBLE);
            mEmail_info.setOnClickListener(emailClickListener);
            TextView email = (TextView) findFragmentViewById(R.id.email_info);
            email.setText(mPage.email);
        }else {
            mEmail_info.setVisibility(View.GONE);
        }
        //website
        if(StringUtil.isValidString(mPage.website)) {
        	isshowContactTitle = true; 
        	View websitell = findFragmentViewById(R.id.website_info_ll); 
        	websitell.setVisibility(View.VISIBLE);
        	websitell.setOnClickListener(webSiteClickListener);
        	TextView website = (TextView) findFragmentViewById(R.id.website_info);
        	website.setText(mPage.website);
        }else {
        	findFragmentViewById(R.id.website_info_ll).setVisibility(View.GONE);
        }
        
        //fax
        if(StringUtil.isValidString(mPage.fax)) {
        	isshowContactTitle = true; 
        	View faxll = findFragmentViewById(R.id.fax_info_ll); 
        	faxll.setVisibility(View.VISIBLE);
//        	faxll.setOnClickListener(faxClickListener);
        	TextView fax = (TextView) findFragmentViewById(R.id.fax_info);
        	fax.setText(mPage.fax);
        }else {
        	findFragmentViewById(R.id.fax_info_ll).setVisibility(View.GONE);
        }
        
        if(isshowContactTitle) {
        	findFragmentViewById(R.id.contact_info_title).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.span_view3).setVisibility(View.VISIBLE);
        }else {
        	findFragmentViewById(R.id.contact_info_title).setVisibility(View.GONE);
        	findFragmentViewById(R.id.span_view3).setVisibility(View.GONE);
        }
    }
    
    private void refreshHeadUi() {
    	if(QiupuHelper.isZhCNLanguage(mActivity)) {
    		if(StringUtil.isValidString(mPage.name)) {
    			mPage_title.setText(mPage.name);
    		}else {
    			mPage_title.setText(mPage.name_en);
    		}
    	}else {
    		if(StringUtil.isValidString(mPage.name_en)) {
    			mPage_title.setText(mPage.name_en);
    		}else {
    			mPage_title.setText(mPage.name);
    		}
    	}
    	
    	if(StringUtil.isValidString(mPage.cover_url)) {
    		setViewIcon(mPage.cover_url, mCover, false);
    	}else {
    		mCover.setImageResource(R.drawable.event_default_cover);
    	}
    	
    	if(StringUtil.isValidString(mPage.logo_url)) {
    		setViewIcon(mPage.logo_url, mProfile_img_ui, false);
    	}else {
    		mProfile_img_ui.setImageResource(R.drawable.default_public_circle);
		}
    	mPageFans.setText(String.format(getResources().getString(R.string.page_fans_count), mPage.followers_count));
    }
    
    private void refreshFansUi() {
        if(mPage.followers_count > 0) {
        	mFanRl.setVisibility(View.VISIBLE);
            TextView in_tv = (TextView) findFragmentViewById(R.id.tv_fans);
            in_tv.setText(getString(R.string.user_followers) + "(" + mPage.followers_count + ")");
            if(mPage.fansList != null && mPage.fansList.size() > 0) {
            	grid_fans.setAdapter(new GridPeopleSimpleAdapter(mActivity, mPage.fansList));
            }
        }else {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.GONE);
        }
    }
    
    View.OnClickListener addressClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
                        Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=" + mAddressDetail.getText().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                        & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
                
                intent.setClassName("com.google.android.apps.maps", 
                        "com.google.android.maps.MapsActivity"); 
                startActivity(intent);
            } catch(ActivityNotFoundException e) {
                Log.d(TAG, "error, no MapsActivity in system.");
            }
        }
    };
    
    View.OnClickListener PhoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("tel:").append(mPage.tel).toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    View.OnClickListener emailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("mailto:").append(mPage.email).toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    View.OnClickListener webSiteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };
    
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        if(mCircle != null) {
//            outState.putLong(CircleUtils.CIRCLE_ID, mCircle.circleid);
//        }
//        super.onSaveInstanceState(outState);
//    }

    @Override
    public void onDestroy() {
    	QiupuHelper.unregisterPageListener(getClass().getName());
    	super.onDestroy();
    }
    
	public interface PageDetailFragmentListenerCallBack {
		public void getPageDetailInfoFragment(PageInfoDetailFragment fragment);
		public PageInfo getPageInfo();
	}
	
	
	public void refreshPage(PageInfo page) {
		mPage = page;
		refreshPageInfoUi();
	}

	@Override
	public void refreshpage(PageInfo info) {
		mPage = orm.queryOnePage(mPage.page_id);
		refreshPageInfoUi();		
	}
	
	View.OnClickListener fansClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			IntentUtil.showUserFansList(mActivity, mPage.page_id);
		}
	};
}
