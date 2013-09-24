package com.borqs.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.borqs.common.adapter.CommentSettingAdapter.SettingData;
import com.borqs.qiupu.R;


public class CommentSettingItemView extends SNSItemView {

    private static final String TAG = "Qiupu.CommentSettingItemView";

    private CompoundButton button;
    private TextView mTitleView;
    private SettingData mSetData;
    private boolean checkedStatus = false;

    public CommentSettingItemView(Context context, SettingData data) {
        super(context);
        mContext = context;
        mSetData = data;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setData(SettingData data) {
        mSetData = data;
        setUI();
    }

    public SettingData getItemData() {
        return mSetData;
    }

    public boolean getCheckedStatus() {
        return checkedStatus;
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_setting_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)mContext.getResources().getDimension(R.dimen.left_menu_item_height)));
        addView(view);

        mTitleView = (TextView) view.findViewById(R.id.item_title);
        button = (CompoundButton) view.findViewById(R.id.item_button);
        button.setOnCheckedChangeListener(listener);
        setUI();
    }

    private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            checkedStatus = isChecked;
        }
    };

    private void setUI() {
        mTitleView.setText(mSetData.title);
        button.setChecked(mSetData.switcher);
    }

    public void setCheckedStatus() {
        boolean result = button.isChecked();
        button.setChecked(!result);
    }

    @Override
    public String getText() {
        return "";
    }

}
