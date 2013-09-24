package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Circletemplate;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.borqs.common.view.DepartmentItemGridView;
import com.borqs.qiupu.R;

public class DepartmentExpnadleAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "DepartmentAdapter";

    private Context             mContext;
    private ArrayList<UserCircle> mDataList = new ArrayList<UserCircle>();
    private Cursor mReferCircleCursor;
    private Cursor mFirstCursor;
    private Cursor mSecondCirclesCursor;
    private Cursor mThirdCirclesCursor;
    private Cursor mFreeCirclesCursor;
    private String mCircleSubType;
    
    private AdapterView.OnItemClickListener mGridItemClickListener;
    
    public DepartmentExpnadleAdapter(Context context,ArrayList<UserCircle> dataList) {
        mContext = context;
        mDataList.addAll(dataList);
    }
    
    public DepartmentExpnadleAdapter(Context context,ArrayList<UserCircle> dataList, boolean isgird) {
        mContext = context;
        mDataList.addAll(dataList);
    }
    
    public DepartmentExpnadleAdapter(Context context, String circleSubType, AdapterView.OnItemClickListener gridItemClickListener) {
        mContext = context;
        mCircleSubType = circleSubType;
        mGridItemClickListener = gridItemClickListener;
    }

    public void alterDataList(ArrayList<UserCircle> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }
    
    private ArrayList<String> parentLists = new ArrayList<String>();
    private HashMap<String, Cursor> childLists = new HashMap<String, Cursor>();
    public void alterDataList(Cursor refrerCursor, Cursor freeCircleCursor, Cursor firstCursor, Cursor secondCursor, Cursor thirdCursor) {
    	if(mReferCircleCursor != null) {
    		mReferCircleCursor.close();
    	}
    	mReferCircleCursor = refrerCursor;
    	
    	if(mFreeCirclesCursor != null) {
    		mFreeCirclesCursor.close();
    	}
    	mFreeCirclesCursor = freeCircleCursor;
    	
    	if(mFirstCursor != null) {
    		mFirstCursor.close();
    	}
    	mFirstCursor = firstCursor;

    	if(mSecondCirclesCursor != null) {
    		mSecondCirclesCursor.close();
    	}
    	mSecondCirclesCursor = secondCursor;

    	if(mThirdCirclesCursor != null) {
    		mThirdCirclesCursor.close();
    	}
    	mThirdCirclesCursor = thirdCursor;

    	generateItem();
    	notifyDataSetChanged();
    }

    static class ViewHolder {
        public DepartmentItemGridView view;
    }
    
    private void generateItem() {
    	parentLists.clear();
    	childLists.clear();
    	String dockStr = "";
    	if(mReferCircleCursor != null && mReferCircleCursor.getCount() > 0) {
    		dockStr = mContext.getString(R.string.frequently_circle_label);
    		parentLists.add(dockStr);
    		childLists.put(dockStr, mReferCircleCursor);
    	}
    	
    	if(mFirstCursor != null && mFirstCursor.getCount() > 0) {
    		if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircleSubType)) {
    			dockStr = mContext.getString(R.string.department_circle_label);
    		}else {
    			dockStr = mContext.getString(R.string.child_circle_label_class);
    		}
    		parentLists.add(dockStr);
    		childLists.put(dockStr, mFirstCursor);
		 }
    	
    	if(mSecondCirclesCursor != null && mSecondCirclesCursor.getCount() > 0) {
    		dockStr = mContext.getString(R.string.project_circle_label);
    		parentLists.add(dockStr);
    		childLists.put(dockStr, mSecondCirclesCursor);
		 }
    	
    	if(mThirdCirclesCursor != null && mThirdCirclesCursor.getCount() > 0) {
    		dockStr = mContext.getString(R.string.application_circle_label);
    		parentLists.add(dockStr);
    		childLists.put(dockStr, mThirdCirclesCursor);
    	}
    	
    	if(mFreeCirclesCursor != null && mFreeCirclesCursor.getCount() > 0) {
    		dockStr = mContext.getString(R.string.interested_circle_label);
    		parentLists.add(dockStr);
    		childLists.put(dockStr, mFreeCirclesCursor);
		 }
    }
    
    public void closeAllCursor() {
    	if(mReferCircleCursor != null) {
    		mReferCircleCursor.close();
    	}
    	if(mFirstCursor != null) {
    		mFirstCursor.close();
    	}
    	if(mSecondCirclesCursor != null) {
    		mSecondCirclesCursor.close();
    	}
    	if(mThirdCirclesCursor != null) {
    		mThirdCirclesCursor.close();
    	}
    	if(mFreeCirclesCursor != null) {
    		mFreeCirclesCursor.close();
    	}
    }

	@Override
	public int getGroupCount() {
		return parentLists.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public String getGroup(int groupPosition) {
		return parentLists.get(groupPosition);
	}

	@Override
	public Cursor getChild(int groupPosition, int childPosition) {
		Cursor childCurosr = childLists.get(parentLists.get(groupPosition));
		return childCurosr;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		Log.d(TAG, "getGroupView: " + groupPosition);
//		if(groupPosition < parentLists.size()) {
			TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.default_textview, null));
			but.setGravity(Gravity.CENTER_VERTICAL);
			but.setHeight((int)mContext.getResources().getDimension(R.dimen.title_bar_height));
			but.setBackgroundResource(R.drawable.bg_tacos);
			but.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.relationship_follower), null, null, null);
			but.setPadding((int)mContext.getResources().getDimension(R.dimen.large_text_padding_left), 0, 0, 0);
			but.setCompoundDrawablePadding((int)mContext.getResources().getDimension(R.dimen.default_padding));
			but.setText(parentLists.get(groupPosition));
			return but;
//		}else {
//			return null;
//		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null || false == (convertView instanceof DepartmentItemGridView)) {
        	convertView = new DepartmentItemGridView(mContext, getChild(groupPosition, childPosition), mGridItemClickListener);
            holder = new ViewHolder();

            holder.view = (DepartmentItemGridView)convertView;
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.view.setDataInfo(getChild(groupPosition, childPosition));
        }
        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
