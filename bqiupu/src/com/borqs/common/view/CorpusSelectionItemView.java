package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.borqs.common.SelectionItem;
import com.borqs.qiupu.R;

public class CorpusSelectionItemView extends SNSItemView {
    private final String TAG="CorpusSelectionItemView";

    private SelectionItem mItem;
    private TextView mTextView;
    private int mTextColor;
    private View mView;
    public CorpusSelectionItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;  
    }
    
    public CorpusSelectionItemView(Context ctx, SelectionItem item) { 
        super(ctx);
        mContext = ctx;
        mItem = item;
        init();
    }
    
    public CorpusSelectionItemView(Context ctx, SelectionItem item, int textColor) { 
        super(ctx);
        mContext = ctx;
        mItem = item;
        mTextColor = textColor;
        init();
    }

    private void init() 
    {
        removeAllViews();
        
        mView = LayoutInflater.from(mContext).inflate(R.layout.simple_textview, null);
        mView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        mTextView = (TextView)mView.findViewById(R.id.text_value);
        addView(mView);
        setUi();
    }
    
    private void setUi() {
    	if(mItem != null) {
    		mTextView.setText(mItem.value);
    		if(mItem.isSelected) {
    			mView.setBackgroundResource(R.drawable.list_selected_holo);
    		}else {
    			mView.setBackgroundResource(R.drawable.list_selector_background);
    		}
    		
    		if(mTextColor > 0) {
    			mTextView.setTextColor(mTextColor);
    		}
    	}
    }

    @Override
    public String getText() {
        return mItem.value;
    }   
    
    public String getItemId() {
        return mItem.id;
    }
    
    public SelectionItem getItem() {
        return mItem;
    }
    
    public void setSelectItem(SelectionItem item) {
        mItem = item;
        setUi();
    }

}
