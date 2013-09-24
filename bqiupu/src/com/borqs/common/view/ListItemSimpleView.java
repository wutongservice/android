package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.qiupu.R;

public class ListItemSimpleView extends SNSItemView {
    private final String TAG="ListItemSimpleView";
    
    private ImageView mIcon; 
    private TextView mText1;
    private TextView mText2;
    private Context mContext;
    
    public ListItemSimpleView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public ListItemSimpleView(Context context) 
    {       
        super(context);
        mContext = context;
        init();
    }

    private void init() 
    {
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        View v  = factory.inflate(R.layout.list_item_simple_view, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    /*LayoutParams.WRAP_CONTENT*/(int) mContext.getResources().getDimension(R.dimen.circle_list_item_height)));
        addView(v);            
        
        mIcon  = (ImageView)v.findViewById(R.id.icon);
        mText1 = (TextView)v.findViewById(R.id.text1);       
        mText2 = (TextView)v.findViewById(R.id.text2);
        
//        setCommentsUI();        
    
    }   

    public void setItemUI(int iconRes, int text1Res, int text2Res)
    { 
    	mIcon.setImageResource(iconRes);
    	if(text1Res > 0) {
    		mText1.setVisibility(View.VISIBLE);
    		mText1.setText(text1Res);
    	}else {
    		mText1.setVisibility(View.GONE);
    	}
    	if(text2Res > 0) {
    		mText2.setVisibility(View.VISIBLE);
    		mText2.setText(text2Res);
    	}else {
    		mText2.setVisibility(View.GONE);
    	}
    }
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    

	@Override
	public String getText() 
	{		
		return "";
	}
}
