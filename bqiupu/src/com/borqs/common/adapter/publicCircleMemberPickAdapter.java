
package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.view.PublicCircleMemberPickItemView;
import com.borqs.qiupu.db.QiupuORM;

public class publicCircleMemberPickAdapter extends BaseAdapter{
    private static final String TAG = "publicCircleMemberPickAdapter";
    private ArrayList<PublicCircleRequestUser> itemList = new ArrayList<PublicCircleRequestUser>();
    private Context      mContext;
    private QiupuORM orm;
    private int mStatus;
    private UserCircle mCircle;
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
    
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap = new HashMap<String, CheckBoxClickActionListener>();
    public void registerCheckClickActionListener(String key, CheckBoxClickActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }
    
    public publicCircleMemberPickAdapter(Context context) {
        mContext = context;
    }
    public int getCount() {
        return itemList.size();
    }
    
    public PublicCircleRequestUser getItem(int position) {
        PublicCircleRequestUser tmpuser = itemList.get(position); 
        postSetupSimpleUser(tmpuser);
        return tmpuser;
    }
    
    public long getItemId(int position) {		
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;		
        PublicCircleRequestUser user = getItem(position);
        if(user != null)
        {
            PublicCircleMemberPickItemView FView;
            if (convertView == null || false == (convertView instanceof PublicCircleMemberPickItemView) )
            {
                FView = new PublicCircleMemberPickItemView(mContext, user, mStatus, mCircle);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                FView.attachCheckListener(mCheckClickListenerMap);
                convertView = FView;
                
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.attachCheckListener(mCheckClickListenerMap);
                holder.view.setUserItem(user);
                holder.view.setStatus(mStatus);
                }
            
            return convertView;
        }
        return convertView;
        
    }

    static class ViewHolder
    {
        public PublicCircleMemberPickItemView view;
    }
    
    public void alterDataList(ArrayList<PublicCircleRequestUser> userList) {
        itemList.clear();
        itemList.addAll(userList);
        
        notifyDataSetChanged();
    }

    public void clearSelect() {
        if(itemList != null) {
            for(int i=0; i<itemList.size(); i++) {
                itemList.get(i).selected = false;
            }
        }
        notifyDataSetChanged();
    }	
    
    public void setSelect(HashSet<Long> set) {
        mSelectedUser.clear();
        mSelectedUser.addAll(set);
    }
    
    private void postSetupSimpleUser(PublicCircleRequestUser info) {
        if (null != info) {
            
            if (mSelectedUser.contains(info.uid)) {
                info.selected = true;
            }
        }
    }
}