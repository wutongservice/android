package com.borqs.common.view;

import twitter4j.UserCircle;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class RecipientsCirclesItemView extends SNSItemView {

	private UserCircle mCircle;

	private TextView mName;
	private ImageView mIcon;

	public RecipientsCirclesItemView(Context context) {
		super(context);
	}

	@Override
	public String getText() {
		return null;
	}

	public RecipientsCirclesItemView(Context context, UserCircle circle) {
		super(context);
		mCircle = circle;
		init();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	private void init() {
		removeAllViews();

		// child 1
		View convertView = LayoutInflater.from(mContext).inflate(
				R.layout.recipient_filter_item, null);
		addView(convertView);

		mName = (TextView) convertView.findViewById(R.id.name);
		mIcon = (ImageView) convertView.findViewById(R.id.id_friend_icon);

		setUI();
	}

	public void setDataInfo(UserCircle circle) {
		mCircle = circle;
		setUI();
	}

	private void setUI() {

		mName.setText(mCircle.name);
		if(mCircle.circleid == QiupuConfig.CIRCLE_ID_PUBLIC) {
			mIcon.setImageResource(R.drawable.default_open_icon);
		}else if(mCircle.circleid == QiupuConfig.CIRCLE_ID_PRIVACY) {
			mIcon.setImageResource(R.drawable.default_secret_icon);
		}else if (!TextUtils.isEmpty(mCircle.profile_image_url)) {
			setViewIcon(mCircle.profile_image_url, mIcon);
		}else {
			mIcon.setImageResource(R.drawable.default_public_circle);
		}
	}

	private void setViewIcon(final String url, final ImageView view) {
		final Resources resources = mContext.getResources();
		view.setImageResource(R.drawable.default_public_circle);
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.width = resources.getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		imagerun.setRoundAngle = true;
		imagerun.setImageView(view);
		imagerun.post(null);
	}

	public UserCircle getDataInfo() {
		return mCircle;
	}

	public long getDataId() {
		return mCircle.circleid;
	}
}
