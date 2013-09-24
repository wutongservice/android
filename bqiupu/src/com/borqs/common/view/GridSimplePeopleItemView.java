package com.borqs.common.view;

import twitter4j.UserImage;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class GridSimplePeopleItemView extends SNSItemView {
    private final String TAG = "GridSimplePeopleItemView";

    private TextView mLabel;
    private ImageView mIcon;
    private UserImage mUser;

    public GridSimplePeopleItemView(Context context, UserImage di) {
        super(context);
        mContext = context;
        mUser = di;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void setUserInfo(UserImage info) {
        mUser = info;
        setUI();
    }

    public UserImage getUserInfo() {
        return mUser;
    }

    private void init() {
        Log.d(TAG, "call init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        View v = factory.inflate(R.layout.grid_simpleuser_item_view, null);
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        addView(v);

        int width = getResources().getDisplayMetrics().widthPixels;
//        int tmpWidth = (width*2/3)/5;
//        Log.d(TAG, "tmpWidth: " + tmpWidth);
        mIcon = (ImageView) v.findViewById(R.id.id_icon);
        mLabel = (TextView) v.findViewById(R.id.id_label);
//        mLabel.setMovementMethod(MyLinkMovementMethod.getInstance());
//        mIcon.setLayoutParams(new LayoutParams(tmpWidth, tmpWidth));
        setUI();
    }

    private void setUI() {
        mIcon.setImageResource(R.drawable.default_user_icon);
        ImageRun imagerun = new ImageRun(null,mUser.image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(mIcon);        
        imagerun.post(null);
        
        mLabel.setText(mUser.userName);
    }

    @Override
    public String getText() {
        return mLabel.getText().toString();
    }
}
