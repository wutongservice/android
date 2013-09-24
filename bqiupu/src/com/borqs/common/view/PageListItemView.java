package com.borqs.common.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.PageInfo;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.page.PageListActivity;
import com.borqs.qiupu.util.StringUtil;

public class PageListItemView extends SNSItemView {

    private static final String TAG = "PageListItemView";
    private PageInfo mPage;
    private ImageView mPageIcon;
    private TextView mPagename;
    private TextView mPageFans;
    private TextView mFollow_action; 
    private HashMap<String, followActionListener> mPageActionMap;
    
    public PageListItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public PageListItemView(Context context, PageInfo page) {
        super(context);

        mPage = page;
        init();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        //child 1
        View convertView = factory.inflate(R.layout.page_item_view, null);
        addView(convertView);
        mPageIcon = (ImageView) convertView.findViewById(R.id.id_page_icon);
        mPagename = (TextView) convertView.findViewById(R.id.id_page_name);
        mPageFans = (TextView) convertView.findViewById(R.id.id_page_fan_count);
        mFollow_action = (TextView) convertView.findViewById(R.id.follow_action); 
        setUI();
    }

    public void setPage(PageInfo pageInfo) {
        mPage = pageInfo;
        setUI();
    }

    private void setUI() {
    	if (mPage != null) {
    		if(QiupuHelper.isZhCNLanguage(mContext)) {
    			if(StringUtil.isValidString(mPage.name)) {
    				mPagename.setText(mPage.name);
    			}else {
    				mPagename.setText(mPage.name_en);
    			}
    		}else {
    			if(StringUtil.isValidString(mPage.name_en)) {
    				mPagename.setText(mPage.name_en);
    			}else {
    				mPagename.setText(mPage.name);
    			}
    		}
    			
    		mPageFans.setText(String.format(mContext.getResources().getString(R.string.page_fans_count), mPage.followers_count));
    		setPageIcon();
    		refreshAtionUI();
    	}
    }
    private void refreshAtionUI() {
    	if(mPage.creatorId == AccountServiceUtils.getBorqsAccountID()) {
			mFollow_action.setVisibility(View.GONE);
		}else if(BpcFriendsFragmentActivity.class.isInstance(mContext)) {
			mFollow_action.setVisibility(View.GONE);
		}else {
			if(mPage.followed) {
				mFollow_action.setVisibility(View.VISIBLE);
				mFollow_action.setText(R.string.un_follow);
				mFollow_action.setBackgroundResource(R.drawable.btn_unfollow_bg);
				mFollow_action.setTextColor(getResources().getColor(android.R.color.black));
			}else {
				mFollow_action.setVisibility(View.VISIBLE);
				mFollow_action.setText(R.string.following);
				mFollow_action.setBackgroundResource(R.drawable.profile_add_circle_bg);
				mFollow_action.setTextColor(getResources().getColor(android.R.color.white));
			}
		}
		mFollow_action.setOnClickListener(followActionClickListener);
    }
    
    private void setPageIcon() {
    	if(StringUtil.isValidString(mPage.logo_url)) {
    		ImageRun imagerun = new ImageRun(null,mPage.logo_url, 0);
    		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
    		imagerun.noimage = true;
    		imagerun.addHostAndPath = true;
    		imagerun.setRoundAngle=true;
    		imagerun.setImageView(mPageIcon);
    		imagerun.post(null);
    	}else {
    		mPageIcon.setImageResource(R.drawable.default_public_circle);
    	}
    }
    
    
    public PageInfo getItem(){
    	return mPage;
    }
    
    public long getDataItemId(){
        return mPage.page_id;
    }

	public void attachActionListener(
			HashMap<String, followActionListener> pageActionMap) {
		mPageActionMap = pageActionMap;
	}
	
	View.OnClickListener followActionClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            if (null != mPageActionMap) {
            	Log.d(TAG, "mPageActionMap size : " + mPageActionMap.size());
                Collection<followActionListener> listeners = mPageActionMap.values();
                Iterator<followActionListener> it = listeners.iterator();
                while (it.hasNext()) {
                	followActionListener checkListener = (followActionListener)it.next();
                	if(mPage.followed) {
                		checkListener.unFollowPage(mPage.page_id);
                	}else {
                		checkListener.followPage(mPage.page_id);
                	}
                }
            }
        }
    };
    

    public interface followActionListener{
    	public void followPage(long pageid);
    	public void unFollowPage(long pageid);
    }


    public boolean refreshItem(long pageid) {
        if (mPage.page_id == pageid) {
        	mPage.followed = !mPage.followed;
        	refreshAtionUI();
            return true;
        }
        return false;
    }
}

