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
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.common.adapter.IconListAdapter.IconListItem;
import com.borqs.common.model.MainItemInfo;
import com.borqs.common.view.LeftMenuItemView;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.LeftMenuMapping;

/**
 * An adapter to store icons and strings for add left menu list.
 */
public class LeftMenuAdapter extends BaseAdapter {
    private static final String TAG = "LeftMenuAdapter";

    private Context mContext;
    private int mFavoriteListSize = 0;
    private int mPluginListSize = 0;
    private final int mOptionSize = 1;

    private int mPosition;
    private HashMap<Long, Integer> positionMap = new HashMap<Long, Integer>();
    private static ArrayList<IconListItem> mMenuItemList;

    public LeftMenuAdapter(Context context,int position, ArrayList<MainItemInfo> pluginInfo) {
//        super(context, getData(context, pluginInfo));
        mContext = context;
        mPosition = position;
        getData(context, pluginInfo);
    }

    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getIndex();
    }

    private void generatePositionMap(int itemSize, int pluginSize) {
        positionMap.clear();

        positionMap.put(new Long(positionMap.size()), -1000);
        for (int i = 0; i < itemSize; i++) {
            positionMap.put(new Long(positionMap.size()), i);
        }

        if (pluginSize > 0 ) {
            positionMap.put(new Long(positionMap.size()), -2000);
        }

        for (int i = 0; i < pluginSize; i++) {
            positionMap.put(new Long(positionMap.size()), i + itemSize);
        }

        positionMap.put(new Long(positionMap.size()), -3000);
        positionMap.put(new Long(positionMap.size()), itemSize + pluginSize);
    }

    private /*static*/ String[] FAVORITE_TITLE = null;
    private static int[] FAVORITE_ICON = {R.drawable.home_screen_menu_loop_icon_default, /*R.drawable.home_screen_menu_profile_icon_default,*/
            R.drawable.home_screen_photo_icon_default,/*R.drawable.home_screen_menu_request_icon_default,*/
            R.drawable.friend_group_icon,
            R.drawable.home_screen_menu_people_icon_default,
            R.drawable.home_screen_even_icon, R.drawable.home_screen_voting_icon_default,
            R.drawable.home_screen_menu_search_icon_default, /*R.drawable.home_screen_menu_exchange_icon_default,*/
                /*R.drawable.home_screen_menu_public_icon_default,*/ /*R.drawable.home_screen_menu_app_icon_default,*/
                /*R.drawable.home_screen_menu_option_icon_default*/};
    protected List<IconListItem> getData(Context context, ArrayList<MainItemInfo> pluginInfo) {
        //TODO: why use static variable? it won't change when user selects different language.
        if (FAVORITE_TITLE == null) {
            FAVORITE_TITLE = context.getResources().getStringArray(R.array.left_menu_item_title);
        }

        if (FAVORITE_TITLE.length != FAVORITE_ICON.length) {
            Log.e(TAG, "getData, skip while the count of title and icon were mismatch.");
            return null;
        }

        final boolean isPersonalView = QiupuApplication.mTopOrganizationId == QiupuApplication.VIEW_MODE_PERSONAL;
        // excluding 1 item in organization view
        mFavoriteListSize = isPersonalView ? FAVORITE_TITLE.length : FAVORITE_TITLE.length - 1;
        mPluginListSize = null == pluginInfo ? 0 : pluginInfo.size();

        generatePositionMap(mFavoriteListSize, mPluginListSize);

        final int menuItemSize = mFavoriteListSize + mPluginListSize + 1;

        mMenuItemList = new ArrayList<IconListItem>(menuItemSize);

        if (isPersonalView) {
            int position;
            for (int i = 0; i < FAVORITE_TITLE.length; i++) {
                position = i;
                addItem(mMenuItemList, FAVORITE_TITLE[i], FAVORITE_ICON[i], position, 0, null, null);
            }
        } else {
            int position;
            for (int i= 0; i < LeftMenuMapping.TYPE_BpcAddFriendsActivity - 1; i++) {
                position = i;
                addItem(mMenuItemList, FAVORITE_TITLE[i], FAVORITE_ICON[i], position, 0, null, null);
            }
            for (int i = LeftMenuMapping.TYPE_BpcAddFriendsActivity; i < FAVORITE_TITLE.length; i++) {
                position = i - 1; // excluding 1 favorite item (add friends)
                addItem(mMenuItemList, FAVORITE_TITLE[i], FAVORITE_ICON[i], position, 0, null, null);
            }
        }

        for (int i = 0; i < mPluginListSize; i++) {
            MainItemInfo item = pluginInfo.get(i);
            addItem(mMenuItemList, item.mLabel, 0, mFavoriteListSize + i, 0, item.mIcon, item.mComponent);
        }

        addItem(mMenuItemList, context.getString(R.string.options), R.drawable.home_screen_menu_option_icon_default,
                menuItemSize, 0, null, null);
        
        return mMenuItemList;
    }

    protected static void addItem(List<IconListItem> data, String title, int resource, int index, int count, Drawable drawable, ComponentName component) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, index, count, drawable, component);
        data.add(temp);
    }

    public static class AttachmentListItem extends IconListAdapter.IconListItem {
        private int mIndex;

        public AttachmentListItem(String title, int resource, int index, int count, Drawable drawable, ComponentName component) {
            super(title, resource, index, count, drawable, component);
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }
    }

    private static class ViewHolder {
        public LeftMenuItemView view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final IconListItem data = getItem(position);
        Integer newposition = positionMap.get(new Long(position));

        if (data != null) {
            if (null != convertView && (convertView instanceof LeftMenuItemView)) {
                holder = (ViewHolder)convertView.getTag();
                setSelectedStatus(holder.view, newposition);
                holder.view.setIconListItemData(data);
                return holder.view;
            } else {
                LeftMenuItemView view = new LeftMenuItemView(mContext, data);
                setSelectedStatus(view, newposition);
                holder = new ViewHolder();
                holder.view = view;
                view.setTag(holder);
                return holder.view;
            }
        } else {
            String categoryItemTitle = "";
            if (newposition != null) {
                if (newposition < 0) {
                    final int pos = Math.abs(newposition);
                    switch (pos) {
                        case 1000:
                            categoryItemTitle = mContext.getString(R.string.common_item);
                            break;
                        case 2000:
                            categoryItemTitle = mContext.getString(R.string.plugin_item);
                            break;
                        case 3000:
                            categoryItemTitle = mContext.getString(R.string.operation_item);
                            break;
                        default:
                            break;
                    }
                }
            }
            TextView textView = generateSpanItemView();
            textView.setText(categoryItemTitle);
            return textView;
        }
    }

    private TextView generateSpanItemView() {
        TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.left_menu_category_item, null));
        but.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 
                (int)mContext.getResources().getDimension(R.dimen.left_menu_category_item_height)));
        but.setOnClickListener(null);
        return but;
    }

    private void setSelectedStatus(View view, int position) {
        if(mPosition == position + 1) {
            view.setBackgroundResource(R.color.left_menu_press_background);
        } else {
            view.setBackgroundResource(0);
        }
    }

    public ArrayList<IconListItem> getListData() {
        return mMenuItemList;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mFavoriteListSize > 0) {
            count += mFavoriteListSize + 1;
        }

        if (mPluginListSize > 0) {
            count += mPluginListSize + 1;
        }

        if (mOptionSize > 0) {
            count += mOptionSize + 1;
        }

        return count;
    }

    @Override
    public IconListItem getItem(int position) {
        Integer newposition = positionMap.get(new Long(position));

        if(newposition >= 0 && newposition < mMenuItemList.size()){
            return mMenuItemList.get(newposition);
        } else{
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
