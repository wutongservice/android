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

import com.borqs.common.view.MediaPlayItemView;
import com.borqs.qiupu.R;

public class MediaPlaySelectionAdapter extends BaseAdapter {

    private static final String TAG = "Qiupu.MediaPlaySelectionAdapter";

    private Context mContext;
    private ArrayList<String> mDataList = new ArrayList<String>();

    public MediaPlaySelectionAdapter(Context context, boolean isMediaFile) {
        mContext = context;
        getData(context, isMediaFile);
    }

    private void getData(Context context, boolean isMediaFile) {
        if (mDataList.size() != 0) {
            mDataList.clear();
        }

        if (isMediaFile) {
            mDataList.add(context.getResources().getString(R.string.play_pos_button));
            mDataList.add(context.getResources().getString(R.string.play_neu_button));
        } else {
            mDataList.add(context.getResources().getString(R.string.play_neu_button));
        }
    }

    private static class ViewHolder {
        public MediaPlayItemView view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final String data = getItem(position);

        if (null != convertView && false == (convertView instanceof MediaPlayItemView)) {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setData(data);
            return holder.view;
        } else {
            MediaPlayItemView view = new MediaPlayItemView(mContext, data);
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
    public String getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
