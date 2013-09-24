
package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.InfoCategory;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.common.ExpandAnimation;
import com.borqs.qiupu.R;

public class InfoCategoryView extends SNSItemView {
    
    private static final String TAG = "Qiupu.InfoCategoryItemView";
    
    private TextView mFirstCategoryView;
    private LinearLayout mItemView;
    private ArrayList<InfoCategory> mCategories = new ArrayList<InfoCategory>();
    private long mScopeId;
    private InfoCategory mSelectCategory;
    
    public InfoCategoryView(Context context) {
        super(context);		
        mContext = context;
    }
    
    public InfoCategoryView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		mContext = ctx;
	}
    
    public InfoCategoryView(Context context, ArrayList<InfoCategory> list) {
        super(context);		
        mCategories.clear();
        mCategories.addAll(list);
    }
    
    public InfoCategoryView(Context context, ArrayList<InfoCategory> list, long scopeId) {
        super(context);		
        mCategories.clear();
        mCategories.addAll(list);
        mScopeId = scopeId;
    } 
    
    public void setScopeId(long scopeId) {
    	mScopeId = scopeId;
    }
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    private void init() {
        
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View convertView  = factory.inflate(R.layout.info_category_view, null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);
        
        mFirstCategoryView = (TextView) convertView.findViewById(R.id.first_category);
        mItemView = (LinearLayout) convertView.findViewById(R.id.category_item_view);
		((LinearLayout.LayoutParams)(mItemView.getLayoutParams())).bottomMargin = -100;
        
        mFirstCategoryView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ExpandAnimation expandAni = new ExpandAnimation(mItemView, 250);
				// Start the animation on the toolbar
				mItemView.startAnimation(expandAni);
			}
		});
        
        setUI();
    }

    public void setData(ArrayList<InfoCategory> list) {
    	mCategories.clear();
    	mCategories.addAll(list);
        setUI();
    }

    public void refreshUI() {
        setUI();
    }

    private void setUI() {
    	mItemView.removeAllViews();
        if(mCategories != null && mCategories.size() > 0) {
        	mFirstCategoryView.setText(mCategories.get(0).categoryName);
        	
        	for(int i=0; i<mCategories.size(); i++) {
        		final InfoCategory tmpCategory = mCategories.get(i);
        		TextView view = new TextView(mContext);
        		view.setText(tmpCategory.categoryName);
        		view.setHeight(40);
        		view.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mFirstCategoryView.setText(tmpCategory.categoryName);
						mSelectCategory = tmpCategory;
						ExpandAnimation expandAni = new ExpandAnimation(mItemView, 250);
						// Start the animation on the toolbar
						mItemView.startAnimation(expandAni);
					}
				});
        		
        		mItemView.addView(view);
        	}
        	
        	TextView view = new TextView(mContext);
        	final String tmpString = getResources().getString(R.string.no_category);
    		view.setText(tmpString);
    		view.setHeight(40);
    		view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mFirstCategoryView.setText(tmpString);
					mSelectCategory = new InfoCategory();
					mSelectCategory.categoryName = tmpString;
					//TODO animation
					ExpandAnimation expandAni = new ExpandAnimation(mItemView, 250);
					// Start the animation on the toolbar
					mItemView.startAnimation(expandAni);
				}
			});
    		mItemView.addView(view);
        	
        } else {
            Log.d(TAG, "category is null or size < 0");
        }
    }
    
    @Override
    public String getText() {		
        return null;
    }
    
    public InfoCategory getSelectCagegory() {
    	return mSelectCategory;
    }
}
