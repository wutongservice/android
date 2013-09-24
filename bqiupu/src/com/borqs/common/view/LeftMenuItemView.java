package com.borqs.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.adapter.IconListAdapter.IconListItem;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;


public class LeftMenuItemView extends SNSItemView {

//    private static final String TAG = "Qiupu.LeftMenuItemView";

    private ImageView mItemIcon, mPluginIcon;
    private TextView mTitleView;
    private IconListItem mItemData;
    protected MissingNumberView mItemCount;


    public LeftMenuItemView(Context context, IconListItem data) {
        super(context);
        mContext = context;
        mItemData = data;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setIconListItemData(IconListItem shareData) {
        mItemData = shareData;
        setUI();
    }

    public IconListItem getItemData() {
        return mItemData;
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.icon_list_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, (int)mContext.getResources().getDimension(R.dimen.left_menu_item_height)));
        addView(view);

        mItemIcon = (ImageView) view.findViewById(R.id.item_icon);
        mPluginIcon = (ImageView) view.findViewById(R.id.plugin_icon);
        mTitleView = (TextView) view.findViewById(R.id.item_text);
        mItemCount = (MissingNumberView) view.findViewById(R.id.item_count);

        setUI();
    }

    private void setUI() {
        if (mItemData.getResource() != 0) {
            mItemIcon.setImageResource(mItemData.getResource());
            mItemIcon.setVisibility(View.VISIBLE);
            mPluginIcon.setVisibility(View.GONE);
        } else {
            mPluginIcon.setImageDrawable(mItemData.getDrawable());
            mPluginIcon.setVisibility(View.VISIBLE);
            mItemIcon.setVisibility(View.GONE);
        }

        mTitleView.setText(mItemData.getTitle());

        if (mItemData.getCount() != 0) {
            mItemCount.setMissCallCount(mItemData.getCount());
            mItemCount.setVisibility(View.VISIBLE);
        } else {
            mItemCount.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public String getText() {
        return "";
    }

}
