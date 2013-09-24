package com.borqs.common.view;

import twitter4j.UserCircle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.borqs.common.api.BpcApiUtils.User;
import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.StringUtil;

public class GroupPrivacySetview extends SNSItemView {

    private static final String TAG = "GroupPrivacySetview";
    private Context mContext;
	private UserCircle mCircle;
	
	private TextView mPrivacy_selected_tv;
	private TextView mApprove_selected_tv;
	private TextView mJoin_selected_tv;
	private CheckBox mInvitePer;
	private CheckBox sendEmail;
	private CheckBox sendSms;
	private String[] mPrivacyItems ;
	private String[] mApproveItems;
	private String[] mJoinItems;
	private int mPrivacy = -1;
	private int mApprove = -1;
	private int mJoinPermission = -1;
	private int mInvitePermission = -1;
	private boolean mSendEmailStatus = true;
	private boolean mSendSmsStatus = false;
	
    public GroupPrivacySetview(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }
    
    public GroupPrivacySetview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public GroupPrivacySetview(Context context, UserCircle circle) {
        super(context);
        mContext = context;
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
        LayoutInflater factory = LayoutInflater.from(mContext);
        View contentView = factory.inflate(R.layout.privacy_approval_set_ui, null);
        addView(contentView);

        mPrivacy_selected_tv = (TextView) contentView.findViewById(R.id.privacy_selected_tv);
        mApprove_selected_tv = (TextView) contentView.findViewById(R.id.approve_selected_tv);
        mJoin_selected_tv = (TextView) contentView.findViewById(R.id.join_selected_tv);
        mInvitePer = (CheckBox) contentView.findViewById(R.id.invite_set_permission);
        View layout_privacy = contentView.findViewById(R.id.layout_privacy_set);
        layout_privacy.setOnClickListener(privacyClickListener);
        View layout_approve = contentView.findViewById(R.id.layout_approve);
        layout_approve.setOnClickListener(approveClickListener);
        View layout_join_permission = contentView.findViewById(R.id.layout_join_permission);
        layout_join_permission.setOnClickListener(joinClickListener);
        mInvitePer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mInvitePermission == UserCircle.INVITE_PERMISSION_NEED_CONFIRM) {
					mInvitePermission = UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM;
				}else if(mInvitePermission == UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM){
					mInvitePermission = UserCircle.INVITE_PERMISSION_NEED_CONFIRM;
				}
				mInvitePer.setChecked(mInvitePermission == UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM);
			}
		});

        sendEmail = (CheckBox) contentView.findViewById(R.id.send_email);
        sendEmail.setVisibility(View.VISIBLE);
        sendEmail.setChecked(mSendEmailStatus);
        sendEmail.setOnClickListener(sendEmailListener);
        sendSms = (CheckBox) contentView.findViewById(R.id.send_sms);
        sendSms.setVisibility(View.VISIBLE);
        sendSms.setChecked(mSendSmsStatus);
        sendSms.setOnClickListener(sendSmsListener);

        setUI();
    }

    private View.OnClickListener sendEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSendEmailStatus = !mSendEmailStatus;
            sendEmail.setChecked(mSendEmailStatus);
        }
    };

    private View.OnClickListener sendSmsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSendSmsStatus = !mSendSmsStatus;
            sendSms.setChecked(mSendSmsStatus);
        }
    };

    private void setUI() {
    	mPrivacyItems = getResources().getStringArray(R.array.privacy_set_title);
    	mApproveItems = getResources().getStringArray(R.array.member_approve);
    	mJoinItems = getResources().getStringArray(R.array.join_permission);

    	if(mCircle == null) {
    		mPrivacy = UserCircle.PRIVACY_OPEN;
    		mApprove = UserCircle.APPROVE_MAMANGER;
    		mJoinPermission = UserCircle.JOIN_PREMISSION_VERIFY;
    		mInvitePermission = UserCircle.INVITE_PERMISSION_NEED_CONFIRM;
    		
    	}else {
    		if(mCircle.mGroup != null) {
    			if(UserCircle.isPrivacyOpen(mCircle.mGroup)) {
    				mPrivacy_selected_tv.setText(R.string.privacy_set_open);
    				mPrivacy = UserCircle.PRIVACY_OPEN;
    			}else if(UserCircle.isPrivacyClosed(mCircle.mGroup)) { 
    				mPrivacy_selected_tv.setText(R.string.privacy_set_closed);
    				mPrivacy = UserCircle.PRIVACY_CLOSED;
    			}else if(UserCircle.isPrivacySecret(mCircle.mGroup)) {
    				mPrivacy_selected_tv.setText(R.string.privacy_set_secret);
    				mPrivacy = UserCircle.PRIVACY_SECRET;
    			}
    			
    			if(UserCircle.canApproveInvite(mCircle.mGroup)) {
    				mApprove_selected_tv.setText(R.string.approve_set_all);
    				mApprove = UserCircle.APPROVE_MEMBER;
    			}else {
    				mApprove_selected_tv.setText(R.string.approve_set_manager);
    				mApprove = UserCircle.APPROVE_MAMANGER;
    			}
    			if(mCircle.mGroup.can_join == UserCircle.JOIN_PREMISSION_VERIFY) {
    				mJoin_selected_tv.setText(R.string.join_permission_verified);
    			}else if(mCircle.mGroup.can_join == UserCircle.JOIN_PERMISSION_DERECT_ADD) {
    				mJoin_selected_tv.setText(R.string.join_permission_to_join);
    			}else if(mCircle.mGroup.can_join == UserCircle.JOIN_PERMISSION_FORBID) {
    				mJoin_selected_tv.setText(R.string.join_permission_forbid);
    			}
    			
    			if(QiupuConfig.isEventIds(mCircle.circleid)) {
    				mInvitePer.setVisibility(View.GONE);
    			}else {
    				mInvitePer.setVisibility(View.VISIBLE);
    				mInvitePermission = mCircle.mGroup.need_invite_confirm;
    				mInvitePer.setChecked(mInvitePermission == UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM);
    			}
    		}
    	}
    }
    
    public void setContent(UserCircle circle) {
    	if(circle != null) {
    		mCircle = circle;
    		setUI();
    	}
    }
    
    private AlertDialog mDialog ;
    View.OnClickListener privacyClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
	        String[] privacySummary = getResources().getStringArray(R.array.privacy_set_summary);
	        int checkedItem = 0;
	        String selectPrivacy = mPrivacy_selected_tv.getText().toString().trim();
	        
	        for(int i=0; i<mPrivacyItems.length; i++) {
	        	if(mPrivacyItems[i].equals(selectPrivacy)) {
	        		checkedItem = i;
	        		break;
	        	}
	        }
			
	        SignChoiceAdapter adapter = new SignChoiceAdapter(mContext);
	        adapter.alterStringArray(mPrivacyItems, privacySummary);
	        mDialog = DialogUtils.showSingleChoiceDialogWithAdapter(mContext, R.string.privacy_set_title, adapter, checkedItem, privacyItemClickListener, null, negativeListener);
		}
	};
	
	private void dimissDialog() {
		if(mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	DialogInterface.OnClickListener privacyItemClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mPrivacy_selected_tv.setText(mPrivacyItems[which]);
			if(which == 0) {
			    mPrivacy = UserCircle.PRIVACY_OPEN;
			}else if(which == 1) {
			    mPrivacy = UserCircle.PRIVACY_CLOSED;
			}else if(which == 2) {
			    mPrivacy = UserCircle.PRIVACY_SECRET;
			}else {
			    Log.d(TAG, "have no checked ");
			}
			dimissDialog();
		}
	};
	
	DialogInterface.OnClickListener approveItemClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mApprove_selected_tv.setText(mApproveItems[which]);
			if(which == 0) {
				mApprove = UserCircle.APPROVE_MEMBER;
			}else if(which == 1) {
				mApprove = UserCircle.APPROVE_MAMANGER;
			}
			dimissDialog();
		}
	};
	
	DialogInterface.OnClickListener joinItemClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mJoin_selected_tv.setText(mJoinItems[which]);
			if(which == 0) {
				mJoinPermission = UserCircle.JOIN_PREMISSION_VERIFY;
			}else if(which == 1) {
				mJoinPermission = UserCircle.JOIN_PERMISSION_DERECT_ADD;
			}else if(which == 2) {
				mJoinPermission = UserCircle.JOIN_PERMISSION_FORBID;
			}
			dimissDialog();
		}
	};
	
	DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	};
	
	View.OnClickListener approveClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int checkedItem = 0;
			String selectApprove = mApprove_selected_tv.getText().toString().trim();
			for(int i=0; i<mApproveItems.length; i++) {
				if(selectApprove.equals(mApproveItems[i])) {
					checkedItem = i;
					break;
				}
			}
			SignChoiceAdapter adapter = new SignChoiceAdapter(mContext);
	        adapter.alterStringArray(mApproveItems, null);
	        mDialog = DialogUtils.showSingleChoiceDialogWithAdapter(mContext, R.string.privacy_set_title, adapter, checkedItem, approveItemClickListener, null, negativeListener);
		}
	};
	
	View.OnClickListener joinClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int checkedItem = 0;
			String selectJoin = mJoin_selected_tv.getText().toString().trim();
			String[] joinSummary = getResources().getStringArray(R.array.join_permission_summary);
			for(int i=0; i<joinSummary.length; i++) {
				if(selectJoin.equals(mJoinItems[i])) {
					checkedItem = i;
					break;
				}
			}
			SignChoiceAdapter adapter = new SignChoiceAdapter(mContext);
	        adapter.alterStringArray(mJoinItems, joinSummary);
	        mDialog = DialogUtils.showSingleChoiceDialogWithAdapter(mContext, R.string.privacy_set_title, adapter, checkedItem, joinItemClickListener, null, negativeListener);
		}
	};
	
	public int getPrivacy() {
		return mPrivacy;
	}
	public int getApprove() {
		return mApprove;
	}
	public int getJoinPermission() {
		return mJoinPermission;
	}
	public int getInvitePermission() {
		return mInvitePermission;
	}

    public boolean getSendEmailStatus() {
        return mSendEmailStatus;
    }

    public boolean getSendSmsStatus() {
        return mSendSmsStatus;
    }

	public class SignChoiceAdapter extends BaseAdapter {
		private Context mSignChoiceAdapterContext;
		private String[] mTitleString ;
		private String[] mSummaryString ;
		public SignChoiceAdapter(Context context) {
			mSignChoiceAdapterContext = context;
	    }

		public void alterStringArray(String[] titles, String[] summary) {
			mTitleString = titles;
			mSummaryString = summary;
		}
		
		@Override
		public int getCount() {
			if(mTitleString != null && mTitleString.length > 0) {
				return mTitleString.length;
			}else {
				return 0;
			}
		}

		@Override
		public SpannableStringBuilder getItem(int position) {
			if(mSummaryString != null && mSummaryString.length > 0) {
				return StringUtil.setRadioBtnSpannable(mSignChoiceAdapterContext, mTitleString[position] + "\r\n" + mSummaryString[position]);
			}else {
				return new SpannableStringBuilder(mTitleString[position]);
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CheckedTextView tv = (CheckedTextView) LayoutInflater.from(mSignChoiceAdapterContext).inflate(android.R.layout.simple_list_item_single_choice, null);
			tv.setMaxLines(3);
			tv.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int) mSignChoiceAdapterContext.getResources().getDimension(R.dimen.privacy_dialog_item_height)));
			tv.setText(getItem(position));
			return tv;
		}
	}
}

