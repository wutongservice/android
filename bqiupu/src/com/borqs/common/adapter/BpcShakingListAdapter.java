
package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.QiupuUser;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.view.BpcShakingItemView;

public class BpcShakingListAdapter extends BaseAdapter {
    private static final String TAG = "Qiupu.BpcShakingListAdapter";
    private ArrayList<QiupuUser> itemList = new ArrayList<QiupuUser>();
    private Context      mContext;
    private boolean from_exchange = false;

    private HashMap<String, UsersActionListner> mUserActionMap = new HashMap<String, UsersActionListner>();

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public void registerUsersActionListner(String key, UsersActionListner rl) {
    	mUserActionMap.put(key, rl);
    }

    public void unregisterUsersActionListner(String key) {
    	mUserActionMap.remove(key);
    }

    public BpcShakingListAdapter(Context context) {
        mContext = context;
    }

    public BpcShakingListAdapter(Context context, boolean fromExchange) {
        mContext = context;
        from_exchange = fromExchange;
    }

    public int getCount() {
        if (null == itemList) {
            return 0;
        } else {
            return itemList.size();
        }
    }

    public QiupuUser getItem(int position) {
        return itemList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        QiupuUser user = getItem(position);
        BpcShakingItemView itemView;
        if (convertView == null || false == (convertView instanceof BpcShakingItemView)) {
            itemView = BpcShakingItemView.newInstance(mContext, user, from_exchange);
            holder = new ViewHolder();

            itemView.setTag(holder);
            holder.view = itemView;
            itemView.attachActionListener(mUserActionMap);

            convertView = itemView;
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.view.setUser(user);
            holder.view.attachActionListener(mUserActionMap);
            holder.view.setFromStatus(from_exchange);
        }

        return convertView;
    }

    static class ViewHolder {
        public BpcShakingItemView view;
    }

    public void alterDataList(ArrayList<QiupuUser> userList) {
        itemList.clear();
        itemList.addAll(userList);

        notifyDataSetChanged();
    }

}