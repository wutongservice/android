
package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Employee;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.view.TopSubMemberItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class TopSubCircleMemberAdapter extends BaseAdapter{
    private static final String TAG = "TopSubCircleMemberAdapter";
    private ArrayList<Employee> itemList = new ArrayList<Employee>();
    private Cursor mCursor;
    private Context mContext;
    private QiupuORM orm;
    private UserCircle mCircle;
    private MoreItemCheckListener mMoreItemCheckLisener;
    
    private HashMap<String, publicCirclePeopleActionListener> mActionListenerMap = new HashMap<String, publicCirclePeopleActionListener>();
    public void registerActionListener(String key, publicCirclePeopleActionListener rl) {
        mActionListenerMap.put(key, rl);
    }

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public TopSubCircleMemberAdapter(Context context, UserCircle circle) {
        mContext = context;
        orm = QiupuORM.getInstance(context);
        mCircle = circle;
    }
    
    public TopSubCircleMemberAdapter(Context context, UserCircle circle, MoreItemCheckListener moreClickListener) {
        mContext = context;
        orm = QiupuORM.getInstance(context);
        mCircle = circle;
        mMoreItemCheckLisener = moreClickListener;
    }

    public int getCount() {
    	int count;
    	if(mCursor != null) {
    		count = mCursor.getCount();
    	}else {
    		if (null == itemList) {
    			count = 0;
    		} else {
    			count = itemList.size();
    		}
    	}
    	if(mMoreItemCheckLisener != null && !mMoreItemCheckLisener.isMoreItemHidden()) {
    		count = count + 1;
    	}
    	return count;
    }
    
    public Employee getItem(int position) {
    	if(mCursor != null) {
    		if(mCursor.moveToPosition(position)) {
    			return QiupuORM.parseAllEmployeeCursor(mCursor);
    		}
    	}else {
    		if(position < itemList.size()) {
    			return itemList.get(position);
    		}
    	}
    	return null;
    }
    
    public long getItemId(int position) {		
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;		
        Employee user = getItem(position);
        if(user != null)
        {
        	TopSubMemberItemView FView;
            if (convertView == null || false == (convertView instanceof TopSubMemberItemView) )
            {
                FView = new TopSubMemberItemView(mContext, user, mCircle);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                FView.attachActionListener(mActionListenerMap);
                convertView = FView;
                
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.attachActionListener(mActionListenerMap);
                holder.view.setUserItem(user);
                }
            return convertView;
        }else {
        	return generateMoreItem();
        }
    }
    
    private View generateMoreItem() {
        if (null != mMoreItemCheckLisener) {
            Button but = generateMoreItemView();
            but.setOnClickListener(mMoreItemCheckLisener.getMoreItemClickListener());
            but.setText(mMoreItemCheckLisener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }
    
    private Button generateMoreItemView() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }

    static class ViewHolder
    {
        public TopSubMemberItemView view;
    }
    
    public void alterDataList(ArrayList<Employee> userList) {
        itemList.clear();
        itemList.addAll(userList);
        
        notifyDataSetChanged();
    }
    
    public void alterDataCursor(Cursor cursor) {
    	if(mCursor != null ) {
    		mCursor.close();
    	}
    	mCursor = cursor;
    	notifyDataSetChanged();
    }
    
    public void alterData(Cursor cursor, ArrayList<Employee> userList) {
    	if(mCursor != null ) {
    		mCursor.close();
    	}
    	mCursor = cursor;
    	
    	itemList.clear();
        itemList.addAll(userList);
    	notifyDataSetChanged();
    }
}