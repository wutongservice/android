package com.borqs.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.borqs.qiupu.R;


public class MediaPlayItemView extends SNSItemView {

    private static final String TAG = "Qiupu.MediaPlayItemView";

    private TextView mTitleView;
    private String title;

    public MediaPlayItemView(Context context, String data) {
        super(context);
        mContext = context;
        title = data;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setData(String data) {
        title = data;
        setUI();
    }

    public String getItemData() {
        return title;
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_setting_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)mContext.getResources().getDimension(R.dimen.left_menu_item_height)));
        addView(view);

        mTitleView = (TextView) view.findViewById(R.id.item_title);
        view.findViewById(R.id.item_button).setVisibility(View.GONE);
        setUI();
    }

    private void setUI() {
        mTitleView.setText(title);
    }

    @Override
    public String getText() {
        return "";
    }

}
