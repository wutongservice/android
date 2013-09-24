package com.borqs.common.view;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.QiupuSimpleUser;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;

public class UserSelectItemView extends SNSItemView 
{
	private final String TAG="UserSelectItem";
	
	private ImageView usericon;	
	private TextView  username;
	private CheckBox  chekbox;
	private boolean isDialogUser = false;
	private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap;
	
	QiupuSimpleUser mUser;
	
	public UserSelectItemView(Context context, QiupuSimpleUser di) {
		super(context);
		mContext = context;
		mUser = di;
		
		init();
	} 
	
	public UserSelectItemView(Context context, QiupuSimpleUser di, boolean isDialoguser) {
        super(context);
        mContext = context;
        mUser = di;
        isDialogUser = isDialoguser;
        init();
	}
    
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public String getName()
	{
		return mUser.nick_name;
	}	
	
	public long getUserID()
	{
		return mUser.uid;
	}	
	
	public boolean isSelected()
	{
		return mUser.selected;
	}
	
	private void init() 
	{
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();

		//child 1
		View v  = factory.inflate(R.layout.user_select_list_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(v);
		
		chekbox    = (CheckBox)v.findViewById(R.id.user_check);
		username   = (TextView)v.findViewById(R.id.user_name);
		usericon   = (ImageView)v.findViewById(R.id.user_icon);
		
		chekbox.setOnClickListener(stOnClik);
		setUI();	
	}
	
	private void setUI()
	{
		username.setText(mUser.nick_name);
		
		usericon.setImageResource(R.drawable.default_user_icon);
		
		ImageRun imagerun = new ImageRun(null, mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = false;        
        imagerun.addHostAndPath = true;
        imagerun.setImageView(usericon);        
        imagerun.post(null);
        
        chekbox.setVisibility(isDialogUser ? View.GONE : View.VISIBLE);
		chekbox.setChecked(mUser.selected);
	}
	
	public void setUserItem(QiupuSimpleUser  di) 
	{
	    mUser = di;
	    setUI();
	}
	
	public void switchCheck()
	{
		mUser.selected = !mUser.selected;
		chekbox.setChecked(mUser.selected);
		 if(PickCircleUserActivity.class.isInstance(mContext))
		 {
			PickCircleUserActivity pa = (PickCircleUserActivity) mContext;
			pa.changeSelect(mUser.uid, isSelected(), true);
		 }
		 
		Log.d(TAG, "onClick select ="+ mUser.selected);
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
                    checkListener.changeItemSelect(mUser.uid, mUser.nick_name, mUser.selected, true);
                }
            }
		}
	};
	
	@Override
	public String getText() 
	{		
		return mUser !=null? mUser.name:"";
	}

    public void gotoUserProfile() {
        IntentUtil.startUserDetailIntent(getContext(), mUser);
    }

    public static AdapterView.OnItemClickListener userClickListener = new AdapterView.OnItemClickListener() {
        private String TAG="AdapterView.OnItemClickListener";
        public void onItemClick(AdapterView<?> adv, View v, int pos, long ID) {
            if(QiupuConfig.DBLOGD) Log.d(TAG, "onItemClick, item clicked");
            if (UserSelectItemView.class.isInstance(v)) {
                UserSelectItemView siv = (UserSelectItemView)v;
                siv.gotoUserProfile();
            }
        }
    };
    
    public void attachCheckListener(HashMap<String, CheckBoxClickActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }
    
    public boolean refreshCheckBox(long uid) {
    	if(mUser.uid == uid) {
    		mUser.selected = !mUser.selected;
    		chekbox.setChecked(mUser.selected);
    		return true;
    	}
    	return false;
    }
}
