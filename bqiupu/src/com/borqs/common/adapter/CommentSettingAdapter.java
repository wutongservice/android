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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.CommentSettingItemView;
import com.borqs.qiupu.R;

public class CommentSettingAdapter extends BaseAdapter {

    private static final String TAG = "Qiupu.CommentSettingAdapter";

    private Context mContext;
    private ArrayList<SettingData> mDataList = new ArrayList<SettingData>();

    public CommentSettingAdapter(Context context, boolean canComment, boolean canLike, boolean canReshare) {
        mContext = context;
        getData(context, canComment, canLike, canReshare);
    }

    private void getData(Context context, boolean canComment, boolean canLike, boolean canReshare) {
        String[] title = context.getResources().getStringArray(R.array.comment_setting);
        boolean[] prop = {canComment, canLike, canReshare};
        for (int i =0, len = title.length; i < len; i++) {
            SettingData data = new SettingData();
            data.title = title[i];
            data.switcher = prop[i];
            mDataList.add(data);
        }
    }

    private static class ViewHolder {
        public CommentSettingItemView view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final SettingData data = getItem(position);

        if (null != convertView && false == (convertView instanceof CommentSettingItemView)) {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setData(data);
            return holder.view;
        } else {
            CommentSettingItemView view = new CommentSettingItemView(mContext, data);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            return holder.view;
        }
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public SettingData getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class SettingData {
        public String title;
        public boolean switcher;
    }
}
