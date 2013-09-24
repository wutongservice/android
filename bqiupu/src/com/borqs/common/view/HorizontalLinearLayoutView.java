package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.QiupuAccountInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.borqs.common.ProfileHeadSourceItem;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;

public class HorizontalLinearLayoutView extends LinearLayout
{
	private final static String TAG = "HorizontalLinearLayoutView";
	protected Context mContext;
	public HorizontalLinearLayoutView(Context context) {
		super(context);	
		mContext = context;
	}

	public HorizontalLinearLayoutView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		mContext = ctx;
	}

	public void setDataArray(ArrayList<ShareSourceItem> items, final QiupuAccountInfo user) {
		removeAllViews();
		for(int i=0; i<items.size(); i++) {
			final ShareSourceItem tmpItem = items.get(i);
			ProfileSourceItemView tmpView = new ProfileSourceItemView(mContext, tmpItem, QiupuConfig.isPublicCircleProfile(user.uid));
			tmpView.setBackgroundResource(R.drawable.list_selector_background);
			tmpView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(tmpItem.mScheme != null){
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tmpItem.mScheme));
						if (/*tmpItem.mCount > 0 &&*/
								BpcApiUtils.isActivityReadyForIntent(mContext.getApplicationContext(), intent)) {
							Bundle bundle = BpcApiUtils.getUserBundle(user.uid, user.nick_name, "");
							intent.putExtras(bundle);
							//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(intent);
						} else {
							Log.e(TAG, "OnItemClickListener.onItemClick, no activity for intent:" + intent +
									", or no share item count:" + tmpItem.mCount);
						}
					}
					else {
//						if(tmpItem.mCount > 0){
							Intent intent = new Intent(mContext, ShareResourcesActivity.class);
							intent.putExtra("userid", user.uid);
							intent.putExtra("sourcefilter", tmpItem.mType);
							intent.putExtra("title", ShareSourceItem.getSourceItemLabel(mContext, tmpItem.mLabel, tmpItem.mType));
							mContext.startActivity(intent);
//						}else {
//							Log.d(TAG, "the source count is 0, no nend to start activity");
//						}
					}
				}
			});
			this.addView(tmpView);
		}
	}
	
	public void setProfileDataArray(ArrayList<ProfileHeadSourceItem> items) {
		removeAllViews();
		for(int i=0; i<items.size(); i++) {
			final ProfileHeadSourceItem tmpItem = items.get(i);
			ProfileHeadSourceView tmpView = new ProfileHeadSourceView(mContext, tmpItem);
//			tmpView.setBackgroundResource(R.drawable.profile_head_source_background);
//			tmpView.setOnClickListener(tmpItem.mClickListener);
			this.addView(tmpView);
		}
	}
}
