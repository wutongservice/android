package com.borqs.common.view;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import twitter4j.QiupuUser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;

public class MulitPhoneEmailSelectItemView extends SNSItemView {
    private final String TAG="RequestContactInfoSimpleView";
    
    private TextView contactInfoitem;
    private CheckBox mCheckBox;
    private Context  mContext;
    private String   itemString;
    private String   mName;
    private boolean mSelected;
    private int mType;
    
    public MulitPhoneEmailSelectItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
    }

    public MulitPhoneEmailSelectItemView(Context context, String item, String name, int type) 
    {       
        super(context);
        mContext = context;
        itemString = item;
        mName = name;
        mType = type;
        init();
        setOnClickListener(stOnClick);
    }
    
    private void init() 
    {
        //Log.d(TAG,  "call MulitPhoneEmailSelectItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.mulit_phone_email_select_item_view, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(v);            
        
        contactInfoitem  = (TextView)v.findViewById(R.id.id_vcard_tv);
        mCheckBox = (CheckBox) v.findViewById(R.id.select_cb);
        mCheckBox.setOnClickListener(stOnClick);
        setUI();        
    }   
    
    private void setUI() { 
        if(mType == QiupuConfig.TYPE_PHONE) {
            contactInfoitem.setText(String.format(mContext.getString(R.string.user_detail_phone_number, itemString)));    
        }else if(mType == QiupuConfig.TYPE_EMAIL) {
            contactInfoitem.setText(String.format(mContext.getString(R.string.user_detail_email, itemString)));
        }
    }
    
    public void switchCheck() {
        mSelected = !mSelected;
        mCheckBox.setChecked(mSelected);
    }
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    public void setContactInfoItem(String item) 
    {
    	itemString = item;
        setUI();
    }   

    View.OnClickListener stOnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            switchCheck();
            changeActivitySelected();
        }
    };
    
	@Override
	public String getText() {		
		return itemString;
	}
	
    public void changeActivitySelected() {

        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                checkPhoneEmailItemListener listener = listeners.get(key).get();
                if (listener != null) {
                    listener.selectItem(itemString, mName, mCheckBox.isChecked());
                }
            }
        }
    }
	
	public interface checkPhoneEmailItemListener {
	    public void selectItem(String content, String name, boolean isSelected);
	}
	
	public static final HashMap<String,WeakReference<checkPhoneEmailItemListener>> listeners = new HashMap<String,WeakReference<checkPhoneEmailItemListener>>();
	
	public static void registerCheckPhoneEmailItemListener(String key,checkPhoneEmailItemListener listener){
	    synchronized(listeners) {
	        WeakReference<checkPhoneEmailItemListener> ref = listeners.get(key);
	        if(ref != null && ref.get() != null) {
	            ref.clear();
	        }
	        listeners.put(key, new WeakReference<checkPhoneEmailItemListener>(listener));
	    }
	}
	
	public static void unRegisterCheckPhoneEmailItemListener(String key){
	    synchronized(listeners) {
	        WeakReference<checkPhoneEmailItemListener> ref = listeners.get(key);
	        if(ref != null && ref.get() != null) {
	            ref.clear();
	        }
	        listeners.remove(key);
	    }
	}
}
