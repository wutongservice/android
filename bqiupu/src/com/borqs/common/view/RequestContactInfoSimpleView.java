package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.borqs.qiupu.R;

public class RequestContactInfoSimpleView extends SNSItemView {
    private final String TAG="RequestContactInfoSimpleView";
    
    private TextView contactInfoitem;
    private Context  mContext;
    private String   itemString;
    
    public RequestContactInfoSimpleView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
    }

    public RequestContactInfoSimpleView(Context context, String item) 
    {       
        super(context);
        mContext = context;
        itemString = item;
        init();
    }
    
    private void init() 
    {
        //Log.d(TAG,  "call RequestContactInfoSimpleView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.request_contactinfo_item_view, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(v);            
        
        contactInfoitem  = (TextView)v.findViewById(R.id.id_vcard_tv);
        
        setUI();        
    
    }   
    
    
    private void setUI()
    { 
    	contactInfoitem.setText(itemString);
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

	@Override
	public String getText() 
	{		
		return itemString;
	}
}
