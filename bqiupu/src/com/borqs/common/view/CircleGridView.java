package com.borqs.common.view;

import twitter4j.CircleGridData;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.common.adapter.GridPeopleSimpleAdapter;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;

public class CircleGridView extends LinearLayout{
	private static final String TAG = "CircleGridView";
	private Context mContext;
	
	private GridView mGrid_in_member;
	
	private View inMemberView;
	private TextView tv_in_member;
	
	private CircleGridData mCircle;
	
	public CircleGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}



	public CircleGridView(Context context,CircleGridData circle) {
		super(context);
		mContext = context;
		mCircle = circle;
		init();
		initUI();
	}
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	private void init() {
		removeAllViews();

		View contentView = LayoutInflater.from(mContext).inflate(
				R.layout.circle_grid_layout, null);
		contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		addView(contentView);

		mGrid_in_member = (GridView) contentView.findViewById(R.id.grid_in_member);
		tv_in_member = (TextView)contentView.findViewById(R.id.tv_in_member);
		inMemberView = contentView.findViewById(R.id.in_member_rl);
	}
	
	private void initUI() {
		if(mCircle == null) {
			if(QiupuConfig.LOGD) Log.e(TAG,"data is null");
			setVisibility(View.GONE);
			return;
		}
		if(mCircle.memberCount > 0 && mCircle.memberList != null && mCircle.memberList.size() >0) {
			setVisibility(View.VISIBLE);
			tv_in_member.setText(mCircle.name + "(" + mCircle.memberCount + ")");
	        if(mCircle.memberList != null && mCircle.memberList.size() > 0) {
	            mGrid_in_member.setAdapter(new GridPeopleSimpleAdapter(mContext, mCircle.memberList));
	        }
	        
	        inMemberView.setOnClickListener(mCircle.clickListener);
		}else {
			setVisibility(View.GONE);
		}
		
	}
	
	public void updateDataInfo(CircleGridData circle) {
		mCircle = circle;
		initUI();
	}
	
	public CircleGridData getDataInfo() {
		return mCircle;
	}
	
}
