package com.borqs.common.adapter;

import java.util.ArrayList;

import com.borqs.common.view.BpcFriendsItemView;
import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.qiupu.R;

public class BPCFriendsAdapter extends BaseAdapter {
    private static final String TAG = "Qiupu.FriendsAdapter";
    private ArrayList<QiupuUser> itemList = new ArrayList<QiupuUser>();
    private Context mContext;

    private MoreItemCheckListener mCheckerListener;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }

    public void setItems(ArrayList<QiupuUser> items) {
        this.itemList = items;
    }

    public BPCFriendsAdapter(Context context, MoreItemCheckListener listener) {
        mContext = context;
        mCheckerListener = listener;
    }

    public int getCount() {
        if (itemList == null) {
            return 0;
        } else {
            if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
                return itemList.size();
            }
            return itemList.size() + 1;
        }
    }

    public QiupuUser getItem(int position) {

        if (itemList != null && position < itemList.size()) {
            return itemList.get(position);
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        QiupuUser user = getItem(position);
        return user != null ? user.uid : -1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        QiupuUser user = getItem(position);
        if (user != null) {
            if (convertView == null || false == (convertView instanceof BpcFriendsItemView)) {
                BpcFriendsItemView FView = new BpcFriendsItemView(mContext, user, false, false);
                holder = new ViewHolder();

                FView.setTag(holder);
                holder.view = FView;

                convertView = FView;

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setUser(user);
            }

            return convertView;
        } else {
            return generateRefreshItem();
        }

    }

    private Button generateMoreItem() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }

    private View generateRefreshItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItem();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }

    static class ViewHolder {
        public BpcFriendsItemView view;
    }

    public void alterDataList(ArrayList<QiupuUser> userList) {
        itemList.clear();
        itemList.addAll(userList);

        notifyDataSetChanged();
    }
}
