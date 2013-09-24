package com.borqs.common.view;

import twitter4j.QiupuSimpleUser;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class RecommendItemView extends SNSItemView {
//    private final String    TAG = "RecommendItemView";

    private ImageView       usericon;
    private TextView        username;
    private TextView        sub_detail;

    private QiupuSimpleUser mUser;

    public RecommendItemView(Context context, QiupuSimpleUser di) {
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

    public QiupuSimpleUser getUser() {
        return mUser;
    }

    private void init() {
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        View v = factory.inflate(R.layout.recommend_list_item, null);
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(v);

        usericon = (ImageView) v.findViewById(R.id.user_icon);
        username = (TextView) v.findViewById(R.id.user_name);
        sub_detail = (TextView) v.findViewById(R.id.sub_title);

        setUI();
    }

    private void setUI() {
        username.setText(mUser.nick_name);
        sub_detail.setText(mUser.contact_method);

        usericon.setImageResource(R.drawable.default_user_icon);
        ImageRun imagerun = new ImageRun(null, mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = false;
        imagerun.addHostAndPath = true;
        imagerun.setImageView(usericon);
        imagerun.post(null);
    }

    public void setUserItem(QiupuSimpleUser di) {
        mUser = di;
        setUI();
    }

    @Override
    public String getText() {
        return mUser != null ? mUser.name : "";
    }

}
