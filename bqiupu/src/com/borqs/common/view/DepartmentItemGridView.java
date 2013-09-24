package com.borqs.common.view;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.borqs.common.adapter.DepartmentGridviewAdapter;
import com.borqs.qiupu.R;

public class DepartmentItemGridView extends SNSItemView {
	private final String TAG = DepartmentItemGridView.class.getSimpleName();

	private MyGridView mGridview;
	private DepartmentGridviewAdapter mAdatper;
	private Cursor mCursor;
	private AdapterView.OnItemClickListener mGridItemClickListener;
	
    public DepartmentItemGridView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public DepartmentItemGridView(Context context, Cursor cursor, AdapterView.OnItemClickListener gridItemClickListener) {
        super(context);
        mCursor = cursor;
        mGridItemClickListener = gridItemClickListener;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
    	removeAllViews();

        // child 1
    	mGridview = (MyGridView) LayoutInflater.from(mContext).inflate(
        		R.layout.circle_circles_gridview, null);
        addView(mGridview);
        mGridview.setNumColumns(3);
    	mGridview.setGravity(Gravity.CENTER);
    	mGridview.setHorizontalSpacing(10);
        mAdatper = new DepartmentGridviewAdapter(mContext);
        mGridview.setAdapter(mAdatper);
        if(mGridItemClickListener != null) {
        	mGridview.setOnItemClickListener(mGridItemClickListener);
        }
    	 setUI();
    }

    public void setDataInfo(Cursor cursor) {
//    	if(mCursor != null) {
//    		mCursor.close();
//    	}
    	mCursor = cursor; 
        setUI();
    }

    private void setUI() {
    	if(mAdatper != null) {
    		mAdatper.alertData(mCursor);
    	}else {
    		Log.e(TAG, "mAdapter is null");
    	}
		
    }
}
