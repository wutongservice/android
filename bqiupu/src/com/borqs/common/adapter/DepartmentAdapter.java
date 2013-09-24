package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Circletemplate;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.common.util.AlphaPost;
import com.borqs.common.view.DepartmentItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class DepartmentAdapter extends BaseAdapter {

    private static final String TAG = "DepartmentAdapter";

    private Context             mContext;
    private ArrayList<UserCircle> mDataList = new ArrayList<UserCircle>();
    private boolean mIsGrid;
    private Cursor mReferCircleCursor;
    private Cursor mFirstCursor;
    private Cursor mSecondCirclesCursor;
    private Cursor mThirdCirclesCursor;
    private Cursor mFreeCirclesCursor;
    private String mCircleSubType;
    

    public DepartmentAdapter(Context context,ArrayList<UserCircle> dataList) {
        mContext = context;
        mDataList.addAll(dataList);
    }
    
    public DepartmentAdapter(Context context,ArrayList<UserCircle> dataList, boolean isgird) {
        mContext = context;
        mDataList.addAll(dataList);
        mIsGrid = isgird;
    }
    
    public DepartmentAdapter(Context context, String circleSubType) {
        mContext = context;
        mCircleSubType = circleSubType;
    }

    public DepartmentAdapter(Context context, Cursor refrerCursor, Cursor freeCircleCursor, Cursor firstCursor, Cursor secondCursor, Cursor thirdCursor) {
        super();

        mContext = context;
        mReferCircleCursor = refrerCursor;
        mFreeCirclesCursor = freeCircleCursor;
        mFirstCursor = firstCursor;
        mSecondCirclesCursor = secondCursor;
        mThirdCirclesCursor = thirdCursor;
//        generateItem();
    }
    
    public void alterDataList(ArrayList<UserCircle> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }
    
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

    public int getCount() {
    	int count = 0;
    	if(mDataList != null && mDataList.size() > 0) {
    		count = mDataList.size(); 
        }else {
            if(mReferCircleCursor != null && mReferCircleCursor.getCount() > 0)
                count = count + mReferCircleCursor.getCount() + 1;
            if(mFirstCursor != null && mFirstCursor.getCount() > 0)
                count = count + mFirstCursor.getCount() + 1;
            if(mSecondCirclesCursor != null &&mSecondCirclesCursor.getCount() > 0) {
            	count = count + mSecondCirclesCursor.getCount() + 1;
            }
            if(mThirdCirclesCursor != null && mThirdCirclesCursor.getCount() > 0) {
            	count = count + mThirdCirclesCursor.getCount() + 1;
            }
            if(mFreeCirclesCursor != null && mFreeCirclesCursor.getCount() > 0) {
            	count = count + mFreeCirclesCursor.getCount() + 1;
            }
        }
    	return count;		
    }

    public UserCircle getItem(int position) {
    	if(mDataList != null && mDataList.size() > 0) {
    		 if(position >= mDataList.size()) {
    	            return null;
    	        }
    	        return mDataList.get(position);
        }else {
            Integer newposition = posMap.get(new Long(position));
            if(debugsort)
                Log.d(TAG, "get Item position="+position + " map to="+ ( + newposition));
            if(newposition != null && newposition >= 0){
            	if(mReferCircleCursor != null && mReferCircleCursor.moveToPosition(newposition)) {
            		UserCircle circle = QiupuORM.createCircleCircles(mReferCircleCursor);
                    return circle;
            	} else if(mFirstCursor != null && mFirstCursor.moveToPosition(newposition - getReferCircleCount())) {
            		UserCircle circle = QiupuORM.createCircleCircles(mFirstCursor);
                    return circle;
            	}else if(mSecondCirclesCursor != null && mSecondCirclesCursor.moveToPosition(newposition - getReferCircleCount()- getDepCircleCount())) {
            		UserCircle circle = QiupuORM.createCircleCircles(mSecondCirclesCursor);
                    return circle;
            	} else if(mThirdCirclesCursor != null && mThirdCirclesCursor.moveToPosition(newposition - getReferCircleCount() - getDepCircleCount()- getProCircleCount())){
                    UserCircle circle = QiupuORM.createCircleCircles(mThirdCirclesCursor);
                    return circle;
                } else if(mFreeCirclesCursor != null && mFreeCirclesCursor.moveToPosition(newposition - getReferCircleCount() - getDepCircleCount() - getProCircleCount()- getAppCircleCount())) {
                	UserCircle circle = QiupuORM.createCircleCircles(mFreeCirclesCursor);
                    return circle;
                }else{
                	Log.d(TAG, "getitem: null");
                    return null;
                }
            }else{
                return null;
            }
        }
       
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle depInfo = getItem(position);
        if (depInfo != null) {
            if (convertView == null || false == (convertView instanceof DepartmentItemView)) {
            	convertView = new DepartmentItemView(mContext, depInfo, mIsGrid);
                holder = new ViewHolder();

                holder.view = (DepartmentItemView)convertView;
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setDataInfo(depInfo);
            }
        }else {
        	Integer newposition = posMap.get(new Long(position));
        	
        	String dockStr = "";
        	if(newposition != null && newposition < 0)
        	{
        		final int pos = Math.abs(newposition);
        		dockStr = generateSpanValue(pos);
        		
        	}
        	TextView bt =  (TextView)generateSpanItem();
        	bt.setText(dockStr);
        	return bt;
        }
        return convertView;

    }
    
    private String generateSpanValue(int pos) {
    	String dockStr = "";
    	if(pos == 10000) {
			dockStr = mContext.getString(R.string.frequently_circle_label);
		}else if(pos == 20000) {
			if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircleSubType)) {
				dockStr = mContext.getString(R.string.department_circle_label);
			}else {
				dockStr = mContext.getString(R.string.child_circle_label_class);
			}
		}else if(pos == 30000) {
			dockStr = mContext.getString(R.string.project_circle_label);
		}else if(pos == 40000) {
			dockStr = mContext.getString(R.string.application_circle_label);
		}else if(pos == 50000) {
			dockStr = mContext.getString(R.string.interested_circle_label);
			
		}else {
			if(debugsort)
				Log.d(TAG, "pos" + pos + " " + dockStr);
		}
    	return dockStr;
    }
    
    private View generateSpanItem() {
	     TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
	     but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
	     but.setOnClickListener(null);
	     return but;
	 }

    static class ViewHolder {
        public DepartmentItemView view;
    }
    
    private ArrayList<AlphaPost> alphaPos   = new ArrayList<AlphaPost>();
    private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private final static boolean debugsort = false;
    private void generateItem() {
    	posMap.clear();
    	
    	if(mReferCircleCursor != null) {
    		final int referCircleCount = mReferCircleCursor.getCount();
    		if(referCircleCount > 0) {
    			posMap.put(new Long(posMap.size()), -10000);
    			alphaPos.add(new AlphaPost(generateSpanValue(10000), posMap.size()));
    			for(int i=0; i<referCircleCount; i++)
    			{
    				if(debugsort)
    					Log.d(TAG, "companyoriginal="+posMap.size() +" map to="+(referCircleCount == 0 ? -10000 : (-1*(i))));
    				posMap.put(new Long(posMap.size()), i);
    			}
    		}
    	}
    	
    	if(mFirstCursor != null) {
			 final int depCirclePos = mFirstCursor.getCount();
			 if(depCirclePos > 0) {
				 posMap.put(new Long(posMap.size()), -20000);
				 alphaPos.add(new AlphaPost(generateSpanValue(20000), posMap.size()));
				 for(int i=0; i<depCirclePos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "frequest circle original="+posMap.size() +" map to="+(depCirclePos == 0 ? -20000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getReferCircleCount());
				 }
			 }
		 }
    	
    	if(mSecondCirclesCursor != null) {
			 final int proCirclesPos = mSecondCirclesCursor.getCount();
			 if(proCirclesPos > 0) {
				 if(mSecondCirclesCursor != null && mSecondCirclesCursor.getCount() > 0) {
					 Log.d(TAG, "no need add span title.");
				 }else {
					 posMap.put(new Long(posMap.size()), -30000);
					 alphaPos.add(new AlphaPost(generateSpanValue(30000), posMap.size()));
				 }
				 
				 for(int i=0; i<proCirclesPos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "frequest user original="+posMap.size() +" map to="+(proCirclesPos == 0 ? -30000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getReferCircleCount() + getDepCircleCount() );
				 }
			 }
		 }
    	
    	if(mThirdCirclesCursor != null) {
    		final int appCirclePos = mThirdCirclesCursor.getCount();
    		if(appCirclePos > 0) {
    			posMap.put(new Long(posMap.size()), -40000);
    			 alphaPos.add(new AlphaPost(generateSpanValue(40000), posMap.size()));
    			for(int i=0; i<appCirclePos; i++)
    			{
    				if(debugsort)
    					Log.d(TAG, "circle original="+posMap.size() +" map to="+(appCirclePos == 0 ? -40000 : (-1*(i))));
    				posMap.put(new Long(posMap.size()), i + getReferCircleCount() + getDepCircleCount() + getProCircleCount());
    			}
    		}
    	}
    	
    	if(mFreeCirclesCursor != null) {
			 final int freeCirclePos = mFreeCirclesCursor.getCount();
			 if(freeCirclePos > 0)
			 {
				 posMap.put(new Long(posMap.size()), -50000);
				 alphaPos.add(new AlphaPost(generateSpanValue(50000), posMap.size()));
				 for(int i=0; i<freeCirclePos; i++)
				 {
				     if(debugsort)
				     Log.d(TAG, "event original="+posMap.size() +" map to="+(freeCirclePos == 0 ? -50000 : (-1*(i))));
				     posMap.put(new Long(posMap.size()), i + getReferCircleCount() + getDepCircleCount() + getProCircleCount() + getAppCircleCount());
				 }
			 }
		 }
    	
    	
    }
    
    private int getReferCircleCount() {
    	return mReferCircleCursor != null ? mReferCircleCursor.getCount() : 0;
    }
    
    private int getDepCircleCount() {
    	return mFirstCursor != null ? mFirstCursor.getCount() : 0;
    }
    
    private int getProCircleCount() {
    	return mSecondCirclesCursor != null ? mSecondCirclesCursor.getCount() : 0;
    }
    private int getAppCircleCount() {
    	return mThirdCirclesCursor != null ? mThirdCirclesCursor.getCount() : 0;
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
}
