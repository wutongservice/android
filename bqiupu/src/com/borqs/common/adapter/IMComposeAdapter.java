/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.ChatInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.IMComposeItemView;
import com.borqs.common.view.LeftMenuItemView;

public class IMComposeAdapter extends BaseAdapter {

    private static final String TAG = "Qiupu.IMComposeAdapter";

    private Context mContext;
    private ArrayList<ChatInfo> mChatInfoList = new ArrayList<ChatInfo>();

    public IMComposeAdapter(Context context, ArrayList<ChatInfo> chatList) {
        mContext = context;
        if (chatList != null && chatList.size() != 0) {
            mChatInfoList.addAll(chatList);
        }
    }

    private static class ViewHolder {
        public IMComposeItemView view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ChatInfo data = getItem(position);

        if (null != convertView && (convertView instanceof LeftMenuItemView)) {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setData(data);
            return holder.view;
        } else {
            IMComposeItemView view = new IMComposeItemView(mContext, data);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            return holder.view;
        }
    }

    @Override
    public int getCount() {
        return mChatInfoList.size();
    }

    @Override
    public ChatInfo getItem(int position) {
        return mChatInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void alterDataList(ArrayList<ChatInfo> chatInfoList) {
        if (chatInfoList != null && chatInfoList.size() != 0) {
            mChatInfoList.add(chatInfoList.get(0));
        }
        notifyDataSetChanged();
    }

    public void alterDataListMore(ArrayList<ChatInfo> chatInfoList) {
        if (chatInfoList != null && chatInfoList.size() != 0) {
            mChatInfoList.addAll(0, chatInfoList);
        }
        notifyDataSetChanged();
    }
}
