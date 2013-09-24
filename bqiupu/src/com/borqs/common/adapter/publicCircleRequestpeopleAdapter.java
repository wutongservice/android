
package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.view.PublicCircleRequestPeopleItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class publicCircleRequestpeopleAdapter extends BaseAdapter{
    private static final String TAG = "publicCircleRequestpeopleAdapter";
    private ArrayList<PublicCircleRequestUser> itemList = new ArrayList<PublicCircleRequestUser>();
    private Context      mContext;
    private QiupuORM orm;
    private int mStatus;
    private UserCircle mCircle;
    
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap = new HashMap<String, CheckBoxClickActionListener>();
    public void registerCheckClickActionListener(String key, CheckBoxClickActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }
    
    private HashMap<String, publicCirclePeopleActionListener> mActionListenerMap = new HashMap<String, publicCirclePeopleActionListener>();
    public void registerActionListener(String key, publicCirclePeopleActionListener rl) {
        mActionListenerMap.put(key, rl);
    }

    public void unRegisterActionListener(String key) {
        mActionListenerMap.remove(key);
    }
    
    private MoreItemCheckListener mCheckerListener;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public publicCircleRequestpeopleAdapter(Context context, int status, UserCircle circle, MoreItemCheckListener listener) {
        mCheckerListener = listener;
        mContext = context;
        orm = QiupuORM.getInstance(context);
        mStatus = status;
        mCircle = circle;
    }

    public int getCount() {
        if (null == itemList) {
            return 0;
        } else {
            int count = itemList.size();
            if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
                ++count;
            }
            return count;
        }
    }
    
    public PublicCircleRequestUser getItem(int position) {
        if(position < itemList.size()) {
            return itemList.get(position);
        }else {
            return null;
        }
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
            PublicCircleRequestPeopleItemView FView;
            if (convertView == null || false == (convertView instanceof PublicCircleRequestPeopleItemView) )
            {
                FView = new PublicCircleRequestPeopleItemView(mContext, user, mStatus, mCircle);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                FView.attachCheckListener(mCheckClickListenerMap);
                FView.attachActionListener(mActionListenerMap);
                convertView = FView;
                
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.attachCheckListener(mCheckClickListenerMap);
                holder.view.attachActionListener(mActionListenerMap);
                holder.view.setUserItem(user);
                holder.view.setStatus(mStatus);
                }
            
            return convertView;
        }else {
            return generateMoreItem();
        }
    }

    private View generateMoreItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItemView();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
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
        public PublicCircleRequestPeopleItemView view;
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
}