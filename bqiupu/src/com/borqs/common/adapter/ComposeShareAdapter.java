package com.borqs.common.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import twitter4j.ComposeShareData;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.listener.ComposeActionListener;
import com.borqs.common.view.ComposeShareItemView;

public class ComposeShareAdapter extends BaseAdapter {
//    private static final String TAG = "Qiupu.ComposeShareAdapter";

    private Context mContext;
    private ArrayList<ComposeShareData> mShareDataList = new ArrayList<ComposeShareData>();
    private WeakReference<ComposeActionListener>  mComposeActionListener;

    public ComposeShareAdapter(Context context) {
        mContext = context;
    }

    public ComposeShareAdapter(Context context, ArrayList<ComposeShareData> shareDataList) {
        mContext = context;
        mShareDataList.clear();
        if (shareDataList != null) {
            mShareDataList.addAll(shareDataList);
        }
    }

    @Override
    public int getCount() {
        return (mShareDataList != null && mShareDataList.size() > 0) ? (mShareDataList.size()) : 0;
    }

    public ComposeShareData getItem(int pos) {
        if (pos >= mShareDataList.size()) {
            return null;
        }
        return mShareDataList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ComposeShareData shareData = getItem(position);

        if (null != convertView && false == (convertView instanceof ComposeShareItemView)) {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setQiupuShareData(shareData);
            holder.view.attachActionListener(mComposeActionListener);
            return holder.view;
        } else {
            ComposeShareItemView view = new ComposeShareItemView(mContext, shareData);
            view.attachActionListener(mComposeActionListener);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            return view;
        }
    }

    public void alterDataList(ArrayList<ComposeShareData> shareDataList) {
        mShareDataList.clear();
        mShareDataList.addAll(shareDataList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        public ComposeShareItemView view;
    }

    public void setComposeActionListener(ComposeActionListener composedeleteActionListener) {
        mComposeActionListener = new WeakReference<ComposeActionListener>(composedeleteActionListener);
    }
}
