package com.borqs.common.view;

import twitter4j.ChatInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.fragment.IMComposeFragment;


public class IMComposeItemView extends SNSItemView {

    private static final String TAG = "Qiupu.IMComposeItemView";

    private ChatInfo mChatInfo;
    private TextView content_tv;
    private ImageView avatar_iv;
    private TextView time_tv;
    private ProgressBar uploading_pb;

    public IMComposeItemView(Context context, ChatInfo data) {
        super(context);
        mContext = context;
        mChatInfo = data;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setData(ChatInfo data) {
        mChatInfo = data;
        setUI();
    }

    public ChatInfo getItemData() {
        return mChatInfo;
    }

    private void init() {
        if (mChatInfo == null) {
            return;
        }

        View view;
        if (mChatInfo.type == IMComposeFragment.FROM_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.im_from_list_item, null);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.im_to_list_item, null);
        }

        addView(view);

        time_tv = (TextView) view.findViewById(R.id.chatting_time_tv);
        avatar_iv = (ImageView) view.findViewById(R.id.chatting_avatar_iv);
        avatar_iv.setVisibility(View.VISIBLE);
        content_tv = (TextView) view.findViewById(R.id.chatting_content_itv);
        uploading_pb = (ProgressBar) view.findViewById(R.id.uploading_pb);

        setUI();
    }

    private void setUI() {
        if (uploading_pb != null && mChatInfo.type == IMComposeFragment.TO_TYPE) {
            uploading_pb.setVisibility(View.GONE);
        }
        content_tv.setText(mChatInfo.msg);
        initImageUI(mChatInfo.profile_url);
    }

    private void initImageUI(String image_url) {
        ImageRun imagerun = new ImageRun(null, image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(avatar_iv);
        imagerun.post(null);
    }

    @Override
    public String getText() {
        return "";
    }

}
