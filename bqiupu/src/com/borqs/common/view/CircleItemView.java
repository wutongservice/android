package com.borqs.common.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import twitter4j.UserCircle;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.listener.CircleActionListner;
import com.borqs.common.listener.FriendsActionListner;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.ui.bpc.CircleFragmentActivity;
import com.borqs.qiupu.ui.bpc.InvitePeopleMainActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;
import com.borqs.qiupu.ui.bpc.PickUserFragmentActivity;
import com.borqs.qiupu.ui.bpc.UserCircleSelectedActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class CircleItemView extends SNSItemView {

    private static final String TAG = "CircleItemView";
    private ImageView icon;
    private TextView title;
    private TextView tv_member_count;
    private View id_layout_title;
//    private View id_layout_icon;
    private UserCircle mCircle;
    private CheckBox  checkbox;
    private ImageView deleteCircle;
    private View create_new_circle;
    private FriendsActionListner action; 
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap;
    
    public UserCircle getCircle() {
        return mCircle;
    }

    public CircleItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public CircleItemView(Context context, UserCircle circle) {
        super(context);
        if(FriendsActionListner.class.isInstance(mContext))
		{
			action = (FriendsActionListner) context;
		}			

        mCircle = circle;
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
        View convertView = factory.inflate(R.layout.circle_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(convertView);

        icon = (ImageView) convertView.findViewById(R.id.id_circle_icon);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)icon.getLayoutParams();
        if(mContext instanceof BpcFriendsFragmentActivity) {
        	params.height = (int)mContext.getResources().getDimension(R.dimen.stream_row_size_profile_icon);
        	params.width = (int)mContext.getResources().getDimension(R.dimen.stream_row_size_profile_icon);
        }else {
        	params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        	params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
//        id_layout_icon = convertView.findViewById(R.id.id_layout_title);
        id_layout_title = convertView.findViewById(R.id.id_layout_title);
        title = (TextView) convertView.findViewById(R.id.id_circle_name);
        tv_member_count = (TextView) convertView.findViewById(R.id.tv_member_count);
        deleteCircle = (ImageView) convertView.findViewById(R.id.circle_delete);
        checkbox = (CheckBox) convertView.findViewById(R.id.circle_check);
        create_new_circle = convertView.findViewById(R.id.create_new_circle);
        
        checkbox.setOnClickListener(stOnClik);
        deleteCircle.setOnClickListener(deleteOnClick);
        setUI();
    }

    public void setCircle(UserCircle user) {
        mCircle = user;
        setUI();
    }

    private void setUI() {
        if (mCircle != null) {
        	icon.setVisibility(View.VISIBLE);
            id_layout_title.setVisibility(View.VISIBLE);
        	setDeleteIcon(mCircle.circleid);
//        	title.setText(CircleUtils.getCircleNameWithCount(mContext, mCircle.circleid, mCircle.name, mCircle.memberCount));
        	title.setText(CircleUtils.getCircleName(mContext, mCircle.circleid, mCircle.name));
        	if(mContext instanceof BpcFriendsFragmentActivity) {
        		tv_member_count.setVisibility(View.VISIBLE);
        	}else {
        		tv_member_count.setVisibility(View.GONE);
        	}
        	tv_member_count.setText(String.format(mContext.getString(R.string.str_people), mCircle.memberCount));
            if(BpcFriendsFragmentActivity.class.isInstance(mContext)
            		|| CircleFragmentActivity.class.isInstance(mContext)
            		|| BpcSearchActivity.class.isInstance(mContext)) {
            	checkbox.setVisibility(View.GONE);
            }
            else if(UserCircleSelectedActivity.class.isInstance(mContext)
            		|| PickCircleUserActivity.class.isInstance(mContext)
            		|| PickUserFragmentActivity.class.isInstance(mContext)
            		|| InvitePeopleMainActivity.class.isInstance(mContext)
            		|| PickAudienceActivity.class.isInstance(mContext))
            {
            	checkbox.setVisibility(View.VISIBLE);
            	checkbox.setChecked(mCircle.selected);
            	deleteCircle.setVisibility(View.GONE);
            }
            else
            {
            	checkbox.setVisibility(View.VISIBLE);
            	checkbox.setChecked(mCircle.selected);
            	deleteCircle.setVisibility(View.VISIBLE);
            }
            create_new_circle.setVisibility(View.GONE);
            if(UserCircle.CIRCLE_TYPE_LOCAL == mCircle.type) {
            	if(mCircle.circleid == QiupuConfig.CIRCLE_ID_PUBLIC) {
            		icon.setImageResource(R.drawable.list_public);
            	}else {
            		icon.setImageResource(R.drawable.list_circle);
            	}
//                shootImageRunner(mCircle.profile_image_url, icon,R.drawable.default_public_circle);
            }else if(UserCircle.CIRLCE_TYPE_PUBLIC == mCircle.type) {
            	if(mContext instanceof BpcFriendsFragmentActivity) {
            		shootImageRunner(mCircle.profile_image_url, icon,R.drawable.default_public_circle);
            	}else {
            		icon.setImageResource(R.drawable.list_public_circle);
            	}
                deleteCircle.setVisibility(View.GONE);
            }else if(UserCircle.CIRCLE_TYPE_EVENT == mCircle.type) {
            	icon.setImageResource(R.drawable.list_event);
//            	shootImageRunner(mCircle.profile_image_url, icon,R.drawable.default_public_circle);
            	deleteCircle.setVisibility(View.GONE);
            }
        }
        else {
        	create_new_circle.setVisibility(View.VISIBLE);
        	id_layout_title.setVisibility(View.GONE);
        	icon.setVisibility(View.GONE);
//        	title.setText(mContext.getString(R.string.create_new_circle));
        	checkbox.setVisibility(View.GONE);
        	deleteCircle.setVisibility(View.VISIBLE);
        	if(UserCircleSelectedActivity.class.isInstance(mContext)
        			|| BpcFriendsFragmentActivity.class.isInstance(mContext))
        	{
        		deleteCircle.setVisibility(View.GONE);
        	}
        }
        
    }
    
    private void shootImageRunner(String photoUrl,final ImageView img,int imageRes) {
		// Get singletone instance of ImageLoader
				ImageLoader imageLoader = QiupuApplication.getApplication(img.getContext()).getImageLoader();
				// Creates display image options for custom display task (all options are optional)
				DisplayImageOptions options = new DisplayImageOptions.Builder()
				           .resetViewBeforeLoading().showStubImage(imageRes).showImageForEmptyUri(imageRes)
				           .cacheInMemory()
				           .cacheOnDisc()
				           .loadFromWeb(!QiupuORM.isDataFlowAutoSaveMode(getContext()))
				           .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				           .bitmapConfig(Bitmap.Config.RGB_565)
//				           .displayer(new RoundedBitmapDisplayer(5))
				           .build();
				// Load and display image asynchronously
				imageLoader.displayImage(photoUrl, img,options,null );
	}
    
    View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			switchCheck();
			if (null != mCheckClickListenerMap) {
                Collection<CheckBoxClickActionListener> listeners = mCheckClickListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    CheckBoxClickActionListener checkListener = (CheckBoxClickActionListener)it.next();
                    checkListener.changeItemSelect(mCircle.circleid, mCircle.name, mCircle.selected, false);
                }
            }
		}
	};
	
	View.OnClickListener deleteOnClick = new View.OnClickListener()
	{
		
		public void onClick(View arg0)
		{
			gotoFragmentAction();
		}
	};
    public void switchCheck()
	{
    	mCircle.selected = !mCircle.selected;
    	checkbox.setChecked(mCircle.selected);			
		Log.d(TAG, "onClick select ="+mCircle.selected);
		if(PickCircleUserActivity.class.isInstance(mContext))
		{
			PickCircleUserActivity pa = (PickCircleUserActivity) mContext;
			pa.changeSelect(mCircle.circleid, mCircle.selected, false);
		}
	}
    
    private void setDeleteIcon(long circleId){
    	if(circleId == QiupuConfig.BLOCKED_CIRCLE 
    			|| circleId == QiupuConfig.ADDRESS_BOOK_CIRCLE
    			|| circleId == QiupuConfig.DEFAULT_CIRCLE
    			|| circleId == QiupuConfig.FAMILY_CIRCLE
    			|| circleId == QiupuConfig.CLOSE_FRIENDS_CIRCLE
    			|| circleId == QiupuConfig.ACQUAINTANCE_CIRCLE )
    	{
    		deleteCircle.setVisibility(View.GONE);
    	}
    	else {
    		deleteCircle.setVisibility(View.VISIBLE);
    	}
    }
    
    public UserCircle getItemView(){
    	return mCircle;
    }
    
    public boolean isCircleSelected(){
        return mCircle.selected;
    }
    public long getDataItemId(){
        return mCircle.circleid;
    }
    
    private void gotoFragmentAction(){

		synchronized(QiupuHelper.circleActionlisteners)
        {
            Set<String> set = QiupuHelper.circleActionlisteners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                CircleActionListner listener = QiupuHelper.circleActionlisteners.get(key).get();
                listener.deleteCircle(mCircle);
            }      
        }      
    }
    
    public void attachCheckListener(HashMap<String, CheckBoxClickActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }
    
    public boolean refreshCheckBox(long circleid) {
    	if(mCircle.circleid == circleid) {
    		mCircle.selected = !mCircle.selected;
        	checkbox.setChecked(mCircle.selected);
        	return true;
    	}
    	return false;
    }
}

