package com.borqs.common.adapter;

import java.util.HashMap;

import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.RequestItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class CircleMainListAdapter extends BaseAdapter{
	private static final String TAG = "CircleMainListAdapter";
	private Cursor mLocalCircles;
	private Cursor mOrgizationCircles;
	private Cursor mFreeCircles;
    private Context mContext;
    
	public CircleMainListAdapter(Context context){
		mContext = context;
	}
	
//	public CircleMainListAdapter(Context context, Cursor localCircles, Cursor publicCircles){
//        mContext = context;
//        if(mPublicCircles != null) {
//            mPublicCircles.close();
//        }
//        mPublicCircles = publicCircles;
//        
//        if(mLocalCircles != null) {
//            mLocalCircles.close();
//        }
//        mLocalCircles = localCircles;
//        generateSpanitem();
//    }
	
	public void alterCircles(Cursor localCircles, Cursor publicCircles){
	    if(mOrgizationCircles != null) {
	    	mOrgizationCircles.close();
	    }
	    mOrgizationCircles = publicCircles;
	    
	    if(mLocalCircles != null) {
            mLocalCircles.close();
        }
        mLocalCircles = localCircles;
        
        generateSpanitem();
		notifyDataSetChanged();
	}
	
	public void alterOrgizationCircles(Cursor orgizationCircles, Cursor freeCircles){
	    if(mOrgizationCircles != null) {
	    	mOrgizationCircles.close();
	    }
	    mOrgizationCircles = orgizationCircles;
	    
	    if(mFreeCircles != null) {
	    	mFreeCircles.close();
        }
	    mFreeCircles = freeCircles;
        
        generateSpanitem();
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    int count = 0;
        if(mOrgizationCircles != null && mOrgizationCircles.getCount() > 0) {
            count = count + mOrgizationCircles.getCount();
            if((mLocalCircles != null && mLocalCircles.getCount() > 0)
            		|| (mFreeCircles != null && mFreeCircles.getCount() > 0)){
                 count = count + 1;
            }
        }
        if(mFreeCircles != null && mFreeCircles.getCount() > 0) {
            count = count + mFreeCircles.getCount();
            if((mLocalCircles != null && mLocalCircles.getCount() > 0)
            		|| (mOrgizationCircles != null && mOrgizationCircles.getCount() > 0)){
                count = count + 1;
           }
        }
        if(mLocalCircles != null && mLocalCircles.getCount() > 0) {
            count = count + mLocalCircles.getCount();
            if((mOrgizationCircles != null && mOrgizationCircles.getCount() > 0)
            		|| (mFreeCircles != null && mFreeCircles.getCount() > 0)){
                count = count + 1;
           }
        }
        return count;
	}
	
	public UserCircle getItem(int position) {
	    
	    Integer newposition = posMap.get(new Long(position));
        if(debugsort)
            Log.d(TAG, "get Item position="+position + " map to="+newposition);
        if(newposition != null && newposition >= 0){
            if(mOrgizationCircles != null && mOrgizationCircles.moveToPosition(newposition)){
            	Log.d(TAG, "get Item position="+position + " map to="+newposition);
                UserCircle circle = QiupuORM.createPublicCircleListInfo(mContext, mOrgizationCircles);
                return circle;
            }else if(mFreeCircles != null && mFreeCircles.moveToPosition(newposition - getPublicCircleCount())) {
            	UserCircle circle = QiupuORM.createPublicCircleListInfo(mContext, mFreeCircles);
                return circle;
            }else if(mLocalCircles != null && mLocalCircles.moveToPosition(newposition - getPublicCircleCount())){
                UserCircle circle = QiupuORM.createCircleInformation(mLocalCircles);
                return circle;
            }else{
                return null;
            }
        }else{
            return null;
        }
	}
	
	public long getItemId(int position) {
	    UserCircle circle =  getItem(position);
		return circle !=null ? circle.type : -1;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle circle = getItem(position);
        Integer newposition = posMap.get(new Long(position));
        if (circle != null) {
            if (convertView == null
                    || false == (convertView instanceof CircleItemView)) {
                CircleItemView rView = new CircleItemView(mContext, circle);
                holder = new ViewHolder();

                rView.setTag(holder);
                holder.view = rView;

                convertView = rView;

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setCircle(circle);
            }

            return convertView;
        } else {
            String dockStr = "";
            if (newposition != null) {
                if (newposition < 0) {
                    final int pos = Math.abs(newposition);
                    if (pos == 1000) {
                        dockStr = mContext.getString(R.string.organization_circle_label);
                    } else if (pos == 2000) {
                        dockStr = mContext.getString(R.string.interested_circle_label);
                    } else if(pos == 3000) {
                    	dockStr = mContext.getString(R.string.local_circle_span);
                    }
                }
            }
            TextView but = generateSpanItem();
            but.setText(dockStr);
            return but;
        }

    }

	private TextView generateSpanItem() {
	    TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
        but.setOnClickListener(null);
        return but;
    }

	static class ViewHolder
	{
		public CircleItemView view;
	}
	
	private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private final static boolean debugsort = false;
    
    private void generateSpanitem()
    {
       posMap.clear();
       int publicCircleCount = 0;
       if(mOrgizationCircles != null) {
           publicCircleCount = mOrgizationCircles.getCount();
           if(publicCircleCount > 0) {
               if((mLocalCircles != null && mLocalCircles.getCount() > 0) || 
            		   (mFreeCircles != null && mFreeCircles.getCount() > 0)) {
                   posMap.put(new Long(posMap.size()), -1000);
               }
               for(int i=0; i<publicCircleCount; i++) {
                   posMap.put(new Long(posMap.size()), i);
               }  
           }
       }
       
       if(mFreeCircles != null) {
           int freecircleCount = mFreeCircles.getCount();
           if(freecircleCount > 0) {
               if((mLocalCircles != null && mLocalCircles.getCount() > 0) || 
            		   (mOrgizationCircles != null && mOrgizationCircles.getCount() > 0)) {
                   posMap.put(new Long(posMap.size()), -2000);
               }
               for(int i=0; i<freecircleCount; i++) {
                   posMap.put(new Long(posMap.size()), i + publicCircleCount);
               }  
           }
       }
       
       if(mLocalCircles != null) {
           int localCircleCount = mLocalCircles.getCount(); 
           if(localCircleCount > 0) {
               if(mOrgizationCircles != null && mOrgizationCircles.getCount() > 0
            		   || (mFreeCircles != null && mFreeCircles.getCount() > 0)) {
                   posMap.put(new Long(posMap.size()), -3000);
               }
               for(int i=0; i<localCircleCount; i++) {
                   posMap.put(new Long(posMap.size()), i + publicCircleCount);
               }  
           }
       }
    }
    
    private int getPublicCircleCount()
    {
        return mOrgizationCircles != null ? mOrgizationCircles.getCount() : 0;
    }
    
    public void clearCursor() {
    	QiupuORM.closeCursor(mLocalCircles);
    	QiupuORM.closeCursor(mOrgizationCircles);
    	QiupuORM.closeCursor(mFreeCircles);
    }
}
