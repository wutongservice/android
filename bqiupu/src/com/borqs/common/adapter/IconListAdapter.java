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

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import com.borqs.common.view.LeftMenuItemView;
import com.borqs.common.view.MissingNumberView;
import com.borqs.qiupu.R;

/**
 * An adapter to store icons.
 */
public class IconListAdapter extends ArrayAdapter<IconListAdapter.IconListItem> {

    protected LayoutInflater mInflater;
    private static final int LAYOUT_ID = R.layout.icon_list_item;
    protected MissingNumberView mItemCount;
    private Context mContext;
    private List<IconListItem> itemList;

    public IconListAdapter(Context context, List<IconListItem> items) {
        super(context, LAYOUT_ID, items);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return (itemList != null && itemList.size() > 0) ? (itemList.size()) : 0;
    }

    public IconListItem getItem(int pos) {
        if (pos >= itemList.size()) {
            return null;
        }
        return itemList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final IconListItem data = getItem(position);

        if (null != convertView && (convertView instanceof LeftMenuItemView)) {
            holder = (ViewHolder)convertView.getTag();
            return holder.view;
        } else {
            LeftMenuItemView view = new LeftMenuItemView(mContext, data);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            return holder.view;
        }
    }

    private static class ViewHolder {
        public LeftMenuItemView view;
    }

    public static class IconListItem {
        private final String title;
        private final int resource;
        private final int index;
        private final Drawable drawable;
        private int count;
        public ComponentName component;

        public IconListItem(String title, int resource, int index, int count, 
                Drawable drawable, ComponentName component) {
            this.resource = resource;
            this.title = title;
            this.index = index;
            this.count = count;
            this.drawable = drawable;
            this.component = component;
        }

        public ComponentName getComponent() {
            return component;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public String getTitle() {
            return title;
        }

        public int getResource() {
            return resource;
        }

        public int getIndex() {
            return index;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }
}
